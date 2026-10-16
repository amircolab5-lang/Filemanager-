package com.example.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Top header for the Home screen (Screenshot 4).
 * Adaptive for Portrait & Landscape / Android TV:
 * - Slim and compact in landscape (34dp) so it never takes up half the screen.
 * - Prominent TV-friendly Refresh button for Android TV remote and quick touch.
 * - Bold and spacious in portrait.
 */
@Composable
fun TicnoHeader(
    onOpenSettings: () -> Unit,
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .displayCutoutPadding()
                .padding(
                    horizontal = if (isLandscape) 16.dp else 18.dp,
                    vertical = if (isLandscape) 6.dp else 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Ticno3DEmblem(
                    modifier = Modifier.size(if (isLandscape) 24.dp else 30.dp)
                )
                Spacer(modifier = Modifier.width(if (isLandscape) 8.dp else 10.dp))
                Text(
                    text = "File Manager",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isLandscape) 16.sp else 22.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Dedicated TV & quick refresh button
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .testTag("header_refresh_button")
                        .size(if (isLandscape) 40.dp else 42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Files",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(if (isLandscape) 20.dp else 24.dp)
                    )
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .testTag("header_settings_button")
                        .size(if (isLandscape) 40.dp else 42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(if (isLandscape) 20.dp else 24.dp)
                    )
                }
            }
        }
    }
}

