package com.thekeeperofpie.artistalleydatabase

import androidx.compose.runtime.Composable
import com.thekeeperofpie.artistalleydatabase.compose.DoubleModalNavigationDrawer
import com.thekeeperofpie.artistalleydatabase.compose.DrawerState
import com.thekeeperofpie.artistalleydatabase.debug.DebugDrawer

object DebugDoubleDrawer {

    @Composable
    operator fun invoke(
        drawerState: DrawerState,
        gesturesEnabled: Boolean,
        drawerContent: @Composable () -> Unit,
        content: @Composable () -> Unit,
    ) = DoubleModalNavigationDrawer(
        startDrawerContent = drawerContent,
        gesturesEnabled = gesturesEnabled,
        drawerState = drawerState,
        endDrawerContent = { DebugDrawer() },
        content = content,
    )
}
