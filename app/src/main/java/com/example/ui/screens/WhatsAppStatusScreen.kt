package com.example.ui.screens

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.FileItem
import com.example.ui.components.AdMobNativeAdCard
import com.example.ui.components.tvGridScrollbar
import com.example.ui.theme.AppIcons
import com.example.ui.theme.TicnoWhatsAppGreen
import com.example.ui.viewmodel.StatusFilter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Professional WhatsApp Status Saver Screen matching User's exact screenshots (Screenshot 4, 5, 6, 7):
 * - TopBar with Back arrow, "WhatsApp", and Search
 * - "Media File" -> opens WhatsApp Media folder directly in file manager
 * - "Automatically save browsed status" [Switch Toggle]
 * - "Automatically clean up status" "15 days ago" -> opens "Select time" dialog
 * - ".Statuses" -> opens status folder selector dialog
 * - Recent status preview card showing WhatsApp badge, image count, date, and thumbnail strip
 * - High-contrast category filter chips (All, Photos, Videos)
 * - Status media grid with prominent, crisp text and save/share buttons
 * - Floating Action Button to save statuses quickly
 */
@Composable
fun WhatsAppStatusScreen(
    statuses: List<FileItem>,
    statusFilter: StatusFilter,
    isLoading: Boolean,
    onFilterChange: (StatusFilter) -> Unit,
    onSaveStatus: (FileItem) -> Unit,
    onShareStatus: (FileItem) -> Unit,
    onOpenStatus: (FileItem) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit = {},
    onOpenMediaFolder: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isDark = MaterialTheme.colorScheme.surface.red < 0.5f
    val gridState = rememberLazyGridState()

    val prefs = remember { context.getSharedPreferences("whatsapp_status_prefs", Context.MODE_PRIVATE) }
    var autoSaveEnabled by remember { mutableStateOf(prefs.getBoolean("pref_auto_save", true)) }
    var cleanupTime by remember { mutableStateOf(prefs.getString("pref_cleanup_time", "15 days") ?: "15 days") }
    var statusFolderMode by remember { mutableStateOf(prefs.getString("pref_status_folder", ".Statuses") ?: ".Statuses") }

    var showCleanupDialog by remember { mutableStateOf(false) }
    var showFolderDialog by remember { mutableStateOf(false) }

    val filtered = remember(statuses, statusFilter) {
        when (statusFilter) {
            StatusFilter.ALL -> statuses
            StatusFilter.PHOTOS -> statuses.filter { it.isImage }
            StatusFilter.VIDEOS -> statuses.filter { it.isVideo }
        }
    }

    val imageStatuses = remember(statuses) { statuses.filter { it.isImage } }
    val todayDateFormatted = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. TopBar matching Screenshot 4: Back Arrow, "WhatsApp", Search icon
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
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
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "WhatsApp",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onRefresh) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        IconButton(onClick = onOpenMediaFolder) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search WhatsApp Media",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // 2. Settings & Media Grid
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Adaptive(minSize = if (isLandscape) 140.dp else 145.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .tvGridScrollbar(gridState),
                contentPadding = PaddingValues(
                    start = if (isLandscape) 8.dp else 16.dp,
                    end = if (isLandscape) 8.dp else 16.dp,
                    top = 8.dp,
                    bottom = if (isLandscape) 32.dp else 88.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 6.dp else 10.dp),
                verticalArrangement = Arrangement.spacedBy(if (isLandscape) 6.dp else 10.dp)
            ) {
                // Settings Section (Span full width) matching Screenshot 4
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        // Option 1: Media File >
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenMediaFolder() }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Media File",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Option 2: Automatically save browsed status [Switch]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Automatically save browsed status",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = autoSaveEnabled,
                                onCheckedChange = {
                                    autoSaveEnabled = it
                                    prefs.edit().putBoolean("pref_auto_save", it).apply()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF00A3FF)
                                )
                            )
                        }

                        // Option 3: Automatically clean up status "15 days ago" >
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCleanupDialog = true }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Automatically clean up status",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$cleanupTime ago",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Option 4: .Statuses >
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showFolderDialog = true }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = statusFolderMode,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Option 5: Featured Status Card (Screenshot 4)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onFilterChange(StatusFilter.PHOTOS) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) Color(0xFF1E222B) else Color(0xFFF1F5F9)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(TicnoWhatsAppGreen),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = AppIcons.StatusSaver,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${imageStatuses.size} Images from $statusFolderMode",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = todayDateFormatted,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                if (imageStatuses.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(imageStatuses.take(6), key = { it.path }) { item ->
                                            AsyncImage(
                                                model = ImageRequest.Builder(context)
                                                    .data(item.file)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(80.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { onOpenStatus(item) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Filter Chips: All, Photos, Videos (High contrast!)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = statusFilter == StatusFilter.ALL,
                                onClick = { onFilterChange(StatusFilter.ALL) },
                                label = { Text("All (${statuses.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TicnoWhatsAppGreen,
                                    selectedLabelColor = Color.White,
                                    containerColor = if (isDark) Color(0xFF1E222B) else Color(0xFFF1F5F9),
                                    labelColor = if (isDark) Color.White else Color(0xFF0F172A)
                                )
                            )
                            FilterChip(
                                selected = statusFilter == StatusFilter.PHOTOS,
                                onClick = { onFilterChange(StatusFilter.PHOTOS) },
                                label = { Text("Photos (${statuses.count { it.isImage }})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TicnoWhatsAppGreen,
                                    selectedLabelColor = Color.White,
                                    containerColor = if (isDark) Color(0xFF1E222B) else Color(0xFFF1F5F9),
                                    labelColor = if (isDark) Color.White else Color(0xFF0F172A)
                                )
                            )
                            FilterChip(
                                selected = statusFilter == StatusFilter.VIDEOS,
                                onClick = { onFilterChange(StatusFilter.VIDEOS) },
                                label = { Text("Videos (${statuses.count { it.isVideo }})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TicnoWhatsAppGreen,
                                    selectedLabelColor = Color.White,
                                    containerColor = if (isDark) Color(0xFF1E222B) else Color(0xFFF1F5F9),
                                    labelColor = if (isDark) Color.White else Color(0xFF0F172A)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // In portrait mode, show AdMob native ad
                if (!isLandscape) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        AdMobNativeAdCard(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }

                // Status Items Grid
                items(filtered, key = { it.path }) { item ->
                    StatusCard(
                        item = item,
                        onOpen = { onOpenStatus(item) },
                        onSave = { onSaveStatus(item) },
                        onShare = { onShareStatus(item) }
                    )
                }

                // In landscape mode, show AdMob native ad at bottom
                if (isLandscape) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        AdMobNativeAdCard(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }

        // Floating Action Button matching Screenshot 4 (Blue circle with Save icon)
        FloatingActionButton(
            onClick = {
                // Save all visible statuses or the latest one
                filtered.firstOrNull()?.let { onSaveStatus(it) }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("whatsapp_save_fab"),
            shape = CircleShape,
            containerColor = Color(0xFF00A3FF),
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = "Save Statuses",
                modifier = Modifier.size(24.dp)
            )
        }
    }

    // Dialog 1: "Select time" dialog matching Screenshot 7
    if (showCleanupDialog) {
        val cleanupOptions = listOf("15 days", "1 month", "3 months", "Never")
        var selectedOption by remember { mutableStateOf(cleanupTime) }

        AlertDialog(
            onDismissRequest = { showCleanupDialog = false },
            title = {
                Text(text = "Select time", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column {
                    cleanupOptions.forEach { opt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedOption = opt }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = opt,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            RadioButton(
                                selected = selectedOption == opt,
                                onClick = { selectedOption = opt },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00A3FF))
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        cleanupTime = selectedOption
                        prefs.edit().putString("pref_cleanup_time", selectedOption).apply()
                        showCleanupDialog = false
                    }
                ) {
                    Text("OK", fontWeight = FontWeight.Bold, color = Color(0xFF00A3FF))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCleanupDialog = false }) {
                    Text("CANCEL", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // Dialog 2: Folder selector dialog matching Screenshot 6
    if (showFolderDialog) {
        val folderOptions = listOf(".Statuses", "AutoSave", "ManuallySave")
        var selectedFolder by remember { mutableStateOf(statusFolderMode) }

        AlertDialog(
            onDismissRequest = { showFolderDialog = false },
            title = {
                Text(text = "Status Folder", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column {
                    folderOptions.forEach { folder ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedFolder = folder }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = folder,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            RadioButton(
                                selected = selectedFolder == folder,
                                onClick = { selectedFolder = folder },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00A3FF))
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        statusFolderMode = selectedFolder
                        prefs.edit().putString("pref_status_folder", selectedFolder).apply()
                        showFolderDialog = false
                    }
                ) {
                    Text("OK", fontWeight = FontWeight.Bold, color = Color(0xFF00A3FF))
                }
            },
            dismissButton = {
                TextButton(onClick = { showFolderDialog = false }) {
                    Text("CANCEL", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun StatusCard(
    item: FileItem,
    onOpen: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.surface.red < 0.5f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E222B) else Color(0xFFFFFFFF)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(item.file)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (item.isVideo) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = "Play Video",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // File size badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.formattedSize,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }

            // Action row: Save & Share
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (item.isVideo) "Video" else "Photo",
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 6.dp)
                )

                Row {
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onSave,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Save to Gallery",
                            tint = TicnoWhatsAppGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
