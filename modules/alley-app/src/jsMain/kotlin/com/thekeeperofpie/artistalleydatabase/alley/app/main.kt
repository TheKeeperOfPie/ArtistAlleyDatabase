package com.thekeeperofpie.artistalleydatabase.alley.app

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.NavHostController
import com.thekeeperofpie.artistalleydatabase.alley.Destinations
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import org.jetbrains.skiko.wasm.onWasmReady
import org.w3c.dom.StorageEvent
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import org.w3c.dom.events.KeyboardEvent

fun initWebSettings(onNewValue: (key: String, value: String?) -> Unit) {
    window.addEventListener("storage", object : EventListener {
        override fun handleEvent(event: Event) {
            event as StorageEvent
            val key = event.key ?: return@handleEvent
            onNewValue(key, event.newValue)
        }
    })
}

@OptIn(ExperimentalBrowserHistoryApi::class)
actual suspend fun bindToNavigationFixed(navHostController: NavHostController, deepLinker: DeepLinker) {
    val route = window.location.hash.substringAfter('#', "")
    if (route.startsWith("import")) {
        navHostController.navigate(Destinations.Import(route.removePrefix("import=")))
    }
    window.bindToNavigationFixed(navHostController, deepLinker)
}

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    onWasmReady {
        ComposeViewport(document.body!!) {
            val keyEvents = remember {
                Channel<WrappedKeyboardEvent>(
                    capacity = 5,
                    onBufferOverflow = BufferOverflow.DROP_OLDEST
                )
            }
            DisposableEffect(keyEvents) {
                val callback: (Event) -> Unit = {
                    if (it is KeyboardEvent) {
                        keyEvents.trySend(WrappedKeyboardEvent(it.key))
                    }
                }

                document.addEventListener("keydown", callback)
                onDispose { document.removeEventListener("keydown", callback) }
            }
            val scope = rememberCoroutineScope()
            val component = ArtistAlleyWebComponent::class.create(scope)
            App(component = component, keyEvents)
        }
    }
}
