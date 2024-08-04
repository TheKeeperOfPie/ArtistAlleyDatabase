package com.thekeeperofpie.artistalleydatabase.compose

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun BackPressStageHandler(content: @Composable () -> Unit) {
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
        ?.onBackPressedDispatcher

    val backPressedStageHandlerOwner = remember { OnBackPressedStageHandlerOwner() }
    CompositionLocalProvider(
        LocalOnBackPressedStageHandlerOwner provides backPressedStageHandlerOwner,
        content = content
    )

    val backCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backPressedStageHandlerOwner.onBackPressed()) {
                    isEnabled = false
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

/**
 * TODO: This documentation is out of date and incorrect
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
fun AddBackPressInvokeTogether(
    terminal: Boolean = false,
    label: String,
    onBackPressed: suspend OnBackPressedStageHandlerOwner.OnBackPressedCallback.(CoroutineScope) -> Unit,
) {
    val currentOnBackPressed = rememberUpdatedState(onBackPressed)
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
        ?.onBackPressedDispatcher
    val backPressedStageHandler = LocalOnBackPressedStageHandlerOwner.current
    val backCoroutineScope = rememberCoroutineScope()

    val backCallback = remember {
        object : OnBackPressedStageHandlerOwner.OnBackPressedCallback(
            type = Type.INVOKE_TOGETHER,
            label = label,
        ) {
            override fun handleOnBackPressed(): Boolean {
                val onBackPressedCallback = this
                backCoroutineScope.launch {
                    if (!terminal) {
                        enabled = false
                    }
                    currentOnBackPressed.value.invoke(
                        onBackPressedCallback,
                        backCoroutineScope
                    )
                    if (terminal) {
                        enabled = false
                        backPressedDispatcher?.onBackPressed()
                    }
                }
                return true
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, backPressedStageHandler) {
        backPressedStageHandler?.add(
            backCallback,
            OnBackPressedStageHandlerOwner.OnBackPressedCallback.Type.INVOKE_TOGETHER
        )
        onDispose { backPressedStageHandler?.remove(backCallback) }
    }
}

@Composable
fun AddBackPressInvokeFirst(
    label: String,
    onBackPressed: () -> Boolean,
) {
    val currentOnBackPressed = rememberUpdatedState(onBackPressed)
    val backPressedStageHandler = LocalOnBackPressedStageHandlerOwner.current

    val backCallback = remember {
        object : OnBackPressedStageHandlerOwner.OnBackPressedCallback(
            type = Type.INVOKE_FIRST,
            label = label,
        ) {
            override fun handleOnBackPressed(): Boolean {
                if (currentOnBackPressed.value.invoke()) {
                    enabled = false
                    return true
                }
                return false
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, backPressedStageHandler) {
        backPressedStageHandler?.add(
            backCallback,
            OnBackPressedStageHandlerOwner.OnBackPressedCallback.Type.INVOKE_FIRST
        )
        onDispose { backPressedStageHandler?.remove(backCallback) }
    }
}

private object LocalOnBackPressedStageHandlerOwner {
    private val LocalOnBackPressedStageHandlerOwner =
        compositionLocalOf<OnBackPressedStageHandlerOwner?> { null }

    val current: OnBackPressedStageHandlerOwner?
        @Composable
        get() = LocalOnBackPressedStageHandlerOwner.current

    infix fun provides(backStageOwner: OnBackPressedStageHandlerOwner):
            ProvidedValue<OnBackPressedStageHandlerOwner?> {
        return LocalOnBackPressedStageHandlerOwner.provides(backStageOwner)
    }
}

class OnBackPressedStageHandlerOwner {
    private val invokeFirst = mutableListOf<OnBackPressedCallback>()
    private val invokeTogether = mutableListOf<OnBackPressedCallback>()

    fun onBackPressed(): Boolean {
        if (invokeFirst.lastOrNull { it.enabled }?.handleOnBackPressed() == false) {
            return false
        }

        val invokeTogether = invokeTogether.filter { it.enabled }
        return if (invokeTogether.isEmpty()) {
            true
        } else {
            invokeTogether.forEach(OnBackPressedCallback::handleOnBackPressed)
            false
        }
    }

    fun add(backCallback: OnBackPressedCallback, type: OnBackPressedCallback.Type) = when (type) {
        OnBackPressedCallback.Type.INVOKE_FIRST -> invokeFirst += backCallback
        OnBackPressedCallback.Type.INVOKE_TOGETHER -> invokeTogether += backCallback
    }

    fun remove(backCallback: OnBackPressedCallback) {
        invokeFirst -= backCallback
        invokeTogether -= backCallback
    }

    abstract class OnBackPressedCallback(var type: Type, label: String) {
        var enabled = true

        /**
         * @return true to continue navigating back
         */
        abstract fun handleOnBackPressed(): Boolean

        enum class Type {
            INVOKE_FIRST,
            INVOKE_TOGETHER,
        }
    }
}
