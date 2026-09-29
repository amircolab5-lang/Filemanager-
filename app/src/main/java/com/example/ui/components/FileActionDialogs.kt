package com.example.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.FileItem
import com.example.ui.theme.LeagueSpartanFontFamily
import com.example.ui.theme.TicnoDarkBg
import com.example.ui.theme.TicnoRose

@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "NEW FOLDER",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = LeagueSpartanFontFamily,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Folder Name") },
                    placeholder = { Text("e.g. My_Project") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_folder_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (folderName.isNotBlank()) onConfirm(folderName) },
                enabled = folderName.isNotBlank(),
                modifier = Modifier.testTag("confirm_create_folder_button")
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CreateFileDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var fileName by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "NEW FILE",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = LeagueSpartanFontFamily,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("File Name with extension") },
                    placeholder = { Text("e.g. notes.txt") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_file_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content (Optional)") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (fileName.isNotBlank()) onConfirm(fileName, content) },
                enabled = fileName.isNotBlank(),
                modifier = Modifier.testTag("confirm_create_file_button")
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RenameDialog(
    item: FileItem,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newName by remember { mutableStateOf(item.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "RENAME",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = LeagueSpartanFontFamily,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rename_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (newName.isNotBlank() && newName != item.name) onConfirm(newName) },
                enabled = newName.isNotBlank() && newName != item.name,
                modifier = Modifier.testTag("confirm_rename_button")
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteConfirmDialog(
    items: List<FileItem>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = TicnoRose,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DELETE PERMANENTLY?",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = LeagueSpartanFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = TicnoRose
                )
            }
        },
        text = {
            Column {
                Text(
                    text = if (items.size == 1) "Are you sure you want to delete \"${items[0].name}\"?"
                    else "Are you sure you want to delete ${items.size} selected items?",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = TicnoRose,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "This action cannot be undone.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = TicnoRose
                    )
                }
            }
        },
        confirmButton = {
            var isDeleting by remember { mutableStateOf(false) }
            Button(
                onClick = {
                    if (!isDeleting) {
                        isDeleting = true
                        onConfirm()
                    }
                },
                enabled = !isDeleting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TicnoRose,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_delete_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isDeleting) "Deleting..." else "Delete",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

@Composable
fun FileDetailsDialog(
    item: FileItem,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "FILE PROPERTIES",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = LeagueSpartanFontFamily,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailRow(label = "NAME", value = item.name)
                DetailRow(label = "TYPE", value = if (item.isDirectory) "Folder" else item.mimeType)
                DetailRow(label = "SIZE", value = "${item.formattedSize} (${item.size} bytes)")
                DetailRow(label = "PATH", value = item.path)
                DetailRow(label = "MODIFIED", value = item.formattedDate)
                DetailRow(label = "IS HIDDEN", value = if (item.isHidden) "Yes (Hidden from standard apps)" else "No")
                DetailRow(label = "READABLE", value = if (item.file.canRead()) "Yes" else "No")
                DetailRow(label = "WRITABLE", value = if (item.file.canWrite()) "Yes" else "No")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = LeagueSpartanFontFamily,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 10.sp
        )
        SelectionContainer {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ImageViewerDialog(
    item: FileItem,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onSaveStatus: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top control bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        maxLines = 1,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (scale > 1.05f) {
                            TextButton(
                                onClick = {
                                    scale = 1f
                                    offset = Offset.Zero
                                }
                            ) {
                                Text(
                                    text = "${(scale * 100).toInt()}% Reset",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        onSaveStatus?.let {
                            IconButton(onClick = it) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Save Status",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        IconButton(onClick = onShare) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Image display with Pinch to Zoom, Pan, and Double Tap
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clipToBounds()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (scale > 1.2f) {
                                        scale = 1f
                                        offset = Offset.Zero
                                    } else {
                                        scale = 2.5f
                                        offset = Offset.Zero
                                    }
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val newScale = (scale * zoom).coerceIn(1f, 5f)
                                scale = newScale
                                if (newScale > 1f) {
                                    val maxOffsetX = (newScale - 1f) * 600f
                                    val maxOffsetY = (newScale - 1f) * 800f
                                    offset = Offset(
                                        x = (offset.x + pan.x).coerceIn(-maxOffsetX, maxOffsetX),
                                        y = (offset.y + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                                    )
                                } else {
                                    offset = Offset.Zero
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(item.file)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                            }
                    )
                }

                // Info footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.formattedSize,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = LeagueSpartanFontFamily,
                        color = Color.LightGray
                    )
                    Text(
                        text = item.formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}

@Composable
fun TextViewerDialog(
    item: FileItem,
    content: String,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            color = TicnoDarkBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = LeagueSpartanFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                        Text(
                            text = "${item.formattedSize} • TEXT VIEWER",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = LeagueSpartanFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row {
                        IconButton(onClick = {
                            clipboardManager.setText(AnnotatedString(content))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Content",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A))
                        .padding(12.dp)
                ) {
                    SelectionContainer {
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = LeagueSpartanFontFamily,
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .horizontalScroll(rememberScrollState())
                        )
                    }
                }
            }
        }
    }
}

private fun launchGooglePlayStore(context: android.content.Context) {
    val pkg = context.packageName
    try {
        val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(playStoreIntent)
        Toast.makeText(context, "Opening Google Play Store...", Toast.LENGTH_SHORT).show()
    } catch (e: ActivityNotFoundException) {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(webIntent)
        Toast.makeText(context, "Opening Google Play Store...", Toast.LENGTH_SHORT).show()
    }
}

private fun launchAmazonAppstore(context: android.content.Context) {
    val pkg = context.packageName
    try {
        val amzIntent = Intent(Intent.ACTION_VIEW, Uri.parse("amzn://apps/android?p=$pkg")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(amzIntent)
        Toast.makeText(context, "Opening Amazon Appstore...", Toast.LENGTH_SHORT).show()
    } catch (e: ActivityNotFoundException) {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.amazon.com/gp/mas/dl/android?p=$pkg")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(webIntent)
        Toast.makeText(context, "Opening Amazon Appstore...", Toast.LENGTH_SHORT).show()
    }
}

private fun launchSamsungGalaxyStore(context: android.content.Context) {
    val pkg = context.packageName
    try {
        val samsungIntent = Intent(Intent.ACTION_VIEW, Uri.parse("samsungapps://ProductDetail/$pkg")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(samsungIntent)
        Toast.makeText(context, "Opening Samsung Galaxy Store...", Toast.LENGTH_SHORT).show()
    } catch (_: Exception) {
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://galaxystore.samsung.com/detail/$pkg")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            Toast.makeText(context, "Opening Samsung Galaxy Store...", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(context, "Could not open Samsung Galaxy Store", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun FeedbackDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    val context = LocalContext.current
    var feedbackText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Feedback & Reviews",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Help us improve Ticno File Manager. Share your feedback directly on Google Play Store, Amazon Appstore, or send an email:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    placeholder = { Text("Your thoughts or suggestions for review...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("feedback_input"),
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 4
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Email: ticnodevelopers@gmail.com\nWebsite: https://ticnodevelopers.blogspot.com/#contact",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            launchGooglePlayStore(context)
                            onSubmit(feedbackText)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).testTag("submit_feedback_playstore")
                    ) {
                        Text("Google Play", fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            launchAmazonAppstore(context)
                            onSubmit(feedbackText)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).testTag("submit_feedback_amazon")
                    ) {
                        Text("Amazon Store", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            if (feedbackText.isNotBlank()) {
                OutlinedButton(
                    onClick = {
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:ticnodevelopers@gmail.com")
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("ticnodevelopers@gmail.com"))
                            putExtra(Intent.EXTRA_SUBJECT, "Ticno File Manager - User Feedback")
                            putExtra(
                                Intent.EXTRA_TEXT,
                                """
                                User Feedback:
                                $feedbackText
                                
                                ---------------------------
                                Device: ${Build.MANUFACTURER} ${Build.MODEL}
                                Android Version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
                                App: Ticno File Manager
                                ---------------------------
                                """.trimIndent()
                            )
                        }
                        try {
                            val chooser = Intent.createChooser(emailIntent, "Send Feedback via Email")
                            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(chooser)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Please email ticnodevelopers@gmail.com", Toast.LENGTH_LONG).show()
                        }
                        onSubmit(feedbackText)
                        onDismiss()
                    }
                ) {
                    Text("Email Feedback")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RateAppDialog(
    onDismiss: () -> Unit,
    onSubmitRating: (Int) -> Unit
) {
    val context = LocalContext.current
    var selectedStars by remember { mutableStateOf(5) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Rate Ticno File Manager",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your rating helps more users discover Ticno File Manager and supports future updates!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        IconButton(
                            onClick = { selectedStars = i },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Text(
                                text = if (i <= selectedStars) "★" else "☆",
                                fontSize = 32.sp,
                                color = if (i <= selectedStars) Color(0xFFFFB300) else Color.Gray
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Choose store to submit review:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Equal-sized, uniform, fixed height buttons for all 3 official stores
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            launchGooglePlayStore(context)
                            onSubmitRating(selectedStars)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("rate_google_play_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("★ Rate on Google Play", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            launchSamsungGalaxyStore(context)
                            onSubmitRating(selectedStars)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("rate_samsung_store_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0381FE),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("★ Rate on Galaxy Store", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            launchAmazonAppstore(context)
                            onSubmitRating(selectedStars)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("rate_amazon_appstore_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9900),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("★ Rate on Amazon Store", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Maybe Later")
            }
        }
    )
}

@Composable
fun AboutDialog(
    onDismiss: () -> Unit,
    onViewPrivacyPolicy: () -> Unit = {}
) {
    val context = LocalContext.current
    val privacyPolicyUrl = "https://ticnodevelopers.blogspot.com/p/file-manager-privacy-policy.html"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "About File Manager",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Ticno File Manager v2.4.0",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "High performance Android file explorer with support for internal storage, SD card, media status saver, hidden files, space cleaner, and lightning-fast media categories.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                // Privacy Policy Link Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Privacy Policy",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "https://ticnodevelopers.blogspot.com/p/file-manager-privacy-policy.html",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Opening Privacy Policy", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Text("Open Link", fontSize = 12.sp)
                            }
                            Button(
                                onClick = onViewPrivacyPolicy,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Read Policy", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Unified Official Studio Branding: ticnodevelopers
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "developed by",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    TicnoBrandBadge()
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

/**
 * Full In-App English Privacy Policy Dialog.
 * Transparently outlines storage permissions, 100% on-device data processing, AdMob compliance, and contact info.
 */
@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val privacyPolicyUrl = "https://ticnodevelopers.blogspot.com/p/file-manager-privacy-policy.html"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Privacy Policy",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Ticno File Manager Privacy Policy",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
                Text(
                    text = "Effective Date: 2025 - 2026\nDeveloper: Ticno Developers",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "1. Commitment to Privacy",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Text(
                    text = "Ticno File Manager is designed to respect your privacy. All your personal files, documents, photos, audio, videos, and archives remain 100% on your device. We DO NOT upload, collect, monitor, or transmit any of your personal files to external servers or third parties.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "2. Device Permissions Used",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Text(
                    text = "• All Files Access (MANAGE_EXTERNAL_STORAGE): Required strictly for core file manager functionality (viewing, organizing, moving, copying, renaming, deleting, and compressing files on internal storage and SD card).\n• Notifications (POST_NOTIFICATIONS): Used only to show background file cleaning, zipping, or transfer progress.\n• Internet & Network State: Used solely by Google AdMob to deliver standard advertisements and verify network connectivity.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "3. Status Saver",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Text(
                    text = "The status saver feature accesses locally cached temporary status media already present on your device storage. Files are only copied to your permanent gallery when you explicitly tap the Save action. Ticno is an independent utility application.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "4. Third-Party Services & Ads",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Text(
                    text = "This app displays advertisements powered by Google AdMob. AdMob may collect device advertising IDs (GAID) according to Google Play Developer Policies to display relevant ads. No personal files are ever shared with advertisers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "5. Contact & Support",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Text(
                    text = "Developer Contact: ticnodevelopers@gmail.com\nSupport & Contact: https://ticnodevelopers.blogspot.com/#contact\nWebsite Policy: https://ticnodevelopers.blogspot.com/p/file-manager-privacy-policy.html",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Link: https://ticnodevelopers.blogspot.com/p/file-manager-privacy-policy.html", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Open Online Link")
            }
        }
    )
}

@Composable
fun CollectionAddDialog(
    onDismiss: () -> Unit,
    onAddCollection: (String) -> Unit
) {
    var collectionName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Collection",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Create a collection of documents to easily organize your work:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = collectionName,
                    onValueChange = { collectionName = it },
                    placeholder = { Text("e.g. Work Invoices, Receipts") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (collectionName.isNotBlank()) {
                        onAddCollection(collectionName)
                    }
                    onDismiss()
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

