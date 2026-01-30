package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear

object StampRallyEditScreen {

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        stampRallyId: String,
        graph: ArtistAlleyEditGraph,
        onClickBack: (force: Boolean) -> Unit,
        viewModel: StampRallyEditViewModel = viewModel {
            graph.stampRallyEditViewModelFactory.create(
                dataYear = dataYear,
                stampRallyId = stampRallyId,
                savedStateHandle = createSavedStateHandle(),
            )
        },
    ) {
        Text("TODO: Stamp rally edit screen")
    }
}
