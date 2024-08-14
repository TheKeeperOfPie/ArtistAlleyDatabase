package com.thekeeperofpie.artistalleydatabase.utils.kotlin

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlin.coroutines.CoroutineContext

/**
 * TODO: This is broken
 */
object CustomDispatchers {

    var enabled = false
        private set

    @VisibleForTesting
    fun enable() {
        enabled = true
    }

    val Main: CoroutineContext
        get() = Dispatchers.Main

    val IO: CoroutineContext
        get() = Dispatchers.IO

    val Default: CoroutineContext
        get() =  Dispatchers.Default

    fun io(parallelism: Int) = if (enabled) {
        Dispatchers.IO
            .limitedParallelism(parallelism)
    } else {
        Dispatchers.IO.limitedParallelism(parallelism)
    }
}
