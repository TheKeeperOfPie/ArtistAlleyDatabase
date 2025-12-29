package com.thekeeperofpie.artistalleydatabase.alley.edit.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Tv
import androidx.compose.ui.graphics.vector.ImageVector
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_top_level_nav_artists
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_top_level_nav_forms
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_top_level_nav_merch
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_top_level_nav_series
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
    FORMS(
        initialDestination = AlleyEditDestination.ArtistFormQueue,
        icon = Icons.AutoMirrored.Default.List,
        title = Res.string.alley_edit_top_level_nav_forms,
    ),
}
