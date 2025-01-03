package com.thekeeperofpie.artistalleydatabase.utils.kotlin

import kotlinx.coroutines.CoroutineDispatcher

expect object PlatformDispatchers {
    val IO: CoroutineDispatcher
}
