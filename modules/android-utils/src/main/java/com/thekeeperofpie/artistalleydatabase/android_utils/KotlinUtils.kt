package com.thekeeperofpie.artistalleydatabase.android_utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.transformLatest

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