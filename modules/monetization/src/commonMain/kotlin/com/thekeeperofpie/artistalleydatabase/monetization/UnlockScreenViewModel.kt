package com.thekeeperofpie.artistalleydatabase.monetization

import androidx.lifecycle.ViewModel
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UnlockScreenViewModel @Inject constructor(
    monetizationController: MonetizationController,
    private val featureOverrideProvider: FeatureOverrideProvider,
) : ViewModel() {

    val adsEnabled = monetizationController.adsEnabled
    val subscribed = monetizationController.subscribed

    fun enableAdsDebug() {
        if (!featureOverrideProvider.isReleaseBuild) {
            adsEnabled.value = true
        }
    }

    fun disableAdsDebug() {
        if (!featureOverrideProvider.isReleaseBuild) {
            adsEnabled.value = false
        }
    }
}
