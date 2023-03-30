package com.thekeeperofpie.artistalleydatabase.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.settings.SettingsStringR

sealed class NavDrawerItems(
    val id: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector,
) {

    companion object {
        const val INITIAL_INDEX = 1
        fun items() = listOf(Anime, Art, Cds, Browse, Search, Import, Export, Settings)
    }

    object Anime : NavDrawerItems("anime", R.string.nav_drawer_anime, Icons.Default.VideoLibrary)
    object Art : NavDrawerItems("art", R.string.nav_drawer_art, Icons.Default.PhotoLibrary)
    object Cds : NavDrawerItems("cds", R.string.nav_drawer_cds, Icons.Default.Album)
    object Browse : NavDrawerItems("browse", R.string.nav_drawer_browse, Icons.Default.ListAlt)
    object Search : NavDrawerItems("search", R.string.nav_drawer_search, Icons.Default.Search)
    object Import : NavDrawerItems("import", R.string.nav_drawer_import, Icons.Default.Create)
    object Export : NavDrawerItems("export", R.string.nav_drawer_export, Icons.Default.Build)
    object Settings :
        NavDrawerItems("settings", SettingsStringR.settings_nav_drawer, Icons.Default.Settings)
}