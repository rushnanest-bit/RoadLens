package com.roadlens.app.domain.util

import com.roadlens.app.data.local.entity.DetectionEventEntity
import com.roadlens.app.data.local.entity.ScanSessionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for ReportExportHelper CSV generation and formatting.
 * Strictly under 200 lines per project engineering rules.
 * Covers positive, negative, and boundary condition scenarios.
 */
class ReportExportHelperTest {

    private val sampleSession = ScanSessionEntity(
        sessionId = "sess-01",
        vehicleLabel = "PATROL-07",
        routeLabel = "Main Expressway",
        startedAt = 1000L
    )

    private val sampleEvent = DetectionEventEntity(
        eventId = 101,
        sessionId = "sess-01",
        capturedAt = 1700000000000L,
        classId = 3,
        className = "Pothole (D40)",
        confidence = 0.8876f,
        left = 10f,
        top = 20f,
        right = 50f,
        bottom = 60f,
        latitude = 12.9716,
        longitude = 77.5946,
        gpsAccuracyM = 4.5f,
        locationSource = "GPS",
        reviewStatus = "CONFIRMED"
    )

    @Test
    fun generateCsv_withValidSessionAndEvent_formatsCsvRowCorrectly() {
        val csv = ReportExportHelper.generateCsv(listOf(sampleSession), listOf(sampleEvent))
        val lines = csv.trim().split("\n")

        assertEquals(2, lines.size)
        assertEquals(
            "Event ID,Session ID,Vehicle,Timestamp,Hazard Type,Confidence,Latitude,Longitude,Review Status",
            lines[0].trim()
        )

        val row = lines[1].trim()
        assertTrue(row.startsWith("101,sess-01,\"PATROL-07\","))
        assertTrue(row.contains("\"Pothole (D40)\",0.89,12.9716,77.5946,CONFIRMED"))
    }

    @Test
    fun generateCsv_withEmptyEvents_returnsOnlyHeader() {
        val csv = ReportExportHelper.generateCsv(listOf(sampleSession), emptyList())
        val lines = csv.trim().split("\n")

        assertEquals(1, lines.size)
        assertTrue(lines[0].startsWith("Event ID,Session ID,Vehicle"))
    }

    @Test
    fun generateCsv_withUnmatchedSession_fallsBackToUnknownVehicle() {
        val eventWithUnknownSession = sampleEvent.copy(sessionId = "non-existent-session")
        val csv = ReportExportHelper.generateCsv(listOf(sampleSession), listOf(eventWithUnknownSession))
        val lines = csv.trim().split("\n")

        assertEquals(2, lines.size)
        assertTrue(lines[1].contains("\"Unknown\""))
    }

    @Test
    fun generateCsv_withNullCoordinates_outputsNA() {
        val eventWithNullGps = sampleEvent.copy(latitude = null, longitude = null)
        val csv = ReportExportHelper.generateCsv(listOf(sampleSession), listOf(eventWithNullGps))
        val lines = csv.trim().split("\n")

        assertEquals(2, lines.size)
        assertTrue(lines[1].contains(",N/A,N/A,"))
    }

    @Test
    fun generateCsv_multipleEvents_generatesMultipleRows() {
        val event2 = sampleEvent.copy(
            eventId = 102,
            classId = 0,
            className = "Longitudinal Crack (D00)",
            confidence = 0.65f,
            latitude = 13.0827,
            longitude = 80.2707,
            reviewStatus = "PENDING"
        )

        val csv = ReportExportHelper.generateCsv(listOf(sampleSession), listOf(sampleEvent, event2))
        val lines = csv.trim().split("\n")

        assertEquals(3, lines.size)
        assertTrue(lines[1].contains("101,sess-01"))
        assertTrue(lines[2].contains("102,sess-01"))
        assertTrue(lines[2].contains("0.65,13.0827,80.2707,PENDING"))
    }
}
