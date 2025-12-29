package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_add_images
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_edit_images
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_save_tooltip
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.PlatformImageCache
import com.thekeeperofpie.artistalleydatabase.alley.models.network.ArtistSave
import com.thekeeperofpie.artistalleydatabase.utils_compose.TaskState
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import org.jetbrains.compose.resources.stringResource

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
