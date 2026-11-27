package com.roadlens.app.ui.viewmodel

import com.roadlens.app.data.local.dao.DetectionEventDao
import com.roadlens.app.data.local.dao.ScanSessionDao
import com.roadlens.app.data.local.entity.DetectionEventEntity
import com.roadlens.app.data.local.entity.ScanSessionEntity
import com.roadlens.app.data.repository.RoadInspectionRepository
import com.roadlens.app.domain.model.MapDefectFilter
import com.roadlens.app.domain.model.MapStatusFilter
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
        DetectionEventEntity(eventId = 4, sessionId = "s1", capturedAt = 1600L, classId = 2, className = "Alligator Crack (D20)", confidence = 0.75f, left = 0f, top = 0f, right = 10f, bottom = 10f, latitude = 12.9730, longitude = 77.5960, reviewStatus = "PENDING"),
        DetectionEventEntity(eventId = 5, sessionId = "s1", capturedAt = 1800L, classId = 1, className = "Transverse Crack (D10)", confidence = 0.7f, left = 0f, top = 0f, right = 10f, bottom = 10f, latitude = 12.9740, longitude = 77.5970, reviewStatus = "DISMISSED")
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
        // Out of 5 sample events, 1 has null coordinates, so 4 should be returned
        assertEquals(4, list.size)
        assertTrue(list.all { it.latitude != null && it.longitude != null })
    }

    @Test
    fun setDefectFilter_potholes_returnsOnlyPotholes() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.setDefectFilter(MapDefectFilter.POTHOLES)
        testDispatcher.scheduler.advanceUntilIdle()
        val list = viewModel.hazards.value
        assertEquals(1, list.size)
        assertEquals(3, list.first().classId)
    }

    @Test
    fun setDefectFilter_allCracks_returnsAllThreeCrackTypes() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.setDefectFilter(MapDefectFilter.CRACKS_ALL)
        testDispatcher.scheduler.advanceUntilIdle()
        val list = viewModel.hazards.value
        assertEquals(3, list.size)
        assertTrue(list.all { it.classId in listOf(0, 1, 2) })
    }

    @Test
    fun setDefectFilter_individualCrackTypes() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.setDefectFilter(MapDefectFilter.ALLIGATOR)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.hazards.value.size)
        assertEquals(2, viewModel.hazards.value.first().classId)

        viewModel.setDefectFilter(MapDefectFilter.LONGITUDINAL)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.hazards.value.size)
        assertEquals(0, viewModel.hazards.value.first().classId)

        viewModel.setDefectFilter(MapDefectFilter.TRANSVERSE)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.hazards.value.size)
        assertEquals(1, viewModel.hazards.value.first().classId)
    }

    @Test
    fun setStatusFilter_and_combinedFiltering() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.setStatusFilter(MapStatusFilter.PENDING)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.hazards.value.size)

        // Combine: All Cracks + Confirmed
        viewModel.setDefectFilter(MapDefectFilter.CRACKS_ALL)
        viewModel.setStatusFilter(MapStatusFilter.CONFIRMED)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.hazards.value.size)
        assertEquals(0, viewModel.hazards.value.first().classId)
        assertEquals("CONFIRMED", viewModel.hazards.value.first().reviewStatus)
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
    fun setFilter_updatesFilterState() {
        viewModel.setFilter(MapDefectFilter.POTHOLES)
        assertEquals(MapDefectFilter.POTHOLES, viewModel.filter.value)
    }

    @Test
    fun updateReview_updatesStatusOfSelectedHazard() = runTest {
        viewModel.selectHazard(sampleEvents[0])
        viewModel.updateReview(1L, "CONFIRMED")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("CONFIRMED", viewModel.selectedHazard.value?.reviewStatus)

        // Non-matching event ID leaves selected hazard intact
        viewModel.updateReview(999L, "DISMISSED")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("CONFIRMED", viewModel.selectedHazard.value?.reviewStatus)
    }
}
