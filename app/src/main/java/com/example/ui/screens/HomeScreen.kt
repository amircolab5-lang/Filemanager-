package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.CategoryType
import com.example.model.FileItem
import com.example.model.StorageStats
import com.example.ui.components.AdMobNativeAdCard
import com.example.ui.components.HomeCategoryGrid
import com.example.ui.components.getFileIcon
import com.example.ui.components.getFileIconColor
import com.example.ui.theme.ProfessionalFontFamily

/**
 * Main File Manager Home screen (Screenshot 4).
 * Features:
 * - Search bar ("Search files")
 * - 8 Circular Category Buttons (Audio, Videos, Images, APKs, Documents, WhatsApp, Download, More)
 * - "All files" row with orange folder icon
 * - "Release more space" row with blue brush icon
 * - Non-intrusive AdMob native card
 * - "Recent documents" section with "Today >" header and recent thumbnails
 */
@Composable
fun HomeScreen(
    storageStats: StorageStats,
    hasPermission: Boolean,
    categoryCounts: Map<CategoryType, Int>,
    whatsAppStatusCount: Int,
    recentDocuments: List<FileItem>,
    onRequestPermission: () -> Unit,
    onCategoryClick: (CategoryType) -> Unit,
    onMoreClick: () -> Unit,
    onOpenAllFiles: () -> Unit,
    onOpenCleaner: () -> Unit,
    onOpenFile: (FileItem) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasSdCard: Boolean = false
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = if (isLandscape) 12.dp else 16.dp),
        verticalArrangement = Arrangement.spacedBy(if (isLandscape) 8.dp else 14.dp),
        contentPadding = PaddingValues(top = if (isLandscape) 2.dp else 4.dp, bottom = if (isLandscape) 16.dp else 24.dp)
    ) {
        // Storage Permission Banner if needed
        if (!hasPermission) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("permission_banner"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Storage Access Required",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Grant permission to manage files and view categories.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                        Button(
                            onClick = onRequestPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("grant_permission_button")
                        ) {
                            Text("Grant", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Search Bar (Pill shape matching Screenshot 4)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isLandscape) 36.dp else 44.dp)
                    .clip(RoundedCornerShape(if (isLandscape) 18.dp else 22.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .clickable(onClick = onSearchClick)
                    .padding(horizontal = 16.dp)
                    .testTag("home_search_bar"),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(if (isLandscape) 18.dp else 20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Search files",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = if (isLandscape) 13.sp else 14.sp
                    )
                }
            }
        }

        // The 8 Circular Categories (Adaptive in landscape)
        item {
            HomeCategoryGrid(
                onCategoryClick = onCategoryClick,
                onMoreClick = onMoreClick,
                counts = categoryCounts,
                whatsAppCount = whatsAppStatusCount
            )
        }

        // Storage Navigation Rows: Side-by-side in landscape for TVs and phones
        item {
            val allFilesSubtitle = if (hasSdCard) "Internal storage, SD card" else "Internal storage"
            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        HomeNavigationActionRow(
                            icon = Icons.Default.Folder,
                            iconBackground = Color(0xFFF97316),
                            title = "All files",
                            subtitle = allFilesSubtitle,
                            onClick = onOpenAllFiles,
                            testTag = "home_all_files_row"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        HomeNavigationActionRow(
                            icon = Icons.Default.CleaningServices,
                            iconBackground = Color(0xFF0EA5E9),
                            title = "Release more space",
                            subtitle = "${storageStats.formattedUsed} used/${storageStats.formattedTotal}",
                            onClick = onOpenCleaner,
                            testTag = "home_release_space_row"
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HomeNavigationActionRow(
                        icon = Icons.Default.Folder,
                        iconBackground = Color(0xFFF97316),
                        title = "All files",
                        subtitle = allFilesSubtitle,
                        onClick = onOpenAllFiles,
                        testTag = "home_all_files_row"
                    )

                    HomeNavigationActionRow(
                        icon = Icons.Default.CleaningServices,
                        iconBackground = Color(0xFF0EA5E9),
                        title = "Release more space",
                        subtitle = "${storageStats.formattedUsed} used/${storageStats.formattedTotal}",
                        onClick = onOpenCleaner,
                        testTag = "home_release_space_row"
                    )
                }
            }
        }

        // Sleek Non-intrusive AdMob Native Ad Card
        item {
            AdMobNativeAdCard(
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Recent Documents Section (Screenshot 4)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = { onCategoryClick(CategoryType.DOCUMENTS) })
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent documents",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )

                    Row(
                        modifier = Modifier.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "View more recent documents",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (recentDocuments.isEmpty()) {
                    // Placeholder card if no recent documents yet
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recent documents",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // Full vertical chronological history list so users can scroll and reach any recent file
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        recentDocuments.forEach { doc ->
                            RecentDocumentHistoryItem(
                                item = doc,
                                onClick = { onOpenFile(doc) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeNavigationActionRow(
    icon: ImageVector,
    iconBackground: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.5.sp
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun RecentDocumentHistoryItem(
    item: FileItem,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Direct icon without enclosing box/background or padding, matching professional reference
        Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center
        ) {
            if (item.isImage || (item.isVideo && item.file.exists())) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(item.file)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(6.dp))
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
                            contentDescription = "Video",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else if (item.isApk && item.apkAppBitmap != null) {
                Image(
                    bitmap = item.apkAppBitmap.asImageBitmap(),
                    contentDescription = item.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(6.dp))
                )
            } else {
                // Pure clean icon directly without box or background
                Icon(
                    imageVector = getFileIcon(item),
                    contentDescription = item.name,
                    tint = getFileIconColor(item),
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.formattedSize,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = ProfessionalFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = " • ",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = item.formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = ProfessionalFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
