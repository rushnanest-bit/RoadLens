package com.roadlens.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.roadlens.app.data.local.entity.ScanSessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for scan session operations.
 * Strictly under 200 lines per project engineering rules.
 */
@Dao
interface ScanSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ScanSessionEntity)

    @Update
    suspend fun updateSession(session: ScanSessionEntity)

    @Query("SELECT * FROM scan_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<ScanSessionEntity>>

    @Query("SELECT * FROM scan_sessions WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): ScanSessionEntity?

    @Query("SELECT COUNT(*) FROM scan_sessions")
    fun getTotalSessionsCount(): Flow<Int>

    @Query("UPDATE scan_sessions SET status = :status, endedAt = :endedAt WHERE sessionId = :sessionId")
    suspend fun finishSession(sessionId: String, status: String, endedAt: Long)
}
