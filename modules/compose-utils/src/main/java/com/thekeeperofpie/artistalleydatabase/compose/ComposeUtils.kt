@file:OptIn(ExperimentalFoundationApi::class)

package com.thekeeperofpie.artistalleydatabase.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImagePainter
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.UriUtils
import kotlinx.coroutines.delay
import kotlin.reflect.KProperty
import kotlin.time.Duration

fun <T> observableStateOf(value: T, onChange: (T) -> Unit) =
    ObservableMutableStateWrapper(value, onChange)

class ObservableMutableStateWrapper<T>(value: T, val onChange: (T) -> Unit) {
    var value by mutableStateOf(value)

    @Suppress("NOTHING_TO_INLINE")
    inline operator fun getValue(thisObj: Any?, property: KProperty<*>) = value

    @Suppress("NOTHING_TO_INLINE")
    inline operator fun setValue(thisObj: Any?, property: KProperty<*>, value: T) {
        this.value = value
        onChange(value)
    }
}

@Composable
fun LazyListState.showFloatingActionButtonOnVerticalScroll(firstIndexToHide: Int = 3): Boolean {
    var previousIndex by remember(this) { mutableStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (firstVisibleItemIndex < firstIndexToHide) {
                true
            } else if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

fun AsyncImagePainter.State.Success.widthToHeightRatio() = result.drawable.intrinsicWidth /
        result.drawable.intrinsicHeight.coerceAtLeast(0).toFloat()

@Composable
fun LoadingResult<*>.ErrorSnackbar(snackbarHostState: SnackbarHostState) {
    val errorMessage = error?.first?.let { stringResource(it) }
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                withDismissAction = true,
                duration = SnackbarDuration.Long,
            )
        }
    }
}

// TODO: Replace other usages
fun UriHandler.openForceExternal(uri: String) =
    openUri("$uri?${UriUtils.FORCE_EXTERNAL_URI_PARAM}=true")

@Composable
fun <T> rememberLambda(block: () -> T) = remember { block }

@Composable
fun rememberCallback(block: () -> Unit) = remember { block }

@Composable
fun <T> rememberCallback(block: (T) -> Unit) = remember { block }

@Composable
fun <T> debounce(currentValue: T, duration: Duration): T {
    var debounced by remember { mutableStateOf(currentValue) }
    LaunchedEffect(currentValue) {
        delay(duration)
        debounced = currentValue
    }
    return debounced
}

@Composable
fun <T> OnChangeEffect(currentValue: T, onChange: suspend (T) -> Unit) {
    var previousValue by remember { mutableStateOf(currentValue) }
    LaunchedEffect(currentValue) {
        if (previousValue != currentValue) {
            previousValue = currentValue
            onChange(currentValue)
        }
    }
}
