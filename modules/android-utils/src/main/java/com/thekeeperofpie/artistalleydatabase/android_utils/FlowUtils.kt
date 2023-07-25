@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thekeeperofpie.artistalleydatabase.android_utils

import androidx.annotation.StringRes
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.transformIf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import java.util.LinkedList

fun <T> Flow<T>.nullable() = this as Flow<T?>

fun <T> Flow<T>.distinctWithBuffer(bufferSize: Int): Flow<T> = flow {
    val past = LinkedList<T>()
    collect {
        val contains = past.contains(it)
        if (!contains) {
            while (past.size > bufferSize) {
                past.removeFirst()
            }
            past.addLast(it)
            emit(it)
        }
    }
}

fun <T> flowForRefreshableContent(
    refresh: StateFlow<*>,
    @StringRes errorTextRes: Int,
    producer: suspend () -> Flow<T>,
) = refresh
    .flatMapLatest {
        producer()
            .map { LoadingResult(success = true, result = it) }
            .catch { emit(LoadingResult(error = errorTextRes to it)) }
            .startWith(LoadingResult(loading = true, success = true))
    }
    .distinctUntilChanged()
    .runningFold(LoadingResult<T>(loading = true, success = true)) { accumulator, value ->
        value.transformIf(value.loading && value.result == null) {
            copy(result = accumulator.result)
        }
    }
