package com.roadlens.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.roadlens.app.data.local.entity.DetectionEventEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for detection events and human review workflows.
 * Strictly under 200 lines per project engineering rules.
 */
@Dao
interface DetectionEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: DetectionEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<DetectionEventEntity>)

    @Update
    suspend fun updateEvent(event: DetectionEventEntity)

    @Query("SELECT * FROM detection_events WHERE sessionId = :sessionId ORDER BY capturedAt ASC")
    fun getEventsForSession(sessionId: String): Flow<List<DetectionEventEntity>>

    @Query("SELECT * FROM detection_events ORDER BY capturedAt DESC")
    fun getAllEvents(): Flow<List<DetectionEventEntity>>

    @Query("SELECT COUNT(*) FROM detection_events")
    fun getTotalHazardsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM detection_events WHERE classId = 3")
    fun getPotholesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM detection_events WHERE reviewStatus = 'PENDING'")
    fun getPendingReviewCount(): Flow<Int>

    @Query("UPDATE detection_events SET reviewStatus = :status, reviewerClass = :reviewerClass, notes = :notes WHERE eventId = :eventId")
    suspend fun updateReviewStatus(eventId: Long, status: String, reviewerClass: String?, notes: String?)
}
