package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.FileItem
import com.example.ui.components.AdMobNativeAdCard
import com.example.ui.components.getFileIcon
import com.example.ui.components.getFileIconBackground
import com.example.ui.components.getFileIconColor
import com.example.ui.theme.LeagueSpartanFontFamily
import com.example.ui.theme.TicnoNeonGreen
import com.example.util.FileManagerHelper
import com.example.util.THideManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private val THideVaultPurple = Color(0xFFA855F7) // Ultra-vibrant teez electric purple (high contrast, bright & sharp)
private val THideVaultDarkPurple = Color(0xFF7C3AED)

enum class THideFilter {
    ALL,
    PHOTOS,
    VIDEOS,
    AUDIO,
    DOCUMENTS,
    OTHER
}

/**
 * Complete T-Hide Private Vault Screen.
 * - Password/PIN protection
 * - Fast setup with security question
 * - Encrypted hidden file storage with .nomedia
 * - Move to vault / Restore from vault
 * - Direct secure preview inside vault
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun THideVaultScreen(
    hiddenFiles: List<FileItem>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onOpenFile: (FileItem) -> Unit,
    onUnhideFile: (FileItem) -> Unit,
    onShareFile: (FileItem) -> Unit,
    onDeleteFile: (FileItem) -> Unit,
    onHideFiles: (List<File>) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isUnlocked by remember { mutableStateOf(THideManager.isUnlocked) }
    var isPinConfigured by remember { mutableStateOf(THideManager.isPinConfigured(context)) }

    var selectedFilter by remember { mutableStateOf(THideFilter.ALL) }
    var showFilePickerSheet by remember { mutableStateOf(false) }
    var showForgotPinDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (isUnlocked) {
            onRefresh()
        }
    }

    if (!isUnlocked) {
        if (!isPinConfigured) {
            // First time PIN setup
            THideSetupPinView(
                onBack = onBack,
                onPinConfigured = {
                    isPinConfigured = true
                    isUnlocked = true
                    onRefresh()
                }
            )
        } else {
            // Unlock with PIN
            THideUnlockPinView(
                onBack = onBack,
                onUnlocked = {
                    isUnlocked = true
                    onRefresh()
                },
                onForgotPin = {
                    showForgotPinDialog = true
                }
            )

            if (showForgotPinDialog) {
                THideForgotPinDialog(
                    onDismiss = { showForgotPinDialog = false },
                    onPinReset = {
                        showForgotPinDialog = false
                        isUnlocked = true
                        onRefresh()
                    }
                )
            }
        }
        return
    }

    // Unlocked Vault View
    val filteredFiles = remember(hiddenFiles, selectedFilter) {
        when (selectedFilter) {
            THideFilter.ALL -> hiddenFiles
            THideFilter.PHOTOS -> hiddenFiles.filter { it.isImage }
            THideFilter.VIDEOS -> hiddenFiles.filter { it.isVideo }
            THideFilter.AUDIO -> hiddenFiles.filter { it.isAudio }
            THideFilter.DOCUMENTS -> hiddenFiles.filter { it.isDocument }
            THideFilter.OTHER -> hiddenFiles.filter { !it.isImage && !it.isVideo && !it.isAudio && !it.isDocument }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showFilePickerSheet = true },
                containerColor = THideVaultPurple,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("thide_add_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Hide Files")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Hide Files",
                        fontFamily = LeagueSpartanFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Vault Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(THideVaultPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        val isDarkTheme = MaterialTheme.colorScheme.surface.red < 0.5f
                        val tHideHeadingColor = if (isDarkTheme) THideVaultPurple else Color(0xFF6B21A8)
                        Column {
                            Text(
                                text = "T-HIDE VAULT",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = LeagueSpartanFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = tHideHeadingColor
                            )
                            Text(
                                text = "${hiddenFiles.size} hidden items protected",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = LeagueSpartanFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onRefresh) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = {
                                THideManager.lock()
                                isUnlocked = false
                            },
                            modifier = Modifier.testTag("thide_lock_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock Vault",
                                tint = THideVaultPurple
                            )
                        }
                    }
                }
            }

            // Category Filter Scroll
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                THideFilter.values().forEach { filter ->
                    val count = when (filter) {
                        THideFilter.ALL -> hiddenFiles.size
                        THideFilter.PHOTOS -> hiddenFiles.count { it.isImage }
                        THideFilter.VIDEOS -> hiddenFiles.count { it.isVideo }
                        THideFilter.AUDIO -> hiddenFiles.count { it.isAudio }
                        THideFilter.DOCUMENTS -> hiddenFiles.count { it.isDocument }
                        THideFilter.OTHER -> hiddenFiles.count { !it.isImage && !it.isVideo && !it.isAudio && !it.isDocument }
                    }
                    val label = when (filter) {
                        THideFilter.ALL -> "All"
                        THideFilter.PHOTOS -> "Photos"
                        THideFilter.VIDEOS -> "Videos"
                        THideFilter.AUDIO -> "Audio"
                        THideFilter.DOCUMENTS -> "Docs"
                        THideFilter.OTHER -> "Other"
                    }

                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = "$label ($count)",
                                fontFamily = LeagueSpartanFontFamily,
                                fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = THideVaultPurple.copy(alpha = 0.15f),
                            selectedLabelColor = THideVaultPurple
                        )
                    )
                }
            }

            // Real AdMob Banner if available
            AdMobNativeAdCard(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Content List or Empty State
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = THideVaultPurple)
                }
            } else if (filteredFiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(THideVaultPurple.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = THideVaultPurple,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (selectedFilter == THideFilter.ALL) "T-Hide Vault is Empty" else "No $selectedFilter files hidden",
                            fontFamily = LeagueSpartanFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Hide your personal photos, videos, audios, and documents securely away from the gallery and other apps.",
                            fontFamily = LeagueSpartanFontFamily,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(0.85f)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { showFilePickerSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = THideVaultPurple),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Select Files to Hide",
                                fontFamily = LeagueSpartanFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredFiles, key = { it.path }) { item ->
                        THideVaultFileItem(
                            item = item,
                            onOpen = { onOpenFile(item) },
                            onUnhide = { onUnhideFile(item) },
                            onShare = { onShareFile(item) },
                            onDelete = { onDeleteFile(item) }
                        )
                    }
                }
            }
        }
    }

    // Modal File Picker Bottom Sheet
    if (showFilePickerSheet) {
        THideFilePickerBottomSheet(
            onDismiss = { showFilePickerSheet = false },
            onFilesSelected = { selectedFiles ->
                showFilePickerSheet = false
                onHideFiles(selectedFiles)
            }
        )
    }
}

/**
 * File card item inside T-Hide Vault.
 */
@Composable
private fun THideVaultFileItem(
    item: FileItem,
    onOpen: () -> Unit,
    onUnhide: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            0.8.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail or icon
            if (item.isImage) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.file)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                val iconBg = getFileIconBackground(item)
                val iconTint = getFileIconColor(item)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getFileIcon(item),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = LeagueSpartanFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.formattedSize,
                        fontSize = 11.sp,
                        fontFamily = LeagueSpartanFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " • ",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = item.formattedDate,
                        fontSize = 11.sp,
                        fontFamily = LeagueSpartanFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick Unhide Button
            OutlinedButton(
                onClick = onUnhide,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = THideVaultPurple),
                border = androidx.compose.foundation.BorderStroke(1.dp, THideVaultPurple.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = "Unhide",
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Unhide",
                    fontSize = 11.sp,
                    fontFamily = LeagueSpartanFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onShare) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Numeric keypad PIN verification screen for T-Hide.
 */
@Composable
private fun THideUnlockPinView(
    onBack: () -> Unit,
    onUnlocked: () -> Unit,
    onForgotPin: () -> Unit
) {
    val context = LocalContext.current
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    fun handleDigit(digit: String) {
        if (enteredPin.length < 4) {
            isError = false
            val next = enteredPin + digit
            enteredPin = next
            if (next.length == 4) {
                val ok = THideManager.verifyPin(context, next)
                if (ok) {
                    onUnlocked()
                } else {
                    isError = true
                    enteredPin = ""
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(THideVaultPurple.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = THideVaultPurple,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "T-Hide Vault",
            fontFamily = LeagueSpartanFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isError) "Incorrect PIN! Please try again." else "Enter your 4-digit PIN to access private files",
            fontFamily = LeagueSpartanFontFamily,
            fontSize = 13.sp,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 4 PIN Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0..3) {
                val filled = i < enteredPin.length
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (isError) MaterialTheme.colorScheme.error
                            else if (filled) THideVaultPurple
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Keypad
        NumericKeypad(
            onDigitClick = { handleDigit(it) },
            onBackspaceClick = {
                if (enteredPin.isNotEmpty()) {
                    isError = false
                    enteredPin = enteredPin.dropLast(1)
                }
            },
            onClearClick = {
                enteredPin = ""
                isError = false
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onForgotPin) {
            Text(
                text = "Forgot PIN?",
                fontFamily = LeagueSpartanFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = THideVaultPurple,
                fontSize = 13.sp
            )
        }
    }
}

/**
 * Setup PIN view when opening T-Hide for the first time.
 */
@Composable
private fun THideSetupPinView(
    onBack: () -> Unit,
    onPinConfigured: () -> Unit
) {
    val context = LocalContext.current
    var step by remember { mutableStateOf(1) } // 1: Enter PIN, 2: Confirm PIN, 3: Security Question
    var firstPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var securityQuestion by remember { mutableStateOf("What is your secret recovery word / nickname?") }
    var securityAnswer by remember { mutableStateOf("") }

    fun handleDigit(digit: String) {
        if (step == 1) {
            if (firstPin.length < 4) {
                firstPin += digit
                errorMessage = null
                if (firstPin.length == 4) {
                    step = 2
                }
            }
        } else if (step == 2) {
            if (confirmPin.length < 4) {
                confirmPin += digit
                errorMessage = null
                if (confirmPin.length == 4) {
                    if (confirmPin == firstPin) {
                        step = 3
                    } else {
                        errorMessage = "PINs do not match. Please try again."
                        confirmPin = ""
                        firstPin = ""
                        step = 1
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(THideVaultPurple.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = THideVaultPurple,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = when (step) {
                1 -> "Set T-Hide PIN"
                2 -> "Confirm Your PIN"
                else -> "Security Recovery"
            },
            fontFamily = LeagueSpartanFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = errorMessage ?: when (step) {
                1 -> "Create a 4-digit PIN to secure your private files"
                2 -> "Re-enter the 4-digit PIN to verify"
                else -> "Set an answer to recover your vault if you ever forget your PIN"
            },
            fontFamily = LeagueSpartanFontFamily,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = if (errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (step in 1..2) {
            val currentEntry = if (step == 1) firstPin else confirmPin
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0..3) {
                    val filled = i < currentEntry.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (filled) THideVaultPurple
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            NumericKeypad(
                onDigitClick = { handleDigit(it) },
                onBackspaceClick = {
                    if (step == 1 && firstPin.isNotEmpty()) firstPin = firstPin.dropLast(1)
                    if (step == 2 && confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                },
                onClearClick = {
                    if (step == 1) firstPin = ""
                    if (step == 2) confirmPin = ""
                }
            )
        } else {
            // Step 3: Security Question & Answer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                OutlinedTextField(
                    value = securityQuestion,
                    onValueChange = { securityQuestion = it },
                    label = { Text("Security Question") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = securityAnswer,
                    onValueChange = { securityAnswer = it },
                    label = { Text("Secret Answer") },
                    placeholder = { Text("Enter your recovery answer") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (securityAnswer.isNotBlank()) {
                            THideManager.setupPin(
                                context = context,
                                pin = firstPin,
                                securityQuestion = securityQuestion,
                                securityAnswer = securityAnswer
                            )
                            onPinConfigured()
                        }
                    },
                    enabled = securityAnswer.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = THideVaultPurple),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Complete T-Hide Setup",
                        fontFamily = LeagueSpartanFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Numeric keypad for PIN entry.
 */
@Composable
private fun NumericKeypad(
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onClearClick: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("C", "0", "DEL")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(0.8f)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable {
                                when (key) {
                                    "C" -> onClearClick()
                                    "DEL" -> onBackspaceClick()
                                    else -> onDigitClick(key)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (key == "DEL") {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Backspace",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text(
                                text = key,
                                fontSize = 22.sp,
                                fontFamily = LeagueSpartanFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Forgot PIN Dialog.
 */
@Composable
private fun THideForgotPinDialog(
    onDismiss: () -> Unit,
    onPinReset: () -> Unit
) {
    val context = LocalContext.current
    val question = remember { THideManager.getSecurityQuestion(context) }
    var answer by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmNewPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.LockReset, contentDescription = null, tint = THideVaultPurple)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset T-Hide PIN", fontFamily = LeagueSpartanFontFamily, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    text = question,
                    fontFamily = LeagueSpartanFontFamily,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it; error = null },
                    label = { Text("Your Answer") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newPin = it; error = null },
                    label = { Text("New 4-digit PIN") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = confirmNewPin,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) confirmNewPin = it; error = null },
                    label = { Text("Confirm New PIN") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPin.length != 4) {
                        error = "PIN must be exactly 4 digits."
                        return@Button
                    }
                    if (newPin != confirmNewPin) {
                        error = "New PINs do not match."
                        return@Button
                    }
                    val ok = THideManager.resetPinWithSecurityAnswer(context, answer, newPin)
                    if (ok) {
                        onPinReset()
                    } else {
                        error = "Incorrect security answer."
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = THideVaultPurple)
            ) {
                Text("Reset & Unlock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Full file selection window for T-Hide Vault.
 * Allows user to navigate any folder in storage, or select by category,
 * with multi-select to hide any file in the system.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun THideFilePickerBottomSheet(
    onDismiss: () -> Unit,
    onFilesSelected: (List<File>) -> Unit
) {
    val context = LocalContext.current
    var pickerMode by remember { mutableStateOf(0) } // 0: Browse Folders, 1: Categories
    var currentDir by remember { mutableStateOf(android.os.Environment.getExternalStorageDirectory()) }
    var folderFiles by remember { mutableStateOf<List<FileItem>>(emptyList()) }
    var categoryFiles by remember { mutableStateOf<List<FileItem>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf(com.example.model.CategoryType.IMAGES) }
    var isLoading by remember { mutableStateOf(true) }
    val selectedPaths = remember { mutableStateListOf<String>() }

    // Load directory files
    LaunchedEffect(currentDir, pickerMode) {
        if (pickerMode == 0) {
            isLoading = true
            withContext(Dispatchers.IO) {
                folderFiles = FileManagerHelper.listFiles(currentDir)
                isLoading = false
            }
        }
    }

    // Load category files
    LaunchedEffect(selectedCategory, pickerMode) {
        if (pickerMode == 1) {
            isLoading = true
            withContext(Dispatchers.IO) {
                categoryFiles = FileManagerHelper.scanCategoryFiles(context, selectedCategory)
                isLoading = false
            }
        }
    }

    val displayFiles = if (pickerMode == 0) folderFiles else categoryFiles

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Select Files to Hide",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = LeagueSpartanFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${selectedPaths.size} selected to hide securely",
                        fontSize = 12.sp,
                        fontFamily = LeagueSpartanFontFamily,
                        color = THideVaultPurple
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (selectedPaths.isNotEmpty()) {
                        TextButton(onClick = { selectedPaths.clear() }) {
                            Text(
                                text = "Clear",
                                fontSize = 12.sp,
                                fontFamily = LeagueSpartanFontFamily,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val selectedFileList = selectedPaths.map { File(it) }.filter { it.exists() }
                            if (selectedFileList.isNotEmpty()) {
                                onFilesSelected(selectedFileList)
                            }
                        },
                        enabled = selectedPaths.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = THideVaultPurple),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Hide (${selectedPaths.size})",
                            fontFamily = LeagueSpartanFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mode Selector: Browse Folders vs Categories
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = pickerMode == 0,
                    onClick = { pickerMode = 0 },
                    label = {
                        Text(
                            text = "Browse Folders",
                            fontFamily = LeagueSpartanFontFamily,
                            fontWeight = if (pickerMode == 0) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = THideVaultPurple.copy(alpha = 0.2f),
                        selectedLabelColor = THideVaultPurple
                    )
                )

                FilterChip(
                    selected = pickerMode == 1,
                    onClick = { pickerMode = 1 },
                    label = {
                        Text(
                            text = "By Category",
                            fontFamily = LeagueSpartanFontFamily,
                            fontWeight = if (pickerMode == 1) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.InsertDriveFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = THideVaultPurple.copy(alpha = 0.2f),
                        selectedLabelColor = THideVaultPurple
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mode 0: Folder Path Breadcrumb & Up Navigation
            if (pickerMode == 0) {
                val isRoot = currentDir.parentFile == null || currentDir.path == "/storage/emulated/0"
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isRoot) {
                            IconButton(
                                onClick = {
                                    val parent = currentDir.parentFile
                                    if (parent != null && parent.canRead()) {
                                        currentDir = parent
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Up",
                                    tint = THideVaultPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = THideVaultPurple,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isRoot) "Internal Storage" else currentDir.name,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = LeagueSpartanFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                // Mode 1: Category Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        com.example.model.CategoryType.IMAGES to "Photos",
                        com.example.model.CategoryType.VIDEOS to "Videos",
                        com.example.model.CategoryType.AUDIO to "Audio",
                        com.example.model.CategoryType.DOCUMENTS to "Documents",
                        com.example.model.CategoryType.DOWNLOADS to "Downloads",
                        com.example.model.CategoryType.ARCHIVES to "Archives"
                    ).forEach { (cat, title) ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontFamily = LeagueSpartanFontFamily,
                                    fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = THideVaultPurple.copy(alpha = 0.2f),
                                selectedLabelColor = THideVaultPurple
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // File / Folder List
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = THideVaultPurple)
                }
            } else if (displayFiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (pickerMode == 0) "No files or folders in this directory" else "No files found in this category",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = LeagueSpartanFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(displayFiles, key = { it.path }) { item ->
                        if (item.isDirectory) {
                            // Folder item to open
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { currentDir = item.file },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(THideVaultPurple.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = THideVaultPurple,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontFamily = LeagueSpartanFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Folder • Tap to open",
                                            fontSize = 11.sp,
                                            fontFamily = LeagueSpartanFontFamily,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            // Selectable File item
                            val isChecked = selectedPaths.contains(item.path)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked) selectedPaths.remove(item.path)
                                        else selectedPaths.add(item.path)
                                    },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isChecked) THideVaultPurple.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .border(
                                                1.5.dp,
                                                if (isChecked) THideVaultPurple else MaterialTheme.colorScheme.outline,
                                                CircleShape
                                            )
                                            .background(if (isChecked) THideVaultPurple else Color.Transparent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isChecked) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontFamily = LeagueSpartanFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${item.formattedSize} • ${item.formattedDate}",
                                            fontSize = 11.sp,
                                            fontFamily = LeagueSpartanFontFamily,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
