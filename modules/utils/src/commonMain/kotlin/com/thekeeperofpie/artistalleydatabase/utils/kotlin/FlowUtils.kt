@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalCoroutinesApi::class)

package com.thekeeperofpie.artistalleydatabase.utils.kotlin

import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.utils.LoadingResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold

fun <T> Flow<T>.nullable() = this as Flow<T?>

fun <T> Flow<T>.distinctWithBuffer(bufferSize: Int): Flow<T> = flow {
    val past = mutableListOf<T>()
    collect {
        val contains = past.contains(it)
        if (!contains) {
            while (past.size > bufferSize) {
                past.removeFirst()
            }
            past.add(it)
            emit(it)
        }
    }
}

fun <T> flowForRefreshableContent(
    refresh: StateFlow<*>,
    errorTextRes: Int,
    producer: suspend () -> Flow<T>,
) = refresh
    .flatMapLatest {
        producer()
            .map { LoadingResult(success = true, result = it) }
            .catch { emit(LoadingResult(error = errorTextRes to it)) }
            .startWith(LoadingResult(loading = true, success = true))
    }
    .distinctUntilChanged()
    .runningFold(LoadingResult.loading<T>()) { accumulator, value ->
        value.transformIf(value.loading && value.result == null) {
            copy(result = accumulator.result)
        }
    }

fun <T> Flow<LoadingResult<T>>.foldPreviousResult(
    initialResult: LoadingResult<T> = LoadingResult.loading<T>(),
) =
    runningFold(initialResult) { accumulator, value ->
        value.transformIf(value.loading && value.result == null) {
            copy(result = accumulator.result)
        }
    }
