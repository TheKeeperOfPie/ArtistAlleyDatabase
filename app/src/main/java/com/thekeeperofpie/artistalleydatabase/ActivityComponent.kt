package com.thekeeperofpie.artistalleydatabase

import androidx.activity.ComponentActivity
import com.thekeeperofpie.artistalleydatabase.inject.ActivityScope
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationProvider
import com.thekeeperofpie.artistalleydatabase.monetization.SubscriptionProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppUpdateChecker
import com.thekeeperofpie.artistalleydatabase.utils_compose.ShareHandler
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Inject
import me.tatarka.inject.annotations.Provides

@ActivityScope
@Component
abstract class ActivityComponent(
    @Component val applicationComponent: ApplicationComponent,
    @get:Provides val activity: ComponentActivity,
) : ActivityVariantComponent {

    abstract val injector: Injector

    // Doesn't seem to be an easy way to provide optional dependencies directly on the component
    @Inject
    class Injector(
        val appUpdateChecker: AppUpdateChecker? = null,
        val monetizationProvider: MonetizationProvider? = null,
        val subscriptionProvider: SubscriptionProvider? = null,
        val shareHandler: ShareHandler? = null,
    )
}
