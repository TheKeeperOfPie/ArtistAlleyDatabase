package com.thekeeperofpie.artistalleydatabase.alley.edit.series

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_resolution_title
import com.thekeeperofpie.artistalleydatabase.alley.artist.SeriesPrediction
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.edit.tags.TagResolutionScreen
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesRow

object SeriesResolutionScreen {

    @Composable
    operator fun invoke(
        seriesId: String,
        graph: ArtistAlleyEditGraph,
        onClickBack: () -> Unit,
        viewModel: SeriesResolutionViewModel = viewModel {
            graph.seriesResolutionViewModelFactory.create(seriesId, createSavedStateHandle())
        },
    ) {
        val artists by viewModel.artists.collectAsStateWithLifecycle(emptyList())
        var chosenSeries by rememberSaveable { mutableStateOf<SeriesInfo?>(null) }
        TagResolutionScreen(
            tagId = seriesId,
            title = Res.string.alley_edit_series_resolution_title,
            artists = { artists },
            chosenValue = { chosenSeries },
            onChosenValueChange = { chosenSeries = it },
            predictions = viewModel::seriesPredictions,
            prediction = { query, value ->
                SeriesPrediction(query, value)
            },
            progress = viewModel.progress,
            onClickBack = onClickBack,
            onClickDone = { chosenSeries?.let { viewModel.onClickDone(it) } },
        ) {
            val series = chosenSeries
            if (series != null) {
                OutlinedCard {
                    SeriesRow(
                        series = series,
                        image = { viewModel.seriesImage(series) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
