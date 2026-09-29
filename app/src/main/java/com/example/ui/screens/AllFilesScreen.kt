package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.StorageStats
import com.example.model.StorageVolumeInfo
import com.example.ui.components.AdMobNativeAdCard

/**
 * All files / Storage selection screen (Screenshot 1).
 * Features:
 * - Top bar: Back arrow, "Home", Search icon
 * - Subheader: "All files"
 * - List: Internal storage and SD card
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllFilesScreen(
    storageStats: StorageStats,
    onBack: () -> Unit,
    onOpenInternalStorage: () -> Unit,
    onOpenSdCard: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    sdCardVolume: StorageVolumeInfo? = null
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Home",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("all_files_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier.testTag("all_files_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // "All files" section title
            Text(
                text = "All files",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Internal Storage Row
            StorageLocationRow(
                icon = Icons.Default.Smartphone,
                title = "Internal storage",
                subtitle = "${storageStats.formattedUsed} used/${storageStats.formattedTotal}",
                onClick = onOpenInternalStorage,
                testTag = "internal_storage_row"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // SD Card Row (ONLY shown if an actual physical/removable SD card is inserted & mounted)
            if (sdCardVolume != null && sdCardVolume.isMounted && sdCardVolume.totalBytes > 0L) {
                Spacer(modifier = Modifier.height(24.dp))

                StorageLocationRow(
                    icon = Icons.Default.SdCard,
                    title = sdCardVolume.name,
                    subtitle = "${sdCardVolume.formattedUsed} used/${sdCardVolume.formattedTotal}",
                    onClick = onOpenSdCard,
                    testTag = "sd_card_row"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Non-intrusive AdMob Native Ad Card
            AdMobNativeAdCard(
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun StorageLocationRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.width(20.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}
