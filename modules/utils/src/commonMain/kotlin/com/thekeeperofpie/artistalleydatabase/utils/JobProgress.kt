package com.thekeeperofpie.artistalleydatabase.utils

sealed interface JobProgress<out T> {
    data class Idle<T>(private val _unit: Unit = Unit) : JobProgress<T>
    data class Loading<T>(private val _unit: Unit = Unit) : JobProgress<T>
    sealed interface Finished<T> : JobProgress<T> {
        data class Result<T>(val value: T) : Finished<T>
        data class UnhandledError<T>(val throwable: Throwable?) : Finished<T>
    }
}
