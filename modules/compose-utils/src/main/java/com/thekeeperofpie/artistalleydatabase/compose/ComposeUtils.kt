@file:OptIn(ExperimentalFoundationApi::class)

package com.thekeeperofpie.artistalleydatabase.compose

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import coil.compose.AsyncImagePainter
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.UriUtils
import kotlinx.coroutines.delay
import kotlin.random.Random
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

context(LazyItemScope)
fun Modifier.animateItemPlacementFixed() =
    animateItemPlacement(animationSpec = NotEqualAnimationSpecDelegate())

context(LazyGridItemScope)
fun Modifier.animateItemPlacementFixed() =
    animateItemPlacement(animationSpec = NotEqualAnimationSpecDelegate())

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

private class NotEqualAnimationSpecDelegate(spec: FiniteAnimationSpec<IntOffset>) :
    FiniteAnimationSpec<IntOffset> by spec {

    constructor() : this(
        spring(
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = IntOffset.VisibilityThreshold
        )
    )

    private val hashCode = Random.nextInt()

    override fun equals(other: Any?): Boolean {
        // There's a bug in 1.6.0-alpha02 animateItemPlacement where it inverts equality, so
        // always return false here so that the equality check is true and passes
        return false
    }

    override fun hashCode(): Int {
        return hashCode
    }
}
