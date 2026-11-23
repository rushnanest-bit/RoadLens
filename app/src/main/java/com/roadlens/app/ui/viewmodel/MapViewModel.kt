package com.roadlens.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roadlens.app.data.local.entity.DetectionEventEntity
import com.roadlens.app.data.repository.RoadInspectionRepository
import com.roadlens.app.domain.model.MapFilterType
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

    private val _filter = MutableStateFlow(MapFilterType.ALL)
    val filter: StateFlow<MapFilterType> = _filter.asStateFlow()

    private val _selectedHazard = MutableStateFlow<DetectionEventEntity?>(null)
    val selectedHazard: StateFlow<DetectionEventEntity?> = _selectedHazard.asStateFlow()

    val hazards: StateFlow<List<DetectionEventEntity>> = combine(
        repository.getAllEvents(),
        _filter
    ) { allEvents, currentFilter ->
        val locatedEvents = allEvents.filter { it.latitude != null && it.longitude != null }
        when (currentFilter) {
            MapFilterType.ALL -> locatedEvents
            MapFilterType.POTHOLES -> locatedEvents.filter { it.classId == 3 }
            MapFilterType.CRACKS -> locatedEvents.filter { it.classId in listOf(0, 1, 2) }
            MapFilterType.OTHER -> locatedEvents.filter { it.classId !in listOf(0, 1, 2, 3) }
            MapFilterType.PENDING -> locatedEvents.filter { it.reviewStatus == "PENDING" }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun setFilter(newFilter: MapFilterType) {
        _filter.value = newFilter
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
