package com.thekeeperofpie.artistalleydatabase.monetization.debug

import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationProvider
import com.thekeeperofpie.artistalleydatabase.monetization.SubscriptionProvider
import me.tatarka.inject.annotations.Provides

interface DebugMonetizationComponent {

    val DebugMonetizationProvider.bind: MonetizationProvider
        @Provides get() = this
    val DebugSubscriptionProvider.bind: SubscriptionProvider
        @Provides get() = this
}
