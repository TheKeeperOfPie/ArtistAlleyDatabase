package com.thekeeperofpie.artistalleydatabase.alley.edit.series

import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_tmdb_type_movie
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_tmdb_type_none
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_tmdb_type_tv
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TmdbType
import org.jetbrains.compose.resources.StringResource

val TmdbType.textRes: StringResource
    get() = when (this) {
        TmdbType.NONE -> Res.string.alley_edit_tmdb_type_none
        TmdbType.TV -> Res.string.alley_edit_tmdb_type_tv
        TmdbType.MOVIE -> Res.string.alley_edit_tmdb_type_movie
    }
