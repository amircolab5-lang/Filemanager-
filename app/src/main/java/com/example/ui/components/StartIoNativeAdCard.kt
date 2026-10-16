package com.example.ui.components

import android.view.View
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.util.StartIoManager
import com.startapp.sdk.ads.banner.Banner
import com.startapp.sdk.ads.banner.BannerListener

/**
 * Start.io High-Yield Banner / Native Ad Card.
 * Displays banner advertising seamlessly inside lists, cards, and screens.
 *
 * If internet is offline or ad fails to load, collapses safely to 0 height with zero crash.
 */
@Composable
fun StartIoNativeAdCard(
    modifier: Modifier = Modifier
) {
    StartIoBannerAd(modifier = modifier)
}

/**
 * Standard Start.io Banner Ad composable wrapper with full offline protection.
 */
@Composable
fun StartIoBannerAd(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    if (!StartIoManager.isNetworkAvailable(context)) {
        return
    }

    var isLoaded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                val container = FrameLayout(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                try {
                    val banner = Banner(ctx, object : BannerListener {
                        override fun onReceiveAd(bannerView: View) {
                            isLoaded = true
                        }

                        override fun onFailedToReceiveAd(bannerView: View?) {
                            isLoaded = false
                        }

                        override fun onImpression(bannerView: View) {}
                        override fun onClick(bannerView: View) {}
                    })

                    container.addView(banner)
                    banner.loadAd()
                } catch (_: Throwable) {
                    isLoaded = false
                }

                container
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (isLoaded) 4.dp else 0.dp)
        )
    }
}
