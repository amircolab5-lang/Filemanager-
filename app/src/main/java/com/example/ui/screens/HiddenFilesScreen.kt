package com.example.ui.screens

import android.content.res.Configuration
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FileItem
import com.example.ui.components.AdMobNativeAdCard
import com.example.ui.components.tvListScrollbar
import com.example.ui.components.getFileIcon
import com.example.ui.components.getFileIconBackground
import com.example.ui.components.getFileIconColor
import com.example.ui.theme.LeagueSpartanFontFamily
import com.example.ui.theme.TicnoNeonGreen

@Composable
fun HiddenFilesScreen(
    hiddenFiles: List<FileItem>,
    isLoading: Boolean,
    onOpenFile: (FileItem) -> Unit,
    onUnhideFile: (FileItem) -> Unit,
    onShareFile: (FileItem) -> Unit,
    onShowDetails: (FileItem) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val listState = rememberLazyListState()

    Column(modifier = modifier.fillMaxSize()) {
        // Banner header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(
                        horizontal = if (isLandscape) 12.dp else 16.dp,
                        vertical = if (isLandscape) 6.dp else 10.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(if (isLandscape) 28.dp else 36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(if (isLandscape) 18.dp else 22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(if (isLandscape) 8.dp else 10.dp))
                    Column {
                        Text(
                            text = "HIDDEN & OBSCURE FILES",
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = LeagueSpartanFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isLandscape) 13.sp else 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${hiddenFiles.size} Hidden Items (.dot & .nomedia files)",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = LeagueSpartanFontFamily,
                            fontSize = if (isLandscape) 10.sp else 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .testTag("refresh_hidden_button")
                        .size(if (isLandscape) 32.dp else 42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Hidden Files",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(if (isLandscape) 18.dp else 24.dp)
                    )
                }
            }
        }

        // List of hidden files
        if (hiddenFiles.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "NO HIDDEN FILES DETECTED",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = LeagueSpartanFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tap the refresh button to run a deep storage scan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))
                AdMobNativeAdCard(modifier = Modifier.fillMaxWidth())
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .tvListScrollbar(listState),
                contentPadding = PaddingValues(
                    horizontal = if (isLandscape) 10.dp else 16.dp,
                    vertical = if (isLandscape) 4.dp else 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(if (isLandscape) 4.dp else 8.dp)
            ) {
                // Info Card at top of list
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "These files are hidden from standard Android apps. Tap 'Unhide' to make them visible everywhere.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = if (isLandscape) 10.sp else 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // In portrait, show ad above files
                if (!isLandscape) {
                    item {
                        AdMobNativeAdCard(
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                items(hiddenFiles, key = { it.path }) { item ->
                    HiddenFileCard(
                        item = item,
                        onOpen = { onOpenFile(item) },
                        onUnhide = { onUnhideFile(item) },
                        onShare = { onShareFile(item) },
                        onDetails = { onShowDetails(item) }
                    )
                }

                // In landscape, show ad below files so user sees hidden files immediately
                if (isLandscape) {
                    item {
                        AdMobNativeAdCard(
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(if (isLandscape) 32.dp else 80.dp))
                }
            }
        }
    }
}

@Composable
fun HiddenFileCard(
    item: FileItem,
    onOpen: () -> Unit,
    onUnhide: () -> Unit,
    onShare: () -> Unit,
    onDetails: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(getFileIconBackground(item)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getFileIcon(item),
                        contentDescription = null,
                        tint = getFileIconColor(item),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = if (item.isNomediaChild) ".NOMEDIA" else "HIDDEN",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = LeagueSpartanFontFamily,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 8.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = item.path,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = LeagueSpartanFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${item.formattedSize} • ${item.formattedDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDetails,
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Details", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", fontSize = 11.sp)
                }

                if (item.name.startsWith(".")) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onUnhide,
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = "Unhide",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Unhide", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
