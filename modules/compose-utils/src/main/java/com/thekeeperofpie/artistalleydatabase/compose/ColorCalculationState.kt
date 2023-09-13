package com.thekeeperofpie.artistalleydatabase.compose

import androidx.collection.LruCache
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

@Stable
class ColorCalculationState @OptIn(DelicateCoroutinesApi::class) constructor(
    val scope: CoroutineScope = GlobalScope,
    val isDarkMode: Boolean = true,
) {
    companion object {
        val DEFAULT_VALUE = Entry()
    }

    private val colors = LruCache<String, MutableState<Entry>>(250)
    private val lock = ReentrantReadWriteLock()

    fun getColors(id: String?) = if (id.isNullOrEmpty()) {
        DEFAULT_VALUE
    } else {
        lock.read {
            var existingState = colors[id]
            if (existingState == null) {
                existingState = mutableStateOf(DEFAULT_VALUE)
                lock.write { colors.put(id, existingState) }
            }
            existingState.value
        }
    }.run { containerColor to textColor }

    fun getContainerColor(id: String?) = if (id.isNullOrEmpty()) {
        Color.Unspecified
    } else {
        lock.read { colors[id]?.value?.containerColor }
    }

    @Composable
    fun allowHardware(id: String?) = if (id == null) {
        true
    } else {
        remember(id) {
            lock.read {
                val existingState = colors[id]
                existingState != null && existingState.value != DEFAULT_VALUE
            }
        }
    }

    fun shouldCalculate(id: String?) = if (id == null) {
        false
    } else {
        lock.read {
            val existingState = colors[id]
            existingState == null || existingState.value == DEFAULT_VALUE
        }
    }

    fun setColor(id: String, containerColor: Color, textColor: Color) {
        if (lock.read {
                val existingState = colors[id]
                existingState != null && existingState.value != DEFAULT_VALUE
            }
        ) return
        lock.write {
            val entry = Entry(containerColor = containerColor, textColor = textColor)
            val existingState = colors[id]
            if (existingState == null) {
                colors.put(id, mutableStateOf(entry))
            } else {
                existingState.value = entry
            }
        }
    }

    @Immutable
    data class Entry(
        val containerColor: Color = Color.Unspecified,
        val textColor: Color = Color.Unspecified,
    )
}

@Composable
fun rememberColorCalculationState(): ColorCalculationState {
    val scope = rememberCoroutineScope()
    val isSystemInDarkTheme = isSystemInDarkTheme()
    return remember(isSystemInDarkTheme) { ColorCalculationState(scope, isSystemInDarkTheme) }
}

val LocalColorCalculationState = staticCompositionLocalOf { ColorCalculationState() }
