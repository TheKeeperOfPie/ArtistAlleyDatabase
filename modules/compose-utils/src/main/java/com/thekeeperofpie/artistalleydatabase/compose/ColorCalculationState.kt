package com.thekeeperofpie.artistalleydatabase.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.withContext

@Stable
class ColorCalculationState @OptIn(DelicateCoroutinesApi::class) constructor(
    val scope: CoroutineScope = GlobalScope,
    val isDarkMode: Boolean = true,
) {
    companion object {
        val DEFAULT_VALUE = Entry()
    }

    private val colors = mutableStateMapOf<String, Entry>()

    fun getColors(id: String?) = if (id.isNullOrEmpty()) {
        DEFAULT_VALUE
    } else {
        colors[id] ?: DEFAULT_VALUE
    }.run { containerColor to textColor }

    @Composable
    fun getColorsComposable(id: String?) = if (id.isNullOrEmpty()) {
        DEFAULT_VALUE
    } else {
        remember(id) {
            derivedStateOf { colors[id] ?: DEFAULT_VALUE }
        }.value
    }.run { containerColor to textColor }

    @Composable
    fun allowHardware(id: String?) = if (id == null) {
        true
    } else {
        remember(id) {
            derivedStateOf { colors[id] != null }
        }.value
    }

    fun shouldCalculate(id: String?) = if (id == null) {
        false
    } else {
        colors[id] == null
    }

    suspend fun setColor(id: String, containerColor: Color, textColor: Color) {
        withContext(CustomDispatchers.Main) {
            if (colors[id] != null) {
                colors[id] = Entry(containerColor = containerColor, textColor = textColor)
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
