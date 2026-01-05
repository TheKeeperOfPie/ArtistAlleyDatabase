package com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_add_action_same_artist_confirm
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_add_action_same_artist_deny
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_add_same_artist_confirm_prompt
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_action_confirm_merge
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_action_dismiss_merge
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_select_all
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import org.jetbrains.compose.resources.stringResource

@Composable
private fun ArtistInferenceMergeList(
    previousYearData: ArtistPreviousYearData,
    fieldState: ArtistInferenceFieldState? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (fieldState != null) {
            val groupState = when {
                fieldState.map.values.all { it } -> ToggleableState.On
                fieldState.map.values.any { it } -> ToggleableState.Indeterminate
                else -> ToggleableState.Off
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            ) {

                TriStateCheckbox(
                    state = groupState,
                    onClick = {
                        val newValue = when (groupState) {
                            ToggleableState.On -> false
                            ToggleableState.Off,
                            ToggleableState.Indeterminate,
                                -> true
                        }
                        fieldState.map.keys.toSet().forEach {
                            fieldState[it] = newValue
                        }
                    },
                )
                Text(stringResource(Res.string.alley_edit_artist_field_label_select_all))
            }
        }

        ArtistInferenceField.entries.forEach { field ->
            val fieldText = when (field) {
                ArtistInferenceField.NAME -> previousYearData.name?.ifBlank { null }
                ArtistInferenceField.SOCIAL_LINKS -> previousYearData.socialLinks.ifEmpty { null }
                    ?.joinToString("\n")
                ArtistInferenceField.STORE_LINKS -> previousYearData.storeLinks.ifEmpty { null }
                    ?.joinToString("\n")
                ArtistInferenceField.SERIES -> previousYearData.seriesInferred.ifEmpty { null }
                    ?.joinToString()
                ArtistInferenceField.MERCH -> previousYearData.merchInferred.ifEmpty { null }
                    ?.joinToString()
            }
            if (fieldText != null) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (fieldState != null) {
                            Checkbox(
                                checked = fieldState[field],
                                onCheckedChange = { fieldState[field] = it },
                            )
                        }
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
    }
}

@Composable
internal fun SameArtistPrompt(
    sameArtist: LoadingResult<ArtistPreviousYearData>,
    onDenySameArtist: () -> Unit,
    onConfirmSameArtist: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        val previousYearData = sameArtist.result
        if (previousYearData != null) {
            ArtistInferenceMergeList(previousYearData)
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

@Composable
fun MergeArtistPrompt(
    previousYearData: ArtistPreviousYearData,
    onConfirmMerge: (Map<ArtistInferenceField, Boolean>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        val fieldState = rememberArtistInferenceFieldState()
        ArtistInferenceMergeList(previousYearData, fieldState)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
        ) {
            FilledTonalButton(onClick = { onConfirmMerge(emptyMap()) }) {
                Text(stringResource(Res.string.alley_edit_artist_edit_action_dismiss_merge))
            }
            FilledTonalButton(onClick = { onConfirmMerge(fieldState.map.toMap()) }) {
                Text(stringResource(Res.string.alley_edit_artist_edit_action_confirm_merge))
            }
        }
    }
}
