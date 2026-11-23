package com.roadlens.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.roadlens.app.data.local.dao.DetectionEventDao
import com.roadlens.app.data.local.dao.HazardClusterDao
import com.roadlens.app.data.local.dao.ScanSessionDao
import com.roadlens.app.data.local.entity.DetectionEventEntity
import com.roadlens.app.data.local.entity.HazardClusterEntity
import com.roadlens.app.data.local.entity.ScanSessionEntity

/**
 * Main Room SQLite Database for offline-first RoadLens perception and session persistence.
 * Strictly under 200 lines per project engineering rules.
 */
@Database(
    entities = [
        ScanSessionEntity::class,
        DetectionEventEntity::class,
        HazardClusterEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class RoadLensDatabase : RoomDatabase() {

    abstract fun scanSessionDao(): ScanSessionDao
    abstract fun detectionEventDao(): DetectionEventDao
    abstract fun hazardClusterDao(): HazardClusterDao

    companion object {
        @Volatile
        private var INSTANCE: RoadLensDatabase? = null

        fun getInstance(context: Context): RoadLensDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RoadLensDatabase::class.java,
                    "roadlens.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
