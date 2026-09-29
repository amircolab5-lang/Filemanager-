package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FileItem

/**
 * 2-Column Grid Folder Card matching the user reference screenshot (Downloads screen).
 * Compact, sleek rectangular card (~48dp height) with muted folder icon and truncated folder name.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderCard(
    folderItem: FileItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onLongClick: (() -> Unit)? = null
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val borderColor = if (isSelected) {
        primaryColor
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }

    val backgroundColor = if (isSelected) {
        primaryColor.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }

    val iconTint = if (isSelected) {
        primaryColor
    } else {
        Color(0xFF8E95A5) // Sleek slate-grey folder icon matching screenshot
    }

    Surface(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(9.dp))
            .testTag("folder_card_${folderItem.name.replace(" ", "_")}")
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(9.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(26.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = folderItem.name,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (isSelectionMode) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = if (isSelected) "Selected" else "Not Selected",
                    tint = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
