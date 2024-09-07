package com.thekeeperofpie.artistalleydatabase.monetization.unity

import androidx.activity.ComponentActivity
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationProvider
import me.tatarka.inject.annotations.Provides

interface UnityComponent {
    val unityMonetizationProvider: (ComponentActivity) -> UnityMonetizationProvider

    @Provides
    fun provideUnityMonetizationProvider(
        activity: ComponentActivity,
    ): (ComponentActivity) -> MonetizationProvider? = unityMonetizationProvider
}
