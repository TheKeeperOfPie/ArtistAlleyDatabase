@file:Suppress("UNCHECKED_CAST")

package com.thekeeperofpie.artistalleydatabase.android_utils.kotlin

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

fun <T1, T2, R> combineStates(
    flow: StateFlow<T1>,
    flow2: StateFlow<T2>,
    transform: (T1, T2) -> R
): StateFlow<R> = combineStatesUnsafe(flow, flow2) { args: Array<*> ->
    transform(
        args[0] as T1,
        args[1] as T2,
    )
}

fun <T1, T2, T3, R> combineStates(
    flow: StateFlow<T1>,
    flow2: StateFlow<T2>,
    flow3: StateFlow<T3>,
    transform: (T1, T2, T3) -> R
): StateFlow<R> = combineStatesUnsafe(flow, flow2, flow3) { args: Array<*> ->
    transform(
        args[0] as T1,
        args[1] as T2,
        args[2] as T3,
    )
}

fun <T1, T2, T3, T4, R> combineStates(
    flow: StateFlow<T1>,
    flow2: StateFlow<T2>,
    flow3: StateFlow<T3>,
    flow4: StateFlow<T4>,
    transform: (T1, T2, T3, T4) -> R
): StateFlow<R> = combineStatesUnsafe(flow, flow2, flow3, flow4) { args: Array<*> ->
    transform(
        args[0] as T1,
        args[1] as T2,
        args[2] as T3,
        args[3] as T4,
    )
}

private inline fun <reified T, R> combineStatesUnsafe(
    vararg flows: StateFlow<T>,
    crossinline transform: (Array<T>) -> R
): StateFlow<R> = object : StateFlow<R> {
    override val replayCache
        get() = listOf(value)
    override val value: R
        get() = transform(currentValues)

    private val currentValues
        get() = Array(flows.size) { flows[it].value }

    override suspend fun collect(collector: FlowCollector<R>): Nothing =
        coroutineScope {
            combine(*flows) { transform(it) }
                .stateIn(
                    scope = this,
                    started = SharingStarted.Eagerly,
                    initialValue = transform(currentValues),
                )
                .collect(collector)
        }
}
