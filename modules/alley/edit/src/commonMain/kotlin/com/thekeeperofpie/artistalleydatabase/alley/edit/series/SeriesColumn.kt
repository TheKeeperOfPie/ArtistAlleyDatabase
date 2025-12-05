package com.thekeeperofpie.artistalleydatabase.alley.edit.series

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_aniList_id
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_aniList_type
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_canonical
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_external_link
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_image
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_source_type
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_synonyms
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_title_english
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_title_native
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_title_preferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_title_romaji
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_uuid
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_wikipedia_id
import com.thekeeperofpie.artistalleydatabase.alley.ui.TwoWayGrid
import org.jetbrains.compose.resources.StringResource

enum class SeriesColumn(
    override val text: StringResource,
    override val size: Dp = 160.dp,
) : TwoWayGrid.Column {
    IMAGE(size = 64.dp, text = Res.string.alley_edit_series_header_image),
    CANONICAL(text = Res.string.alley_edit_series_header_canonical),
    NOTES(text = Res.string.alley_edit_series_header_notes),
    ANILIST_ID(text = Res.string.alley_edit_series_header_aniList_id),
    ANILIST_TYPE(text = Res.string.alley_edit_series_header_aniList_type),
    SOURCE_TYPE(text = Res.string.alley_edit_series_header_source_type),
    TITLE_ENGLISH(text = Res.string.alley_edit_series_header_title_english),
    TITLE_ROMAJI(text = Res.string.alley_edit_series_header_title_romaji),
    TITLE_NATIVE(text = Res.string.alley_edit_series_header_title_native),
    TITLE_PREFERRED(text = Res.string.alley_edit_series_header_title_preferred),
    SYNONYMS(text = Res.string.alley_edit_series_header_synonyms),
    WIKIPEDIA_ID(text = Res.string.alley_edit_series_header_wikipedia_id),
    EXTERNAL_LINK(text = Res.string.alley_edit_series_header_external_link),
    UUID(text = Res.string.alley_edit_series_header_uuid),
}
