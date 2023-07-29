package com.thekeeperofpie.artistalleydatabase.monetization

import kotlinx.coroutines.flow.combine

class MonetizationController(
    settings: MonetizationSettings,
    overrideProvider: MonetizationOverrideProvider,
) {

    val unlocked = combine(
        settings.adsEnabled,
        settings.subscribed,
        settings.unlockAllFeatures,
        overrideProvider.overrideUnlock,
    ) { adsEnabled, subscribed, unlockAllFeatures, overrideUnlock ->
        adsEnabled || subscribed || unlockAllFeatures || overrideUnlock
    }

    val unlockDatabaseFeatures = combine(
        settings.subscribed,
        settings.unlockAllFeatures,
        overrideProvider.overrideUnlock,
    ) { subscribed, unlockAllFeatures, overrideUnlock ->
        subscribed || unlockAllFeatures || overrideUnlock
    }

    var adsEnabled = settings.adsEnabled
    var subscribed = settings.subscribed
}
