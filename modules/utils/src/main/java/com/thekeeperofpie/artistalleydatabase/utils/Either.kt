package com.thekeeperofpie.artistalleydatabase.utils

sealed class Either<Left, Right>(private val value: Any?) {

    data class Left<Left, Right>(val value: Left) : Either<Left, Right>(value)
    data class Right<Left, Right>(val value: Right) : Either<Left, Right>(value)

    @Suppress("UNCHECKED_CAST")
    fun leftOrNull() = value.takeIf { this is Either.Left } as Left?

    @Suppress("UNCHECKED_CAST")
    fun rightOrNull() = value.takeIf { this is Either.Right } as Right?

    fun eitherValueUnchecked() = value
}