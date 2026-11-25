package com.roadlens.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roadlens.app.data.local.entity.DetectionEventEntity
import com.roadlens.app.data.repository.RoadInspectionRepository
import com.roadlens.app.domain.model.MapDefectFilter
import com.roadlens.app.domain.model.MapStatusFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel managing hazard map markers, category filtering, and pin inspections.
 * Maintained well below 200 lines strictly per project engineering rules.
 */
class MapViewModel(
    private val repository: RoadInspectionRepository
) : ViewModel() {

    private val _defectFilter = MutableStateFlow(MapDefectFilter.ALL)
    val defectFilter: StateFlow<MapDefectFilter> = _defectFilter.asStateFlow()
    val filter: StateFlow<MapDefectFilter> = _defectFilter.asStateFlow()

    private val _statusFilter = MutableStateFlow(MapStatusFilter.ALL)
    val statusFilter: StateFlow<MapStatusFilter> = _statusFilter.asStateFlow()

    private val _selectedHazard = MutableStateFlow<DetectionEventEntity?>(null)
    val selectedHazard: StateFlow<DetectionEventEntity?> = _selectedHazard.asStateFlow()

    val hazards: StateFlow<List<DetectionEventEntity>> = combine(
        repository.getAllEvents(),
        _defectFilter,
        _statusFilter
    ) { allEvents, currentDefect, currentStatus ->
        val locatedEvents = allEvents.filter { it.latitude != null && it.longitude != null }
        locatedEvents.filter { event ->
            val matchesDefect = when (currentDefect) {
                MapDefectFilter.ALL -> true
                MapDefectFilter.POTHOLES -> event.classId == 3
                MapDefectFilter.CRACKS_ALL -> event.classId in listOf(0, 1, 2)
                MapDefectFilter.ALLIGATOR -> event.classId == 2
                MapDefectFilter.LONGITUDINAL -> event.classId == 0
                MapDefectFilter.TRANSVERSE -> event.classId == 1
            }
            val matchesStatus = when (currentStatus) {
                MapStatusFilter.ALL -> true
                MapStatusFilter.PENDING -> event.reviewStatus == "PENDING"
                MapStatusFilter.CONFIRMED -> event.reviewStatus == "CONFIRMED"
                MapStatusFilter.DISMISSED -> event.reviewStatus == "DISMISSED"
            }
            matchesDefect && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun setDefectFilter(newFilter: MapDefectFilter) {
        _defectFilter.value = newFilter
    }

    fun setFilter(newFilter: MapDefectFilter) {
        _defectFilter.value = newFilter
    }

    fun setStatusFilter(newFilter: MapStatusFilter) {
        _statusFilter.value = newFilter
    }

    fun selectHazard(hazard: DetectionEventEntity?) {
        _selectedHazard.value = hazard
    }

    fun updateReview(eventId: Long, status: String) {
        viewModelScope.launch {
            repository.updateReview(eventId, status)
            _selectedHazard.value = _selectedHazard.value?.let {
                if (it.eventId == eventId) it.copy(reviewStatus = status) else it
            }
        }
    }
}
