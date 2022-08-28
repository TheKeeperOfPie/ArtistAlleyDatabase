package com.thekeeperofpie.artistalleydatabase.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.R

sealed class NavDrawerItems(
    val id: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector,
) {

    companion object {
        fun items() = listOf(Home, Browse, Search, Import, Export)
    }

    object Home : NavDrawerItems("home", R.string.nav_drawer_home, Icons.Default.Home)
    object Browse : NavDrawerItems("browse", R.string.nav_drawer_browse, Icons.Default.ListAlt)
    object Search : NavDrawerItems("search", R.string.nav_drawer_search, Icons.Default.Search)
    object Import : NavDrawerItems("import", R.string.nav_drawer_import, Icons.Default.Create)
    object Export : NavDrawerItems("export", R.string.nav_drawer_export, Icons.Default.Build)
}