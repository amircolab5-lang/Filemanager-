package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.model.CategoryType
import com.example.model.FileItem
import com.example.util.FileManagerHelper
import com.example.ui.components.AboutDialog
import com.example.ui.components.CollectionAddDialog
import com.example.ui.components.CreateFileDialog
import com.example.ui.components.CreateFolderDialog
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.FeedbackDialog
import com.example.ui.components.FileDetailsDialog
import com.example.ui.components.ImageViewerDialog
import com.example.ui.components.PrivacyPolicyDialog
import com.example.ui.components.RateAppDialog
import com.example.ui.components.RenameDialog
import com.example.ui.components.TextViewerDialog
import com.example.ui.components.TicnoHeader
import com.example.ui.screens.AllFilesScreen
import com.example.ui.screens.CategoryViewScreen
import com.example.ui.screens.ExplorerScreen
import com.example.ui.screens.HiddenFilesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MoreCategoriesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StorageCleanScreen
import com.example.ui.screens.THideVaultScreen
import com.example.ui.screens.WhatsAppStatusScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ActiveDialog
import com.example.ui.viewmodel.FileManagerViewModel
import com.example.ui.viewmodel.ScreenDestination
import com.example.util.FileIntentHelper
import com.example.util.PermissionHelper
import coil.Coil
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: FileManagerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }

        val prefs = getSharedPreferences("ticno_file_manager_prefs", android.content.Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("pref_dark_mode", false)
        val initialBg = if (isDarkMode) android.graphics.Color.parseColor("#0F1115") else android.graphics.Color.parseColor("#F8FAFC")
        window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(initialBg))
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { isDarkMode },
            navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { isDarkMode }
        )
        val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = !isDarkMode
        insetsController.isAppearanceLightNavigationBars = !isDarkMode

        val isOnlyPortrait = prefs.getBoolean("pref_only_portrait", false)
        requestedOrientation = if (isOnlyPortrait) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }

        // Background asynchronous initialization to ensure instant cold startup (<2ms)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val imageLoader = ImageLoader.Builder(applicationContext)
                    .components {
                        add(VideoFrameDecoder.Factory())
                    }
                    .crossfade(true)
                    .build()
                Coil.setImageLoader(imageLoader)
            } catch (_: Throwable) {}

            try {
                val httpCacheDir = java.io.File(applicationContext.cacheDir, "WebView/Default/HTTP Cache")
                httpCacheDir.mkdirs()
                val codeCacheDir = java.io.File(httpCacheDir, "Code Cache")
                java.io.File(codeCacheDir, "js").mkdirs()
                java.io.File(codeCacheDir, "wasm").mkdirs()
            } catch (_: Throwable) {}

            // Initialize MobileAds in background promptly
            try {
                MobileAds.initialize(applicationContext) {}
            } catch (_: Throwable) {}
        }

        // Show Start.io Splash ad on launch safely
        com.example.util.StartIoManager.showSplash(this, savedInstanceState)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = uiState.isDarkMode) {
                TicnoFileApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkPermission()
        viewModel.onAppResumed()
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
    }
}

@Composable
fun TicnoFileApp(viewModel: FileManagerViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Toast handler
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Dynamic screen orientation lock handler
    LaunchedEffect(uiState.isOnlyPortrait) {
        val activity = context as? ComponentActivity ?: return@LaunchedEffect
        val target = if (uiState.isOnlyPortrait) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        if (activity.requestedOrientation != target) {
            activity.requestedOrientation = target
        }
    }

    // Dynamic Edge-to-Edge System Bar update (ensures phone time, battery, wifi, and nav buttons are always sharp & visible)
    LaunchedEffect(uiState.isDarkMode) {
        val activity = context as? ComponentActivity ?: return@LaunchedEffect
        val isDark = uiState.isDarkMode
        val window = activity.window
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        activity.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { isDark },
            navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { isDark }
        )
        val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = !isDark
        insetsController.isAppearanceLightNavigationBars = !isDark
    }

    // Back handling
    BackHandler(enabled = true) {
        when {
            uiState.activeDialog != null -> viewModel.dismissDialog()
            uiState.isSelectionMode -> viewModel.clearSelection()
            uiState.currentScreen == ScreenDestination.EXPLORER -> {
                val handled = viewModel.navigateUp()
                if (!handled) {
                    if (viewModel.openedFromCategory != null) {
                        viewModel.openCategory(viewModel.openedFromCategory!!)
                    } else {
                        viewModel.navigateTo(ScreenDestination.ALL_FILES)
                    }
                }
            }
            uiState.currentScreen != ScreenDestination.HOME -> viewModel.navigateTo(ScreenDestination.HOME)
            else -> (context as? ComponentActivity)?.moveTaskToBack(true)
        }
    }

    val onOpenFile: (FileItem) -> Unit = { item ->
        if (item.isInstalledApk && !item.packageName.isNullOrBlank()) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(item.packageName)
            if (launchIntent != null) {
                context.startActivity(launchIntent)
            } else {
                FileIntentHelper.openFile(context, item.file)
            }
        } else if (item.isImage) {
            viewModel.showImageViewer(item)
        } else if (item.isDocument && item.extension in listOf("txt", "json", "xml", "log", "md", "csv")) {
            viewModel.showTextViewer(item)
        } else {
            FileIntentHelper.openFile(context, item.file)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .displayCutoutPadding()
            .navigationBarsPadding(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (uiState.currentScreen == ScreenDestination.HOME) {
                TicnoHeader(
                    onOpenSettings = { viewModel.navigateTo(ScreenDestination.SETTINGS) },
                    onRefresh = { viewModel.refreshAll() }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 1400.dp)
            ) {
                when (uiState.currentScreen) {
                ScreenDestination.HOME -> {
                    HomeScreen(
                        storageStats = uiState.storageStats,
                        hasPermission = uiState.hasStoragePermission,
                        categoryCounts = uiState.categoryCounts,
                        whatsAppStatusCount = uiState.categoryCounts[CategoryType.WHATSAPP_STATUS] ?: uiState.whatsAppStatuses.size,
                        recentDocuments = uiState.recentDocuments,
                        onRequestPermission = {
                            context.startActivity(PermissionHelper.requestStoragePermissionIntent(context))
                        },
                        onCategoryClick = { viewModel.openCategory(it) },
                        onMoreClick = { viewModel.navigateTo(ScreenDestination.MORE_CATEGORIES) },
                        onOpenAllFiles = { viewModel.navigateTo(ScreenDestination.ALL_FILES) },
                        onOpenCleaner = { viewModel.navigateTo(ScreenDestination.STORAGE_CLEANER) },
                        onOpenFile = onOpenFile,
                        onSearchClick = {
                            viewModel.navigateTo(ScreenDestination.EXPLORER)
                        },
                        hasSdCard = (uiState.sdCardVolume?.totalBytes ?: 0L) > 0L,
                        isRefreshing = uiState.isLoading,
                        onRefresh = { viewModel.refreshAll() }
                    )
                }

                ScreenDestination.ALL_FILES -> {
                    AllFilesScreen(
                        storageStats = uiState.storageStats,
                        sdCardVolume = uiState.sdCardVolume,
                        onBack = { viewModel.navigateTo(ScreenDestination.HOME) },
                        onOpenInternalStorage = {
                            viewModel.openDirectory(FileManagerHelper.getStorageRoot())
                        },
                        onOpenSdCard = {
                            viewModel.openSdCard()
                        },
                        onSearchClick = {
                            viewModel.navigateTo(ScreenDestination.EXPLORER)
                        },
                        onRefresh = { viewModel.refreshAll() }
                    )
                }

                ScreenDestination.MORE_CATEGORIES -> {
                    MoreCategoriesScreen(
                        onBack = { viewModel.navigateTo(ScreenDestination.HOME) },
                        onCategoryClick = { viewModel.openCategory(it) },
                        onSearchClick = {
                            viewModel.navigateTo(ScreenDestination.EXPLORER)
                        },
                        onCollectionAddClick = {
                            viewModel.showCollectionAddDialog()
                        },
                        onOpenDirectory = { dir -> viewModel.openDirectory(dir) },
                        counts = uiState.categoryCounts,
                        whatsAppCount = uiState.whatsAppStatuses.size
                    )
                }

                ScreenDestination.SETTINGS -> {
                    SettingsScreen(
                        onBack = { viewModel.navigateTo(ScreenDestination.HOME) },
                        onFeedbackClick = { viewModel.showFeedbackDialog() },
                        onRateClick = { viewModel.showRateDialog() },
                        onAboutClick = { viewModel.showAboutDialog() },
                        onPrivacyPolicyClick = { viewModel.showPrivacyPolicyDialog() },
                        isDarkMode = uiState.isDarkMode,
                        onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                        isOnlyPortrait = uiState.isOnlyPortrait,
                        onToggleOrientationLock = { viewModel.toggleOrientationLock(it) }
                    )
                }

                ScreenDestination.EXPLORER -> {
                    ExplorerScreen(
                        currentDir = uiState.currentDirectory,
                        files = uiState.currentFiles,
                        selectedFiles = uiState.selectedFiles,
                        isSelectionMode = uiState.isSelectionMode,
                        clipboardFiles = uiState.clipboardFiles,
                        isCutOperation = uiState.isCutOperation,
                        sortMode = uiState.sortMode,
                        viewMode = uiState.viewMode,
                        searchQuery = uiState.searchQuery,
                        showHiddenFiles = uiState.showHiddenFiles,
                        onNavigateDir = { viewModel.openDirectory(it) },
                        onNavigateUp = { viewModel.navigateUp() },
                        onOpenFile = onOpenFile,
                        onToggleSelect = { viewModel.toggleFileSelection(it) },
                        onSelectAll = { viewModel.selectAllFiles() },
                        onClearSelection = { viewModel.clearSelection() },
                        onCopySelected = { viewModel.copySelected() },
                        onCutSelected = { viewModel.cutSelected() },
                        onDeleteSelected = { viewModel.showDeleteDialog(it) },
                        onShareSelected = { FileIntentHelper.shareFiles(context, it.map { item -> item.file }) },
                        onPaste = { viewModel.pasteClipboard() },
                        onClearClipboard = { viewModel.clearClipboard() },
                        onSetSortMode = { viewModel.setSortMode(it) },
                        onToggleViewMode = { viewModel.toggleViewMode() },
                        onSetSearchQuery = { viewModel.setSearchQuery(it) },
                        onToggleShowHiddenFiles = { viewModel.setShowHiddenFiles(it) },
                        onShowCreateFolder = { viewModel.showCreateFolderDialog() },
                        onShowCreateFile = { viewModel.showCreateFileDialog() },
                        onShowRename = { viewModel.showRenameDialog(it) },
                        onShowDetails = { viewModel.showFileDetails(it) },
                        onRefresh = { viewModel.loadCurrentDirectory() },
                        isLoading = uiState.isLoading,
                        sdCardRoot = uiState.sdCardVolume?.rootDir
                    )
                }

                ScreenDestination.WHATSAPP_STATUS -> {
                    WhatsAppStatusScreen(
                        statuses = uiState.whatsAppStatuses,
                        statusFilter = uiState.statusFilter,
                        isLoading = uiState.isLoading,
                        onFilterChange = { viewModel.setStatusFilter(it) },
                        onSaveStatus = { viewModel.saveWhatsAppStatus(it) },
                        onShareStatus = { FileIntentHelper.shareFile(context, it.file) },
                        onOpenStatus = onOpenFile,
                        onRefresh = { viewModel.loadWhatsAppStatuses() },
                        onBack = { viewModel.navigateTo(ScreenDestination.HOME) },
                        onOpenMediaFolder = { viewModel.openWhatsAppMediaDirectory() }
                    )
                }

                ScreenDestination.HIDDEN_FILES -> {
                    THideVaultScreen(
                        hiddenFiles = uiState.hiddenFiles,
                        isLoading = uiState.isLoading,
                        onBack = { viewModel.navigateTo(ScreenDestination.HOME) },
                        onOpenFile = onOpenFile,
                        onUnhideFile = { viewModel.unhideItem(it) },
                        onShareFile = { FileIntentHelper.shareFile(context, it.file) },
                        onDeleteFile = { viewModel.deleteVaultItem(it) },
                        onHideFiles = { viewModel.hideFilesToVault(it) },
                        onRefresh = { viewModel.loadHiddenFiles() }
                    )
                }

                ScreenDestination.CATEGORY_VIEW -> {
                    uiState.selectedCategory?.let { category ->
                        CategoryViewScreen(
                            category = category,
                            files = uiState.categoryFiles,
                            isLoading = uiState.isLoading,
                            onBack = {
                                if (uiState.isSelectionMode) {
                                    viewModel.clearSelection()
                                } else {
                                    viewModel.navigateTo(ScreenDestination.HOME)
                                }
                            },
                            onOpenFile = onOpenFile,
                            onOpenImage = { item, list -> viewModel.showImageViewer(item, list) },
                            onShowDetails = { viewModel.showFileDetails(it) },
                            onShareFile = { FileIntentHelper.shareFile(context, it.file) },
                            onRenameFile = { viewModel.showRenameDialog(it) },
                            onDeleteFile = { viewModel.showDeleteDialog(listOf(it)) },
                            onOpenDirectory = { dir ->
                                viewModel.openDirectoryFromCategory(dir, category)
                            },
                            onExtractZip = { zipItem -> viewModel.extractZip(zipItem) },
                            categoryFolders = uiState.categoryFolders,
                            isSelectionMode = uiState.isSelectionMode,
                            selectedFiles = uiState.selectedFiles,
                            onToggleSelect = { viewModel.toggleFileSelection(it) },
                            onSelectAll = { viewModel.selectAllFiles() },
                            onClearSelection = { viewModel.clearSelection() },
                            onCopySelected = { viewModel.copySelected() },
                            onCutSelected = { viewModel.cutSelected() },
                            onShareSelected = { files ->
                                FileIntentHelper.shareFiles(context, files.map { it.file })
                            },
                            onDeleteSelected = { files -> viewModel.showDeleteDialog(files) },
                            onRefresh = { viewModel.refreshCurrentCategory() }
                        )
                    }
                }

                ScreenDestination.STORAGE_CLEANER -> {
                    StorageCleanScreen(
                        junkCleanResult = uiState.junkCleanResult,
                        isCleaning = uiState.isCleaning,
                        isCleanComplete = uiState.isCleanComplete,
                        cleanedBytesFormatted = uiState.cleanedBytesFormatted,
                        isLoading = uiState.isLoading,
                        largeFiles = if (uiState.largeFiles.isNotEmpty()) uiState.largeFiles else (uiState.junkCleanResult?.largeFiles ?: emptyList()),
                        onBack = { viewModel.navigateTo(ScreenDestination.HOME) },
                        onCleanJunk = { includeApks -> viewModel.cleanStorageJunk(includeApks) },
                        onOpenFile = onOpenFile,
                        onDeleteLargeFile = { viewModel.showDeleteDialog(listOf(it)) },
                        onRefresh = { viewModel.scanStorageJunk() }
                    )
                }
            }

            // Interactive Dialogs
            when (val dialog = uiState.activeDialog) {
                is ActiveDialog.CreateFolder -> {
                    CreateFolderDialog(
                        onDismiss = { viewModel.dismissDialog() },
                        onConfirm = { name -> viewModel.confirmCreateFolder(name) }
                    )
                }
                is ActiveDialog.CreateFile -> {
                    CreateFileDialog(
                        onDismiss = { viewModel.dismissDialog() },
                        onConfirm = { name, content -> viewModel.confirmCreateFile(name, content) }
                    )
                }
                is ActiveDialog.Rename -> {
                    RenameDialog(
                        item = dialog.item,
                        onDismiss = { viewModel.dismissDialog() },
                        onConfirm = { newName -> viewModel.confirmRename(dialog.item, newName) }
                    )
                }
                is ActiveDialog.DeleteConfirm -> {
                    DeleteConfirmDialog(
                        items = dialog.items,
                        onDismiss = { viewModel.dismissDialog() },
                        onConfirm = {
                            val activity = context as? android.app.Activity
                            if (dialog.items.size >= 2 && activity != null) {
                                com.example.util.StartIoManager.showRewardedAd(activity) {
                                    viewModel.confirmDelete(dialog.items)
                                }
                            } else {
                                viewModel.confirmDelete(dialog.items)
                            }
                        }
                    )
                }
                is ActiveDialog.FileDetails -> {
                    FileDetailsDialog(
                        item = dialog.item,
                        onDismiss = { viewModel.dismissDialog() }
                    )
                }
                is ActiveDialog.ImageViewer -> {
                    ImageViewerDialog(
                        item = dialog.item,
                        items = dialog.items,
                        onDismiss = { viewModel.dismissDialog() },
                        onShare = { item -> FileIntentHelper.shareFile(context, item.file) }
                    )
                }
                is ActiveDialog.TextViewer -> {
                    TextViewerDialog(
                        item = dialog.item,
                        content = dialog.content,
                        onDismiss = { viewModel.dismissDialog() }
                    )
                }
                is ActiveDialog.FeedbackDialog -> {
                    FeedbackDialog(
                        onDismiss = { viewModel.dismissDialog() },
                        onSubmit = { viewModel.showToast("Feedback sent! Thank you.") }
                    )
                }
                is ActiveDialog.RateAppDialog -> {
                    RateAppDialog(
                        onDismiss = { viewModel.dismissDialog() },
                        onSubmitRating = { stars -> viewModel.showToast("Thank you for your $stars-star rating!") }
                    )
                }
                is ActiveDialog.AboutDialog -> {
                    AboutDialog(
                        onDismiss = { viewModel.dismissDialog() },
                        onViewPrivacyPolicy = { viewModel.showPrivacyPolicyDialog() }
                    )
                }
                is ActiveDialog.PrivacyPolicyDialog -> {
                    PrivacyPolicyDialog(
                        onDismiss = { viewModel.dismissDialog() }
                    )
                }
                is ActiveDialog.CollectionAddDialog -> {
                    CollectionAddDialog(
                        onDismiss = { viewModel.dismissDialog() },
                        onAddCollection = { name ->
                            val folder = com.example.util.DocumentCollectionsManager.createCollection(name)
                            viewModel.dismissDialog()
                            if (folder != null) {
                                viewModel.showToast("Collection '$name' created in Documents/Collections")
                            } else {
                                viewModel.showToast("Please enter a valid collection name")
                            }
                        }
                    )
                }
                null -> {}
            }
        }
    }
}
}
