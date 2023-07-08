package com.thekeeperofpie.artistalleydatabase.compose

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun UpIconButton(option: UpIconOption) {
    when (option) {
        is UpIconOption.Back -> ArrowBackIconButton(option.onClick)
        is UpIconOption.NavDrawer -> NavMenuIconButton(option.onClick)
    }
}

sealed interface UpIconOption {
    class Back(val onClick: () -> Unit) : UpIconOption {
        constructor(navHostController: NavHostController) :
                this(onClick = { navHostController.popBackStack() })
    }

    class NavDrawer(val onClick: () -> Unit) : UpIconOption
}
