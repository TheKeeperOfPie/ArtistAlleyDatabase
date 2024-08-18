package com.thekeeperofpie.artistalleydatabase.utils_compose

import org.jetbrains.compose.resources.StringResource

data class LoadingResult<T>(
    val loading: Boolean = false,
    val success: Boolean = false,
    val result: T? = null,
    val error: Pair<StringResource, Throwable?>? = null,
) {
    companion object {
        fun <T> loading() = LoadingResult<T>(loading = true)
        fun <T> empty() = LoadingResult<T>()
        fun <T> error(error: StringResource, throwable: Throwable? = null) =
            LoadingResult<T>(error = error to throwable)

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
