package com.example.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AdMobNativeAdCard

/**
 * Professional Settings Screen matching WhatsApp Status Saver settings layout (clean grouped cards,
 * crisp contrast, bold readable text, and scrollable container that never overlaps system navigation bars).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onFeedbackClick: () -> Unit,
    onRateClick: () -> Unit,
    onAboutClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit = onAboutClick,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    isOnlyPortrait: Boolean = false,
    onToggleOrientationLock: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(26.dp),
                            tint = MaterialTheme.colorScheme.onBackground
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
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Group 1: Display & Orientation Card (matching WhatsApp Status Saver settings cards)
            Text(
                text = "Preferences",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp, top = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkMode) 0.dp else 1.5.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isDarkMode) Color(0xFF2C3341) else Color(0xFFE2E8F0)
                )
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                    // 1. Theme Mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleDarkMode(!isDarkMode) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.linearGradient(
                                            if (isDarkMode) listOf(Color(0xFF334155), Color(0xFF1E293B))
                                            else listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = "Theme Mode",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = "Theme mode",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isDarkMode) "Dark mode (Active)" else "Light mode (Active)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        GlossyGreenThemeSwitch(
                            checked = isDarkMode,
                            onCheckedChange = onToggleDarkMode,
                            modifier = Modifier.testTag("settings_theme_switch")
                        )
                    }

                    HorizontalDivider(
                        color = if (isDarkMode) Color(0xFF2C3341) else Color(0xFFF1F5F9),
                        thickness = 1.dp
                    )

                    // 2. Screen Orientation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleOrientationLock(!isOnlyPortrait) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.linearGradient(
                                            if (isOnlyPortrait) listOf(Color(0xFF6366F1), Color(0xFF4338CA))
                                            else listOf(Color(0xFF0EA5E9), Color(0xFF0284C7))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isOnlyPortrait) Icons.Default.StayCurrentPortrait else Icons.Default.ScreenRotation,
                                    contentDescription = "Screen Orientation",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = "Screen orientation",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isOnlyPortrait) "Only portrait (Vertical locked)" else "Portrait + Landscape (Auto-rotate)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        GlossyOrientationSwitch(
                            checked = isOnlyPortrait,
                            onCheckedChange = onToggleOrientationLock,
                            modifier = Modifier.testTag("settings_orientation_switch")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Group 2: Support & About Card (clean list items with arrows matching WhatsApp Status Saver)
            Text(
                text = "Support & Legal",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkMode) 0.dp else 1.5.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isDarkMode) Color(0xFF2C3341) else Color(0xFFE2E8F0)
                )
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                    // Feedback
                    ProfessionalSettingsRow(
                        icon = Icons.Default.Feedback,
                        iconTint = Color(0xFF3B82F6),
                        title = "Feedback",
                        subtitle = "Send suggestions or report an issue",
                        onClick = onFeedbackClick,
                        testTag = "settings_feedback_row"
                    )

                    HorizontalDivider(
                        color = if (isDarkMode) Color(0xFF2C3341) else Color(0xFFF1F5F9),
                        thickness = 1.dp
                    )

                    // 5-Star Rate
                    ProfessionalSettingsRow(
                        icon = Icons.Default.ThumbUp,
                        iconTint = Color(0xFFF59E0B),
                        title = "5-star rate",
                        subtitle = "Support us with your valuable rating",
                        onClick = onRateClick,
                        testTag = "settings_rate_row"
                    )

                    HorizontalDivider(
                        color = if (isDarkMode) Color(0xFF2C3341) else Color(0xFFF1F5F9),
                        thickness = 1.dp
                    )

                    // About
                    ProfessionalSettingsRow(
                        icon = Icons.Default.Info,
                        iconTint = Color(0xFF10B981),
                        title = "About",
                        subtitle = "Ticno File Manager v2.4.3",
                        onClick = onAboutClick,
                        testTag = "settings_about_row"
                    )

                    HorizontalDivider(
                        color = if (isDarkMode) Color(0xFF2C3341) else Color(0xFFF1F5F9),
                        thickness = 1.dp
                    )

                    // Privacy Policy
                    ProfessionalSettingsRow(
                        icon = Icons.Default.Policy,
                        iconTint = Color(0xFF8B5CF6),
                        title = "Privacy Policy",
                        subtitle = "Read our 100% on-device privacy policy",
                        onClick = onPrivacyPolicyClick,
                        testTag = "settings_privacy_policy_row"
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Group 3: AdMob Native Card
            AdMobNativeAdCard(
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Group 4: Official Studio Branding
            com.example.ui.components.TicnoBrandBadge(
                isDark = isDarkMode,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            // Extra generous bottom padding to guarantee zero overlap with phone's 3-button navigation bar
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

/**
 * Custom Monochrome Glossy Theme Toggle Button.
 * Clean, high contrast: dark charcoal and white in Dark Mode, crisp soft-slate and white in Light Mode.
 * Fully crash-proof with non-negative clamped thumb offsets.
 */
@Composable
fun GlossyGreenThemeSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 28.dp else 3.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "ThemeThumbOffset"
    )

    // Crisp high-contrast neutral gradients: dark slate/charcoal for active Dark Mode, soft clean slate for Light Mode
    val trackColors = if (checked) {
        listOf(
            Color(0xFF0F172A), // Deep obsidian slate
            Color(0xFF1E293B), // Charcoal gradient
            Color(0xFF334155)  // Polished slate highlight
        )
    } else {
        listOf(
            Color(0xFFF1F5F9), // Clean soft light track
            Color(0xFFE2E8F0), // Smooth silver gradient fill
            Color(0xFFCBD5E1)  // Clean slate edge
        )
    }

    val borderColors = if (checked) {
        listOf(Color(0xFF64748B), Color(0xFF334155))
    } else {
        listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8))
    }

    Box(
        modifier = modifier
            .width(62.dp)
            .height(34.dp)
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(17.dp))
            .clip(RoundedCornerShape(17.dp))
            .background(brush = Brush.horizontalGradient(colors = trackColors))
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(colors = borderColors),
                shape = RoundedCornerShape(17.dp)
            )
            .clickable { onCheckedChange(!checked) }
            .testTag("theme_mode_switch"),
        contentAlignment = Alignment.CenterStart
    ) {
        // Upper glass highlight for authentic glossy shine
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(15.dp)
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(topStart = 17.dp, topEnd = 17.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (checked) 0.25f else 0.60f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                )
        )

        // Sliding pure white circular knob / thumb
        Box(
            modifier = Modifier
                .padding(start = thumbOffset.coerceAtLeast(0.dp))
                .size(26.dp)
                .shadow(elevation = 4.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFF8FAFC),
                            Color(0xFFF1F5F9)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = if (checked) Color(0xFF94A3B8).copy(alpha = 0.6f) else Color(0xFFCBD5E1),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (checked) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = null,
                tint = if (checked) Color(0xFF0F172A) else Color(0xFFF59E0B),
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

/**
 * Custom Glossy Orientation Toggle Button matching GlossyGreenThemeSwitch.
 * Checked = Only Portrait (Vertical locked)
 * Unchecked = Portrait + Landscape (Auto-rotate)
 */
@Composable
fun GlossyOrientationSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 28.dp else 3.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "OrientationThumbOffset"
    )

    val trackColors = if (checked) {
        listOf(
            Color(0xFF312E81), // Indigo slate
            Color(0xFF4338CA), // Vibrant indigo
            Color(0xFF6366F1)  // Polished glow
        )
    } else {
        listOf(
            Color(0xFFF1F5F9), // Clean soft light track
            Color(0xFFE2E8F0), // Smooth silver gradient fill
            Color(0xFFCBD5E1)  // Clean slate edge
        )
    }

    val borderColors = if (checked) {
        listOf(Color(0xFF818CF8), Color(0xFF4F46E5))
    } else {
        listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8))
    }

    Box(
        modifier = modifier
            .width(62.dp)
            .height(34.dp)
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(17.dp))
            .clip(RoundedCornerShape(17.dp))
            .background(brush = Brush.horizontalGradient(colors = trackColors))
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(colors = borderColors),
                shape = RoundedCornerShape(17.dp)
            )
            .clickable { onCheckedChange(!checked) }
            .testTag("orientation_mode_switch"),
        contentAlignment = Alignment.CenterStart
    ) {
        // Upper glass highlight for authentic glossy shine
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(15.dp)
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(topStart = 17.dp, topEnd = 17.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (checked) 0.30f else 0.60f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                )
        )

        // Sliding pure white circular knob / thumb
        Box(
            modifier = Modifier
                .padding(start = thumbOffset.coerceAtLeast(0.dp))
                .size(26.dp)
                .shadow(elevation = 4.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFF8FAFC),
                            Color(0xFFF1F5F9)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = if (checked) Color(0xFF818CF8).copy(alpha = 0.6f) else Color(0xFFCBD5E1),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (checked) Icons.Default.StayCurrentPortrait else Icons.Default.ScreenRotation,
                contentDescription = null,
                tint = if (checked) Color(0xFF4338CA) else Color(0xFF0284C7),
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
private fun ProfessionalSettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
