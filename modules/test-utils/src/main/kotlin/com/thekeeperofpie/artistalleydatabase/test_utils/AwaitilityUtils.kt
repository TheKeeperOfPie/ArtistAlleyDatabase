package com.thekeeperofpie.artistalleydatabase.test_utils

import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import org.awaitility.core.ConditionFactory
import org.awaitility.core.ConditionTimeoutException
import org.mockito.Mockito
import kotlin.reflect.KFunction1
import kotlin.reflect.jvm.javaMethod
import kotlin.time.Duration
import kotlin.time.toJavaDuration

fun ConditionFactory.atLeast(duration: Duration) = atLeast(duration.toJavaDuration())!!
fun ConditionFactory.during(duration: Duration) = during(duration.toJavaDuration())!!
fun ConditionFactory.atMost(duration: Duration) = atMost(duration.toJavaDuration())!!

fun ConditionFactory.ignoringTimeouts(ignore: Boolean, block: ConditionFactory.() -> Unit) {
    if (ignore) {
        try {
            block()
        } catch (ignored: ConditionTimeoutException) {
        }
    } else block()
}

fun ConditionFactory.untilEither(
    first: ConditionFactory.() -> Unit,
    second: ConditionFactory.() -> Unit,
) {
    val channel = Channel<Result<Unit>>()
    val result = runBlocking {
        coroutineScope {
            listOf(
                async { channel.send(runCatching { first() }) },
                async { channel.send(runCatching { second() }) },
            )
            channel.consumeAsFlow().take(1)
                .first()
        }
    }
    if (result.isFailure) {
        throw result.exceptionOrNull() ?: AssertionError("Expected did not happen")
    }
}

fun <Receiver> ConditionFactory.untilCalled(
    receiver: Receiver,
    kFunction: KFunction1<Receiver, *>
) = until {
    Mockito.mockingDetails(receiver).invocations
        .any { it.method == kFunction.javaMethod }
}