package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.EcoRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    object Idle : UiState<Nothing>
    object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class EcoViewModel(
    application: Application,
    private val repository: EcoRepository
) : AndroidViewModel(application) {

    // Language configuration ("en", "am", "ru")
    private val _activeLanguage = MutableStateFlow("en")
    val activeLanguage: StateFlow<String> = _activeLanguage.asStateFlow()

    // Screen Tabs navigation (0 = MAP, 1 = PLOTS, 2 = AI SCAN, 3 = SETTINGS)
    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    // Reactive streams from database
    val plotsList: StateFlow<List<PlotEntity>> = repository.allPlots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sosAlertsList: StateFlow<List<ClimateSosEntity>> = repository.allSosAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val diseaseHistoryList: StateFlow<List<PlantDiseaseEntity>> = repository.allDiseases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active AQI fetch state
    private val _aqiState = MutableStateFlow<UiState<AqiCacheEntity>>(UiState.Idle)
    val aqiState: StateFlow<UiState<AqiCacheEntity>> = _aqiState.asStateFlow()

    // Active AI Scan State
    private val _scanState = MutableStateFlow<UiState<PlantDiseaseEntity>>(UiState.Idle)
    val scanState: StateFlow<UiState<PlantDiseaseEntity>> = _scanState.asStateFlow()

    // Offline mode / Sync trigger simulation state
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        // Pre-populate database with default items if empty for premium first-launch UX
        viewModelScope.launch {
            plotsList.first { list ->
                if (list.isEmpty()) {
                    prePopulateDatabase()
                }
                true
            }
            // Trigger an initial AQI lookup to keep design polished
            fetchAqi("Ararat Valley")
        }
    }

    private suspend fun prePopulateDatabase() {
        // Create 2 default user plots
        repository.insertPlot(
            PlotEntity(
                name = "Ararat Valley Plot A",
                cropType = "Grape (Rkatsiteli)",
                locationName = "Ararat Province",
                latitude = 40.0,
                longitude = 44.5,
                areaHectares = 4.2,
                soilMoisture = 48.0,
                healthScore = 94
            )
        )
        repository.insertPlot(
            PlotEntity(
                name = "Shirak Wheat Field",
                cropType = "Winter Wheat",
                locationName = "Gyumri Delta",
                latitude = 40.8,
                longitude = 43.8,
                areaHectares = 12.5,
                soilMoisture = 32.0,
                healthScore = 85
            )
        )

        // Create initial climate SOS crowdsourced alerts
        repository.insertSosAlert(
            ClimateSosEntity(
                alertType = "WATER_SHORTAGE",
                description = "Severe canal supply block near Garni fields. Immediate water redistribution needed.",
                latitude = 40.11,
                longitude = 44.73,
                reportedBy = "User Ararat Valley",
                status = "ACTIVE"
            )
        )
        repository.insertSosAlert(
            ClimateSosEntity(
                alertType = "PEST_OUTBREAK",
                description = "Locust swarm spotted migrating from southern valleys into fruit plots.",
                latitude = 39.85,
                longitude = 44.92,
                reportedBy = "Regional Agronomy Station",
                status = "ACTIVE"
            )
        )
    }

    fun setLanguage(lang: String) {
        _activeLanguage.value = lang
    }

    fun setTab(tabId: Int) {
        _activeTab.value = tabId
    }

    // ----------------------------------------------------
    // Plot Operations
    // ----------------------------------------------------
    fun addNewPlot(name: String, crop: String, location: String, area: Double) {
        viewModelScope.launch {
            val randomLat = 39.5 + (Math.random() * 1.5)
            val randomLng = 43.5 + (Math.random() * 1.5)
            val newPlot = PlotEntity(
                name = name,
                cropType = crop,
                locationName = location,
                latitude = randomLat,
                longitude = randomLng,
                areaHectares = area,
                soilMoisture = 40.0 + (Math.random() * 30.0),
                healthScore = 80 + (Math.random() * 20.0).toInt()
            )
            repository.insertPlot(newPlot)
        }
    }

    fun deletePlot(plotId: Long) {
        viewModelScope.launch {
            repository.deletePlotById(plotId)
        }
    }

    // ----------------------------------------------------
    // Climate SOS reports
    // ----------------------------------------------------
    fun reportClimateSos(alertType: String, description: String) {
        viewModelScope.launch {
            val randomLat = 39.8 + (Math.random() * 1.0)
            val randomLng = 43.8 + (Math.random() * 1.0)
            val alert = ClimateSosEntity(
                alertType = alertType,
                description = description,
                latitude = randomLat,
                longitude = randomLng,
                reportedBy = "EcoSys User Account"
            )
            repository.insertSosAlert(alert)
        }
    }

    fun deleteSosAlert(alertId: Long) {
        viewModelScope.launch {
            repository.deleteSosAlertById(alertId)
        }
    }

    // ----------------------------------------------------
    // AQI & Plant Scanning API Call Simulations
    // ----------------------------------------------------
    fun fetchAqi(location: String) {
        _aqiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val result = repository.fetchAqiInsights(location)
                _aqiState.value = UiState.Success(result)
            } catch (e: Exception) {
                _aqiState.value = UiState.Error(e.localizedMessage ?: "Failed to load AQI")
            }
        }
    }

    fun runDiseaseDiagnosis(crop: String, suspectedDisease: String? = null) {
        _scanState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val report = repository.diagnosePlantDisease(crop, suspectedDisease)
                _scanState.value = UiState.Success(report)
            } catch (e: Exception) {
                _scanState.value = UiState.Error(e.localizedMessage ?: "Scan execution failed")
            }
        }
    }

    fun resetScanState() {
        _scanState.value = UiState.Idle
    }

    fun triggerDatabaseSync() {
        if (_isSyncing.value) return
        _isSyncing.value = true
        viewModelScope.launch {
            delay(1500) // Simulates background network API Sync
            _isSyncing.value = false
        }
    }

    // Helper to extract localized text values dynamically based on selected language
    fun getLocalizedText(en: String, am: String, ru: String): String {
        return when (_activeLanguage.value) {
            "am" -> am.ifEmpty { en }
            "ru" -> ru.ifEmpty { en }
            else -> en
        }
    }
}

class EcoViewModelFactory(
    private val application: Application,
    private val repository: EcoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EcoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EcoViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
