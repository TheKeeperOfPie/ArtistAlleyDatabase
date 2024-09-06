package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable

actual interface AppUpdateChecker {

    fun initialize(activity: ComponentActivity)

    @Composable
    actual fun applySnackbarState(snackbarHostState: SnackbarHostState)
}
