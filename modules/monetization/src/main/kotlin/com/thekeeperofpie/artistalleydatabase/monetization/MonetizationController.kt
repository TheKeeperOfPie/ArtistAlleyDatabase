package com.thekeeperofpie.artistalleydatabase.monetization

import kotlinx.coroutines.flow.combine

class MonetizationController(settings: MonetizationSettings) {

    val unlocked = combine(
        settings.adsEnabled,
        settings.subscribed
    ) { adsEnabled, subscribed -> adsEnabled || subscribed }

    var adsEnabled = settings.adsEnabled
    var subscribed = settings.subscribed
}
