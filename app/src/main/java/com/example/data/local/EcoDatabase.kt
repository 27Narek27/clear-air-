package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PlotEntity::class,
        AqiCacheEntity::class,
        PlantDiseaseEntity::class,
        ClimateSosEntity::class,
    ],
    version = 2,          // увеличиваем версию при изменении схемы
    exportSchema = true,  // true — сохраняет JSON-схему для MigrationTestHelper
)
abstract class EcoDatabase : RoomDatabase() {
    abstract fun plotDao(): PlotDao
    abstract fun aqiCacheDao(): AqiCacheDao
    abstract fun plantDiseaseDao(): PlantDiseaseDao
    abstract fun climateSosDao(): ClimateSosDao

    companion object {
        @Volatile
        private var INSTANCE: EcoDatabase? = null

        // ─────────────────────────────────────────────────────────────────
        // Миграции
        // ВАЖНО: никогда не используйте fallbackToDestructiveMigration()
        // в продакшне — это уничтожает все данные пользователя.
        //
        // Схема изменений v1 → v2:
        //  - aqi_cache: primaryKey изменён с (location) на (geohash, observed_at)
        //  - aqi_cache: добавлены колонки geohash, observed_at, location_name, expires_at, cached_at
        //  - aqi_cache: удалена колонка cachedTimestamp
        //  - plots: добавлены колонки created_at, updated_at, sync_state
        //  - plant_diseases: переименована scannedTimestamp → scanned_at, добавлены plot_id, synced_at
        //  - climate_sos_alerts: добавлена колонка sync_state
        // ─────────────────────────────────────────────────────────────────
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Пересоздаём aqi_cache с новой схемой (составной PK)
                db.execSQL("DROP TABLE IF EXISTS aqi_cache")
                db.execSQL("""
                    CREATE TABLE aqi_cache (
                        geohash TEXT NOT NULL,
                        observed_at INTEGER NOT NULL,
                        location_name TEXT NOT NULL,
                        aqiValue INTEGER NOT NULL,
                        levelCode TEXT NOT NULL,
                        pm25 REAL NOT NULL,
                        pm10 REAL NOT NULL,
                        primaryPollutant TEXT NOT NULL,
                        advisoryEn TEXT NOT NULL,
                        advisoryAm TEXT NOT NULL,
                        advisoryRu TEXT NOT NULL,
                        expires_at INTEGER NOT NULL,
                        cached_at INTEGER NOT NULL,
                        PRIMARY KEY(geohash, observed_at)
                    )
                """.trimIndent())

                // Добавляем новые колонки в plots
                db.execSQL("ALTER TABLE plots ADD COLUMN created_at INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE plots ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE plots ADD COLUMN sync_state TEXT NOT NULL DEFAULT 'PENDING'")

                // Обновляем plant_diseases
                db.execSQL("ALTER TABLE plant_diseases ADD COLUMN plot_id INTEGER")
                db.execSQL("ALTER TABLE plant_diseases ADD COLUMN synced_at INTEGER")

                // Обновляем climate_sos_alerts
                db.execSQL("ALTER TABLE climate_sos_alerts ADD COLUMN sync_state TEXT NOT NULL DEFAULT 'PENDING'")
            }
        }

        fun getDatabase(context: Context): EcoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EcoDatabase::class.java,
                    "ecosys_database",
                )
                    .addMigrations(MIGRATION_1_2)
                    // fallbackToDestructiveMigration() — УДАЛЕНО
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}