package com.thekeeperofpie.artistalleydatabase.monetization

import androidx.compose.runtime.staticCompositionLocalOf
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.DateTimePeriod
import org.jetbrains.compose.resources.StringResource

expect interface SubscriptionProvider {

    val subscriptionDetails: StateFlow<LoadingResult<SubscriptionDetails<*>>>

    val error: StateFlow<Pair<StringResource, Throwable?>?>

    val loading: Boolean

    fun loadSubscriptionDetails()

    fun requestSubscribe(subscription: SubscriptionDetails<*>)

    fun getManageSubscriptionUrl(subscription: SubscriptionDetails<*>?): String
}

data class SubscriptionDetails<T>(
    val id: String,
    val value: T,
    val cost: String?,
    val period: DateTimePeriod?,
)

val LocalSubscriptionProvider = staticCompositionLocalOf<SubscriptionProvider?> { null }
