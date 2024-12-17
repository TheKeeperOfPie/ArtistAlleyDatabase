package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

object ComposeUtils {

    internal val whileSubscribedFiveSeconds =
        SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds)
}

context(ViewModel)
@Suppress("CONTEXT_RECEIVERS_DEPRECATED")
fun <T : Any> Flow<LoadingResult<T>>.stateInForCompose() =
    stateIn(viewModelScope, ComposeUtils.whileSubscribedFiveSeconds, LoadingResult.loading())

context(ViewModel)
@Suppress("CONTEXT_RECEIVERS_DEPRECATED")
fun <T : Any> Flow<T>.stateInForCompose(initial: T) =
    stateIn(viewModelScope, ComposeUtils.whileSubscribedFiveSeconds, initial)
