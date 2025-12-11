package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.serialization.json.Json
import kotlin.uuid.Uuid

object ArtistHistoryScreen {

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        artistId: Uuid,
        graph: ArtistAlleyEditGraph,
        viewModel: ArtistHistoryViewModel = viewModel {
            graph.artistHistoryViewModelFactory.create(dataYear, artistId)
        },
    ) {
        Column {
            val history by viewModel.history.collectAsStateWithLifecycle()
            history.forEach {
                Text(Json.encodeToString(it))
            }
        }
    }
}
