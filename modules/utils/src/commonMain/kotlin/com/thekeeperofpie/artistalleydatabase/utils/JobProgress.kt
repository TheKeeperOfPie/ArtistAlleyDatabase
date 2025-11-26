package com.thekeeperofpie.artistalleydatabase.utils

sealed interface JobProgress {
    data object Idle : JobProgress
    data object Loading : JobProgress
    sealed interface Finished : JobProgress {
        data object Success : Finished
        data class UnhandledError(val throwable: Throwable) : Finished
    }
}
