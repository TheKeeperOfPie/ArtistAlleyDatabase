package com.thekeeperofpie.artistalleydatabase.test_utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KProperty

/**
 * Allows initialization of thread-local, class level properties which are propagated automatically
 * through [runTest] for coroutine contexts. This allows parallel test execution with class
 * property access, which simplifies code by avoiding method-local mock creation.
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class TestBase {

    private val collector = CollectorRule()

    protected fun runTest(testBody: suspend TestScope.() -> Unit) = collector.runTest(testBody)

    protected fun <T> delegate(init: () -> T) = KotlinThreadLocalContextDelegate(collector, init)

    protected class CollectorRule {

        val delegates = mutableListOf<KotlinThreadLocalContextDelegate<*>>()

        fun runTest(testBody: suspend TestScope.() -> Unit) = kotlinx.coroutines.test.runTest {
            val newContext: CoroutineContext =
                delegates.fold(coroutineContext) { context, delegate ->
                    context + delegate.contextElement()
                }
            withContext(newContext) {
                testBody()
            }
        }
    }

    protected class KotlinThreadLocalContextDelegate<T>(collector: CollectorRule, init: () -> T) {

        private val threadLocal = ThreadLocal.withInitial(init)

        init {
            collector.delegates += this
        }

        operator fun getValue(thisRef: Any?, property: KProperty<*>) = threadLocal.get()!!

        fun contextElement() = threadLocal.asContextElement(threadLocal.get()!!)
    }
}