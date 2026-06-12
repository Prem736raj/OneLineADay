package com.onelineaday.dailydiary

import android.app.Application
import com.google.android.gms.ads.MobileAds

import com.onelineaday.dailydiary.ads.InterstitialAdManager
import com.onelineaday.dailydiary.ads.RewardedAdManager

class OneLineApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Mobile Ads SDK
        MobileAds.initialize(this) {}
        
        // Pre-load ads
        InterstitialAdManager.loadAd(this)
        RewardedAdManager.loadAd(this)
    }
}
