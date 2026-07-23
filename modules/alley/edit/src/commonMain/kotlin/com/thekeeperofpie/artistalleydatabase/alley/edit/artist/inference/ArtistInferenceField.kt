package com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_merch
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_merch_with_year
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_name
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_name_with_year
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_series
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_series_with_year
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_social_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_social_links_with_year
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_store_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_store_links_with_year
import org.jetbrains.compose.resources.StringResource

enum class ArtistInferenceField(
    val label: StringResource,
    val labelWithYear: StringResource,
) {
    NAME(
        Res.string.alley_edit_artist_field_label_name,
        Res.string.alley_edit_artist_field_label_name_with_year,
    ),
    SOCIAL_LINKS(
        Res.string.alley_edit_artist_field_label_social_links,
        Res.string.alley_edit_artist_field_label_social_links_with_year,
    ),
    STORE_LINKS(
        Res.string.alley_edit_artist_field_label_store_links,
        Res.string.alley_edit_artist_field_label_store_links_with_year,
    ),
    SERIES(
        Res.string.alley_edit_artist_field_label_series,
        Res.string.alley_edit_artist_field_label_series_with_year,
    ),
    MERCH(
        Res.string.alley_edit_artist_field_label_merch,
        Res.string.alley_edit_artist_field_label_merch_with_year,
    ),
}

@Stable
class ArtistInferenceFieldState(val map: SnapshotStateMap<ArtistInferenceField, Boolean>) {
    operator fun get(field: ArtistInferenceField) = map[field] ?: false
    operator fun set(field: ArtistInferenceField, checked: Boolean) = map.set(field, checked)
}

@Composable
fun rememberArtistInferenceFieldState(): ArtistInferenceFieldState {
    val map = rememberSaveable {
        mutableStateMapOf<ArtistInferenceField, Boolean>().apply {
            ArtistInferenceField.entries.forEach {
                this[it] = false
            }
        }
    }
    return remember(map) { ArtistInferenceFieldState(map) }
}
