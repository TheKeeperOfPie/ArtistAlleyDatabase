package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalWithComputedDefaultOf
import androidx.compose.ui.platform.LocalWindowInfo

actual val LocalWindowConfiguration: ProvidableCompositionLocal<WindowConfiguration> =
    compositionLocalWithComputedDefaultOf {
        val windowInfo = LocalWindowInfo.currentValue
        val containerDpSize = windowInfo.containerDpSize
        WindowConfiguration(
            screenWidthDp = containerDpSize.width,
            screenHeightDp = containerDpSize.height,
        )
    }

actual val WindowInsets.Companion.isImeVisibleKmp @Composable get() = false
