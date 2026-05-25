package com.example.ui.localization

/**
 * AppStrings — централизованное хранилище всех строк интерфейса.
 *
 * Использование:
 *   AppStrings.get("key", "en")   // возвращает строку на нужном языке
 *   AppStrings.get("key", "am")   // армянский
 *   AppStrings.get("key", "ru")   // русский
 *
 * Fallback-цепочка: am → ru → en → ключ
 */
object AppStrings {

    private val en: Map<String, String> = mapOf(
        // App identity
        "app_name"              to "EcoSys",
        "app_tag"               to "CLIMATE-AWARE FARMING PLATFORM",

        // Bottom nav tabs
        "map_tab"               to "Map",
        "plots_tab"             to "Plots",
        "scan_tab"              to "Scan",
        "settings_tab"          to "Settings",

        // Status bar / header
        "syncing"               to "SYNCING",
        "offline_ready"         to "OFFLINE READY",

        // Map screen
        "tap_hint"              to "Tap a marker to see AQI & plot details",
        "add_plot"              to "Add Plot",
        "report_sos"            to "Report SOS",
        "coord_label"           to "GPS",
        "aqi_loading"           to "Loading AQI data…",
        "aqi_status"            to "AQI",
        "aqi_idle"              to "Tap a plot to load AQI",
        "close"                 to "Close",
        "dismiss"               to "DISMISS",

        // SOS dialog
        "sos_type"              to "ALERT TYPE",
        "sos_desc"              to "Describe the situation…",
        "publish"               to "PUBLISH",
        "cancel"                to "Cancel",

        // Add plot dialog
        "plot_name_hint"        to "Plot name",
        "crop_hint"             to "Crop type",
        "location_hint"         to "Location / province",
        "area_hint"             to "Area (hectares)",
        "register"              to "REGISTER",
        "save"                  to "SAVE",

        // Plot card
        "soil_moisture"         to "Soil Moisture",
        "health_rating"         to "Health",
        "healthy"               to "HEALTHY",
        "delete"                to "Delete",
        "no_plots"              to "No plots registered yet.\nTap + to add your first plot.",

        // Scan screen
        "scan_plant"            to "AI Plant Scan",
        "choose_crop"           to "Select crop for diagnosis",
        "camera_preview"        to "Camera preview (CameraX in production)",
        "scanning"              to "AI SCANNING…",
        "trigger_scan"          to "Run AI Diagnosis",
        "diagnosis"             to "Diagnosis",
        "conf_label"            to "CONF",
        "history_scans"         to "Scan History",
        "no_scans"              to "No scans recorded yet.\nRun your first diagnosis above.",
        "clear_result"          to "Clear result",

        // Settings screen
        "lang_switch"           to "Interface Language",
        "sync_local"            to "Sync Local DB",
        "sync_now"              to "SYNC NOW",
        "sync_in_progress"      to "SYNCING…",
        "sys_architecture"      to "System Architecture",
        "architecture_explain"  to "EcoSys uses offline-first MVVM architecture. " +
                "Room is the source of truth. StateFlows drive the UI. " +
                "WorkManager handles background sync with exponential backoff.",
        "tflite_bullet"         to "MVP: Backend inference (FastAPI) — faster iteration, no APK bloat.",
        "cloud_bullet"          to "Phase 2: Hybrid — on-device TFLite pre-screen + cloud full classification.",
    )

    private val am: Map<String, String> = mapOf(
        // App identity
        "app_name"              to "ԷկոՍիս",
        "app_tag"               to "ԿԼԻՄԱՅԱԿԱՆ ԳՅՈՒՂԱՏՆՏԵՍՈՒԹՅՈՒՆ",

        // Bottom nav tabs
        "map_tab"               to "Քարտեզ",
        "plots_tab"             to "Հողամաս",
        "scan_tab"              to "Սկան",
        "settings_tab"          to "Կարգ.",

        // Status bar / header
        "syncing"               to "ՀԱՄԱԺԱՄ",
        "offline_ready"         to "ՕՖԼԱՅՆ",

        // Map screen
        "tap_hint"              to "Հպեք մարկերին՝ AQI տեղեկատվություն տեսնելու",
        "add_plot"              to "Ավելացնել",
        "report_sos"            to "SOS Ազդանշան",
        "coord_label"           to "GPS",
        "aqi_loading"           to "AQI բեռնվում է…",
        "aqi_status"            to "AQI",
        "aqi_idle"              to "Հպեք հողամասին՝ AQI տեսնելու",
        "close"                 to "Փակել",
        "dismiss"               to "ՓԱԿԵԼ",

        // SOS dialog
        "sos_type"              to "ԱԶԴԱՆՇԱՆԻ ՏԵՍԱԿ",
        "sos_desc"              to "Նկարագրեք իրավիճակը…",
        "publish"               to "ՀՐԱՊԱՐԱԿԵԼ",
        "cancel"                to "Չեղարկել",

        // Add plot dialog
        "plot_name_hint"        to "Հողամասի անուն",
        "crop_hint"             to "Մշակաբույսի տեսակ",
        "location_hint"         to "Վայր / մարզ",
        "area_hint"             to "Մակերես (հա)",
        "register"              to "ԱՎԵԼԱՑՆԵԼ",
        "save"                  to "ՊԱՀՊԱՆԵԼ",

        // Plot card
        "soil_moisture"         to "Հողի խոնավ.",
        "health_rating"         to "Առողջություն",
        "healthy"               to "ԱՌՈՂՋ",
        "delete"                to "Ջնջել",
        "no_plots"              to "Հողամասեր չկան։\nՍեղմեք + ավելացնելու համար։",

        // Scan screen
        "scan_plant"            to "ԱԻ Բույսի Սկան",
        "choose_crop"           to "Ընտրեք մշակաբույս",
        "camera_preview"        to "Տեսախցիկի նախադիտում",
        "scanning"              to "ՍԿԱՆԱՎՈՐՎՈՒՄ Է…",
        "trigger_scan"          to "Գործարկել ԱԻ",
        "diagnosis"             to "Ախտորոշում",
        "conf_label"            to "ՎՍТ",
        "history_scans"         to "Սկանների պատմություն",
        "no_scans"              to "Սկաններ չկան։\nԱջ կատարեք ախտորոշում։",
        "clear_result"          to "Մաքրել արդյունքը",

        // Settings screen
        "lang_switch"           to "Ինտերֆեյսի լեզու",
        "sync_local"            to "Համաժամ. տվ. բազա",
        "sync_now"              to "ՀԱՄԱԺԱՄ ՀԻՄԱ",
        "sync_in_progress"      to "ՀԱՄԱԺԱՄ…",
        "sys_architecture"      to "Համակարգի ճարտ.",
        "architecture_explain"  to "EcoSys-ն օֆլայն-ֆըրսթ MVVM ճարտ. է: Room-ը ճշմարտության աղբյուրն է: StateFlow-ները ղեկավարում են UI-ը: WorkManager-ը ֆոնային համաժամացումն է:",
        "tflite_bullet"         to "MVP: Backend inference (FastAPI) — ավելի արագ կրկնություն:",
        "cloud_bullet"          to "Փուլ 2: Հիբրիդ — on-device TFLite + cloud full classification:",
    )

    private val ru: Map<String, String> = mapOf(
        // App identity
        "app_name"              to "ЭкоСис",
        "app_tag"               to "КЛИМАТИЧЕСКОЕ ЗЕМЛЕДЕЛИЕ",

        // Bottom nav tabs
        "map_tab"               to "Карта",
        "plots_tab"             to "Участки",
        "scan_tab"              to "Скан",
        "settings_tab"          to "Настр.",

        // Status bar / header
        "syncing"               to "СИНХРОН",
        "offline_ready"         to "ОФЛАЙН",

        // Map screen
        "tap_hint"              to "Нажмите на маркер для просмотра AQI",
        "add_plot"              to "Добавить",
        "report_sos"            to "SOS Репорт",
        "coord_label"           to "GPS",
        "aqi_loading"           to "Загрузка AQI…",
        "aqi_status"            to "AQI",
        "aqi_idle"              to "Нажмите на участок для AQI",
        "close"                 to "Закрыть",
        "dismiss"               to "ЗАКРЫТЬ",

        // SOS dialog
        "sos_type"              to "ТИП ОПОВЕЩЕНИЯ",
        "sos_desc"              to "Опишите ситуацию…",
        "publish"               to "ОПУБЛИКОВАТЬ",
        "cancel"                to "Отмена",

        // Add plot dialog
        "plot_name_hint"        to "Название участка",
        "crop_hint"             to "Тип культуры",
        "location_hint"         to "Место / провинция",
        "area_hint"             to "Площадь (га)",
        "register"              to "ДОБАВИТЬ",
        "save"                  to "СОХРАНИТЬ",

        // Plot card
        "soil_moisture"         to "Влажность почвы",
        "health_rating"         to "Здоровье",
        "healthy"               to "ЗДОРОВЫЙ",
        "delete"                to "Удалить",
        "no_plots"              to "Участков нет.\nНажмите + для добавления.",

        // Scan screen
        "scan_plant"            to "ИИ Скан Растения",
        "choose_crop"           to "Выберите культуру",
        "camera_preview"        to "Предпросмотр камеры",
        "scanning"              to "ИИ СКАНИРУЕТ…",
        "trigger_scan"          to "Запустить диагностику",
        "diagnosis"             to "Диагноз",
        "conf_label"            to "УВЕ",
        "history_scans"         to "История сканирований",
        "no_scans"              to "Сканирований нет.\nЗапустите диагностику выше.",
        "clear_result"          to "Очистить результат",

        // Settings screen
        "lang_switch"           to "Язык интерфейса",
        "sync_local"            to "Синхр. локальная БД",
        "sync_now"              to "СИНХР. СЕЙЧАС",
        "sync_in_progress"      to "СИНХРОНИЗАЦИЯ…",
        "sys_architecture"      to "Архитектура системы",
        "architecture_explain"  to "EcoSys использует offline-first MVVM. Room — источник истины. StateFlow управляет UI. WorkManager — фоновая синхронизация с экспоненциальным откатом.",
        "tflite_bullet"         to "MVP: Backend inference (FastAPI) — быстрая итерация, нет раздувания APK.",
        "cloud_bullet"          to "Фаза 2: Гибрид — on-device TFLite пре-скрининг + cloud классификация.",
    )

    /**
     * Получить строку по ключу и языковому коду.
     * Fallback: am → ru → en → ключ
     */
    fun get(key: String, lang: String): String {
        return when (lang) {
            "am" -> am[key] ?: ru[key] ?: en[key] ?: key
            "ru" -> ru[key] ?: en[key] ?: key
            else -> en[key] ?: key
        }
    }
}