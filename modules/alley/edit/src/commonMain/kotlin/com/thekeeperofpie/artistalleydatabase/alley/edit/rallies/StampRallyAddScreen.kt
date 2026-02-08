package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_title_adding
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_action_save_and_exit_tooltip
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_action_save_tooltip
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ContentSavingBox
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.FormSaveButton
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ScrollableSideBySide
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.GenericTaskErrorEffect
import com.thekeeperofpie.artistalleydatabase.utils_compose.TaskState
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import org.jetbrains.compose.resources.stringResource

object StampRallyAddScreen {

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        stampRallyId: String,
        graph: ArtistAlleyEditGraph,
        onClickBack: (force: Boolean) -> Unit,
        viewModel: StampRallyAddViewModel = viewModel {
            graph.stampRallyAddViewModelFactory.create(
                dataYear = dataYear,
                stampRallyId = stampRallyId,
                savedStateHandle = createSavedStateHandle(),
            )
        },
    ) {
        val seriesById by viewModel.tagAutocomplete.seriesById.collectAsStateWithLifecycle()
        val merchById by viewModel.tagAutocomplete.merchById.collectAsStateWithLifecycle()
        StampRallyAddScreen(
            dataYear = dataYear,
            state = viewModel.state,
            seriesById = { seriesById },
            seriesPredictions = viewModel::seriesPredictions,
            merchById = { merchById },
            merchPredictions = viewModel::merchPredictions,
            seriesImage = viewModel::seriesImage,
            onClickBack = onClickBack,
            onClickSave = viewModel::onClickSave,
            onClickDone = viewModel::onClickDone,
        )
    }

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        state: State,
        seriesById: () -> Map<String, SeriesInfo>,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        merchById: () -> Map<String, MerchInfo>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        onClickBack: (force: Boolean) -> Unit,
        onClickSave: () -> Unit,
        onClickDone: () -> Unit,
    ) {
        val snackbarHostState = remember { SnackbarHostState() }
        val saveTaskState = state.saveTaskState
        GenericTaskErrorEffect(saveTaskState, snackbarHostState)
        LaunchedEffect(saveTaskState) {
            snapshotFlow { saveTaskState.lastResult }
                .filterNotNull()
                .collectLatest { (isManual, result) ->
                    when (result) {
                        is BackendRequest.StampRallySave.Response.Failed -> {
                            snackbarHostState.showSnackbar(message = result.errorMessage)
                            saveTaskState.clearResult()
                        }
                        is BackendRequest.StampRallySave.Response.Outdated -> {
                            // TODO
                        }
                        is BackendRequest.StampRallySave.Response.Success -> {
                            saveTaskState.clearResult()
                            if (isManual) {
                                onClickBack(true)
                            }
                        }
                    }
                }
        }



        val errorState = rememberErrorState(state.stampRallyFormState)
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(
                                Res.string.alley_edit_artist_edit_title_adding,
                                stringResource(dataYear.shortName),
                            ),
                        )
                    },
                    navigationIcon = { ArrowBackIconButton(onClick = { onClickBack(false) }) },
                    actions = {
                        val enabled = !errorState.hasAnyError
                        FormSaveButton(
                            enabled = enabled,
                            saveTaskState = saveTaskState,
                            tooltip = Res.string.alley_edit_stamp_rally_action_save_tooltip,
                            onClickSave = onClickSave,
                        )
                        TooltipIconButton(
                            icon = Icons.Default.DoneAll,
                            tooltipText = stringResource(Res.string.alley_edit_stamp_rally_action_save_and_exit_tooltip),
                            enabled = enabled,
                            onClick = onClickDone,
                        )
                    },
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = Modifier.fillMaxWidth()
        ) { scaffoldPadding ->
            ContentSavingBox(
                saving = saveTaskState.showBlockingLoadingIndicator,
                modifier = Modifier.padding(scaffoldPadding)
            ) {
                ScrollableSideBySide(
                    showSecondary = { false }, // TODO: Does this need a secondary?
                    primary = {
                        StampRallyForm(
                            state = state.stampRallyFormState,
                            errorState = errorState,
                            initialStampRally = { null },
                            seriesById = seriesById,
                            seriesPredictions = seriesPredictions,
                            merchById = merchById,
                            merchPredictions = merchPredictions,
                            seriesImage = seriesImage,
                        )
                    },
                    secondary = {},
                    secondaryExpanded = {},
                )
            }
        }
    }

    @Stable
    class State(
        val stampRallyFormState: StampRallyFormState,
        val saveTaskState: TaskState<BackendRequest.StampRallySave.Response>,
    )
}
