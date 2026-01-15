package com.thekeeperofpie.artistalleydatabase.alley.edit.merch

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_resolution_title
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.edit.tags.TagResolutionScreen
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo

object MerchResolutionScreen {

    @Composable
    operator fun invoke(
        merchId: String,
        graph: ArtistAlleyEditGraph,
        onClickBack: () -> Unit,
        viewModel: MerchResolutionViewModel = viewModel {
            graph.merchResolutionViewModelFactory.create(merchId, createSavedStateHandle())
        },
    ) {
        val artists by viewModel.artists.collectAsStateWithLifecycle(emptyList())
        var chosenMerch by rememberSaveable { mutableStateOf<MerchInfo?>(null) }
        TagResolutionScreen(
            tagId = merchId,
            title = Res.string.alley_edit_series_resolution_title,
            artists = { artists },
            chosenValue = { chosenMerch },
            onChosenValueChange = { chosenMerch = it },
            predictions = viewModel::merchPredictions,
            prediction = { _, value -> Text(value.name) },
            progress = viewModel.progress,
            onClickBack = onClickBack,
            onClickDone = { chosenMerch?.let { viewModel.onClickDone(it) } },
        ) {
            val merch = chosenMerch
            if (merch != null) {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(text = merch.name)
                        val notes = merch.notes
                        if (!notes.isNullOrBlank()) {
                            Text(text = notes, modifier = Modifier.padding(start = 80.dp))
                        }
                    }
                }
            }
        }
    }
}
