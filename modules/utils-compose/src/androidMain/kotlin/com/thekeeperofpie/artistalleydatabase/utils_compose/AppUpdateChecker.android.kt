package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable

typealias AppUpdateCheckerInjector = (ComponentActivity) -> AppUpdateChecker?

actual interface AppUpdateChecker {

    @Composable
    actual fun applySnackbarState(snackbarHostState: SnackbarHostState)
}
