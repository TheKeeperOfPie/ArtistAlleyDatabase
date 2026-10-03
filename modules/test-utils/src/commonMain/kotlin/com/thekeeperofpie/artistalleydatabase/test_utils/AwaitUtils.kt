package com.thekeeperofpie.artistalleydatabase.test_utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestDispatcher
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

suspend fun TestDispatcher.yieldingWaitUntil(
    timeout: Duration = 5.seconds,
    step: Duration = 10.milliseconds,
    predicate: suspend () -> Boolean,
) {
    var succeeded = false
    repeat((timeout / step).toInt()) {
        if (succeeded) return@repeat
        delay(step)
        scheduler.runCurrent()
        scheduler.advanceUntilIdle()
        succeeded = predicate()
    }
    assertTrue(succeeded)
}
