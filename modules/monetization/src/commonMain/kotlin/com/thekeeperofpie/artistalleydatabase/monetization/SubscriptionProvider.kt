package com.thekeeperofpie.artistalleydatabase.monetization

import androidx.activity.ComponentActivity
import androidx.compose.runtime.staticCompositionLocalOf
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.StringResource
import java.time.Period

interface SubscriptionProvider {

    val subscriptionDetails: StateFlow<LoadingResult<SubscriptionDetails<*>>>

    val error: StateFlow<Pair<StringResource, Throwable?>?>

    val loading: Boolean

    fun initialize(activity: ComponentActivity)

    fun loadSubscriptionDetails()

    fun requestSubscribe(subscription: SubscriptionDetails<*>)

    fun getManageSubscriptionUrl(subscription: SubscriptionDetails<*>?): String

    data class SubscriptionDetails<T>(
        val id: String,
        val value: T,
        val cost: String?,
        val period: Period?,
    )
}

val LocalSubscriptionProvider = staticCompositionLocalOf<SubscriptionProvider?> { null }
