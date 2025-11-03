package com.thekeeperofpie.artistalleydatabase.alley.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.backhandler.LocalCompatNavigationEventDispatcherOwner
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.NavHostController
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventInput
import com.thekeeperofpie.artistalleydatabase.alley.Destinations
import dev.zacsweers.metro.createGraphFactory
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.jetbrains.skiko.wasm.onWasmReady
import org.w3c.dom.StorageEvent
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import org.w3c.dom.events.KeyboardEvent

fun initWebSettings(onNewValue: (key: String, value: String?) -> Unit) {
    window.addEventListener("storage", object : EventListener {
        override fun handleEvent(event: Event) {
            event as StorageEvent
            val key = event.key ?: return
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
            val scope = rememberCoroutineScope()
            val component = createGraphFactory<ArtistAlleyWebComponent.Factory>().create(scope)
            KeyboardEventEffect()
            App(component = component)
        }
    }
}

@OptIn(InternalComposeUiApi::class)
@Composable
private fun KeyboardEventEffect() {
    val scope = rememberCoroutineScope()
    val keyEvents = remember {
        Channel<KeyboardEvent>(
            capacity = 5,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
    }
    val navigationEventDispatcherOwner = LocalCompatNavigationEventDispatcherOwner.current
    DisposableEffect(scope, navigationEventDispatcherOwner, keyEvents) {
        val navigationEventDispatcher =
            navigationEventDispatcherOwner?.navigationEventDispatcher
                ?: return@DisposableEffect onDispose {}
        val callback: (Event) -> Unit = {
            if (it is KeyboardEvent) {
                keyEvents.trySend(it)
            }
        }

        document.addEventListener("keydown", callback)
        val input = EscapeNavigationEventHandler(scope, keyEvents)
        navigationEventDispatcher.addInput(input)
        onDispose {
            document.removeEventListener("keydown", callback)
            navigationEventDispatcher.removeInput(input)
        }
    }
}

private class EscapeNavigationEventHandler(
    scope: CoroutineScope,
    keyEvents: Channel<KeyboardEvent>,
) : NavigationEventInput() {
    init {
        scope.launch {
            while (true) {
                val event = keyEvents.receive()
                if (event.key == "Escape") {
                    dispatchOnBackStarted(NavigationEvent())
                    dispatchOnBackCompleted()
                }
            }
        }
    }
}
