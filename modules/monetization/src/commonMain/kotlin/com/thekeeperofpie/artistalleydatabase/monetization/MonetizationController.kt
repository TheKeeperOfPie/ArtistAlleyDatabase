package com.thekeeperofpie.artistalleydatabase.monetization

import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
class MonetizationController(
    settings: MonetizationSettings,
    overrideProvider: MonetizationOverrideProvider,
) {
    val unlocked = combineStates(
        settings.adsEnabled,
        settings.subscribed,
        settings.unlockAllFeatures,
        overrideProvider.overrideUnlock,
    ) { adsEnabled, subscribed, unlockAllFeatures, overrideUnlock ->
        adsEnabled || subscribed || unlockAllFeatures || overrideUnlock
    }

    val unlockDatabaseFeatures = combineStates(
        settings.subscribed,
        settings.unlockAllFeatures,
        overrideProvider.overrideUnlock,
    ) { subscribed, unlockAllFeatures, overrideUnlock ->
        subscribed || unlockAllFeatures || overrideUnlock
    }

    var adsEnabled = settings.adsEnabled
    var subscribed = settings.subscribed
}
