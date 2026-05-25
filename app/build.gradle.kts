plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.example"
  // ИСПРАВЛЕНО: compileSdk — правильный синтаксис для AGP 9.x
  compileSdk = 36

  defaultConfig {
    applicationId = "com.example"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro",
      )
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      // Использует дефолтный debug signing config
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  buildFeatures {
    compose = true
    buildConfig = true
  }

  // ИСПРАВЛЕНО: Room exportSchema = false в EcoDatabase.kt,
  // но если вернёте true — раскомментируйте ksp-блок ниже:
  // ksp {
  //     arg("room.schemaLocation", "$projectDir/schemas")
  //     arg("room.incremental", "true")
  //     arg("room.expandProjection", "true")
  // }

  testOptions {
    unitTests { isIncludeAndroidResources = true }
  }
}

// Configure Secrets plugin for .env / .env.example
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))

  // Core
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.activity.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)

  // Lifecycle / ViewModel
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Room
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.room.ktx)
  "ksp"(libs.androidx.room.compiler)

  // Coroutines
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.android)

  // Networking
  implementation(libs.retrofit)
  implementation(libs.converter.moshi)
  implementation(libs.okhttp)
  implementation(libs.logging.interceptor)

  // Moshi
  implementation(libs.moshi.kotlin)
  "ksp"(libs.moshi.kotlin.codegen)

  // Opционально (раскомментируйте по мере надобности):
  // implementation(libs.accompanist.permissions)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  // implementation(libs.androidx.datastore.preferences)
  // implementation(libs.androidx.navigation.compose)
  // implementation(libs.coil.compose)
  // implementation(libs.firebase.ai)
  // implementation(libs.play.services.location)

  // Tests
  testImplementation(libs.junit)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.androidx.core)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  testImplementation(libs.androidx.compose.ui.test.junit4)

  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.runner)
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)

  debugImplementation(libs.androidx.compose.ui.tooling)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
}