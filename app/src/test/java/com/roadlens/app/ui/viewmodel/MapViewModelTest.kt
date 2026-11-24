package com.roadlens.app.ui.viewmodel

import com.roadlens.app.data.local.dao.DetectionEventDao
import com.roadlens.app.data.local.dao.ScanSessionDao
import com.roadlens.app.data.local.entity.DetectionEventEntity
import com.roadlens.app.data.local.entity.ScanSessionEntity
import com.roadlens.app.data.repository.RoadInspectionRepository
import com.roadlens.app.domain.model.MapFilterType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for MapViewModel hazard filtering and pin selection logic.
 * Strictly under 200 lines per project engineering rules.
 * Covers positive, negative, and boundary condition scenarios (KPI 4.7).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeEventDao(val events: List<DetectionEventEntity>) : DetectionEventDao {
        override suspend fun insertEvent(event: DetectionEventEntity): Long = 1L
        override suspend fun insertEvents(events: List<DetectionEventEntity>) = Unit
        override suspend fun updateEvent(event: DetectionEventEntity) = Unit
        override fun getEventsForSession(sessionId: String): Flow<List<DetectionEventEntity>> = flowOf(emptyList())
        override fun getAllEvents(): Flow<List<DetectionEventEntity>> = flowOf(events)
        override fun getTotalHazardsCount(): Flow<Int> = flowOf(events.size)
        override fun getPotholesCount(): Flow<Int> = flowOf(events.count { it.classId == 3 })
        override fun getPendingReviewCount(): Flow<Int> = flowOf(events.count { it.reviewStatus == "PENDING" })
        override suspend fun updateReviewStatus(eventId: Long, status: String, reviewerClass: String?, notes: String?) = Unit
    }

    private class FakeSessionDao : ScanSessionDao {
        override suspend fun insertSession(session: ScanSessionEntity) = Unit
        override suspend fun updateSession(session: ScanSessionEntity) = Unit
        override fun getAllSessions(): Flow<List<ScanSessionEntity>> = flowOf(emptyList())
        override suspend fun getSessionById(sessionId: String): ScanSessionEntity? = null
        override fun getTotalSessionsCount(): Flow<Int> = flowOf(0)
        override suspend fun finishSession(sessionId: String, status: String, endedAt: Long) = Unit
        override suspend fun finishSessionWithLocation(sessionId: String, status: String, endedAt: Long, stopLocation: String?) = Unit
        override suspend fun updateSessionStatus(sessionId: String, status: String) = Unit
    }

    private lateinit var fakeDao: FakeEventDao
    private lateinit var viewModel: MapViewModel

    private val sampleEvents = listOf(
        DetectionEventEntity(eventId = 1, sessionId = "s1", capturedAt = 1000L, classId = 3, className = "Pothole (D40)", confidence = 0.9f, left = 0f, top = 0f, right = 10f, bottom = 10f, latitude = 12.9716, longitude = 77.5946, reviewStatus = "PENDING"),
        DetectionEventEntity(eventId = 2, sessionId = "s1", capturedAt = 1200L, classId = 0, className = "Longitudinal Crack (D00)", confidence = 0.8f, left = 0f, top = 0f, right = 10f, bottom = 10f, latitude = 12.9720, longitude = 77.5950, reviewStatus = "CONFIRMED"),
        DetectionEventEntity(eventId = 3, sessionId = "s1", capturedAt = 1400L, classId = 3, className = "Pothole (D40)", confidence = 0.85f, left = 0f, top = 0f, right = 10f, bottom = 10f, latitude = null, longitude = null, reviewStatus = "PENDING"), // unlocated
        DetectionEventEntity(eventId = 4, sessionId = "s1", capturedAt = 1600L, classId = 9, className = "Manhole Defect", confidence = 0.75f, left = 0f, top = 0f, right = 10f, bottom = 10f, latitude = 12.9730, longitude = 77.5960, reviewStatus = "PENDING")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeEventDao(sampleEvents)
        val fakeRepo = RoadInspectionRepository(
            injectedEventDao = fakeDao,
            injectedSessionDao = FakeSessionDao()
        )
        viewModel = MapViewModel(fakeRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun hazards_filtersOutEventsWithoutCoordinates() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        val list = viewModel.hazards.value
        // Out of 4 sample events, 1 has null coordinates, so 3 should be returned
        assertEquals(3, list.size)
        assertTrue(list.all { it.latitude != null && it.longitude != null })
    }

    @Test
    fun setFilter_potholes_returnsOnlyPotholes() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.setFilter(MapFilterType.POTHOLES)
        testDispatcher.scheduler.advanceUntilIdle()

        val list = viewModel.hazards.value
        assertEquals(1, list.size)
        assertEquals(3, list.first().classId)
    }

    @Test
    fun setFilter_cracks_returnsOnlyCracks() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.setFilter(MapFilterType.CRACKS)
        testDispatcher.scheduler.advanceUntilIdle()

        val list = viewModel.hazards.value
        assertEquals(1, list.size)
        assertEquals(0, list.first().classId)
    }

    @Test
    fun setFilter_other_returnsOnlyOtherHazards() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.setFilter(MapFilterType.OTHER)
        testDispatcher.scheduler.advanceUntilIdle()

        val list = viewModel.hazards.value
        assertEquals(1, list.size)
        assertEquals(9, list.first().classId)
    }

    @Test
    fun setFilter_pending_returnsOnlyPendingStatus() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.setFilter(MapFilterType.PENDING)
        testDispatcher.scheduler.advanceUntilIdle()

        val list = viewModel.hazards.value
        assertEquals(2, list.size)
        assertTrue(list.all { it.reviewStatus == "PENDING" })
    }

    @Test
    fun selectHazard_updatesAndClearsSelectedState() {
        assertNull(viewModel.selectedHazard.value)
        viewModel.selectHazard(sampleEvents[0])
        assertNotNull(viewModel.selectedHazard.value)
        assertEquals(1L, viewModel.selectedHazard.value?.eventId)

        viewModel.selectHazard(null)
        assertNull(viewModel.selectedHazard.value)
    }

    @Test
    fun updateReview_updatesStatusOfSelectedHazard() = runTest {
        viewModel.selectHazard(sampleEvents[0])
        viewModel.updateReview(1L, "CONFIRMED")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("CONFIRMED", viewModel.selectedHazard.value?.reviewStatus)
    }
}
