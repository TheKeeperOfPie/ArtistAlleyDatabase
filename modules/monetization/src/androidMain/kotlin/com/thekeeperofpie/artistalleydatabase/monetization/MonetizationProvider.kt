package com.thekeeperofpie.artistalleydatabase.monetization

import android.app.Activity
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource

actual interface MonetizationProvider {
    fun initialize(activity: Activity)

    actual var error: Pair<StringResource, Throwable?>?
    @Composable
    actual fun BannerAdView()
    actual fun requestEnableAds()
    actual fun revokeAds()
}
