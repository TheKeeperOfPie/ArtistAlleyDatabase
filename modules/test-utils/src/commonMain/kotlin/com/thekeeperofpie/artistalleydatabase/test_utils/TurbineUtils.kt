package com.thekeeperofpie.artistalleydatabase.test_utils

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow

suspend fun <T> ReceiveTurbine<T>.await(predicate: (T) -> Boolean): T {
    while (true) {
        val item = awaitItem()
        if (predicate(item)) {
            return item
        }
    }
}

suspend fun <T> Flow<T>.await(predicate: (T) -> Boolean) = test { await(predicate) }
