package com.thekeeperofpie.artistalleydatabase.monetization

import kotlinx.coroutines.flow.MutableStateFlow

interface MonetizationSettings {

    val adsEnabled: MutableStateFlow<Boolean>
    val subscribed: MutableStateFlow<Boolean>
    val unlockAllFeatures: MutableStateFlow<Boolean>
}
