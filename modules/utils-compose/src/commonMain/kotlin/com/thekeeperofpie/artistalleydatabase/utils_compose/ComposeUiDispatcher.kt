package com.thekeeperofpie.artistalleydatabase.utils_compose

import app.cash.molecule.RecompositionMode
import kotlin.coroutines.CoroutineContext

expect object ComposeUiDispatcher {
    val Main: CoroutineContext
    val recompositionMode: RecompositionMode
}
