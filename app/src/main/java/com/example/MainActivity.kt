package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.local.EcoDatabase
import com.example.data.repository.EcoRepository
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.EcoViewModel
import com.example.ui.viewmodel.EcoViewModelFactory

/**
 * Единственная Activity приложения (Single-Activity Architecture).
 *
 * TODO (после MVP):
 *  - Перенести инициализацию БД и репозитория в Application-класс или Hilt.
 *    Сейчас они создаются здесь для простоты MVP.
 *    При повороте экрана ViewModel сохраняется (viewModels {}),
 *    но Repository и DB пересоздаются — это нормально, т.к. EcoDatabase
 *    использует синглтон через companion object.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Room Database (синглтон через companion object — безопасно)
        val database = EcoDatabase.getDatabase(this)

        // 2. Repository
        val repository = EcoRepository(
            plotDao        = database.plotDao(),
            aqiCacheDao    = database.aqiCacheDao(),
            plantDiseaseDao = database.plantDiseaseDao(),
            climateSosDao  = database.climateSosDao(),
        )

        // 3. ViewModel (переживает повороты экрана)
        val viewModel: EcoViewModel by viewModels {
            EcoViewModelFactory(application, repository)
        }

        // 4. Compose UI
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}