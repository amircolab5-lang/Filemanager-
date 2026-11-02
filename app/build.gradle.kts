import java.util.Base64

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.example"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.ticno.filemanager"
    minSdk = 24
    targetSdk = 36
    versionCode = 248
    versionName = "2.4.8"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  val rootDebugKeystore = file("${rootDir}/debug.keystore")
  val rootDebugKeystoreBase64 = file("${rootDir}/debug.keystore.base64")
  if (!rootDebugKeystore.exists()) {
    try {
      val base64Str = if (rootDebugKeystoreBase64.exists()) {
        rootDebugKeystoreBase64.readText().trim()
      } else {
        "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"
      }
      val decoded = Base64.getDecoder().decode(base64Str)
      rootDebugKeystore.writeBytes(decoded)
    } catch (_: Exception) {}
  }

  val rootReleaseKeystore = file("${rootDir}/release.keystore")
  val rootReleaseKeystoreBase64 = file("${rootDir}/release.keystore.base64")
  if (!rootReleaseKeystore.exists()) {
    try {
      val base64Str = if (rootReleaseKeystoreBase64.exists()) {
        rootReleaseKeystoreBase64.readText().trim()
      } else {
        "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"
      }
      val decoded = Base64.getDecoder().decode(base64Str)
      rootReleaseKeystore.writeBytes(decoded)
    } catch (_: Exception) {}
  }

  signingConfigs {
    create("release") {
      enableV1Signing = true
      enableV2Signing = true
      enableV3Signing = true
      val keystorePath = System.getenv("KEYSTORE_PATH")
      val releaseKeystore = file("${rootDir}/release.keystore")
      if (keystorePath != null && file(keystorePath).exists()) {
        storeFile = file(keystorePath)
        storePassword = System.getenv("STORE_PASSWORD")
        keyAlias = System.getenv("KEY_ALIAS") ?: "ticnodevelopers"
        keyPassword = System.getenv("KEY_PASSWORD")
      } else {
        storeFile = releaseKeystore
        storePassword = "ticno12345"
        keyAlias = "ticnodevelopers"
        keyPassword = "ticno12345"
      }
    }
    create("debugConfig") {
      enableV1Signing = true
      enableV2Signing = true
      enableV3Signing = true
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = true
      isMinifyEnabled = false
      isShrinkResources = false
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      isDebuggable = true
      isMinifyEnabled = false
      isShrinkResources = false
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }

  lint {
    abortOnError = false
    checkReleaseBuilds = false
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
  packaging {
    resources {
      excludes += setOf(
        "META-INF/DEPENDENCIES",
        "META-INF/LICENSE",
        "META-INF/LICENSE.txt",
        "META-INF/license.txt",
        "META-INF/NOTICE",
        "META-INF/NOTICE.txt",
        "META-INF/notice.txt",
        "META-INF/ASL2.0",
        "META-INF/*.kotlin_module"
      )
    }
  }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
  ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
  ignoreList.add("MAPS_API_KEY")
}

dependencies {
  implementation(files("libs/app-logic.jar"))
  implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.10")
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.coil.video)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation("com.startapp:inapp-sdk:5.0.2")
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
}
