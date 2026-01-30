package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies

import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_list_sort_fandom
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_list_sort_host
import org.jetbrains.compose.resources.StringResource

internal enum class StampRallyListSortBy(val label: StringResource){
    HOST(Res.string.alley_edit_stamp_rally_list_sort_host),
    FANDOM(Res.string.alley_edit_stamp_rally_list_sort_fandom),
}
