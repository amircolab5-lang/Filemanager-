package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JosefinSansFontFamily
import com.example.ui.theme.LeagueSpartanFontFamily

// Authentic Ticno Brand Blue from user's ticno logo .png
val TicnoBrandBlue = Color(0xFF3882F6)

/**
 * Single curly bracket rotated 90 degrees matching Logopit_1789244649953.png:
 * [isTop] = true: Rotates { 90 degrees clockwise.
 *   - Outer shape has vertical side legs pointing DOWN (inward toward the text) with flat cuts
 *   - Rounded outer corners
 *   - Central peak/cusp pointing UP (away from text)
 * [isTop] = false: Rotates } 90 degrees.
 *   - Outer shape has vertical side legs pointing UP (inward toward the text) with flat cuts
 *   - Rounded outer corners
 *   - Central peak/cusp pointing DOWN (away from text)
 */
@Composable
fun RotatedCurlyBracket(
    isTop: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    strokeWidth: Dp = 4.dp
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path()
        val strokePx = strokeWidth.toPx()

        if (isTop) {
            // Top bracket: legs go vertically DOWN to h (facing the text inside)
            val leftX = strokePx / 2f
            val rightX = w - strokePx / 2f
            val cornerRadius = (w * 0.08f).coerceIn(8f, 24f)
            val armY = (h * 0.44f).coerceAtLeast(strokePx / 2f)
            val peakY = strokePx / 2f

            path.moveTo(leftX, h)
            path.lineTo(leftX, armY + cornerRadius)
            path.quadraticBezierTo(leftX, armY, leftX + cornerRadius, armY)
            path.lineTo(w * 0.38f, armY)
            path.cubicTo(
                w * 0.44f, armY,
                w * 0.47f, peakY,
                w * 0.50f, peakY
            )
            path.cubicTo(
                w * 0.53f, peakY,
                w * 0.56f, armY,
                w * 0.62f, armY
            )
            path.lineTo(rightX - cornerRadius, armY)
            path.quadraticBezierTo(rightX, armY, rightX, armY + cornerRadius)
            path.lineTo(rightX, h)
        } else {
            // Bottom bracket: legs go vertically UP to 0 (facing the text inside)
            val leftX = strokePx / 2f
            val rightX = w - strokePx / 2f
            val cornerRadius = (w * 0.08f).coerceIn(8f, 24f)
            val armY = (h * 0.56f).coerceAtMost(h - strokePx / 2f)
            val peakY = h - strokePx / 2f

            path.moveTo(leftX, 0f)
            path.lineTo(leftX, armY - cornerRadius)
            path.quadraticBezierTo(leftX, armY, leftX + cornerRadius, armY)
            path.lineTo(w * 0.38f, armY)
            path.cubicTo(
                w * 0.44f, armY,
                w * 0.47f, peakY,
                w * 0.50f, peakY
            )
            path.cubicTo(
                w * 0.53f, peakY,
                w * 0.56f, armY,
                w * 0.62f, armY
            )
            path.lineTo(rightX - cornerRadius, armY)
            path.quadraticBezierTo(rightX, armY, rightX, armY - cornerRadius)
            path.lineTo(rightX, 0f)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokePx,
                cap = StrokeCap.Butt,
                join = StrokeJoin.Round
            )
        )
    }
}

/**
 * Official Ticnodevelopers SVG Logo:
 * Implements the exact 800x400 (2:1 aspect ratio) vector design:
 * - Top half (200px): Solid Black (#000000) with "TICNO" in pure White (#FFFFFF),
 *   FontWeight.Black (900), uppercase, centered, with 5px proportional letter-spacing.
 * - Bottom half (200px): Solid White (#FFFFFF) with "DEVELOPERS" in pure Black (#000000),
 *   FontWeight.Black (900), uppercase, centered, with 3px proportional letter-spacing.
 * - Framed with a clean subtle border and smooth rounded corners for crisp contrast across all themes.
 */
@Composable
fun TicnodevelopersSvgLogo(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp,
    borderWidth: Dp = 1.dp
) {
    TicnoNeonLogo(
        modifier = modifier
    )
}

/**
 * Official Ticnodevelopers Brand Logo:
 * Displays the exact SVG logo requested by user.
 */
@Composable
fun TicnodevelopersBrandLogo(
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    bracketColor: Color = MaterialTheme.colorScheme.onSurface,
    fontSize: TextUnit = 25.sp,
    bracketWidth: Dp = 260.dp,
    bracketHeight: Dp = 30.dp,
    strokeWidth: Dp = 4.5.dp
) {
    TicnoNeonLogo(
        modifier = modifier.width(bracketWidth)
    )
}

/**
 * Authentic "Ticno" brand logo text rendered with League Spartan Bold in authentic brand blue (#3882F6).
 * Exactly matches the user's official ticno logo .png.
 */
@Composable
fun TicnoLogo(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 32.sp,
    color: Color = TicnoBrandBlue
) {
    Text(
        text = "Ticno",
        fontFamily = LeagueSpartanFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = fontSize,
        color = color,
        modifier = modifier.testTag("ticno_brand_logo_text")
    )
}

/**
 * Authentic brand badge featuring the new official Ticnodevelopers neon SVG logo
 * along with the official website https://ticnodevelopers.blogspot.com.
 */
@Composable
fun TicnoBrandBadge(
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
    showDevelopersSubtitle: Boolean = true,
    showWebsite: Boolean = true,
    logoSize: TextUnit = 25.sp,
    logoWidth: Dp = 270.dp
) {
    val context = LocalContext.current
    val websiteUrl = "https://ticnodevelopers.blogspot.com"
    val brandAccentColor = if (isDark) Color.White else TicnoBrandBlue

    Column(
        modifier = modifier.testTag("ticno_brand_logo"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Official Ticnodevelopers Neon SVG Logo as requested by user (no changes)
        TicnoNeonLogo(
            modifier = Modifier.width(logoWidth)
        )

        if (showWebsite) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (e: Throwable) {}
                    }
                    .padding(horizontal = 4.dp, vertical = 6.dp)
                    .testTag("ticno_website_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Website",
                    tint = Color(0xFF3D7EEB),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "https://ticnodevelopers.blogspot.com",
                    fontFamily = LeagueSpartanFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF3D7EEB),
                    textDecoration = TextDecoration.Underline,
                    letterSpacing = 0.2.sp,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Dedicated Brand Logo Card rendering the official Ticnodevelopers emblem:
 * Formatted with the official 2:1 SVG logo.
 */
@Composable
fun TicnodevelopersLogoCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp,
    elevation: Dp = 4.dp
) {
    Surface(
        modifier = modifier.testTag("ticnodevelopers_logo_card"),
        shape = RoundedCornerShape(cornerRadius),
        color = Color(0xFF000000),
        shadowElevation = elevation
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            TicnodevelopersSvgLogo(
                modifier = Modifier.fillMaxWidth(0.9f),
                cornerRadius = 8.dp
            )
        }
    }
}


