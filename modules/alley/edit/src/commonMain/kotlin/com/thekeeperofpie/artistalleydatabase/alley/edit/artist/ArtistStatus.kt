package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_status_final
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_status_final_description
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_status_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_status_inferred_description
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_status_locked
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_status_locked_description
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_status_locked_unknown_editor
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_status_needs_attention
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_status_needs_attention_description
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_status_unknown
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_status_unknown_description
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistStatus
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

val ArtistStatus.icon: ImageVector
    get() = when (this) {
        ArtistStatus.UNKNOWN -> Icons.Default.DeviceUnknown
        ArtistStatus.LOCKED -> Icons.Default.Lock
        ArtistStatus.NEEDS_ATTENTION -> Icons.Default.Warning
        ArtistStatus.INFERRED -> Icons.Default.Tag
        ArtistStatus.FINAL -> Icons.Default.Check
    }

val ArtistStatus.title: StringResource
    get() = when (this) {
        ArtistStatus.UNKNOWN -> Res.string.alley_edit_artist_status_unknown
        ArtistStatus.LOCKED -> Res.string.alley_edit_artist_status_locked
        ArtistStatus.NEEDS_ATTENTION -> Res.string.alley_edit_artist_status_needs_attention
        ArtistStatus.INFERRED -> Res.string.alley_edit_artist_status_inferred
        ArtistStatus.FINAL -> Res.string.alley_edit_artist_status_final
    }

@Stable
@Composable
fun ArtistStatus.description(lastEditor: String?): String = when (this) {
    ArtistStatus.UNKNOWN -> stringResource(Res.string.alley_edit_artist_status_unknown_description)
    ArtistStatus.LOCKED -> stringResource(
        Res.string.alley_edit_artist_status_locked_description,
        lastEditor ?: stringResource(Res.string.alley_edit_artist_status_locked_unknown_editor),
    )
    ArtistStatus.NEEDS_ATTENTION -> stringResource(Res.string.alley_edit_artist_status_needs_attention_description)
    ArtistStatus.INFERRED -> stringResource(Res.string.alley_edit_artist_status_inferred_description)
    ArtistStatus.FINAL -> stringResource(Res.string.alley_edit_artist_status_final_description)
}

val ArtistStatus.shouldStartLocked: Boolean
    get() = when (this) {
        ArtistStatus.UNKNOWN,
        ArtistStatus.INFERRED,
            -> false
        ArtistStatus.LOCKED,
        ArtistStatus.NEEDS_ATTENTION,
        ArtistStatus.FINAL,
            -> true
    }
