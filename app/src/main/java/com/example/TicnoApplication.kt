package com.example

import android.app.Application
import com.google.android.gms.ads.MobileAds
import java.io.File

class TicnoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Pre-create Chromium WebView Code Cache directories to prevent
        // simple_file_enumerator ENOENT errors during WebView/AdMob cache initialization
        try {
            val httpCacheDir = File(cacheDir, "WebView/Default/HTTP Cache")
            httpCacheDir.mkdirs()
            val codeCacheDir = File(httpCacheDir, "Code Cache")
            File(codeCacheDir, "js").mkdirs()
            File(codeCacheDir, "wasm").mkdirs()
        } catch (_: Throwable) {
            // Ignored - fallback gracefully
        }

        // Initialize Google Mobile Ads SDK immediately on app start
        try {
            MobileAds.initialize(this) {}
        } catch (_: Throwable) {
            // Fallback gracefully if Google Play Services is unavailable
        }

        // Initialize Start.io Ads SDK with App ID 209701719 safely
        try {
            com.example.util.StartIoManager.init(this)
        } catch (_: Throwable) {
            // Fallback gracefully if offline or services unavailable
        }
    }
}
