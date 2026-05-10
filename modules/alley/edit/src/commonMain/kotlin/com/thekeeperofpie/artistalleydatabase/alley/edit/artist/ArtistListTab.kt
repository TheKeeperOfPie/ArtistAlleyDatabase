package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.ui.graphics.vector.ImageVector
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_list_tab_all
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_list_tab_missing_confirmed
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_list_tab_missing_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_list_tab_missing_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_list_tab_missing_updated_inferred
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.automirrored.filled.List
import com.thekeeperofpie.artistalleydatabase.icons.filled.Image
import com.thekeeperofpie.artistalleydatabase.icons.filled.Link
import com.thekeeperofpie.artistalleydatabase.icons.filled.Tag
import org.jetbrains.compose.resources.StringResource

internal enum class ArtistListTab(val icon: ImageVector, val label: StringResource) {
    ALL(Icons.AutoMirrored.Default.List, Res.string.alley_edit_artist_list_tab_all),
    MISSING_LINKS(Icons.Default.Link, Res.string.alley_edit_artist_list_tab_missing_links),
    MISSING_INFERRED(Icons.Default.Tag, Res.string.alley_edit_artist_list_tab_missing_inferred),
    MISSING_UPDATED_INFERRED(Icons.Default.Tag, Res.string.alley_edit_artist_list_tab_missing_updated_inferred),
    MISSING_CONFIRMED(
        Icons.Default.Image,
        Res.string.alley_edit_artist_list_tab_missing_confirmed
    ),
}
