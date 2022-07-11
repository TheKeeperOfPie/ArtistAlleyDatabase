package com.thekeeperofpie.artistalleydatabase.utils

sealed class Either<Left, Right>(val value: Any?) {

    data class Left<Left, Right>(val left: Left) : Either<Left, Right>(left)
    data class Right<Left, Right>(val right: Right) : Either<Left, Right>(right)
}