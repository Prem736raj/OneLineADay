package com.onelineaday.dailydiary.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Manages loading and showing interstitial ads on a time interval.
 * 
 * Uses Google's test ad unit ID by default.
 * Replace TEST_INTERSTITIAL_AD_UNIT_ID with your real ad unit ID before publishing.
 */
object InterstitialAdManager {

    private const val TAG = "InterstitialAdManager"

    // ⚠️ Test ad unit ID — replace with your real one before publishing
    private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-8204679574020840/2394730635"

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    
    // Configurable interval (3 minutes)
    private const val INTERVAL_MILLIS = 3 * 60 * 1000L
    
    // Keep track of when we last showed an ad. Initialized to 0 so the very first time they update/save an entry, it shows instantly.
    private var lastAdShownTime = 0L

    /**
     * Pre-loads an interstitial ad so it's ready when needed.
     */
    fun loadAd(context: Context) {
        if (com.onelineaday.dailydiary.PremiumManager.isPremium.value) return
        if (interstitialAd != null || isLoading) return

        isLoading = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully")
                    interstitialAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Interstitial ad failed to load: ${error.message}")
                    interstitialAd = null
                    isLoading = false
                }
            }
        )
    }

    /**
     * Shows the interstitial ad ONLY IF the configured time interval has passed.
     * 
     * [onAdDismissed] is called when the ad is closed (or if it wasn't shown).
     */
    fun showAdIfTimePassed(activity: Activity, onAdDismissed: () -> Unit = {}) {
        if (com.onelineaday.dailydiary.PremiumManager.isPremium.value) return

        val currentTime = System.currentTimeMillis()
        val timePassed = currentTime - lastAdShownTime
        
        Log.d(TAG, "Time since last ad: ${timePassed / 1000} seconds")
        
        val ad = interstitialAd
        
        if (timePassed < INTERVAL_MILLIS || ad == null) {
            // Not enough time has passed OR ad isn't loaded yet
            onAdDismissed()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial ad dismissed")
                interstitialAd = null
                lastAdShownTime = System.currentTimeMillis() // Reset the timer
                onAdDismissed()
                // Pre-load the next ad
                loadAd(activity)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.e(TAG, "Interstitial ad failed to show: ${error.message}")
                interstitialAd = null
                onAdDismissed()
                // Try loading again
                loadAd(activity)
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial ad shown")
            }
        }

        ad.show(activity)
    }
}
