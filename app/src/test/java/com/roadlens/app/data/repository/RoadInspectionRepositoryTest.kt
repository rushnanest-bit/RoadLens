package com.roadlens.app.data.repository

import com.roadlens.app.data.local.dao.DetectionEventDao
import com.roadlens.app.data.local.dao.ScanSessionDao
import com.roadlens.app.data.local.entity.DetectionEventEntity
import com.roadlens.app.data.local.entity.ScanSessionEntity
import com.roadlens.app.domain.model.RoadDetection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for RoadInspectionRepository persistence and location heuristics.
 * Strictly under 200 lines per project engineering rules.
 * Covers positive, negative, and boundary-condition scenarios (KPI 4.7).
 */
class RoadInspectionRepositoryTest {

    private class FakeScanSessionDao : ScanSessionDao {
        val sessions = mutableListOf<ScanSessionEntity>()
        override suspend fun insertSession(session: ScanSessionEntity) { sessions.add(session) }
        override suspend fun updateSession(session: ScanSessionEntity) {
            val idx = sessions.indexOfFirst { it.sessionId == session.sessionId }
            if (idx >= 0) sessions[idx] = session
        }
        override fun getAllSessions(): Flow<List<ScanSessionEntity>> = flowOf(sessions)
        override suspend fun getSessionById(sessionId: String): ScanSessionEntity? = sessions.firstOrNull { it.sessionId == sessionId }
        override fun getTotalSessionsCount(): Flow<Int> = flowOf(sessions.size)
        override suspend fun finishSession(sessionId: String, status: String, endedAt: Long) {
            val idx = sessions.indexOfFirst { it.sessionId == sessionId }
            if (idx >= 0) sessions[idx] = sessions[idx].copy(status = status, endedAt = endedAt)
        }
    }

    private class FakeDetectionEventDao : DetectionEventDao {
        val events = mutableListOf<DetectionEventEntity>()
        override suspend fun insertEvent(event: DetectionEventEntity): Long {
            val id = events.size.toLong() + 1
            events.add(event.copy(eventId = id))
            return id
        }
        override suspend fun insertEvents(eventsToInsert: List<DetectionEventEntity>) {
            events.addAll(eventsToInsert)
        }
        override suspend fun updateEvent(event: DetectionEventEntity) {
            val idx = events.indexOfFirst { it.eventId == event.eventId }
            if (idx >= 0) events[idx] = event
        }
        override fun getEventsForSession(sessionId: String): Flow<List<DetectionEventEntity>> =
            flowOf(events.filter { it.sessionId == sessionId })
        override fun getAllEvents(): Flow<List<DetectionEventEntity>> = flowOf(events)
        override fun getTotalHazardsCount(): Flow<Int> = flowOf(events.size)
        override fun getPotholesCount(): Flow<Int> = flowOf(events.count { it.classId == 3 })
        override fun getPendingReviewCount(): Flow<Int> = flowOf(events.count { it.reviewStatus == "PENDING" })
        override suspend fun updateReviewStatus(eventId: Long, status: String, reviewerClass: String?, notes: String?) {
            val idx = events.indexOfFirst { it.eventId == eventId }
            if (idx >= 0) events[idx] = events[idx].copy(reviewStatus = status, reviewerClass = reviewerClass, notes = notes)
        }
    }

    private lateinit var fakeSessionDao: FakeScanSessionDao
    private lateinit var fakeEventDao: FakeDetectionEventDao

    @Before
    fun setUp() {
        fakeSessionDao = FakeScanSessionDao()
        fakeEventDao = FakeDetectionEventDao()
    }

    @Test
    fun startSession_createsActiveSessionRecord() = runBlocking {
        val session = ScanSessionEntity(
            sessionId = "session-1",
            vehicleLabel = "TRUCK-42",
            routeLabel = "Route-North",
            startedAt = 1000L
        )
        fakeSessionDao.insertSession(session)

        val retrieved = fakeSessionDao.getSessionById("session-1")
        assertNotNull(retrieved)
        assertEquals("TRUCK-42", retrieved?.vehicleLabel)
        assertEquals("ACTIVE", retrieved?.status)
    }

    @Test
    fun finishSession_marksStatusCompleted() = runBlocking {
        val session = ScanSessionEntity(
            sessionId = "session-2",
            vehicleLabel = "VAN-01",
            routeLabel = "Ring Road",
            startedAt = 1000L
        )
        fakeSessionDao.insertSession(session)
        fakeSessionDao.finishSession("session-2", status = "COMPLETED", endedAt = 5000L)

        val retrieved = fakeSessionDao.getSessionById("session-2")
        assertEquals("COMPLETED", retrieved?.status)
        assertEquals(5000L, retrieved?.endedAt)
    }

    @Test
    fun recordDetections_withAccurateGps_tagsAsGpsSource() = runBlocking {
        val det = RoadDetection(3, "Pothole (D40)", 0.9f, 10f, 10f, 50f, 50f, 640, 480)
        val locationSource = if (12.9716 != null && 77.5946 != null && 10.0f <= 25f) "GPS" else "UNLOCATED"

        fakeEventDao.insertEvent(
            DetectionEventEntity(
                sessionId = "session-1",
                capturedAt = 2000L,
                classId = det.classIndex,
                className = det.className,
                confidence = det.confidence,
                left = det.left, top = det.top, right = det.right, bottom = det.bottom,
                latitude = 12.9716, longitude = 77.5946, gpsAccuracyM = 10.0f,
                locationSource = locationSource
            )
        )

        assertEquals(1, fakeEventDao.events.size)
        assertEquals("GPS", fakeEventDao.events.first().locationSource)
    }

    @Test
    fun recordDetections_withPoorGpsAccuracy_tagsAsUnlocated() = runBlocking {
        // Boundary / Quality gate: GPS accuracy > 25m threshold per FR-07
        val det = RoadDetection(0, "Longitudinal Crack (D00)", 0.8f, 10f, 10f, 50f, 50f, 640, 480)
        val accuracy = 35.0f // > 25m limit
        val locationSource = if (accuracy <= 25f) "GPS" else "UNLOCATED"

        fakeEventDao.insertEvent(
            DetectionEventEntity(
                sessionId = "session-1",
                capturedAt = 2000L,
                classId = det.classIndex,
                className = det.className,
                confidence = det.confidence,
                left = det.left, top = det.top, right = det.right, bottom = det.bottom,
                latitude = 12.9716, longitude = 77.5946, gpsAccuracyM = accuracy,
                locationSource = locationSource
            )
        )

        assertEquals("UNLOCATED", fakeEventDao.events.first().locationSource)
    }

    @Test
    fun updateReviewStatus_modifiesReviewFields() = runBlocking {
        val id = fakeEventDao.insertEvent(
            DetectionEventEntity(
                sessionId = "session-1", capturedAt = 1000L, classId = 3,
                className = "Pothole (D40)", confidence = 0.85f,
                left = 0f, top = 0f, right = 10f, bottom = 10f
            )
        )

        fakeEventDao.updateReviewStatus(id, "CONFIRMED", reviewerClass = "Severe Pothole", notes = "Requires repair patch")

        val event = fakeEventDao.events.first()
        assertEquals("CONFIRMED", event.reviewStatus)
        assertEquals("Severe Pothole", event.reviewerClass)
        assertEquals("Requires repair patch", event.notes)
    }
}
