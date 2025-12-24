package com.thekeeperofpie.artistalleydatabase.alley.edit.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_title_editing
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_error_saving_bad_fields
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_form_action_save_tooltip
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_form_action_submit_private_key
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_form_private_key_prompt
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_form_saved_changes
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistErrorState
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistForm
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistFormState
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.rememberErrorState
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ContentSavingBox
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.GenericExitDialog
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendFormRequest
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.alley.ui.TooltipIconButton
import com.thekeeperofpie.artistalleydatabase.alley.ui.currentWindowSizeClass
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.TaskState
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

object ArtistFormScreen {

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        artistId: Uuid,
        privateKey: String,
        graph: ArtistAlleyEditGraph,
        onClickBack: (force: Boolean) -> Unit,
        viewModel: ArtistFormViewModel = viewModel {
            graph.artistFormViewModelFactory.create(
                dataYear = dataYear,
                artistId = artistId,
                privateKey = privateKey,
                savedStateHandle = createSavedStateHandle(),
            )
        },
    ) {
        LaunchedEffect(viewModel) {
            viewModel.initialize()
        }
        ArtistFormScreen(
            dataYear = dataYear,
            state = viewModel.state,
            seriesPredictions = viewModel::seriesPredictions,
            merchPredictions = viewModel::merchPredictions,
            seriesImage = viewModel::seriesImage,
            onClickBack = onClickBack,
            onClickDone = viewModel::onClickDone,
            onSubmitPrivateKey = viewModel::onSubmitPrivateKey,
        )
    }

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        state: State,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        onClickBack: (force: Boolean) -> Unit,
        onClickDone: () -> Unit,
        onSubmitPrivateKey: (String) -> Unit,
    ) {
        val snackbarHostState = remember { SnackbarHostState() }
        val saveTaskState = state.saveTaskState
        LaunchedEffect(saveTaskState) {
            snapshotFlow { saveTaskState.lastResult }
                .map { it?.second }
                .filterNotNull()
                .collectLatest {
                    when (it) {
                        is BackendFormRequest.ArtistSave.Response.Failed -> {
                            snackbarHostState.showSnackbar(
                                message = it.throwable.message.orEmpty(),
                                withDismissAction = true,
                                duration = SnackbarDuration.Indefinite,
                            )
                            saveTaskState.clearError()
                        }
                        is BackendFormRequest.ArtistSave.Response.Success -> {
                            snackbarHostState.showSnackbar(
                                message = getString(Res.string.alley_edit_artist_form_saved_changes),
                                duration = SnackbarDuration.Long,
                            )
                            saveTaskState.clearResult()
                        }
                    }
                }
        }

        val windowSizeClass = currentWindowSizeClass()
        val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
        val errorState = rememberErrorState(state.artistFormState.textState)
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(
                                Res.string.alley_edit_artist_edit_title_editing,
                                stringResource(dataYear.shortName),
                                state.artistFormState.textState.name.value.text.ifBlank { state.artistFormState.textState.id.value.text.toString() },
                            )
                        )
                    },
                    navigationIcon = { ArrowBackIconButton(onClick = { onClickBack(false) }) },
                    actions = {
                        val enabled = !errorState.hasAnyError
                        TooltipIconButton(
                            icon = Icons.Default.DoneAll,
                            tooltipText = stringResource(Res.string.alley_edit_artist_form_action_save_tooltip),
                            enabled = enabled,
                            onClick = onClickDone,
                        )
                    },
                    modifier = Modifier
                        .conditionally(!isExpanded, Modifier.widthIn(max = 960.dp))
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = Modifier.fillMaxWidth()
        ) { scaffoldPadding ->
            Box(Modifier.padding(scaffoldPadding)) {
                when (state.progress.collectAsStateWithLifecycle().value) {
                    State.Progress.LOADING ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            CircularWavyProgressIndicator()
                        }
                    State.Progress.LOADED -> {
                        Form(
                            saveTaskState = saveTaskState,
                            artistFormState = state.artistFormState,
                            errorState = errorState,
                            seriesPredictions = seriesPredictions,
                            merchPredictions = merchPredictions,
                            seriesImage = seriesImage,
                            modifier = Modifier.padding(scaffoldPadding),
                        )

                        val errorMessage =
                            stringResource(Res.string.alley_edit_artist_error_saving_bad_fields)
                        GenericExitDialog(
                            onClickBack = { onClickBack(true) },
                            onClickSave = onClickDone,
                            saveErrorMessage = { errorMessage.takeIf { errorState.hasAnyError } },
                        )
                    }
                    State.Progress.BAD_AUTH -> PrivateKeyPrompt(onSubmitPrivateKey)
                }
            }
        }
    }

    @Composable
    private fun Form(
        artistFormState: ArtistFormState,
        errorState: ArtistErrorState,
        saveTaskState: TaskState<BackendFormRequest.ArtistSave.Response>,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        modifier: Modifier = Modifier,
    ) {
        ContentSavingBox(
            saving = saveTaskState.isActive && saveTaskState.isManualTrigger,
            modifier = modifier
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                ArtistForm(
                    state = artistFormState,
                    errorState = errorState,
                    seriesPredictions = seriesPredictions,
                    merchPredictions = merchPredictions,
                    seriesImage = seriesImage,
                    forceLockId = true,
                    showStatus = false,
                    showEditorNotes = false,
                    modifier = Modifier.fillMaxHeight()
                        .width(960.dp)
                        .verticalScroll(rememberScrollState())
                )
                // TODO: Support images?
            }
        }
    }

    @Composable
    private fun PrivateKeyPrompt(onSubmitKey: (String) -> Unit) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .widthIn(max = 960.dp)
            ) {
                Text(
                    text = stringResource(Res.string.alley_edit_artist_form_private_key_prompt),
                    style = MaterialTheme.typography.titleMedium,
                )

                // Do not use rememberTextFieldState to avoid key being persisted
                val state = remember { TextFieldState("") }
                OutlinedTextField(state = state, modifier = Modifier.fillMaxWidth())

                FilledTonalButton(onClick = { onSubmitKey(state.text.toString()) }) {
                    Text(stringResource(Res.string.alley_edit_artist_form_action_submit_private_key))
                }
            }
        }
    }

    @Stable
    class State(
        val progress: StateFlow<Progress>,
        val artistFormState: ArtistFormState,
        val saveTaskState: TaskState<BackendFormRequest.ArtistSave.Response>,
    ) {
        @Serializable
        enum class Progress {
            LOADING, LOADED, BAD_AUTH,
        }
    }
}
