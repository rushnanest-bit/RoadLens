package com.roadlens.app.ui.viewmodel

import com.roadlens.app.data.local.dao.DetectionEventDao
import com.roadlens.app.data.local.dao.ScanSessionDao
import com.roadlens.app.data.local.entity.DetectionEventEntity
import com.roadlens.app.data.local.entity.ScanSessionEntity
import com.roadlens.app.data.repository.RoadInspectionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ReportViewModel session reports aggregation and detail inspection.
 * Strictly under 200 lines per project engineering rules.
 * Covers positive, negative, and boundary condition scenarios.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReportViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeSessionDao(private val sessions: List<ScanSessionEntity>) : ScanSessionDao {
        override suspend fun insertSession(session: ScanSessionEntity) = Unit
        override suspend fun updateSession(session: ScanSessionEntity) = Unit
        override fun getAllSessions(): Flow<List<ScanSessionEntity>> = flowOf(sessions)
        override suspend fun getSessionById(sessionId: String): ScanSessionEntity? =
            sessions.firstOrNull { it.sessionId == sessionId }
        override fun getTotalSessionsCount(): Flow<Int> = flowOf(sessions.size)
        override suspend fun finishSession(sessionId: String, status: String, endedAt: Long) = Unit
        override suspend fun finishSessionWithLocation(sessionId: String, status: String, endedAt: Long, stopLocation: String?) = Unit
        override suspend fun updateSessionStatus(sessionId: String, status: String) = Unit
    }

    private class FakeEventDao(private val events: List<DetectionEventEntity>) : DetectionEventDao {
        override suspend fun insertEvent(event: DetectionEventEntity): Long = 1L
        override suspend fun insertEvents(events: List<DetectionEventEntity>) = Unit
        override suspend fun updateEvent(event: DetectionEventEntity) = Unit
        override fun getEventsForSession(sessionId: String): Flow<List<DetectionEventEntity>> =
            flowOf(events.filter { it.sessionId == sessionId })
        override fun getAllEvents(): Flow<List<DetectionEventEntity>> = flowOf(events)
        override fun getTotalHazardsCount(): Flow<Int> = flowOf(events.size)
        override fun getPotholesCount(): Flow<Int> = flowOf(events.count { it.classId == 3 })
        override fun getPendingReviewCount(): Flow<Int> = flowOf(events.count { it.reviewStatus == "PENDING" })
        override suspend fun updateReviewStatus(eventId: Long, status: String, reviewerClass: String?, notes: String?) = Unit
    }

    private val sessionA = ScanSessionEntity(
        sessionId = "sess-1",
        vehicleLabel = "TRUCK-01",
        routeLabel = "North Route",
        startedAt = 1000L
    )

    private val sessionB = ScanSessionEntity(
        sessionId = "sess-2",
        vehicleLabel = "VAN-02",
        routeLabel = "Highway 101",
        startedAt = 2000L
    )

    private val event1 = DetectionEventEntity(
        eventId = 1,
        sessionId = "sess-1",
        capturedAt = 1100L,
        classId = 3, // Pothole
        className = "Pothole (D40)",
        confidence = 0.9f,
        left = 0f,
        top = 0f,
        right = 10f,
        bottom = 10f
    )

    private val event2 = DetectionEventEntity(
        eventId = 2,
        sessionId = "sess-1",
        capturedAt = 1200L,
        classId = 0, // Longitudinal Crack
        className = "Longitudinal Crack (D00)",
        confidence = 0.85f,
        left = 0f,
        top = 0f,
        right = 10f,
        bottom = 10f
    )

    private val event3 = DetectionEventEntity(
        eventId = 3,
        sessionId = "sess-1",
        capturedAt = 1300L,
        classId = 5, // Other hazard
        className = "Manhole Depression",
        confidence = 0.75f,
        left = 0f,
        top = 0f,
        right = 10f,
        bottom = 10f
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun sessionReports_aggregatesPotholesCracksAndOtherCorrectly() = runTest {
        val repo = RoadInspectionRepository(
            injectedSessionDao = FakeSessionDao(listOf(sessionA, sessionB)),
            injectedEventDao = FakeEventDao(listOf(event1, event2, event3))
        )
        val vm = ReportViewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()

        val reports = vm.sessionReports.value
        assertEquals(2, reports.size)

        val reportA = reports.first { it.session.sessionId == "sess-1" }
        assertEquals(3, reportA.totalHazards)
        assertEquals(1, reportA.potholes)
        assertEquals(1, reportA.cracks)
        assertEquals(1, reportA.otherHazards)

        val reportB = reports.first { it.session.sessionId == "sess-2" }
        assertEquals(0, reportB.totalHazards)
        assertEquals(0, reportB.potholes)
        assertEquals(0, reportB.cracks)
        assertEquals(0, reportB.otherHazards)
    }

    @Test
    fun selectSession_withValidSession_updatesSelectedSessionDetail() = runTest {
        val repo = RoadInspectionRepository(
            injectedSessionDao = FakeSessionDao(listOf(sessionA)),
            injectedEventDao = FakeEventDao(listOf(event1, event2))
        )
        val vm = ReportViewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.selectSession(sessionA)
        testDispatcher.scheduler.advanceUntilIdle()

        val detail = vm.selectedSessionDetail.value
        assertNotNull(detail)
        assertEquals("sess-1", detail?.session?.sessionId)
        assertEquals(2, detail?.events?.size)
    }

    @Test
    fun selectSession_withNull_clearsSelectedSessionDetail() = runTest {
        val repo = RoadInspectionRepository(
            injectedSessionDao = FakeSessionDao(listOf(sessionA)),
            injectedEventDao = FakeEventDao(listOf(event1))
        )
        val vm = ReportViewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.selectSession(sessionA)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(vm.selectedSessionDetail.value)

        vm.selectSession(null)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNull(vm.selectedSessionDetail.value)
    }

    @Test
    fun sessionReports_emptySessions_emitsEmptyList() = runTest {
        val repo = RoadInspectionRepository(
            injectedSessionDao = FakeSessionDao(emptyList()),
            injectedEventDao = FakeEventDao(emptyList())
        )
        val vm = ReportViewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.sessionReports.value.isEmpty())
        assertTrue(vm.rawSessions.value.isEmpty())
        assertTrue(vm.rawEvents.value.isEmpty())
    }
}
