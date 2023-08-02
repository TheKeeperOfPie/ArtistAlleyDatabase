package com.thekeeperofpie.artistalleydatabase.monetization

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

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
    }.distinctUntilChanged()

    val unlockDatabaseFeatures = combine(
        settings.subscribed,
        settings.unlockAllFeatures,
        overrideProvider.overrideUnlock,
    ) { subscribed, unlockAllFeatures, overrideUnlock ->
        subscribed || unlockAllFeatures || overrideUnlock
    }.distinctUntilChanged()

    var adsEnabled = settings.adsEnabled
    var subscribed = settings.subscribed
}
