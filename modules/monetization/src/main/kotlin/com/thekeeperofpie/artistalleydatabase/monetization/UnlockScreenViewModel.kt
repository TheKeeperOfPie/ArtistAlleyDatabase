package com.thekeeperofpie.artistalleydatabase.monetization

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UnlockScreenViewModel @Inject constructor(
    monetizationController: MonetizationController
) : ViewModel() {

    val adsEnabled = monetizationController.adsEnabled
    val subscribed = monetizationController.subscribed

    fun enableAdsDebug() {
        adsEnabled.value = true
    }

    fun onSubscribeClick() {
    }
}
