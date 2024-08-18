package com.thekeeperofpie.artistalleydatabase.monetization.debug

import androidx.activity.ComponentActivity
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import com.thekeeperofpie.artistalleydatabase.monetization.SubscriptionProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import kotlin.time.Duration.Companion.seconds

class DebugSubscriptionProvider(
    private val scope: CoroutineScope,
    private val settings: MonetizationSettings,
) : SubscriptionProvider {

    override val subscriptionDetails =
        MutableStateFlow(LoadingResult.loading<SubscriptionProvider.SubscriptionDetails<*>>())

    override val error = MutableStateFlow<Pair<StringResource, Throwable?>?>(null)
    override var loading = false

    override fun initialize(activity: ComponentActivity) {
    }

    override fun loadSubscriptionDetails() {
        scope.launch {
            delay(3.seconds)
            subscriptionDetails.emit(
                LoadingResult.success(
                    SubscriptionProvider.SubscriptionDetails(
                        id = "debug",
                        value = Unit,
                        cost = null,
                        period = null,
                    )
                )
            )
        }
    }

    override fun requestSubscribe(subscription: SubscriptionProvider.SubscriptionDetails<*>) {
        scope.launch(CustomDispatchers.Main) {
            loading = true
            delay(3.seconds)
            settings.subscribed.value = true
            loading = false
        }
    }

    override fun getManageSubscriptionUrl(
        subscription: SubscriptionProvider.SubscriptionDetails<*>?,
    ) = "https://play.google.com/store/account/subscriptions"
}
