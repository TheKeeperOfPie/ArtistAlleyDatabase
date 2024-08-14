package com.thekeeperofpie.artistalleydatabase.utils.kotlin

data class LoadingResult<T>(
    val loading: Boolean = false,
    val success: Boolean = false,
    val result: T? = null,
    val error: Pair<Int, Throwable?>? = null,
) {
    companion object {
        fun <T> loading() = LoadingResult<T>(loading = true)
        fun <T> empty() = LoadingResult<T>()
        // TODO: Support StringResource
        fun <T> error(errorTextRes: Int, throwable: Throwable? = null) =
            LoadingResult<T>(error = errorTextRes to throwable)

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
