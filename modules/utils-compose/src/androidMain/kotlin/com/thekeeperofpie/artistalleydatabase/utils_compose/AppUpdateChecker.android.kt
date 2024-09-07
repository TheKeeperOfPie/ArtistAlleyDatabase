package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable

actual interface AppUpdateChecker {

    @Composable
    actual fun applySnackbarState(snackbarHostState: SnackbarHostState)
}
