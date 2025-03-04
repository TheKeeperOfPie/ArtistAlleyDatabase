package com.thekeeperofpie.artistalleydatabase.utils.kotlin

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlin.time.Duration

fun <T> List<T>.splitAtIndex(index: Int) =
    if (index == size - 1) {
        this to emptyList()
    } else {
        subList(0, index) to subList(index, size)
    }

@ExperimentalCoroutinesApi
fun <T, R> Flow<T>.mapLatestNotNull(transform: suspend (value: T) -> R) =
    transformLatest {
        val result = transform(it)
        if (result != null) {
            emit(result)
        }
    }

@ExperimentalCoroutinesApi
inline fun <T, R> Flow<T>.flatMapLatestNotNull(crossinline transform: suspend (value: T) -> Flow<R?>): Flow<R> =
    transformLatest {
        @Suppress("UNCHECKED_CAST")
        emitAll(transform(it).filterNot { it == null } as Flow<R>)
    }

suspend fun <T> FlowCollector<T>.emitNotNull(value: T?) {
    if (value != null) emit(value)
}

@Suppress("NOTHING_TO_INLINE")
inline fun <Input, Output> suspend1(
    noinline block: suspend (Input) -> Output,
): suspend (Input) -> Output = block

infix fun <A, B, C> Pair<A, B>.to(third: C): Triple<A, B, C> = Triple(first, second, third)

fun <T> Flow<T>.chunked(maxSize: Int, maxDuration: Duration) = channelFlow {
    val chunk = mutableListOf<T>()
    var timeout: Job? = null

    suspend fun send() = chunk.takeIf(MutableList<T>::isNotEmpty)
        ?.toList()
        ?.also { chunk.clear() }
        ?.let { send(it) }

    collect {
        chunk.add(it)
        if (chunk.size >= maxSize) {
            send()
        } else if (timeout == null) {
            timeout = launch {
                delay(maxDuration)
                send()
                timeout = null
            }
        }
    }

    send()
}

@Suppress("UNCHECKED_CAST")
fun <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R> combine(
    flow0: Flow<T0>,
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    flow8: Flow<T8>,
    flow9: Flow<T9>,
    flow10: Flow<T10>,
    flow11: Flow<T11>,
    flow12: Flow<T12>,
    transform: suspend (T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) -> R,
): Flow<R> = kotlinx.coroutines.flow.combine(
    flow0,
    flow1,
    flow2,
    flow3,
    flow4,
    flow5,
    flow6,
    flow7,
    flow8,
    flow9,
    flow10,
    flow11,
    flow12,
) { args: Array<*> ->
    transform(
        args[0] as T0,
        args[1] as T1,
        args[2] as T2,
        args[3] as T3,
        args[4] as T4,
        args[5] as T5,
        args[6] as T6,
        args[7] as T7,
        args[8] as T8,
        args[9] as T9,
        args[10] as T10,
        args[11] as T11,
        args[12] as T12,
    )
}

@Suppress("UNCHECKED_CAST")
fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R> combine(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    flow8: Flow<T8>,
    flow9: Flow<T9>,
    flow10: Flow<T10>,
    flow11: Flow<T11>,
    flow12: Flow<T12>,
    flow13: Flow<T13>,
    flow14: Flow<T14>,
    transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) -> R,
): Flow<R> = kotlinx.coroutines.flow.combine(
    flow,
    flow2,
    flow3,
    flow4,
    flow5,
    flow6,
    flow7,
    flow8,
    flow9,
    flow10,
    flow11,
    flow12,
    flow13,
    flow14,
) { args: Array<*> ->
    transform(
        args[0] as T1,
        args[1] as T2,
        args[2] as T3,
        args[3] as T4,
        args[4] as T5,
        args[5] as T6,
        args[6] as T7,
        args[7] as T8,
        args[8] as T9,
        args[9] as T10,
        args[10] as T11,
        args[11] as T12,
        args[12] as T13,
        args[13] as T14,
    )
}

@Suppress("UNCHECKED_CAST")
fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R> combine(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    flow8: Flow<T8>,
    flow9: Flow<T9>,
    flow10: Flow<T10>,
    flow11: Flow<T11>,
    flow12: Flow<T12>,
    flow13: Flow<T13>,
    flow14: Flow<T14>,
    flow15: Flow<T15>,
    transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) -> R,
): Flow<R> = kotlinx.coroutines.flow.combine(
    flow,
    flow2,
    flow3,
    flow4,
    flow5,
    flow6,
    flow7,
    flow8,
    flow9,
    flow10,
    flow11,
    flow12,
    flow13,
    flow14,
    flow15,
) { args: Array<*> ->
    transform(
        args[0] as T1,
        args[1] as T2,
        args[2] as T3,
        args[3] as T4,
        args[4] as T5,
        args[5] as T6,
        args[6] as T7,
        args[7] as T8,
        args[8] as T9,
        args[9] as T10,
        args[10] as T11,
        args[11] as T12,
        args[12] as T13,
        args[13] as T14,
        args[14] as T15,
    )
}

inline fun <T> T.transformIf(shouldRun: Boolean, block: T.() -> T) =
    if (shouldRun) run(block) else this

fun <T> MutableSet<T>.toggle(value: T) {
    if (contains(value)) {
        remove(value)
    } else {
        add(value)
    }
}

fun <T> MutableList<T>.toggle(value: T) {
    if (contains(value)) {
        remove(value)
    } else {
        add(value)
    }
}
