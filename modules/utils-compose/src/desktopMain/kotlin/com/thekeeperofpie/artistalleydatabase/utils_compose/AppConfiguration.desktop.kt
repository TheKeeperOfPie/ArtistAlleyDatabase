package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalWithComputedDefaultOf

actual val LocalAppConfiguration: ProvidableCompositionLocal<AppConfiguration> =
    compositionLocalWithComputedDefaultOf {
        TODO()
    }

actual val WindowInsets.Companion.isImeVisibleKmp @Composable get() = false
