package com.roadlens.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.roadlens.app.data.local.entity.HazardClusterEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for spatial clusters and repeat occurrence hazard tracking.
 * Strictly under 200 lines per project engineering rules.
 */
@Dao
interface HazardClusterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCluster(cluster: HazardClusterEntity): Long

    @Update
    suspend fun updateCluster(cluster: HazardClusterEntity)

    @Query("SELECT * FROM hazard_clusters ORDER BY eventCount DESC")
    fun getAllClusters(): Flow<List<HazardClusterEntity>>

    @Query("SELECT * FROM hazard_clusters WHERE damageClass = :damageClass ORDER BY eventCount DESC")
    fun getClustersByClass(damageClass: String): Flow<List<HazardClusterEntity>>
}
