package com.thekeeperofpie.artistalleydatabase.utils_compose

import kotlin.coroutines.CoroutineContext

expect object ComposeUiDispatcher {
    val Main: CoroutineContext
}
