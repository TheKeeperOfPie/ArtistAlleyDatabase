package com.thekeeperofpie.artistalleydatabase.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import java.util.LinkedList

fun <T> Flow<T>.nullable() = this as Flow<T?>

fun <T> Flow<T>.distinctWithBuffer(bufferSize: Int): Flow<T> = flow {
    val past = LinkedList<T>()
    collect {
        val contains = past.contains(it)
        if (!contains) {
            while (past.size > bufferSize) {
                past.removeFirst()
            }
            past.addLast(it)
            emit(it)
        }
    }
}

fun <T> Flow<T>.start(value: T) = onStart { emit(value) }