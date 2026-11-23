package com.roadlens.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a named fleet road-scanning session.
 * Stores route metadata, device timestamps, and telemetry summary per Project Spec 7.2.
 * Strictly under 200 lines per project engineering rules.
 */
@Entity(tableName = "scan_sessions")
data class ScanSessionEntity(
    @PrimaryKey
    val sessionId: String,
    val vehicleLabel: String,
    val routeLabel: String,
    val startedAt: Long,
    val endedAt: Long? = null,
    val status: String = "ACTIVE", // ACTIVE, PAUSED, COMPLETED
    val distanceM: Float = 0f,
    val modelVersion: String = "yolo11n-v1.0",
    val appVersion: String = "1.0",
    val sourceMode: String = "CAMERA", // CAMERA, VIDEO, IMAGE
    val startLocation: String? = null,
    val stopLocation: String? = null
)
