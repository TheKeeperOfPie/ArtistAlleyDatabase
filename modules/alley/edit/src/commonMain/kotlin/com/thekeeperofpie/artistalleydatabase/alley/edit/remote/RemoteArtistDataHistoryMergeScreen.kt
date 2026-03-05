package com.thekeeperofpie.artistalleydatabase.alley.edit.remote

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_form_merge_outdated
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistRemoteEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.GenericTaskErrorEffect
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationResults
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import org.jetbrains.compose.resources.getString
import kotlin.time.Instant

internal object RemoteArtistDataHistoryMergeScreen {

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        id: ArtistRemoteEntry.Id,
        timestamp: Instant,
        graph: ArtistAlleyEditGraph,
        onClickBack: (force: Boolean) -> Unit,
        viewModel: RemoteArtistDataHistoryMergeViewModel = viewModel {
            graph.remoteArtistDataHistoryMergeViewModelFactory.create(
                dataYear = dataYear,
                id = id,
                timestamp = timestamp,
            )
        },
    ) {
        val entry by viewModel.entry.collectAsStateWithLifecycle()
        val entryInfo by viewModel.entryInfo.collectAsStateWithLifecycle()
        val seriesById by viewModel.tagAutocomplete.seriesById.collectAsStateWithLifecycle()
        val merchById by viewModel.tagAutocomplete.merchById.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }
        val saveTaskState = viewModel.saveTaskState
        RemoteArtistDataMergeScreen(
            dataYear = dataYear,
            entry = entry,
            entryInfo = entryInfo?.second,
            confirmedArtistId = { entry?.confirmedId },
            snackbarHostState = snackbarHostState,
            saving = { saveTaskState.showBlockingLoadingIndicator },
            seriesById = { seriesById },
            merchById = { merchById },
            seriesImage = viewModel::seriesImage,
            inferredArtists = { LoadingResult.empty() },
            onConfirmId = {},
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
                        is BackendRequest.SaveRemoteArtistData.Response.Failed -> {
                            snackbarHostState.showSnackbar(message = result.errorMessage)
                            saveTaskState.clearResult()
                        }
                        is BackendRequest.SaveRemoteArtistData.Response.Outdated -> {
                            snackbarHostState.showSnackbar(message = getString(Res.string.alley_edit_artist_form_merge_outdated))
                            saveTaskState.clearResult()
                        }
                        is BackendRequest.SaveRemoteArtistData.Response.Success -> {
                            saveTaskState.clearResult()
                            onClickBack(true)
                        }
                    }
                }
        }
    }
}
