package com.thekeeperofpie.artistalleydatabase.monetization

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

interface MonetizationProvider {

    var error: Pair<Int, Throwable?>?

    fun initialize(activity: Activity)

    @Composable
    fun BannerAdView()

    fun requestEnableAds()

    fun onAdsRevoked()
}

val LocalMonetizationProvider = staticCompositionLocalOf<MonetizationProvider?> { null }
