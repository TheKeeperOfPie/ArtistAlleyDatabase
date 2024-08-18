package com.thekeeperofpie.artistalleydatabase.monetization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import org.jetbrains.compose.resources.StringResource

expect interface MonetizationProvider {

    var error: Pair<StringResource, Throwable?>?

    @Composable
    fun BannerAdView()

    fun requestEnableAds()

    fun revokeAds()
}

val LocalMonetizationProvider = staticCompositionLocalOf<MonetizationProvider?> { null }
