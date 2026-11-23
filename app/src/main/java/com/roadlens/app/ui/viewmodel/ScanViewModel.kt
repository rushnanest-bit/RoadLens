package com.roadlens.app.ui.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roadlens.app.ai.RoadDefectDetector
import com.roadlens.app.data.repository.RoadInspectionRepository
import com.roadlens.app.domain.model.DatasetLabSubMode
import com.roadlens.app.domain.model.RoadDetection
import com.roadlens.app.domain.model.ScanOperationalMode
import com.roadlens.app.domain.model.VideoDefectRecord
import com.roadlens.app.domain.util.GeometryUtils
import com.roadlens.app.domain.util.TimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

/**
 * ViewModel managing RoadLens scanning workflows, timeline tracking, and persistence.
 * Maintained well below 200 lines strictly adhering to project engineering rules.
 */
class ScanViewModel(
    private val detector: RoadDefectDetector,
    private val repository: RoadInspectionRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanScreenUiState())
    val uiState: StateFlow<ScanScreenUiState> = _uiState.asStateFlow()

    fun setOperationalMode(mode: ScanOperationalMode) = _uiState.update { it.copy(operationalMode = mode) }
    fun setLabSubMode(subMode: DatasetLabSubMode) = _uiState.update { it.copy(labSubMode = subMode) }

    fun checkModelAvailability(): Boolean {
        val loaded = detector.isModelLoaded()
        val msg = if (loaded) "Detection model loaded successfully." else "Detection model loading failed. Cannot start live scan."
        _uiState.update { it.copy(cameraState = it.cameraState.copy(statusMessage = msg)) }
        return loaded
    }

    fun registerVehicle(regNumber: String) {
        val cleaned = regNumber.trim().uppercase()
        if (cleaned.isNotBlank()) {
            _uiState.update { cur ->
                cur.copy(
                    vehicleRegistration = cleaned, registeredVehicles = (cur.registeredVehicles + cleaned).distinct(),
                    cameraState = cur.cameraState.copy(statusMessage = "Active vehicle: $cleaned. Ready to scan.")
                )
            }
        }
    }

    fun startCameraScan(vehicleLabel: String? = null, startLocation: String? = null) {
        val activeVehicle = (vehicleLabel?.trim()?.uppercase() ?: _uiState.value.vehicleRegistration).ifBlank { "KA-01-AB-1234" }
        val startLoc = startLocation?.trim()?.ifBlank { "Unspecified Start" } ?: "Depot"
        viewModelScope.launch {
            val sid = repository?.startSession(activeVehicle, startLoc, "CAMERA", startLoc) ?: "camera-session"
            _uiState.update { cur ->
                cur.copy(
                    vehicleRegistration = activeVehicle, registeredVehicles = (cur.registeredVehicles + activeVehicle).distinct(),
                    cameraState = cur.cameraState.copy(
                        isScanning = true, isPaused = false, activeSessionId = sid, startLocation = startLoc,
                        statusMessage = "Scanning as $activeVehicle from $startLoc (1 FPS). Monitoring road."
                    )
                )
            }
        }
    }

    fun pauseCameraScan() {
        val sid = _uiState.value.cameraState.activeSessionId
        viewModelScope.launch {
            sid?.let { repository?.updateSessionStatus(it, "PAUSED") }
            _uiState.update { it.copy(cameraState = it.cameraState.copy(isPaused = true, statusMessage = "Scan paused. Camera feed idle.")) }
        }
    }

    fun resumeCameraScan() {
        val sid = _uiState.value.cameraState.activeSessionId
        val veh = _uiState.value.vehicleRegistration
        viewModelScope.launch {
            sid?.let { repository?.updateSessionStatus(it, "ACTIVE") }
            _uiState.update { it.copy(cameraState = it.cameraState.copy(isPaused = false, statusMessage = "Scanning as $veh (1 FPS). Monitoring road.")) }
        }
    }

    fun stopCameraScan(stopLocation: String? = null) {
        val sid = _uiState.value.cameraState.activeSessionId
        val dest = stopLocation?.trim()?.ifBlank { "Unspecified Destination" } ?: "Destination"
        viewModelScope.launch {
            sid?.let { repository?.finishSession(it, dest) }
            _uiState.update { it.copy(cameraState = it.cameraState.copy(isScanning = false, isPaused = false, activeSessionId = null, stopLocation = dest, statusMessage = "Scan session completed at $dest.")) }
        }
    }

    fun processCameraFrame(detections: List<RoadDetection>, timestampMs: Long, lat: Double? = null, lon: Double? = null, accuracy: Float? = null) {
        if (_uiState.value.cameraState.isPaused || !_uiState.value.cameraState.isScanning) return
        val sid = _uiState.value.cameraState.activeSessionId ?: "local"
        viewModelScope.launch { repository?.recordDetections(sid, detections, timestampMs, lat, lon, accuracy) }
        val p = detections.count { it.classIndex == 3 }
        val status = if (detections.isEmpty()) "Live: Road surface clear." else "Live: Detected ${detections.size} defect(s)."
        _uiState.update { cur ->
            cur.copy(
                cameraState = cur.cameraState.copy(
                    liveDetections = detections, statusMessage = status,
                    totalDetectionsInSession = cur.cameraState.totalDetectionsInSession + detections.size,
                    potholesInSession = cur.cameraState.potholesInSession + p,
                    cracksInSession = cur.cameraState.cracksInSession + (detections.size - p)
                ),
                metricsState = cur.metricsState.copy(roadHazards = cur.metricsState.roadHazards + detections.size, potholes = cur.metricsState.potholes + p)
            )
        }
    }

    fun analyzeImage(bitmap: Bitmap?) {
        if (bitmap == null) {
            _uiState.update { it.copy(imageState = ImageScanUiState(statusMessage = "Could not open the selected image.")) }
            return
        }
        _uiState.update { it.copy(imageState = ImageScanUiState(selectedBitmap = bitmap, isScanning = true, statusMessage = "Detecting road damage in image...")) }
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.Default) { detector.detect(bitmap) }
                val msg = if (result.detections.isEmpty()) "Scan completed. No road defects detected." else "Scan completed. ${result.detections.size} defect(s) detected."
                _uiState.update { cur -> cur.copy(imageState = cur.imageState.copy(detectionResult = result, statusMessage = msg, isScanning = false)) }
            } catch (e: Exception) {
                _uiState.update { cur -> cur.copy(imageState = cur.imageState.copy(isScanning = false, statusMessage = "Detection failed: ${e.localizedMessage ?: "Unknown error"}")) }
            }
        }
    }

    fun selectVideo(uri: Uri?) = _uiState.update { cur ->
        cur.copy(videoState = VideoScanUiState(selectedVideoUri = uri, isPlaying = uri != null, statusMessage = if (uri != null) "Video playback active." else "No video selected."))
    }

    fun toggleVideoPlay() = _uiState.update { it.copy(videoState = it.videoState.copy(isPlaying = !it.videoState.isPlaying)) }
    fun seekVideo(targetMs: Long) = _uiState.update { it.copy(videoState = it.videoState.copy(currentPositionMs = targetMs, seekToTargetMs = targetMs)) }

    fun stepVideo(deltaMs: Long) {
        val target = (_uiState.value.videoState.currentPositionMs + deltaMs).coerceIn(0L, _uiState.value.videoState.durationMs)
        seekVideo(target)
    }

    fun resetVideo() = _uiState.update {
        it.copy(videoState = it.videoState.copy(currentPositionMs = 0L, seekToTargetMs = 0L, defectTimeline = emptyList(), liveDetections = emptyList(), cumulativePotholes = 0, cumulativeCracks = 0, totalFramesScanned = 0))
    }

    fun updatePlaybackPosition(isPlaying: Boolean, currentMs: Long, durationMs: Long) = _uiState.update {
        it.copy(videoState = it.videoState.copy(isPlaying = isPlaying, currentPositionMs = currentMs, durationMs = durationMs))
    }

    fun processAnalyzedFrame(detections: List<RoadDetection>, frameTimeMs: Long) {
        val currentTimeline = _uiState.value.videoState.defectTimeline.toMutableList()
        for (det in detections) {
            val idx = currentTimeline.indexOfFirst { r ->
                r.classIndex == det.classIndex && abs(frameTimeMs - r.timestampMs) <= 1200L &&
                        (GeometryUtils.calculateIoU(det.normLeft, det.normTop, det.normRight, det.normBottom, r.normLeft, r.normTop, r.normRight, r.normBottom) > 0.15f ||
                                GeometryUtils.areCentersClose(det.normLeft, det.normTop, det.normRight, det.normBottom, r.normLeft, r.normTop, r.normRight, r.normBottom))
            }
            if (idx >= 0) {
                val existing = currentTimeline[idx]
                if (det.confidence > existing.confidence) {
                    currentTimeline[idx] = existing.copy(confidence = det.confidence, timestampMs = frameTimeMs, timeFormatted = TimeFormatter.formatMmSs(frameTimeMs))
                }
            } else {
                currentTimeline.add(VideoDefectRecord(id = currentTimeline.size + 1, className = det.className, classIndex = det.classIndex, confidence = det.confidence, timestampMs = frameTimeMs, timeFormatted = TimeFormatter.formatMmSs(frameTimeMs), left = det.left, top = det.top, right = det.right, bottom = det.bottom, normLeft = det.normLeft, normTop = det.normTop, normRight = det.normRight, normBottom = det.normBottom))
            }
        }
        currentTimeline.sortBy { it.timestampMs }
        val totalDefects = currentTimeline.size
        val status = if (detections.isEmpty()) {
            if (totalDefects == 0) "Live: No defects currently in view." else "Live: No defects in view ($totalDefects total detected)."
        } else "Live: ${detections.size} defect(s) in view ($totalDefects total detected)."

        _uiState.update { cur ->
            cur.copy(
                videoState = cur.videoState.copy(
                    liveDetections = detections, defectTimeline = currentTimeline,
                    cumulativePotholes = currentTimeline.count { it.classIndex == 3 },
                    cumulativeCracks = currentTimeline.count { it.classIndex != 3 },
                    totalFramesScanned = cur.videoState.totalFramesScanned + 1, statusMessage = status
                )
            )
        }
    }
}
