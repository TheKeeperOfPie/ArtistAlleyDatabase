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
import com.thekeeperofpie.artistalleydatabase.AppNavDestinations
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.art.ArtNavDestinations
import com.thekeeperofpie.artistalleydatabase.cds.CdNavDestinations

enum class NavDrawerItems(
    val id: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector,
    val route: String = id,
) {

    ANIME(AnimeNavDestinations.HOME.id, R.string.nav_drawer_anime, Icons.Default.VideoLibrary),
    ART(ArtNavDestinations.HOME.id, R.string.nav_drawer_art, Icons.Default.PhotoLibrary),
    CDS(CdNavDestinations.HOME.id, R.string.nav_drawer_cds, Icons.Default.Album),
    BROWSE(AppNavDestinations.BROWSE.id, R.string.nav_drawer_browse, Icons.Default.ListAlt),
    SEARCH(AppNavDestinations.SEARCH.id, R.string.nav_drawer_search, Icons.Default.Search),
    IMPORT(AppNavDestinations.IMPORT.id, R.string.nav_drawer_import, Icons.Default.Create),
    EXPORT(AppNavDestinations.EXPORT.id, R.string.nav_drawer_export, Icons.Default.Build),
    SETTINGS(
        AppNavDestinations.SETTINGS.id,
        com.thekeeperofpie.artistalleydatabase.settings.R.string.settings_nav_drawer,
        Icons.Default.Settings,
        route = "settings?root=true"
    ),
    ;
}
