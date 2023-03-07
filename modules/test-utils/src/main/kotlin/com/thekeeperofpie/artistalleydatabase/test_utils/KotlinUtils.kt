package com.thekeeperofpie.artistalleydatabase.test_utils

import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.withContext

suspend fun <T> withTestDispatcher(
    dispatcher: CoroutineDispatcher,
    block: suspend CoroutineScope.() -> T
) = withContext(CustomDispatchers.mainThreadLocal.asContextElement(dispatcher), block)

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun <T> TestScope.withDispatchers(block: suspend CoroutineScope.() -> T) =
    withTestDispatcher(StandardTestDispatcher(testScheduler), block)