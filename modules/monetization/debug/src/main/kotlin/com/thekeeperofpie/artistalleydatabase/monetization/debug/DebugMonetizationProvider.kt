package com.thekeeperofpie.artistalleydatabase.monetization.debug

import android.app.Activity
import androidx.compose.runtime.Composable
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationProvider
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings

class DebugMonetizationProvider(private val settings: MonetizationSettings) : MonetizationProvider {

    override var error: Pair<Int, Throwable?>? = null

    override fun initialize(activity: Activity) = Unit

    @Composable
    override fun BannerAdView() = Unit

    override fun requestEnableAds() {
        settings.adsEnabled.value = true
    }

    override fun revokeAds() {
        settings.adsEnabled.value = false
    }
}
