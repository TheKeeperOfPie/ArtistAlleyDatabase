package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_generate_link_content_description
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_history_content_description
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_save_and_exit_tooltip
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_form_link_action_cancel
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_form_link_action_generate
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_form_link_description
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_form_link_description_generated
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_form_link_title
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_pending_form_warning
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_pending_form_warning_action_open
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_title_editing
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_error_saving_bad_fields
import com.thekeeperofpie.artistalleydatabase.alley.PlatformSpecificConfig
import com.thekeeperofpie.artistalleydatabase.alley.PlatformType
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.edit.form.ArtistFormAccessKey
import com.thekeeperofpie.artistalleydatabase.alley.edit.form.ArtistFormMergeScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.ImagesEditScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.secrets.BuildKonfig
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ContentSavingBox
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.GenericExitDialog
import com.thekeeperofpie.artistalleydatabase.alley.images.ImageGrid
import com.thekeeperofpie.artistalleydatabase.alley.images.ImagePager
import com.thekeeperofpie.artistalleydatabase.alley.images.rememberImagePagerState
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.ArtistSave
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.alley.ui.currentWindowSizeClass
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.GenericTaskErrorEffect
import com.thekeeperofpie.artistalleydatabase.utils_compose.TaskState
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationResultEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

object ArtistEditScreen {

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        artistId: Uuid,
        graph: ArtistAlleyEditGraph,
        onClickBack: (force: Boolean) -> Unit,
        onClickEditImages: (displayName: String, List<EditImage>) -> Unit,
        onClickHistory: () -> Unit,
        onClickMerge: () -> Unit,
        onClickDebugForm: (formLink: String) -> Unit,
        viewModel: ArtistEditViewModel = viewModel {
            graph.artistEditViewModelFactory.create(
                dataYear = dataYear,
                artistId = artistId,
                savedStateHandle = createSavedStateHandle(),
            )
        },
    ) {
        NavigationResultEffect(ImagesEditScreen.RESULT_KEY) { images ->
            Snapshot.withMutableSnapshot {
                viewModel.state.artistFormState.images.clear()
                viewModel.state.artistFormState.images += images
            }
        }
        NavigationResultEffect(ArtistHistoryScreen.RESULT_KEY) {
            viewModel.initialize(force = true)
        }
        NavigationResultEffect(ArtistFormMergeScreen.RESULT_KEY) {
            viewModel.initialize(force = true)
        }
        LaunchedEffect(viewModel) {
            viewModel.initialize()
        }
        val seriesById by viewModel.tagAutocomplete.seriesById.collectAsStateWithLifecycle(emptyMap())
        val merchById by viewModel.tagAutocomplete.merchById.collectAsStateWithLifecycle(emptyMap())
        ArtistEditScreen(
            dataYear = dataYear,
            state = viewModel.state,
            seriesById = { seriesById },
            seriesPredictions = viewModel::seriesPredictions,
            merchById = { merchById },
            merchPredictions = viewModel::merchPredictions,
            seriesImage = viewModel::seriesImage,
            generateFormLink = viewModel::generateFormLink,
            onClickBack = onClickBack,
            onClickEditImages = {
                onClickEditImages(
                    viewModel.state.artistFormState.info.name.value.text.toString(),
                    it
                )
            },
            onClickHistory = onClickHistory,
            onClickMerge = onClickMerge,
            onClickSave = viewModel::onClickSave,
            onClickDone = viewModel::onClickDone,
            onClickDebugForm = onClickDebugForm,
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
        generateFormLink: () -> Unit,
        onClickBack: (force: Boolean) -> Unit,
        onClickEditImages: (List<EditImage>) -> Unit,
        onClickHistory: () -> Unit,
        onClickMerge: () -> Unit,
        onClickSave: () -> Unit,
        onClickDone: () -> Unit,
        onClickDebugForm: (formLink: String) -> Unit,
    ) {
        val snackbarHostState = remember { SnackbarHostState() }
        val saveTaskState = state.saveTaskState
        GenericTaskErrorEffect(saveTaskState, snackbarHostState)
        LaunchedEffect(saveTaskState) {
            snapshotFlow { saveTaskState.lastResult }
                .filterNotNull()
                .collectLatest { (isManual, result) ->
                    when (result) {
                        is ArtistSave.Response.Failed -> {
                            snackbarHostState.showSnackbar(message = result.errorMessage)
                            saveTaskState.clearResult()
                        }
                        is ArtistSave.Response.Outdated -> {
                            // TODO
                        }
                        is ArtistSave.Response.Success -> {
                            saveTaskState.clearResult()
                            if (isManual) {
                                onClickBack(true)
                            }
                        }
                    }
                }
        }

        val windowSizeClass = currentWindowSizeClass()
        val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
        val errorState = rememberErrorState(state.artistFormState)
        var showFormLinkDialog by mutableStateOf(false)
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(
                                Res.string.alley_edit_artist_edit_title_editing,
                                stringResource(dataYear.shortName),
                                state.artistFormState.info.name.value.text.ifBlank {
                                    state.artistFormState.editorState.id.value.text.toString()
                                },
                            ),
                        )
                    },
                    navigationIcon = { ArrowBackIconButton(onClick = { onClickBack(false) }) },
                    actions = {
                        AppBarActions(
                            errorState = errorState,
                            saveTaskState = saveTaskState,
                            onClickGenerateFormLink = { showFormLinkDialog = true },
                            onClickHistory = onClickHistory,
                            onClickSave = onClickSave,
                            onClickDone = onClickDone,
                        )
                    },
                    modifier = Modifier
                        .conditionally(!isExpanded, Modifier.widthIn(max = 960.dp))
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = Modifier.fillMaxWidth()
        ) { scaffoldPadding ->
            ContentSavingBox(
                saving = saveTaskState.showBlockingLoadingIndicator,
                modifier = Modifier.padding(scaffoldPadding)
            ) {
                val imagePagerState = rememberImagePagerState(state.artistFormState.images, 0)
                val form = remember {
                    movableContentOf { modifier: Modifier ->
                        Column(modifier = modifier) {
                            val initialArtist by state.initialArtist.collectAsStateWithLifecycle()
                            val hasPendingFormSubmission by state.hasPendingFormSubmission.collectAsStateWithLifecycle()
                            if (hasPendingFormSubmission) {
                                PendingFormSubmissionPrompt(onClickMerge)
                            }
                            ArtistForm(
                                initialArtist = { initialArtist },
                                state = state.artistFormState,
                                errorState = errorState,
                                seriesById = seriesById,
                                seriesPredictions = seriesPredictions,
                                merchById = merchById,
                                merchPredictions = merchPredictions,
                                seriesImage = seriesImage,
                            )
                        }
                    }
                }
                if (isExpanded) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        form(
                            Modifier.fillMaxHeight()
                                .width(800.dp)
                                .verticalScroll(rememberScrollState())
                        )
                        Column {
                            EditImagesButton(
                                images = state.artistFormState.images,
                                onClickEdit = { onClickEditImages(state.artistFormState.images.toList()) },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            ImageGrid(
                                images = state.artistFormState.images,
                                onClickImage = {},
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    Box(
                        contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 960.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            EditImagesButton(
                                images = state.artistFormState.images,
                                onClickEdit = { onClickEditImages(state.artistFormState.images.toList()) },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            ImagePager(
                                images = state.artistFormState.images,
                                pagerState = imagePagerState,
                                sharedElementId = state.artistFormState.editorState.id.value.text.toString(),
                                onClickPage = {
                                    // TODO: Open images screen
                                },
                            )

                            form(Modifier.fillMaxWidth())
                        }
                    }
                }
            }

            if (showFormLinkDialog) {
                val formLink by state.formLink.collectAsStateWithLifecycle()
                FormLinkDialog(
                    formLink = { formLink },
                    onDismiss = { showFormLinkDialog = false },
                    onClickGenerate = generateFormLink,
                    onClickDebugForm = onClickDebugForm,
                )
            } else {
                val errorMessage =
                    stringResource(Res.string.alley_edit_artist_error_saving_bad_fields)
                GenericExitDialog(
                    onClickBack = { onClickBack(true) },
                    onClickSave = onClickDone,
                    saveErrorMessage = { errorMessage.takeIf { errorState.hasAnyError } },
                )
            }
        }
    }

    @Composable
    private fun AppBarActions(
        errorState: ArtistErrorState,
        saveTaskState: TaskState<ArtistSave.Response>,
        onClickGenerateFormLink: () -> Unit,
        onClickHistory: () -> Unit,
        onClickSave: () -> Unit,
        onClickDone: () -> Unit,
    ) {
        TooltipIconButton(
            icon = Icons.Default.AddLink,
            tooltipText = stringResource(Res.string.alley_edit_artist_action_generate_link_content_description),
            onClick = onClickGenerateFormLink,
        )
        TooltipIconButton(
            icon = Icons.Default.History,
            tooltipText = stringResource(Res.string.alley_edit_artist_action_history_content_description),
            onClick = onClickHistory,
        )

        val enabled = !errorState.hasAnyError
        ArtistSaveButton(enabled, saveTaskState, onClickSave)
        TooltipIconButton(
            icon = Icons.Default.DoneAll,
            tooltipText = stringResource(Res.string.alley_edit_artist_action_save_and_exit_tooltip),
            enabled = enabled,
            onClick = onClickDone,
        )
    }

    @Composable
    private fun PendingFormSubmissionPrompt(onClickMerge: () -> Unit) {
        OutlinedCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color.Yellow,
                )
                Text(
                    text = stringResource(Res.string.alley_edit_artist_edit_pending_form_warning),
                    modifier = Modifier.weight(1f)
                )
                FilledTonalButton(onClick = onClickMerge) {
                    Text(stringResource(Res.string.alley_edit_artist_edit_pending_form_warning_action_open))
                }
            }
        }
    }

    @Composable
    private fun FormLinkDialog(
        formLink: () -> String?,
        onDismiss: () -> Unit,
        onClickGenerate: () -> Unit,
        onClickDebugForm: (formLink: String) -> Unit,
    ) {
        NavigationBackHandler(
            state = rememberNavigationEventState(NavigationEventInfo.None),
            onBackCompleted = onDismiss,
        )

        @Suppress("SimplifyBooleanWithConstants")
        val isDebug = PlatformSpecificConfig.type == PlatformType.DESKTOP || BuildKonfig.isWasmDebug
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(Res.string.alley_edit_artist_edit_form_link_title)) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // TODO: Surface errors
                    val formLink = formLink()
                    if (formLink == null) {
                        Text(stringResource(Res.string.alley_edit_artist_edit_form_link_description))
                    } else {
                        Text(stringResource(Res.string.alley_edit_artist_edit_form_link_description_generated))
                        OutlinedTextField(value = formLink, onValueChange = {}, readOnly = true)

                        if (isDebug) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                val key =
                                    formLink.substringAfter("?${AlleyCryptography.ACCESS_KEY_PARAM}=")
                                FilledTonalButton(onClick = {
                                    ArtistFormAccessKey.setKey(key)
                                    onClickDebugForm(formLink)
                                    onDismiss()
                                }) {
                                    Text("Open form")
                                }
                                FilledTonalButton(onClick = {
                                    ArtistFormAccessKey.setKey(key.dropLast(10))
                                    onClickDebugForm(formLink)
                                    onDismiss()
                                }) {
                                    Text("Open broken form")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                val countdown by produceState(5) {
                    (4 downTo 0).forEach {
                        delay(1.seconds)
                        value = it
                    }
                }
                TextButton(
                    onClick = {
                        if (countdown <= 0 || isDebug) {
                            onClickGenerate()
                        }
                    },
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val textAlpha by animateFloatAsState(if (countdown <= 0) 1f else 0f)
                        Text(
                            text = stringResource(Res.string.alley_edit_artist_edit_form_link_action_generate),
                            modifier = Modifier.graphicsLayer { alpha = textAlpha }
                        )
                        androidx.compose.animation.AnimatedVisibility(
                            visible = countdown > 0,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            Text(countdown.toString())
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.alley_edit_artist_edit_form_link_action_cancel))
                }
            }
        )
    }

    @Stable
    class State(
        val initialArtist: StateFlow<ArtistDatabaseEntry.Impl?>,
        val artistFormState: ArtistFormState,
        val hasPendingFormSubmission: StateFlow<Boolean>,
        val formLink: StateFlow<String?>,
        val saveTaskState: TaskState<ArtistSave.Response>,
    )
}
