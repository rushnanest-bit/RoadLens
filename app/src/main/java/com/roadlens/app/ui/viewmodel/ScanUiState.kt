package com.roadlens.app.ui.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import com.roadlens.app.data.local.entity.ScanSessionEntity
import com.roadlens.app.domain.model.DatasetLabSubMode
import com.roadlens.app.domain.model.DetectionResult
import com.roadlens.app.domain.model.RoadDetection
import com.roadlens.app.domain.model.ScanOperationalMode
import com.roadlens.app.domain.model.VideoDefectRecord

/**
 * Summary data for a completed live inspection session.
 */
data class SessionSummaryData(
    val sessionId: String,
    val vehicleLabel: String,
    val startLocation: String,
    val stopLocation: String,
    val totalDetections: Int,
    val potholes: Int,
    val cracks: Int,
    val completedAt: Long = System.currentTimeMillis()
)

/**
 * Immutable UI state for live CameraX scanning and active session telemetry.
 * Strictly under 200 lines per project engineering rules.
 */
data class CameraScanUiState(
    val isScanning: Boolean = false,
    val isPaused: Boolean = false,
    val activeSessionId: String? = null,
    val startLocation: String? = null,
    val stopLocation: String? = null,
    val liveDetections: List<RoadDetection> = emptyList(),
    val totalDetectionsInSession: Int = 0,
    val potholesInSession: Int = 0,
    val cracksInSession: Int = 0,
    val statusMessage: String = "Ready to start live road scan.",
    val recentSessions: List<ScanSessionEntity> = emptyList(),
    val lastSessionHazards: Int = 0,
    val lastSessionPotholes: Int = 0,
    val lastSessionCracks: Int = 0,
    val completedSummary: SessionSummaryData? = null
)

/**
 * Immutable UI state for static image scanning in the Dataset Lab.
 */
data class ImageScanUiState(
    val selectedBitmap: Bitmap? = null,
    val detectionResult: DetectionResult? = null,
    val statusMessage: String = "Select a test road photo to run benchmark inference.",
    val isScanning: Boolean = false
)

/**
 * Immutable UI state for continuous video scanning and timeline tracking in the Dataset Lab.
 */
data class VideoScanUiState(
    val selectedVideoUri: Uri? = null,
    val durationMs: Long = 0L,
    val currentPositionMs: Long = 0L,
    val liveDetections: List<RoadDetection> = emptyList(),
    val defectTimeline: List<VideoDefectRecord> = emptyList(),
    val isPlaying: Boolean = false,
    val statusMessage: String = "Select a road video file to replay detections.",
    val cumulativePotholes: Int = 0,
    val cumulativeCracks: Int = 0,
    val totalFramesScanned: Int = 0,
    val seekToTargetMs: Long? = null
)

/**
 * Overview statistics displayed on the home dashboard.
 */
data class OverviewMetricsUiState(
    val totalScans: Int = 0,
    val roadHazards: Int = 0,
    val potholes: Int = 0,
    val pendingReview: Int = 0
)

/**
 * Unified root UI state for RoadLens scanning and evaluation features.
 */
data class ScanScreenUiState(
    val operationalMode: ScanOperationalMode = ScanOperationalMode.LIVE_SCAN,
    val labSubMode: DatasetLabSubMode = DatasetLabSubMode.IMAGE,
    val vehicleRegistration: String = "KA-01-AB-1234",
    val registeredVehicles: List<String> = listOf("KA-01-AB-1234"),
    val cameraState: CameraScanUiState = CameraScanUiState(),
    val imageState: ImageScanUiState = ImageScanUiState(),
    val videoState: VideoScanUiState = VideoScanUiState(),
    val metricsState: OverviewMetricsUiState = OverviewMetricsUiState()
)
