package com.thekeeperofpie.artistalleydatabase.debug

import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.Composable
import com.thekeeperofpie.artistalleydatabase.debug.network.DebugNetworkPanel

object DebugDrawer {

    @Composable
    operator fun invoke() {
        ModalDrawerSheet {
            DebugNetworkPanel()
        }
    }
}
