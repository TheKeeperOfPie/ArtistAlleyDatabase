package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.compositionLocalWithComputedDefaultOf
import androidx.compose.ui.platform.LocalConfiguration

actual val LocalAppConfiguration: CompositionLocal<AppConfiguration> =
    compositionLocalWithComputedDefaultOf {
        val configuration = LocalConfiguration.currentValue
        AppConfiguration(
            screenWidthDp = configuration.screenWidthDp,
            screenHeightDp = configuration.screenHeightDp,
        )
    }

@OptIn(ExperimentalLayoutApi::class)
actual val WindowInsets.Companion.isImeVisibleKmp @Composable get() = isImeVisible
