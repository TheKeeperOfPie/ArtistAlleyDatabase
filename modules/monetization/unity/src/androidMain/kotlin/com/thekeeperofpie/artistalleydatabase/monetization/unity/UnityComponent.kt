package com.thekeeperofpie.artistalleydatabase.monetization.unity

import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationProvider
import dev.zacsweers.metro.Provides

interface UnityComponent {
    @Provides
    fun provideMonetizationProvider(provider: UnityMonetizationProvider): MonetizationProvider? =
        provider
}
