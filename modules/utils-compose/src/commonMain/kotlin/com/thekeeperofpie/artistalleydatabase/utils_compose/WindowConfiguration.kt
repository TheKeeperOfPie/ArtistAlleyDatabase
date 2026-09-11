package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize

expect val LocalWindowConfiguration : ProvidableCompositionLocal<WindowConfiguration>

data class WindowConfiguration(
    val screenWidthDp: Dp,
    val screenHeightDp: Dp,
)

expect val WindowInsets.Companion.isImeVisibleKmp: Boolean
    @Composable get

@Composable
fun currentWindowSizeClass(): WindowSizeClass {
    val density = LocalDensity.current
    val windowConfiguration = LocalWindowConfiguration.current
    val width = windowConfiguration.screenWidthDp
    val height = windowConfiguration.screenHeightDp
    return remember(density, windowConfiguration) {
        WindowSizeClass.calculateFromSize(DpSize(width, height))
    }
}
