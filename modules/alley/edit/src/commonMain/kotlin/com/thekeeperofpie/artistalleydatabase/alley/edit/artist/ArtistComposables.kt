package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_add_images
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_edit_images
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_save_tooltip
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_add_action_same_artist_confirm
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_add_action_same_artist_deny
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_add_potential_same_artists
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_add_same_artist_confirm_prompt
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_merch_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_name
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_series_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_social_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_store_links
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.PlatformImageCache
import com.thekeeperofpie.artistalleydatabase.alley.models.network.ArtistSave
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.TaskState
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun EditImagesButton(
    images: SnapshotStateList<EditImage>,
    onClickEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (images.isEmpty()) {
        val launcher = rememberFilePickerLauncher(
            type = FileKitType.Image,
            mode = FileKitMode.Multiple(),
        ) {
            if (it != null) {
                images += it
                    .map(PlatformImageCache::add)
                    .map(EditImage::LocalImage)
            }
        }
        FilledTonalButton(onClick = { launcher.launch() }, modifier = modifier.padding(16.dp)) {
            Text(stringResource(Res.string.alley_edit_artist_action_add_images))
        }
    } else {
        FilledTonalButton(onClick = onClickEdit, modifier = modifier.padding(16.dp)) {
            Text(stringResource(Res.string.alley_edit_artist_action_edit_images))
        }
    }
}

@Composable
internal fun ArtistSaveButton(
    enabled: Boolean,
    saveTaskState: TaskState<ArtistSave.Response>,
    onClickSave: () -> Unit,
) {
    Box(contentAlignment = Alignment.Center) {
        val isSaving = saveTaskState.isActive && !saveTaskState.isManualTrigger
        TooltipIconButton(
            icon = Icons.Default.Save,
            tooltipText = stringResource(Res.string.alley_edit_artist_action_save_tooltip),
            enabled = enabled,
            onClick = onClickSave,
            modifier = Modifier.alpha(if (isSaving) 0.5f else 1f)
        )

        AnimatedVisibility(
            visible = isSaving,
            modifier = Modifier.matchParentSize()
        ) {
            CircularWavyProgressIndicator(modifier = Modifier.padding(4.dp))
        }
    }
}

@Composable
fun PotentialSameArtists(
    inferredArtists: StateFlow<List<ArtistInference.MatchResult>>,
    onClickSameArtist: (artistId: Uuid) -> Unit,
) {
    val inferredArtists by inferredArtists.collectAsStateWithLifecycle()
    if (inferredArtists.isNotEmpty()) {
        OutlinedCard(Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)) {
            Text(
                text = stringResource(Res.string.alley_edit_artist_add_potential_same_artists),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
            ) {
                inferredArtists.forEach {
                    val via = it.via
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                            positioning = TooltipAnchorPosition.Below,
                            spacingBetweenTooltipAndAnchor = 0.dp,
                        ),
                        tooltip = { PlainTooltip { Text(via) } },
                        state = rememberTooltipState(),
                    ) {
                        AssistChip(
                            onClick = { onClickSameArtist(it.data.id) },
                            label = { Text(it.name) },
                        )
                    }
                }
            }
        }
    }
}

private enum class SameArtistField(val label: StringResource) {
    NAME(Res.string.alley_edit_artist_field_label_name),
    SOCIAL_LINKS(Res.string.alley_edit_artist_field_label_social_links),
    STORE_LINKS(Res.string.alley_edit_artist_field_label_store_links),
    SERIES(Res.string.alley_edit_artist_field_label_series_inferred),
    MERCH(Res.string.alley_edit_artist_field_label_merch_inferred),
}

@Composable
internal fun SameArtistPrompt(
    sameArtist: LoadingResult<ArtistInference.PreviousYearData>,
    onDenySameArtist: () -> Unit,
    onConfirmSameArtist: () -> Unit,
) {
    Column {
        val artist = sameArtist.result
        SameArtistField.entries.forEach { field ->
            val fieldText = when (field) {
                SameArtistField.NAME -> artist?.name?.ifBlank { null }
                SameArtistField.SOCIAL_LINKS -> artist?.socialLinks?.ifEmpty { null }?.joinToString("\n")
                SameArtistField.STORE_LINKS -> artist?.storeLinks?.ifEmpty { null }
                    ?.joinToString("\n")
                SameArtistField.SERIES -> artist?.seriesInferred?.ifEmpty { null }
                    ?.joinToString()
                SameArtistField.MERCH -> artist?.merchInferred?.ifEmpty { null }
                    ?.joinToString()
            }
            if (fieldText != null) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(stringResource(field.label))
                        if (fieldText.length < 40) {
                            Text(text = fieldText)
                        }
                    }
                    if (fieldText.length >= 40) {
                        Text(text = fieldText, modifier = Modifier.padding(start = 80.dp))
                    }
                }
            }
        }

        OutlinedCard(modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)) {
            Text(
                text = stringResource(Res.string.alley_edit_artist_add_same_artist_confirm_prompt),
                modifier = Modifier.padding(16.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
            ) {
                FilledTonalButton(onClick = { onDenySameArtist() }) {
                    Text(stringResource(Res.string.alley_edit_artist_add_action_same_artist_deny))
                }
                FilledTonalButton(onClick = { onConfirmSameArtist() }) {
                    Text(stringResource(Res.string.alley_edit_artist_add_action_same_artist_confirm))
                }
            }
        }
    }
}
