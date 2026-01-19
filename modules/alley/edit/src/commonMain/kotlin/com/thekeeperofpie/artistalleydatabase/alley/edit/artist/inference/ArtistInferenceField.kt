package com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_merch_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_name
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_series_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_social_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_store_links
import org.jetbrains.compose.resources.StringResource

enum class ArtistInferenceField(val label: StringResource) {
    NAME(Res.string.alley_edit_artist_field_label_name),
    SOCIAL_LINKS(Res.string.alley_edit_artist_field_label_social_links),
    STORE_LINKS(Res.string.alley_edit_artist_field_label_store_links),
    SERIES(Res.string.alley_edit_artist_field_label_series_inferred),
    MERCH(Res.string.alley_edit_artist_field_label_merch_inferred),
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
