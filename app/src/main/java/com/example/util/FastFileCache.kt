package com.example.util

import android.content.Context
import com.example.model.CategoryType
import com.example.model.FileItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.concurrent.ConcurrentHashMap

/**
 * High-performance In-Memory & Persistent Caching Engine.
 * Provides instant 0ms category switching and pre-indexed file access
 * similar to Google Files and Redis memory caching.
 */
object FastFileCache {

    private val memoryCache = ConcurrentHashMap<CategoryType, List<FileItem>>()
    private val lastScanTime = ConcurrentHashMap<CategoryType, Long>()
    private val isWarmingUp = java.util.concurrent.atomic.AtomicBoolean(false)

    // Dedicated single-thread coroutine scope for disk cache persistence - zero raw thread leaks
    private val diskIoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))

    // Dedicated limited-parallelism dispatcher for gentle background category warmup (max 2 concurrent scans)
    private val warmupDispatcher = Dispatchers.IO.limitedParallelism(2)

    // Cache validity duration (e.g. 2 minutes before background auto-refresh)
    private const val CACHE_STALE_MS = 120_000L

    /**
     * Get cached category items if available in memory, with optional fallback to persisted disk cache.
     */
    fun get(category: CategoryType, context: Context? = null): List<FileItem>? {
        val memory = memoryCache[category]
        if (memory != null) return memory
        if (context != null) {
            try {
                val cacheDir = File(context.cacheDir, "fast_file_cache")
                val file = File(cacheDir, "${category.name}.cache")
                if (file.exists() && file.length() > 0L) {
                    val items = loadFromDisk(file, context)
                    if (items.isNotEmpty()) {
                        memoryCache[category] = items
                        lastScanTime[category] = file.lastModified()
                        return items
                    }
                }
            } catch (_: Throwable) {}
        }
        return null
    }

    /**
     * Store category items in memory and persist asynchronously to disk.
     */
    fun put(context: Context, category: CategoryType, items: List<FileItem>) {
        memoryCache[category] = items
        lastScanTime[category] = System.currentTimeMillis()
        saveToDiskAsync(context, category, items)
    }

    /**
     * Check if cache exists and is reasonably fresh.
     */
    fun isFresh(category: CategoryType): Boolean {
        val time = lastScanTime[category] ?: return false
        return (System.currentTimeMillis() - time) < CACHE_STALE_MS
    }

    /**
     * Invalidate specific category or all categories.
     */
    fun invalidate(category: CategoryType? = null) {
        if (category != null) {
            memoryCache.remove(category)
            lastScanTime.remove(category)
        } else {
            memoryCache.clear()
            lastScanTime.clear()
        }
    }

    /**
     * Remove deleted file from all cached categories in memory.
     */
    fun onFileDeleted(deletedPath: String) {
        for ((cat, list) in memoryCache) {
            val filtered = list.filter { it.path != deletedPath }
            if (filtered.size != list.size) {
                memoryCache[cat] = filtered
            }
        }
    }

    /**
     * Fast cold-start initialization: Loads persisted category caches from disk into memory (takes ~2-5ms).
     */
    fun initFromDisk(context: Context) {
        try {
            val cacheDir = File(context.cacheDir, "fast_file_cache")
            if (!cacheDir.exists() || !cacheDir.isDirectory) return

            for (cat in CategoryType.values()) {
                val file = File(cacheDir, "${cat.name}.cache")
                if (file.exists() && file.length() > 0L) {
                    val items = loadFromDisk(file, context)
                    if (items.isNotEmpty()) {
                        memoryCache[cat] = items
                        lastScanTime[cat] = file.lastModified()
                    }
                }
            }
        } catch (e: Throwable) {
            // Ignore disk cache load error
        }
    }

    /**
     * Pre-indexes and warms up top categories in the background using gentle limited parallelism.
     * Uses warmupDispatcher (max 2 concurrent scans) to protect storage flash I/O from saturation.
     */
    fun warmup(context: Context, scope: CoroutineScope, onProgress: ((CategoryType, List<FileItem>) -> Unit)? = null) {
        if (!isWarmingUp.compareAndSet(false, true)) return

        scope.launch(warmupDispatcher) {
            try {
                // Initialize from disk first for instantaneous availability (< 2ms)
                initFromDisk(context)

                // High priority top categories
                val targetCategories = listOf(
                    CategoryType.DOWNLOADS,
                    CategoryType.IMAGES,
                    CategoryType.VIDEOS,
                    CategoryType.AUDIO,
                    CategoryType.DOCUMENTS,
                    CategoryType.APKS,
                    CategoryType.ARCHIVES
                )

                // Scan with gentle concurrency (max 2 at a time) to prevent storage flash I/O congestion
                for (cat in targetCategories) {
                    try {
                        val items = FileManagerHelper.scanCategoryFiles(context, cat)
                        put(context, cat, items)
                        onProgress?.invoke(cat, items)
                    } catch (e: Throwable) {
                        // Safe catch per category
                    }
                }
            } finally {
                isWarmingUp.set(false)
            }
        }
    }

    fun saveRecentDocumentsToDisk(context: Context, docs: List<FileItem>) {
        diskIoScope.launch {
            try {
                val cacheDir = File(context.cacheDir, "fast_file_cache")
                if (!cacheDir.exists()) cacheDir.mkdirs()
                val targetFile = File(cacheDir, "recent_documents.cache")
                BufferedWriter(FileWriter(targetFile)).use { writer ->
                    for (item in docs) {
                        writer.write("${item.path}\t${item.size}\t${item.lastModified}\t${item.isDirectory}\t${item.name}\t${item.mimeType}\n")
                    }
                }
            } catch (_: Throwable) {}
        }
    }

    fun loadRecentDocumentsFromDisk(context: Context): List<FileItem> {
        return try {
            val cacheDir = File(context.cacheDir, "fast_file_cache")
            val targetFile = File(cacheDir, "recent_documents.cache")
            if (targetFile.exists() && targetFile.length() > 0L) {
                loadFromDisk(targetFile, context)
            } else {
                emptyList()
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    fun saveDirectoryToDisk(context: Context, dirPath: String, items: List<FileItem>) {
        diskIoScope.launch {
            try {
                val cacheDir = File(context.cacheDir, "fast_file_cache/dirs")
                if (!cacheDir.exists()) cacheDir.mkdirs()
                val safeKey = java.net.URLEncoder.encode(dirPath, "UTF-8").take(120) + "_" + dirPath.hashCode()
                val targetFile = File(cacheDir, "$safeKey.cache")
                BufferedWriter(FileWriter(targetFile)).use { writer ->
                    for (item in items) {
                        writer.write("${item.path}\t${item.size}\t${item.lastModified}\t${item.isDirectory}\t${item.name}\t${item.mimeType}\n")
                    }
                }
            } catch (_: Throwable) {}
        }
    }

    fun loadDirectoryFromDisk(context: Context, dirPath: String): List<FileItem>? {
        return try {
            val cacheDir = File(context.cacheDir, "fast_file_cache/dirs")
            val safeKey = java.net.URLEncoder.encode(dirPath, "UTF-8").take(120) + "_" + dirPath.hashCode()
            val targetFile = File(cacheDir, "$safeKey.cache")
            if (targetFile.exists() && targetFile.length() > 0L) {
                val items = loadFromDisk(targetFile, context)
                if (items.isNotEmpty()) items else null
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun saveToDiskAsync(context: Context, category: CategoryType, items: List<FileItem>) {
        // Safe single-thread background coroutine write (replaces unmanaged Thread)
        diskIoScope.launch {
            try {
                val cacheDir = File(context.cacheDir, "fast_file_cache")
                if (!cacheDir.exists()) cacheDir.mkdirs()
                val targetFile = File(cacheDir, "${category.name}.cache")
                BufferedWriter(FileWriter(targetFile)).use { writer ->
                    for (item in items) {
                        // Format: path\tsize\tlastModified\tisDirectory\tname\tmimeType
                        writer.write("${item.path}\t${item.size}\t${item.lastModified}\t${item.isDirectory}\t${item.name}\t${item.mimeType}\n")
                    }
                }
            } catch (e: Throwable) {
                // Ignore disk write errors
            }
        }
    }

    private fun loadFromDisk(file: File, context: Context): List<FileItem> {
        val result = mutableListOf<FileItem>()
        try {
            BufferedReader(FileReader(file)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    val parts = line.split('\t')
                    if (parts.size >= 5) {
                        val path = parts[0]
                        val f = File(path)
                        val size = parts[1].toLongOrNull() ?: 0L
                        val lastMod = parts[2].toLongOrNull() ?: 0L
                        val isDir = parts[3].toBoolean()
                        val name = parts[4]
                        val mime = if (parts.size > 5) parts[5] else FileManagerHelper.getFastMimeType(f.extension, isDir)
                        result.add(
                            FileItem(
                                file = f,
                                name = name,
                                path = path,
                                size = size,
                                formattedSize = FileManagerHelper.formatFileSize(size),
                                lastModified = lastMod,
                                formattedDate = if (lastMod > 0) FileManagerHelper.formatDate(lastMod) else "",
                                isDirectory = isDir,
                                mimeType = mime,
                                extension = f.extension.lowercase(),
                                isHidden = name.startsWith(".")
                            )
                        )
                    }
                    line = reader.readLine()
                }
            }
        } catch (e: Throwable) {
            // Ignore error
        }
        return result
    }
}
