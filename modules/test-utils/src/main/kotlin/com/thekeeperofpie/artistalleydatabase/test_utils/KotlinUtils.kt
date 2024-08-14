package com.thekeeperofpie.artistalleydatabase.test_utils

import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.withContext

suspend fun <T> TestScope.withDispatchers(block: suspend CoroutineScope.() -> T): T {
    if (!CustomDispatchers.enabled) {
        throw IllegalStateException("CustomDispatchers not enabled")
    }

    // TODO: Fix this
    return withContext(StandardTestDispatcher(testScheduler), block)
}
