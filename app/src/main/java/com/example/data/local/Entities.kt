package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// ─────────────────────────────────────────────────────────────────────────────
// Участки пользователя
// ─────────────────────────────────────────────────────────────────────────────
@Entity(
    tableName = "plots",
    indices = [
        Index(value = ["latitude", "longitude"]),
        Index(value = ["updated_at"]),
    ]
)
data class PlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val cropType: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val areaHectares: Double,
    val soilMoisture: Double,   // 0..100 %
    val healthScore: Int,       // 0..100 %
    @ColumnInfo(name = "created_at")  val createdAt: Long  = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")  val updatedAt: Long  = System.currentTimeMillis(),
    @ColumnInfo(name = "sync_state")  val syncState: String = "PENDING", // PENDING | SYNCED | FAILED
)

// ─────────────────────────────────────────────────────────────────────────────
// Кэш AQI
// ИСПРАВЛЕНО: primaryKey был String (locationName) — ненадёжно.
// Теперь составной ключ (geohash + observedAt) как в архитектурной спеке.
// Для простоты MVP используем geohash = "lat5_lon5" (5 знаков после точки).
// ─────────────────────────────────────────────────────────────────────────────
@Entity(
    tableName = "aqi_cache",
    primaryKeys = ["geohash", "observed_at"],
    indices = [
        Index(value = ["expires_at"]),
        Index(value = ["location_name"]),
    ]
)
data class AqiCacheEntity(
    val geohash: String,                    // "40.18044_44.51361" (5 знаков)
    @ColumnInfo(name = "observed_at") val observedAt: Long,
    @ColumnInfo(name = "location_name") val locationName: String,  // человекочитаемое имя
    val aqiValue: Int,
    val levelCode: String,                  // GOOD | MODERATE | UNHEALTHY
    val pm25: Double,
    val pm10: Double,
    val primaryPollutant: String,
    val advisoryEn: String,
    val advisoryAm: String,
    val advisoryRu: String,
    @ColumnInfo(name = "expires_at") val expiresAt: Long,          // TTL
    @ColumnInfo(name = "cached_at")  val cachedAt: Long = System.currentTimeMillis(),
)

// ─────────────────────────────────────────────────────────────────────────────
// История диагнозов болезней растений
// ─────────────────────────────────────────────────────────────────────────────
@Entity(
    tableName = "plant_diseases",
    indices = [
        Index(value = ["plot_id"]),
        Index(value = ["scanned_at"]),
    ]
)
data class PlantDiseaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "plot_id")     val plotId: Long? = null,   // опциональная связь с участком
    val cropName: String,
    val suspectedDisease: String,
    val confidence: Double,             // 0.0..1.0
    val symptomsSummary: String,
    val diagnosisEn: String,
    val diagnosisAm: String,
    val diagnosisRu: String,
    val treatmentsEn: String,
    val treatmentsAm: String,
    val treatmentsRu: String,
    @ColumnInfo(name = "scanned_at")  val scannedAt: Long  = System.currentTimeMillis(),
    @ColumnInfo(name = "synced_at")   val syncedAt: Long?  = null,
    val isLocalScanOnly: Boolean = true,
)

// ─────────────────────────────────────────────────────────────────────────────
// SOS-алерты (краудсорсинг)
// ─────────────────────────────────────────────────────────────────────────────
@Entity(
    tableName = "climate_sos_alerts",
    indices = [
        Index(value = ["status"]),
        Index(value = ["timestamp"]),
    ]
)
data class ClimateSosEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alertType: String,          // WATER_SHORTAGE | PEST_OUTBREAK | FROST_ALERT | FOREST_FIRE
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val reportedBy: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "ACTIVE",  // ACTIVE | RESOLVED
    @ColumnInfo(name = "sync_state") val syncState: String = "PENDING",
)