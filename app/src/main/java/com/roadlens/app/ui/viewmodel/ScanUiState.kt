package com.roadlens.app.ui.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import com.roadlens.app.domain.model.DetectionResult
import com.roadlens.app.domain.model.RoadDetection
import com.roadlens.app.domain.model.ScanInputMode
import com.roadlens.app.domain.model.VideoDefectRecord

/**
 * Immutable UI state for static image scanning.
 */
data class ImageScanUiState(
    val selectedBitmap: Bitmap? = null,
    val detectionResult: DetectionResult? = null,
    val statusMessage: String = "Choose a road image to start scanning.",
    val isScanning: Boolean = false
)

/**
 * Immutable UI state for continuous video scanning and timeline tracking.
 */
data class VideoScanUiState(
    val selectedVideoUri: Uri? = null,
    val durationMs: Long = 0L,
    val currentPositionMs: Long = 0L,
    val liveDetections: List<RoadDetection> = emptyList(),
    val defectTimeline: List<VideoDefectRecord> = emptyList(),
    val isPlaying: Boolean = false,
    val statusMessage: String = "Choose a road video to start scanning.",
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
 * Unified root UI state for RoadLens scanning features.
 */
data class ScanScreenUiState(
    val inputMode: ScanInputMode = ScanInputMode.IMAGE,
    val imageState: ImageScanUiState = ImageScanUiState(),
    val videoState: VideoScanUiState = VideoScanUiState(),
    val metricsState: OverviewMetricsUiState = OverviewMetricsUiState()
)
