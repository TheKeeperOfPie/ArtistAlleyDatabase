package com.thekeeperofpie.artistalleydatabase.alley.web

import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeInit
import dev.zacsweers.metro.createGraphFactory
import kotlinx.browser.document
import org.jetbrains.skiko.wasm.onWasmReady

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    onWasmReady {
        ComposeInit.init()
        ComposeViewport(document.body!!) {
            val scope = rememberCoroutineScope()
            val graph = remember(scope) {
                createGraphFactory<ArtistAlleyWebGraph.Factory>().create(scope)
            }
            App(graph = graph)
        }
    }
}
