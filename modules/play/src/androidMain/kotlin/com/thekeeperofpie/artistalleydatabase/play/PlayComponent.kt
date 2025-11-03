package com.thekeeperofpie.artistalleydatabase.play

import com.thekeeperofpie.artistalleydatabase.monetization.SubscriptionProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppUpdateChecker
import dev.zacsweers.metro.Provides

interface PlayComponent {
    @Provides
    fun bindAppUpdateChecker(appUpdateChecker: PlayAppUpdateChecker): AppUpdateChecker? =
        appUpdateChecker

    @Provides
    fun bindSubscriptionProvider(provider: PlaySubscriptionProvider): SubscriptionProvider? =
        provider
}
