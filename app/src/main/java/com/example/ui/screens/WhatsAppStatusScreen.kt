package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import com.example.ui.theme.AppIcons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.FileItem
import com.example.ui.components.AdMobNativeAdCard
import com.example.ui.theme.LeagueSpartanFontFamily
import com.example.ui.theme.TicnoWhatsAppBright
import com.example.ui.theme.TicnoWhatsAppGreen
import com.example.ui.viewmodel.StatusFilter

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
    modifier: Modifier = Modifier
) {
    val filtered = remember(statuses, statusFilter) {
        when (statusFilter) {
            StatusFilter.ALL -> statuses
            StatusFilter.PHOTOS -> statuses.filter { it.isImage }
            StatusFilter.VIDEOS -> statuses.filter { it.isVideo }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // High-contrast vibrant banner header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(TicnoWhatsAppGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AppIcons.StatusSaver,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    val isDark = MaterialTheme.colorScheme.surface.red < 0.5f
                    val headingColor = if (isDark) TicnoWhatsAppBright else Color(0xFF047857)
                    Column {
                        Text(
                            text = "STATUS SAVER",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = LeagueSpartanFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = headingColor
                        )
                        Text(
                            text = "${filtered.size} Statuses Found (.Statuses folder)",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = LeagueSpartanFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.testTag("refresh_statuses_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Statuses",
                        tint = TicnoWhatsAppBright
                    )
                }
            }
        }

        // Filter chips: All, Photos, Videos
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = statusFilter == StatusFilter.ALL,
                onClick = { onFilterChange(StatusFilter.ALL) },
                label = { Text("All (${statuses.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TicnoWhatsAppGreen.copy(alpha = 0.2f),
                    selectedLabelColor = TicnoWhatsAppGreen
                )
            )
            FilterChip(
                selected = statusFilter == StatusFilter.PHOTOS,
                onClick = { onFilterChange(StatusFilter.PHOTOS) },
                label = { Text("Photos (${statuses.count { it.isImage }})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TicnoWhatsAppGreen.copy(alpha = 0.2f),
                    selectedLabelColor = TicnoWhatsAppGreen
                )
            )
            FilterChip(
                selected = statusFilter == StatusFilter.VIDEOS,
                onClick = { onFilterChange(StatusFilter.VIDEOS) },
                label = { Text("Videos (${statuses.count { it.isVideo }})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TicnoWhatsAppGreen.copy(alpha = 0.2f),
                    selectedLabelColor = TicnoWhatsAppGreen
                )
            )
        }

        // Native Ad Card
        AdMobNativeAdCard(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        // Informational guide card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tip: View a status in your messaging app, and it will automatically be detected here. Tap the download icon to save it permanently!",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Status Grid
        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = AppIcons.StatusSaver,
                        contentDescription = null,
                        tint = TicnoWhatsAppGreen.copy(alpha = 0.4f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "NO STATUSES CACHED YET",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = LeagueSpartanFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "View statuses in your social app to cache them here, then tap to save directly to your phone storage.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 88.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.path }) { item ->
                    StatusCard(
                        item = item,
                        onOpen = { onOpenStatus(item) },
                        onSave = { onSaveStatus(item) },
                        onShare = { onShareStatus(item) }
                    )
                }
            }
        }
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        border = CardDefaults.outlinedCardBorder()
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
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = LeagueSpartanFontFamily,
                        color = Color.White,
                        fontSize = 9.sp
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
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = LeagueSpartanFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
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
