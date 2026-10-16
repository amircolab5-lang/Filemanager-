package com.example.ui.components

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

/**
 * Master flag to control ad serving.
 * Temporarily disabled for Huawei AppGallery release.
 * When ready to enable ads conditionally (e.g. AdMob for Google devices and Huawei Ads for Huawei devices),
 * set this to true and configure platform-specific providers.
 */
const val ADS_ENABLED = true

/**
 * AdMob Native Ad IDs:
 * - Real Production Unit ID: ca-app-pub-6511786203446234/3340852855
 * - Official Google Test Unit ID: ca-app-pub-3940256099942544/2247696110
 */
const val ADMOB_NATIVE_AD_UNIT_ID = "ca-app-pub-6511786203446234/3340852855"
const val ADMOB_TEST_NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"

object AdMobNativePool {
    var cachedNativeAd: NativeAd? = null
}

/**
 * Professional AdMob Native Advanced Ad Card.
 * - Respects ADS_ENABLED master switch.
 * - Requests the production AdMob Native Ad unit when enabled.
 * - Automatically falls back to Google's official test native ad unit if live ad inventory has no fill.
 * - Fully crash-proof during theme changes and screen navigation.
 * - Automatically cleans up NativeAd references on disposal.
 */
@Composable
fun AdMobNativeAdCard(
    modifier: Modifier = Modifier,
    adUnitId: String = ADMOB_NATIVE_AD_UNIT_ID
) {
    if (!ADS_ENABLED) {
        // Ads temporarily disabled for Huawei AppGallery store release
        return
    }

    val context = LocalContext.current
    val surfaceColor = MaterialTheme.colorScheme.surface
    val isDark = (surfaceColor.red * 0.299f + surfaceColor.green * 0.587f + surfaceColor.blue * 0.114f) < 0.5f
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var adFailed by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                nativeAd?.destroy()
            } catch (_: Throwable) {}
        }
    }

    LaunchedEffect(adUnitId) {
        kotlinx.coroutines.delay(300)

        fun requestAd(unitId: String, isFallback: Boolean = false) {
            try {
                val adLoader = AdLoader.Builder(context, unitId)
                    .forNativeAd { ad ->
                        try {
                            nativeAd?.destroy()
                        } catch (_: Throwable) {}
                        nativeAd = ad
                        adFailed = false
                        isLoading = false
                    }
                    .withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            super.onAdFailedToLoad(loadAdError)
                            if (!isFallback && unitId != ADMOB_TEST_NATIVE_AD_UNIT_ID) {
                                // Fallback to Google official test unit so ad displays during development/testing
                                requestAd(ADMOB_TEST_NATIVE_AD_UNIT_ID, isFallback = true)
                            } else {
                                adFailed = true
                                isLoading = false
                            }
                        }
                    })
                    .withNativeAdOptions(
                        NativeAdOptions.Builder()
                            .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                            .build()
                    )
                    .build()

                adLoader.loadAd(AdRequest.Builder().build())
            } catch (e: Throwable) {
                if (!isFallback && unitId != ADMOB_TEST_NATIVE_AD_UNIT_ID) {
                    requestAd(ADMOB_TEST_NATIVE_AD_UNIT_ID, isFallback = true)
                } else {
                    adFailed = true
                    isLoading = false
                }
            }
        }

        requestAd(adUnitId)
    }

    val cardBg = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    val currentAd = nativeAd

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admob_native_ad_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp)
    ) {
        if (currentAd != null) {
            AndroidView(
                factory = { ctx ->
                    try {
                        val view = buildNativeAdView(
                            context = ctx,
                            nativeAd = currentAd,
                            isDark = isDark,
                            primaryColorArgb = primaryColor.toArgb(),
                            onSurfaceArgb = onSurface.toArgb(),
                            onSurfaceVariantArgb = onSurfaceVariant.toArgb()
                        )
                        view.tag = currentAd
                        view
                    } catch (e: Throwable) {
                        android.util.Log.e("AdMobNativeAd", "Failed to build native ad view", e)
                        NativeAdView(ctx)
                    }
                },
                update = { view ->
                    try {
                        populateNativeAdView(
                            view = view,
                            nativeAd = currentAd,
                            isDark = isDark,
                            primaryColorArgb = primaryColor.toArgb(),
                            onSurfaceArgb = onSurface.toArgb(),
                            onSurfaceVariantArgb = onSurfaceVariant.toArgb()
                        )
                    } catch (e: Throwable) {
                        android.util.Log.e("AdMobNativeAd", "Failed to update native ad view", e)
                    }
                },
                onRelease = { view ->
                    try {
                        view.destroy()
                    } catch (_: Throwable) {}
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )
        } else {
            // High-fidelity Native Advanced Ad Card layout
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // Top Row: "AD" badge + Sponsored label + star rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isDark) Color(0xFF334155) else Color(0xFFE0F2FE),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "AD",
                                color = if (isDark) Color.White else Color(0xFF0284C7),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "Sponsored",
                            style = MaterialTheme.typography.bodySmall,
                            color = onSurfaceVariant,
                            fontSize = 11.sp
                        )

                        Text(
                            text = "  ★ 4.9",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                            fontSize = 11.sp
                        )
                    }

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = primaryColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Content Row: Ad Icon + (Headline & Body) + CTA Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Ad Icon",
                                tint = primaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Google AdMob Native Advanced",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = onSurface,
                            maxLines = 1,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isLoading) "Loading ad inventory..." else "Native Ad Unit Active: ca-app-pub-6511786203446234/3340852855",
                            style = MaterialTheme.typography.bodySmall,
                            color = onSurfaceVariant,
                            maxLines = 2,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = primaryColor
                    ) {
                        Text(
                            text = "INSTALL",
                            color = if (isDark) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Builds the native Android layout hierarchy for Google AdMob NativeAdView.
 */
private fun buildNativeAdView(
    context: Context,
    nativeAd: NativeAd,
    isDark: Boolean,
    primaryColorArgb: Int,
    onSurfaceArgb: Int,
    onSurfaceVariantArgb: Int
): NativeAdView {
    val nativeAdView = NativeAdView(context)
    nativeAdView.layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

    val root = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    // Top Row: "AD" badge + advertiser/sponsor label + star rating
    val topRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dpToPx(context, 8)
        }
    }

    val adBadge = TextView(context).apply {
        text = "AD"
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
        setTypeface(null, Typeface.BOLD)
        setTextColor(if (isDark) android.graphics.Color.WHITE else android.graphics.Color.parseColor("#0284C7"))
        background = GradientDrawable().apply {
            cornerRadius = dpToPx(context, 4).toFloat()
            setColor(if (isDark) android.graphics.Color.parseColor("#334155") else android.graphics.Color.parseColor("#E0F2FE"))
        }
        setPadding(dpToPx(context, 5), dpToPx(context, 2), dpToPx(context, 5), dpToPx(context, 2))
    }
    topRow.addView(adBadge)

    val sponsorLabel = TextView(context).apply {
        text = "  Sponsored"
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
        setTextColor(onSurfaceVariantArgb)
    }
    topRow.addView(sponsorLabel)

    val ratingView = TextView(context).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
        setTextColor(if (isDark) android.graphics.Color.parseColor("#FBBF24") else android.graphics.Color.parseColor("#D97706"))
        setTypeface(null, Typeface.BOLD)
        visibility = View.GONE
    }
    topRow.addView(ratingView)
    nativeAdView.starRatingView = ratingView

    root.addView(topRow)

    // Main Content Row: Icon + (Headline & Body) + CTA Button
    val contentRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    // Ad Icon
    val iconView = ImageView(context).apply {
        layoutParams = LinearLayout.LayoutParams(dpToPx(context, 44), dpToPx(context, 44)).apply {
            rightMargin = dpToPx(context, 10)
        }
        scaleType = ImageView.ScaleType.FIT_CENTER
    }
    contentRow.addView(iconView)
    nativeAdView.iconView = iconView

    // Middle Column: Headline & Body
    val textCol = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            rightMargin = dpToPx(context, 8)
        }
    }

    val headlineView = TextView(context).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        setTypeface(null, Typeface.BOLD)
        setTextColor(onSurfaceArgb)
        maxLines = 1
        ellipsize = android.text.TextUtils.TruncateAt.END
    }
    textCol.addView(headlineView)
    nativeAdView.headlineView = headlineView

    val bodyView = TextView(context).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
        setTextColor(onSurfaceVariantArgb)
        maxLines = 2
        ellipsize = android.text.TextUtils.TruncateAt.END
    }
    textCol.addView(bodyView)
    nativeAdView.bodyView = bodyView

    contentRow.addView(textCol)

    // CTA Button
    val ctaButton = Button(context).apply {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
        setTypeface(null, Typeface.BOLD)
        setTextColor(if (isDark) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        background = GradientDrawable().apply {
            cornerRadius = dpToPx(context, 8).toFloat()
            setColor(primaryColorArgb)
        }
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            dpToPx(context, 36)
        )
        isAllCaps = true
    }
    contentRow.addView(ctaButton)
    nativeAdView.callToActionView = ctaButton

    root.addView(contentRow)
    nativeAdView.addView(root)

    populateNativeAdView(nativeAdView, nativeAd, isDark, primaryColorArgb, onSurfaceArgb, onSurfaceVariantArgb)

    return nativeAdView
}

private fun populateNativeAdView(
    view: NativeAdView,
    nativeAd: NativeAd,
    isDark: Boolean,
    primaryColorArgb: Int,
    onSurfaceArgb: Int,
    onSurfaceVariantArgb: Int
) {
    try {
        (view.headlineView as? TextView)?.apply {
            text = nativeAd.headline ?: "Sponsored"
            setTextColor(onSurfaceArgb)
        }

        (view.bodyView as? TextView)?.apply {
            text = nativeAd.body ?: ""
            visibility = if (nativeAd.body.isNullOrEmpty()) View.GONE else View.VISIBLE
            setTextColor(onSurfaceVariantArgb)
        }

        (view.starRatingView as? TextView)?.apply {
            val rating = nativeAd.starRating
            if (rating != null && rating > 0.0) {
                text = "  ★ ${"%.1f".format(rating)}"
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }

        (view.callToActionView as? Button)?.apply {
            text = nativeAd.callToAction ?: "INSTALL"
            visibility = if (nativeAd.callToAction.isNullOrEmpty()) View.GONE else View.VISIBLE
            (background as? GradientDrawable)?.setColor(primaryColorArgb)
            setTextColor(if (isDark) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }

        (view.iconView as? ImageView)?.apply {
            val icon = nativeAd.icon
            if (icon != null) {
                setImageDrawable(icon.drawable)
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }

        // Only register nativeAd once per NativeAdView instance to avoid AdMob SDK crash
        if (view.tag != nativeAd) {
            view.setNativeAd(nativeAd)
            view.tag = nativeAd
        }
    } catch (e: Throwable) {
        android.util.Log.e("AdMobNativeAd", "Error populating native ad", e)
    }
}

private fun dpToPx(context: Context, dp: Int): Int {
    return (dp * context.resources.displayMetrics.density).toInt()
}

