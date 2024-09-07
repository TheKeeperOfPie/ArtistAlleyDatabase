package com.thekeeperofpie.artistalleydatabase.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector
import artistalleydatabase.modules.settings.generated.resources.settings_nav_drawer
import com.thekeeperofpie.anichive.R
import com.thekeeperofpie.artistalleydatabase.AppNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.art.ArtNavDestinations
import com.thekeeperofpie.artistalleydatabase.cds.CdNavDestinations
import com.thekeeperofpie.artistalleydatabase.settings.SettingsStrings
import com.thekeeperofpie.artistalleydatabase.utils_compose.StringResourceCompat
import com.thekeeperofpie.artistalleydatabase.utils_compose.StringResourceCompose
import com.thekeeperofpie.artistalleydatabase.utils_compose.StringResourceId

enum class NavDrawerItems(
    val id: String,
    val titleRes: StringResourceCompat,
    val icon: ImageVector,
    val route: String = id,
) {

    ANIME(AnimeNavDestinations.HOME.id, R.string.nav_drawer_anime, Icons.Default.VideoLibrary),
    ART(ArtNavDestinations.HOME.id, R.string.nav_drawer_art, Icons.Default.PhotoLibrary),
    CDS(CdNavDestinations.HOME.id, R.string.nav_drawer_cds, Icons.Default.Album),
    BROWSE(AppNavDestinations.BROWSE.id, R.string.nav_drawer_browse, Icons.Default.ListAlt),
    IMPORT(AppNavDestinations.IMPORT.id, R.string.nav_drawer_import, Icons.Default.Create),
    EXPORT(AppNavDestinations.EXPORT.id, R.string.nav_drawer_export, Icons.Default.Build),
    SETTINGS(
        AppNavDestinations.SETTINGS.id,
        StringResourceCompose(SettingsStrings.settings_nav_drawer),
        Icons.Default.Settings,
        route = "settings?root=true"
    ),
    ANIME_2_ANIME(
        AppNavDestinations.ANIME_2_ANIME.id,
        R.string.nav_drawer_anime_2_anime,
        Icons.Default.Games,
    ),
    ;
    constructor(
        id: String,
        titleResId: Int,
        icon: ImageVector,
        route: String = id,
    ): this(
        id,
        titleRes = StringResourceId(titleResId),
        icon,
        route
    )
}
