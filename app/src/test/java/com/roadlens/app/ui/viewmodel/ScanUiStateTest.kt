package com.roadlens.app.ui.viewmodel

import com.roadlens.app.domain.model.DatasetLabSubMode
import com.roadlens.app.domain.model.ScanOperationalMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for ScanUiState models, initial state flags, and copy immutability.
 * Strictly under 200 lines per project engineering rules.
 */
class ScanUiStateTest {

    @Test
    fun cameraScanUiState_defaultValues_areCorrect() {
        val state = CameraScanUiState()
        assertFalse(state.isScanning)
        assertFalse(state.isPaused)
        assertNull(state.activeSessionId)
        assertNull(state.startLocation)
        assertNull(state.stopLocation)
        assertTrue(state.liveDetections.isEmpty())
        assertEquals(0, state.totalDetectionsInSession)
        assertEquals(0, state.potholesInSession)
        assertEquals(0, state.cracksInSession)
        assertEquals("Ready to start live road scan.", state.statusMessage)
        assertNull(state.completedSummary)
    }

    @Test
    fun sessionSummaryData_instantiatesCorrectly() {
        val summary = SessionSummaryData(
            sessionId = "sess-100",
            vehicleLabel = "KA-01-AB-1234",
            startLocation = "Origin Hub",
            stopLocation = "Terminal Depot",
            totalDetections = 10,
            potholes = 4,
            cracks = 6,
            completedAt = 1700000000000L
        )
        assertEquals("sess-100", summary.sessionId)
        assertEquals("KA-01-AB-1234", summary.vehicleLabel)
        assertEquals("Origin Hub", summary.startLocation)
        assertEquals("Terminal Depot", summary.stopLocation)
        assertEquals(10, summary.totalDetections)
        assertEquals(4, summary.potholes)
        assertEquals(6, summary.cracks)
        assertEquals(1700000000000L, summary.completedAt)
    }

    @Test
    fun videoScanUiState_defaultValues_areCorrect() {
        val state = VideoScanUiState()
        assertNull(state.selectedVideoUri)
        assertEquals(0L, state.durationMs)
        assertEquals(0L, state.currentPositionMs)
        assertFalse(state.isPlaying)
        assertTrue(state.liveDetections.isEmpty())
        assertTrue(state.defectTimeline.isEmpty())
        assertNull(state.seekToTargetMs)
    }

    @Test
    fun imageScanUiState_defaultValues_areCorrect() {
        val state = ImageScanUiState()
        assertNull(state.selectedBitmap)
        assertNull(state.detectionResult)
        assertFalse(state.isScanning)
    }

    @Test
    fun overviewMetricsUiState_defaultValues_areCorrect() {
        val metrics = OverviewMetricsUiState(
            totalScans = 15,
            roadHazards = 42,
            potholes = 18,
            pendingReview = 5
        )
        assertEquals(15, metrics.totalScans)
        assertEquals(42, metrics.roadHazards)
        assertEquals(18, metrics.potholes)
        assertEquals(5, metrics.pendingReview)
    }

    @Test
    fun scanScreenUiState_defaultValues_andCopyFunctionality() {
        val root = ScanScreenUiState()
        assertEquals(ScanOperationalMode.LIVE_SCAN, root.operationalMode)
        assertEquals(DatasetLabSubMode.IMAGE, root.labSubMode)
        assertEquals("KA-01-AB-1234", root.vehicleRegistration)

        val modified = root.copy(operationalMode = ScanOperationalMode.DATASET_LAB)
        assertEquals(ScanOperationalMode.DATASET_LAB, modified.operationalMode)
    }
}
