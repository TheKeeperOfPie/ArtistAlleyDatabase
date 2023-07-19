package com.thekeeperofpie.artistalleydatabase.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun UpIconButton(option: UpIconOption, modifier: Modifier = Modifier) {
    when (option) {
        is UpIconOption.Back -> ArrowBackIconButton(option.onClick, modifier)
        is UpIconOption.NavDrawer -> NavMenuIconButton(option.onClick, modifier)
    }
}

sealed interface UpIconOption {
    class Back(val onClick: () -> Unit) : UpIconOption {
        constructor(navHostController: NavHostController) :
                this(onClick = { navHostController.popBackStack() })
    }

    class NavDrawer(val onClick: () -> Unit) : UpIconOption
}
