package com.example.util

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.startapp.sdk.adsbase.Ad
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.StartAppSDK
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener
import com.startapp.sdk.adsbase.adlisteners.AdEventListener
import com.startapp.sdk.adsbase.adlisteners.VideoListener

/**
 * Start.io (StartApp) Advertising Manager.
 * App ID: 209701719
 *
 * Implements high-revenue ad formats:
 * - Splash Ad (Auto Return & Launch)
 * - Interstitial Ads on key actions
 * - Rewarded Ads on "Release more space" and batch file deletion (>= 2 files)
 * - Native / Banner ads with offline protection
 *
 * All operations are fail-safe and completely crash-proof even with no internet connection.
 */
object StartIoManager {
    const val APP_ID = "209701719"
    private const val TAG = "StartIoManager"

    @Volatile
    private var isInitialized = false

    private var interstitialAd: StartAppAd? = null
    private var rewardedAd: StartAppAd? = null

    /**
     * Initializes Start.io SDK safely.
     */
    fun init(context: Context) {
        if (isInitialized) return
        try {
            // Initialize Start.io with App ID 209701719 and enable Return Ads for maximum earnings
            StartAppSDK.init(context, APP_ID, true)
            StartAppSDK.enableReturnAds(true)
            isInitialized = true
            Log.d(TAG, "Start.io SDK initialized successfully with App ID: $APP_ID")

            // Preload interstitial and rewarded ads if network is available
            if (isNetworkAvailable(context)) {
                preloadAds(context)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Safe Start.io init fallback: ${t.message}")
        }
    }

    /**
     * Checks if internet connectivity is active.
     */
    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = cm.activeNetwork ?: return true
                val capabilities = cm.getNetworkCapabilities(network) ?: return true
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } else {
                @Suppress("DEPRECATION")
                val netInfo = cm.activeNetworkInfo
                netInfo != null && netInfo.isConnected
            }
        } catch (_: Throwable) {
            true
        }
    }

    /**
     * Preloads ads for fast presentation.
     */
    fun preloadAds(context: Context) {
        try {
            if (!isNetworkAvailable(context)) return

            interstitialAd = StartAppAd(context).apply {
                loadAd(StartAppAd.AdMode.AUTOMATIC)
            }

            rewardedAd = StartAppAd(context).apply {
                loadAd(StartAppAd.AdMode.REWARDED_VIDEO)
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to preload Start.io ads: ${t.message}")
        }
    }

    /**
     * Shows the splash ad on launch if network is available.
     * Guaranteed never to crash under any circumstance.
     */
    fun showSplash(activity: Activity, savedInstanceState: Bundle?) {
        try {
            if (!isNetworkAvailable(activity)) return
            StartAppAd.showSplash(activity, savedInstanceState)
        } catch (t: Throwable) {
            Log.w(TAG, "StartAppAd.showSplash safe bypass: ${t.message}")
        }
    }

    /**
     * Shows a Rewarded Video ad.
     * Triggered on "Release more space" / Storage Clean and when deleting multiple files (>= 2 files).
     *
     * If there is no internet, or if ad loading fails, [onCompleted] is called immediately
     * so user flow is never interrupted and never crashes.
     */
    fun showRewardedAd(activity: Activity, onCompleted: () -> Unit) {
        if (!isNetworkAvailable(activity)) {
            Log.d(TAG, "No internet: proceeding without rewarded ad")
            onCompleted()
            return
        }

        try {
            var rewardGiven = false
            fun triggerCompletion() {
                if (!rewardGiven) {
                    rewardGiven = true
                    activity.runOnUiThread {
                        try {
                            onCompleted()
                        } catch (e: Throwable) {
                            Log.e(TAG, "Error in onCompleted callback: ${e.message}")
                        }
                    }
                }
            }

            val ad = StartAppAd(activity)
            ad.setVideoListener(object : VideoListener {
                override fun onVideoCompleted() {
                    Log.d(TAG, "Rewarded video completed - granting reward")
                    triggerCompletion()
                }
            })

            ad.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, object : AdEventListener {
                override fun onReceiveAd(receivedAd: Ad) {
                    try {
                        val shown = ad.showAd(object : AdDisplayListener {
                            override fun adHidden(displayAd: Ad) {
                                triggerCompletion()
                            }

                            override fun adDisplayed(displayAd: Ad) {
                                Log.d(TAG, "Rewarded ad displayed")
                            }

                            override fun adClicked(displayAd: Ad) {
                                Log.d(TAG, "Rewarded ad clicked")
                            }

                            override fun adNotDisplayed(displayAd: Ad) {
                                Log.w(TAG, "Rewarded ad not displayed")
                                triggerCompletion()
                            }
                        })
                        if (!shown) {
                            triggerCompletion()
                        }
                    } catch (t: Throwable) {
                        Log.e(TAG, "Failed showing rewarded ad: ${t.message}")
                        triggerCompletion()
                    }
                }

                override fun onFailedToReceiveAd(adError: Ad?) {
                    Log.w(TAG, "Rewarded ad failed to receive: ${adError?.errorMessage}")
                    triggerCompletion()
                }
            })
        } catch (t: Throwable) {
            Log.e(TAG, "Rewarded ad exception safe fallback: ${t.message}")
            onCompleted()
        }
    }

    /**
     * Shows an Interstitial Ad on key transition events (e.g. exiting clean screen or major file ops).
     */
    fun showInterstitial(activity: Activity, onClosed: () -> Unit = {}) {
        if (!isNetworkAvailable(activity)) {
            onClosed()
            return
        }

        try {
            var closed = false
            fun triggerClosed() {
                if (!closed) {
                    closed = true
                    activity.runOnUiThread { onClosed() }
                }
            }

            val ad = interstitialAd ?: StartAppAd(activity)
            val shown = ad.showAd(object : AdDisplayListener {
                override fun adHidden(displayAd: Ad) {
                    triggerClosed()
                    preloadAds(activity)
                }

                override fun adDisplayed(displayAd: Ad) {}
                override fun adClicked(displayAd: Ad) {}
                override fun adNotDisplayed(displayAd: Ad) {
                    triggerClosed()
                }
            })
            if (!shown) {
                triggerClosed()
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Interstitial ad safe fallback: ${t.message}")
            onClosed()
        }
    }
}
