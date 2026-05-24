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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize Room Database and system DAOs
        val database = EcoDatabase.getDatabase(this)
        
        // 2. Instantiate Solution-aware Repository
        val repository = EcoRepository(
            plotDao = database.plotDao(),
            aqiCacheDao = database.aqiCacheDao(),
            plantDiseaseDao = database.plantDiseaseDao(),
            climateSosDao = database.climateSosDao()
        )

        // 3. Instantiate dynamic ViewModel via Factory
        val viewModelFactory = EcoViewModelFactory(application, repository)
        val viewModel: EcoViewModel by viewModels { viewModelFactory }

        // 4. Set Content on Material 3 custom cyber-green theme context
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}
