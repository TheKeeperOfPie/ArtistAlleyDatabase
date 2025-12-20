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
import artistalleydatabase.app.generated.resources.Res
import artistalleydatabase.app.generated.resources.nav_drawer_anime
import artistalleydatabase.app.generated.resources.nav_drawer_anime_2_anime
import artistalleydatabase.app.generated.resources.nav_drawer_art
import artistalleydatabase.app.generated.resources.nav_drawer_browse
import artistalleydatabase.app.generated.resources.nav_drawer_cds
import artistalleydatabase.app.generated.resources.nav_drawer_export
import artistalleydatabase.app.generated.resources.nav_drawer_import
import artistalleydatabase.modules.settings.generated.resources.settings_nav_drawer
import com.thekeeperofpie.artistalleydatabase.AppNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.art.ArtNavDestinations
import com.thekeeperofpie.artistalleydatabase.cds.CdNavDestinations
import com.thekeeperofpie.artistalleydatabase.settings.SettingsStrings
import org.jetbrains.compose.resources.StringResource

enum class NavDrawerItems(
    val id: String,
    val titleRes: StringResource,
    val icon: ImageVector,
    val route: String = id,
) {
    ANIME(AnimeNavDestinations.HOME.id, Res.string.nav_drawer_anime, Icons.Default.VideoLibrary),
    ART(ArtNavDestinations.HOME.id, Res.string.nav_drawer_art, Icons.Default.PhotoLibrary),
    CDS(CdNavDestinations.HOME.id, Res.string.nav_drawer_cds, Icons.Default.Album),
    BROWSE(AppNavDestinations.BROWSE.id, Res.string.nav_drawer_browse, Icons.Default.ListAlt),
    IMPORT(AppNavDestinations.IMPORT.id, Res.string.nav_drawer_import, Icons.Default.Create),
    EXPORT(AppNavDestinations.EXPORT.id, Res.string.nav_drawer_export, Icons.Default.Build),
    SETTINGS(
        AppNavDestinations.SETTINGS.id,
        SettingsStrings.settings_nav_drawer,
        Icons.Default.Settings,
        route = "settings?root=true"
    ),
    ANIME_2_ANIME(
        AppNavDestinations.ANIME_2_ANIME.id,
        Res.string.nav_drawer_anime_2_anime,
        Icons.Default.Games,
    ),
}
