package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.StorageStats
import com.example.ui.theme.LeagueSpartanFontFamily
import com.example.ui.theme.LeagueSpartanStatValue
import com.example.ui.theme.TicnoAmber
import com.example.ui.theme.TicnoDarkCard
import com.example.ui.theme.TicnoNeonGreen
import com.example.ui.theme.TicnoRose

@Composable
fun StorageCard(
    stats: StorageStats,
    onCleanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = stats.usedPercentage.coerceIn(0f, 1f),
        label = "storageProgress"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val progressColor = when {
        stats.usedPercentage > 0.90f -> TicnoRose
        stats.usedPercentage > 0.75f -> TicnoAmber
        else -> primaryColor
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("storage_overview_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(primaryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SdStorage,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "INTERNAL STORAGE",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = LeagueSpartanFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(stats.usedPercentage * 100).toInt()}% USED",
                            style = LeagueSpartanStatValue,
                            color = progressColor,
                            fontSize = 18.sp
                        )
                    }
                }

                // Total & Free info
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "FREE: ${stats.formattedFree}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = LeagueSpartanFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = TicnoNeonGreen
                    )
                    Text(
                        text = "TOTAL: ${stats.formattedTotal}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = LeagueSpartanFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Animated Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surface,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Usage metric chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "USED: ${stats.formattedUsed}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = LeagueSpartanFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "SYS://EXT_STORAGE",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = LeagueSpartanFontFamily,
                    color = primaryColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}
