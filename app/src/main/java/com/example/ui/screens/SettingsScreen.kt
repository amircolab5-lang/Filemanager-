package com.example.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.ExperimentalMaterial3Api
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
 * Settings Screen with Dark Green Glossy Shine Gradient Theme Toggle & Privacy Policy.
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
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
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
                            modifier = Modifier.size(28.dp)
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
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Feedback
            SettingsItemRow(
                icon = Icons.Default.Feedback,
                title = "Feedback",
                onClick = onFeedbackClick,
                testTag = "settings_feedback_row"
            )

            Spacer(modifier = Modifier.height(26.dp))

            // 5-star rate
            SettingsItemRow(
                icon = Icons.Default.ThumbUp,
                title = "5-star rate",
                onClick = onRateClick,
                testTag = "settings_rate_row"
            )

            Spacer(modifier = Modifier.height(26.dp))

            // About
            SettingsItemRow(
                icon = Icons.Default.Info,
                title = "About",
                onClick = onAboutClick,
                testTag = "settings_about_row"
            )

            Spacer(modifier = Modifier.height(26.dp))

            // Privacy Policy
            SettingsItemRow(
                icon = Icons.Default.Policy,
                title = "Privacy Policy",
                onClick = onPrivacyPolicyClick,
                testTag = "settings_privacy_policy_row"
            )

            Spacer(modifier = Modifier.height(26.dp))

            // Theme Mode row with Dark Green Glossy Shine Gradient Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // High-visibility themed icon badge (Crisp monochrome/slate, no green)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                if (isDarkMode) listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                else listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
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

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Theme mode",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isDarkMode) "Dark mode (Active)" else "Light mode (Active)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                // Modern Glossy Black & White Theme Toggle
                GlossyGreenThemeSwitch(
                    checked = isDarkMode,
                    onCheckedChange = onToggleDarkMode,
                    modifier = Modifier.testTag("settings_theme_switch")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Official Ticno Branding
            com.example.ui.components.TicnoBrandBadge(
                isDark = isDarkMode,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            )
        }
    }
}

/**
 * Custom Monochrome Glossy Theme Toggle Button.
 * Clean, high contrast: dark charcoal and white in Dark Mode, crisp soft-slate and white in Light Mode.
 * No green colors.
 */
@Composable
fun GlossyGreenThemeSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 28.dp else 3.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
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
                .padding(start = thumbOffset)
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

@Composable
private fun SettingsItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(22.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
    }
}
