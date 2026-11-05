package com.thekeeperofpie.artistalleydatabase.utils_app_state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val DEFAULT_DEBOUNCE_TIMEOUT = 500.milliseconds

@Composable
fun <T> DebounceEffect(
    value: T,
    timeoutMillis: Duration = DEFAULT_DEBOUNCE_TIMEOUT,
    onDebounced: suspend (T) -> Unit,
) {
    require(timeoutMillis >= Duration.ZERO) { "Debounce timeout must not be negative, was $timeoutMillis" }
    // Keep the latest callback reference without restarting the timer on recomposition
    val currentOnDebounced by rememberUpdatedState(onDebounced)

    LaunchedEffect(value, timeoutMillis) {
        delay(timeoutMillis)
        currentOnDebounced(value)
    }
}

@Composable
fun <T> rememberDebounced(
    value: T,
    timeoutMillis: Duration = DEFAULT_DEBOUNCE_TIMEOUT,
): State<T> {
    val state = remember { mutableStateOf(value) }
    DebounceEffect(value, timeoutMillis) { debounced ->
        state.value = debounced
    }
    return state
}
