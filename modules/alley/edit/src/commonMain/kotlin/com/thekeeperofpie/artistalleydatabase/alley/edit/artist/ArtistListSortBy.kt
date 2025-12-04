package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_list_sort_booth
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_list_sort_name
import org.jetbrains.compose.resources.StringResource

internal enum class ArtistListSortBy(val label: StringResource){
    BOOTH(Res.string.alley_edit_artist_list_sort_booth),
    NAME(Res.string.alley_edit_artist_list_sort_name),
}
