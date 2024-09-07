package com.thekeeperofpie.artistalleydatabase.monetization.debug

import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import com.thekeeperofpie.artistalleydatabase.monetization.SubscriptionDetails
import com.thekeeperofpie.artistalleydatabase.monetization.SubscriptionProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.StringResource
import kotlin.time.Duration.Companion.seconds

@SingletonScope
@Inject
class DebugSubscriptionProvider(
    private val scope: ApplicationScope,
    private val settings: MonetizationSettings,
) : SubscriptionProvider {

    override val subscriptionDetails =
        MutableStateFlow(LoadingResult.loading<SubscriptionDetails<*>>())

    override val error = MutableStateFlow<Pair<StringResource, Throwable?>?>(null)
    override var loading = false

    override fun loadSubscriptionDetails() {
        scope.launch {
            delay(3.seconds)
            subscriptionDetails.emit(
                LoadingResult.success(
                    SubscriptionDetails(
                        id = "debug",
                        value = Unit,
                        cost = null,
                        period = null,
                    )
                )
            )
        }
    }

    override fun requestSubscribe(subscription: SubscriptionDetails<*>) {
        scope.launch(CustomDispatchers.Main) {
            loading = true
            delay(3.seconds)
            settings.subscribed.value = true
            loading = false
        }
    }

    override fun getManageSubscriptionUrl(
        subscription: SubscriptionDetails<*>?,
    ) = "https://play.google.com/store/account/subscriptions"
}
