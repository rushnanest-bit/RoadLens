package com.roadlens.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents an individual road distress observation event.
 * Contains bounding box geometry, GPS metadata, and human review state per Project Spec 7.2.
 * Strictly under 200 lines per project engineering rules.
 */
@Entity(
    tableName = "detection_events",
    foreignKeys = [
        ForeignKey(
            entity = ScanSessionEntity::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("sessionId"),
        Index("classId"),
        Index("reviewStatus")
    ]
)
data class DetectionEventEntity(
    @PrimaryKey(autoGenerate = true)
    val eventId: Long = 0,
    val sessionId: String,
    val capturedAt: Long,
    val classId: Int,
    val className: String,
    val confidence: Float,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val gpsAccuracyM: Float? = null,
    val locationSource: String = "UNLOCATED", // GPS, UNLOCATED
    val evidencePath: String? = null,
    val reviewStatus: String = "PENDING", // PENDING, CONFIRMED, DISMISSED
    val reviewerClass: String? = null,
    val notes: String? = null
)
