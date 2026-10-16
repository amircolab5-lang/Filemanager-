package com.example.ui.screens

import android.content.Context
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
import com.example.ui.components.tvListScrollbar
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

private val THideVaultPurple = Color(0xFF7C3AED) // Rich vibrant purple (bold and AAA high contrast)
private val THideVaultDarkPurple = Color(0xFF6D28D9)

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
    val isDarkTheme = MaterialTheme.colorScheme.surface.red < 0.5f
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
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Hide Files", tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Hide Files",
                        fontFamily = LeagueSpartanFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = Color.White
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
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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

                        val tHideHeadingColor = if (isDarkTheme) Color(0xFFC084FC) else Color(0xFF581C87)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "T-HIDE VAULT",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = tHideHeadingColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${hiddenFiles.size} hidden items protected",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) Color(0xFFCBD5E1) else Color(0xFF334155),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onRefresh) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = if (isDarkTheme) Color(0xFFCBD5E1) else Color(0xFF334155)
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
                                tint = if (isDarkTheme) Color(0xFFC084FC) else Color(0xFF6B21A8)
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
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isDarkTheme) Color(0xFF7C3AED) else Color(0xFF6B21A8),
                            selectedLabelColor = Color.White,
                            containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                            labelColor = if (isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                        )
                    )
                }
            }

            val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
            val listState = rememberLazyListState()

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
                            colors = ButtonDefaults.buttonColors(
                                containerColor = THideVaultPurple,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Select Files to Hide",
                                fontFamily = LeagueSpartanFontFamily,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .tvListScrollbar(listState),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isLandscape) {
                        item {
                            AdMobNativeAdCard(
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    items(filteredFiles, key = { it.path }) { item ->
                        THideVaultFileItem(
                            item = item,
                            onOpen = { onOpenFile(item) },
                            onUnhide = { onUnhideFile(item) },
                            onShare = { onShareFile(item) },
                            onDelete = { onDeleteFile(item) }
                        )
                    }

                    if (isLandscape) {
                        item {
                            AdMobNativeAdCard(
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    val iconBg = getFileIconBackground(item)
                    val iconTint = getFileIconColor(item)
                    Box(
                        modifier = Modifier
                            .size(46.dp)
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

                val isDarkTheme = MaterialTheme.colorScheme.surface.red < 0.5f
                val itemTextColor = if (isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                val itemSubtextColor = if (isDarkTheme) Color(0xFFCBD5E1) else Color(0xFF334155)

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = itemTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${item.formattedSize}  •  ${item.formattedDate}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = itemSubtextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val isDarkTheme = MaterialTheme.colorScheme.surface.red < 0.5f

            // Action row below file info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onUnhide,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkTheme) Color(0xFF9333EA) else Color(0xFF7C3AED),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = "Unhide",
                        modifier = Modifier.size(15.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Unhide",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp)
                ) {
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

    val scrollState = rememberScrollState()
    val isDark = MaterialTheme.colorScheme.surface.red < 0.5f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(80.dp)
                .shadow(elevation = 6.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFFA855F7)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "T-Hide Private Vault",
            fontFamily = LeagueSpartanFontFamily,
            fontWeight = FontWeight.Black,
            fontSize = 26.sp,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isError) "Incorrect PIN! Please try again." else "Enter your 4-digit PIN to access private files",
            fontFamily = LeagueSpartanFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = if (isError) Color(0xFFEF4444) else if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 4 PIN Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0..3) {
                val filled = i < enteredPin.length
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(
                            if (isError) Color(0xFFEF4444)
                            else if (filled) Color(0xFFA855F7)
                            else if (isDark) Color(0xFF1E293B)
                            else Color(0xFFF1F5F9)
                        )
                        .border(
                            width = 2.dp,
                            color = if (isError) Color(0xFFDC2626)
                            else if (filled) Color(0xFF7C3AED)
                            else if (isDark) Color(0xFF64748B)
                            else Color(0xFF475569),
                            shape = CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onForgotPin) {
            Text(
                text = "Forgot PIN?",
                fontFamily = LeagueSpartanFontFamily,
                fontWeight = FontWeight.Black,
                color = if (isDark) Color(0xFFD8B4FE) else Color(0xFF6D28D9),
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(60.dp))
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

    val scrollState = rememberScrollState()
    val isDark = MaterialTheme.colorScheme.surface.red < 0.5f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(80.dp)
                .shadow(elevation = 6.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFFA855F7)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
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
            fontWeight = FontWeight.Black,
            fontSize = 26.sp,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = errorMessage ?: when (step) {
                1 -> "Create a 4-digit PIN to secure your private files"
                2 -> "Re-enter the 4-digit PIN to verify"
                else -> "Set an answer to recover your vault if you ever forget your PIN"
            },
            fontFamily = LeagueSpartanFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = if (errorMessage != null) Color(0xFFEF4444) else if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (step in 1..2) {
            val currentEntry = if (step == 1) firstPin else confirmPin
            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0..3) {
                    val filled = i < currentEntry.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                if (filled) Color(0xFFA855F7)
                                else if (isDark) Color(0xFF1E293B)
                                else Color(0xFFF1F5F9)
                            )
                            .border(
                                width = 2.dp,
                                color = if (filled) Color(0xFF7C3AED)
                                else if (isDark) Color(0xFF64748B)
                                else Color(0xFF475569),
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

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

            Spacer(modifier = Modifier.height(60.dp))
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = THideVaultPurple,
                        contentColor = Color.White,
                        disabledContainerColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
                        disabledContentColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Complete T-Hide Setup",
                        fontFamily = LeagueSpartanFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(60.dp))
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
    val isDark = MaterialTheme.colorScheme.surface.red < 0.5f
    val buttonBg = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)
    val buttonBorder = if (isDark) Color(0xFF475569) else Color(0xFF94A3B8)
    val textColor = if (isDark) Color.White else Color(0xFF0F172A)

    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("C", "0", "DEL")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(0.85f)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .shadow(elevation = if (isDark) 0.dp else 2.5.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(buttonBg)
                            .border(1.5.dp, buttonBorder, CircleShape)
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
                                tint = if (isDark) Color(0xFFF87171) else Color(0xFFB91C1C),
                                modifier = Modifier.size(26.dp)
                            )
                        } else if (key == "C") {
                            Text(
                                text = "C",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDark) Color(0xFFF87171) else Color(0xFFB91C1C)
                            )
                        } else {
                            Text(
                                text = key,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = textColor
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
    val isDark = MaterialTheme.colorScheme.surface.red < 0.5f

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
                colors = ButtonDefaults.buttonColors(
                    containerColor = THideVaultPurple,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Reset & Unlock",
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF0F172A)
                )
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

    val isDarkTheme = MaterialTheme.colorScheme.surface.red < 0.5f
    val accentPurple = if (isDarkTheme) Color(0xFFC084FC) else Color(0xFF6B21A8)
    val buttonPurple = if (isDarkTheme) Color(0xFF7C3AED) else Color(0xFF6B21A8)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
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
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${selectedPaths.size} selected to hide securely",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentPurple
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (selectedPaths.isNotEmpty()) {
                        TextButton(onClick = { selectedPaths.clear() }) {
                            Text(
                                text = "Clear",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = THideVaultPurple,
                            contentColor = Color.White,
                            disabledContainerColor = if (isDarkTheme) Color(0xFF334155) else Color(0xFFCBD5E1),
                            disabledContentColor = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Hide (${selectedPaths.size})",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = Color.White
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
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (isDarkTheme) Color(0xFF7C3AED) else Color(0xFF6B21A8),
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White,
                        containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                        labelColor = if (isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF0F172A),
                        iconColor = if (isDarkTheme) Color(0xFFC084FC) else Color(0xFF6B21A8)
                    )
                )

                FilterChip(
                    selected = pickerMode == 1,
                    onClick = { pickerMode = 1 },
                    label = {
                        Text(
                            text = "By Category",
                            fontFamily = LeagueSpartanFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.InsertDriveFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (isDarkTheme) Color(0xFF7C3AED) else Color(0xFF6B21A8),
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White,
                        containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                        labelColor = if (isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF0F172A),
                        iconColor = if (isDarkTheme) Color(0xFFC084FC) else Color(0xFF6B21A8)
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
                    color = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0))
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
                                    tint = accentPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = accentPurple,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isRoot) "Internal Storage" else currentDir.name,
                            style = MaterialTheme.typography.bodyMedium,
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
                                    fontSize = 12.sp,
                                    fontFamily = LeagueSpartanFontFamily,
                                    fontWeight = FontWeight.Black
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (isDarkTheme) Color(0xFF7C3AED) else Color(0xFF6B21A8),
                                selectedLabelColor = Color.White,
                                containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                                labelColor = if (isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF0F172A)
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
                                    containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF8FAFC)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)
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
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isDarkTheme) Color(0xFF3B0764) else Color(0xFFF3E8FF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = if (isDarkTheme) Color(0xFFC084FC) else Color(0xFF7C3AED),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF0F172A),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Folder • Tap to open",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF475569)
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
                                    containerColor = if (isChecked) (if (isDarkTheme) Color(0xFF3B0764) else Color(0xFFF3E8FF))
                                    else (if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF8FAFC))
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isChecked) buttonPurple
                                    else (if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0))
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
                                                if (isChecked) buttonPurple else (if (isDarkTheme) Color(0xFF64748B) else Color(0xFF94A3B8)),
                                                CircleShape
                                            )
                                            .background(if (isChecked) buttonPurple else Color.Transparent),
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
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF0F172A),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${item.formattedSize} • ${item.formattedDate}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF475569)
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
