package com.thekeeperofpie.artistalleydatabase.play

import androidx.activity.ComponentActivity
import com.thekeeperofpie.artistalleydatabase.monetization.SubscriptionProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppUpdateChecker
import me.tatarka.inject.annotations.Provides

interface PlayComponent {
    val playAppUpdateChecker: (ComponentActivity) -> PlayAppUpdateChecker
    val playSubscriptionProvider: (ComponentActivity) -> PlaySubscriptionProvider

    @Provides
    fun provideAppUpdateChecker(
        activity: ComponentActivity,
    ): (ComponentActivity) -> AppUpdateChecker? = playAppUpdateChecker

    @Provides
    fun provideSubscriptionProvider(
        activity: ComponentActivity,
    ): (ComponentActivity) -> SubscriptionProvider? = playSubscriptionProvider
}
