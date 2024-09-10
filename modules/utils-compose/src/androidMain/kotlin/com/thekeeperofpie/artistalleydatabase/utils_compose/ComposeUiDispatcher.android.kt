package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.ui.platform.AndroidUiDispatcher
import app.cash.molecule.RecompositionMode
import kotlin.coroutines.CoroutineContext

actual object ComposeUiDispatcher {
    actual val Main: CoroutineContext = AndroidUiDispatcher.Main
    actual val recompositionMode = RecompositionMode.ContextClock
}
