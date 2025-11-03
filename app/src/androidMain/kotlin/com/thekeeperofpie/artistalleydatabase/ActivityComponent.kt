package com.thekeeperofpie.artistalleydatabase

import androidx.activity.ComponentActivity
import com.thekeeperofpie.artistalleydatabase.inject.ActivityScope
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationProvider
import com.thekeeperofpie.artistalleydatabase.monetization.SubscriptionProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppUpdateChecker
import com.thekeeperofpie.artistalleydatabase.utils_compose.ShareHandler
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provides

@ActivityScope
@GraphExtension
interface ActivityComponent : ActivityVariantComponent {

    val injector: Injector

    // TODO: Does this work now with Metro?
    // Doesn't seem to be an easy way to provide optional dependencies directly on the component
    @Inject
    class Injector(
        val appUpdateChecker: AppUpdateChecker? = null,
        val monetizationProvider: MonetizationProvider? = null,
        val subscriptionProvider: SubscriptionProvider? = null,
        val shareHandler: ShareHandler? = null,
    )

    @GraphExtension.Factory
    interface Factory {
        fun create(@Provides activity: ComponentActivity): ActivityComponent
    }
}
