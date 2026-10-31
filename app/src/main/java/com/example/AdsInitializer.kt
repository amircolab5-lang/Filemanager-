package com.example

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.startapp.sdk.adsbase.StartAppSDK

class AdsInitializer : ContentProvider() {
    override fun onCreate(): Boolean {
        val ctx = context ?: return true

        try {
            // Enable high-speed Coil caching for 120 FPS scrolling
            val imageLoader = ImageLoader.Builder(ctx)
                .memoryCache {
                    MemoryCache.Builder(ctx)
                        .maxSizePercent(0.25)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(ctx.cacheDir.resolve("image_cache"))
                        .maxSizeBytes(100L * 1024 * 1024) // 100MB
                        .build()
                }
                .respectCacheHeaders(false)
                .crossfade(true)
                .build()
            Coil.setImageLoader(imageLoader)
        } catch (_: Throwable) {}

        try {
            // In debug mode, enable test ads so ad requests return test ads instead of NO FILL
            if (BuildConfig.DEBUG) {
                StartAppSDK.setTestAdsEnabled(true)
            }
        } catch (_: Throwable) {}
        return true
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
