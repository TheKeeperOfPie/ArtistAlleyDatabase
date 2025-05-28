package com.thekeeperofpie.artistalleydatabase.utils.kotlin

import kotlinx.coroutines.Dispatchers

actual object PlatformDispatchers {
    actual val IO = Dispatchers.Default
}
