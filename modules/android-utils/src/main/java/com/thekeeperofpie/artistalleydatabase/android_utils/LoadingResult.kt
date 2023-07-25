package com.thekeeperofpie.artistalleydatabase.android_utils

data class LoadingResult<T>(
    val loading: Boolean = false,
    val success: Boolean = false,
    val result: T? = null,
    val error: Pair<Int, Throwable?>? = null,
) {
    companion object {
        fun <T> loading() = LoadingResult<T>(loading = true)
        fun <T> empty() = LoadingResult<T>()
    }

    fun <Output> transformResult(transform: (T) -> Output): LoadingResult<Output> {
        val newResult = result?.let(transform)
        return LoadingResult(
            loading = loading,
            success = success,
            result = newResult,
            error = error,
        )
    }
}
