package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
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

@Composable
fun <T> MutableStateFlow<T>.collectAsMutableStateWithLifecycle(
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    context: CoroutineContext = EmptyCoroutineContext,
): MutableState<T> {
    val result = remember(this) { MutableStateFlowState(this) }
    @Suppress("CHANGING_ARGUMENTS_EXECUTION_ORDER_FOR_NAMED_VARARGS")
    LaunchedEffect(this, lifecycle, minActiveState, context) {
        lifecycle.repeatOnLifecycle(minActiveState) {
            if (context == EmptyCoroutineContext) {
                collect { result.state.value = it }
            } else withContext(context) {
                collect { result.state.value = it }
            }
        }
    }
    return result
}

internal class MutableStateFlowState<T>(private val flow: MutableStateFlow<T>) : MutableState<T> {

    val state = mutableStateOf(flow.value)

    override var value: T
        get() = state.value
        set(value) {
            state.value = value
            flow.value = value
        }

    override fun component1()  = value
    override fun component2(): (T) -> Unit = { value = it }
}
