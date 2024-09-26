package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.material3.DrawerState
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

@Composable
fun DoubleModalNavigationDrawer(
    startDrawerContent: @Composable () -> Unit,
    endDrawerContent: (@Composable () -> Unit)? = null,
    startDrawerState: DrawerState,
    endDrawerState: DrawerState,
    gesturesEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    if (endDrawerContent == null) {
        ModalNavigationDrawer(
            drawerContent = startDrawerContent,
            drawerState = startDrawerState,
            gesturesEnabled = gesturesEnabled,
            content = content,
        )
    } else {
        val original = LocalLayoutDirection.current
        val reversed = when (original) {
            LayoutDirection.Ltr -> LayoutDirection.Rtl
            LayoutDirection.Rtl -> LayoutDirection.Ltr
        }
        CompositionLocalProvider(LocalLayoutDirection provides reversed) {
            ModalNavigationDrawer(
                drawerContent = {
                    CompositionLocalProvider(LocalLayoutDirection provides original) {
                        endDrawerContent()
                    }
                },
                drawerState = endDrawerState,
                gesturesEnabled = false,
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides original) {
                    ModalNavigationDrawer(
                        drawerContent = startDrawerContent,
                        drawerState = startDrawerState,
                        gesturesEnabled = gesturesEnabled,
                        content = content,
                    )
                }
            }
        }
    }
}
