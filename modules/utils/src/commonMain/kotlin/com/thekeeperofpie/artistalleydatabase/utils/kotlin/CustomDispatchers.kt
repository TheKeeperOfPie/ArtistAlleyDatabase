package com.thekeeperofpie.artistalleydatabase.utils.kotlin

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Really hacky, very bad way to swap [Dispatchers] during tests.
 *
 * Holds a [ThreadLocal] which can be edited for a specific block of code.
 *
 * Requires the usage of this class in place of all [Dispatchers] calls.
 *
 * Usage:
 * ```kotlin
 * fun function() {
 *     withContext(CustomDispatchers.IO) {
 *         // Do something on IO thread
 *         withContext(CustomDispatchers.Main) {
 *             // Do something on main thread
 *         }
 *     }
 * }
 * ```
 *
 * Test code:
 * ```kotlin
 * class TestClass {
 *     companion object {
 *         @JvmStatic
 *         fun enableTestDispatchers() {
 *             CustomDispatchers.enable()
 *         }
 *     }
 *
 *     @Test
 *     fun test() {
 *         val testScope = TestScope()
 *         testScope.withDispatchers {
 *             // Test logic that accesses [CustomDispatchers.Main]
 *             testScope.advancedUntilIdle()
 *         }
 *     }
 * }
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class)
object CustomDispatchers {

    val mainThreadLocal = ThreadLocal<CoroutineDispatcher>()
    val ioThreadLocal = ThreadLocal<CoroutineDispatcher>()
    val defaultThreadLocal = ThreadLocal<CoroutineDispatcher>()

    var enabled = false
        private set

    @VisibleForTesting
    fun enable() {
        enabled = true
    }

    val Main: CoroutineContext
        get() = if (enabled) {
            (mainThreadLocal.get() ?: Dispatchers.Main).withContextElements()
        } else {
            Dispatchers.Main
        }

    val IO: CoroutineContext
        get() = if (enabled) {
            (ioThreadLocal.get() ?: Dispatchers.IO).withContextElements()
        } else {
            Dispatchers.IO
        }

    val Default: CoroutineContext
        get() = if (enabled) {
            (defaultThreadLocal.get() ?: Dispatchers.Default).withContextElements()
        } else {
            Dispatchers.Default
        }

    fun io(parallelism: Int) = if (enabled) {
        (ioThreadLocal.get() ?: Dispatchers.IO)
            .limitedParallelism(parallelism)
            .withContextElements()
    } else {
        Dispatchers.IO.limitedParallelism(parallelism)
    }

    private fun CoroutineDispatcher.withContextElements() =
        this + mainThreadLocal.asContextElement(mainThreadLocal.get() ?: Dispatchers.Main) +
                ioThreadLocal.asContextElement(ioThreadLocal.get() ?: Dispatchers.IO) +
                defaultThreadLocal.asContextElement(defaultThreadLocal.get() ?: Dispatchers.Default)
}
