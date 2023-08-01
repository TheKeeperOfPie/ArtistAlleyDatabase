package com.thekeeperofpie.artistalleydatabase.compose.update

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

interface AppUpdateChecker {

    fun initialize(activity: ComponentActivity)

    @SuppressLint("ComposableNaming")
    @Composable
    fun applySnackbarState(snackbarHostState: SnackbarHostState)
}

val LocalAppUpdateChecker = staticCompositionLocalOf<AppUpdateChecker?> { null }
