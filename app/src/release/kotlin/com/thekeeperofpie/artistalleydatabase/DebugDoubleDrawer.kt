package com.thekeeperofpie.artistalleydatabase

import androidx.compose.runtime.Composable
import com.thekeeperofpie.artistalleydatabase.compose.DoubleModalNavigationDrawer
import com.thekeeperofpie.artistalleydatabase.compose.DrawerState

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
        content = content,
    )
}
