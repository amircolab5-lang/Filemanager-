package com.example.ui.components

import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Folder
import com.example.util.DocumentCollectionsManager
import java.io.File
import androidx.compose.material3.Surface
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CategoryType
import com.example.ui.theme.AppIcons
import com.example.util.FileManagerHelper

data class CategoryIconItem(
    val category: CategoryType?,
    val title: String,
    val countText: String,
    val circleColor: Color,
    val icon: ImageVector,
    val isMore: Boolean = false
)

/**
 * The 8 circular category buttons shown on the main File Manager Home screen (Screenshot 4).
 * Arranged in 4 columns x 2 rows:
 * Audio, Videos, Images, APKs
 * Documents, WhatsApp, Download, More
 */
@Composable
fun HomeCategoryGrid(
    onCategoryClick: (CategoryType) -> Unit,
    onMoreClick: () -> Unit,
    counts: Map<CategoryType, Int> = emptyMap(),
    whatsAppCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val items = listOf(
        CategoryIconItem(
            category = CategoryType.AUDIO,
            title = "Audio",
            countText = (counts[CategoryType.AUDIO] ?: 0).toString(),
            circleColor = Color(0xFFFF2D55),
            icon = Icons.Default.Audiotrack
        ),
        CategoryIconItem(
            category = CategoryType.VIDEOS,
            title = "Videos",
            countText = (counts[CategoryType.VIDEOS] ?: 0).toString(),
            circleColor = Color(0xFF7C4DFF),
            icon = Icons.Default.PlayArrow
        ),
        CategoryIconItem(
            category = CategoryType.IMAGES,
            title = "Images",
            countText = (counts[CategoryType.IMAGES] ?: 0).let { if (it > 999) "999+" else it.toString() },
            circleColor = Color(0xFF00A3FF),
            icon = Icons.Default.Image
        ),
        CategoryIconItem(
            category = CategoryType.APKS,
            title = "APKs",
            countText = (counts[CategoryType.APKS] ?: 0).toString(),
            circleColor = Color(0xFF00C853),
            icon = Icons.Default.Android
        ),
        CategoryIconItem(
            category = CategoryType.DOCUMENTS,
            title = "Documents",
            countText = (counts[CategoryType.DOCUMENTS] ?: 0).let { if (it > 999) "999+" else it.toString() },
            circleColor = Color(0xFFFF6D00),
            icon = Icons.Default.Description
        ),
        CategoryIconItem(
            category = CategoryType.WHATSAPP_STATUS,
            title = "Status Saver",
            countText = whatsAppCount.toString(),
            circleColor = Color(0xFF25D366),
            icon = AppIcons.StatusSaver
        ),
        CategoryIconItem(
            category = CategoryType.DOWNLOADS,
            title = "Download",
            countText = (counts[CategoryType.DOWNLOADS] ?: 0).toString(),
            circleColor = Color(0xFF00B4D8),
            icon = Icons.Default.Download
        ),
        CategoryIconItem(
            category = null,
            title = "More",
            countText = "",
            circleColor = Color(0xFF94A3B8),
            icon = Icons.Default.MoreHoriz,
            isMore = true
        )
    )

    if (isLandscape) {
        // Landscape / TV: Single sleek horizontal row with all 8 categories
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (item in items) {
                CategoryCircleCell(
                    item = item,
                    onClick = {
                        if (item.isMore) {
                            onMoreClick()
                        } else {
                            item.category?.let(onCategoryClick)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Row 1: Audio, Videos, Images, APKs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 0..3) {
                    val item = items[i]
                    CategoryCircleCell(
                        item = item,
                        onClick = { item.category?.let(onCategoryClick) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Row 2: Documents, WhatsApp, Download, More
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 4..7) {
                    val item = items[i]
                    CategoryCircleCell(
                        item = item,
                        onClick = {
                            if (item.isMore) {
                                onMoreClick()
                            } else {
                                item.category?.let(onCategoryClick)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * The circular category buttons shown on the "More" screen.
 * Shows categories conditionally based on whether the app or related files exist on device:
 * - Messenger: only if Messenger is installed or files exist
 * - XShare: only if XShare is installed or files exist
 * - XHide: only if XHide is installed or files exist
 * - Bluetooth: only if Bluetooth is supported or files exist
 * Dynamic 4-column responsive grid.
 */
@Composable
fun MoreCategoryGrid(
    onCategoryClick: (CategoryType) -> Unit,
    counts: Map<CategoryType, Int> = emptyMap(),
    whatsAppCount: Int = 0,
    onCollectionAddClick: () -> Unit,
    onOpenDirectory: ((File) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val candidateItems = listOf(
        CategoryIconItem(
            category = CategoryType.AUDIO,
            title = "Audio",
            countText = (counts[CategoryType.AUDIO] ?: 0).toString(),
            circleColor = Color(0xFFFF2D55),
            icon = Icons.Default.Audiotrack
        ),
        CategoryIconItem(
            category = CategoryType.VIDEOS,
            title = "Videos",
            countText = (counts[CategoryType.VIDEOS] ?: 0).toString(),
            circleColor = Color(0xFF7C4DFF),
            icon = Icons.Default.PlayArrow
        ),
        CategoryIconItem(
            category = CategoryType.IMAGES,
            title = "Images",
            countText = (counts[CategoryType.IMAGES] ?: 0).let { if (it > 999) "999+" else it.toString() },
            circleColor = Color(0xFF00A3FF),
            icon = Icons.Default.Image
        ),
        CategoryIconItem(
            category = CategoryType.APKS,
            title = "APKs",
            countText = (counts[CategoryType.APKS] ?: 0).toString(),
            circleColor = Color(0xFF00C853),
            icon = Icons.Default.Android
        ),
        CategoryIconItem(
            category = CategoryType.DOCUMENTS,
            title = "Documents",
            countText = (counts[CategoryType.DOCUMENTS] ?: 0).let { if (it > 999) "999+" else it.toString() },
            circleColor = Color(0xFFFF6D00),
            icon = Icons.Default.Description
        ),
        CategoryIconItem(
            category = CategoryType.WHATSAPP_STATUS,
            title = "Status Saver",
            countText = whatsAppCount.toString(),
            circleColor = Color(0xFF25D366),
            icon = AppIcons.StatusSaver
        ),
        CategoryIconItem(
            category = CategoryType.DOWNLOADS,
            title = "Download",
            countText = (counts[CategoryType.DOWNLOADS] ?: 0).toString(),
            circleColor = Color(0xFF00B4D8),
            icon = Icons.Default.Download
        ),
        CategoryIconItem(
            category = CategoryType.BLUETOOTH,
            title = "Bluetooth",
            countText = (counts[CategoryType.BLUETOOTH] ?: 0).toString(),
            circleColor = Color(0xFF1E88E5),
            icon = Icons.Default.Bluetooth
        ),
        CategoryIconItem(
            category = CategoryType.MESSENGER,
            title = "Messenger",
            countText = (counts[CategoryType.MESSENGER] ?: 0).toString(),
            circleColor = Color(0xFF0084FF),
            icon = AppIcons.Messenger
        ),
        CategoryIconItem(
            category = CategoryType.ZIPS,
            title = "Zips",
            countText = (counts[CategoryType.ZIPS] ?: 0).toString(),
            circleColor = Color(0xFF7CB342),
            icon = Icons.Default.FolderZip
        ),
        CategoryIconItem(
            category = CategoryType.XHIDE,
            title = "T-Hide",
            countText = (counts[CategoryType.XHIDE] ?: 0).let { if (it > 0) it.toString() else "" },
            circleColor = Color(0xFFA855F7), // Ultra-vibrant electric purple
            icon = AppIcons.XHide
        )
    )

    val availableItems = candidateItems

    val rows = remember(availableItems) { availableItems.chunked(4) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                rowItems.forEach { item ->
                    CategoryCircleCell(
                        item = item,
                        onClick = { item.category?.let(onCategoryClick) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Balance last row with empty space if fewer than 4 items
                if (rowItems.size < 4) {
                    repeat(4 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.8.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        )

        Spacer(modifier = Modifier.height(12.dp))

        // "Collection of documents" section
        Text(
            text = "Collection of documents",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        val collections = remember { DocumentCollectionsManager.getCollections() }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Circular "+" button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF94A3B8).copy(alpha = 0.25f))
                    .clickable(onClick = onCollectionAddClick)
                    .testTag("collection_add_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Collection",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (collections.isEmpty()) {
                Text(
                    text = "Tap + to create custom document collections (Bills, Work, Receipts)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            } else {
                collections.forEach { col ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onOpenDirectory?.invoke(col.folder) },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = col.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${col.fileCount} items",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCircleCell(
    item: CategoryIconItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val circleSize = if (isLandscape) 46.dp else 62.dp
    val iconSize = if (isLandscape) 22.dp else 30.dp
    val verticalPadding = if (isLandscape) 2.dp else 6.dp

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = verticalPadding, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Vibrant circle with centered white icon
        Box(
            modifier = Modifier
                .size(circleSize)
                .clip(CircleShape)
                .background(item.circleColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = Color.White,
                modifier = Modifier.size(iconSize)
            )
        }

        Spacer(modifier = Modifier.height(if (isLandscape) 3.dp else 6.dp))

        Text(
            text = item.title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.ExtraBold,
            fontSize = if (isLandscape) 11.5.sp else 13.5.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = if (item.countText.isNotEmpty()) item.countText else " ",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            fontSize = if (isLandscape) 10.sp else 11.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}
