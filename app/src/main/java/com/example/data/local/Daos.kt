package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────────────────────────────────────
// Участки
// ─────────────────────────────────────────────────────────────────────────────
@Dao
interface PlotDao {
    @Query("SELECT * FROM plots ORDER BY name ASC")
    fun getAllPlots(): Flow<List<PlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlot(plot: PlotEntity): Long

    @Update
    suspend fun updatePlot(plot: PlotEntity)

    @Query("DELETE FROM plots WHERE id = :id")
    suspend fun deletePlotById(id: Long)

    @Query("SELECT * FROM plots WHERE id = :id LIMIT 1")
    suspend fun getPlotById(id: Long): PlotEntity?
}

// ─────────────────────────────────────────────────────────────────────────────
// AQI-кэш
// ИСПРАВЛЕНО: запрос теперь по geohash, а не по строковому названию
// ─────────────────────────────────────────────────────────────────────────────
@Dao
interface AqiCacheDao {
    /**
     * Возвращает самую свежую запись по геохэшу, у которой ещё не истёк TTL.
     * [nowMillis] = System.currentTimeMillis()
     */
    @Query("""
        SELECT * FROM aqi_cache
        WHERE geohash = :geohash AND expires_at > :nowMillis
        ORDER BY observed_at DESC
        LIMIT 1
    """)
    suspend fun getValidAqi(geohash: String, nowMillis: Long): AqiCacheEntity?

    @Query("SELECT * FROM aqi_cache ORDER BY cached_at DESC")
    fun getAllCachedAqi(): Flow<List<AqiCacheEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAqi(aqi: AqiCacheEntity)

    /** Удаляем просроченные записи — вызывать при запуске или фоновой синхронизации */
    @Query("DELETE FROM aqi_cache WHERE expires_at < :nowMillis")
    suspend fun deleteExpired(nowMillis: Long)
}

// ─────────────────────────────────────────────────────────────────────────────
// Болезни растений
// ─────────────────────────────────────────────────────────────────────────────
@Dao
interface PlantDiseaseDao {
    @Query("SELECT * FROM plant_diseases ORDER BY scanned_at DESC")
    fun getAllDiseases(): Flow<List<PlantDiseaseEntity>>

    @Query("SELECT * FROM plant_diseases WHERE plot_id = :plotId ORDER BY scanned_at DESC")
    fun getDiseasesByPlot(plotId: Long): Flow<List<PlantDiseaseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiseaseReport(disease: PlantDiseaseEntity): Long

    @Query("DELETE FROM plant_diseases WHERE id = :id")
    suspend fun deleteDiseaseReportById(id: Long)
}

// ─────────────────────────────────────────────────────────────────────────────
// SOS-алерты
// ─────────────────────────────────────────────────────────────────────────────
@Dao
interface ClimateSosDao {
    @Query("SELECT * FROM climate_sos_alerts ORDER BY timestamp DESC")
    fun getAllSosAlerts(): Flow<List<ClimateSosEntity>>

    @Query("SELECT * FROM climate_sos_alerts WHERE status = 'ACTIVE' ORDER BY timestamp DESC")
    fun getActiveSosAlerts(): Flow<List<ClimateSosEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSosAlert(alert: ClimateSosEntity): Long

    @Query("UPDATE climate_sos_alerts SET status = :status WHERE id = :id")
    suspend fun updateSosStatus(id: Long, status: String)

    @Query("UPDATE climate_sos_alerts SET sync_state = :syncState WHERE id = :id")
    suspend fun updateSyncState(id: Long, syncState: String)

    @Query("DELETE FROM climate_sos_alerts WHERE id = :id")
    suspend fun deleteSosAlertById(id: Long)

    /** Все записи, ожидающие отправки на сервер */
    @Query("SELECT * FROM climate_sos_alerts WHERE sync_state = 'PENDING'")
    suspend fun getPendingAlerts(): List<ClimateSosEntity>
}