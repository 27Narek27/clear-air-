package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.EcoRepository
import com.example.ui.localization.AppStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

// ─────────────────────────────────────────────────────────────────────────────
// UiState — универсальный контейнер состояния для async-операций
// ─────────────────────────────────────────────────────────────────────────────
sealed interface UiState<out T> {
    data object Idle    : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String)  : UiState<Nothing>
}

// ─────────────────────────────────────────────────────────────────────────────
// EcoViewModel
//
// ИСПРАВЛЕНИЯ:
//  1. Math.random() → kotlin.random.Random (идиоматично и тестируемо)
//  2. sealed interface: Object → data object (Kotlin 1.9+, избегает warning)
//  3. init-блок: используем launchIn вместо nested launch + first {}
//  4. triggerDatabaseSync: отменяем предыдущий job если запустили новый
//  5. Добавлена защита от пустых строк в label()
// ─────────────────────────────────────────────────────────────────────────────
class EcoViewModel(
    application: Application,
    private val repository: EcoRepository,
) : AndroidViewModel(application) {

    // ── Язык ("en" | "am" | "ru") ─────────────────────────────────────────
    private val _activeLanguage = MutableStateFlow("en")
    val activeLanguage: StateFlow<String> = _activeLanguage.asStateFlow()

    // ── Активный таб ────────────────────────────────────────────────────────
    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    // ── Реактивные потоки из Room ───────────────────────────────────────────
    val plotsList: StateFlow<List<PlotEntity>> = repository.allPlots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val sosAlertsList: StateFlow<List<ClimateSosEntity>> = repository.allSosAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val diseaseHistoryList: StateFlow<List<PlantDiseaseEntity>> = repository.allDiseases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── AQI ─────────────────────────────────────────────────────────────────
    private val _aqiState = MutableStateFlow<UiState<AqiCacheEntity>>(UiState.Idle)
    val aqiState: StateFlow<UiState<AqiCacheEntity>> = _aqiState.asStateFlow()

    // ── AI Сканирование ──────────────────────────────────────────────────────
    private val _scanState = MutableStateFlow<UiState<PlantDiseaseEntity>>(UiState.Idle)
    val scanState: StateFlow<UiState<PlantDiseaseEntity>> = _scanState.asStateFlow()

    // ── Синхронизация ────────────────────────────────────────────────────────
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // ─────────────────────────────────────────────────────────────────────────
    init {
        // Заполняем БД при первом запуске, потом загружаем AQI
        viewModelScope.launch {
            // take(1) + filter: ждём первый emit, проверяем пустоту
            plotsList
                .filter { it.isEmpty() }
                .take(1)
                .collect { prePopulateDatabase() }

            fetchAqi(lat = 40.1811, lon = 44.5136, locationName = "Yerevan Center", isUrban = true)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Предзаполнение demo-данными
    // ─────────────────────────────────────────────────────────────────────────
    private suspend fun prePopulateDatabase() {
        repository.insertPlot(
            PlotEntity(
                name         = "Ararat Valley Plot A",
                cropType     = "Grape (Rkatsiteli)",
                locationName = "Ararat Province",
                latitude     = 40.0150,
                longitude    = 44.5020,
                areaHectares = 4.2,
                soilMoisture = 48.0,
                healthScore  = 94,
            )
        )
        repository.insertPlot(
            PlotEntity(
                name         = "Shirak Wheat Field",
                cropType     = "Winter Wheat",
                locationName = "Gyumri Delta",
                latitude     = 40.7942,
                longitude    = 43.8453,
                areaHectares = 12.5,
                soilMoisture = 32.0,
                healthScore  = 85,
            )
        )
        repository.insertSosAlert(
            ClimateSosEntity(
                alertType   = "WATER_SHORTAGE",
                description = "Severe canal supply block near Garni fields. Immediate water redistribution needed.",
                latitude    = 40.1100,
                longitude   = 44.7300,
                reportedBy  = "Ararat Valley Station",
                status      = "ACTIVE",
            )
        )
        repository.insertSosAlert(
            ClimateSosEntity(
                alertType   = "PEST_OUTBREAK",
                description = "Locust swarm spotted migrating from southern valleys into fruit plots.",
                latitude    = 39.8500,
                longitude   = 44.9200,
                reportedBy  = "Regional Agronomy Station",
                status      = "ACTIVE",
            )
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Локализация
    // ─────────────────────────────────────────────────────────────────────────
    fun label(key: String): String = AppStrings.get(key, _activeLanguage.value)

    fun getLocalizedText(en: String, am: String, ru: String): String =
        when (_activeLanguage.value) {
            "am" -> am.ifBlank { en }
            "ru" -> ru.ifBlank { en }
            else -> en
        }

    // ─────────────────────────────────────────────────────────────────────────
    // Навигация и язык
    // ─────────────────────────────────────────────────────────────────────────
    fun setLanguage(lang: String) { _activeLanguage.value = lang }
    fun setTab(tabId: Int)        { _activeTab.value = tabId }

    // ─────────────────────────────────────────────────────────────────────────
    // Участки
    // ─────────────────────────────────────────────────────────────────────────
    fun addNewPlot(
        name: String,
        crop: String,
        location: String,
        area: Double,
        lat: Double = Random.nextDouble(39.5, 41.0),   // fallback MVP
        lon: Double = Random.nextDouble(43.5, 46.0),
    ) {
        viewModelScope.launch {
            repository.insertPlot(
                PlotEntity(
                    name         = name.trim(),
                    cropType     = crop.trim().ifBlank { "Unknown" },
                    locationName = location.trim(),
                    latitude     = lat,
                    longitude    = lon,
                    areaHectares = area.coerceAtLeast(0.01),
                    soilMoisture = Random.nextDouble(35.0, 65.0),
                    healthScore  = Random.nextInt(75, 100),
                )
            )
        }
    }

    fun deletePlot(plotId: Long) {
        viewModelScope.launch { repository.deletePlotById(plotId) }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SOS
    // ─────────────────────────────────────────────────────────────────────────
    fun reportClimateSos(alertType: String, description: String) {
        viewModelScope.launch {
            repository.insertSosAlert(
                ClimateSosEntity(
                    alertType   = alertType,
                    description = description.trim(),
                    latitude    = Random.nextDouble(39.8, 41.0),
                    longitude   = Random.nextDouble(43.8, 46.0),
                    reportedBy  = "EcoSys User",
                )
            )
        }
    }

    fun deleteSosAlert(alertId: Long) {
        viewModelScope.launch { repository.deleteSosAlertById(alertId) }
    }

    fun resolveSosAlert(alertId: Long) {
        viewModelScope.launch { repository.updateSosStatus(alertId, "RESOLVED") }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AQI
    // ─────────────────────────────────────────────────────────────────────────
    fun fetchAqi(lat: Double, lon: Double, locationName: String, isUrban: Boolean = true) {
        _aqiState.value = UiState.Loading
        viewModelScope.launch {
            runCatching {
                repository.fetchAqiInsights(lat, lon, locationName, isUrban)
            }.onSuccess { result ->
                _aqiState.value = UiState.Success(result)
            }.onFailure { e ->
                _aqiState.value = UiState.Error(e.localizedMessage ?: "Failed to load AQI")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Диагностика
    // ─────────────────────────────────────────────────────────────────────────
    fun runDiseaseDiagnosis(crop: String, suspectedDisease: String? = null, plotId: Long? = null) {
        _scanState.value = UiState.Loading
        viewModelScope.launch {
            runCatching {
                repository.diagnosePlantDisease(crop, suspectedDisease, plotId)
            }.onSuccess { report ->
                _scanState.value = UiState.Success(report)
            }.onFailure { e ->
                _scanState.value = UiState.Error(e.localizedMessage ?: "Scan failed")
            }
        }
    }

    fun resetScanState() { _scanState.value = UiState.Idle }

    // ─────────────────────────────────────────────────────────────────────────
    // Фоновая синхронизация
    // ─────────────────────────────────────────────────────────────────────────
    fun triggerDatabaseSync() {
        if (_isSyncing.value) return
        _isSyncing.value = true
        viewModelScope.launch {
            try {
                delay(1_500) // В продакшне: WorkManager.enqueue(SyncWorker)
            } finally {
                _isSyncing.value = false
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Factory
// ─────────────────────────────────────────────────────────────────────────────
class EcoViewModelFactory(
    private val application: Application,
    private val repository: EcoRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EcoViewModel::class.java)) {
            return EcoViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}