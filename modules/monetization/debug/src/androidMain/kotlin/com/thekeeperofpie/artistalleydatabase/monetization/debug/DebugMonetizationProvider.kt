package com.thekeeperofpie.artistalleydatabase.monetization.debug

import androidx.compose.runtime.Composable
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationProvider
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import org.jetbrains.compose.resources.StringResource

@SingleIn(AppScope::class)
@Inject
class DebugMonetizationProvider(private val settings: MonetizationSettings) : MonetizationProvider {

    override var error: Pair<StringResource, Throwable?>? = null

    @Composable
    override fun BannerAdView() = Unit

    override fun requestEnableAds() {
        settings.adsEnabled.value = true
    }

    override fun revokeAds() {
        settings.adsEnabled.value = false
    }
}
