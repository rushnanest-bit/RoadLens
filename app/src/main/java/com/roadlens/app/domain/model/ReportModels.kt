package com.roadlens.app.domain.model

import com.roadlens.app.data.local.entity.DetectionEventEntity
import com.roadlens.app.data.local.entity.ScanSessionEntity

/**
 * Domain presentation models for fleet road inspection reports.
 * Maintained well below 100 lines per project engineering rules.
 */
data class SessionReportItem(
    val session: ScanSessionEntity,
    val totalHazards: Int,
    val potholes: Int,
    val cracks: Int,
    val otherHazards: Int
)

data class SessionDetailReport(
    val session: ScanSessionEntity,
    val events: List<DetectionEventEntity>
)
