package com.thekeeperofpie.artistalleydatabase.utils

sealed interface JobProgress {
    data object Idle : JobProgress
    data object Loading : JobProgress
    sealed interface Finished : JobProgress {
        data class Result<T>(val value: T) : Finished
        data class UnhandledError(val throwable: Throwable?) : Finished
    }
}
