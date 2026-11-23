package com.roadlens.app.data.repository

import com.roadlens.app.data.local.RoadLensDatabase
import com.roadlens.app.data.local.entity.DetectionEventEntity
import com.roadlens.app.data.local.entity.ScanSessionEntity
import com.roadlens.app.domain.model.RoadDetection
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository orchestrating local Room persistence for sessions, detections, and review actions.
 * Strictly under 200 lines per project engineering rules.
 */
class RoadInspectionRepository(
    private val database: RoadLensDatabase
) {
    private val sessionDao = database.scanSessionDao()
    private val eventDao = database.detectionEventDao()

    val totalSessions: Flow<Int> = sessionDao.getTotalSessionsCount()
    val totalHazards: Flow<Int> = eventDao.getTotalHazardsCount()
    val totalPotholes: Flow<Int> = eventDao.getPotholesCount()
    val pendingReviews: Flow<Int> = eventDao.getPendingReviewCount()

    suspend fun startSession(
        vehicleLabel: String,
        routeLabel: String,
        sourceMode: String,
        startLocation: String? = null
    ): String {
        val sessionId = UUID.randomUUID().toString()
        val session = ScanSessionEntity(
            sessionId = sessionId,
            vehicleLabel = vehicleLabel,
            routeLabel = routeLabel,
            startedAt = System.currentTimeMillis(),
            sourceMode = sourceMode,
            startLocation = startLocation
        )
        sessionDao.insertSession(session)
        return sessionId
    }

    suspend fun finishSession(sessionId: String, stopLocation: String? = null) {
        if (stopLocation != null) {
            sessionDao.finishSessionWithLocation(sessionId, status = "COMPLETED", endedAt = System.currentTimeMillis(), stopLocation = stopLocation)
        } else {
            sessionDao.finishSession(sessionId, status = "COMPLETED", endedAt = System.currentTimeMillis())
        }
    }

    suspend fun updateSessionStatus(sessionId: String, status: String) {
        sessionDao.updateSessionStatus(sessionId, status)
    }

    suspend fun recordDetections(
        sessionId: String,
        detections: List<RoadDetection>,
        timestampMs: Long,
        lat: Double? = null,
        lon: Double? = null,
        accuracy: Float? = null,
        evidencePath: String? = null
    ) {
        if (detections.isEmpty()) return
        val locationSource = if (lat != null && lon != null && (accuracy == null || accuracy <= 25f)) "GPS" else "UNLOCATED"

        val entities = detections.map { det ->
            DetectionEventEntity(
                sessionId = sessionId,
                capturedAt = timestampMs,
                classId = det.classIndex,
                className = det.className,
                confidence = det.confidence,
                left = det.left,
                top = det.top,
                right = det.right,
                bottom = det.bottom,
                latitude = lat,
                longitude = lon,
                gpsAccuracyM = accuracy,
                locationSource = locationSource,
                evidencePath = evidencePath
            )
        }
        eventDao.insertEvents(entities)
    }

    suspend fun updateReview(eventId: Long, status: String, reviewerClass: String? = null, notes: String? = null) {
        eventDao.updateReviewStatus(eventId, status, reviewerClass, notes)
    }

    fun getAllSessions(): Flow<List<ScanSessionEntity>> = sessionDao.getAllSessions()

    fun getEventsForSession(sessionId: String): Flow<List<DetectionEventEntity>> = eventDao.getEventsForSession(sessionId)

    fun getAllEvents(): Flow<List<DetectionEventEntity>> = eventDao.getAllEvents()
}
