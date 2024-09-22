package com.thekeeperofpie.artistalleydatabase.monetization.unity

import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationProvider
import me.tatarka.inject.annotations.Provides

interface UnityComponent {
    val UnityMonetizationProvider.bind: MonetizationProvider?
        @Provides get() = this
}
