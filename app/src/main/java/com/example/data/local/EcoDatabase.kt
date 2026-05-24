package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PlotEntity::class,
        AqiCacheEntity::class,
        PlantDiseaseEntity::class,
        ClimateSosEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EcoDatabase : RoomDatabase() {
    abstract fun plotDao(): PlotDao
    abstract fun aqiCacheDao(): AqiCacheDao
    abstract fun plantDiseaseDao(): PlantDiseaseDao
    abstract fun climateSosDao(): ClimateSosDao

    companion object {
        @Volatile
        private var INSTANCE: EcoDatabase? = null

        fun getDatabase(context: Context): EcoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EcoDatabase::class.java,
                    "ecosys_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
