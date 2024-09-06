@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalCoroutinesApi::class)

package com.thekeeperofpie.artistalleydatabase.utils.kotlin

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun <T> Flow<T>.nullable() = this as Flow<T?>

fun <T> Flow<T>.distinctWithBuffer(bufferSize: Int): Flow<T> = flow {
    val past = mutableListOf<T>()
    collect {
        val contains = past.contains(it)
        if (!contains) {
            while (past.size > bufferSize) {
                past.removeFirst()
            }
            past.add(it)
            emit(it)
        }
    }
}
