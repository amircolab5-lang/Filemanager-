package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CategoryType
import com.example.model.FileItem
import com.example.model.SortMode
import com.example.model.StorageStats
import com.example.model.StorageVolumeInfo
import com.example.model.ViewMode
import com.example.util.FastFileCache
import com.example.util.FileManagerHelper
import com.example.util.PermissionHelper
import com.example.util.THideManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class ScreenDestination {
    HOME,
    EXPLORER,
    WHATSAPP_STATUS,
    HIDDEN_FILES,
    CATEGORY_VIEW,
    STORAGE_CLEANER,
    MORE_CATEGORIES,
    ALL_FILES,
    SETTINGS
}

enum class StatusFilter {
    ALL,
    PHOTOS,
    VIDEOS
}

sealed interface ActiveDialog {
    data class CreateFolder(val parentDir: File) : ActiveDialog
    data class CreateFile(val parentDir: File) : ActiveDialog
    data class Rename(val item: FileItem) : ActiveDialog
    data class DeleteConfirm(val items: List<FileItem>) : ActiveDialog
    data class FileDetails(val item: FileItem) : ActiveDialog
    data class ImageViewer(val item: FileItem) : ActiveDialog
    data class TextViewer(val item: FileItem, val content: String) : ActiveDialog
    data object FeedbackDialog : ActiveDialog
    data object RateAppDialog : ActiveDialog
    data object AboutDialog : ActiveDialog
    data object PrivacyPolicyDialog : ActiveDialog
    data object CollectionAddDialog : ActiveDialog
}

data class FileManagerUiState(
    val currentScreen: ScreenDestination = ScreenDestination.HOME,
    val isDarkMode: Boolean = false, // Light by default as requested!
    val hasStoragePermission: Boolean = false,
    val currentDirectory: File = FileManagerHelper.getStorageRoot(),
    val currentFiles: List<FileItem> = emptyList(),
    val selectedFiles: Set<FileItem> = emptySet(),
    val isSelectionMode: Boolean = false,
    val clipboardFiles: List<File> = emptyList(),
    val isCutOperation: Boolean = false,
    val showHiddenFiles: Boolean = true,
    val sortMode: SortMode = SortMode.NAME_ASC,
    val viewMode: ViewMode = ViewMode.LIST,
    val searchQuery: String = "",
    val storageStats: StorageStats = StorageStats(),
    val storageVolumes: List<StorageVolumeInfo> = emptyList(),
    val sdCardVolume: StorageVolumeInfo? = null,
    val categoryCounts: Map<CategoryType, Int> = emptyMap(),
    val recentDocuments: List<FileItem> = emptyList(),
    val whatsAppStatuses: List<FileItem> = emptyList(),
    val statusFilter: StatusFilter = StatusFilter.ALL,
    val hiddenFiles: List<FileItem> = emptyList(),
    val selectedCategory: CategoryType? = null,
    val categoryFiles: List<FileItem> = emptyList(),
    val categoryFolders: List<FileItem> = emptyList(),
    val largeFiles: List<FileItem> = emptyList(),
    val junkCleanResult: com.example.util.JunkCleanResult? = null,
    val isCleaning: Boolean = false,
    val isCleanComplete: Boolean = false,
    val cleanedBytesFormatted: String = "",
    val isLoading: Boolean = false,
    val activeDialog: ActiveDialog? = null,
    val toastMessage: String? = null
)

class FileManagerViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("ticno_file_manager_prefs", Context.MODE_PRIVATE)

    private fun loadCachedStorageStats(): StorageStats {
        val total = prefs.getLong("stat_total", 0L)
        if (total > 0L) {
            val used = prefs.getLong("stat_used", 0L)
            val free = prefs.getLong("stat_free", 0L)
            val pct = prefs.getFloat("stat_pct", 0f)
            return StorageStats(
                totalBytes = total,
                freeBytes = free,
                usedBytes = used,
                usedPercentage = pct,
                formattedTotal = FileManagerHelper.formatFileSize(total),
                formattedUsed = FileManagerHelper.formatFileSize(used),
                formattedFree = FileManagerHelper.formatFileSize(free)
            )
        }
        return StorageStats(
            totalBytes = 64L * 1024 * 1024 * 1024,
            freeBytes = 32L * 1024 * 1024 * 1024,
            usedBytes = 32L * 1024 * 1024 * 1024,
            usedPercentage = 0.5f,
            formattedTotal = "64.0 GB",
            formattedUsed = "32.0 GB",
            formattedFree = "32.0 GB"
        )
    }

    private fun saveCachedStorageStats(stats: StorageStats) {
        prefs.edit()
            .putLong("stat_total", stats.totalBytes)
            .putLong("stat_used", stats.usedBytes)
            .putLong("stat_free", stats.freeBytes)
            .putFloat("stat_pct", stats.usedPercentage)
            .apply()
    }

    private fun loadCachedCategoryCounts(): Map<CategoryType, Int> {
        val result = mutableMapOf<CategoryType, Int>()
        for (cat in CategoryType.values()) {
            val key = "cat_count_${cat.name}"
            if (prefs.contains(key)) {
                result[cat] = prefs.getInt(key, 0)
            }
        }
        return result
    }

    private fun saveCachedCategoryCounts(counts: Map<CategoryType, Int>) {
        val editor = prefs.edit()
        counts.forEach { (cat, count) ->
            editor.putInt("cat_count_${cat.name}", count)
        }
        editor.apply()
    }

    private val _uiState = MutableStateFlow(
        FileManagerUiState(
            isDarkMode = prefs.getBoolean("pref_dark_mode", false),
            storageStats = loadCachedStorageStats(),
            categoryCounts = loadCachedCategoryCounts()
        )
    )
    val uiState: StateFlow<FileManagerUiState> = _uiState.asStateFlow()

    // Concurrency limiter to protect eMMC/UFS flash storage from I/O congestion
    private val backgroundIo = Dispatchers.IO.limitedParallelism(2)

    init {
        val hasPerm = PermissionHelper.hasStoragePermission(getApplication())
        _uiState.update { it.copy(hasStoragePermission = hasPerm) }

        // Start initial stats and cache load asynchronously on background IO dispatcher
        // Zero work on Main Thread during cold start ensures instant UI display (<5ms)
        viewModelScope.launch(backgroundIo) {
            FastFileCache.initFromDisk(getApplication())
            loadStorageStats()
        }

        if (hasPerm) {
            // Load category counts progressively and stagger heavy scans to prevent startup flash lag
            viewModelScope.launch(backgroundIo) {
                loadCategoryCounts()
                loadRecentDocuments()

                // Stagger background category warmup by 2s so initial UI renders with zero frame drops
                delay(2000)
                FastFileCache.warmup(getApplication(), viewModelScope)
            }
        }
    }

    fun checkPermission() {
        try {
            val prevPerm = _uiState.value.hasStoragePermission
            val hasPerm = PermissionHelper.hasStoragePermission(getApplication())
            _uiState.update { it.copy(hasStoragePermission = hasPerm) }
            // Only trigger full refresh if permission state transitioned from false to true
            if (!prevPerm && hasPerm) {
                refreshAll()
            }
        } catch (e: Throwable) {
            _uiState.update { it.copy(hasStoragePermission = false) }
        }
    }

    fun refreshAll() {
        FastFileCache.invalidate()
        loadStorageStats()
        loadCurrentDirectory()
        loadCategoryCounts()
        loadRecentDocuments()
        viewModelScope.launch(backgroundIo) {
            delay(1500)
            FastFileCache.warmup(getApplication(), viewModelScope)
        }
    }

    fun invalidateCategoryCache(category: CategoryType? = null) {
        FastFileCache.invalidate(category)
    }

    fun toggleDarkMode(enabled: Boolean) {
        try {
            prefs.edit().putBoolean("pref_dark_mode", enabled).apply()
            _uiState.update { it.copy(isDarkMode = enabled) }
        } catch (_: Throwable) {}
    }

    fun showFeedbackDialog() {
        _uiState.update { it.copy(activeDialog = ActiveDialog.FeedbackDialog) }
    }

    fun showRateDialog() {
        _uiState.update { it.copy(activeDialog = ActiveDialog.RateAppDialog) }
    }

    fun showAboutDialog() {
        _uiState.update { it.copy(activeDialog = ActiveDialog.AboutDialog) }
    }

    fun showPrivacyPolicyDialog() {
        _uiState.update { it.copy(activeDialog = ActiveDialog.PrivacyPolicyDialog) }
    }

    fun showCollectionAddDialog() {
        _uiState.update { it.copy(activeDialog = ActiveDialog.CollectionAddDialog) }
    }

    fun loadRecentDocuments() {
        viewModelScope.launch {
            val docs = withContext(Dispatchers.IO) {
                FileManagerHelper.scanRecentDocuments(getApplication(), maxCount = 100)
            }
            _uiState.update { it.copy(recentDocuments = docs) }
        }
    }

    fun loadCategoryCounts() {
        viewModelScope.launch(backgroundIo) {
            val counts = FileManagerHelper.queryCategoryCountsReactive(getApplication()) { cat, count ->
                _uiState.update { state ->
                    val updated = state.categoryCounts.toMutableMap()
                    updated[cat] = count
                    state.copy(categoryCounts = updated)
                }
            }
            _uiState.update { it.copy(categoryCounts = counts) }
            saveCachedCategoryCounts(counts)
        }
    }

    private suspend fun computeCategoryFolders(files: List<FileItem>): List<FileItem> = withContext(Dispatchers.Default) {
        val bucketGroups = files.filter { !it.isDirectory }.groupBy { it.bucketName.ifBlank { it.file.parentFile?.name ?: "Other" } }
        bucketGroups.map { (bucketName, bucketFiles) ->
            val firstFile = bucketFiles.first()
            val parentFolder = firstFile.file.parentFile ?: firstFile.file
            FileItem(
                file = parentFolder,
                name = bucketName,
                path = parentFolder.absolutePath,
                size = bucketFiles.sumOf { it.size },
                isDirectory = true,
                isHidden = false,
                extension = "",
                mimeType = "resource/folder",
                lastModified = bucketFiles.maxOfOrNull { it.lastModified } ?: 0L,
                formattedSize = "${bucketFiles.size} items",
                formattedDate = "",
                itemCount = bucketFiles.size
            )
        }.sortedBy { it.name.lowercase() }
    }

    fun navigateTo(destination: ScreenDestination) {
        _uiState.update { it.copy(currentScreen = destination, searchQuery = "", isSelectionMode = false, selectedFiles = emptySet()) }
        when (destination) {
            ScreenDestination.HOME -> {
                loadStorageStats()
                if (_uiState.value.categoryCounts.isEmpty()) {
                    loadCategoryCounts()
                }
                if (_uiState.value.recentDocuments.isEmpty()) {
                    loadRecentDocuments()
                }
            }
            ScreenDestination.EXPLORER -> loadCurrentDirectory()
            ScreenDestination.WHATSAPP_STATUS -> loadWhatsAppStatuses()
            ScreenDestination.HIDDEN_FILES -> loadHiddenFiles()
            ScreenDestination.STORAGE_CLEANER -> scanStorageJunk()
            ScreenDestination.MORE_CATEGORIES -> {
                if (_uiState.value.categoryCounts.isEmpty()) {
                    loadCategoryCounts()
                }
            }
            ScreenDestination.ALL_FILES -> loadStorageStats()
            ScreenDestination.SETTINGS -> {}
            ScreenDestination.CATEGORY_VIEW -> {}
        }
    }

    fun openCategory(category: CategoryType) {
        if (category == CategoryType.WHATSAPP_STATUS) {
            navigateTo(ScreenDestination.WHATSAPP_STATUS)
            return
        }
        if (category == CategoryType.HIDDEN_FILES || category == CategoryType.XHIDE) {
            navigateTo(ScreenDestination.HIDDEN_FILES)
            return
        }

        // Instant response from cache (0ms delay, just like Google Files persistent memory cache)
        val cached = FastFileCache.get(category)
        if (cached != null) {
            viewModelScope.launch {
                val folders = if (category == CategoryType.IMAGES) computeCategoryFolders(cached) else emptyList()
                _uiState.update {
                    it.copy(
                        currentScreen = ScreenDestination.CATEGORY_VIEW,
                        selectedCategory = category,
                        categoryFiles = cached,
                        categoryFolders = folders,
                        isLoading = false,
                        isSelectionMode = false,
                        selectedFiles = emptySet()
                    )
                }
            }
            // If stale (> 2 minutes), quietly refresh in background without showing a loader
            if (!FastFileCache.isFresh(category)) {
                viewModelScope.launch(backgroundIo) {
                    val fresh = FileManagerHelper.scanCategoryFiles(getApplication(), category)
                    FastFileCache.put(getApplication(), category, fresh)
                    if (_uiState.value.currentScreen == ScreenDestination.CATEGORY_VIEW && _uiState.value.selectedCategory == category) {
                        val freshFolders = if (category == CategoryType.IMAGES) computeCategoryFolders(fresh) else emptyList()
                        _uiState.update { it.copy(categoryFiles = fresh, categoryFolders = freshFolders) }
                    }
                }
            }
            return
        }

        _uiState.update {
            it.copy(
                currentScreen = ScreenDestination.CATEGORY_VIEW,
                selectedCategory = category,
                categoryFolders = emptyList(),
                isLoading = true,
                isSelectionMode = false,
                selectedFiles = emptySet()
            )
        }

        viewModelScope.launch(backgroundIo) {
            val items = FileManagerHelper.scanCategoryFiles(getApplication(), category)
            FastFileCache.put(getApplication(), category, items)
            val folders = if (category == CategoryType.IMAGES) computeCategoryFolders(items) else emptyList()
            _uiState.update { it.copy(categoryFiles = items, categoryFolders = folders, isLoading = false) }
        }
    }

    fun extractZip(item: FileItem) {
        viewModelScope.launch {
            showToast("Extracting ${item.name}...")
            val resultDir = withContext(Dispatchers.IO) {
                FileManagerHelper.extractZipFile(item.file)
            }
            if (resultDir != null) {
                showToast("Extracted to: ${resultDir.name}")
                loadCurrentDirectory()
                if (_uiState.value.currentScreen == ScreenDestination.CATEGORY_VIEW) {
                    _uiState.value.selectedCategory?.let { openCategory(it) }
                }
            } else {
                showToast("Failed to extract zip file")
            }
        }
    }

    fun compressFiles(items: List<FileItem>) {
        if (items.isEmpty()) return
        viewModelScope.launch {
            val first = items.first()
            val zipName = if (items.size == 1) "${first.file.nameWithoutExtension}.zip" else "Archive_${System.currentTimeMillis() / 1000}.zip"
            val parent = first.file.parentFile ?: FileManagerHelper.getStorageRoot()
            val zipOut = File(parent, zipName)

            showToast("Compressing to $zipName...")
            val success = withContext(Dispatchers.IO) {
                FileManagerHelper.compressToZip(items.map { it.file }, zipOut)
            }
            if (success) {
                showToast("Created: $zipName")
                clearSelection()
                loadCurrentDirectory()
                if (_uiState.value.currentScreen == ScreenDestination.CATEGORY_VIEW) {
                    _uiState.value.selectedCategory?.let { openCategory(it) }
                }
            } else {
                showToast("Failed to create zip archive")
            }
        }
    }

    fun openDirectory(directory: File) {
        val isDownloadDir = directory.name.equals("Download", ignoreCase = true) || directory.name.equals("Downloads", ignoreCase = true)
        _uiState.update {
            it.copy(
                currentDirectory = directory,
                currentScreen = ScreenDestination.EXPLORER,
                selectedFiles = emptySet(),
                isSelectionMode = false,
                searchQuery = "",
                sortMode = if (isDownloadDir) SortMode.DATE_DESC else it.sortMode
            )
        }
        loadCurrentDirectory()
    }

    fun navigateUp(): Boolean {
        val current = _uiState.value.currentDirectory
        val root = FileManagerHelper.getStorageRoot()
        val sdRoot = _uiState.value.sdCardVolume?.rootDir

        val isAtInternalRoot = current.absolutePath == root.absolutePath
        val isAtSdRoot = sdRoot != null && current.absolutePath == sdRoot.absolutePath

        if (!isAtInternalRoot && !isAtSdRoot && current.parentFile != null) {
            openDirectory(current.parentFile!!)
            return true
        } else if (_uiState.value.currentScreen != ScreenDestination.HOME) {
            navigateTo(ScreenDestination.HOME)
            return true
        }
        return false
    }

    fun loadCurrentDirectory() {
        val dir = _uiState.value.currentDirectory
        val showHidden = _uiState.value.showHiddenFiles
        val sort = _uiState.value.sortMode

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val files = withContext(Dispatchers.IO) {
                try {
                    FileManagerHelper.listFiles(getApplication(), dir, showHidden = showHidden, sortMode = sort)
                } catch (e: Throwable) {
                    emptyList()
                }
            }
            _uiState.update { it.copy(currentFiles = files, isLoading = false) }
        }
    }

    fun loadStorageStats() {
        viewModelScope.launch {
            val (stats, volumes) = withContext(Dispatchers.IO) {
                val s = FileManagerHelper.getStorageStats()
                val v = FileManagerHelper.getStorageVolumes(getApplication())
                Pair(s, v)
            }
            val sd = volumes.firstOrNull { it.isSdCard && it.isMounted && it.totalBytes > 0L }
            _uiState.update {
                it.copy(
                    storageStats = stats,
                    storageVolumes = volumes,
                    sdCardVolume = sd
                )
            }
            saveCachedStorageStats(stats)
        }
    }

    fun openSdCard() {
        try {
            val sdRoot = _uiState.value.sdCardVolume?.rootDir
                ?: FileManagerHelper.getSdCardRoot(getApplication())

            if (sdRoot != null) {
                val isReadable = try {
                    sdRoot.exists() && sdRoot.isDirectory && (sdRoot.listFiles() != null)
                } catch (e: Throwable) {
                    false
                }
                if (isReadable) {
                    openDirectory(sdRoot)
                    return
                }
            }
            showToast("SD card is not accessible or not inserted")
        } catch (e: Throwable) {
            showToast("Failed to access SD card")
        }
    }

    fun loadWhatsAppStatuses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val statuses = withContext(Dispatchers.IO) {
                FileManagerHelper.scanWhatsAppStatuses(getApplication())
            }
            _uiState.update { it.copy(whatsAppStatuses = statuses, isLoading = false) }
        }
    }

    fun setStatusFilter(filter: StatusFilter) {
        _uiState.update { it.copy(statusFilter = filter) }
    }

    fun saveWhatsAppStatus(item: FileItem) {
        viewModelScope.launch {
            val saved = withContext(Dispatchers.IO) {
                FileManagerHelper.saveStatusMedia(getApplication(), item.file)
            }
            if (saved != null) {
                showToast("Status saved to Pictures/TicnoSaver: ${saved.name}")
            } else {
                showToast("Failed to save status")
            }
        }
    }

    fun loadHiddenFiles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val hidden = withContext(Dispatchers.IO) {
                THideManager.getVaultFiles(getApplication())
            }
            _uiState.update { it.copy(hiddenFiles = hidden, isLoading = false) }
        }
    }

    fun unhideItem(item: FileItem) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                THideManager.unhideFile(getApplication(), item)
            }
            if (success) {
                showToast("Restored ${item.name} to device storage")
                loadHiddenFiles()
                loadCategoryCounts()
                loadCurrentDirectory()
            } else {
                showToast("Failed to unhide file")
            }
        }
    }

    fun hideFilesToVault(files: List<File>) {
        viewModelScope.launch {
            val count = withContext(Dispatchers.IO) {
                THideManager.hideFiles(getApplication(), files)
            }
            if (count > 0) {
                showToast("Moved $count file(s) into T-Hide vault")
                loadHiddenFiles()
                loadCategoryCounts()
                loadCurrentDirectory()
            } else {
                showToast("Failed to hide file(s)")
            }
        }
    }

    fun hideSelectedFilesToVault() {
        val files = _uiState.value.selectedFiles.map { it.file }
        if (files.isEmpty()) return
        clearSelection()
        hideFilesToVault(files)
    }

    fun deleteVaultItem(item: FileItem) {
        viewModelScope.launch {
            val deleted = withContext(Dispatchers.IO) {
                THideManager.deletePermanently(getApplication(), item)
            }
            if (deleted) {
                showToast("Permanently deleted from T-Hide")
                loadHiddenFiles()
                loadCategoryCounts()
            } else {
                showToast("Failed to delete file")
            }
        }
    }

    fun loadLargeFiles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val files = withContext(Dispatchers.IO) {
                FileManagerHelper.scanLargeFiles(getApplication())
            }
            _uiState.update { it.copy(largeFiles = files, isLoading = false) }
        }
    }

    fun scanStorageJunk() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isCleanComplete = false) }
            val result = withContext(Dispatchers.IO) {
                FileManagerHelper.scanJunkAndCache(getApplication())
            }
            _uiState.update {
                it.copy(
                    junkCleanResult = result,
                    largeFiles = result.largeFiles,
                    isLoading = false
                )
            }
        }
    }

    fun cleanStorageJunk() {
        val junk = _uiState.value.junkCleanResult ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isCleaning = true) }
            val toDelete = mutableListOf<File>()
            toDelete.addAll(junk.cacheFiles)
            toDelete.addAll(junk.tempFiles)
            toDelete.addAll(junk.apkFiles)

            val freedBytes = withContext(Dispatchers.IO) {
                FileManagerHelper.cleanJunkFiles(getApplication(), toDelete)
            }
            val formatted = FileManagerHelper.formatFileSize(freedBytes)

            loadStorageStats()
            if (freedBytes > 0) {
                showToast("Cleaned $formatted of junk files!")
            } else {
                showToast("Storage is clean!")
            }

            _uiState.update {
                it.copy(
                    isCleaning = false,
                    isCleanComplete = true,
                    cleanedBytesFormatted = formatted,
                    junkCleanResult = it.junkCleanResult?.copy(
                        cacheBytes = 0L,
                        apkBytes = 0L,
                        tempBytes = 0L,
                        totalCleanableBytes = 0L,
                        formattedCleanableSize = "0 B"
                    )
                )
            }
        }
    }

    fun toggleShowHiddenFiles() {
        _uiState.update { it.copy(showHiddenFiles = !it.showHiddenFiles) }
        loadCurrentDirectory()
    }

    fun setShowHiddenFiles(show: Boolean) {
        _uiState.update { it.copy(showHiddenFiles = show) }
        loadCurrentDirectory()
    }

    fun setSortMode(sortMode: SortMode) {
        _uiState.update { it.copy(sortMode = sortMode) }
        loadCurrentDirectory()
    }

    fun toggleViewMode() {
        val next = if (_uiState.value.viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST
        _uiState.update { it.copy(viewMode = next) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    // Selection
    fun toggleFileSelection(item: FileItem) {
        _uiState.update { state ->
            val newSelection = state.selectedFiles.toMutableSet()
            if (newSelection.any { it.path == item.path }) {
                newSelection.removeAll { it.path == item.path }
            } else {
                newSelection.add(item)
            }
            state.copy(
                selectedFiles = newSelection,
                isSelectionMode = newSelection.isNotEmpty()
            )
        }
    }

    fun selectAllFiles() {
        _uiState.update { state ->
            val targetList = if (state.currentScreen == ScreenDestination.CATEGORY_VIEW) state.categoryFiles else state.currentFiles
            if (state.selectedFiles.size == targetList.size && targetList.isNotEmpty()) {
                state.copy(
                    selectedFiles = emptySet(),
                    isSelectionMode = false
                )
            } else {
                state.copy(
                    selectedFiles = targetList.toSet(),
                    isSelectionMode = targetList.isNotEmpty()
                )
            }
        }
    }

    fun selectAllGivenFiles(items: List<FileItem>) {
        _uiState.update { state ->
            if (state.selectedFiles.size == items.size && items.isNotEmpty()) {
                state.copy(
                    selectedFiles = emptySet(),
                    isSelectionMode = false
                )
            } else {
                state.copy(
                    selectedFiles = items.toSet(),
                    isSelectionMode = items.isNotEmpty()
                )
            }
        }
    }

    fun clearSelection() {
        _uiState.update {
            it.copy(
                selectedFiles = emptySet(),
                isSelectionMode = false
            )
        }
    }

    // Clipboard
    fun copySelected() {
        val files = _uiState.value.selectedFiles.map { it.file }
        _uiState.update {
            it.copy(
                clipboardFiles = files,
                isCutOperation = false,
                isSelectionMode = false,
                selectedFiles = emptySet()
            )
        }
        showToast("Copied ${files.size} item(s) to clipboard")
    }

    fun cutSelected() {
        val files = _uiState.value.selectedFiles.map { it.file }
        _uiState.update {
            it.copy(
                clipboardFiles = files,
                isCutOperation = true,
                isSelectionMode = false,
                selectedFiles = emptySet()
            )
        }
        showToast("Cut ${files.size} item(s) to clipboard")
    }

    fun pasteClipboard() {
        val files = _uiState.value.clipboardFiles
        if (files.isEmpty()) return
        val dest = _uiState.value.currentDirectory
        val isCut = _uiState.value.isCutOperation
        val app = getApplication<Application>()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val count = withContext(Dispatchers.IO) {
                if (isCut) {
                    FileManagerHelper.move(files, dest, app)
                } else {
                    FileManagerHelper.copy(files, dest, app)
                }
            }
            _uiState.update {
                it.copy(
                    clipboardFiles = if (isCut) emptyList() else it.clipboardFiles,
                    isLoading = false
                )
            }
            showToast("Pasted $count item(s) into ${dest.name}")
            invalidateCategoryCache()
            loadCurrentDirectory()
            loadStorageStats()
            if (_uiState.value.currentScreen == ScreenDestination.CATEGORY_VIEW) {
                _uiState.value.selectedCategory?.let { openCategory(it) }
            }
        }
    }

    fun clearClipboard() {
        _uiState.update { it.copy(clipboardFiles = emptyList()) }
    }

    // CRUD & Dialogs
    fun showCreateFolderDialog() {
        _uiState.update { it.copy(activeDialog = ActiveDialog.CreateFolder(it.currentDirectory)) }
    }

    fun showCreateFileDialog() {
        _uiState.update { it.copy(activeDialog = ActiveDialog.CreateFile(it.currentDirectory)) }
    }

    fun showRenameDialog(item: FileItem) {
        _uiState.update { it.copy(activeDialog = ActiveDialog.Rename(item)) }
    }

    fun showDeleteDialog(items: List<FileItem>) {
        _uiState.update { it.copy(activeDialog = ActiveDialog.DeleteConfirm(items)) }
    }

    fun showFileDetails(item: FileItem) {
        _uiState.update { it.copy(activeDialog = ActiveDialog.FileDetails(item)) }
    }

    fun showImageViewer(item: FileItem) {
        _uiState.update { it.copy(activeDialog = ActiveDialog.ImageViewer(item)) }
    }

    fun showTextViewer(item: FileItem) {
        viewModelScope.launch {
            val content = withContext(Dispatchers.IO) {
                try {
                    item.file.readLines().take(500).joinToString("\n")
                } catch (e: Exception) {
                    "Error reading file text: ${e.localizedMessage}"
                }
            }
            _uiState.update { it.copy(activeDialog = ActiveDialog.TextViewer(item, content)) }
        }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(activeDialog = null) }
    }

    fun confirmCreateFolder(name: String) {
        val dir = _uiState.value.currentDirectory
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                FileManagerHelper.createFolder(dir, name)
            }
            dismissDialog()
            if (success) {
                showToast("Folder created: $name")
                loadCurrentDirectory()
            } else {
                showToast("Failed to create folder")
            }
        }
    }

    fun confirmCreateFile(name: String, content: String) {
        val dir = _uiState.value.currentDirectory
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                FileManagerHelper.createFile(dir, name, content)
            }
            dismissDialog()
            if (success) {
                showToast("File created: $name")
                loadCurrentDirectory()
            } else {
                showToast("Failed to create file")
            }
        }
    }

    fun confirmRename(item: FileItem, newName: String) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                FileManagerHelper.rename(item.file, newName)
            }
            dismissDialog()
            if (success) {
                showToast("Renamed to $newName")
                invalidateCategoryCache()
                loadCurrentDirectory()
                if (_uiState.value.currentScreen == ScreenDestination.HIDDEN_FILES) {
                    loadHiddenFiles()
                }
                if (_uiState.value.currentScreen == ScreenDestination.CATEGORY_VIEW) {
                    _uiState.value.selectedCategory?.let { openCategory(it) }
                }
            } else {
                showToast("Failed to rename")
            }
        }
    }

    fun confirmDelete(items: List<FileItem>) {
        if (items.isEmpty()) return

        // 1. Immediately dismiss dialog and clear selection so user never experiences UI lag or double clicks
        dismissDialog()
        clearSelection()

        val deletedPaths = items.map { it.path }.toSet()

        // 2. Optimistic instant UI update (0ms): file entries disappear immediately from current list
        _uiState.update { current ->
            val updatedDirFiles = current.currentFiles.filterNot { it.path in deletedPaths }
            val updatedCategoryFiles = current.categoryFiles.filterNot { it.path in deletedPaths }
            val updatedCategoryFolders = current.categoryFolders.mapNotNull { folderItem ->
                // If folder itself was deleted, remove it
                if (folderItem.path in deletedPaths) null else folderItem
            }
            val updatedLarge = current.largeFiles.filterNot { it.path in deletedPaths }
            val updatedRecent = current.recentDocuments.filterNot { it.path in deletedPaths }
            val updatedHidden = current.hiddenFiles.filterNot { it.path in deletedPaths }
            val updatedWhatsApp = current.whatsAppStatuses.filterNot { it.path in deletedPaths }
            val updatedJunk = current.junkCleanResult?.let { j ->
                j.copy(largeFiles = j.largeFiles.filterNot { it.path in deletedPaths })
            }
            current.copy(
                currentFiles = updatedDirFiles,
                categoryFiles = updatedCategoryFiles,
                categoryFolders = updatedCategoryFolders,
                largeFiles = updatedLarge,
                recentDocuments = updatedRecent,
                hiddenFiles = updatedHidden,
                whatsAppStatuses = updatedWhatsApp,
                junkCleanResult = updatedJunk
            )
        }

        // 3. Perform file deletion on background IO dispatcher
        viewModelScope.launch {
            try {
                val count = withContext(Dispatchers.IO) {
                    FileManagerHelper.deleteRecursively(items.map { it.file }, getApplication())
                }

                if (count == 0 && items.isNotEmpty()) {
                    val hasSdCard = items.any { FileManagerHelper.isRemovableSdCardPath(it.path) }
                    if (hasSdCard) {
                        showToast("Cannot delete SD Card file: Android requires SD Card root permission. Please delete via system Files app.")
                    } else {
                        showToast("Could not delete file(s). System storage may be protected or read-only.")
                    }
                    // Re-sync with disk if deletion was rejected by system
                    loadCurrentDirectory()
                } else {
                    showToast("Deleted $count item(s)")
                }

                // Update caches in memory without heavy re-scans
                items.forEach { FastFileCache.onFileDeleted(it.path) }
                loadStorageStats()
            } catch (e: Exception) {
                showToast("Delete operation encountered an issue: ${e.localizedMessage ?: "Unknown error"}")
                loadCurrentDirectory()
                loadStorageStats()
            }
        }
    }

    fun showToast(message: String) {
        _uiState.update { it.copy(toastMessage = message) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
