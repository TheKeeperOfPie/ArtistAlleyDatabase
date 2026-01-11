package com.thekeeperofpie.artistalleydatabase.alley.edit.form

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_form_merge_outdated
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.GenericTaskErrorEffect
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationResults
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import org.jetbrains.compose.resources.getString
import kotlin.time.Instant
import kotlin.uuid.Uuid

object ArtistFormHistoryScreen {

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        artistId: Uuid,
        formTimestamp: Instant,
        graph: ArtistAlleyEditGraph,
        onClickBack: (force: Boolean) -> Unit,
        viewModel: ArtistFormHistoryViewModel = viewModel {
            graph.artistFormHistoryViewModelFactory.create(
                dataYear = dataYear,
                artistId = artistId,
                formTimestamp = formTimestamp,
                savedStateHandle = createSavedStateHandle(),
            )
        },
    ) {
        val artistWithFormEntry by viewModel.entry.collectAsStateWithLifecycle()
        val seriesById by viewModel.seriesById.collectAsStateWithLifecycle()
        val merchById by viewModel.merchById.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }
        val saveTaskState = viewModel.saveTaskState
        ArtistFormMergeScreen(
            dataYear = dataYear,
            artistId = artistId,
            snackbarHostState = snackbarHostState,
            entry = { artistWithFormEntry?.run { artist to formDiff } },
            saving = { saveTaskState.showBlockingLoadingIndicator },
            seriesById = { seriesById },
            merchById = { merchById },
            seriesImage = viewModel::seriesImage,
            onClickBack = onClickBack,
            onClickSave = viewModel::onClickSave,
        )

        GenericTaskErrorEffect(saveTaskState, snackbarHostState)

        val navigationResults = LocalNavigationResults.current
        LaunchedEffect(navigationResults, saveTaskState) {
            snapshotFlow { saveTaskState.lastResult }
                .filterNotNull()
                .collectLatest { (_, result) ->
                    when (result) {
                        is BackendRequest.ArtistCommitForm.Response.Failed -> {
                            snackbarHostState.showSnackbar(message = result.errorMessage)
                            saveTaskState.clearResult()
                        }
                        is BackendRequest.ArtistCommitForm.Response.Outdated -> {
                            snackbarHostState.showSnackbar(message = getString(Res.string.alley_edit_artist_form_merge_outdated))
                            saveTaskState.clearResult()
                        }
                        is BackendRequest.ArtistCommitForm.Response.Success -> {
                            saveTaskState.clearResult()
                            onClickBack(true)
                        }
                    }
                }
        }
    }
}
