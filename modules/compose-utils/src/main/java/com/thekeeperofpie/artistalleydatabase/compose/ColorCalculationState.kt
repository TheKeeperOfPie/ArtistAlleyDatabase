package com.thekeeperofpie.artistalleydatabase.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope

class ColorCalculationState @OptIn(DelicateCoroutinesApi::class) constructor(
    val scope: CoroutineScope = GlobalScope,
    val isDarkMode: Boolean = true,
) {
    private val colors = mutableStateMapOf<String, Entry>()

    fun getColors(id: String?) = (if (id.isNullOrEmpty()) null else colors[id])
        ?.let { it.containerColor to it.textColor }
        ?: (Color.Unspecified to Color.Unspecified)

    fun hasColor(id: String?) = if (id == null) false else colors[id] != null

    fun setColor(id: String, containerColor: Color, textColor: Color) {
        colors[id] = Entry(containerColor = containerColor, textColor = textColor)
        if (colors.size > 250) {
            // TODO: Make a real mutableLruCacheOf
            // Replicate LRU behavior while still using a snapshot aware data structure
            colors.entries
                .sortedBy { it.value.lastAccessed }
                .take(50)
                .forEach {
                    colors.remove(it.key)
                }
        }
    }

    data class Entry(
        val containerColor: Color,
        val textColor: Color,
        var lastAccessed: Long = System.currentTimeMillis(),
    )
}

@Composable
fun rememberColorCalculationState(): ColorCalculationState {
    val scope = rememberCoroutineScope()
    val isSystemInDarkTheme = isSystemInDarkTheme()
    return remember(isSystemInDarkTheme) { ColorCalculationState(scope, isSystemInDarkTheme) }
}

val LocalColorCalculationState = staticCompositionLocalOf { ColorCalculationState() }
