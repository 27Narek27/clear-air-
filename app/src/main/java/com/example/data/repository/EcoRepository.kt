package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Senior Architect Design Specification - FastAPI Integration & Custom Pipeline:
 *
 * 1. Computer Vision Diagnosis:
 *    - Route: `POST /api/v1/cv/diagnose`
 *    - Payload: MultipartForm (image binary data) or JSON with base64 image encoding.
 *    - Header: `Accept-Language` (either "en", "am", or "ru") for localized response schemas.
 *
 * 2. AQI Forecast & Insights:
 *    - Route: `GET /api/v1/aqi/insights?location={location_name}`
 *    - Response: JSON with PM2.5, PM10, AQI index, and multilingual advisory objects.
 *
 * 3. AI Climate Agronomy Advice:
 *    - Route: `GET /api/v1/agronomy/advice/{plot_id}?lang={lang_code}`
 *    - Generates personalized recommendations (Gemini flash API based) according to dynamic soil moisture
 *      indicators and localized climates.
 *
 * 4. Crowdsourced SOS Environmental Mapping:
 *    - Route: `POST /api/v1/sos/report` & `GET /api/v1/sos/alerts`
 *    - Integrates with spatial database parameters for real-time localized warnings.
 *
 * Tradeoffs: Edge TensorFlow Lite vs Cloud/Backend Inference:
 *  - Edge TFLite: Zero latency, offline-capable (perfect for mountain/remote Armenian farm zones),
 *    zero backend compute billing, but limited in model complexity and requires APK updates to update the model.
 *  - Cloud Inference (FastAPI + PyTorch/TensorFlow): Unlimited parameters, dynamic context (can incorporate local weather
 *    and soil forecasts), easy updates, but requires active high-speed internet and increases server compute hosting bills.
 */
class EcoRepository(
    private val plotDao: PlotDao,
    private val aqiCacheDao: AqiCacheDao,
    private val plantDiseaseDao: PlantDiseaseDao,
    private val climateSosDao: ClimateSosDao
) {
    // ----------------------------------------------------
    // User Plots Methods
    // ----------------------------------------------------
    val allPlots: Flow<List<PlotEntity>> = plotDao.getAllPlots()

    suspend fun insertPlot(plot: PlotEntity) = withContext(Dispatchers.IO) {
        plotDao.insertPlot(plot)
    }

    suspend fun updatePlot(plot: PlotEntity) = withContext(Dispatchers.IO) {
        plotDao.updatePlot(plot)
    }

    suspend fun deletePlotById(id: Long) = withContext(Dispatchers.IO) {
        plotDao.deletePlotById(id)
    }

    // ----------------------------------------------------
    // Climate SOS Alerts Methods
    // ----------------------------------------------------
    val allSosAlerts: Flow<List<ClimateSosEntity>> = climateSosDao.getAllSosAlerts()

    suspend fun insertSosAlert(alert: ClimateSosEntity) = withContext(Dispatchers.IO) {
        climateSosDao.insertSosAlert(alert)
    }

    suspend fun updateSosStatus(id: Long, status: String) = withContext(Dispatchers.IO) {
        climateSosDao.updateSosStatus(id, status)
    }

    suspend fun deleteSosAlertById(id: Long) = withContext(Dispatchers.IO) {
        climateSosDao.deleteSosAlertById(id)
    }

    // ----------------------------------------------------
    // Plant Disease History Methods
    // ----------------------------------------------------
    val allDiseases: Flow<List<PlantDiseaseEntity>> = plantDiseaseDao.getAllDiseases()

    suspend fun insertDiseaseReport(disease: PlantDiseaseEntity) = withContext(Dispatchers.IO) {
        plantDiseaseDao.insertDiseaseReport(disease)
    }

    suspend fun deleteDiseaseReportById(id: Long) = withContext(Dispatchers.IO) {
        plantDiseaseDao.deleteDiseaseReportById(id)
    }

    // ----------------------------------------------------
    // AQI Insights (Network Simulation or Local Offline Cache Lookup)
    // ----------------------------------------------------
    val allCachedAqi: Flow<List<AqiCacheEntity>> = aqiCacheDao.getAllCachedAqi()

    suspend fun fetchAqiInsights(location: String): AqiCacheEntity {
        return withContext(Dispatchers.IO) {
            // First check if cached AQI is present and recent (e.g. within 1 hour)
            val cached = aqiCacheDao.getAqiByLocation(location)
            if (cached != null && (System.currentTimeMillis() - cached.cachedTimestamp < 3600000)) {
                return@withContext cached
            }

            // Simulate FastAPI GET request: /api/v1/aqi/insights?location={location}
            delay(1000) // Simulating network lag
            
            // Build mock localized responses that mimic live FastAPI server outputs
            val aqiValue = when (location) {
                "Ararat Valley" -> 42
                "Yerevan Center" -> 118
                "Syunik Uplands" -> 28
                else -> 55
            }
            
            val levelCode = when {
                aqiValue <= 50 -> "GOOD"
                aqiValue <= 100 -> "MODERATE"
                else -> "UNHEALTHY"
            }

            val pm25 = aqiValue * 0.28
            val pm10 = aqiValue * 0.54
            val primaryPollutant = if (aqiValue > 100) "PM2.5" else "O3"

            val advisoryEn = when (levelCode) {
                "GOOD" -> "Air quality is excellent. Ideal for field work and soil aeration."
                "MODERATE" -> "Moderate air. Sensitive crops might experience minor particulate dust."
                else -> "Unhealthy air. Alert: Protect greenhouse workers from high particulate index!"
            }

            val advisoryAm = when (levelCode) {
                "GOOD" -> "Օդի որակը հիանալի է: Իդեալական է դաշտային աշխատանքների և հողի օդափոխության համար:"
                "MODERATE" -> "Չափավոր օդ: Զգայուն մշակաբույսերը կարող են ենթարկվել փոշու աննշան ազդեցության:"
                else -> "Անառողջ օդ: Զգուշացում. Պաշտպանեք ջերմոցային աշխատողներին բարձր փոշուց:"
            }

            val advisoryRu = when (levelCode) {
                "GOOD" -> "Качество воздуха отличное. Идеально для полевых работ и аэрации почвы."
                "MODERATE" -> "Умеренное качество. Чувствительные культуры могут незначительно страдать от пыли."
                else -> "Нездоровая атмосфера. Внимание: защитите рабочих теплиц от высокой концентрации частиц!"
            }

            val newCache = AqiCacheEntity(
                location = location,
                aqiValue = aqiValue,
                levelCode = levelCode,
                pm25 = pm25,
                pm10 = pm10,
                primaryPollutant = primaryPollutant,
                advisoryEn = advisoryEn,
                advisoryAm = advisoryAm,
                advisoryRu = advisoryRu,
                cachedTimestamp = System.currentTimeMillis()
            )

            // Save to Room Cache (Offline-first architecture)
            aqiCacheDao.insertAqi(newCache)
            return@withContext newCache
        }
    }

    // ----------------------------------------------------
    // AI Computer Vision Diagnosis Simulation
    // ----------------------------------------------------
    suspend fun diagnosePlantDisease(crop: String, suspectedDisease: String? = null): PlantDiseaseEntity {
        return withContext(Dispatchers.IO) {
            // Simulate FastAPI call: POST /api/v1/cv/diagnose
            delay(1200)

            val diseaseName = suspectedDisease ?: when (crop) {
                "Tomato" -> "Early Blight (Alternaria solani)"
                "Wheat" -> "Leaf Rust (Puccinia recondita)"
                "Grape" -> "Powdery Mildew (Uncinula necator)"
                "Potato" -> "Late Blight (Phytophthora infestans)"
                else -> "Nutrient Deficiency (Nitrogen)"
            }

            val confidence = 0.88 + (Math.random() * 0.10) // 88% - 98%
            
            val symptoms = when (crop) {
                "Tomato" -> "Dark concentric spots on older leaves, yellowing halos."
                "Wheat" -> "Small, orange-brown pustules on leaves resembling metallic rust."
                "Grape" -> "White powdery dust coating berries and upper dry stems."
                else -> "Leaf margins turning yellow with slowed growth rates."
            }

            // High-fidelity multilingual response payloads as returned by the localization-safe FastAPI contract
            val diagnosisEn = "A high-confidence diagnosis indicates $diseaseName affecting the vegetative foliage."
            val diagnosisAm = "Բարձր ճշգրտության ախտորոշումը ցույց է տալիս, որ բուսական սաղարթի վրա առկա է $diseaseName:"
            val diagnosisRu = "Высокоточный диагноз указывает на наличие заболевания $diseaseName на листьях растения."

            val treatmentsEn = "1. Space crops to improve ventilation.\n2. Apply localized organic copper fungicide.\n3. Implement drip irrigation to avoid wet leaves."
            val treatmentsAm = "1. Հեռավորություն պահպանեք բույսերի միջև օդափոխության համար:\n2. Կիրառել տեղական օրգանական պղնձի ֆունգիցիդ:\n3. Խուսափեք տերևները թրջելուց՝ օգտագործելով կաթիլային ոռոգում:"
            val treatmentsRu = "1. Рассадите растения шире для аэрации.\n2. Локально примените органический медный фунгицид.\n3. Перейдите на капельный полив во избежание намокания листьев."

            val report = PlantDiseaseEntity(
                cropName = crop,
                suspectedDisease = diseaseName,
                confidence = confidence,
                symptomsSummary = symptoms,
                diagnosisEn = diagnosisEn,
                diagnosisAm = diagnosisAm,
                diagnosisRu = diagnosisRu,
                treatmentsEn = treatmentsEn,
                treatmentsAm = treatmentsAm,
                treatmentsRu = treatmentsRu,
                scannedTimestamp = System.currentTimeMillis(),
                isLocalScanOnly = false // Simulates successfully synced with backend
            )

            // Save to local database scanned history for offline-first availability
            plantDiseaseDao.insertDiseaseReport(report)
            return@withContext report
        }
    }
}
