package com.roadlens.app.ui.viewmodel

import android.graphics.Bitmap
import com.roadlens.app.ai.RoadDefectDetector
import com.roadlens.app.data.local.dao.DetectionEventDao
import com.roadlens.app.data.local.dao.ScanSessionDao
import com.roadlens.app.data.local.entity.DetectionEventEntity
import com.roadlens.app.data.local.entity.ScanSessionEntity
import com.roadlens.app.data.repository.RoadInspectionRepository
import com.roadlens.app.domain.model.DetectionResult
import com.roadlens.app.domain.model.RoadDetection
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
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ScanViewModel interaction with RoadInspectionRepository streams.
 * Strictly under 200 lines per project engineering rules.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ScanViewModelRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()

    private class StubDetector : RoadDefectDetector {
        override fun detectOnly(bitmap: Bitmap): List<RoadDetection> = emptyList()
        override fun detect(bitmap: Bitmap): DetectionResult = DetectionResult(bitmap, emptyList())
        override fun close() = Unit
    }

    private class SimpleSessionDao(val list: List<ScanSessionEntity>) : ScanSessionDao {
        override suspend fun insertSession(session: ScanSessionEntity) = Unit
        override suspend fun updateSession(session: ScanSessionEntity) = Unit
        override fun getAllSessions(): Flow<List<ScanSessionEntity>> = flowOf(list)
        override suspend fun getSessionById(sessionId: String) = list.firstOrNull()
        override fun getTotalSessionsCount(): Flow<Int> = flowOf(list.size)
        override suspend fun finishSession(sessionId: String, status: String, endedAt: Long) = Unit
        override suspend fun finishSessionWithLocation(sessionId: String, status: String, endedAt: Long, stopLocation: String?) = Unit
        override suspend fun updateSessionStatus(sessionId: String, status: String) = Unit
    }

    private class SimpleEventDao(val list: List<DetectionEventEntity>) : DetectionEventDao {
        override suspend fun insertEvent(event: DetectionEventEntity) = 1L
        override suspend fun insertEvents(events: List<DetectionEventEntity>) = Unit
        override suspend fun updateEvent(event: DetectionEventEntity) = Unit
        override fun getEventsForSession(sessionId: String): Flow<List<DetectionEventEntity>> = flowOf(list)
        override fun getAllEvents(): Flow<List<DetectionEventEntity>> = flowOf(list)
        override fun getTotalHazardsCount(): Flow<Int> = flowOf(list.size)
        override fun getPotholesCount(): Flow<Int> = flowOf(list.count { it.classId == 3 })
        override fun getPendingReviewCount(): Flow<Int> = flowOf(list.count { it.reviewStatus == "PENDING" })
        override suspend fun updateReviewStatus(eventId: Long, status: String, reviewerClass: String?, notes: String?) = Unit
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_withRepository_streamsOverviewMetricsAndRecentSessions() = runTest {
        val session = ScanSessionEntity(sessionId = "s1", vehicleLabel = "V1", routeLabel = "R1", startedAt = 100L)
        val event = DetectionEventEntity(
            eventId = 1, sessionId = "s1", capturedAt = 100L, classId = 3, className = "Pothole (D40)",
            confidence = 0.9f, left = 0f, top = 0f, right = 10f, bottom = 10f
        )
        val repo = RoadInspectionRepository(
            injectedSessionDao = SimpleSessionDao(listOf(session)),
            injectedEventDao = SimpleEventDao(listOf(event))
        )

        val vm = ScanViewModel(StubDetector(), repo)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, vm.uiState.value.metricsState.totalScans)
        assertEquals(1, vm.uiState.value.metricsState.roadHazards)
        assertEquals(1, vm.uiState.value.metricsState.potholes)
        assertEquals(1, vm.uiState.value.cameraState.recentSessions.size)
    }
}
