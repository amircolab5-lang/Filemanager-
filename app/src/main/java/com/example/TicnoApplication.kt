package com.example

import android.app.Application
import java.io.File

class TicnoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Pre-create Chromium WebView Code Cache directories to prevent
        // simple_file_enumerator ENOENT errors during WebView/AdMob cache initialization
        try {
            val codeCacheDir = File(cacheDir, "WebView/Default/HTTP Cache/Code Cache")
            File(codeCacheDir, "js").mkdirs()
            File(codeCacheDir, "wasm").mkdirs()
        } catch (_: Throwable) {
            // Ignored - fallback gracefully
        }
    }
}
