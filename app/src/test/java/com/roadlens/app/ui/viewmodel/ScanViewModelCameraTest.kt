package com.roadlens.app.ui.viewmodel

import android.graphics.Bitmap
import com.roadlens.app.ai.RoadDefectDetector
import com.roadlens.app.domain.model.DetectionResult
import com.roadlens.app.domain.model.RoadDetection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for live camera scanning lifecycle, model readiness gating, and pause controls.
 * Strictly under 200 lines per project engineering rules.
 * Covers positive, negative, and boundary condition scenarios (KPI 4.7).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ScanViewModelCameraTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeDetector : RoadDefectDetector {
        var modelLoaded: Boolean = true
        var detectionsToReturn: List<RoadDetection> = emptyList()

        override fun isModelLoaded(): Boolean = modelLoaded
        override fun detectOnly(bitmap: Bitmap): List<RoadDetection> = detectionsToReturn
        override fun detect(bitmap: Bitmap): DetectionResult = DetectionResult(bitmap, detectionsToReturn)
        override fun close() = Unit
    }

    private lateinit var fakeDetector: FakeDetector
    private lateinit var viewModel: ScanViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDetector = FakeDetector()
        viewModel = ScanViewModel(fakeDetector)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun checkModelAvailability_whenLoaded_returnsTrueAndSuccessMessage() {
        fakeDetector.modelLoaded = true
        val ready = viewModel.checkModelAvailability()
        assertTrue(ready)
        assertEquals("Detection model loaded successfully.", viewModel.uiState.value.cameraState.statusMessage)
    }

    @Test
    fun checkModelAvailability_whenNotLoaded_returnsFalseAndErrorMessage() {
        // Negative test scenario
        fakeDetector.modelLoaded = false
        val ready = viewModel.checkModelAvailability()
        assertFalse(ready)
        assertEquals("Detection model loading failed. Cannot start live scan.", viewModel.uiState.value.cameraState.statusMessage)
    }

    @Test
    fun startCameraScan_withVehicleAndStartLocation_activatesSession() = runTest {
        // Positive test scenario
        viewModel.startCameraScan(vehicleLabel = "ka-05-xy-9999", startLocation = "Central Depot")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value.cameraState
        assertTrue(state.isScanning)
        assertFalse(state.isPaused)
        assertEquals("Central Depot", state.startLocation)
        assertEquals("KA-05-XY-9999", viewModel.uiState.value.vehicleRegistration)
        assertTrue(state.statusMessage.contains("Central Depot"))
    }

    @Test
    fun pauseAndResumeCameraScan_togglesPausedState() = runTest {
        viewModel.startCameraScan(vehicleLabel = "KA-01-AB-1234", startLocation = "Depot A")
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.cameraState.isPaused)

        // Pause
        viewModel.pauseCameraScan()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.cameraState.isPaused)
        assertEquals("Scan paused. Camera feed idle.", viewModel.uiState.value.cameraState.statusMessage)

        // Resume
        viewModel.resumeCameraScan()
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.cameraState.isPaused)
        assertTrue(viewModel.uiState.value.cameraState.statusMessage.contains("Scanning as KA-01-AB-1234"))
    }

    @Test
    fun stopCameraScan_withStopLocation_completesSession() = runTest {
        viewModel.startCameraScan(vehicleLabel = "KA-01-AB-1234", startLocation = "Depot A")
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.cameraState.isScanning)

        viewModel.stopCameraScan(stopLocation = "South Terminal")
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value.cameraState
        assertFalse(state.isScanning)
        assertFalse(state.isPaused)
        assertEquals("South Terminal", state.stopLocation)
        assertEquals("Scan session completed at South Terminal.", state.statusMessage)
    }

    @Test
    fun processCameraFrame_whenPaused_ignoresDetections() = runTest {
        // Boundary condition: paused state must discard frames
        viewModel.startCameraScan(vehicleLabel = "KA-01-AB-1234", startLocation = "Depot A")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.pauseCameraScan()
        testDispatcher.scheduler.advanceUntilIdle()

        val det = RoadDetection(3, "Pothole (D40)", 0.9f, 0f, 0f, 10f, 10f, 640, 480)
        viewModel.processCameraFrame(listOf(det), timestampMs = 1000L)
        testDispatcher.scheduler.advanceUntilIdle()

        // No detections accumulated because scan is paused
        assertEquals(0, viewModel.uiState.value.cameraState.totalDetectionsInSession)
    }

    @Test
    fun stopCameraScan_populatesCompletedSummary_andDismissClearsIt() = runTest {
        viewModel.startCameraScan(vehicleLabel = "KA-01-AB-1234", startLocation = "Central Hub")
        testDispatcher.scheduler.advanceUntilIdle()

        val pothole = RoadDetection(3, "Pothole (D40)", 0.9f, 0f, 0f, 10f, 10f, 640, 480)
        val crack = RoadDetection(0, "Longitudinal Crack (D00)", 0.85f, 0f, 0f, 10f, 10f, 640, 480)
        viewModel.processCameraFrame(listOf(pothole, crack), timestampMs = 2000L)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.stopCameraScan(stopLocation = "Airport Depot")
        testDispatcher.scheduler.advanceUntilIdle()

        val summary = viewModel.uiState.value.cameraState.completedSummary
        assertNotNull(summary)
        assertEquals("KA-01-AB-1234", summary?.vehicleLabel)
        assertEquals("Central Hub", summary?.startLocation)
        assertEquals("Airport Depot", summary?.stopLocation)
        assertEquals(2, summary?.totalDetections)
        assertEquals(1, summary?.potholes)
        assertEquals(1, summary?.cracks)

        viewModel.dismissCompletionSummary()
        assertNull(viewModel.uiState.value.cameraState.completedSummary)
    }
}
