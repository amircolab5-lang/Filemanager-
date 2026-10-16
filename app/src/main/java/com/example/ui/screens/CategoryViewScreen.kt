package com.example.ui.screens

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.platform.LocalConfiguration
import com.example.ui.components.tvListScrollbar
import com.example.ui.components.tvGridScrollbar
import com.example.ui.components.PullToRefreshLayout
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CategoryType
import com.example.model.FileItem
import com.example.model.ViewMode
import com.example.ui.components.AdMobNativeAdCard
import com.example.ui.components.StartIoNativeAdCard
import com.example.ui.components.FileGridItem
import com.example.ui.components.FileListItem
import com.example.ui.components.FolderCard
import com.example.ui.theme.LeagueSpartanFontFamily
import com.example.ui.theme.TicnoRose
import java.io.File

/**
 * Category View Screen inspired by Google Files.
 * - Pictures/Videos: Filter by album/folders (Camera, Screenshots, Download, WhatsApp, etc.)
 * - Downloads: Shows folders first at the top, then regular files below
 * - Archives: Direct ZIP extraction support
 * - Clean, professional header with no clutter
 */
@Composable
fun CategoryViewScreen(
    category: CategoryType,
    files: List<FileItem>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onOpenFile: (FileItem) -> Unit,
    onShowDetails: (FileItem) -> Unit,
    onShareFile: (FileItem) -> Unit,
    onRenameFile: (FileItem) -> Unit,
    onDeleteFile: (FileItem) -> Unit,
    onOpenDirectory: (File) -> Unit = {},
    onExtractZip: (FileItem) -> Unit = {},
    categoryFolders: List<FileItem> = emptyList(),
    isSelectionMode: Boolean = false,
    selectedFiles: Set<FileItem> = emptySet(),
    onToggleSelect: (FileItem) -> Unit = {},
    onSelectAll: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    onCopySelected: () -> Unit = {},
    onCutSelected: () -> Unit = {},
    onShareSelected: (List<FileItem>) -> Unit = {},
    onDeleteSelected: (List<FileItem>) -> Unit = {},
    onRefresh: () -> Unit = {},
    onOpenImage: ((FileItem, List<FileItem>) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    var viewMode by remember {
        mutableStateOf(
            if (category == CategoryType.IMAGES || category == CategoryType.VIDEOS || category == CategoryType.DOWNLOADS) ViewMode.GRID
            else ViewMode.LIST
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    var selectedFolderFilter by remember { mutableStateOf("All") }
    var selectedSubtypeFilter by remember { mutableStateOf("All") }

    // Folder options for images & videos - prioritize Screenshots folder right after "All"
    val folderOptions = remember(files, category) {
        if (category == CategoryType.IMAGES || category == CategoryType.VIDEOS) {
            val names = files.mapNotNull {
                it.bucketName ?: it.file.parentFile?.name
            }.filter { it.isNotBlank() }.distinct().sortedWith(
                compareByDescending<String> {
                    it.contains("screenshot", ignoreCase = true)
                }.thenBy { it.lowercase() }
            )
            listOf("All") + names
        } else {
            emptyList()
        }
    }

    // Dynamic document subtype filters - ONLY show document formats that actually exist on the device!
    val documentFilters = remember(files, category) {
        if (category == CategoryType.DOCUMENTS) {
            val exts = files.map { it.file.extension.lowercase() }.toSet()
            val available = mutableListOf<String>()

            if (exts.any { it == "pdf" }) available.add("PDF")
            if (exts.any { it in listOf("txt", "text", "log", "md") }) available.add("Text")
            if (exts.any { it in listOf("html", "htm") }) available.add("HTML")
            if (exts.any { it in listOf("doc", "docx") }) available.add("Word")
            if (exts.any { it in listOf("xls", "xlsx", "csv") }) available.add("Excel")
            if (exts.any { it in listOf("ppt", "pptx") }) available.add("PowerPoint")
            if (exts.any { it in listOf("epub", "mobi") }) available.add("eBook")
            if (exts.any { it in listOf("xml", "json") }) available.add("Code")

            val standardExts = setOf("pdf", "txt", "text", "log", "md", "html", "htm", "doc", "docx", "xls", "xlsx", "csv", "ppt", "pptx", "epub", "mobi", "xml", "json")
            val otherExts = exts.filter { it.isNotBlank() && it !in standardExts }
            for (ext in otherExts.take(2)) {
                available.add(ext.uppercase())
            }

            if (available.isNotEmpty()) {
                listOf("All") + available
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // App & APK subtype filters (e.g. Installed, Not Installed)
    val apkFilters = remember(files, category) {
        if (category == CategoryType.APKS && files.isNotEmpty()) {
            val hasInstalled = files.any { it.isInstalledApk }
            val hasNotInstalled = files.any { !it.isInstalledApk }
            if (hasInstalled && hasNotInstalled) {
                listOf("All", "Installed", "Not Installed")
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // Filter files based on user selection and search query
    val displayedFiles = remember(files, category, selectedFolderFilter, selectedSubtypeFilter, searchQuery) {
        val baseFiltered = when {
            (category == CategoryType.IMAGES || category == CategoryType.VIDEOS) && selectedFolderFilter != "All" -> {
                files.filter { (it.bucketName ?: it.file.parentFile?.name) == selectedFolderFilter }
            }
            category == CategoryType.DOCUMENTS && selectedSubtypeFilter != "All" -> {
                files.filter { item ->
                    val ext = item.file.extension.lowercase()
                    when (selectedSubtypeFilter) {
                        "PDF" -> ext == "pdf"
                        "Text" -> ext in listOf("txt", "text", "log", "md")
                        "HTML" -> ext in listOf("html", "htm")
                        "Word" -> ext in listOf("doc", "docx")
                        "Excel" -> ext in listOf("xls", "xlsx", "csv")
                        "PowerPoint" -> ext in listOf("ppt", "pptx")
                        "eBook" -> ext in listOf("epub", "mobi")
                        "Code" -> ext in listOf("xml", "json")
                        else -> ext.equals(selectedSubtypeFilter, ignoreCase = true)
                    }
                }
            }
            category == CategoryType.APKS && selectedSubtypeFilter != "All" -> {
                files.filter { item ->
                    if (selectedSubtypeFilter == "Installed" || selectedSubtypeFilter == "Apps") item.isInstalledApk
                    else !item.isInstalledApk
                }
            }
            else -> files
        }

        if (searchQuery.isNotBlank()) {
            baseFiltered.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
        } else {
            baseFiltered
        }
    }

    // In Downloads and Images, partition into folders and regular files
    val isFolderFirstCategory = category == CategoryType.DOWNLOADS || category == CategoryType.IMAGES
    val isDownloads = category == CategoryType.DOWNLOADS
    val isImages = category == CategoryType.IMAGES

    // For images, dynamically discover all album/bucket folders from images files (precomputed on background dispatcher)
    val imageFolders = remember(categoryFolders, files, isImages) {
        if (isImages) {
            val list = if (categoryFolders.isNotEmpty()) {
                categoryFolders
            } else {
                val bucketGroups = files.filter { !it.isDirectory }.groupBy { it.bucketName.ifBlank { it.file.parentFile?.name ?: "Other" } }
                bucketGroups.map { (bucketName, bucketFiles) ->
                    val sortedBucketFiles = bucketFiles.sortedByDescending { it.lastModified }
                    val firstFile = sortedBucketFiles.first()
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
                }
            }
            list.sortedWith(
                compareByDescending<FileItem> {
                    it.name.contains("screenshot", ignoreCase = true)
                }.thenByDescending {
                    it.lastModified
                }
            )
        } else emptyList()
    }

    val displayFolders = remember(displayedFiles, isDownloads, isImages, imageFolders, selectedFolderFilter) {
        when {
            isDownloads -> emptyList() // Downloads displayed in single uniform list/grid sorted newest first
            isImages && selectedFolderFilter == "All" -> imageFolders
            else -> emptyList()
        }
    }

    val displayContentFiles = remember(displayedFiles, isDownloads, isImages, selectedFolderFilter) {
        when {
            isDownloads -> displayedFiles.sortedByDescending { it.lastModified }
            isImages && selectedFolderFilter == "All" -> emptyList() // When at top level of Images, show folders only as user requested!
            isImages -> displayedFiles.sortedByDescending { it.lastModified } // In image folder, newest first!
            else -> displayedFiles
        }
    }

    // Android back press handler: returns from opened folder back to all folders!
    BackHandler(enabled = isImages && selectedFolderFilter != "All") {
        selectedFolderFilter = "All"
    }

    val handleOpenFile: (FileItem) -> Unit = { item ->
        if (item.isImage && onOpenImage != null) {
            val imageList = (if (isFolderFirstCategory) displayContentFiles else displayedFiles).filter { it.isImage }
            onOpenImage(item, imageList)
        } else {
            onOpenFile(item)
        }
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (isSelectionMode) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = if (isLandscape) 4.dp else 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onClearSelection) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel Selection"
                            )
                        }
                        Text(
                            text = "${selectedFiles.size} selected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onSelectAll) {
                            Icon(
                                imageVector = Icons.Default.SelectAll,
                                contentDescription = "Select All"
                            )
                        }
                        IconButton(onClick = onCopySelected) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy"
                            )
                        }
                        IconButton(onClick = onCutSelected) {
                            Icon(
                                imageVector = Icons.Default.ContentCut,
                                contentDescription = "Move"
                            )
                        }
                        IconButton(onClick = { onShareSelected(selectedFiles.toList()) }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share"
                            )
                        }
                        IconButton(onClick = { onDeleteSelected(selectedFiles.toList()) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = TicnoRose
                            )
                        }
                    }
                }
            }
        } else {
            // Professional Top Bar matching reference screenshot (Search, Grid toggle, 3-dots menu)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                if (isSearchActive) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = if (isLandscape) 4.dp else 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            isSearchActive = false
                            searchQuery = ""
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Close Search"
                            )
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Search in ${category.title}...", fontSize = 14.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )

                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Search"
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .displayCutoutPadding()
                            .padding(horizontal = if (isLandscape) 12.dp else 8.dp, vertical = if (isLandscape) 4.dp else 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (isImages && selectedFolderFilter != "All") {
                                        selectedFolderFilter = "All"
                                    } else {
                                        onBack()
                                    }
                                },
                                modifier = Modifier.size(if (isLandscape) 44.dp else 48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = if (isImages && selectedFolderFilter != "All") selectedFolderFilter else category.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isLandscape) 15.sp else 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { isSearchActive = true },
                                modifier = if (isLandscape) Modifier.size(32.dp) else Modifier
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    modifier = Modifier.size(if (isLandscape) 18.dp else 24.dp)
                                )
                            }

                            // Dedicated Refresh Button for TV Remote and fast click
                            IconButton(
                                onClick = onRefresh,
                                modifier = if (isLandscape) Modifier.size(32.dp) else Modifier
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Files",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(if (isLandscape) 18.dp else 24.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    viewMode = if (viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST
                                },
                                modifier = if (isLandscape) Modifier.size(32.dp) else Modifier
                            ) {
                                Icon(
                                    imageVector = if (viewMode == ViewMode.LIST) Icons.Default.GridView else Icons.Default.ViewList,
                                    contentDescription = "Toggle Grid/List",
                                    modifier = Modifier.size(if (isLandscape) 18.dp else 24.dp)
                                )
                            }

                            Box {
                                IconButton(
                                    onClick = { showMenu = !showMenu },
                                    modifier = if (isLandscape) Modifier.size(32.dp) else Modifier
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More Options",
                                        modifier = Modifier.size(if (isLandscape) 18.dp else 24.dp)
                                    )
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Refresh") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            showMenu = false
                                            onRefresh()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Select all") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.SelectAll,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            showMenu = false
                                            onSelectAll()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (viewMode == ViewMode.GRID) "List view" else "Grid view") },
                                        onClick = {
                                            showMenu = false
                                            viewMode = if (viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Search") },
                                        onClick = {
                                            showMenu = false
                                            isSearchActive = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Folder Chips for Images / Videos (Google Files style)
        if (folderOptions.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = if (isLandscape) 2.dp else 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                folderOptions.forEach { folderName ->
                    val isSelected = selectedFolderFilter == folderName
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFolderFilter = folderName },
                        modifier = Modifier.height(if (isLandscape) 26.dp else 32.dp),
                        label = {
                            Text(
                                text = folderName,
                                fontSize = if (isLandscape) 11.sp else 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        // Document Type Chips (PDF, Word, Excel, etc.)
        if (documentFilters.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = if (isLandscape) 2.dp else 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                documentFilters.forEach { filterName ->
                    val isSelected = selectedSubtypeFilter == filterName
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSubtypeFilter = filterName },
                        modifier = Modifier.height(if (isLandscape) 26.dp else 32.dp),
                        label = {
                            Text(
                                text = filterName,
                                fontSize = if (isLandscape) 11.sp else 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        // App & APK Type Chips (All, Apps, APK Files)
        if (apkFilters.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = if (isLandscape) 2.dp else 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                apkFilters.forEach { filterName ->
                    val isSelected = selectedSubtypeFilter == filterName
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSubtypeFilter = filterName },
                        modifier = Modifier.height(if (isLandscape) 26.dp else 32.dp),
                        label = {
                            Text(
                                text = filterName,
                                fontSize = if (isLandscape) 11.sp else 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        PullToRefreshLayout(
            isRefreshing = isLoading,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            if (isLoading && displayedFiles.isEmpty() && displayFolders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (displayedFiles.isEmpty() && displayFolders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No files found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No ${category.title.lowercase()} found in this location.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        AdMobNativeAdCard(modifier = Modifier.fillMaxWidth())
                    }
                }
            } else if (viewMode == ViewMode.LIST) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .tvListScrollbar(listState),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(if (isLandscape) 4.dp else 8.dp)
                ) {
                    if (isLoading) {
                        item(key = "loading_top_indicator_col") {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .padding(bottom = 2.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    // Show Folders section in full-width list cards (portrait) or 2 columns (landscape)
                    if (displayFolders.isNotEmpty()) {
                        if (isLandscape) {
                            items(displayFolders.chunked(2), key = { pair -> pair.first().path }) { rowFolders ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    for (folder in rowFolders) {
                                        FolderCard(
                                            folderItem = folder,
                                            onClick = {
                                                if (isImages) {
                                                    selectedFolderFilter = folder.name
                                                } else {
                                                    onOpenDirectory(folder.file)
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (rowFolders.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        } else {
                            items(displayFolders, key = { it.path }) { folder ->
                                FolderCard(
                                    folderItem = folder,
                                    onClick = {
                                        if (isImages) {
                                            selectedFolderFilter = folder.name
                                        } else {
                                            onOpenDirectory(folder.file)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    val listItems = if (isFolderFirstCategory) displayContentFiles else displayedFiles

                    if (!isLandscape) {
                        if (listItems.size > 4) {
                            items(listItems.take(4), key = { it.path }) { item ->
                                val isItemSelected = selectedFiles.any { it.path == item.path }
                                FileListItem(
                                    item = item,
                                    isSelected = isItemSelected,
                                    isSelectionMode = isSelectionMode,
                                    onClick = {
                                        if (isSelectionMode) {
                                            onToggleSelect(item)
                                        } else if (item.isDirectory) {
                                            onOpenDirectory(item.file)
                                        } else if (item.isArchive) {
                                            onExtractZip(item)
                                        } else {
                                            handleOpenFile(item)
                                        }
                                    },
                                    onLongClick = { onToggleSelect(item) },
                                    onToggleSelect = { onToggleSelect(item) },
                                    onOpenDetails = { onShowDetails(item) },
                                    onShare = { onShareFile(item) },
                                    onRename = { onRenameFile(item) },
                                    onDelete = { onDeleteFile(item) },
                                    onExtract = if (item.isArchive) { { onExtractZip(item) } } else null
                                )
                            }
                            item {
                                StartIoNativeAdCard(modifier = Modifier.padding(vertical = 4.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                AdMobNativeAdCard(modifier = Modifier.padding(vertical = 4.dp))
                            }
                            items(listItems.drop(4), key = { it.path }) { item ->
                                val isItemSelected = selectedFiles.any { it.path == item.path }
                                FileListItem(
                                    item = item,
                                    isSelected = isItemSelected,
                                    isSelectionMode = isSelectionMode,
                                    onClick = {
                                        if (isSelectionMode) {
                                            onToggleSelect(item)
                                        } else if (item.isDirectory) {
                                            onOpenDirectory(item.file)
                                        } else if (item.isArchive) {
                                            onExtractZip(item)
                                        } else {
                                            handleOpenFile(item)
                                        }
                                    },
                                    onLongClick = { onToggleSelect(item) },
                                    onToggleSelect = { onToggleSelect(item) },
                                    onOpenDetails = { onShowDetails(item) },
                                    onShare = { onShareFile(item) },
                                    onRename = { onRenameFile(item) },
                                    onDelete = { onDeleteFile(item) },
                                    onExtract = if (item.isArchive) { { onExtractZip(item) } } else null
                                )
                            }
                        } else {
                            items(listItems, key = { it.path }) { item ->
                                val isItemSelected = selectedFiles.any { it.path == item.path }
                                FileListItem(
                                    item = item,
                                    isSelected = isItemSelected,
                                    isSelectionMode = isSelectionMode,
                                    onClick = {
                                        if (isSelectionMode) {
                                            onToggleSelect(item)
                                        } else if (item.isDirectory) {
                                            onOpenDirectory(item.file)
                                        } else if (item.isArchive) {
                                            onExtractZip(item)
                                        } else {
                                            handleOpenFile(item)
                                        }
                                    },
                                    onLongClick = { onToggleSelect(item) },
                                    onToggleSelect = { onToggleSelect(item) },
                                    onOpenDetails = { onShowDetails(item) },
                                    onShare = { onShareFile(item) },
                                    onRename = { onRenameFile(item) },
                                    onDelete = { onDeleteFile(item) },
                                    onExtract = if (item.isArchive) { { onExtractZip(item) } } else null
                                )
                            }
                            if (listItems.isNotEmpty()) {
                                item {
                                    AdMobNativeAdCard(modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    } else {
                        items(listItems, key = { it.path }) { item ->
                            val isItemSelected = selectedFiles.any { it.path == item.path }
                            FileListItem(
                                item = item,
                                isSelected = isItemSelected,
                                isSelectionMode = isSelectionMode,
                                onClick = {
                                    if (isSelectionMode) {
                                        onToggleSelect(item)
                                    } else if (item.isDirectory) {
                                        onOpenDirectory(item.file)
                                    } else if (item.isArchive) {
                                        onExtractZip(item)
                                    } else {
                                        handleOpenFile(item)
                                    }
                                },
                                onLongClick = { onToggleSelect(item) },
                                onToggleSelect = { onToggleSelect(item) },
                                onOpenDetails = { onShowDetails(item) },
                                onShare = { onShareFile(item) },
                                onRename = { onRenameFile(item) },
                                onDelete = { onDeleteFile(item) },
                                onExtract = if (item.isArchive) { { onExtractZip(item) } } else null
                            )
                        }
                        if (listItems.isNotEmpty()) {
                            item {
                                StartIoNativeAdCard(modifier = Modifier.padding(vertical = 4.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                AdMobNativeAdCard(modifier = Modifier.padding(vertical = 6.dp))
                            }
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Adaptive(minSize = if (isLandscape) 180.dp else 145.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .tvGridScrollbar(gridState),
                    contentPadding = PaddingValues(
                        start = if (isLandscape) 8.dp else 12.dp,
                        end = if (isLandscape) 8.dp else 12.dp,
                        top = 2.dp,
                        bottom = if (isLandscape) 32.dp else 88.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 6.dp else 8.dp),
                    verticalArrangement = Arrangement.spacedBy(if (isLandscape) 6.dp else 8.dp)
                ) {
                    if (isLoading) {
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }, key = "loading_top_indicator_grid") {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .padding(bottom = 2.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    // Show Folders section in grid at the top
                    if (displayFolders.isNotEmpty()) {
                        items(displayFolders, key = { it.path }) { folderItem ->
                            FolderCard(
                                folderItem = folderItem,
                                onClick = {
                                    if (isImages) {
                                        selectedFolderFilter = folderItem.name
                                    } else {
                                        onOpenDirectory(folderItem.file)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    val gridItems = if (isFolderFirstCategory) displayContentFiles else displayedFiles

                    if (!isLandscape) {
                        if (gridItems.size > 6) {
                            items(gridItems.take(6), key = { it.path }) { item ->
                                val isItemSelected = selectedFiles.any { it.path == item.path }
                                FileGridItem(
                                    item = item,
                                    isSelected = isItemSelected,
                                    isSelectionMode = isSelectionMode,
                                    onClick = {
                                        if (isSelectionMode) {
                                            onToggleSelect(item)
                                        } else if (item.isDirectory) {
                                            onOpenDirectory(item.file)
                                        } else if (item.isArchive) {
                                            onExtractZip(item)
                                        } else {
                                            handleOpenFile(item)
                                        }
                                    },
                                    onLongClick = { onToggleSelect(item) },
                                    onToggleSelect = { onToggleSelect(item) },
                                    onRename = { onRenameFile(item) },
                                    onDelete = { onDeleteFile(item) },
                                    onShare = { onShareFile(item) },
                                    onOpenDetails = { onShowDetails(item) },
                                    onExtract = if (item.isArchive) { { onExtractZip(item) } } else null
                                )
                            }
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                AdMobNativeAdCard(modifier = Modifier.padding(vertical = 4.dp))
                            }
                            items(gridItems.drop(6), key = { it.path }) { item ->
                                val isItemSelected = selectedFiles.any { it.path == item.path }
                                FileGridItem(
                                    item = item,
                                    isSelected = isItemSelected,
                                    isSelectionMode = isSelectionMode,
                                    onClick = {
                                        if (isSelectionMode) {
                                            onToggleSelect(item)
                                        } else if (item.isDirectory) {
                                            onOpenDirectory(item.file)
                                        } else if (item.isArchive) {
                                            onExtractZip(item)
                                        } else {
                                            handleOpenFile(item)
                                        }
                                    },
                                    onLongClick = { onToggleSelect(item) },
                                    onToggleSelect = { onToggleSelect(item) },
                                    onRename = { onRenameFile(item) },
                                    onDelete = { onDeleteFile(item) },
                                    onShare = { onShareFile(item) },
                                    onOpenDetails = { onShowDetails(item) },
                                    onExtract = if (item.isArchive) { { onExtractZip(item) } } else null
                                )
                            }
                        } else {
                            items(gridItems, key = { it.path }) { item ->
                                val isItemSelected = selectedFiles.any { it.path == item.path }
                                FileGridItem(
                                    item = item,
                                    isSelected = isItemSelected,
                                    isSelectionMode = isSelectionMode,
                                    onClick = {
                                        if (isSelectionMode) {
                                            onToggleSelect(item)
                                        } else if (item.isDirectory) {
                                            onOpenDirectory(item.file)
                                        } else if (item.isArchive) {
                                            onExtractZip(item)
                                        } else {
                                            handleOpenFile(item)
                                        }
                                    },
                                    onLongClick = { onToggleSelect(item) },
                                    onToggleSelect = { onToggleSelect(item) },
                                    onRename = { onRenameFile(item) },
                                    onDelete = { onDeleteFile(item) },
                                    onShare = { onShareFile(item) },
                                    onOpenDetails = { onShowDetails(item) },
                                    onExtract = if (item.isArchive) { { onExtractZip(item) } } else null
                                )
                            }
                            if (gridItems.isNotEmpty()) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    AdMobNativeAdCard(modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    } else {
                        items(gridItems, key = { it.path }) { item ->
                            val isItemSelected = selectedFiles.any { it.path == item.path }
                            FileGridItem(
                                item = item,
                                isSelected = isItemSelected,
                                isSelectionMode = isSelectionMode,
                                onClick = {
                                    if (isSelectionMode) {
                                        onToggleSelect(item)
                                    } else if (item.isDirectory) {
                                        onOpenDirectory(item.file)
                                    } else if (item.isArchive) {
                                        onExtractZip(item)
                                    } else {
                                        handleOpenFile(item)
                                    }
                                },
                                onLongClick = { onToggleSelect(item) },
                                onToggleSelect = { onToggleSelect(item) },
                                onRename = { onRenameFile(item) },
                                onDelete = { onDeleteFile(item) },
                                onShare = { onShareFile(item) },
                                onOpenDetails = { onShowDetails(item) },
                                onExtract = if (item.isArchive) { { onExtractZip(item) } } else null
                            )
                        }

                        // In landscape mode, show ad card cleanly at the end so user's thumbnails are always visible first!
                        if (gridItems.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                AdMobNativeAdCard(
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
