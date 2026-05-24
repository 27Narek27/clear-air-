package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.EcoViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: EcoViewModel) {
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val activeLanguage by viewModel.activeLanguage.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    
    // UI strings corresponding to dynamic localization dictionaries
    val dictEn = mapOf(
        "app_tag" to "Simple Air & Farm Monitor",
        "map_tab" to "Map",
        "plots_tab" to "Plots",
        "scan_tab" to "Scan",
        "settings_tab" to "Settings",
        "add_plot" to "Add plot",
        "report_sos" to "Report issue",
        "sos_type" to "Warning Type",
        "sos_desc" to "Describe Environmental Danger",
        "cancel" to "Cancel",
        "publish" to "Publish Alert",
        "plot_details" to "Plot Details",
        "soil_moisture" to "Soil Moisture",
        "health_rating" to "Crop Health Score",
        "scan_plant" to "Plant Scan",
        "choose_crop" to "Choose crop",
        "trigger_scan" to "Scan plant",
        "history_scans" to "Scan history",
        "sys_architecture" to "Senior Architect System Specs & FastAPI Diagram",
        "architecture_explain" to "This offline-first mobile app synchronizes client-side Room entities with localized FastAPI endpoints in Armenia, Yerevan, and world clusters. Crop AI and Air Quality analytics are served locally or in-cloud according to network metrics.",
        "lang_switch" to "Configure Dynamic System Locale",
        "sync_local" to "Sync Offline Local Storage DB",
        "sync_in_progress" to "Synchronizing with Remote FastAPI...",
        "no_scans" to "No plant scans recorded yet. Capture farm specimens above.",
        "no_plots" to "No agricultural plots recorded. Register an initial block to begin monitoring.",
        "report_issue" to "Report SOS Node",
        "close" to "Close",
        "water_shortage" to "Water Shortage / Canal Block",
        "pest_outbreak" to "Pest Invasion / Insects",
        "frost_alert" to "Sudden Mountain Frost Danger",
        "fire_alert" to "Active Brushfire Alert",
        "selected_marker" to "Selected Location Context"
    )

    val dictAm = mapOf(
        "app_tag" to "Պարզ օդի և դաշտի մոնիթոր",
        "map_tab" to "Քարտեզ",
        "plots_tab" to "Հողամասեր",
        "scan_tab" to "Սկան",
        "settings_tab" to "Կարգավորումներ",
        "add_plot" to "Գրանցել Հողամաս",
        "report_sos" to "Հայտնել Կլիմայական SOS",
        "sos_type" to "Վտանգի Տեսակը",
        "sos_desc" to "Նկարագրեք էկոլոգիական վտանգը",
        "cancel" to "Չեղարկել",
        "publish" to "Հրապարակել ահազանգ",
        "plot_details" to "Հողի մանրամասները",
        "soil_moisture" to "Հողի խոնավություն",
        "health_rating" to "Բույսերի առողջության միավոր",
        "scan_plant" to "Սկանավորել Տերևի Օրինակը",
        "choose_crop" to "Ընտրեք Մշակաբույսի Տեսակը",
        "trigger_scan" to "Սկանավորել բույսը",
        "history_scans" to "Սարքի Ախտորոշումների Պատմություն",
        "sys_architecture" to "Համակարգի Ճարտարապետություն և Ֆասթ-ԱՊԻ Դիագրամ",
        "architecture_explain" to "Այս անցանց-առաջնային բջջային հավելվածը սինխրոնացնում է Room տվյալների բազան տեղայնացված FastAPI սերվերների հետ Հայաստանում: Բույսերի արհեստական բանականության սկանավորումները իրականացվում են տեղում կամ ամպում:",
        "lang_switch" to "Համակարգի Դինամիկ Լեզուն",
        "sync_local" to "Սինխրոնացնել Տեղական ՏԲ",
        "sync_in_progress" to "Սինխրոնացվում է FastAPI սերվերի հետ...",
        "no_scans" to "Ոչ մի սկանավորում չի գտնվել: Կատարեք առաջին սկանը վերևում:",
        "no_plots" to "Հողամասեր չկան: Գրանցեք ձեր առաջին հողամասը՝ մոնիտորինգի համար:",
        "report_issue" to "Հաղորդել SOS խնդիր",
        "close" to "Փակել",
        "water_shortage" to "Ջրի սակավություն / Ջրանցքի խցանում",
        "pest_outbreak" to "Վնասատուների ներխուժում",
        "frost_alert" to "Հանկարծակի ցրտահարության վտանգ",
        "fire_alert" to "Հրդեհի վտանգ",
        "selected_marker" to "Ընտրված վայրի տվյալները"
    )

    val dictRu = mapOf(
        "app_tag" to "Простой монитор воздуха и полей",
        "map_tab" to "Карта",
        "plots_tab" to "Участки",
        "scan_tab" to "Скан",
        "settings_tab" to "Настройки",
        "add_plot" to "Регистрация Участка",
        "report_sos" to "Сообщить о Climate SOS",
        "sos_type" to "Тип Угрозы",
        "sos_desc" to "Опишите экологическую опасность",
        "cancel" to "Отмена",
        "publish" to "Публикация",
        "plot_details" to "Параметры Участка",
        "soil_moisture" to "Влагосодержание Почвы",
        "health_rating" to "Индекс Здоровья Культуры",
        "scan_plant" to "Сканировать Образец Листа",
        "choose_crop" to "Выберите Сельхозкультуру",
        "trigger_scan" to "Сканировать растение",
        "history_scans" to "История Диагностики на Устройстве",
        "sys_architecture" to "Спецификация Архитектуры и FastAPI Модель",
        "architecture_explain" to "Данное автономно-приоритетное приложение синхронизирует локальные Room-ресурсы с FastAPI кластерами. ИИ диагностика листьев выполняется локально через TFLite или отправляется в облачный API.",
        "lang_switch" to "Настроить Локаль Системы",
        "sync_local" to "Синхронизация Базы Данных",
        "sync_in_progress" to "Синхронизация с серверами FastAPI...",
        "no_scans" to "Сканы не обнаружены. Проведите сканирование листа выше.",
        "no_plots" to "Участки не зарегистрированы. Добавьте первый участок для слежения.",
        "report_issue" to "Сообщить о бедствии SOS",
        "close" to "Закрыть",
        "water_shortage" to "Дефицит Воды / Канал Заблокирован",
        "pest_outbreak" to "Нашествие Вредителей / Насекомых",
        "frost_alert" to "Заморозки в горных районах",
        "fire_alert" to "Лесной Пожар",
        "selected_marker" to "Данные Выбранного Объекта"
    )

    // Helper translation string resolver lookup
    fun getLabel(key: String): String {
        return when (activeLanguage) {
            "am" -> dictAm[key] ?: dictEn[key] ?: key
            "ru" -> dictRu[key] ?: dictEn[key] ?: key
            else -> dictEn[key] ?: key
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E141B)),
        bottomBar = {
            Column(modifier = Modifier.background(Color(0xFF121A24))) {
                // Custom Material 3 styled Navigation Bar
                NavigationBar(
                    containerColor = DeepCharcoal,
                    tonalElevation = 8.dp,
                    modifier = Modifier.navigationBarsPadding(),
                    windowInsets = WindowInsets.navigationBars
                ) {
                    val items = listOf(
                        Triple(0, getLabel("map_tab"), Icons.Default.Map),
                        Triple(1, getLabel("plots_tab"), Icons.Default.Spa),
                        Triple(2, getLabel("scan_tab"), Icons.Default.CameraAlt),
                        Triple(3, getLabel("settings_tab"), Icons.Default.Settings)
                    )
                    items.forEach { (tabId, label, icon) ->
                        val isSelected = activeTab == tabId
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.setTab(tabId) },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) CyberBlack else Color.White.copy(alpha = 0.5f)
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    color = if (isSelected) NeonGreen else Color.White.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontFamily = FontFamily.Monospace
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = NeonGreen,
                                selectedIconColor = CyberBlack,
                                unselectedIconColor = Color.White.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
                
                // Bottom Gesture line area padding
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(Color(0xFF121A24))
                ) {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(4.dp)
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(2.dp))
                            .align(Alignment.Center)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0E141B))
        ) {
            // App Status bar line indicator simulator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = remember { LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) },
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Wi-Fi simulator dot
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(NeonGreen, CircleShape)
                        )
                        // Battery indicator
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(7.dp)
                                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(1.dp))
                                .padding(0.5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(0.8f)
                                    .background(Color.White.copy(alpha = 0.7f))
                            )
                        }
                    }
                }
            }

            // Real App Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderGrey)
                    .background(Color(0xFF121A24))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ClearAir",
                            color = NeonGreen,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(NeonGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .border(0.5.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "NEW",
                                color = NeonGreen,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Text(
                        text = getLabel("app_tag"),
                        color = NeonGreen.copy(alpha = 0.6f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cache Status Tag
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSyncing) AlertOrange.copy(alpha = 0.15f) else NeonGreen.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSyncing) AlertOrange.copy(alpha = 0.3f) else NeonGreen.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(if (isSyncing) AlertOrange else NeonGreen, CircleShape)
                            )
                            Text(
                                text = if (isSyncing) "Syncing..." else "Offline ready",
                                color = if (isSyncing) AlertOrange else NeonGreen,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Multi-language active indicator pill
                    Box(
                        modifier = Modifier
                            .background(BorderGrey, RoundedCornerShape(8.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = activeLanguage.uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Divider(color = BorderGrey, thickness = 1.dp)

            // Animated Screen Switching Container
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) + slideInHorizontally(animationSpec = tween(220)) { it / 8 } togetherWith
                        fadeOut(animationSpec = tween(180)) + slideOutHorizontally(animationSpec = tween(180)) { -it / 10 }
                },
                label = "tabTransition",
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) { tab ->
                when (tab) {
                    0 -> MapTabScreen(viewModel = viewModel, getLabel = ::getLabel)
                    1 -> PlotsTabScreen(viewModel = viewModel, getLabel = ::getLabel)
                    2 -> ScanTabScreen(viewModel = viewModel, getLabel = ::getLabel)
                    else -> SettingsTabScreen(viewModel = viewModel, getLabel = ::getLabel)
                }
            }
        }
    }
}

// =========================================================================================
// TAB 0: INTERACTIVE MAP SCREEN & CROWDSOURCED CLIMATE SOS WRITER
// =========================================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapTabScreen(
    viewModel: EcoViewModel,
    getLabel: (String) -> String
) {
    val plots by viewModel.plotsList.collectAsStateWithLifecycle()
    val sosAlerts by viewModel.sosAlertsList.collectAsStateWithLifecycle()
    val aqiState by viewModel.aqiState.collectAsStateWithLifecycle()
    
    var selectedPlotId by remember { mutableStateOf<Long?>(null) }
    var selectedSosId by remember { mutableStateOf<Long?>(null) }
    var showReportSosDialog by remember { mutableStateOf(false) }
    var showAddPlotDialog by remember { mutableStateOf(false) }

    // Forms fields for SOS dialog
    var sosType by remember { mutableStateOf("WATER_SHORTAGE") }
    var sosDescription by remember { mutableStateOf("") }

    // Forms fields for Add Plot dialog
    var plotName by remember { mutableStateOf("") }
    var cropType by remember { mutableStateOf("Tomato (Early Blight Resistance)") }
    var plotLocation by remember { mutableStateOf("Ararat Valley") }
    var plotSize by remember { mutableStateOf("4.5") }

    val activeLanguage by viewModel.activeLanguage.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        // Futuristic Interactive Grid Map Canvas implementation
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEFF3F8))
                .pointerInput(plots, sosAlerts) {
                    detectTapGestures { offset ->
                        // Detect click on plots elements mapped to grid
                        val w = size.width
                        val h = size.height

                        var clickedPlot: PlotEntity? = null
                        plots.forEachIndexed { index, plot ->
                            val cx = w * 0.2f + (plot.latitude % 1.0f) * w * 1.5f
                            val cy = h * 0.3f + (plot.longitude % 1.0f) * h * 1.2f
                            val distance = Math.hypot((offset.x - cx).toDouble(), (offset.y - cy).toDouble())
                            if (distance < 40.0) {
                                clickedPlot = plot
                            }
                        }

                        var clickedSos: ClimateSosEntity? = null
                        sosAlerts.forEachIndexed { index, alert ->
                            val cx = w * 0.4f + (alert.latitude % 1.0f) * w * 1.6f
                            val cy = h * 0.5f - (alert.longitude % 1.0f) * h * 1.5f
                            val distance = Math.hypot((offset.x - cx).toDouble(), (offset.y - cy).toDouble())
                            if (distance < 40.0) {
                                clickedSos = alert
                            }
                        }

                        if (clickedPlot != null) {
                            selectedPlotId = clickedPlot!!.id
                            selectedSosId = null
                            viewModel.fetchAqi(clickedPlot!!.locationName)
                        } else if (clickedSos != null) {
                            selectedSosId = clickedSos!!.id
                            selectedPlotId = null
                        } else {
                            // Reset selections if clicked on background grid empty spot
                            selectedPlotId = null
                            selectedSosId = null
                        }
                    }
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // 1. Draw glowing grid lines in 40dp units
            val stepSize = 80.dp.toPx()
            var x = 0f
            while (x < canvasWidth) {
                drawLine(
                    color = Color(0xFFD5DEE8),
                    start = Offset(x, 0f),
                    end = Offset(x, canvasHeight),
                    strokeWidth = 1.dp.toPx()
                )
                x += stepSize
            }
            var y = 0f
            while (y < canvasHeight) {
                drawLine(
                    color = Color(0xFFD5DEE8),
                    start = Offset(0f, y),
                    end = Offset(canvasWidth, y),
                    strokeWidth = 1.dp.toPx()
                )
                y += stepSize
            }

            // 2. Draw glowing concentric crop target guidelines
            drawCircle(
                color = Color(0xFF9FC5E8).copy(alpha = 0.35f),
                radius = canvasWidth * 0.3f,
                center = Offset(canvasWidth / 2f, canvasHeight / 2f),
                style = Stroke(width = 1.dp.toPx())
            )

            // 3. Draw active Plot coordinate nodes
            plots.forEach { plot ->
                val px = canvasWidth * 0.2f + (plot.latitude % 1.0f) * canvasWidth * 1.5f
                val py = canvasHeight * 0.3f + (plot.longitude % 1.0f) * canvasHeight * 1.2f

                // Pulsing outer halo for healthy vegetation
                drawCircle(
                    color = Color(0xFF6FCF97).copy(alpha = 0.30f),
                    radius = 32.dp.toPx(),
                    center = Offset(px.toFloat(), py.toFloat())
                )
                drawCircle(
                    color = Color(0xFF2F855A),
                    radius = 6.dp.toPx(),
                    center = Offset(px.toFloat(), py.toFloat())
                )
            }

            // 4. Draw Climate SOS alerts in orange/red
            sosAlerts.forEach { alert ->
                val ax = canvasWidth * 0.4f + (alert.latitude % 1.0f) * canvasWidth * 1.6f
                val ay = canvasHeight * 0.5f - (alert.longitude % 1.0f) * canvasHeight * 1.5f

                // Outer danger radar beacon
                drawCircle(
                    color = AlertRed.copy(alpha = 0.2f),
                    radius = 28.dp.toPx(),
                    center = Offset(ax.toFloat(), ay.toFloat())
                )
                drawCircle(
                    color = AlertRed,
                    radius = 5.dp.toPx(),
                    center = Offset(ax.toFloat(), ay.toFloat())
                )
            }
        }

        // Overlay Instructions Map helper banner
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .background(DeepCharcoal.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                .border(1.dp, BorderGrey, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "info",
                    tint = NeonGreen,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (activeLanguage == "am") "Սեղմեք քարտեզի կետերին՝ մանրամասների համար" 
                           else if (activeLanguage == "ru") "Нажмите на точки карты для инфо" 
                           else "Tap map markers to see details",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // FLOATING AQI STATUS AND CROP METRICS DISPLAY OVERLAY
        selectedPlotId?.let { plotId ->
            plots.find { it.id == plotId }?.let { plot ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 110.dp)
                        .fillMaxWidth()
                        .background(DeepCharcoal.copy(alpha = 0.95f), RoundedCornerShape(20.dp))
                        .border(1.dp, MutedTeal, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = plot.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "SENSOR IP: 10.244.18.2 // LAT: ${String.format("%.3f", plot.latitude)}",
                                    color = TextMuted,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            IconButton(onClick = { selectedPlotId = null }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "close",
                                    tint = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Divider(color = BorderGrey, thickness = 0.5.dp)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Soil Moisture Metrics Unit
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(CyberBlack, RoundedCornerShape(12.dp))
                                    .border(1.dp, BorderGrey, RoundedCornerShape(12.dp))
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = getLabel("soil_moisture").uppercase(),
                                    color = TextMuted,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${plot.soilMoisture.toInt()}%",
                                    color = SoftTeal,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            // Crop Health metric
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(CyberBlack, RoundedCornerShape(12.dp))
                                    .border(1.dp, BorderGrey, RoundedCornerShape(12.dp))
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = getLabel("health_rating").uppercase(),
                                    color = TextMuted,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${plot.healthScore}%",
                                    color = NeonGreen,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Localized AQI Fetching Block from Room database or FastAPI simulations
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CyberBlack, RoundedCornerShape(12.dp))
                                .border(1.dp, BorderGrey, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            when (val state = aqiState) {
                                is UiState.Loading -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            color = NeonGreen,
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            text = "Pulling Live AQI Insights from FastAPI...",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                                is UiState.Success -> {
                                    val aqi = state.data
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.CloudSync,
                                                    contentDescription = "aqi",
                                                    tint = NeonGreen,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "${getLabel("aqi_status")}: ${plot.locationName}",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = "AQI: ${aqi.aqiValue} [${aqi.levelCode}]",
                                                color = if (aqi.aqiValue <= 50) NeonGreen else AlertOrange,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        // Linear quality Bar
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(5.dp)
                                                .background(BorderGrey, RoundedCornerShape(4.dp))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(Math.min(1.0f, aqi.aqiValue / 150.0f))
                                                    .fillMaxHeight()
                                                    .background(
                                                        if (aqi.aqiValue <= 50) NeonGreen else AlertOrange,
                                                        RoundedCornerShape(4.dp)
                                                    )
                                            )
                                        }

                                        Text(
                                            text = viewModel.getLocalizedText(aqi.advisoryEn, aqi.advisoryAm, aqi.advisoryRu),
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 10.sp,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                                else -> {
                                    Text(
                                        text = "AQI Idle Mode. Click plot to poll API.",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // FLOATING CLIMATE SOS INDIVIDUAL INFO CONTEXT
        selectedSosId?.let { sosId ->
            sosAlerts.find { it.id == sosId }?.let { alert ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 110.dp)
                        .fillMaxWidth()
                        .background(DeepCharcoal.copy(alpha = 0.95f), RoundedCornerShape(20.dp))
                        .border(1.dp, AlertRed, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "warning",
                                    tint = AlertRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = alert.alertType.uppercase().replace("_", " "),
                                    color = AlertRed,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp
                                )
                            }
                            IconButton(onClick = { selectedSosId = null }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "close",
                                    tint = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Divider(color = BorderGrey, thickness = 0.5.dp)

                        Text(
                            text = alert.description,
                            color = Color.White,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "REPORTED: ${alert.reportedBy}",
                                color = TextMuted,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Button(
                                onClick = {
                                    viewModel.deleteSosAlert(alert.id)
                                    selectedSosId = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AlertRed.copy(alpha = 0.2f)),
                                border = BorderStroke(1.dp, AlertRed),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text(
                                    text = "DISMISS / SOLVE",
                                    color = AlertRed,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // SIDE REGISTRATION FLOATING ACTION BUTTONS
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Add plot registration floating button
            FloatingActionButton(
                onClick = { showAddPlotDialog = true },
                containerColor = NeonGreen,
                contentColor = CyberBlack,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddHome,
                    contentDescription = "Register Plot"
                )
            }

            // Report SOS warning floating button
            FloatingActionButton(
                onClick = { showReportSosDialog = true },
                containerColor = AlertRed,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = getLabel("report_sos")
                )
            }
        }
    }

    // DIALOG: RECORD NEW CLIMATE OUTBREAK WARNING (SOS MAPPING)
    if (showReportSosDialog) {
        AlertDialog(
            onDismissRequest = { showReportSosDialog = false },
            title = {
                Text(
                    text = getLabel("report_sos"),
                    color = AlertRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            containerColor = DeepCharcoal,
            shape = RoundedCornerShape(20.dp),
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Your telemetry data will pinpoint this environmental threat on coordinates map instantly.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Text(
                        text = getLabel("sos_type"),
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    // Selection Row for alert Type
                    val sosTypes = listOf("WATER_SHORTAGE", "PEST_OUTBREAK", "FROST_ALERT", "FIRE_ALERT")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        sosTypes.forEach { type ->
                            val active = sosType == type
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (active) AlertRed.copy(alpha = 0.2f) else CyberBlack,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (active) AlertRed else BorderGrey,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { sosType = type }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = type.replace("_", " "),
                                    color = if (active) Color.White else Color.White.copy(alpha = 0.6f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Description text input
                    OutlinedTextField(
                        value = sosDescription,
                        onValueChange = { sosDescription = it },
                        label = { Text(getLabel("sos_desc"), color = Color.White.copy(alpha = 0.4f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AlertRed,
                            unfocusedBorderColor = BorderGrey,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (sosDescription.isNotBlank()) {
                            viewModel.reportClimateSos(sosType, sosDescription)
                            sosDescription = ""
                            showReportSosDialog = false
                        }
                    }
                ) {
                    Text(text = getLabel("publish"), color = AlertRed, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportSosDialog = false }) {
                    Text(text = getLabel("cancel"), color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }

    // DIALOG: ADD NEW AGRICULTURAL PLOT
    if (showAddPlotDialog) {
        AlertDialog(
            onDismissRequest = { showAddPlotDialog = false },
            title = {
                Text(
                    text = getLabel("add_plot"),
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            containerColor = DeepCharcoal,
            shape = RoundedCornerShape(20.dp),
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = plotName,
                        onValueChange = { plotName = it },
                        label = { Text("Plot Name (e.g., Ararat Valley B)", color = Color.White.copy(alpha = 0.4f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = BorderGrey,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = cropType,
                        onValueChange = { cropType = it },
                        label = { Text("Crop Type (e.g., Wheat, Grape)", color = Color.White.copy(alpha = 0.4f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = BorderGrey,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = plotLocation,
                        onValueChange = { plotLocation = it },
                        label = { Text("Location (e.g., Ararat Valley, Yerevan)", color = Color.White.copy(alpha = 0.4f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = BorderGrey,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = plotSize,
                        onValueChange = { plotSize = it },
                        label = { Text("Area (Hectares)", color = Color.White.copy(alpha = 0.4f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = BorderGrey,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (plotName.isNotBlank() && plotLocation.isNotBlank()) {
                            val hectares = plotSize.toDoubleOrNull() ?: 1.0
                            viewModel.addNewPlot(plotName, cropType, plotLocation, hectares)
                            plotName = ""
                            showAddPlotDialog = false
                        }
                    }
                ) {
                    Text(text = "Register", color = NeonGreen, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPlotDialog = false }) {
                    Text(text = getLabel("cancel"), color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }
}

// =========================================================================================
// TAB 1: PLOTS MANAGEMENT SCREEN (SOIL MOISTURE, HEALTH METRICS, REGISTER SENSORS)
// =========================================================================================
@Composable
fun PlotsTabScreen(
    viewModel: EcoViewModel,
    getLabel: (String) -> String
) {
    val plots by viewModel.plotsList.collectAsStateWithLifecycle()
    var isRegisterDialogExpanded by remember { mutableStateOf(false) }

    var inputName by remember { mutableStateOf("") }
    var inputCrop by remember { mutableStateOf("") }
    var inputLoc by remember { mutableStateOf("") }
    var inputSize by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = getLabel("title_plots"),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "REGISTERED SENSORS AND ACTIVE PLOTS",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Button(
                    onClick = { isRegisterDialogExpanded = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "add",
                        tint = CyberBlack,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = getLabel("add_plot"),
                        color = CyberBlack,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        if (plots.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DeepCharcoal, RoundedCornerShape(16.dp))
                        .border(1.dp, BorderGrey, RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Agriculture,
                            contentDescription = "empty",
                            tint = NeonGreen.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = getLabel("no_plots"),
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(plots, key = { it.id }) { plot ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DeepCharcoal, RoundedCornerShape(16.dp))
                        .border(1.dp, BorderGrey, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = plot.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "COORD: ${String.format("%.4f", plot.latitude)}, ${String.format("%.4f", plot.longitude)} // ${plot.locationName}",
                                    color = TextMuted,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            // Delete button
                            IconButton(onClick = { viewModel.deletePlot(plot.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = AlertRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Divider(color = BorderGrey, thickness = 0.5.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Info Grid
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column {
                                    Text(text = "CROP SPECIES", color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                    Text(text = plot.cropType, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text(text = "FIELD AREA", color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                    Text(text = "${plot.areaHectares} Hectares", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Circular Soil Indicator Meter
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(CyberBlack, CircleShape)
                                    .border(1.dp, BorderGrey, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = { (plot.soilMoisture / 100f).toFloat() },
                                    modifier = Modifier.fillMaxSize(),
                                    color = if (plot.soilMoisture >= 40.0) SoftTeal else AlertOrange,
                                    strokeWidth = 3.dp,
                                    trackColor = Color.White.copy(alpha = 0.05f),
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${plot.soilMoisture.toInt()}%",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "SOIL",
                                        color = TextMuted,
                                        fontSize = 7.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // Progress Score bar for vegetative health rating
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CyberBlack, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            if (plot.healthScore >= 90) NeonGreen else AlertOrange,
                                            CircleShape
                                        )
                                )
                                Text(
                                    text = getLabel("health_rating").uppercase(),
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = "${plot.healthScore}% [${if (plot.healthScore >= 90) getLabel("healthy") else "STRESSED"}]",
                                color = if (plot.healthScore >= 90) NeonGreen else AlertOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }

    // NEW PLOT INLINE DIALOG EXPANSION
    if (isRegisterDialogExpanded) {
        AlertDialog(
            onDismissRequest = { isRegisterDialogExpanded = false },
            title = {
                Text(
                    text = getLabel("add_plot"),
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            containerColor = DeepCharcoal,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("Plot Title") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            focusedBorderColor = NeonGreen
                        )
                    )
                    OutlinedTextField(
                        value = inputCrop,
                        onValueChange = { inputCrop = it },
                        label = { Text("Crop Type") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            focusedBorderColor = NeonGreen
                        )
                    )
                    OutlinedTextField(
                        value = inputLoc,
                        onValueChange = { inputLoc = it },
                        label = { Text("Province Delta") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            focusedBorderColor = NeonGreen
                        )
                    )
                    OutlinedTextField(
                        value = inputSize,
                        onValueChange = { inputSize = it },
                        label = { Text("Sizing in Hectares") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            focusedBorderColor = NeonGreen
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (inputName.isNotBlank() && inputLoc.isNotBlank()) {
                            val h = inputSize.toDoubleOrNull() ?: 2.0
                            viewModel.addNewPlot(inputName, inputCrop, inputLoc, h)
                            inputName = ""
                            inputCrop = ""
                            inputLoc = ""
                            inputSize = ""
                            isRegisterDialogExpanded = false
                        }
                    }
                ) {
                    Text(text = "Save", color = NeonGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { isRegisterDialogExpanded = false }) {
                    Text(text = "Cancel", color = Color.White.copy(alpha = 0.5f))
                }
            }
        )
    }
}

// =========================================================================================
// TAB 2: AI PLANT SCAN SCREEN (COMPUTER VISION DISEASES CLASSIFICATION)
// =========================================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanTabScreen(
    viewModel: EcoViewModel,
    getLabel: (String) -> String
) {
    val scanState by viewModel.scanState.collectAsStateWithLifecycle()
    val history by viewModel.diseaseHistoryList.collectAsStateWithLifecycle()
    
    var activeCropSelection by remember { mutableStateOf("Tomato") }
    val cropOptions = listOf("Tomato", "Wheat", "Grape", "Potato")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = getLabel("scan_plant"),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Quick crop health check",
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Active Scan control Box area
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepCharcoal, RoundedCornerShape(20.dp))
                    .border(1.dp, BorderGrey, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = getLabel("choose_crop"),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Selection Segment row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        cropOptions.forEach { option ->
                            val isSelected = activeCropSelection == option
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) NeonGreen.copy(alpha = 0.2f) else CyberBlack,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) NeonGreen else BorderGrey,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { activeCropSelection = option }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = option,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Scan Leaf drawing graphical preview & loading scanner animation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(CyberBlack, RoundedCornerShape(12.dp))
                            .border(1.dp, BorderGrey, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (scanState is UiState.Loading) {

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(color = NeonGreen)
                                Text(
                                    text = "Scanning image...",
                                    color = NeonGreen,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Glowing green scanning bar overlay effect
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .background(NeonGreen)
                                    .align(Alignment.Center)
                            )
                        } else {
                            // Static drawing vector graphic representing agricultural leaf scanning
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterFrames,
                                    contentDescription = "leaf",
                                    tint = MutedTeal.copy(alpha = 0.8f),
                                    modifier = Modifier.size(44.dp)
                                )
                                Text(
                                    text = "Camera preview",
                                    color = TextMuted,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Button trigger FastAPI diagnose
                    Button(
                        onClick = { viewModel.runDiseaseDiagnosis(activeCropSelection) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = scanState !is UiState.Loading
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "scan", tint = CyberBlack)
                            Text(
                                text = getLabel("trigger_scan").uppercase(),
                                color = CyberBlack,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Active output diagnostic cards if success!
        item {
            AnimatedVisibility(
                visible = scanState is UiState.Success,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (scanState is UiState.Success) {
                    val report = (scanState as UiState.Success<PlantDiseaseEntity>).data
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DeepCharcoal, RoundedCornerShape(20.dp))
                            .border(2.dp, NeonGreen, RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${report.cropName}: ${report.suspectedDisease}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Scan confidence",
                                        color = TextMuted,
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .background(NeonGreen.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${(report.confidence * 100).toInt()}% conf",
                                        color = NeonGreen,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Divider(color = BorderGrey, thickness = 0.5.dp)

                            // Localized custom diagnosis text block!
                            Text(
                                text = getLabel("diagnosis").uppercase(),
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = viewModel.getLocalizedText(report.diagnosisEn, report.diagnosisAm, report.diagnosisRu),
                                color = Color.White,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )

                            // Bio-treatments
                            Text(
                                text = "Recommended treatment",
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CyberBlack, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = viewModel.getLocalizedText(report.treatmentsEn, report.treatmentsAm, report.treatmentsRu),
                                    color = NeonGreen,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }

                            // Dismiss back
                            TextButton(
                                onClick = { viewModel.resetScanState() },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(text = "Clear result", color = Color.White.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
        }

        // SCANS ARCHIVE LOGS FROM DATABASE (OFFLINE-FIRST)
        item {
            Text(
                text = getLabel("history_scans"),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (history.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DeepCharcoal, RoundedCornerShape(12.dp))
                        .border(1.dp, BorderGrey, RoundedCornerShape(12.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getLabel("no_scans"),
                        color = TextMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(history) { report ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DeepCharcoal, RoundedCornerShape(12.dp))
                        .border(1.dp, BorderGrey, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${report.cropName} - ${report.suspectedDisease}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "LOCAL SCAN: DB LOCAL RECORD // CONF: ${(report.confidence * 100).toInt()}%",
                                color = TextMuted,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Icon indicating offline status
                        Icon(
                            imageVector = Icons.Default.OfflineBolt,
                            contentDescription = "offline synced",
                            tint = NeonGreen.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================================
// TAB 3: SETTINGS & ARCHITECTURE SPECS & METADATA OVERVIEW
// =========================================================================================
@Composable
fun SettingsTabScreen(
    viewModel: EcoViewModel,
    getLabel: (String) -> String
) {
    val activeLanguage by viewModel.activeLanguage.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = getLabel("title_settings"),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "SYSTEM CONFIGURATOR, LOCALES & SYSTEM BLUEPRINTS",
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Language Switcher Options
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepCharcoal, RoundedCornerShape(16.dp))
                    .border(1.dp, BorderGrey, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = "language", tint = NeonGreen)
                        Text(
                            text = getLabel("lang_switch"),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val languages = listOf("en" to "ENGLISH", "am" to "ՀԱՅԵՐԵՆ", "ru" to "РУССКИЙ")
                        languages.forEach { (code, name) ->
                            val isSelected = activeLanguage == code
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) NeonGreen.copy(alpha = 0.2f) else CyberBlack,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) NeonGreen else BorderGrey,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.setLanguage(code) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = name,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Room Cache database syncer trigger
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepCharcoal, RoundedCornerShape(16.dp))
                    .border(1.dp, BorderGrey, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.CloudSync, contentDescription = "sync", tint = SoftTeal)
                        Text(
                            text = getLabel("sync_local"),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Trigger dynamic telemetry broadcast to post offline Room plots metadata into remote clusters.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Button(
                        onClick = { viewModel.triggerDatabaseSync() },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTeal),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSyncing
                    ) {
                        Text(
                            text = if (isSyncing) getLabel("sync_in_progress") else "SYNC DATABASES NOW",
                            color = CyberBlack,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // SENIOR SOLUTION ARCHITECT ADVANCED SCHEMAS AND FastAPI COMPILER BLUEPRINTS
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepCharcoal, RoundedCornerShape(20.dp))
                    .border(1.dp, MutedTeal, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.DeveloperMode, contentDescription = "architect", tint = NeonGreen)
                        Text(
                            text = getLabel("sys_architecture"),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = getLabel("architecture_explain"),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Divider(color = BorderGrey, thickness = 0.5.dp)

                    // TEXT DIAGRAM
                    Text(
                        text = "ARCHITECTURE BLUEPRINT SCHEMATIC:",
                        color = NeonGreen,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CyberBlack, RoundedCornerShape(8.dp))
                            .border(1.dp, BorderGrey, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = """
                            +-------------------------------------------+
                            |            Jetpack Compose UI             |
                            +-------------------------------------------+
                                   |                       ^
                            User actions (LOCALE)     Stateflows
                                   v                       |
                            +-------------------------------------------+
                            |           EcoViewModel (MVVM)             |
                            +-------------------------------------------+
                                   |                       ^
                                 Query                   Flows
                                   v                       |
                            +-------------------------------------------+
                            |               EcoRepository               |
                            +-------------------------------------------+
                                 /                         \
                          Offline Caching             Remote APIs
                            /                               \
                    +----------------+              +----------------+
                    | SQLite (Room)  |              | FastAPI (Cloud)|
                    +----------------+              +----------------+
                    | - Plots        |              | - AI Agronomy  |
                    | - AQI Cache    |              | - CV Diagnosis |
                    | - Warnings SOS |              | - Live Pins    |
                    +----------------+              +----------------+
                            """.trimIndent(),
                            color = NeonGreen,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 11.sp
                        )
                    }

                    // TFLITE VS BACKEND CLOUD TRADEOFF COMPARATIVE DATA
                    Text(
                        text = "EDGE TFLITE VS BACKEND CLOUD INFERENCE:",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "●", color = NeonGreen, fontSize = 10.sp)
                            Text(
                                text = "TensorFlow Lite local edge model operates fully offline. Essential for Armenian farm zones with nil network connections.",
                                color = TextMuted,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "●", color = NeonGreen, fontSize = 10.sp)
                            Text(
                                text = "PyTorch Cloud FastAPI models deliver 99.2% accuracy on complex vegetative diseases by processing high density parameters.",
                                color = TextMuted,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
