package com.roadlens.app.ui.viewmodel

import android.graphics.Bitmap
import com.roadlens.app.ai.RoadDefectDetector
import com.roadlens.app.domain.model.DatasetLabSubMode
import com.roadlens.app.domain.model.DetectionResult
import com.roadlens.app.domain.model.RoadDetection
import com.roadlens.app.domain.model.ScanOperationalMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ScanViewModel business logic, video deduplication, and playback controls.
 * Strictly under 200 lines per project engineering rules.
 * Covers positive, negative, and boundary-condition scenarios (KPI 4.7).
 */
class ScanViewModelTest {

    private class FakeDetector : RoadDefectDetector {
        var detectionsToReturn: List<RoadDetection> = emptyList()

        override fun detectOnly(bitmap: Bitmap): List<RoadDetection> = detectionsToReturn
        override fun detect(bitmap: Bitmap): DetectionResult {
            return DetectionResult(annotatedBitmap = bitmap, detections = detectionsToReturn)
        }
        override fun close() = Unit
    }

    private lateinit var fakeDetector: FakeDetector
    private lateinit var viewModel: ScanViewModel

    @Before
    fun setUp() {
        fakeDetector = FakeDetector()
        viewModel = ScanViewModel(fakeDetector)
    }

    @Test
    fun setOperationalMode_updatesOperationalModeState() {
        viewModel.setOperationalMode(ScanOperationalMode.DATASET_LAB)
        assertEquals(ScanOperationalMode.DATASET_LAB, viewModel.uiState.value.operationalMode)
        viewModel.setOperationalMode(ScanOperationalMode.LIVE_SCAN)
        assertEquals(ScanOperationalMode.LIVE_SCAN, viewModel.uiState.value.operationalMode)
    }

    @Test
    fun setLabSubMode_updatesDatasetLabSubMode() {
        viewModel.setLabSubMode(DatasetLabSubMode.VIDEO)
        assertEquals(DatasetLabSubMode.VIDEO, viewModel.uiState.value.labSubMode)
        viewModel.setLabSubMode(DatasetLabSubMode.IMAGE)
        assertEquals(DatasetLabSubMode.IMAGE, viewModel.uiState.value.labSubMode)
    }

    @Test
    fun toggleVideoPlay_switchesPlayingState() {
        assertFalse(viewModel.uiState.value.videoState.isPlaying)
        viewModel.toggleVideoPlay()
        assertTrue(viewModel.uiState.value.videoState.isPlaying)
        viewModel.toggleVideoPlay()
        assertFalse(viewModel.uiState.value.videoState.isPlaying)
    }

    @Test
    fun seekVideo_updatesPositionAndTarget() {
        viewModel.seekVideo(15000L)
        assertEquals(15000L, viewModel.uiState.value.videoState.currentPositionMs)
        assertEquals(15000L, viewModel.uiState.value.videoState.seekToTargetMs)
    }

    @Test
    fun stepVideo_forwardAndBackward_clampsWithinDuration() {
        // Set duration to 10,000ms
        viewModel.updatePlaybackPosition(isPlaying = true, currentMs = 5000L, durationMs = 10000L)

        // Step forward +3000ms -> 8000ms
        viewModel.stepVideo(3000L)
        assertEquals(8000L, viewModel.uiState.value.videoState.currentPositionMs)

        // Step forward +5000ms -> should clamp to duration (10000ms)
        viewModel.stepVideo(5000L)
        assertEquals(10000L, viewModel.uiState.value.videoState.currentPositionMs)

        // Step backward -15000ms -> should clamp to 0ms
        viewModel.stepVideo(-15000L)
        assertEquals(0L, viewModel.uiState.value.videoState.currentPositionMs)
    }

    @Test
    fun analyzeImage_nullBitmap_setsErrorMessage() {
        // Negative scenario
        viewModel.analyzeImage(null)
        val state = viewModel.uiState.value.imageState
        assertEquals("Could not open the selected image.", state.statusMessage)
        assertFalse(state.isScanning)
    }

    @Test
    fun processAnalyzedFrame_singleDefect_addsToTimeline() {
        val detection = RoadDetection(
            classIndex = 3, className = "Pothole (D40)", confidence = 0.85f,
            left = 100f, top = 100f, right = 200f, bottom = 200f,
            imageWidth = 640, imageHeight = 480
        )

        viewModel.processAnalyzedFrame(listOf(detection), frameTimeMs = 1000L)

        val state = viewModel.uiState.value.videoState
        assertEquals(1, state.defectTimeline.size)
        assertEquals("Pothole (D40)", state.defectTimeline.first().className)
        assertEquals(1, state.cumulativePotholes)
        assertEquals(0, state.cumulativeCracks)
    }

    @Test
    fun processAnalyzedFrame_duplicateDetectionWithinThreshold_updatesConfidenceWithoutDuplicating() {
        val frame1 = RoadDetection(
            classIndex = 3, className = "Pothole (D40)", confidence = 0.70f,
            left = 100f, top = 100f, right = 200f, bottom = 200f, imageWidth = 640, imageHeight = 480
        )
        val frame2 = RoadDetection(
            classIndex = 3, className = "Pothole (D40)", confidence = 0.92f,
            left = 102f, top = 102f, right = 202f, bottom = 202f, imageWidth = 640, imageHeight = 480
        )

        viewModel.processAnalyzedFrame(listOf(frame1), frameTimeMs = 1000L)
        viewModel.processAnalyzedFrame(listOf(frame2), frameTimeMs = 1200L)

        val timeline = viewModel.uiState.value.videoState.defectTimeline
        assertEquals(1, timeline.size)
        assertEquals(0.92f, timeline.first().confidence, 0.001f)
    }

    @Test
    fun processAnalyzedFrame_distinctDefects_addsBothRecords() {
        val pothole = RoadDetection(
            classIndex = 3, className = "Pothole (D40)", confidence = 0.88f,
            left = 50f, top = 50f, right = 150f, bottom = 150f, imageWidth = 640, imageHeight = 480
        )
        val crack = RoadDetection(
            classIndex = 0, className = "Longitudinal Crack (D00)", confidence = 0.75f,
            left = 400f, top = 300f, right = 500f, bottom = 400f, imageWidth = 640, imageHeight = 480
        )

        viewModel.processAnalyzedFrame(listOf(pothole, crack), frameTimeMs = 1000L)

        val state = viewModel.uiState.value.videoState
        assertEquals(2, state.defectTimeline.size)
        assertEquals(1, state.cumulativePotholes)
        assertEquals(1, state.cumulativeCracks)
    }

    @Test
    fun resetVideo_clearsAllTimelineAndCounters() {
        val detection = RoadDetection(
            classIndex = 3, className = "Pothole (D40)", confidence = 0.85f,
            left = 100f, top = 100f, right = 200f, bottom = 200f, imageWidth = 640, imageHeight = 480
        )
        viewModel.processAnalyzedFrame(listOf(detection), frameTimeMs = 500L)
        assertTrue(viewModel.uiState.value.videoState.defectTimeline.isNotEmpty())

        viewModel.resetVideo()

        val state = viewModel.uiState.value.videoState
        assertTrue(state.defectTimeline.isEmpty())
        assertEquals(0, state.cumulativePotholes)
        assertEquals(0, state.cumulativeCracks)
        assertEquals(0, state.totalFramesScanned)
    }
}
