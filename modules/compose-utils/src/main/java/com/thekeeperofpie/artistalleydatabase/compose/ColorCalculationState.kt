package com.thekeeperofpie.artistalleydatabase.compose

import androidx.collection.LruCache
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
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
    private val colors = LruCache<String, Pair<Color, Color>>(200)

    fun getColors(id: String?) = (if (id.isNullOrEmpty()) null else colors[id])
        ?: (Color.Unspecified to Color.Unspecified)

    fun hasColor(id: String?) = if (id == null) false else colors[id] != null

    fun setColor(id: String, containerColor: Color, textColor: Color) =
        colors.put(id, containerColor to textColor)
}

@Composable
fun rememberColorCalculationState(): ColorCalculationState {
    val scope = rememberCoroutineScope()
    val isSystemInDarkTheme = isSystemInDarkTheme()
    return remember(isSystemInDarkTheme) { ColorCalculationState(scope, isSystemInDarkTheme) }
}

val LocalColorCalculationState = staticCompositionLocalOf { ColorCalculationState() }
