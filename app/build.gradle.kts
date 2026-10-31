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
  if (!rootDebugKeystore.exists() && rootDebugKeystoreBase64.exists()) {
    try {
      val decoded = Base64.getDecoder().decode(rootDebugKeystoreBase64.readText().trim())
      rootDebugKeystore.writeBytes(decoded)
    } catch (_: Exception) {}
  }

  val rootReleaseKeystore = file("${rootDir}/release.keystore")
  val rootReleaseKeystoreBase64 = file("${rootDir}/release.keystore.base64")
  if (!rootReleaseKeystore.exists() && rootReleaseKeystoreBase64.exists()) {
    try {
      val decoded = Base64.getDecoder().decode(rootReleaseKeystoreBase64.readText().trim())
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
        keyAlias = System.getenv("KEY_ALIAS") ?: "upload"
        keyPassword = System.getenv("KEY_PASSWORD")
      } else if (releaseKeystore.exists()) {
        storeFile = releaseKeystore
        storePassword = "ticno12345"
        keyAlias = "ticnodevelopers"
        keyPassword = "ticno12345"
      } else {
        // Fallback to auto-generated debug keystore so release builds can package successfully
        storeFile = file("${rootDir}/debug.keystore")
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
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
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
