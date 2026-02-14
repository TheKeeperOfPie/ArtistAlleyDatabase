package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies.form

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
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_form_merge_outdated
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

internal object StampRallyFormHistoryScreen {

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        artistId: Uuid,
        stampRallyId: String,
        formTimestamp: Instant,
        graph: ArtistAlleyEditGraph,
        onClickBack: (force: Boolean) -> Unit,
        viewModel: StampRallyFormHistoryViewModel = viewModel {
            graph.stampRallyFormHistoryViewModelFactory.create(
                dataYear = dataYear,
                artistId = artistId,
                stampRallyId = stampRallyId,
                formTimestamp = formTimestamp,
                savedStateHandle = createSavedStateHandle(),
            )
        },
    ) {
        val stampRallyWithFormEntry by viewModel.entry.collectAsStateWithLifecycle()
        val seriesById by viewModel.tagAutocomplete.seriesById.collectAsStateWithLifecycle()
        val merchById by viewModel.tagAutocomplete.merchById.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }
        val saveTaskState = viewModel.saveTaskState
        StampRallyFormMergeScreen(
            dataYear = dataYear,
            stampRallyId = stampRallyId,
            snackbarHostState = snackbarHostState,
            entry = { stampRallyWithFormEntry?.run { stampRally to formDiff } },
            saving = { saveTaskState.showBlockingLoadingIndicator },
            seriesById = { seriesById },
            merchById = { merchById },
            seriesImage = viewModel::seriesImage,
            onClickBack = onClickBack,
            onClickSave = viewModel::onClickSave,
            onConfirmDelete = null,
        )

        GenericTaskErrorEffect(saveTaskState, snackbarHostState)

        val navigationResults = LocalNavigationResults.current
        LaunchedEffect(navigationResults, saveTaskState) {
            snapshotFlow { saveTaskState.lastResult }
                .filterNotNull()
                .collectLatest { (_, result) ->
                    when (result) {
                        is BackendRequest.StampRallyCommitForm.Response.Failed -> {
                            snackbarHostState.showSnackbar(message = result.errorMessage)
                            saveTaskState.clearResult()
                        }
                        is BackendRequest.StampRallyCommitForm.Response.Outdated -> {
                            snackbarHostState.showSnackbar(message = getString(Res.string.alley_edit_stamp_rally_form_merge_outdated))
                            saveTaskState.clearResult()
                        }
                        is BackendRequest.StampRallyCommitForm.Response.Success -> {
                            saveTaskState.clearResult()
                            onClickBack(true)
                        }
                    }
                }
        }
    }
}
