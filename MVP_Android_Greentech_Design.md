# Executive Summary (MVP scope in 10–12 weeks)

Build an Android MVP for **climate-aware farming and eco-response** with four core outcomes:
1. Farmers/users can register **plots** and receive localized guidance.
2. Users can run **plant disease diagnosis** from camera photos.
3. Users can view **AQI insights** and health/activity recommendations.
4. Community can post **SOS map reports** (disease outbreak, irrigation issue, pollution hotspot).

**Team fit (2 Android + 1 backend + 1 designer):**
- Weeks 1–2: architecture setup, auth, core UI shell, Room schema.
- Weeks 3–6: plot management, AQI ingestion/cache, SOS reports/map.
- Weeks 7–9: CV diagnosis + agronomy advice API integration, offline sync hardening.
- Weeks 10–12: localization (EN/AM/RU), QA, telemetry, release prep.

**MVP success criteria:**
- App usable with intermittent network (offline-first local reads).
- End-to-end diagnosis + advice in user locale.
- AQI and SOS map visible within <2s on cached/open flows.
- Crash-free sessions >99% in beta.

---

# Architecture (MVVM + Clean Architecture + modularization)

## Recommended architecture
- **Presentation:** MVVM (`ViewModel`, `StateFlow`, unidirectional UI state).
- **Domain:** use-cases/interactors encapsulating business rules.
- **Data:** repositories coordinating local Room + remote APIs.
- **Modularization:** feature-first for scalability, shared core modules for reuse.

## Why this is practical for MVP
- Clean boundaries help 2 Android devs work in parallel safely.
- ViewModel + Flow + WorkManager is proven on Android and easy to test.
- Keeps future iOS/web backend contracts stable while Android evolves.

---

# Module Structure (feature/core/data/domain/designsystem)

```text
:app
:core:common          // Result wrappers, dispatchers, error mapping, utils
:core:network         // Retrofit/Ktor client, interceptors, DTO serializers
:core:database        // Room DB, DAOs, migrations
:core:datastore       // Proto/DataStore for settings, locale, tokens metadata
:core:designsystem    // Material3 theme, colors, typography, reusable components
:core:maps            // Map abstractions, clustering helpers, geo utils

:domain:plot          // Plot entities + use cases
:domain:aqi
:domain:diagnosis
:domain:sos

:data:plot            // Repository impl + mappers + local/remote data sources
:data:aqi
:data:diagnosis
:data:sos

:feature:auth
:feature:home
:feature:plots
:feature:aqi
:feature:diagnosis
:feature:sosmap
:feature:settings
```

**Recommendation:** keep `domain` modules pure Kotlin (no Android deps), making logic testable and reusable.

---

# Data Layer Design

## Room/SQLite schema

### 1) User plots
```kotlin
@Entity(
    tableName = "user_plots",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["updated_at"]),
        Index(value = ["lat", "lon"]) // map proximity queries
    ]
)
data class UserPlotEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    val name: String,
    val cropType: String,
    val areaSqm: Double,
    val lat: Double,
    val lon: Double,
    val soilType: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "sync_state") val syncState: String // PENDING, SYNCED, FAILED
)
```

### 2) AQI cache
```kotlin
@Entity(
    tableName = "aqi_cache",
    primaryKeys = ["geohash", "observed_at"],
    indices = [
        Index(value = ["expires_at"]),
        Index(value = ["aqi"])
    ]
)
data class AqiCacheEntity(
    val geohash: String,
    @ColumnInfo(name = "observed_at") val observedAt: Long,
    val aqi: Int,
    val pm25: Double?,
    val pm10: Double?,
    val no2: Double?,
    val o3: Double?,
    val source: String,
    @ColumnInfo(name = "expires_at") val expiresAt: Long
)
```

### 3) Plant disease history
```kotlin
@Entity(
    tableName = "plant_disease_history",
    indices = [
        Index(value = ["plot_id"]),
        Index(value = ["diagnosed_at"]),
        Index(value = ["status"])
    ]
)
data class PlantDiseaseHistoryEntity(
    @PrimaryKey val diagnosisId: String,
    @ColumnInfo(name = "plot_id") val plotId: String,
    @ColumnInfo(name = "image_uri") val imageUri: String,
    val diseaseCode: String,
    val confidence: Double,
    val status: String, // SUSPECTED, CONFIRMED, RESOLVED
    @ColumnInfo(name = "advice_text") val adviceText: String,
    val locale: String,
    @ColumnInfo(name = "diagnosed_at") val diagnosedAt: Long,
    @ColumnInfo(name = "synced_at") val syncedAt: Long?
)
```

## Indexing strategy
- Index by **user/filter usage** (user_id, plot_id, diagnosed_at).
- Add geo query index (`lat`, `lon`) for nearby map cards.
- TTL cleanup on `expires_at` for AQI.

## Migration strategy
- Start with explicit Room migrations (`Migration(1,2)`) rather than destructive migration.
- Keep additive schema changes whenever possible.
- Add a migration test per version jump with `MigrationTestHelper`.

---

# Repository & Offline-First Strategy

## Source of truth
- **Room is source-of-truth for UI reads.**
- Network responses are mapped and persisted first; UI observes local `Flow`.

## Sync queue design
`sync_queue` table stores pending write operations:
- `id`, `entityType`, `entityId`, `opType`, `payloadJson`, `attemptCount`, `nextAttemptAt`, `lastError`.
- WorkManager background worker drains queue when network available.

## Conflict resolution
- **Default:** server-wins for derived data (AQI/advice).
- **User-authored data (plot edits/SOS):** last-write-wins using `updatedAt` + device clock skew guard.
- Keep soft conflict log for support diagnostics.

## Retry policy
- Exponential backoff with jitter: 15s, 60s, 5m, 30m, 2h; max 6 attempts.
- 4xx validation/auth errors: no blind retry; mark failed and surface action.
- 5xx/network timeouts: retry.

## Caching TTL
- AQI tile TTL: **15 min urban**, **30 min rural**.
- Agronomy advice TTL: 24h unless user changes plot/crop conditions.
- Disease diagnosis result TTL: persistent history; refresh recommendation only.

## Sync flow example
1. User submits SOS offline.
2. App writes SOS row + `sync_queue` item (`PENDING_UPLOAD`).
3. UI immediately shows report badge “Pending sync”.
4. WorkManager detects network, POSTs to backend.
5. On success: local row marked `SYNCED`, queue item removed.
6. On fail: increment attempts, schedule next backoff.

---

# Backend (FastAPI) API Design

Base path: `/api/v1`  
Auth: `Authorization: Bearer <JWT>`

## 1) AI agronomy advice
**POST** `/agronomy/advice`

Request DTO:
```json
{
  "plot_id": "plot_123",
  "locale": "am-AM",
  "crop_type": "tomato",
  "growth_stage": "flowering",
  "soil_moisture": 0.31,
  "recent_weather": {"temp_c": 28, "humidity": 0.62},
  "aqi_context": {"aqi": 87, "pm25": 18.2}
}
```
Response DTO:
```json
{
  "advice_id": "adv_789",
  "locale": "am-AM",
  "summary": "...",
  "actions": [
    {"code": "IRRIGATE_EARLY", "priority": "HIGH", "text": "..."}
  ],
  "valid_until": "2026-05-25T10:00:00Z"
}
```

## 2) Computer vision diagnosis
**POST** `/diagnosis/plant`
- multipart image upload + metadata.

Response DTO:
```json
{
  "diagnosis_id": "diag_456",
  "locale": "ru-RU",
  "top_predictions": [
    {"disease_code": "late_blight", "confidence": 0.91}
  ],
  "severity": "MEDIUM",
  "next_steps": ["..."],
  "human_review_recommended": false
}
```

## 3) AQI insights
**GET** `/aqi/insights?lat=40.18&lon=44.51&locale=en-US`

Response DTO:
```json
{
  "location": {"lat": 40.18, "lon": 44.51},
  "observed_at": "2026-05-24T08:30:00Z",
  "aqi": 102,
  "category": "Unhealthy for Sensitive Groups",
  "recommendations": ["Reduce strenuous outdoor activity"]
}
```

## 4) Crowdsourced SOS map reports
- **POST** `/sos/reports`
- **GET** `/sos/reports?bbox=...&page=1&page_size=20`

Report create request:
```json
{
  "plot_id": "plot_123",
  "type": "DISEASE_OUTBREAK",
  "lat": 40.177,
  "lon": 44.512,
  "description": "Leaf damage spreading",
  "locale": "en-US"
}
```

List response (paginated):
```json
{
  "items": [{"report_id":"sos_1","type":"DISEASE_OUTBREAK","status":"OPEN"}],
  "page": 1,
  "page_size": 20,
  "total": 245,
  "next_cursor": "eyJwYWdlIjoyfQ=="
}
```

## Error model
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "crop_type is required",
    "details": {"field": "crop_type"},
    "request_id": "req_abc123"
  }
}
```

Use stable machine-readable `code` values; localize only end-user texts.

---

# AI Inference Tradeoff

| Criterion | On-device (TFLite) | Backend Inference |
|---|---|---|
| Latency | Very low once model loaded | Network dependent |
| Privacy | Strong (image stays device-local) | Image sent to server |
| Model size | APK bloat / dynamic download needed | No APK impact |
| Battery/thermal | Higher device usage | Lower device usage |
| Offline | Works offline | No network = unavailable |
| Cost | Device cost, less server | Ongoing inference infra cost |
| Update speed | Slow (app/model update flow) | Fast server-side rollout |

## Recommendation
- **MVP:** backend inference for diagnosis + advice (faster iteration, lower Android complexity).
- **Phase 2:** hybrid strategy:
  - on-device lightweight pre-screening (healthy vs suspect),
  - backend for full classification and recommendations.

---

# UI/UX Plan (production-aware but concise)

## Navigation
Bottom nav (5 tabs): **Home, Plots, Diagnose, SOS Map, Settings**.

## Core screens
- Home dashboard: AQI card, today actions, recent diagnosis.
- Plot list/detail: crop, soil, timeline.
- Diagnose flow: CameraX capture -> preview -> result card.
- SOS map: clustered markers + filter chips + report CTA.
- Settings: language, units, notifications, privacy.

## Visual style system
- Background: deep charcoal.
- Primary accent: neon-green CTAs/highlights.
- Overlays: muted teal for info layers.
- Cards: clean elevated M3 cards with strong spacing.

## Accessibility
- Maintain WCAG contrast for green-on-charcoal combinations.
- Don’t rely on color-only AQI states (add icon+label).
- Touch targets >= 48dp, dynamic type respected.

---

# Localization Architecture (EN/AM/RU)

## Resource organization
- `res/values/strings.xml` (default)
- `res/values-en/strings.xml`
- `res/values-am/strings.xml`
- `res/values-ru/strings.xml`
- also `plurals.xml` per locale.

## Dynamic language switching
- Persist selected locale in DataStore.
- Apply with AppCompat locale APIs at runtime (`setApplicationLocales`).
- Recreate affected activities safely via single-activity architecture.

## Plurals/formatting
- Use ICU message/plural resources.
- Locale-aware number/date formatting via `NumberFormat`/`DateTimeFormatter`.
- Keep units translatable but normalized in domain (e.g., metric canonical).

## Backend multilingual contract
Every response that includes user text should accept `locale` and return:
- `locale_used`
- localized human text fields
- stable `code` fields for logic.

## Fallback matrix
| Requested | 1st fallback | 2nd fallback | Final |
|---|---|---|---|
| am | ru | en | default |
| ru | en | default | - |
| en | default | - | - |

Practical note: Android resource fallback is system-driven (typically to default). The custom chain above should be implemented on backend text selection and optional in-app remote text handling.

## Terminology consistency
- Maintain shared glossary (`term_key -> localized value`) used by backend prompt templates and app labels.
- API fields should be translation-safe: `disease_code`, `severity_code`, `action_code` + localized `display_text`.

---

# Security & Compliance Baseline

- JWT/OAuth2 (short-lived access token + refresh token).
- Store tokens in EncryptedSharedPreferences or encrypted DataStore wrapper.
- TLS in transit, certificate pinning (at least for production flavor).
- Encrypt sensitive local files (diagnosis images) or store in app-private scoped storage.
- Basic abuse protection: rate limiting, IP/device fingerprint heuristics, CAPTCHA for suspicious SOS bursts.
- Audit logs with request IDs; minimal PII collection.

---

# Delivery Plan

## 30/60/90 day roadmap

### Day 0–30
**Must-have**
- Project scaffolding + modular setup.
- Auth/session handling.
- Plot CRUD + local DB.
- AQI read API integration + cache.

### Day 31–60
**Must-have**
- SOS map report create/list + offline queue.
- Diagnose capture/upload/result history.
- Basic localization framework (EN/AM/RU resources).

### Day 61–90
**Must-have**
- Agronomy advice end-to-end.
- Sync robustness, retry/conflict rules.
- QA, performance pass, analytics, beta release.

**Nice-to-have**
- push notifications, advanced filters, on-device precheck model.

## Risks & mitigations
- **Model quality variance:** add confidence threshold + fallback “human review suggested”.
- **Network instability:** offline-first SOT + resilient queue.
- **Localization quality:** glossary + screenshot-based L10n QA.
- **Team capacity:** strict scope control; postpone non-critical gamification.

---

# Tech Stack Recommendations

- Language: **Kotlin**
- Async/reactive: **Coroutines + Flow**
- DI: **Hilt** (preferred for ecosystem maturity)
- Networking: **Retrofit + OkHttp + Kotlinx Serialization**
- Local DB: **Room**
- Background sync: **WorkManager**
- Maps: **Google Maps SDK** (or Mapbox if regional constraints)
- Camera: **CameraX**
- Image loading: **Coil**
- Logging/observability: **Timber + Firebase Crashlytics/Analytics (or OSS alternative)**

**Platform target:**
- `targetSdkVersion 34+`
- Material 3, edge-to-edge, runtime permissions best practices, foreground service minimization, strict background work policies.

