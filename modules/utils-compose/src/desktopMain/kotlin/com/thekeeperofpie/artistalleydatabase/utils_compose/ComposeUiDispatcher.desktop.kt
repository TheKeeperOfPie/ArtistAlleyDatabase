package com.thekeeperofpie.artistalleydatabase.utils_compose

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

actual object ComposeUiDispatcher {
    actual val Main: CoroutineContext = Dispatchers.Main
}
