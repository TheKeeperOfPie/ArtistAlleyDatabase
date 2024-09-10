package com.thekeeperofpie.artistalleydatabase.utils_compose

import app.cash.molecule.RecompositionMode
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

actual object ComposeUiDispatcher {
    actual val Main: CoroutineContext = Dispatchers.Main
    actual val recompositionMode = RecompositionMode.Immediate
}
