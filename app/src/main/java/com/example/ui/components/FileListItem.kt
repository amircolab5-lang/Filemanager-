package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Html
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.FileItem
import com.example.ui.theme.AppIcons
import com.example.ui.theme.LeagueSpartanFontFamily
import com.example.ui.theme.ProfessionalFontFamily
import com.example.ui.theme.TicnoNeonGreen
import com.example.ui.theme.TicnoWhatsAppGreen

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileListItem(
    item: FileItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleSelect: () -> Unit,
    onOpenDetails: () -> Unit,
    onShare: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    onExtract: (() -> Unit)? = null
) {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("file_item_${item.name.replace(" ", "_")}")
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) primaryColor.copy(alpha = 0.15f)
                else Color.Transparent
            )
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) onToggleSelect() else onClick()
                },
                onLongClick = onLongClick
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect() },
                modifier = Modifier.size(34.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        // Thumbnail / File Icon (enlarged for better readability)
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(getFileIconBackground(item)),
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
                        .size(52.dp)
                        .clip(RoundedCornerShape(9.dp))
                )
            } else if (item.isApk && item.apkAppBitmap != null) {
                Image(
                    bitmap = item.apkAppBitmap.asImageBitmap(),
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Icon(
                    imageVector = getFileIcon(item),
                    contentDescription = null,
                    tint = getFileIconColor(item),
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // File info
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (item.isApk && item.isInstalledApk) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF2E7D32).copy(alpha = 0.18f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "Installed",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }

                if (item.isHidden) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(primaryColor.copy(alpha = 0.2f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "HIDDEN",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = LeagueSpartanFontFamily,
                            fontSize = 8.sp,
                            color = primaryColor
                        )
                    }
                }

                if (item.isStatusMedia) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TicnoWhatsAppGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "STATUS",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = LeagueSpartanFontFamily,
                            fontSize = 8.sp,
                            color = TicnoWhatsAppGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = item.formattedSize,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = ProfessionalFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "•",
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

        // Modern 3-dot overflow button with clear icons
        if (!isSelectionMode) {
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "File Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (item.isArchive && onExtract != null) {
                        DropdownMenuItem(
                            text = { Text("Extract ZIP") },
                            leadingIcon = { Icon(Icons.Default.FolderZip, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onExtract()
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Details") },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onOpenDetails()
                        }
                    )
                    if (!item.isDirectory) {
                        DropdownMenuItem(
                            text = { Text("Share") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onShare()
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

fun getFileIcon(item: FileItem): ImageVector {
    val ext = item.file.extension.lowercase()
    return when {
        item.isDirectory -> Icons.Default.Folder
        item.isImage -> Icons.Default.Image
        item.isVideo -> Icons.Default.Movie
        item.isAudio -> Icons.Default.Audiotrack
        item.isApk -> Icons.Default.Android
        item.isArchive -> Icons.Default.FolderZip
        // Dedicated distinct icons for document and file types
        ext == "pdf" -> Icons.Default.PictureAsPdf
        ext == "py" -> AppIcons.Python
        ext in listOf("html", "htm") -> Icons.Default.Html
        ext in listOf("xml", "json", "js", "ts", "kt", "java", "c", "cpp", "cs", "php", "sh", "sql") -> Icons.Default.Code
        ext in listOf("xls", "xlsx", "csv") -> Icons.Default.TableChart
        ext in listOf("doc", "docx") -> Icons.Default.Description
        ext in listOf("ppt", "pptx") -> Icons.Default.Description
        ext in listOf("txt", "text", "log", "md") -> Icons.Default.Description
        item.isDocument -> Icons.Default.Description
        else -> Icons.Default.InsertDriveFile
    }
}

fun getFileIconColor(item: FileItem): Color {
    val ext = item.file.extension.lowercase()
    return when {
        item.isDirectory -> Color(0xFFFFB74D) // Folder amber
        item.isImage -> Color(0xFFAB47BC)
        item.isVideo -> Color(0xFFFF7043)
        item.isAudio -> Color(0xFFFFA726)
        item.isApk -> TicnoNeonGreen
        item.isArchive -> Color(0xFF8D6E63)
        // Distinct colors for specific formats
        ext == "pdf" -> Color(0xFFE53935) // PDF red
        ext == "py" -> Color(0xFF3776AB) // Python blue/gold
        ext in listOf("html", "htm") -> Color(0xFFFF6D00) // HTML vibrant orange
        ext in listOf("xml", "json", "js", "ts", "kt", "java", "c", "cpp", "cs", "php", "sh", "sql") -> Color(0xFF2563EB) // Code blue
        ext in listOf("xls", "xlsx", "csv") -> Color(0xFF43A047) // Excel green
        ext in listOf("doc", "docx") -> Color(0xFF1E88E5) // Word blue
        ext in listOf("ppt", "pptx") -> Color(0xFFFF5722) // PPT deep orange
        ext in listOf("txt", "text", "log", "md") -> Color(0xFF78909C) // Text slate
        item.isDocument -> Color(0xFF42A5F5)
        else -> Color(0xFF90A4AE)
    }
}

fun getFileIconBackground(item: FileItem): Color {
    return getFileIconColor(item).copy(alpha = 0.15f)
}
