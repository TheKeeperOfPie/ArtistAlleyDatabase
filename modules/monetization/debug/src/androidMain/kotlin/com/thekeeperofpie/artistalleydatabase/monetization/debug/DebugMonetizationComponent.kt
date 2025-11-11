package com.thekeeperofpie.artistalleydatabase.monetization.debug

import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationProvider
import com.thekeeperofpie.artistalleydatabase.monetization.SubscriptionProvider
import dev.zacsweers.metro.Binds

interface DebugMonetizationComponent {

    @Binds
    val DebugMonetizationProvider.bindMonetizationProvider: MonetizationProvider

    @Binds
    val DebugSubscriptionProvider.bindSubscriptionProvider: SubscriptionProvider
}
