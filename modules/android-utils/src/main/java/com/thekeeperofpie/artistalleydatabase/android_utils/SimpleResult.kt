package com.thekeeperofpie.artistalleydatabase.android_utils

sealed class SimpleResult<Value> {

    companion object {
        fun <Value> successIfNotNull(value: Value?): SimpleResult<Value> =
            if (value == null) Failure() else Success(value)
    }

    fun getOrThrow() = (this as Success).value
    fun getOrNull() = (this as? Success)?.value

    data class Success<Value>(val value: Value) : SimpleResult<Value>()
    data class Failure<Value>(val exception: Throwable? = null) : SimpleResult<Value>()
}
