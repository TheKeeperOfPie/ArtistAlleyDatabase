package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.compositionLocalWithComputedDefaultOf

actual val LocalAppConfiguration: CompositionLocal<AppConfiguration> =
    compositionLocalWithComputedDefaultOf {
        TODO()
    }

actual val WindowInsets.Companion.isImeVisibleKmp @Composable get() = false
