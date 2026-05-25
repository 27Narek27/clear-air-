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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.*
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*
import com.example.ui.viewmodel.EcoViewModel
import com.example.ui.viewmodel.UiState
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// ─────────────────────────────────────────────────────────────────────────────
// ГЛАВНЫЙ ЭКРАН — навигация + статус-бар + шапка
// ИСПРАВЛЕНО:
//   - Нет inline-словарей; используем AppStrings через viewModel.label()
//   - Название "EcoSys" берётся из AppStrings, не хардкодится как "ClearAir"
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: EcoViewModel) {
    val activeTab      by viewModel.activeTab.collectAsStateWithLifecycle()
    val activeLanguage by viewModel.activeLanguage.collectAsStateWithLifecycle()
    val isSyncing      by viewModel.isSyncing.collectAsStateWithLifecycle()

    // Ярлык — вызывается при каждой рекомпозиции, но AppStrings.get()
    // просто делает map-lookup (O(1)), словари уже созданы.
    val L = remember(activeLanguage) {
        { key: String -> AppStrings.get(key, activeLanguage) }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E141B)),
        bottomBar = {
            Column(modifier = Modifier.background(Color(0xFF121A24))) {
                NavigationBar(
                    containerColor  = DeepCharcoal,
                    tonalElevation  = 8.dp,
                    modifier        = Modifier.navigationBarsPadding(),
                    windowInsets    = WindowInsets.navigationBars,
                ) {
                    val items = listOf(
                        Triple(0, L("map_tab"),      Icons.Default.Map),
                        Triple(1, L("plots_tab"),    Icons.Default.Spa),
                        Triple(2, L("scan_tab"),     Icons.Default.CameraAlt),
                        Triple(3, L("settings_tab"), Icons.Default.Settings),
                    )
                    items.forEach { (tabId, label, icon) ->
                        val isSelected = activeTab == tabId
                        NavigationBarItem(
                            selected = isSelected,
                            onClick  = { viewModel.setTab(tabId) },
                            icon     = {
                                Icon(
                                    imageVector     = icon,
                                    contentDescription = label,
                                    tint            = if (isSelected) CyberBlack else Color.White.copy(alpha = 0.5f),
                                )
                            },
                            label = {
                                Text(
                                    text       = label,
                                    color      = if (isSelected) NeonGreen else Color.White.copy(alpha = 0.6f),
                                    fontSize   = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontFamily = FontFamily.Monospace,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor      = NeonGreen,
                                selectedIconColor   = CyberBlack,
                                unselectedIconColor = Color.White.copy(alpha = 0.4f),
                            ),
                        )
                    }
                }
                // Жест-полоска снизу
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(Color(0xFF121A24)),
                ) {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(4.dp)
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(2.dp))
                            .align(Alignment.Center),
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0E141B)),
        ) {
            // Симулятор статус-бара
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text(
                        text = remember {
                            val cal = java.util.Calendar.getInstance()
                            String.format("%02d:%02d", cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE))
                        },
                        color      = Color.White.copy(alpha = 0.6f),
                        fontSize   = 10.sp,
                        fontFamily = FontFamily.Monospace,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(NeonGreen, CircleShape))
                        Box(
                            modifier = Modifier
                                .width(12.dp).height(7.dp)
                                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(1.dp))
                                .padding(0.5.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight().fillMaxWidth(0.8f)
                                    .background(Color.White.copy(alpha = 0.7f)),
                            )
                        }
                    }
                }
            }

            // Шапка приложения
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderGrey)
                    .background(Color(0xFF121A24))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // ИСПРАВЛЕНО: название берётся из AppStrings, не хардкодится
                        Text(
                            text       = L("app_name"),
                            color      = NeonGreen,
                            fontSize   = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(NeonGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .border(0.5.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                        ) {
                            Text(text = "MVP", color = NeonGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                    Text(text = L("app_tag"), color = NeonGreen.copy(alpha = 0.6f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    // Индикатор синхронизации
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSyncing) AlertOrange.copy(alpha = 0.15f) else NeonGreen.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp),
                            )
                            .border(1.dp, if (isSyncing) AlertOrange.copy(alpha = 0.3f) else NeonGreen.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(6.dp).background(if (isSyncing) AlertOrange else NeonGreen, CircleShape))
                            Text(
                                text       = if (isSyncing) L("syncing") else L("offline_ready"),
                                color      = if (isSyncing) AlertOrange else NeonGreen,
                                fontSize   = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                    }
                    // Языковая пилюля
                    Box(
                        modifier = Modifier
                            .background(BorderGrey, RoundedCornerShape(8.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(text = activeLanguage.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            HorizontalDivider(color = BorderGrey, thickness = 1.dp)

            // Анимированное переключение экранов
            AnimatedContent(
                targetState  = activeTab,
                transitionSpec = {
                    fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 8 } togetherWith
                            fadeOut(tween(180)) + slideOutHorizontally(tween(180)) { -it / 10 }
                },
                label    = "tabTransition",
                modifier = Modifier.fillMaxSize().weight(1f),
            ) { tab ->
                when (tab) {
                    0    -> MapTabScreen(viewModel = viewModel, L = L)
                    1    -> PlotsTabScreen(viewModel = viewModel, L = L)
                    2    -> ScanTabScreen(viewModel = viewModel, L = L)
                    else -> SettingsTabScreen(viewModel = viewModel, L = L)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB 0 — КАРТА И SOS-РЕПОРТЫ
// ИСПРАВЛЕНО: fetchAqi вызывается с lat/lon участка
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapTabScreen(
    viewModel: EcoViewModel,
    L: (String) -> String,
) {
    val plots      by viewModel.plotsList.collectAsStateWithLifecycle()
    val sosAlerts  by viewModel.sosAlertsList.collectAsStateWithLifecycle()
    val aqiState   by viewModel.aqiState.collectAsStateWithLifecycle()

    var selectedPlotId    by remember { mutableStateOf<Long?>(null) }
    var selectedSosId     by remember { mutableStateOf<Long?>(null) }
    var showReportSos     by remember { mutableStateOf(false) }
    var showAddPlot       by remember { mutableStateOf(false) }

    var sosType       by remember { mutableStateOf("WATER_SHORTAGE") }
    var sosDesc       by remember { mutableStateOf("") }
    var plotName      by remember { mutableStateOf("") }
    var plotCrop      by remember { mutableStateOf("") }
    var plotLocation  by remember { mutableStateOf("") }
    var plotSize      by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEFF3F8))
                .pointerInput(plots, sosAlerts) {
                    detectTapGestures { offset ->
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()

                        var clickedPlot: PlotEntity? = null
                        plots.forEach { plot ->
                            val cx = w * 0.2f + (plot.latitude % 1.0f).toFloat() * w * 1.5f
                            val cy = h * 0.3f + (plot.longitude % 1.0f).toFloat() * h * 1.2f
                            if (Math.hypot((offset.x - cx).toDouble(), (offset.y - cy).toDouble()) < 40.0) {
                                clickedPlot = plot
                            }
                        }

                        var clickedSos: ClimateSosEntity? = null
                        sosAlerts.forEach { alert ->
                            val ax = w * 0.4f + (alert.latitude % 1.0f).toFloat() * w * 1.6f
                            val ay = h * 0.5f - (alert.longitude % 1.0f).toFloat() * h * 1.5f
                            if (Math.hypot((offset.x - ax).toDouble(), (offset.y - ay).toDouble()) < 40.0) {
                                clickedSos = alert
                            }
                        }

                        when {
                            clickedPlot != null -> {
                                selectedPlotId = clickedPlot!!.id
                                selectedSosId  = null
                                // ИСПРАВЛЕНО: передаём реальные координаты участка
                                viewModel.fetchAqi(
                                    lat          = clickedPlot!!.latitude,
                                    lon          = clickedPlot!!.longitude,
                                    locationName = clickedPlot!!.locationName,
                                )
                            }
                            clickedSos != null -> {
                                selectedSosId  = clickedSos!!.id
                                selectedPlotId = null
                            }
                            else -> {
                                selectedPlotId = null
                                selectedSosId  = null
                            }
                        }
                    }
                },
        ) {
            val cw = size.width
            val ch = size.height
            val step = 80.dp.toPx()

            // Сетка карты
            var x = 0f
            while (x < cw) { drawLine(Color(0xFFD5DEE8), Offset(x, 0f), Offset(x, ch), 1.dp.toPx()); x += step }
            var y = 0f
            while (y < ch) { drawLine(Color(0xFFD5DEE8), Offset(0f, y), Offset(cw, y), 1.dp.toPx()); y += step }

            // Концентрические круги
            drawCircle(Color(0xFF9FC5E8).copy(alpha = 0.35f), cw * 0.3f, Offset(cw / 2f, ch / 2f), style = Stroke(1.dp.toPx()))

            // Маркеры участков
            plots.forEach { plot ->
                val px = cw * 0.2f + (plot.latitude  % 1.0).toFloat() * cw * 1.5f
                val py = ch * 0.3f + (plot.longitude % 1.0).toFloat() * ch * 1.2f
                drawCircle(Color(0xFF6FCF97).copy(alpha = 0.30f), 32.dp.toPx(), Offset(px, py))
                drawCircle(Color(0xFF2F855A), 6.dp.toPx(), Offset(px, py))
            }

            // Маркеры SOS
            sosAlerts.forEach { alert ->
                val ax = cw * 0.4f + (alert.latitude  % 1.0).toFloat() * cw * 1.6f
                val ay = ch * 0.5f - (alert.longitude % 1.0).toFloat() * ch * 1.5f
                drawCircle(AlertRed.copy(alpha = 0.2f), 28.dp.toPx(), Offset(ax, ay))
                drawCircle(AlertRed, 5.dp.toPx(), Offset(ax, ay))
            }
        }

        // Подсказка сверху
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .background(DeepCharcoal.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                .border(1.dp, BorderGrey, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Info, "info", tint = NeonGreen, modifier = Modifier.size(16.dp))
                Text(text = L("tap_hint"), color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }

        // Оверлей выбранного участка
        selectedPlotId?.let { plotId ->
            plots.find { it.id == plotId }?.let { plot ->
                PlotInfoOverlay(plot = plot, aqiState = aqiState, viewModel = viewModel, L = L) {
                    selectedPlotId = null
                }
            }
        }

        // Оверлей выбранного SOS
        selectedSosId?.let { sosId ->
            sosAlerts.find { it.id == sosId }?.let { alert ->
                SosInfoOverlay(alert = alert, viewModel = viewModel, L = L) {
                    selectedSosId = null
                }
            }
        }

        // FAB-кнопки
        Column(
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FloatingActionButton(
                onClick        = { showAddPlot = true },
                containerColor = NeonGreen,
                contentColor   = CyberBlack,
                shape          = RoundedCornerShape(16.dp),
                modifier       = Modifier.size(52.dp),
            ) { Icon(Icons.Default.AddHome, L("add_plot")) }

            FloatingActionButton(
                onClick        = { showReportSos = true },
                containerColor = AlertRed,
                contentColor   = Color.White,
                shape          = RoundedCornerShape(16.dp),
                modifier       = Modifier.size(52.dp),
            ) { Icon(Icons.Default.Warning, L("report_sos")) }
        }
    }

    // Диалог SOS
    if (showReportSos) {
        SosReportDialog(
            L           = L,
            initialType = sosType,
            onTypeChange = { sosType = it },
            description  = sosDesc,
            onDescChange = { sosDesc = it },
            onDismiss    = { showReportSos = false },
            onConfirm    = {
                if (sosDesc.isNotBlank()) {
                    viewModel.reportClimateSos(sosType, sosDesc)
                    sosDesc = ""
                    showReportSos = false
                }
            },
        )
    }

    // Диалог добавления участка
    if (showAddPlot) {
        AddPlotDialog(
            L            = L,
            name         = plotName,    onNameChange     = { plotName     = it },
            crop         = plotCrop,    onCropChange     = { plotCrop     = it },
            location     = plotLocation, onLocationChange = { plotLocation = it },
            size         = plotSize,    onSizeChange     = { plotSize     = it },
            onDismiss    = { showAddPlot = false },
            onConfirm    = {
                if (plotName.isNotBlank() && plotLocation.isNotBlank()) {
                    viewModel.addNewPlot(plotName, plotCrop, plotLocation, plotSize.toDoubleOrNull() ?: 1.0)
                    plotName = ""; plotCrop = ""; plotLocation = ""; plotSize = ""
                    showAddPlot = false
                }
            },
        )
    }
}

// ── Оверлей участка ──────────────────────────────────────────────────────────
@Composable
private fun BoxScope.PlotInfoOverlay(
    plot: PlotEntity,
    aqiState: UiState<AqiCacheEntity>,
    viewModel: EcoViewModel,
    L: (String) -> String,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(horizontal = 16.dp, vertical = 110.dp)
            .fillMaxWidth()
            .background(DeepCharcoal.copy(alpha = 0.95f), RoundedCornerShape(20.dp))
            .border(1.dp, MutedTeal, RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = plot.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = "${L("coord_label")}: ${String.format("%.4f", plot.latitude)}, ${String.format("%.4f", plot.longitude)}", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, L("close"), tint = Color.White.copy(alpha = 0.6f))
                }
            }

            HorizontalDivider(color = BorderGrey, thickness = 0.5.dp)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricBox(label = L("soil_moisture"), value = "${plot.soilMoisture.toInt()}%", valueColor = SoftTeal, modifier = Modifier.weight(1f))
                MetricBox(label = L("health_rating"), value = "${plot.healthScore}%",  valueColor = NeonGreen, modifier = Modifier.weight(1f))
            }

            // AQI-блок
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberBlack, RoundedCornerShape(12.dp))
                    .border(1.dp, BorderGrey, RoundedCornerShape(12.dp))
                    .padding(12.dp),
            ) {
                when (val state = aqiState) {
                    is UiState.Loading -> {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = NeonGreen, strokeWidth = 2.dp)
                            Text(text = L("aqi_loading"), fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f), fontFamily = FontFamily.Monospace)
                        }
                    }
                    is UiState.Success -> {
                        val aqi = state.data
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CloudSync, "aqi", tint = NeonGreen, modifier = Modifier.size(16.dp))
                                    Text(text = "${L("aqi_status")}: ${aqi.locationName}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text  = "AQI: ${aqi.aqiValue} [${aqi.levelCode}]",
                                    color = if (aqi.aqiValue <= 50) NeonGreen else AlertOrange,
                                    fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace,
                                )
                            }
                            LinearProgressBar(progress = (aqi.aqiValue / 150f).coerceAtMost(1f), color = if (aqi.aqiValue <= 50) NeonGreen else AlertOrange)
                            Text(text = viewModel.getLocalizedText(aqi.advisoryEn, aqi.advisoryAm, aqi.advisoryRu), color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, lineHeight = 14.sp)
                        }
                    }
                    else -> Text(text = L("aqi_idle"), fontSize = 10.sp, color = TextMuted)
                }
            }
        }
    }
}

// ── Оверлей SOS ───────────────────────────────────────────────────────────────
@Composable
private fun BoxScope.SosInfoOverlay(
    alert: ClimateSosEntity,
    viewModel: EcoViewModel,
    L: (String) -> String,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(horizontal = 16.dp, vertical = 110.dp)
            .fillMaxWidth()
            .background(DeepCharcoal.copy(alpha = 0.95f), RoundedCornerShape(20.dp))
            .border(1.dp, AlertRed, RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Warning, "warning", tint = AlertRed, modifier = Modifier.size(18.dp))
                    Text(text = alert.alertType.replace("_", " "), color = AlertRed, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, L("close"), tint = Color.White.copy(alpha = 0.6f))
                }
            }
            HorizontalDivider(color = BorderGrey, thickness = 0.5.dp)
            Text(text = alert.description, color = Color.White, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "BY: ${alert.reportedBy}", color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                Button(
                    onClick = { viewModel.resolveSosAlert(alert.id); onClose() },
                    colors  = ButtonDefaults.buttonColors(containerColor = AlertRed.copy(alpha = 0.2f)),
                    border  = BorderStroke(1.dp, AlertRed),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp),
                ) {
                    Text(text = L("dismiss"), color = AlertRed, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

// ── Диалог SOS ────────────────────────────────────────────────────────────────
@Composable
private fun SosReportDialog(
    L: (String) -> String,
    initialType: String,
    onTypeChange: (String) -> Unit,
    description: String,
    onDescChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = L("report_sos"), color = AlertRed, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        containerColor = DeepCharcoal,
        shape          = RoundedCornerShape(20.dp),
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = L("sos_type"), color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("WATER_SHORTAGE", "PEST_OUTBREAK", "FROST_ALERT", "FIRE_ALERT").forEach { type ->
                        val active = initialType == type
                        Box(
                            modifier = Modifier
                                .background(if (active) AlertRed.copy(alpha = 0.2f) else CyberBlack, RoundedCornerShape(8.dp))
                                .border(1.dp, if (active) AlertRed else BorderGrey, RoundedCornerShape(8.dp))
                                .clickable { onTypeChange(type) }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                        ) {
                            Text(text = type.replace("_", " "), color = if (active) Color.White else Color.White.copy(alpha = 0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
                OutlinedTextField(
                    value       = description,
                    onValueChange = onDescChange,
                    label       = { Text(L("sos_desc"), color = Color.White.copy(alpha = 0.4f)) },
                    colors      = OutlinedTextFieldDefaults.colors(focusedBorderColor = AlertRed, unfocusedBorderColor = BorderGrey, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier    = Modifier.fillMaxWidth(),
                    maxLines    = 3,
                )
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text(L("publish"), color = AlertRed, fontWeight = FontWeight.Black) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(L("cancel"), color = Color.White.copy(alpha = 0.6f)) } },
    )
}

// ── Диалог добавления участка ─────────────────────────────────────────────────
@Composable
private fun AddPlotDialog(
    L: (String) -> String,
    name: String, onNameChange: (String) -> Unit,
    crop: String, onCropChange: (String) -> Unit,
    location: String, onLocationChange: (String) -> Unit,
    size: String, onSizeChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(L("add_plot"), color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        containerColor = DeepCharcoal,
        shape          = RoundedCornerShape(20.dp),
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val fieldColors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonGreen, unfocusedBorderColor = BorderGrey, focusedTextColor = Color.White, unfocusedTextColor = Color.White, unfocusedLabelColor = Color.White.copy(alpha = 0.4f), focusedLabelColor = NeonGreen)
                OutlinedTextField(value = name,     onValueChange = onNameChange,     label = { Text(L("plot_name_hint")) }, colors = fieldColors, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = crop,     onValueChange = onCropChange,     label = { Text(L("crop_hint")) },     colors = fieldColors, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = location, onValueChange = onLocationChange, label = { Text(L("location_hint")) }, colors = fieldColors, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = size,     onValueChange = onSizeChange,     label = { Text(L("area_hint")) },     colors = fieldColors, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text(L("register"), color = NeonGreen, fontWeight = FontWeight.Black) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(L("cancel"),   color = Color.White.copy(alpha = 0.6f)) } },
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB 1 — УЧАСТКИ
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PlotsTabScreen(viewModel: EcoViewModel, L: (String) -> String) {
    val plots by viewModel.plotsList.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf("") }
    var inputCrop by remember { mutableStateOf("") }
    var inputLoc  by remember { mutableStateOf("") }
    var inputSize by remember { mutableStateOf("") }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = L("plots_tab"), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = "REGISTERED SENSORS AND ACTIVE PLOTS", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
                Button(onClick = { showDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = NeonGreen), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                    Icon(Icons.Default.Add, "add", tint = CyberBlack, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(text = L("add_plot"), color = CyberBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }

        if (plots.isEmpty()) {
            item {
                EmptyState(icon = Icons.Default.Agriculture, text = L("no_plots"))
            }
        } else {
            items(plots, key = { it.id }) { plot ->
                PlotCard(plot = plot, L = L, onDelete = { viewModel.deletePlot(plot.id) })
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(L("add_plot"), color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            containerColor = DeepCharcoal,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val fc = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, focusedBorderColor = NeonGreen, unfocusedBorderColor = BorderGrey, unfocusedTextColor = Color.White.copy(alpha = 0.7f))
                    OutlinedTextField(inputName, { inputName = it }, label = { Text(L("plot_name_hint"), color = Color.White.copy(alpha = 0.4f)) }, colors = fc)
                    OutlinedTextField(inputCrop, { inputCrop = it }, label = { Text(L("crop_hint"),      color = Color.White.copy(alpha = 0.4f)) }, colors = fc)
                    OutlinedTextField(inputLoc,  { inputLoc  = it }, label = { Text(L("location_hint"), color = Color.White.copy(alpha = 0.4f)) }, colors = fc)
                    OutlinedTextField(inputSize, { inputSize = it }, label = { Text(L("area_hint"),     color = Color.White.copy(alpha = 0.4f)) }, colors = fc, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (inputName.isNotBlank() && inputLoc.isNotBlank()) {
                        viewModel.addNewPlot(inputName, inputCrop, inputLoc, inputSize.toDoubleOrNull() ?: 2.0)
                        inputName = ""; inputCrop = ""; inputLoc = ""; inputSize = ""
                        showDialog = false
                    }
                }) { Text(L("save"), color = NeonGreen) }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text(L("cancel"), color = Color.White.copy(alpha = 0.5f)) } },
        )
    }
}

@Composable
private fun PlotCard(plot: PlotEntity, L: (String) -> String, onDelete: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(DeepCharcoal, RoundedCornerShape(16.dp))
            .border(1.dp, BorderGrey, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(plot.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("${String.format("%.4f", plot.latitude)}, ${String.format("%.4f", plot.longitude)} // ${plot.locationName}", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, L("delete"), tint = AlertRed, modifier = Modifier.size(18.dp))
                }
            }
            HorizontalDivider(color = BorderGrey, thickness = 0.5.dp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text("CROP", color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        Text(plot.cropType, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("AREA", color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        Text("${plot.areaHectares} ha", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Box(modifier = Modifier.size(54.dp).background(CyberBlack, CircleShape).border(1.dp, BorderGrey, CircleShape), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(progress = { (plot.soilMoisture / 100f).toFloat() }, modifier = Modifier.fillMaxSize(), color = if (plot.soilMoisture >= 40.0) SoftTeal else AlertOrange, strokeWidth = 3.dp, trackColor = Color.White.copy(alpha = 0.05f))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${plot.soilMoisture.toInt()}%", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        Text("SOIL", color = TextMuted, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth().background(CyberBlack, RoundedCornerShape(8.dp)).padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(if (plot.healthScore >= 90) NeonGreen else AlertOrange, CircleShape))
                    Text(L("health_rating").uppercase(), color = Color.White.copy(alpha = 0.8f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
                Text(
                    "${plot.healthScore}% [${if (plot.healthScore >= 90) L("healthy") else "STRESSED"}]",
                    color = if (plot.healthScore >= 90) NeonGreen else AlertOrange,
                    fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB 2 — AI СКАН
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ScanTabScreen(viewModel: EcoViewModel, L: (String) -> String) {
    val scanState by viewModel.scanState.collectAsStateWithLifecycle()
    val history   by viewModel.diseaseHistoryList.collectAsStateWithLifecycle()
    var selectedCrop by remember { mutableStateOf("Tomato") }
    val cropOptions = listOf("Tomato", "Wheat", "Grape", "Potato")

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Column {
                Text(L("scan_plant"), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("COMPUTER VISION DISEASE DETECTION", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
        }

        item {
            Box(modifier = Modifier.fillMaxWidth().background(DeepCharcoal, RoundedCornerShape(20.dp)).border(1.dp, BorderGrey, RoundedCornerShape(20.dp)).padding(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(L("choose_crop"), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        cropOptions.forEach { option ->
                            val sel = selectedCrop == option
                            Box(
                                modifier = Modifier.weight(1f)
                                    .background(if (sel) NeonGreen.copy(alpha = 0.2f) else CyberBlack, RoundedCornerShape(8.dp))
                                    .border(1.dp, if (sel) NeonGreen else BorderGrey, RoundedCornerShape(8.dp))
                                    .clickable { selectedCrop = option }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(option, color = if (sel) Color.White else Color.White.copy(alpha = 0.6f), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    // Превью камеры
                    Box(modifier = Modifier.fillMaxWidth().height(140.dp).background(CyberBlack, RoundedCornerShape(12.dp)).border(1.dp, BorderGrey, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        if (scanState is UiState.Loading) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                CircularProgressIndicator(color = NeonGreen)
                                Text(L("scanning"), color = NeonGreen, fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.FilterFrames, "camera", tint = MutedTeal.copy(alpha = 0.8f), modifier = Modifier.size(44.dp))
                                Text(L("camera_preview"), color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                    Button(
                        onClick  = { viewModel.runDiseaseDiagnosis(selectedCrop) },
                        colors   = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape    = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled  = scanState !is UiState.Loading,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.QrCodeScanner, "scan", tint = CyberBlack)
                            Text(L("trigger_scan").uppercase(), color = CyberBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Результат диагностики
        item {
            AnimatedVisibility(visible = scanState is UiState.Success, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                (scanState as? UiState.Success<PlantDiseaseEntity>)?.data?.let { report ->
                    DiagnosisResultCard(report = report, viewModel = viewModel, L = L)
                }
            }
        }

        item { Text(L("history_scans"), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }

        if (history.isEmpty()) {
            item { EmptyState(icon = Icons.Default.Biotech, text = L("no_scans")) }
        } else {
            items(history) { report ->
                Box(modifier = Modifier.fillMaxWidth().background(DeepCharcoal, RoundedCornerShape(12.dp)).border(1.dp, BorderGrey, RoundedCornerShape(12.dp)).padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("${report.cropName} — ${report.suspectedDisease}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("${L("conf_label")}: ${(report.confidence * 100).toInt()}%", color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        }
                        Icon(Icons.Default.OfflineBolt, "offline", tint = NeonGreen.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosisResultCard(report: PlantDiseaseEntity, viewModel: EcoViewModel, L: (String) -> String) {
    Box(modifier = Modifier.fillMaxWidth().background(DeepCharcoal, RoundedCornerShape(20.dp)).border(2.dp, NeonGreen, RoundedCornerShape(20.dp)).padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("${report.cropName}: ${report.suspectedDisease}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Confidence", color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                }
                Box(modifier = Modifier.background(NeonGreen.copy(alpha = 0.15f), RoundedCornerShape(10.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("${(report.confidence * 100).toInt()}% ${L("conf_label")}", color = NeonGreen, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            }
            HorizontalDivider(color = BorderGrey, thickness = 0.5.dp)
            Text(L("diagnosis").uppercase(), color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Text(viewModel.getLocalizedText(report.diagnosisEn, report.diagnosisAm, report.diagnosisRu), color = Color.White, fontSize = 12.sp, lineHeight = 16.sp)
            Text("TREATMENT", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.fillMaxWidth().background(CyberBlack, RoundedCornerShape(8.dp)).padding(10.dp)) {
                Text(viewModel.getLocalizedText(report.treatmentsEn, report.treatmentsAm, report.treatmentsRu), color = NeonGreen, fontSize = 11.sp, lineHeight = 15.sp)
            }
            TextButton(onClick = { viewModel.resetScanState() }, modifier = Modifier.align(Alignment.End)) {
                Text(L("clear_result"), color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB 3 — НАСТРОЙКИ
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SettingsTabScreen(viewModel: EcoViewModel, L: (String) -> String) {
    val activeLanguage by viewModel.activeLanguage.collectAsStateWithLifecycle()
    val isSyncing      by viewModel.isSyncing.collectAsStateWithLifecycle()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Column {
                Text(L("settings_tab"), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("SYSTEM CONFIG, LOCALE & ARCHITECTURE", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
        }

        // Языковой переключатель
        item {
            SettingsCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Language, "language", tint = NeonGreen)
                    Text(L("lang_switch"), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("en" to "ENGLISH", "am" to "ՀԱՅԵՐԵՆ", "ru" to "РУССКИЙ").forEach { (code, name) ->
                        val sel = activeLanguage == code
                        Box(
                            modifier = Modifier.weight(1f)
                                .background(if (sel) NeonGreen.copy(alpha = 0.2f) else CyberBlack, RoundedCornerShape(8.dp))
                                .border(1.dp, if (sel) NeonGreen else BorderGrey, RoundedCornerShape(8.dp))
                                .clickable { viewModel.setLanguage(code) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(name, color = if (sel) Color.White else Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Синхронизация
        item {
            SettingsCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CloudSync, "sync", tint = SoftTeal)
                    Text(L("sync_local"), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Text("Sends pending offline Room data to remote FastAPI clusters.", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, lineHeight = 15.sp)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick  = { viewModel.triggerDatabaseSync() },
                    colors   = ButtonDefaults.buttonColors(containerColor = SoftTeal),
                    shape    = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled  = !isSyncing,
                ) {
                    Text(if (isSyncing) L("sync_in_progress") else L("sync_now"), color = CyberBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Архитектурная схема
        item {
            SettingsCard(borderColor = MutedTeal) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.DeveloperMode, "arch", tint = NeonGreen)
                    Text(L("sys_architecture"), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Text(L("architecture_explain"), color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, lineHeight = 15.sp)
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = BorderGrey, thickness = 0.5.dp)
                Spacer(Modifier.height(8.dp))
                Text("ARCHITECTURE BLUEPRINT:", color = NeonGreen, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Box(modifier = Modifier.fillMaxWidth().background(CyberBlack, RoundedCornerShape(8.dp)).border(1.dp, BorderGrey, RoundedCornerShape(8.dp)).padding(10.dp)) {
                    Text(
                        text = """
+-------------------------------------------+
|          Jetpack Compose UI               |
+-------------------------------------------+
       |                       ^
User actions (LOCALE)      StateFlows
       v                       |
+-------------------------------------------+
|           EcoViewModel (MVVM)             |
+-------------------------------------------+
       |                       ^
    Calls                   Flows
       v                       |
+-------------------------------------------+
|              EcoRepository                |
+-------------------------------------------+
        /                         \
 Offline Cache               Remote APIs
      /                               \
+----------------+          +----------------+
| Room (SQLite)  |          | FastAPI Cloud  |
+----------------+          +----------------+
| - plots        |          | - AI agronomy  |
| - aqi_cache    |          | - CV diagnosis |
| - sos_alerts   |          | - SOS reports  |
+----------------+          +----------------+""".trimIndent(),
                        color = NeonGreen, fontSize = 8.sp, fontFamily = FontFamily.Monospace, lineHeight = 11.sp,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text("TFLITE VS CLOUD INFERENCE:", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Spacer(Modifier.height(6.dp))
                BulletText(L("tflite_bullet"))
                Spacer(Modifier.height(4.dp))
                BulletText(L("cloud_bullet"))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Переиспользуемые компоненты
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MetricBox(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(CyberBlack, RoundedCornerShape(12.dp))
            .border(1.dp, BorderGrey, RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = label.uppercase(), color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
        Spacer(Modifier.height(4.dp))
        Text(text = value, color = valueColor, fontSize = 18.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun LinearProgressBar(progress: Float, color: Color) {
    Box(modifier = Modifier.fillMaxWidth().height(5.dp).background(BorderGrey, RoundedCornerShape(4.dp))) {
        Box(modifier = Modifier.fillMaxWidth(progress).fillMaxHeight().background(color, RoundedCornerShape(4.dp)))
    }
}

@Composable
private fun EmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DeepCharcoal, RoundedCornerShape(16.dp))
            .border(1.dp, BorderGrey, RoundedCornerShape(16.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, "empty", tint = NeonGreen.copy(alpha = 0.4f), modifier = Modifier.size(48.dp))
            Text(text, color = Color.White.copy(alpha = 0.7f), textAlign = TextAlign.Center, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SettingsCard(borderColor: Color = BorderGrey, content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DeepCharcoal, RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Column(content = content)
    }
}

@Composable
private fun BulletText(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("●", color = NeonGreen, fontSize = 10.sp)
        Text(text, color = TextMuted, fontSize = 10.sp, lineHeight = 14.sp)
    }
}