package com.thekeeperofpie.artistalleydatabase.utils_compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.InternalComposeUiApi
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventInput
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent

@OptIn(InternalComposeUiApi::class)
@Composable
fun KeyboardEventEffect() {
    val scope = rememberCoroutineScope()
    val keyEvents = remember {
        Channel<KeyboardEvent>(
            capacity = 5,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
    }
    val navigationEventDispatcherOwner = LocalNavigationEventDispatcherOwner.current
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
