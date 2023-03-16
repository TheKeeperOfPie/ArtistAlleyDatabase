package com.thekeeperofpie.artistalleydatabase.android_utils.kotlin

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

fun <T> Iterable<T>.split(predicate: (T) -> Boolean): Pair<List<T>, List<T>> {
    val filtered = mutableListOf<T>()
    val remaining = mutableListOf<T>()
    forEach {
        if (predicate(it)) {
            filtered.add(it)
        } else {
            remaining.add(it)
        }
    }
    return filtered to remaining
}

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
    noinline block: suspend (Input) -> Output
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