package com.roadlens.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roadlens.app.data.local.entity.DetectionEventEntity
import com.roadlens.app.data.local.entity.ScanSessionEntity
import com.roadlens.app.data.repository.RoadInspectionRepository
import com.roadlens.app.domain.model.SessionDetailReport
import com.roadlens.app.domain.model.SessionReportItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel managing fleet inspection session reports and detail inspection views.
 * Strictly under 200 lines per project engineering rules.
 */
class ReportViewModel(
    private val repository: RoadInspectionRepository
) : ViewModel() {

    private val _selectedSessionDetail = MutableStateFlow<SessionDetailReport?>(null)
    val selectedSessionDetail: StateFlow<SessionDetailReport?> = _selectedSessionDetail.asStateFlow()

    val rawSessions: StateFlow<List<ScanSessionEntity>> = repository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val rawEvents: StateFlow<List<DetectionEventEntity>> = repository.getAllEvents()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val sessionReports: StateFlow<List<SessionReportItem>> = combine(
        rawSessions,
        rawEvents
    ) { sessions, events ->
        val eventsBySession = events.groupBy { it.sessionId }
        sessions.map { session ->
            val sessionEvents = eventsBySession[session.sessionId] ?: emptyList()
            val potholes = sessionEvents.count { it.classId == 3 }
            val cracks = sessionEvents.count { it.classId in listOf(0, 1, 2) }
            val other = sessionEvents.size - (potholes + cracks)
            SessionReportItem(
                session = session,
                totalHazards = sessionEvents.size,
                potholes = potholes,
                cracks = cracks,
                otherHazards = other
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun selectSession(session: ScanSessionEntity?) {
        if (session == null) {
            _selectedSessionDetail.value = null
            return
        }
        viewModelScope.launch {
            val events = rawEvents.value.filter { it.sessionId == session.sessionId }
            _selectedSessionDetail.value = SessionDetailReport(session, events)
        }
    }
}
