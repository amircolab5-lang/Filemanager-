package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FileItem
import com.example.model.SortMode
import com.example.model.ViewMode
import com.example.ui.components.BreadcrumbBar
import com.example.ui.components.FileGridItem
import com.example.ui.components.FileListItem
import com.example.ui.components.FolderCard
import com.example.ui.theme.LeagueSpartanFontFamily
import com.example.ui.theme.TicnoNeonGreen
import com.example.ui.theme.TicnoRose
import com.example.util.FileManagerHelper
import java.io.File

/**
 * File Explorer Screen.
 * Fully matches user request:
 * - Top header with Back arrow, Folder name ("Home" or folder name), Search, and 3-dots menu
 * - 3-dots menu with: "Create folder", "Show" (Show or hide files dialog), "Sort by"
 * - "Show or hide files" dialog matching Screenshot 4:
 *   (•) Show hidden files
 *   ( ) Hide files
 *   CANCEL | OK
 * - Selection mode toolbar
 * - Breadcrumb navigation
 * - List & Grid view modes
 */
@Composable
fun ExplorerScreen(
    currentDir: File,
    files: List<FileItem>,
    selectedFiles: Set<FileItem>,
    isSelectionMode: Boolean,
    clipboardFiles: List<File>,
    isCutOperation: Boolean,
    sortMode: SortMode,
    viewMode: ViewMode,
    searchQuery: String,
    showHiddenFiles: Boolean,
    onNavigateDir: (File) -> Unit,
    onNavigateUp: () -> Unit,
    onOpenFile: (FileItem) -> Unit,
    onToggleSelect: (FileItem) -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onCopySelected: () -> Unit,
    onCutSelected: () -> Unit,
    onDeleteSelected: (List<FileItem>) -> Unit,
    onShareSelected: (List<FileItem>) -> Unit,
    onPaste: () -> Unit,
    onClearClipboard: () -> Unit,
    onSetSortMode: (SortMode) -> Unit,
    onToggleViewMode: () -> Unit,
    onSetSearchQuery: (String) -> Unit,
    onToggleShowHiddenFiles: (Boolean) -> Unit,
    onShowCreateFolder: () -> Unit,
    onShowCreateFile: () -> Unit,
    onShowRename: (FileItem) -> Unit,
    onShowDetails: (FileItem) -> Unit,
    modifier: Modifier = Modifier,
    sdCardRoot: File? = null
) {
    var showMoreMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showHiddenFilesDialog by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }

    val storageRoot = remember { FileManagerHelper.getStorageRoot() }
    val isAtRoot = remember(currentDir) { currentDir.absolutePath == storageRoot.absolutePath }
    val isAtSdRoot = remember(currentDir, sdCardRoot) {
        sdCardRoot != null && currentDir.absolutePath == sdCardRoot.absolutePath
    }
    val folderTitle = when {
        isAtRoot -> "Internal Storage"
        isAtSdRoot -> "SD Card"
        else -> currentDir.name
    }

    val filteredFiles = remember(files, searchQuery) {
        if (searchQuery.isBlank()) files
        else files.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Selection Mode Header OR Professional Top App Bar (Screenshot 5)
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
                            .padding(horizontal = 8.dp, vertical = 6.dp),
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
                // Top Bar matching Screenshot 5
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            IconButton(
                                onClick = onNavigateUp,
                                modifier = Modifier.testTag("explorer_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = folderTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { isSearchActive = !isSearchActive },
                                modifier = Modifier.testTag("explorer_search_toggle")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(onClick = onToggleViewMode) {
                                Icon(
                                    imageVector = if (viewMode == ViewMode.LIST) Icons.Default.GridView else Icons.Default.ViewList,
                                    contentDescription = "Toggle View",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // 3-Dots Overflow Menu (Screenshot 5)
                            Box {
                                IconButton(
                                    onClick = { showMoreMenu = true },
                                    modifier = Modifier.testTag("explorer_more_menu_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More Options",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                DropdownMenu(
                                    expanded = showMoreMenu,
                                    onDismissRequest = { showMoreMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Create folder") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.CreateNewFolder,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            showMoreMenu = false
                                            onShowCreateFolder()
                                        },
                                        modifier = Modifier.testTag("menu_create_folder")
                                    )

                                    DropdownMenuItem(
                                        text = { Text("Show") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Visibility,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            showMoreMenu = false
                                            showHiddenFilesDialog = true
                                        },
                                        modifier = Modifier.testTag("menu_show_hidden")
                                    )

                                    DropdownMenuItem(
                                        text = { Text("Sort by") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Sort,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            showMoreMenu = false
                                            showSortMenu = true
                                        },
                                        modifier = Modifier.testTag("menu_sort_by")
                                    )
                                }
                            }
                        }
                    }
                }

                // Breadcrumbs bar
                BreadcrumbBar(
                    currentDir = currentDir,
                    storageRoot = storageRoot,
                    sdCardRoot = sdCardRoot,
                    onNavigateDir = onNavigateDir,
                    onNavigateUp = onNavigateUp
                )
            }

            // Search Bar if active
            if (isSearchActive) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSetSearchQuery,
                        placeholder = { Text("Search files in $folderTitle...") },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                onSetSearchQuery("")
                                isSearchActive = false
                            }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear Search")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("explorer_search_field")
                    )
                }
            }

            // Items Counter and Hidden Status info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${filteredFiles.size} items",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (showHiddenFiles) {
                    Text(
                        text = "Hidden files: ON",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Clipboard Paste Bar if files copied/cut
            if (clipboardFiles.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = TicnoNeonGreen.copy(alpha = 0.15f),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = null,
                                tint = TicnoNeonGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${if (isCutOperation) "Move" else "Copy"} ${clipboardFiles.size} items here",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = TicnoNeonGreen
                            )
                        }

                        Row {
                            IconButton(onClick = onPaste) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "Paste Here",
                                    tint = TicnoNeonGreen
                                )
                            }
                            IconButton(onClick = onClearClipboard) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel Clipboard",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // File Listing
            if (filteredFiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "EMPTY DIRECTORY",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No files match '$searchQuery'" else "No files found in this folder.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (viewMode == ViewMode.LIST) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(filteredFiles, key = { it.path }) { item ->
                        FileListItem(
                            item = item,
                            isSelected = selectedFiles.contains(item),
                            isSelectionMode = isSelectionMode,
                            onClick = {
                                if (item.isDirectory) onNavigateDir(item.file)
                                else onOpenFile(item)
                            },
                            onLongClick = { onToggleSelect(item) },
                            onToggleSelect = { onToggleSelect(item) },
                            onOpenDetails = { onShowDetails(item) },
                            onShare = { onShareSelected(listOf(item)) },
                            onRename = { onShowRename(item) },
                            onDelete = { onDeleteSelected(listOf(item)) }
                        )
                    }
                }
            } else {
                val (explorerFolders, explorerNonFolders) = remember(filteredFiles) {
                    filteredFiles.partition { it.isDirectory }
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 88.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Folders displayed at the top in 2 columns (matching reference screenshot)
                    if (explorerFolders.isNotEmpty()) {
                        items(explorerFolders, key = { "folder_${it.path}" }) { folderItem ->
                            FolderCard(
                                folderItem = folderItem,
                                isSelected = selectedFiles.contains(folderItem),
                                isSelectionMode = isSelectionMode,
                                onClick = {
                                    if (isSelectionMode) onToggleSelect(folderItem)
                                    else onNavigateDir(folderItem.file)
                                },
                                onLongClick = { onToggleSelect(folderItem) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // If odd number of folders, insert empty spacer with a stable unique key so files start cleanly on next row without crashing
                        if (explorerFolders.size % 2 != 0) {
                            item(key = "explorer_odd_folder_spacer", contentType = "spacer") {
                                Spacer(modifier = Modifier.fillMaxWidth().height(48.dp))
                            }
                        }
                    }

                    // Files displayed in 2 columns below folders
                    items(explorerNonFolders, key = { "file_${it.path}" }) { item ->
                        FileGridItem(
                            item = item,
                            isSelected = selectedFiles.contains(item),
                            isSelectionMode = isSelectionMode,
                            onClick = {
                                if (item.isDirectory) onNavigateDir(item.file)
                                else onOpenFile(item)
                            },
                            onLongClick = { onToggleSelect(item) },
                            onToggleSelect = { onToggleSelect(item) },
                            onRename = { onShowRename(item) },
                            onDelete = { onDeleteSelected(listOf(item)) },
                            onShare = { onShareSelected(listOf(item)) },
                            onOpenDetails = { onShowDetails(item) }
                        )
                    }
                }
            }
        }

        // FAB for New Item (Folder / File)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                if (showFabMenu) {
                    FloatingActionButton(
                        onClick = {
                            showFabMenu = false
                            onShowCreateFolder()
                        },
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                            .testTag("fab_create_folder"),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = "Create Folder",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    FloatingActionButton(
                        onClick = {
                            showFabMenu = false
                            onShowCreateFile()
                        },
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                            .testTag("fab_create_file"),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Icon(
                            imageVector = Icons.Default.NoteAdd,
                            contentDescription = "Create File",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                FloatingActionButton(
                    onClick = { showFabMenu = !showFabMenu },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("explorer_main_fab")
                ) {
                    Icon(
                        imageVector = if (showFabMenu) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Add"
                    )
                }
            }
        }

        // Sort Dropdown Menu
        if (showSortMenu) {
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false }
            ) {
                SortMode.entries.forEach { mode ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = mode.label,
                                fontWeight = if (sortMode == mode) FontWeight.Bold else FontWeight.Normal,
                                color = if (sortMode == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            onSetSortMode(mode)
                            showSortMenu = false
                        }
                    )
                }
            }
        }

        // "Show or hide files" Dialog (Exact match to Screenshot 4)
        if (showHiddenFilesDialog) {
            var selectedOption by remember { mutableStateOf(showHiddenFiles) }

            AlertDialog(
                onDismissRequest = { showHiddenFilesDialog = false },
                title = {
                    Text(
                        text = "Show or hide files",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedOption = true }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedOption,
                                onClick = { selectedOption = true }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Show hidden files",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedOption = false }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = !selectedOption,
                                onClick = { selectedOption = false }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Hide files",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onToggleShowHiddenFiles(selectedOption)
                            showHiddenFilesDialog = false
                        },
                        modifier = Modifier.testTag("confirm_hidden_files_btn")
                    ) {
                        Text(
                            text = "OK",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showHiddenFilesDialog = false },
                        modifier = Modifier.testTag("cancel_hidden_files_btn")
                    ) {
                        Text(
                            text = "CANCEL",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    }
}
