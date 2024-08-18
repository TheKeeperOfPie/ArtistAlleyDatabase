package com.thekeeperofpie.artistalleydatabase.monetization.debug

import android.app.Activity
import androidx.compose.runtime.Composable
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationProvider
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import org.jetbrains.compose.resources.StringResource

class DebugMonetizationProvider(private val settings: MonetizationSettings) : MonetizationProvider {

    override var error: Pair<StringResource, Throwable?>? = null

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
