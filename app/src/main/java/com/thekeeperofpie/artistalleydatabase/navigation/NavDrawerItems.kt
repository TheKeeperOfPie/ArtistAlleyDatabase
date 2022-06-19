package com.thekeeperofpie.artistalleydatabase.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.R

sealed class NavDrawerItems(
    @StringRes val titleRes: Int,
    val icon: ImageVector,
) {

    companion object {
        val ITEMS = listOf(Home, Import, Export)
    }

    object Home : NavDrawerItems(R.string.nav_drawer_home, Icons.Default.Home)
    object Import : NavDrawerItems(R.string.nav_drawer_import, Icons.Default.Create)
    object Export : NavDrawerItems(R.string.nav_drawer_export, Icons.Default.Build)
}