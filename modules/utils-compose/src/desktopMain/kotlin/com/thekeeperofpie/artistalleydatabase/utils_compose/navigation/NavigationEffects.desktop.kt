package com.thekeeperofpie.artistalleydatabase.utils_compose.navigation

import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.onClick
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerButton
import androidx.navigationevent.NavigationEventInput
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

@OptIn(InternalComposeUiApi::class)
@Composable
fun Modifier.mouseNavigationEvents(): Modifier {
    val scope = rememberCoroutineScope()
    val navEvents = remember { Channel<Boolean>(1) }
    val navigationEventDispatcherOwner = LocalNavigationEventDispatcherOwner.current
    DisposableEffect(scope, navigationEventDispatcherOwner, navEvents) {
        val navigationEventDispatcher =
            navigationEventDispatcherOwner?.navigationEventDispatcher
                ?: return@DisposableEffect onDispose {}
        val input = MouseNavigationEventHandler(scope, navEvents)
        navigationEventDispatcher.addInput(input)
        onDispose { navigationEventDispatcher.removeInput(input) }
    }

    return onClick(
        matcher = PointerMatcher.mouse(PointerButton.Back),
        onClick = { navEvents.trySend(false) },
    ).onClick(
        matcher = PointerMatcher.mouse(PointerButton.Forward),
        onClick = { navEvents.trySend(true) },
    )
}

private class MouseNavigationEventHandler(
    scope: CoroutineScope,
    navEvents: Channel<Boolean>,
) : NavigationEventInput() {
    init {
        scope.launch {
            while (true) {
                val isForward = navEvents.receive()
                if (isForward) {
                    dispatchOnForwardCompleted()
                } else {
                    dispatchOnBackCompleted()
                }
            }
        }
    }
}
