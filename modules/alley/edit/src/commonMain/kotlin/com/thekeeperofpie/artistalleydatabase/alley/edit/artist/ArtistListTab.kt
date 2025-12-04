package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Tag
import androidx.compose.ui.graphics.vector.ImageVector
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_list_tab_all
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_list_tab_missing_confirmed
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_list_tab_missing_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_list_tab_missing_links
import org.jetbrains.compose.resources.StringResource

internal enum class ArtistListTab(val icon: ImageVector, val label: StringResource) {
    ALL(Icons.AutoMirrored.Default.List, Res.string.alley_edit_artist_list_tab_all),
    MISSING_LINKS(Icons.Default.Link, Res.string.alley_edit_artist_list_tab_missing_links),
    MISSING_INFERRED(Icons.Default.Tag, Res.string.alley_edit_artist_list_tab_missing_inferred),
    MISSING_CONFIRMED(
        Icons.Default.Image,
        Res.string.alley_edit_artist_list_tab_missing_confirmed
    ),
}
