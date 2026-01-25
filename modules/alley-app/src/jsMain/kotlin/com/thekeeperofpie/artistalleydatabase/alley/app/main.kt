package com.thekeeperofpie.artistalleydatabase.alley.app

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.KeyboardEventEffect
import dev.zacsweers.metro.createGraphFactory
import kotlinx.browser.document
import org.jetbrains.skiko.wasm.onWasmReady

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    onWasmReady {
        ComposeViewport(document.body!!) {
            val scope = rememberCoroutineScope()
            val graph = createGraphFactory<ArtistAlleyWebGraph.Factory>().create(scope)
            KeyboardEventEffect()
            App(graph = graph)
        }
    }
}
