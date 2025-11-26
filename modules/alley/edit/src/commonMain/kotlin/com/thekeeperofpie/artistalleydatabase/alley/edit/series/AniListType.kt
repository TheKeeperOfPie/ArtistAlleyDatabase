package com.thekeeperofpie.artistalleydatabase.alley.edit.series

import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_aniList_type_anime
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_aniList_type_manga
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_aniList_type_none
import com.thekeeperofpie.artistalleydatabase.alley.models.AniListType
import org.jetbrains.compose.resources.StringResource

val AniListType.textRes: StringResource
    get() = when(this) {
        AniListType.NONE -> Res.string.alley_edit_aniList_type_none
        AniListType.ANIME -> Res.string.alley_edit_aniList_type_anime
        AniListType.MANGA -> Res.string.alley_edit_aniList_type_manga
    }
