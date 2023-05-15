package com.thekeeperofpie.artistalleydatabase.compose

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.reflect.KProperty

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
