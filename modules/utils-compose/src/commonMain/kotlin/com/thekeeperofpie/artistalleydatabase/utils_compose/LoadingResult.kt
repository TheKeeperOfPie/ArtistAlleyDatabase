package com.thekeeperofpie.artistalleydatabase.utils_compose

import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.transformIf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import org.jetbrains.compose.resources.StringResource

data class LoadingResult<T>(
    val loading: Boolean = false,
    val success: Boolean = false,
    val result: T? = null,
    val error: Pair<StringResourceCompat, Throwable?>? = null,
) {
    companion object {
        fun <T> loading() = LoadingResult<T>(loading = true)
        fun <T> empty() = LoadingResult<T>()
        fun <T> error(error: StringResource, throwable: Throwable? = null) =
            LoadingResult<T>(error = StringResourceCompose(error) to throwable)
        fun <T> error(error: Int, throwable: Throwable? = null) =
            LoadingResult<T>(error = StringResourceId(error) to throwable)

        fun <T> success(value: T) = LoadingResult(loading = false, success = true, result = value)
    }

    fun isEmpty() = !loading && !success && result == null && error == null

    suspend fun <Output> transformResult(transform: suspend (T) -> Output?): LoadingResult<Output> {
        val newResult = result?.let { transform(it) }
        return LoadingResult(
            loading = loading,
            success = success,
            result = newResult,
            error = error,
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun <T> flowForRefreshableContent(
    refresh: StateFlow<*>,
    errorTextRes: StringResourceCompat,
    producer: suspend () -> Flow<T>,
) = refresh
    .flatMapLatest {
        producer()
            .map {
                LoadingResult(
                    success = true,
                    result = it
                )
            }
            .catch { emit(LoadingResult(error = errorTextRes to it)) }
            .startWith(
                LoadingResult(
                    loading = true,
                    success = true
                )
            )
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
