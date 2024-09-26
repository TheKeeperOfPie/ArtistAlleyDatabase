package com.thekeeperofpie.artistalleydatabase.debug

import androidx.compose.material3.DrawerState
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.Composable
import com.thekeeperofpie.artistalleydatabase.debug.network.DebugNetworkPanel

object DebugDrawer {

    @Composable
    operator fun invoke(debugComponent: DebugComponent, drawerState: DrawerState) {
        ModalDrawerSheet(drawerState = drawerState) {
            DebugNetworkPanel(debugComponent)
        }
    }
}
