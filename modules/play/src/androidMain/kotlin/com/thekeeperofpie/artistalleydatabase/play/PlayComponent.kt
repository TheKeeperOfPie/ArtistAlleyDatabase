package com.thekeeperofpie.artistalleydatabase.play

import com.thekeeperofpie.artistalleydatabase.monetization.SubscriptionProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppUpdateChecker
import me.tatarka.inject.annotations.Provides

interface PlayComponent {
    val PlayAppUpdateChecker.bind: AppUpdateChecker?
        @Provides get() = this
    val PlaySubscriptionProvider.bind: SubscriptionProvider?
        @Provides get() = this
}
