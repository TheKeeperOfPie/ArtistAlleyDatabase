package com.thekeeperofpie.artistalleydatabase.compose

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Add a step to the back press exit transition, which registers a self-removing callback that also
 * invokes the next stage, calling and removing callbacks until the root back pressed behavior is
 * invoked and the screen actually exits.
 *
 * Allows layering multiple back exit animations in multiple locations in the [Composable] graph
 * without them being aware of each other.
 *
 * The root behavior which exits the screen should pass [terminal] as `true` and manually time the
 * real exit.
 *
 * Note that duration is not handled in any way, and must be accounted for by each stage manually.
 */
@Composable
fun AddBackPressTransitionStage(
    terminal: Boolean = false,
    onBackPressed: suspend OnBackPressedCallback.(CoroutineScope) -> Unit
) {
    val currentOnBackPressed = rememberUpdatedState(onBackPressed)
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
        ?.onBackPressedDispatcher
    val backCoroutineScope = rememberCoroutineScope()

    val backCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val onBackPressedCallback = this
                backCoroutineScope.launch {
                    currentOnBackPressed.value.invoke(
                        onBackPressedCallback,
                        backCoroutineScope
                    )
                }
                if (!terminal) {
                    remove()
                    backPressedDispatcher?.onBackPressed()
                }
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, backPressedDispatcher) {
        backPressedDispatcher?.addCallback(lifecycleOwner, backCallback)
        onDispose { backCallback.remove() }
    }
}