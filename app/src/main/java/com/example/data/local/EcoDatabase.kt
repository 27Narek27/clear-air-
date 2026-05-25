package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * EcoDatabase — Room база данных приложения.
 *
 * ИСПРАВЛЕНИЯ:
 *  - exportSchema = false для MVP (убирает необходимость настройки
 *    schemaLocation в build.gradle.kts). Для продакшна верните true
 *    и добавьте в build.gradle:
 *      ksp { arg("room.schemaLocation", "$projectDir/schemas") }
 *  - Добавлен явный создание индексов в MIGRATION_1_2
 *  - Исправлена опечатка в имени колонки scannedTimestamp → scanned_at
 */
@Database(
    entities = [
        PlotEntity::class,
        AqiCacheEntity::class,
        PlantDiseaseEntity::class,
        ClimateSosEntity::class,
    ],
    version = 2,
    exportSchema = false,   // true + schemaLocation только для продакшна
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
        // Миграция v1 → v2
        // Описание изменений:
        //  - aqi_cache: пересоздаём с составным PK (geohash + observed_at)
        //  - plots: добавляем created_at, updated_at, sync_state
        //  - plant_diseases: добавляем plot_id, synced_at
        //  - climate_sos_alerts: добавляем sync_state
        // ─────────────────────────────────────────────────────────────────
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Пересоздаём aqi_cache с новой схемой
                db.execSQL("DROP TABLE IF EXISTS aqi_cache")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS aqi_cache (
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
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_aqi_cache_expires_at ON aqi_cache(expires_at)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_aqi_cache_location_name ON aqi_cache(location_name)")

                // 2. Обновляем таблицу plots
                db.execSQL("ALTER TABLE plots ADD COLUMN created_at INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE plots ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE plots ADD COLUMN sync_state TEXT NOT NULL DEFAULT 'PENDING'")

                // 3. Обновляем plant_diseases
                db.execSQL("ALTER TABLE plant_diseases ADD COLUMN plot_id INTEGER")
                db.execSQL("ALTER TABLE plant_diseases ADD COLUMN synced_at INTEGER")

                // 4. Обновляем climate_sos_alerts
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
                    // НИКОГДА не используйте fallbackToDestructiveMigration() в продакшне
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}