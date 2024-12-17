@file:OptIn(ExperimentalMaterial3Api::class)

package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import artistalleydatabase.modules.utils_compose.generated.resources.Res
import artistalleydatabase.modules.utils_compose.generated.resources.app_bar_back_icon_content_description
import artistalleydatabase.modules.utils_compose.generated.resources.nav_drawer_icon_content_description
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationController
import org.jetbrains.compose.resources.stringResource

@Composable
fun UpIconButton(option: UpIconOption, modifier: Modifier = Modifier) {
    when (option) {
        is UpIconOption.Back -> ArrowBackIconButton(option.onClick, modifier)
        is UpIconOption.NavDrawer -> NavMenuIconButton(option.onClick, modifier)
    }
}

sealed interface UpIconOption {
    class Back(val onClick: () -> Unit) : UpIconOption {
        constructor(navigationController: NavigationController) :
                this(onClick = { navigationController.navigateUp() })
    }

    class NavDrawer(val onClick: () -> Unit) : UpIconOption
}

@Composable
fun ArrowBackIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(Res.string.app_bar_back_icon_content_description),
        )
    }
}

@Composable
fun NavMenuIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = stringResource(Res.string.nav_drawer_icon_content_description),
        )
    }
}

@Composable
fun AppBar(
    text: String,
    upIconOption: UpIconOption? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = { Text(text = text, maxLines = 1) },
        navigationIcon = {
            if (upIconOption != null) {
                UpIconButton(option = upIconOption)
            }
        },
        scrollBehavior = scrollBehavior,
        colors = colors,
        actions = actions,
    )
}
