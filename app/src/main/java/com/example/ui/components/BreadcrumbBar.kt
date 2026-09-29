package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LeagueSpartanFontFamily
import java.io.File

@Composable
fun BreadcrumbBar(
    currentDir: File,
    storageRoot: File,
    onNavigateDir: (File) -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    sdCardRoot: File? = null
) {
    val scrollState = rememberScrollState()

    // Build crumb list from root to currentDir
    val crumbs = mutableListOf<Pair<String, File>>()
    var temp: File? = currentDir
    val rootPath = storageRoot.absolutePath
    val sdRootPath = sdCardRoot?.absolutePath

    val isUnderSdCard = sdRootPath != null && (currentDir.absolutePath == sdRootPath || currentDir.absolutePath.startsWith("$sdRootPath/"))
    val activeRootPath = if (isUnderSdCard) sdRootPath!! else rootPath
    val rootLabel = if (isUnderSdCard) "SD card" else "Internal"

    while (temp != null) {
        val name = if (temp.absolutePath == activeRootPath) rootLabel else temp.name
        crumbs.add(0, Pair(name, temp))
        if (temp.absolutePath == activeRootPath) break
        temp = temp.parentFile
    }

    LaunchedEffect(currentDir) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Up Button
            IconButton(
                onClick = onNavigateUp,
                modifier = Modifier
                    .testTag("navigate_up_button")
                    .size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate Up",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Breadcrumbs Row
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState),
                verticalAlignment = Alignment.CenterVertically
            ) {
                crumbs.forEachIndexed { index, (name, dir) ->
                    val isLast = index == crumbs.lastIndex
                    val isFirst = index == 0

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onNavigateDir(dir) }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        if (isFirst) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home",
                                tint = if (isLast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = LeagueSpartanFontFamily,
                            fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                            color = if (isLast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }

                    if (!isLast) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
