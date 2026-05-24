package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlotDao {
    @Query("SELECT * FROM plots ORDER BY name ASC")
    fun getAllPlots(): Flow<List<PlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlot(plot: PlotEntity)

    @Update
    suspend fun updatePlot(plot: PlotEntity)

    @Query("DELETE FROM plots WHERE id = :id")
    suspend fun deletePlotById(id: Long)
}

@Dao
interface AqiCacheDao {
    @Query("SELECT * FROM aqi_cache WHERE location = :location LIMIT 1")
    suspend fun getAqiByLocation(location: String): AqiCacheEntity?

    @Query("SELECT * FROM aqi_cache ORDER BY cachedTimestamp DESC")
    fun getAllCachedAqi(): Flow<List<AqiCacheEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAqi(aqi: AqiCacheEntity)
}

@Dao
interface PlantDiseaseDao {
    @Query("SELECT * FROM plant_diseases ORDER BY scannedTimestamp DESC")
    fun getAllDiseases(): Flow<List<PlantDiseaseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiseaseReport(disease: PlantDiseaseEntity)

    @Query("DELETE FROM plant_diseases WHERE id = :id")
    suspend fun deleteDiseaseReportById(id: Long)
}

@Dao
interface ClimateSosDao {
    @Query("SELECT * FROM climate_sos_alerts ORDER BY timestamp DESC")
    fun getAllSosAlerts(): Flow<List<ClimateSosEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSosAlert(alert: ClimateSosEntity)

    @Query("UPDATE climate_sos_alerts SET status = :status WHERE id = :id")
    suspend fun updateSosStatus(id: Long, status: String)

    @Query("DELETE FROM climate_sos_alerts WHERE id = :id")
    suspend fun deleteSosAlertById(id: Long)
}
