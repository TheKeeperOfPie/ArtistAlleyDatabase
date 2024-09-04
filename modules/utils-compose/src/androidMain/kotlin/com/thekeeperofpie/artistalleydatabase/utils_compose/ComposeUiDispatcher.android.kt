package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.ui.platform.AndroidUiDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

actual object ComposeUiDispatcher {
    actual val Main: CoroutineContext = AndroidUiDispatcher.Main
}
