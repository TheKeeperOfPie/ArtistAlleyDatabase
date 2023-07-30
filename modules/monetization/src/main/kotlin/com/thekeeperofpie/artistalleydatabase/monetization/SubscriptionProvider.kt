package com.thekeeperofpie.artistalleydatabase.monetization

import androidx.activity.ComponentActivity
import androidx.compose.runtime.staticCompositionLocalOf
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import kotlinx.coroutines.flow.StateFlow

interface SubscriptionProvider {

    val subscriptionDetails: StateFlow<LoadingResult<SubscriptionDetails<*>>>

    val error: StateFlow<Pair<Int, Throwable?>?>

    val loading: Boolean

    fun initialize(activity: ComponentActivity)

    fun loadSubscriptionDetails()

    fun requestSubscribe(subscription: SubscriptionDetails<*>)

    fun getManageSubscriptionUrl(subscription: SubscriptionDetails<*>?): String

    data class SubscriptionDetails<T>(
        val id: String,
        val value: T,
    )
}

val LocalSubscriptionProvider = staticCompositionLocalOf<SubscriptionProvider?> { null }
