package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plots")
data class PlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val cropType: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val areaHectares: Double,
    val soilMoisture: Double, // 0..100 %
    val healthScore: Int, // 0..100 %
    val lastCheckedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "aqi_cache")
data class AqiCacheEntity(
    @PrimaryKey val location: String,
    val aqiValue: Int,
    val levelCode: String, // GOOD, MODERATE, UNHEALTHY
    val pm25: Double,
    val pm10: Double,
    val primaryPollutant: String,
    val advisoryEn: String,
    val advisoryAm: String,
    val advisoryRu: String,
    val cachedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "plant_diseases")
data class PlantDiseaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cropName: String,
    val suspectedDisease: String,
    val confidence: Double, // e.g. 0.94 for 94%
    val symptomsSummary: String,
    val diagnosisEn: String,
    val diagnosisAm: String,
    val diagnosisRu: String,
    val treatmentsEn: String,
    val treatmentsAm: String,
    val treatmentsRu: String,
    val scannedTimestamp: Long = System.currentTimeMillis(),
    val isLocalScanOnly: Boolean = true
)

@Entity(tableName = "climate_sos_alerts")
data class ClimateSosEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alertType: String, // WATER_SHORTAGE, PEST_OUTBREAK, FROST_ALERT, FOREST_FIRE
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val reportedBy: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "ACTIVE" // ACTIVE, RESOLVED
)
