package com.thekeeperofpie.artistalleydatabase.play

import com.thekeeperofpie.artistalleydatabase.monetization.SubscriptionProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppUpdateChecker
import dev.zacsweers.metro.Binds

interface PlayComponent {
    @Binds
    val PlayAppUpdateChecker.bindAppUpdateChecker: AppUpdateChecker?

    @Binds
    val PlaySubscriptionProvider.bindSubscriptionProvider: SubscriptionProvider?
}
