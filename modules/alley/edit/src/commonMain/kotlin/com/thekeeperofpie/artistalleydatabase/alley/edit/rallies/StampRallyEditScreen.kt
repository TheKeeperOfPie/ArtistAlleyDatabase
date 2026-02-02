package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.CircularWavyProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_action_save_and_exit_tooltip
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_action_save_tooltip
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_delete_failed
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_delete_failed_outdated
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_title_editing_booth_name
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_title_editing_name
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_error_saving_bad_fields
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.EditImagesButton
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ContentSavingBox
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.DeleteButton
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.FormHistoryButton
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.FormRefreshButton
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.FormSaveButton
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.GenericExitDialog
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ScrollableSideBySide
import com.thekeeperofpie.artistalleydatabase.alley.images.ImageGrid
import com.thekeeperofpie.artistalleydatabase.alley.images.ImagePager
import com.thekeeperofpie.artistalleydatabase.alley.images.rememberImagePagerState
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.JobProgress
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.GenericTaskErrorEffect
import com.thekeeperofpie.artistalleydatabase.utils_compose.TaskState
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

object StampRallyEditScreen {

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        stampRallyId: String,
        graph: ArtistAlleyEditGraph,
        onClickBack: (force: Boolean) -> Unit,
        onClickEditImages: (displayName: String, List<EditImage>) -> Unit,
        onClickHistory: () -> Unit,
        viewModel: StampRallyEditViewModel = viewModel {
            graph.stampRallyEditViewModelFactory.create(
                dataYear = dataYear,
                stampRallyId = stampRallyId,
                savedStateHandle = createSavedStateHandle(),
            )
        },
    ) {
        LaunchedEffect(viewModel) {
            viewModel.initialize()
        }
        val seriesById by viewModel.tagAutocomplete.seriesById.collectAsStateWithLifecycle(emptyMap())
        val merchById by viewModel.tagAutocomplete.merchById.collectAsStateWithLifecycle(emptyMap())
        StampRallyEditScreen(
            dataYear = dataYear,
            state = viewModel.state,
            seriesById = { seriesById },
            seriesPredictions = viewModel::seriesPredictions,
            merchById = { merchById },
            merchPredictions = viewModel::merchPredictions,
            seriesImage = viewModel::seriesImage,
            hasPendingChanges = viewModel::hasPendingChanges,
            onClickBack = onClickBack,
            onClickEditImages = {
                onClickEditImages(
                    viewModel.state.stampRallyFormState.fandom.value.text.toString(),
                    it
                )
            },
            onClickRefresh = { viewModel.initialize(force = true) },
            onClickHistory = onClickHistory,
            onClickSave = viewModel::onClickSave,
            onClickDone = viewModel::onClickDone,
            onConfirmDelete = viewModel::onConfirmDelete,
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
        hasPendingChanges: () -> Boolean,
        onClickBack: (force: Boolean) -> Unit,
        onClickEditImages: (List<EditImage>) -> Unit,
        onClickRefresh: () -> Unit,
        onClickHistory: () -> Unit,
        onClickSave: () -> Unit,
        onClickDone: () -> Unit,
        onConfirmDelete: () -> Unit,
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
                            snackbarHostState.showSnackbar(message = getString(Res.string.alley_edit_stamp_rally_edit_delete_failed_outdated))
                            saveTaskState.clearResult()
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

        DeleteProgressEffect(state.deleteProgress, snackbarHostState, onClickBack)
        val errorState = rememberErrorState(state.stampRallyFormState)
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val formState = state.stampRallyFormState
                        val name = formState.fandom.value.text.ifBlank {
                            formState.editorState.id.value.text.toString()
                        }
                        val conventionName = stringResource(dataYear.shortName)
                        val booth = formState.hostTable.value.text.toString()
                        val text = if (booth.isNotEmpty()) {
                            stringResource(
                                Res.string.alley_edit_stamp_rally_edit_title_editing_booth_name,
                                conventionName,
                                booth,
                                name,
                            )
                        } else {
                            stringResource(
                                Res.string.alley_edit_stamp_rally_edit_title_editing_name,
                                conventionName,
                                name,
                            )
                        }
                        Text(text = text)
                    },
                    navigationIcon = { ArrowBackIconButton(onClick = { onClickBack(false) }) },
                    actions = {
                        AppBarActions(
                            errorState = errorState,
                            saveTaskState = saveTaskState,
                            hasPendingChanges = hasPendingChanges,
                            onClickRefresh = onClickRefresh,
                            onClickHistory = onClickHistory,
                            onClickSave = onClickSave,
                            onClickDone = onClickDone,
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
                val imagePagerState = rememberImagePagerState(state.stampRallyFormState.images, 0)
                val initialStampRally by state.initialStampRally.collectAsStateWithLifecycle()
                val stampRallyProgress by state.stampRallyProgress.collectAsStateWithLifecycle()

                ScrollableSideBySide(
                    showSecondary = { false },
                    primary = {
                        Form(
                            state = state,
                            stampRallyFormState = state.stampRallyFormState,
                            initialStampRally = { initialStampRally },
                            errorState = errorState,
                            seriesById = seriesById,
                            seriesPredictions = seriesPredictions,
                            merchById = merchById,
                            merchPredictions = merchPredictions,
                            seriesImage = seriesImage,
                            onConfirmDelete = onConfirmDelete,
                        )
                    },
                    secondary = {
                        ImagePager(
                            images = state.stampRallyFormState.images,
                            pagerState = imagePagerState,
                            sharedElementId = state.stampRallyFormState.editorState.id.value.text.toString(),
                            onClickPage = {
                                // TODO: Open images screen
                            },
                        )

                        if (initialStampRally != null && stampRallyProgress !is JobProgress.Loading) {
                            EditImagesButton(
                                images = state.stampRallyFormState.images,
                                onClickEdit = { onClickEditImages(state.stampRallyFormState.images.toList()) },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    },
                    secondaryExpanded = {
                        Column {
                            EditImagesButton(
                                images = state.stampRallyFormState.images,
                                onClickEdit = { onClickEditImages(state.stampRallyFormState.images.toList()) },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            ImageGrid(
                                images = state.stampRallyFormState.images,
                                onClickImage = {},
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            val errorMessage =
                stringResource(Res.string.alley_edit_stamp_rally_error_saving_bad_fields)
            GenericExitDialog(
                onClickBack = { onClickBack(true) },
                onClickSave = onClickDone,
                saveErrorMessage = { errorMessage.takeIf { errorState.hasAnyError } },
            )
        }
    }

    @Composable
    private fun DeleteProgressEffect(
        deleteProgress: StateFlow<JobProgress<BackendRequest.StampRallyDelete.Response>>,
        snackbarHostState: SnackbarHostState,
        onClickBack: (force: Boolean) -> Unit,
    ) {
        LaunchedEffect(deleteProgress) {
            deleteProgress.collectLatest {
                when (it) {
                    is JobProgress.Finished.Result<BackendRequest.StampRallyDelete.Response> ->
                        when (it.value) {
                            is BackendRequest.StampRallyDelete.Response.Failed -> snackbarHostState.showSnackbar(
                                message = getString(Res.string.alley_edit_stamp_rally_edit_delete_failed)
                            )
                            is BackendRequest.StampRallyDelete.Response.Outdated -> snackbarHostState.showSnackbar(
                                message = getString(Res.string.alley_edit_stamp_rally_edit_delete_failed_outdated)
                            )
                            BackendRequest.StampRallyDelete.Response.Success -> onClickBack(true)
                        }
                    is JobProgress.Finished.UnhandledError<*> ->
                        snackbarHostState.showSnackbar(message = getString(Res.string.alley_edit_stamp_rally_edit_delete_failed))
                    is JobProgress.Idle<*>,
                    is JobProgress.Loading<*>,
                        -> Unit
                }
            }
        }
    }

    @Composable
    private fun AppBarActions(
        errorState: StampRallyErrorState,
        saveTaskState: TaskState<BackendRequest.StampRallySave.Response>,
        hasPendingChanges: () -> Boolean,
        onClickRefresh: () -> Unit,
        onClickHistory: () -> Unit,
        onClickSave: () -> Unit,
        onClickDone: () -> Unit,
    ) {
        FormRefreshButton(hasPendingChanges, onClickRefresh)
        FormHistoryButton(hasPendingChanges, onClickHistory)

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
    }

    @Composable
    private fun Form(
        state: State,
        stampRallyFormState: StampRallyFormState,
        initialStampRally: () -> StampRallyDatabaseEntry?,
        errorState: StampRallyErrorState,
        seriesById: () -> Map<String, SeriesInfo>,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        merchById: () -> Map<String, MerchInfo>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        onConfirmDelete: () -> Unit,
    ) {
        Column {
            val initialStampRally = initialStampRally()
            val stampRallyProgress by state.stampRallyProgress.collectAsStateWithLifecycle()
            if (initialStampRally == null || stampRallyProgress is JobProgress.Loading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize().padding(32.dp)
                ) {
                    CircularWavyProgressIndicator()
                }
            } else {
                StampRallyForm(
                    initialStampRally = { initialStampRally },
                    state = stampRallyFormState,
                    errorState = errorState,
                    seriesById = seriesById,
                    seriesPredictions = seriesPredictions,
                    merchById = merchById,
                    merchPredictions = merchPredictions,
                    seriesImage = seriesImage,
                )

                DeleteButton(
                    onConfirmDelete = onConfirmDelete,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }

    @Stable
    class State(
        val stampRallyProgress: StateFlow<JobProgress<Unit>>,
        val initialStampRally: StateFlow<StampRallyDatabaseEntry?>,
        val stampRallyFormState: StampRallyFormState,
        val saveTaskState: TaskState<BackendRequest.StampRallySave.Response>,
        val deleteProgress: StateFlow<JobProgress<BackendRequest.StampRallyDelete.Response>>,
    )
}
