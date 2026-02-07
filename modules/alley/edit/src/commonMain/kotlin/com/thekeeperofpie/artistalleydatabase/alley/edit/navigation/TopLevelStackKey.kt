package com.thekeeperofpie.artistalleydatabase.alley.edit.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Approval
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Tv
import androidx.compose.ui.graphics.vector.ImageVector
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_top_level_nav_admin
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_top_level_nav_artist_forms
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_top_level_nav_artists
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_top_level_nav_merch
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_top_level_nav_rallies
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_top_level_nav_rally_forms
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_top_level_nav_series
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_top_level_nav_tag_resolution
import com.thekeeperofpie.artistalleydatabase.alley.edit.AlleyEditDestination
import org.jetbrains.compose.resources.StringResource

internal enum class TopLevelStackKey(
    val initialDestination: AlleyEditDestination,
    val icon: ImageVector,
    val title: StringResource,
) {
    ARTISTS(
        initialDestination = AlleyEditDestination.Home,
        icon = Icons.Default.Brush,
        title = Res.string.alley_edit_top_level_nav_artists,
    ),
    SERIES(
        initialDestination = AlleyEditDestination.Series,
        icon = Icons.Default.Tv,
        title = Res.string.alley_edit_top_level_nav_series,
    ),
    MERCH(
        initialDestination = AlleyEditDestination.Merch,
        icon = Icons.Default.ShoppingBag,
        title = Res.string.alley_edit_top_level_nav_merch,
    ),
    RALLIES(
        initialDestination = AlleyEditDestination.StampRallies,
        icon = Icons.Default.Approval,
        title = Res.string.alley_edit_top_level_nav_rallies,
    ),
    ARTIST_FORMS(
        initialDestination = AlleyEditDestination.ArtistFormQueue,
        icon = Icons.AutoMirrored.Default.List,
        title = Res.string.alley_edit_top_level_nav_artist_forms,
    ),
    RALLY_FORMS(
        initialDestination = AlleyEditDestination.StampRallyFormQueue,
        icon = Icons.AutoMirrored.Default.List,
        title = Res.string.alley_edit_top_level_nav_rally_forms,
    ),
    TAG_RESOLUTION(
        initialDestination = AlleyEditDestination.TagResolution,
        icon = Icons.Default.Tag,
        title = Res.string.alley_edit_top_level_nav_tag_resolution,
    ),
    ADMIN(
        initialDestination = AlleyEditDestination.Admin,
        icon = Icons.Default.AdminPanelSettings,
        title = Res.string.alley_edit_top_level_nav_admin,
    ),
}
