package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object AppIcons {
    val StatusSaver: ImageVector by lazy {
        ImageVector.Builder(
            name = "StatusSaver",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Outer circular status ring
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2.0f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12.0f, 2.0f)
                curveTo(17.52f, 2.0f, 22.0f, 6.48f, 22.0f, 12.0f)
                curveTo(22.0f, 17.52f, 17.52f, 22.0f, 12.0f, 22.0f)
                curveTo(6.48f, 22.0f, 2.0f, 17.52f, 2.0f, 12.0f)
                curveTo(2.0f, 6.48f, 6.48f, 2.0f, 12.0f, 2.0f)
                close()
            }
            // Inner download arrow & tray
            path(
                fill = SolidColor(Color.White),
                stroke = null
            ) {
                // Down arrow
                moveTo(11.0f, 6.5f)
                lineTo(13.0f, 6.5f)
                lineTo(13.0f, 12.0f)
                lineTo(15.5f, 12.0f)
                lineTo(12.0f, 16.0f)
                lineTo(8.5f, 12.0f)
                lineTo(11.0f, 12.0f)
                close()
                // Horizontal base bar
                moveTo(7.5f, 17.0f)
                lineTo(16.5f, 17.0f)
                lineTo(16.5f, 18.5f)
                lineTo(7.5f, 18.5f)
                close()
            }
        }.build()
    }

    val WhatsApp: ImageVector get() = StatusSaver

    val Messenger: ImageVector by lazy {
        ImageVector.Builder(
            name = "Messenger",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                fillAlpha = 1.0f,
                stroke = null
            ) {
                moveTo(12f, 2f)
                curveTo(6.48f, 2f, 2f, 6.18f, 2f, 11.34f)
                curveTo(2f, 14.29f, 3.49f, 16.9f, 5.82f, 18.57f)
                lineTo(5.17f, 21.64f)
                curveTo(5.08f, 22.06f, 5.48f, 22.42f, 5.88f, 22.25f)
                lineTo(9.44f, 20.73f)
                curveTo(10.26f, 20.97f, 11.12f, 21.1f, 12f, 21.1f)
                curveTo(17.52f, 21.1f, 22f, 16.92f, 22f, 11.76f)
                curveTo(22f, 6.18f, 17.52f, 2f, 12f, 2f)
                close()
                // Lightning bolt cutout/overlay in contrasting color if needed, or bolt path:
                moveTo(13.2f, 8f)
                lineTo(9.8f, 12.5f)
                lineTo(7.2f, 10.5f)
                lineTo(4.5f, 14.5f)
                lineTo(8f, 12.5f)
                lineTo(10.6f, 14.5f)
                lineTo(14.5f, 9.5f)
                close()
            }
        }.build()
    }

    val XShare: ImageVector by lazy {
        ImageVector.Builder(
            name = "XShare",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                fillAlpha = 1.0f,
                stroke = null
            ) {
                moveTo(6.5f, 8f)
                lineTo(17.5f, 8f)
                lineTo(14.5f, 5f)
                lineTo(16f, 3.5f)
                lineTo(21.5f, 9f)
                lineTo(16f, 14.5f)
                lineTo(14.5f, 13f)
                lineTo(17.5f, 10f)
                lineTo(6.5f, 10f)
                close()
                moveTo(17.5f, 16f)
                lineTo(6.5f, 16f)
                lineTo(9.5f, 19f)
                lineTo(8f, 20.5f)
                lineTo(2.5f, 15f)
                lineTo(8f, 9.5f)
                lineTo(9.5f, 11f)
                lineTo(6.5f, 14f)
                lineTo(17.5f, 14f)
                close()
            }
        }.build()
    }

    val XHide: ImageVector by lazy {
        ImageVector.Builder(
            name = "XHide",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                fillAlpha = 1.0f,
                stroke = null
            ) {
                moveTo(12f, 2f)
                curveTo(17.52f, 2f, 22f, 6.48f, 22f, 12f)
                curveTo(22f, 17.52f, 17.52f, 22f, 12f, 22f)
                curveTo(6.48f, 22f, 2f, 17.52f, 2f, 12f)
                curveTo(2f, 6.48f, 6.48f, 2f, 12f, 2f)
                close()
                // Star / Asterisk pattern
                moveTo(11f, 6f)
                lineTo(13f, 6f)
                lineTo(13f, 9.5f)
                lineTo(15.5f, 7.5f)
                lineTo(17f, 9f)
                lineTo(14.5f, 11f)
                lineTo(18f, 11f)
                lineTo(18f, 13f)
                lineTo(14.5f, 13f)
                lineTo(17f, 15f)
                lineTo(15.5f, 16.5f)
                lineTo(13f, 14.5f)
                lineTo(13f, 18f)
                lineTo(11f, 18f)
                lineTo(11f, 14.5f)
                lineTo(8.5f, 16.5f)
                lineTo(7f, 15f)
                lineTo(9.5f, 13f)
                lineTo(6f, 13f)
                lineTo(6f, 11f)
                lineTo(9.5f, 11f)
                lineTo(7f, 9f)
                lineTo(8.5f, 7.5f)
                lineTo(11f, 9.5f)
                close()
            }
        }.build()
    }

    val Python: ImageVector by lazy {
        ImageVector.Builder(
            name = "Python",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFFFD43B)),
                fillAlpha = 1.0f,
                stroke = null
            ) {
                // Top snake
                moveTo(11.9f, 2.0f)
                curveTo(8.5f, 2.0f, 6.5f, 3.5f, 6.5f, 5.8f)
                lineTo(6.5f, 8.0f)
                lineTo(12.0f, 8.0f)
                lineTo(12.0f, 9.5f)
                lineTo(4.5f, 9.5f)
                curveTo(2.8f, 9.5f, 2.0f, 10.9f, 2.0f, 13.0f)
                curveTo(2.0f, 15.1f, 3.1f, 16.5f, 5.0f, 16.5f)
                lineTo(6.5f, 16.5f)
                lineTo(6.5f, 14.5f)
                curveTo(6.5f, 12.8f, 7.8f, 11.5f, 9.5f, 11.5f)
                lineTo(14.5f, 11.5f)
                curveTo(15.9f, 11.5f, 17.0f, 10.4f, 17.0f, 9.0f)
                lineTo(17.0f, 5.8f)
                curveTo(17.0f, 3.5f, 15.3f, 2.0f, 11.9f, 2.0f)
                close()
                // Top eye dot
                moveTo(9.5f, 4.2f)
                curveTo(10.1f, 4.2f, 10.5f, 4.6f, 10.5f, 5.2f)
                curveTo(10.5f, 5.8f, 10.1f, 6.2f, 9.5f, 6.2f)
                curveTo(8.9f, 6.2f, 8.5f, 5.8f, 8.5f, 5.2f)
                curveTo(8.5f, 4.6f, 8.9f, 4.2f, 9.5f, 4.2f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF3776AB)),
                fillAlpha = 1.0f,
                stroke = null
            ) {
                // Bottom snake
                moveTo(12.1f, 22.0f)
                curveTo(15.5f, 22.0f, 17.5f, 20.5f, 17.5f, 18.2f)
                lineTo(17.5f, 16.0f)
                lineTo(12.0f, 16.0f)
                lineTo(12.0f, 14.5f)
                lineTo(19.5f, 14.5f)
                curveTo(21.2f, 14.5f, 22.0f, 13.1f, 22.0f, 11.0f)
                curveTo(22.0f, 8.9f, 20.9f, 7.5f, 19.0f, 7.5f)
                lineTo(17.5f, 7.5f)
                lineTo(17.5f, 9.5f)
                curveTo(17.5f, 11.2f, 16.2f, 12.5f, 14.5f, 12.5f)
                lineTo(9.5f, 12.5f)
                curveTo(8.1f, 12.5f, 7.0f, 13.6f, 7.0f, 15.0f)
                lineTo(7.0f, 18.2f)
                curveTo(7.0f, 20.5f, 8.7f, 22.0f, 12.1f, 22.0f)
                close()
                // Bottom eye dot
                moveTo(14.5f, 19.8f)
                curveTo(13.9f, 19.8f, 13.5f, 19.4f, 13.5f, 18.8f)
                curveTo(13.5f, 18.2f, 13.9f, 17.8f, 14.5f, 17.8f)
                curveTo(15.1f, 17.8f, 15.5f, 18.2f, 15.5f, 18.8f)
                curveTo(15.5f, 19.4f, 15.1f, 19.8f, 14.5f, 19.8f)
                close()
            }
        }.build()
    }
}
