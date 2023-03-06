package com.thekeeperofpie.artistalleydatabase.test_utils

import org.mockito.Answers
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.kotlin.whenever
import org.mockito.stubbing.Answer

// Must be public for inline methods below to work
val ANSWER_THROWS = Answer {
    when (val name = it.method.name) {
        // Delegate to the actual toString, since that will probably not be mocked
        "toString" -> Answers.CALLS_REAL_METHODS.answer(it)
        else -> {
            val arguments = it.arguments
                ?.takeUnless { it.isEmpty() }
                ?.mapIndexed { index, arg ->
                    try {
                        arg?.toString()
                    } catch (e: Exception) {
                        "toString[$index] threw ${e.message}"
                    }
                }
                ?.joinToString()
                ?: "no arguments"

            throw UnsupportedOperationException(
                "${it.mock::class.java.simpleName}#$name with $arguments should not be called"
            )
        }
    }
}

fun <Type : Any?> whenever(mock: Type, block: InvocationOnMock.() -> Type) =
    whenever(mock).thenAnswer { block(it) }!!

fun <Receiver: Any, Type : Any?> Receiver.doNothing(mock: Receiver.() -> Type) =
    org.mockito.kotlin.doNothing().whenever(this).mock()

inline fun <reified T> spyStrict(value: T?, block: T.() -> Unit = {}): T {
    val swappingAnswer = object : Answer<Any?> {
        var delegate: Answer<*> = Answers.RETURNS_DEFAULTS

        override fun answer(invocation: InvocationOnMock?): Any? {
            return delegate.answer(invocation)
        }
    }

    val settings = Mockito.withSettings()
        .spiedInstance(value)
        .defaultAnswer(swappingAnswer)

    return Mockito.mock(T::class.java, settings)
        .apply(block)
        .also {
            // To allow Mockito.when() usage inside block, only swap to throwing afterwards
            swappingAnswer.delegate = ANSWER_THROWS
        }
}

inline fun <reified Type> mockStrict(block: Type.() -> Unit = {}) = spyStrict(null, block)

inline fun <reified Type : Any?> mockWhen(mock: Type, crossinline block: Type.() -> Unit = {}) {
    whenever(mock) {
        mockStrict {
            block()
        }
    }
}