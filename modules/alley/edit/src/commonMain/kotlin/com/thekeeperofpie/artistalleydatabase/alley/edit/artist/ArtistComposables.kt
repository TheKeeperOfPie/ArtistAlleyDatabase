package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_add_images
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_edit_images
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_add_potential_same_artists
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference.ArtistInference
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.PlatformImageCache
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.flow.StateFlow
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
