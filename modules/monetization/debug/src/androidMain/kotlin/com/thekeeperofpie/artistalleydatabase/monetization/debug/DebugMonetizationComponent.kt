package com.thekeeperofpie.artistalleydatabase.monetization.debug

import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationProvider
import com.thekeeperofpie.artistalleydatabase.monetization.SubscriptionProvider
import dev.zacsweers.metro.Provides

interface DebugMonetizationComponent {

    @Provides
    fun bindMonetizationProvider(provider: DebugMonetizationProvider): MonetizationProvider =
        provider

    @Provides
    fun bindSubscriptionProvider(provider: DebugSubscriptionProvider): SubscriptionProvider =
        provider
}
