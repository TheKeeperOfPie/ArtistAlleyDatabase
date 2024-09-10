package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.ui.unit.Dp

expect val LocalWindowConfiguration : ProvidableCompositionLocal<WindowConfiguration>

data class WindowConfiguration(
    val screenWidthDp: Dp,
    val screenHeightDp: Dp,
)

expect val WindowInsets.Companion.isImeVisibleKmp: Boolean
    @Composable get
