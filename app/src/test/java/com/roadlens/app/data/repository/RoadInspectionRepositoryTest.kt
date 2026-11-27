package com.roadlens.app.data.repository

import com.roadlens.app.data.local.dao.DetectionEventDao
import com.roadlens.app.data.local.dao.ScanSessionDao
import com.roadlens.app.data.local.entity.DetectionEventEntity
import com.roadlens.app.data.local.entity.ScanSessionEntity
import com.roadlens.app.domain.model.RoadDetection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for RoadInspectionRepository persistence, flows, and GPS heuristics.
 * Strictly under 200 lines per project engineering rules.
 * Covers positive, negative, and boundary-condition scenarios.
 */
class RoadInspectionRepositoryTest {

    private class FakeScanSessionDao : ScanSessionDao {
        val sessions = mutableListOf<ScanSessionEntity>()
        override suspend fun insertSession(session: ScanSessionEntity) { sessions.add(session) }
        override suspend fun updateSession(session: ScanSessionEntity) {
            val idx = sessions.indexOfFirst { it.sessionId == session.sessionId }
            if (idx >= 0) sessions[idx] = session
        }
        override fun getAllSessions(): Flow<List<ScanSessionEntity>> = kotlinx.coroutines.flow.flow { emit(sessions.toList()) }
        override suspend fun getSessionById(sessionId: String): ScanSessionEntity? =
            sessions.firstOrNull { it.sessionId == sessionId }
        override fun getTotalSessionsCount(): Flow<Int> = kotlinx.coroutines.flow.flow { emit(sessions.size) }
        override suspend fun finishSession(sessionId: String, status: String, endedAt: Long) {
            val idx = sessions.indexOfFirst { it.sessionId == sessionId }
            if (idx >= 0) sessions[idx] = sessions[idx].copy(status = status, endedAt = endedAt)
        }
        override suspend fun finishSessionWithLocation(sessionId: String, status: String, endedAt: Long, stopLocation: String?) {
            val idx = sessions.indexOfFirst { it.sessionId == sessionId }
            if (idx >= 0) sessions[idx] = sessions[idx].copy(status = status, endedAt = endedAt, stopLocation = stopLocation)
        }
        override suspend fun updateSessionStatus(sessionId: String, status: String) {
            val idx = sessions.indexOfFirst { it.sessionId == sessionId }
            if (idx >= 0) sessions[idx] = sessions[idx].copy(status = status)
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
            kotlinx.coroutines.flow.flow { emit(events.filter { it.sessionId == sessionId }) }
        override fun getAllEvents(): Flow<List<DetectionEventEntity>> = kotlinx.coroutines.flow.flow { emit(events.toList()) }
        override fun getTotalHazardsCount(): Flow<Int> = kotlinx.coroutines.flow.flow { emit(events.size) }
        override fun getPotholesCount(): Flow<Int> = kotlinx.coroutines.flow.flow { emit(events.count { it.classId == 3 }) }
        override fun getPendingReviewCount(): Flow<Int> = kotlinx.coroutines.flow.flow { emit(events.count { it.reviewStatus == "PENDING" }) }
        override suspend fun updateReviewStatus(eventId: Long, status: String, reviewerClass: String?, notes: String?) {
            val idx = events.indexOfFirst { it.eventId == eventId }
            if (idx >= 0) events[idx] = events[idx].copy(reviewStatus = status, reviewerClass = reviewerClass, notes = notes)
        }
    }

    private lateinit var fakeSessionDao: FakeScanSessionDao
    private lateinit var fakeEventDao: FakeDetectionEventDao
    private lateinit var repository: RoadInspectionRepository

    @Before
    fun setUp() {
        fakeSessionDao = FakeScanSessionDao()
        fakeEventDao = FakeDetectionEventDao()
        repository = RoadInspectionRepository(
            injectedSessionDao = fakeSessionDao,
            injectedEventDao = fakeEventDao
        )
    }

    @Test
    fun startSession_createsActiveSessionRecord() = runBlocking {
        val sessionId = repository.startSession(
            vehicleLabel = "TRUCK-42",
            routeLabel = "Route-North",
            sourceMode = "CAMERA",
            startLocation = "Depot 1"
        )
        assertNotNull(sessionId)
        assertEquals(1, fakeSessionDao.sessions.size)
        val s = fakeSessionDao.sessions.first()
        assertEquals(sessionId, s.sessionId)
        assertEquals("TRUCK-42", s.vehicleLabel)
        assertEquals("ACTIVE", s.status)
        assertEquals("Depot 1", s.startLocation)
    }

    @Test
    fun finishSession_marksStatusCompleted() = runBlocking {
        val sessionId = repository.startSession("VAN-01", "Ring Road", "VIDEO")
        repository.finishSession(sessionId)

        val s = fakeSessionDao.sessions.first()
        assertEquals("COMPLETED", s.status)
        assertTrue(s.endedAt != null && s.endedAt > 0L)
    }

    @Test
    fun finishSession_withStopLocation_savesDestination() = runBlocking {
        val sessionId = repository.startSession("VAN-02", "Expressway", "CAMERA")
        repository.finishSession(sessionId, stopLocation = "Airport Hub")

        val s = fakeSessionDao.sessions.first()
        assertEquals("COMPLETED", s.status)
        assertEquals("Airport Hub", s.stopLocation)
    }

    @Test
    fun updateSessionStatus_modifiesStatus() = runBlocking {
        val sessionId = repository.startSession("VAN-03", "Downtown", "CAMERA")
        repository.updateSessionStatus(sessionId, "PAUSED")

        assertEquals("PAUSED", fakeSessionDao.sessions.first().status)
    }

    @Test
    fun recordDetections_withAccurateGps_tagsAsGpsSource() = runBlocking {
        val det = RoadDetection(3, "Pothole (D40)", 0.9f, 10f, 10f, 50f, 50f, 640, 480)
        repository.recordDetections("sess-1", listOf(det), 1000L, 12.9716, 77.5946, 10.0f)

        assertEquals(1, fakeEventDao.events.size)
        val e = fakeEventDao.events.first()
        assertEquals("GPS", e.locationSource)
        assertEquals(3, e.classId)
        assertEquals(12.9716, e.latitude!!, 0.0001)
    }

    @Test
    fun recordDetections_withPoorAccuracyOrNullCoordinates_tagsUnlocated() = runBlocking {
        val det = RoadDetection(0, "Longitudinal Crack (D00)", 0.8f, 10f, 10f, 50f, 50f, 640, 480)
        // Case 1: Inaccurate GPS (> 25m threshold)
        repository.recordDetections("sess-1", listOf(det), 1000L, 12.9716, 77.5946, 35.0f)
        assertEquals("UNLOCATED", fakeEventDao.events[0].locationSource)

        // Case 2: Null coordinates
        repository.recordDetections("sess-1", listOf(det), 1000L, null, null, null)
        assertEquals("UNLOCATED", fakeEventDao.events[1].locationSource)

        // Case 3: Empty detections list does nothing
        repository.recordDetections("sess-1", emptyList(), 1000L, 12.9716, 77.5946, 5.0f)
        assertEquals(2, fakeEventDao.events.size)
    }

    @Test
    fun updateReview_modifiesReviewFields() = runBlocking {
        val det = RoadDetection(3, "Pothole (D40)", 0.85f, 0f, 0f, 10f, 10f, 640, 480)
        repository.recordDetections("sess-1", listOf(det), 1000L)
        fakeEventDao.events[0] = fakeEventDao.events[0].copy(eventId = 99L)

        repository.updateReview(99L, "CONFIRMED", "Severe Pothole", "Patch scheduled")
        val e = fakeEventDao.events.first()
        assertEquals("CONFIRMED", e.reviewStatus)
        assertEquals("Severe Pothole", e.reviewerClass)
        assertEquals("Patch scheduled", e.notes)
    }

    @Test
    fun flowsAndQueries_delegateCorrectlyToDaos() = runBlocking {
        val sessionId = repository.startSession("CAR-1", "Local", "CAMERA")
        val det = RoadDetection(3, "Pothole (D40)", 0.9f, 0f, 0f, 10f, 10f, 640, 480)
        repository.recordDetections(sessionId, listOf(det), 1000L)

        assertEquals(1, repository.getAllSessions().first().size)
        assertEquals(1, repository.getAllEvents().first().size)
        assertEquals(1, repository.getEventsForSession(sessionId).first().size)
        assertEquals(1, repository.totalSessions.first())
        assertEquals(1, repository.totalHazards.first())
        assertEquals(1, repository.totalPotholes.first())
        assertEquals(1, repository.pendingReviews.first())
    }

    @Test(expected = IllegalStateException::class)
    fun constructor_withoutDatabaseOrDaos_throwsIllegalStateException() {
        RoadInspectionRepository(null, null, null)
    }
}
