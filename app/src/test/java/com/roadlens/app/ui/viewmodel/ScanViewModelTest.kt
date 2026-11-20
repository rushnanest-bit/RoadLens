package com.roadlens.app.ui.viewmodel

import android.graphics.Bitmap
import com.roadlens.app.ai.RoadDefectDetector
import com.roadlens.app.domain.model.DetectionResult
import com.roadlens.app.domain.model.RoadDetection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ScanViewModel business logic, video deduplication, and defect counting.
 * Strictly under 200 lines per project engineering rules.
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
    fun processAnalyzedFrame_singleDefect_addsToTimeline() {
        val detection = RoadDetection(
            classIndex = 3, // Pothole
            className = "Pothole (D40)",
            confidence = 0.85f,
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
        val frame1Detection = RoadDetection(
            classIndex = 3,
            className = "Pothole (D40)",
            confidence = 0.70f,
            left = 100f, top = 100f, right = 200f, bottom = 200f,
            imageWidth = 640, imageHeight = 480
        )

        val frame2Detection = RoadDetection(
            classIndex = 3,
            className = "Pothole (D40)",
            confidence = 0.92f, // higher confidence in next frame
            left = 102f, top = 102f, right = 202f, bottom = 202f, // virtually same position
            imageWidth = 640, imageHeight = 480
        )

        // Process frame 1 at 1000ms
        viewModel.processAnalyzedFrame(listOf(frame1Detection), frameTimeMs = 1000L)
        assertEquals(1, viewModel.uiState.value.videoState.defectTimeline.size)
        assertEquals(0.70f, viewModel.uiState.value.videoState.defectTimeline.first().confidence, 0.001f)

        // Process frame 2 at 1200ms (within 1200ms threshold)
        viewModel.processAnalyzedFrame(listOf(frame2Detection), frameTimeMs = 1200L)

        // Verify deduplication: count must still be 1, but confidence upgraded to 0.92
        val timeline = viewModel.uiState.value.videoState.defectTimeline
        assertEquals(1, timeline.size)
        assertEquals(0.92f, timeline.first().confidence, 0.001f)
    }

    @Test
    fun processAnalyzedFrame_distinctDefects_addsBothRecords() {
        val pothole = RoadDetection(
            classIndex = 3,
            className = "Pothole (D40)",
            confidence = 0.88f,
            left = 50f, top = 50f, right = 150f, bottom = 150f,
            imageWidth = 640, imageHeight = 480
        )

        val crack = RoadDetection(
            classIndex = 0,
            className = "Longitudinal Crack (D00)",
            confidence = 0.75f,
            left = 400f, top = 300f, right = 500f, bottom = 400f,
            imageWidth = 640, imageHeight = 480
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
            classIndex = 3,
            className = "Pothole (D40)",
            confidence = 0.85f,
            left = 100f, top = 100f, right = 200f, bottom = 200f,
            imageWidth = 640, imageHeight = 480
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
