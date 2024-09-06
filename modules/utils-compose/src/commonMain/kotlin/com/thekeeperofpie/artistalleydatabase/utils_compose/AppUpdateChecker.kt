package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

expect interface AppUpdateChecker {

    @Suppress("ComposableNaming")
    @Composable
    fun applySnackbarState(snackbarHostState: SnackbarHostState)
}

val LocalAppUpdateChecker = staticCompositionLocalOf<AppUpdateChecker?> { null }
