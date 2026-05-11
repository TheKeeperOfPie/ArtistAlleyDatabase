package com.thekeeperofpie.artistalleydatabase

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable

object DebugDoubleDrawer {

    @Composable
    operator fun invoke(
        applicationComponent: ApplicationComponent,
        drawerContent: @Composable () -> Unit,
        startDrawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
        endDrawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
        gesturesEnabled: Boolean,
        content: @Composable () -> Unit,
    ) = ModalNavigationDrawer(
        drawerContent = drawerContent,
        drawerState = startDrawerState,
        gesturesEnabled = gesturesEnabled,
        content = content,
    )
}
