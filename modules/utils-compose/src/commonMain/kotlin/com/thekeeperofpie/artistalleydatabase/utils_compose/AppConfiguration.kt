package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal

expect val LocalAppConfiguration : CompositionLocal<AppConfiguration>

data class AppConfiguration(
    val screenWidthDp: Int,
    val screenHeightDp: Int,
)

expect val WindowInsets.Companion.isImeVisibleKmp: Boolean
    @Composable get
