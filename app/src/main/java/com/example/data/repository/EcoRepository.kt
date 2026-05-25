package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * EcoRepository — единственный источник правды для UI.
 *
 * Стратегия offline-first:
 *  1. Сначала проверяем локальный Room-кэш.
 *  2. Если данные свежие (TTL не истёк) — возвращаем без сетевого запроса.
 *  3. Если кэш пустой или устаревший — идём в сеть, сохраняем, возвращаем.
 *
 * ИСПРАВЛЕНИЯ:
 *  - Math.random() → kotlin.random.Random (идиоматично, тестируемо через seed)
 *  - confidence вычисляется через Random.nextDouble() с явным диапазоном
 *  - Убраны magic numbers, вынесены в константы
 *
 * TODO (после MVP):
 *  Замените блоки "// NETWORK STUB" на вызовы Retrofit-сервиса.
 *
 * FastAPI endpoints:
 *  POST /api/v1/diagnosis/plant      — мультипарт с изображением
 *  GET  /api/v1/aqi/insights         — ?lat=&lon=&locale=
 *  POST /api/v1/agronomy/advice      — JSON с параметрами участка
 *  POST /api/v1/sos/reports
 *  GET  /api/v1/sos/reports          — ?bbox=&page=&page_size=
 */
class EcoRepository(
    private val plotDao: PlotDao,
    private val aqiCacheDao: AqiCacheDao,
    private val plantDiseaseDao: PlantDiseaseDao,
    private val climateSosDao: ClimateSosDao,
) {

    // ── TTL-константы ───────────────────────────────────────────────────────
    private val AQI_TTL_URBAN_MS = 15 * 60 * 1_000L   // 15 минут
    private val AQI_TTL_RURAL_MS = 30 * 60 * 1_000L   // 30 минут

    // ── Участки ─────────────────────────────────────────────────────────────
    val allPlots: Flow<List<PlotEntity>> = plotDao.getAllPlots()

    suspend fun insertPlot(plot: PlotEntity): Long = withContext(Dispatchers.IO) {
        plotDao.insertPlot(plot)
    }

    suspend fun updatePlot(plot: PlotEntity) = withContext(Dispatchers.IO) {
        plotDao.updatePlot(plot)
    }

    suspend fun deletePlotById(id: Long) = withContext(Dispatchers.IO) {
        plotDao.deletePlotById(id)
    }

    // ── SOS-алерты ──────────────────────────────────────────────────────────
    val allSosAlerts: Flow<List<ClimateSosEntity>> = climateSosDao.getAllSosAlerts()

    suspend fun insertSosAlert(alert: ClimateSosEntity): Long = withContext(Dispatchers.IO) {
        climateSosDao.insertSosAlert(alert)
    }

    suspend fun updateSosStatus(id: Long, status: String) = withContext(Dispatchers.IO) {
        climateSosDao.updateSosStatus(id, status)
    }

    suspend fun deleteSosAlertById(id: Long) = withContext(Dispatchers.IO) {
        climateSosDao.deleteSosAlertById(id)
    }

    // ── Болезни растений ────────────────────────────────────────────────────
    val allDiseases: Flow<List<PlantDiseaseEntity>> = plantDiseaseDao.getAllDiseases()

    suspend fun insertDiseaseReport(disease: PlantDiseaseEntity): Long = withContext(Dispatchers.IO) {
        plantDiseaseDao.insertDiseaseReport(disease)
    }

    suspend fun deleteDiseaseReportById(id: Long) = withContext(Dispatchers.IO) {
        plantDiseaseDao.deleteDiseaseReportById(id)
    }

    // ── AQI ─────────────────────────────────────────────────────────────────

    /**
     * Псевдо-геохэш: округляем до 2 знаков — достаточно для MVP (~1 км точность).
     * В продакшне: ch.hsr.geohash или com.github.davidmoten:geo
     */
    private fun makeGeohash(lat: Double, lon: Double): String {
        val latR = (lat * 100).roundToInt() / 100.0
        val lonR = (lon * 100).roundToInt() / 100.0
        return "${latR}_${lonR}"
    }

    /**
     * Получить AQI для заданной геопозиции.
     * Offline-first: кэш → сеть → сохранить → вернуть.
     */
    suspend fun fetchAqiInsights(
        lat: Double,
        lon: Double,
        locationName: String,
        isUrban: Boolean = true,
    ): AqiCacheEntity = withContext(Dispatchers.IO) {
        val now     = System.currentTimeMillis()
        val geohash = makeGeohash(lat, lon)

        // 1. Проверяем кэш
        val cached = aqiCacheDao.getValidAqi(geohash, now)
        if (cached != null) return@withContext cached

        // 2. Housekeeping: удаляем просроченное
        aqiCacheDao.deleteExpired(now)

        // 3. NETWORK STUB — заменить на Retrofit:
        //    val dto = apiService.getAqiInsights(lat, lon, locale)
        delay(900)

        val aqiValue = when {
            locationName.contains("Yerevan", ignoreCase = true) -> 118
            locationName.contains("Ararat",  ignoreCase = true) -> 42
            locationName.contains("Syunik",  ignoreCase = true) -> 28
            locationName.contains("Gyumri",  ignoreCase = true) -> 65
            else -> 55
        }
        val levelCode = when {
            aqiValue <= 50  -> "GOOD"
            aqiValue <= 100 -> "MODERATE"
            else            -> "UNHEALTHY"
        }
        val advisoryEn = when (levelCode) {
            "GOOD"     -> "Air quality is excellent. Ideal for field work and soil aeration."
            "MODERATE" -> "Moderate air. Sensitive crops may experience minor particulate dust."
            else       -> "Unhealthy air. Protect greenhouse workers from high particulate index!"
        }
        val advisoryAm = when (levelCode) {
            "GOOD"     -> "Օդի որակը հիանալի է։ Իդեալական դաշտային աշխատանքների համար։"
            "MODERATE" -> "Չափավոր օդ։ Զգայուն մշակաբույսերը կարող են ենթարկվել փոշու։"
            else       -> "Անառողջ օդ։ Պաշտպանեք ջերմոցային աշխատողներին։"
        }
        val advisoryRu = when (levelCode) {
            "GOOD"     -> "Качество воздуха отличное. Идеально для полевых работ."
            "MODERATE" -> "Умеренное качество. Чувствительные культуры могут страдать от пыли."
            else       -> "Нездоровая атмосфера. Защитите рабочих теплиц от частиц!"
        }

        val ttl = if (isUrban) AQI_TTL_URBAN_MS else AQI_TTL_RURAL_MS
        val entity = AqiCacheEntity(
            geohash          = geohash,
            observedAt       = now,
            locationName     = locationName,
            aqiValue         = aqiValue,
            levelCode        = levelCode,
            pm25             = aqiValue * 0.28,
            pm10             = aqiValue * 0.54,
            primaryPollutant = if (aqiValue > 100) "PM2.5" else "O3",
            advisoryEn       = advisoryEn,
            advisoryAm       = advisoryAm,
            advisoryRu       = advisoryRu,
            expiresAt        = now + ttl,
        )
        aqiCacheDao.insertAqi(entity)
        entity
    }

    // ── Диагностика ─────────────────────────────────────────────────────────

    /**
     * Запустить AI-диагностику растений.
     * Результат сохраняется в Room для offline-доступа.
     *
     * ИСПРАВЛЕНО: confidence использует kotlin.random.Random, не Math.random()
     */
    suspend fun diagnosePlantDisease(
        crop: String,
        suspectedDisease: String? = null,
        plotId: Long? = null,
    ): PlantDiseaseEntity = withContext(Dispatchers.IO) {
        // NETWORK STUB — заменить на:
        //   val dto = apiService.diagnosePlant(imageFile, crop, locale)
        delay(1200)

        val diseaseName = suspectedDisease ?: when (crop) {
            "Tomato"  -> "Early Blight (Alternaria solani)"
            "Wheat"   -> "Leaf Rust (Puccinia recondita)"
            "Grape"   -> "Powdery Mildew (Uncinula necator)"
            "Potato"  -> "Late Blight (Phytophthora infestans)"
            else      -> "Nutrient Deficiency (Nitrogen)"
        }

        // Confidence 88–98% — ИСПРАВЛЕНО: kotlin.random.Random
        val confidence = Random.nextDouble(0.88, 0.98)

        val symptoms = when (crop) {
            "Tomato" -> "Dark concentric spots on older leaves, yellowing halos."
            "Wheat"  -> "Small orange-brown pustules on leaves resembling metallic rust."
            "Grape"  -> "White powdery dust coating berries and upper dry stems."
            else     -> "Leaf margins turning yellow with slowed growth rates."
        }

        val treatmentsEn = "1. Space crops to improve ventilation.\n" +
                "2. Apply organic copper fungicide locally.\n" +
                "3. Use drip irrigation to avoid wetting leaves."
        val treatmentsAm = "1. Հեռավորություն պահպանեք բույսերի միջև։\n" +
                "2. Կիրառել օրգանական պղնձի ֆունգիցիդ։\n" +
                "3. Օգտագործեք կաթիլային ոռոգում։"
        val treatmentsRu = "1. Увеличьте расстояние между растениями.\n" +
                "2. Локально нанесите органический медный фунгицид.\n" +
                "3. Перейдите на капельный полив."

        val entity = PlantDiseaseEntity(
            plotId           = plotId,
            cropName         = crop,
            suspectedDisease = diseaseName,
            confidence       = confidence,
            symptomsSummary  = symptoms,
            diagnosisEn      = "High-confidence diagnosis: $diseaseName affecting the vegetative foliage.",
            diagnosisAm      = "Բարձր ճշգրտության ախտորոշում. $diseaseName բուսական սաղարթի վրա:",
            diagnosisRu      = "Высокоточный диагноз: $diseaseName поражает листья растения.",
            treatmentsEn     = treatmentsEn,
            treatmentsAm     = treatmentsAm,
            treatmentsRu     = treatmentsRu,
            isLocalScanOnly  = false,
        )

        plantDiseaseDao.insertDiseaseReport(entity)
        entity
    }
}