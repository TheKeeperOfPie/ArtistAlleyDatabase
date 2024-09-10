package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalWithComputedDefaultOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

actual val LocalWindowConfiguration: ProvidableCompositionLocal<WindowConfiguration> =
    compositionLocalWithComputedDefaultOf {
        val configuration = LocalConfiguration.currentValue
        WindowConfiguration(
            screenWidthDp = configuration.screenWidthDp.dp,
            screenHeightDp = configuration.screenHeightDp.dp,
        )
    }

@OptIn(ExperimentalLayoutApi::class)
actual val WindowInsets.Companion.isImeVisibleKmp @Composable get() = isImeVisible
