package com.thekeeperofpie.artistalleydatabase.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope

class ColorCalculationState @OptIn(DelicateCoroutinesApi::class) constructor(
    val scope: CoroutineScope = GlobalScope,
    val colorMap: MutableMap<String, Pair<Color, Color>> = mutableMapOf(),
    val isDarkMode: Boolean = true,
) {
    fun getColors(id: String?) = colorMap[id.orEmpty()]
        ?: (Color.Unspecified to Color.Unspecified)

    fun hasColor(id: String?) = colorMap.contains(id)
}

@Composable
fun rememberColorCalculationState(
    colorMap: MutableMap<String, Pair<Color, Color>>,
): ColorCalculationState {
    val scope = rememberCoroutineScope()
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val isDarkMode = remember(isSystemInDarkTheme) { isSystemInDarkTheme }
    return ColorCalculationState(scope, colorMap, isDarkMode)
}
