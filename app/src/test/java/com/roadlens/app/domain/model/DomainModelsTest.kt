package com.roadlens.app.domain.model

import com.roadlens.app.data.local.entity.DetectionEventEntity
import com.roadlens.app.data.local.entity.ScanSessionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit tests for domain models, filters, enums, and video defect records.
 * Strictly under 200 lines per project engineering rules.
 */
class DomainModelsTest {

    @Test
    fun mapDefectFilter_enumEntries_haveExpectedTitles() {
        assertEquals("All Hazards", MapDefectFilter.ALL.title)
        assertEquals("Potholes", MapDefectFilter.POTHOLES.title)
        assertEquals("All Cracks", MapDefectFilter.CRACKS_ALL.title)
        assertEquals("Alligator (D20)", MapDefectFilter.ALLIGATOR.title)
        assertEquals("Longitudinal (D00)", MapDefectFilter.LONGITUDINAL.title)
        assertEquals("Transverse (D10)", MapDefectFilter.TRANSVERSE.title)
    }

    @Test
    fun mapStatusFilter_enumEntries_haveExpectedTitles() {
        assertEquals("All", MapStatusFilter.ALL.title)
        assertEquals("Pending", MapStatusFilter.PENDING.title)
        assertEquals("Confirmed", MapStatusFilter.CONFIRMED.title)
        assertEquals("Dismissed", MapStatusFilter.DISMISSED.title)
    }

    @Test
    fun scanOperationalMode_and_DatasetLabSubMode_haveExpectedTitles() {
        assertEquals("Live Fleet Scan", ScanOperationalMode.LIVE_SCAN.title)
        assertEquals("Dataset Lab", ScanOperationalMode.DATASET_LAB.title)
        assertEquals("Static Image", DatasetLabSubMode.IMAGE.title)
        assertEquals("Video Replay", DatasetLabSubMode.VIDEO.title)
    }

    @Test
    fun sessionReportItem_and_SessionDetailReport_instantiateCorrectly() {
        val session = ScanSessionEntity(sessionId = "s1", vehicleLabel = "V1", routeLabel = "R1", startedAt = 100L)
        val event = DetectionEventEntity(
            eventId = 1, sessionId = "s1", capturedAt = 100L, classId = 3, className = "Pothole (D40)",
            confidence = 0.9f, left = 0f, top = 0f, right = 10f, bottom = 10f
        )

        val reportItem = SessionReportItem(
            session = session,
            totalHazards = 5,
            potholes = 2,
            cracks = 2,
            otherHazards = 1
        )
        assertEquals("s1", reportItem.session.sessionId)
        assertEquals(5, reportItem.totalHazards)
        assertEquals(2, reportItem.potholes)
        assertEquals(2, reportItem.cracks)
        assertEquals(1, reportItem.otherHazards)

        val detailReport = SessionDetailReport(session = session, events = listOf(event))
        assertEquals(1, detailReport.events.size)
        assertEquals("Pothole (D40)", detailReport.events.first().className)
    }

    @Test
    fun videoDefectRecord_instantiatesCorrectly() {
        val record = VideoDefectRecord(
            id = 1,
            className = "Pothole (D40)",
            classIndex = 3,
            confidence = 0.92f,
            timestampMs = 5400L,
            timeFormatted = "00:05",
            left = 100f,
            top = 100f,
            right = 200f,
            bottom = 200f,
            normLeft = 0.15f,
            normTop = 0.20f,
            normRight = 0.31f,
            normBottom = 0.41f
        )
        assertNotNull(record)
        assertEquals(1, record.id)
        assertEquals(5400L, record.timestampMs)
        assertEquals("00:05", record.timeFormatted)
        assertEquals(0.92f, record.confidence, 0.001f)
    }
}
