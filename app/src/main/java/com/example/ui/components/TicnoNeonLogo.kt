package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag

/**
 * Exact SVG requested by user:
 * viewBox: 0 0 800 250 (aspect ratio 800 / 250 = 3.2f)
 *
 * <defs>
 *   <filter id="glow-blue" ...>
 *   <style>
 *     .text-ticno { font-family: 'Arial Black', sans-serif; font-size: 85px; font-weight: 900; fill: transparent; stroke: #3D7EEB; stroke-width: 3px; filter: url(#glow-blue); }
 *     .text-dev { font-family: 'Segoe UI', sans-serif; font-size: 30px; font-weight: 600; fill: #3D7EEB; filter: url(#glow-blue); }
 *     .path-blue { fill: none; stroke: #3D7EEB; stroke-width: 6; stroke-linecap: round; stroke-linejoin: round; filter: url(#glow-blue); }
 *     .fill-blue { fill: #3D7EEB; filter: url(#glow-blue); }
 *   </style>
 * </defs>
 *
 * Logo Group: translate(150, 125)
 *   Hexagon outline:
 *     M 0 -80 L -70 -40 L -70 40 L -30 63
 *     M 0 -80 L 70 -40 L 70 40 L 30 63
 *   Angle Brackets:
 *     M -20 -15 L -45 10 L -20 35
 *     M 20 -15 L 45 10 L 20 35
 *   Slash:
 *     M -15 40 L 15 -20
 *   T shape top:
 *     M 0 -35 L -30 -15 L -10 -5 Z (fill)
 *     M 0 -35 L 30 -15 L 10 -5 Z (fill)
 *
 * Text Group: translate(260, 135)
 *   TICNO at (0, 0), font-size 85, textLength 270
 *   DEVELOPERS at (0, 45), font-size 30, textLength 270
 */
@Composable
fun TicnoNeonLogo(
    modifier: Modifier = Modifier
) {
    val blueColor = Color(0xFF3D7EEB)
    val glowColor = Color(0xFF3D7EEB).copy(alpha = 0.85f)

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(800f / 250f)
            .testTag("ticno_neon_svg_logo")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scale = size.width / 800f
            val blueArgb = blueColor.toArgb()
            val glowArgb = glowColor.toArgb()

            // Draw Logo Group transformed to (150 * scale, 125 * scale)
            val logoOriginX = 150f * scale
            val logoOriginY = 125f * scale

            val strokeWidthPx = 6f * scale

            // Path 1: Hexagon Left
            val hexLeft = Path().apply {
                moveTo(logoOriginX + 0f * scale, logoOriginY - 80f * scale)
                lineTo(logoOriginX - 70f * scale, logoOriginY - 40f * scale)
                lineTo(logoOriginX - 70f * scale, logoOriginY + 40f * scale)
                lineTo(logoOriginX - 30f * scale, logoOriginY + 63f * scale)
            }

            // Path 2: Hexagon Right
            val hexRight = Path().apply {
                moveTo(logoOriginX + 0f * scale, logoOriginY - 80f * scale)
                lineTo(logoOriginX + 70f * scale, logoOriginY - 40f * scale)
                lineTo(logoOriginX + 70f * scale, logoOriginY + 40f * scale)
                lineTo(logoOriginX + 30f * scale, logoOriginY + 63f * scale)
            }

            // Path 3: Bracket Left <
            val bracketLeft = Path().apply {
                moveTo(logoOriginX - 20f * scale, logoOriginY - 15f * scale)
                lineTo(logoOriginX - 45f * scale, logoOriginY + 10f * scale)
                lineTo(logoOriginX - 20f * scale, logoOriginY + 35f * scale)
            }

            // Path 4: Bracket Right >
            val bracketRight = Path().apply {
                moveTo(logoOriginX + 20f * scale, logoOriginY - 15f * scale)
                lineTo(logoOriginX + 45f * scale, logoOriginY + 10f * scale)
                lineTo(logoOriginX + 20f * scale, logoOriginY + 35f * scale)
            }

            // Path 5: Slash /
            val slashPath = Path().apply {
                moveTo(logoOriginX - 15f * scale, logoOriginY + 40f * scale)
                lineTo(logoOriginX + 15f * scale, logoOriginY - 20f * scale)
            }

            // Path 6 & 7: T Shape wings (fill)
            val tLeft = Path().apply {
                moveTo(logoOriginX + 0f * scale, logoOriginY - 35f * scale)
                lineTo(logoOriginX - 30f * scale, logoOriginY - 15f * scale)
                lineTo(logoOriginX - 10f * scale, logoOriginY - 5f * scale)
                close()
            }

            val tRight = Path().apply {
                moveTo(logoOriginX + 0f * scale, logoOriginY - 35f * scale)
                lineTo(logoOriginX + 30f * scale, logoOriginY - 15f * scale)
                lineTo(logoOriginX + 10f * scale, logoOriginY - 5f * scale)
                close()
            }

            val strokePaths = listOf(hexLeft, hexRight, bracketLeft, bracketRight, slashPath)

            // 1. Draw Glow pass for strokes
            val glowStroke = Stroke(
                width = strokeWidthPx * 1.8f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
            for (p in strokePaths) {
                drawPath(p, glowColor.copy(alpha = 0.45f), style = glowStroke)
            }

            // 2. Draw Main Crisp Stroke pass
            val mainStroke = Stroke(
                width = strokeWidthPx,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
            for (p in strokePaths) {
                drawPath(p, blueColor, style = mainStroke)
            }

            // 3. Draw T-shape wings (fill)
            drawPath(tLeft, blueColor)
            drawPath(tRight, blueColor)

            // Text Group: translated to (260 * scale, 135 * scale)
            val textOriginX = 260f * scale
            val textOriginY = 135f * scale

            drawIntoCanvas { canvas ->
                val nativeCanvas = canvas.nativeCanvas

                // Paint for "TICNO": font-family Arial Black, size 85px, stroke-width 3px, stroke #3D7EEB
                val ticnoGlowPaint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.create("Arial", android.graphics.Typeface.BOLD)
                    textSize = 85f * scale
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = 5f * scale
                    color = glowArgb
                }

                val ticnoPaint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.create("Arial", android.graphics.Typeface.BOLD)
                    textSize = 85f * scale
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = 3f * scale
                    color = blueArgb
                }

                // Adjust letter spacing to match textLength="270"
                val desiredTicnoWidth = 270f * scale
                val baseTicnoWidth = ticnoPaint.measureText("TICNO")
                if (baseTicnoWidth > 0f) {
                    val extraSpacing = (desiredTicnoWidth - baseTicnoWidth) / 4f
                    val spacing = (extraSpacing / ticnoPaint.textSize).coerceAtLeast(0f)
                    ticnoGlowPaint.letterSpacing = spacing
                    ticnoPaint.letterSpacing = spacing
                }

                // Draw TICNO glow pass then main stroke
                nativeCanvas.drawText("TICNO", textOriginX, textOriginY, ticnoGlowPaint)
                nativeCanvas.drawText("TICNO", textOriginX, textOriginY, ticnoPaint)

                // Paint for "DEVELOPERS": font-family Segoe UI, size 30px, font-weight 600, fill #3D7EEB
                val devGlowPaint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
                    textSize = 30f * scale
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = 2.5f * scale
                    color = glowArgb
                }

                val devPaint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
                    textSize = 30f * scale
                    style = android.graphics.Paint.Style.FILL
                    color = blueArgb
                }

                val desiredDevWidth = 270f * scale
                val baseDevWidth = devPaint.measureText("DEVELOPERS")
                if (baseDevWidth > 0f) {
                    val extraSpacing = (desiredDevWidth - baseDevWidth) / 9f
                    val spacing = (extraSpacing / devPaint.textSize).coerceAtLeast(0f)
                    devGlowPaint.letterSpacing = spacing
                    devPaint.letterSpacing = spacing
                }

                // Draw DEVELOPERS glow pass then crisp text
                nativeCanvas.drawText("DEVELOPERS", textOriginX, textOriginY + (45f * scale), devGlowPaint)
                nativeCanvas.drawText("DEVELOPERS", textOriginX, textOriginY + (45f * scale), devPaint)
            }
        }
    }
}
