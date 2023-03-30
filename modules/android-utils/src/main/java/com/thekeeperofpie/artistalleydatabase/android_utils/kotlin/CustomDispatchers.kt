package com.thekeeperofpie.artistalleydatabase.android_utils.kotlin

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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
object CustomDispatchers {

    val mainThreadLocal = ThreadLocal<CoroutineDispatcher>()

    private var enabled = false

    @VisibleForTesting
    fun enable() {
        enabled = true
    }

    val Main: CoroutineContext
        get() = if (enabled) {
            (mainThreadLocal.get() ?: Dispatchers.Main).let {
                it + mainThreadLocal.asContextElement(it)
            }
        } else {
            Dispatchers.Main
        }

    val IO: CoroutineContext
        get() = if (enabled) {
            Dispatchers.IO + mainThreadLocal.asContextElement(mainThreadLocal.get() ?: Dispatchers.Main)
        } else {
            Dispatchers.IO
        }

    val Default: CoroutineContext
        get() = if (enabled) {
            Dispatchers.Default + mainThreadLocal.asContextElement(mainThreadLocal.get() ?: Dispatchers.Main)
        } else {
            Dispatchers.Default
        }
}