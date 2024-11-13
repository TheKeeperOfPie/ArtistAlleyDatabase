package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.utils.Either
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
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
import org.jetbrains.compose.resources.stringResource

@Immutable
data class LoadingResult<T>(
    val loading: Boolean = false,
    val success: Boolean = false,
    val result: T? = null,
    val error: Error? = null,
) {
    companion object {
        fun <T> loading() = LoadingResult<T>(loading = true)
        fun <T> empty() = LoadingResult<T>()
        fun <T> error(error: String, throwable: Throwable? = null) =
            LoadingResult<T>(error = Error(error, throwable))
        fun <T> error(error: StringResource, throwable: Throwable? = null) =
            LoadingResult<T>(error = Error(error, throwable))

        fun <T> success(value: T) = LoadingResult(loading = false, success = true, result = value)

        fun <T> combine(
            vararg results: LoadingResult<T>,
            combiner: (List<T>) -> T,
        ): LoadingResult<T> {
            val loading = results.any { it.loading }
            return LoadingResult(
                loading = loading,
                success = !loading,
                result = combiner(results.mapNotNull { it.result }),
                error = results.firstNotNullOfOrNull { it.error },
            )
        }
    }

    fun isEmpty() = !loading && !success && result == null && error == null

    inline fun <Output> transformResult(transform: (T) -> Output?): LoadingResult<Output> {
        val newResult = result?.let { transform(it) }
        return LoadingResult(
            loading = loading,
            success = success,
            result = newResult,
            error = error,
        )
    }

    data class Error(
        val message: Either<String, StringResource>,
        val throwable: Throwable?,
    ) {
        constructor(message: String, throwable: Throwable? = null) :
                this(Either.Left(message), throwable)

        constructor(message: StringResource, throwable: Throwable? = null) :
                this(Either.Right(message), throwable)

        @Composable
        fun message() = when (message) {
            is Either.Left -> message.value
            is Either.Right -> stringResource(message.value)
        }
    }
}

fun <T> flowForRefreshableContent(
    refresh: RefreshFlow,
    errorTextRes: StringResource,
    producer: suspend () -> Flow<T>,
) = flowForRefreshableContent(
    refresh = refresh.updates,
    errorTextRes = errorTextRes,
    producer = producer,
)

@OptIn(ExperimentalCoroutinesApi::class)
fun <T> flowForRefreshableContent(
    refresh: StateFlow<*>,
    errorTextRes: StringResource,
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
            .catch { emit(LoadingResult(error = LoadingResult.Error(errorTextRes, it))) }
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
