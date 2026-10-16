package com.example.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * High-visibility scrollbar indicator for Android TV and mobile horizontal / landscape modes.
 * In vertical (portrait) mode, no scrollbar is shown.
 * In horizontal (landscape) mode, displays clearly on the right edge.
 */
fun Modifier.tvListScrollbar(
    state: LazyListState,
    width: Dp = 6.dp,
    trackColor: Color = Color(0x33888888),
    thumbColor: Color = Color(0xFF00A3FF)
): Modifier = composed {
    val configuration = LocalConfiguration.current
    // Hide scrollbar in vertical (portrait) mode as requested
    if (configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
        return@composed Modifier
    }

    val isScrolling = state.isScrollInProgress
    val targetAlpha = if (isScrolling) 1f else 0.45f
    val alpha by animateFloatAsState(targetValue = targetAlpha, label = "scrollbarAlpha")

    drawWithContent {
        drawContent()
        val totalCount = state.layoutInfo.totalItemsCount
        val visibleInfo = state.layoutInfo.visibleItemsInfo
        if (totalCount > 0 && visibleInfo.isNotEmpty() && visibleInfo.size < totalCount) {
            val firstIndex = visibleInfo.first().index
            val visibleCount = visibleInfo.size
            val heightRatio = (visibleCount.toFloat() / totalCount).coerceIn(0.1f, 1f)
            val offsetRatio = (firstIndex.toFloat() / totalCount).coerceIn(0f, 1f - heightRatio)

            val scrollbarHeight = (size.height * heightRatio).coerceIn(28.dp.toPx(), size.height)
            val scrollbarY = size.height * offsetRatio

            // Draw track line
            drawRoundRect(
                color = trackColor.copy(alpha = alpha * 0.4f),
                topLeft = Offset(size.width - width.toPx() - 2.dp.toPx(), 4.dp.toPx()),
                size = Size(width.toPx(), (size.height - 8.dp.toPx()).coerceAtLeast(0f)),
                cornerRadius = CornerRadius(width.toPx() / 2)
            )

            // Draw thumb indicator
            drawRoundRect(
                color = thumbColor.copy(alpha = alpha),
                topLeft = Offset(size.width - width.toPx() - 2.dp.toPx(), scrollbarY),
                size = Size(width.toPx(), scrollbarHeight),
                cornerRadius = CornerRadius(width.toPx() / 2)
            )
        }
    }
}

fun Modifier.tvGridScrollbar(
    state: LazyGridState,
    width: Dp = 6.dp,
    trackColor: Color = Color(0x33888888),
    thumbColor: Color = Color(0xFF00A3FF)
): Modifier = composed {
    val configuration = LocalConfiguration.current
    // Hide scrollbar in vertical (portrait) mode as requested
    if (configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
        return@composed Modifier
    }

    val isScrolling = state.isScrollInProgress
    val targetAlpha = if (isScrolling) 1f else 0.45f
    val alpha by animateFloatAsState(targetValue = targetAlpha, label = "gridScrollbarAlpha")

    drawWithContent {
        drawContent()
        val totalCount = state.layoutInfo.totalItemsCount
        val visibleInfo = state.layoutInfo.visibleItemsInfo
        if (totalCount > 0 && visibleInfo.isNotEmpty() && visibleInfo.size < totalCount) {
            val firstIndex = visibleInfo.first().index
            val visibleCount = visibleInfo.size
            val heightRatio = (visibleCount.toFloat() / totalCount).coerceIn(0.1f, 1f)
            val offsetRatio = (firstIndex.toFloat() / totalCount).coerceIn(0f, 1f - heightRatio)

            val scrollbarHeight = (size.height * heightRatio).coerceIn(28.dp.toPx(), size.height)
            val scrollbarY = size.height * offsetRatio

            // Draw track line
            drawRoundRect(
                color = trackColor.copy(alpha = alpha * 0.4f),
                topLeft = Offset(size.width - width.toPx() - 2.dp.toPx(), 4.dp.toPx()),
                size = Size(width.toPx(), (size.height - 8.dp.toPx()).coerceAtLeast(0f)),
                cornerRadius = CornerRadius(width.toPx() / 2)
            )

            // Draw thumb indicator
            drawRoundRect(
                color = thumbColor.copy(alpha = alpha),
                topLeft = Offset(size.width - width.toPx() - 2.dp.toPx(), scrollbarY),
                size = Size(width.toPx(), scrollbarHeight),
                cornerRadius = CornerRadius(width.toPx() / 2)
            )
        }
    }
}
