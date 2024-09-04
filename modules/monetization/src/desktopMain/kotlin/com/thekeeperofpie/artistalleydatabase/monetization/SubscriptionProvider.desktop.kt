package com.thekeeperofpie.artistalleydatabase.monetization

import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.StringResource

actual interface SubscriptionProvider {
    actual val subscriptionDetails: StateFlow<LoadingResult<SubscriptionDetails<*>>>
    actual val error: StateFlow<Pair<StringResource, Throwable?>?>
    actual val loading: Boolean
    actual fun loadSubscriptionDetails()
    actual fun requestSubscribe(subscription: SubscriptionDetails<*>)
    actual fun getManageSubscriptionUrl(subscription: SubscriptionDetails<*>?): String
}
