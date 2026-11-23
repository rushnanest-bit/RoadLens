package com.roadlens.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a consolidated spatial cluster of road distresses across route passes.
 * Tracks repeat occurrences and triage priority per Project Spec 7.2.
 * Strictly under 200 lines per project engineering rules.
 */
@Entity(tableName = "hazard_clusters")
data class HazardClusterEntity(
    @PrimaryKey(autoGenerate = true)
    val clusterId: Long = 0,
    val centroidLat: Double,
    val centroidLon: Double,
    val damageClass: String,
    val firstSeenAt: Long,
    val lastSeenAt: Long,
    val eventCount: Int = 1,
    val distinctSessionCount: Int = 1,
    val triageLevel: String = "LOW" // LOW, MEDIUM, HIGH
)
