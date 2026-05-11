package com.thekeeperofpie.artistalleydatabase

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import com.thekeeperofpie.artistalleydatabase.debug.DebugDrawer
import com.thekeeperofpie.artistalleydatabase.utils_compose.DoubleModalNavigationDrawer

object DebugDoubleDrawer {

    @Composable
    operator fun invoke(
        applicationComponent: ApplicationComponent,
        drawerContent: @Composable () -> Unit,
        startDrawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
        endDrawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
        gesturesEnabled: Boolean,
        content: @Composable () -> Unit,
    ) = DoubleModalNavigationDrawer(
        startDrawerContent = drawerContent,
        endDrawerContent = { DebugDrawer(applicationComponent, endDrawerState) },
        startDrawerState = startDrawerState,
        endDrawerState = endDrawerState,
        gesturesEnabled = gesturesEnabled,
        content = content,
    )
}
