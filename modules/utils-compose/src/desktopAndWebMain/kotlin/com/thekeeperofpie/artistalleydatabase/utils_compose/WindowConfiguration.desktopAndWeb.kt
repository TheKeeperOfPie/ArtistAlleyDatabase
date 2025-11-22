package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

actual val LocalWindowConfiguration: ProvidableCompositionLocal<WindowConfiguration> =
    compositionLocalOf { throw IllegalStateException("WindowConfiguration not provided") }

actual val WindowInsets.Companion.isImeVisibleKmp @Composable get() = false
