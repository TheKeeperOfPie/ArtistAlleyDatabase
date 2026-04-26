package com.thekeeperofpie.artistalleydatabase.alley.edit.series

import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_type_aniList
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_type_other
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_type_wikipedia
import org.jetbrains.compose.resources.StringResource

internal enum class SeriesType(val textRes: StringResource) {
    ANILIST(Res.string.alley_edit_series_header_type_aniList),
    WIKIPEDIA(Res.string.alley_edit_series_header_type_wikipedia),
    OTHER(Res.string.alley_edit_series_header_type_other),
}
