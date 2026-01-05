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
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularWavyProgressIndicator
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_generate_link_tooltip
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_request_merge_tooltip
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_save_and_exit_tooltip
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_form_link_action_cancel
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_form_link_action_copy
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_form_link_action_generate
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_form_link_description
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_form_link_description_exists
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_form_link_description_generated
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_form_link_title
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_outdated
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_pending_form_abandon_description
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_pending_form_abandon_title
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_pending_form_action_abandon
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_pending_form_action_cancel
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_pending_form_warning
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_pending_form_warning_action_open
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_title_editing_booth_name
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_title_editing_name
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_error_saving_bad_fields
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_abandon_description
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_abandon_title
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_action_abandon
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_action_cancel
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_action_view_tooltip
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_refresh_abandon_description
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_refresh_abandon_title
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_refresh_action_abandon
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_refresh_action_cancel
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_refresh_action_view_tooltip
import com.thekeeperofpie.artistalleydatabase.alley.PlatformSpecificConfig
import com.thekeeperofpie.artistalleydatabase.alley.PlatformType
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference.ArtistInferenceField
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference.ArtistPreviousYearData
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference.MergeArtistPrompt
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference.SameArtistPrompt
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference.SameArtistPrompter
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
import com.thekeeperofpie.artistalleydatabase.utils.JobProgress
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
import org.jetbrains.compose.resources.getString
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
            hasPendingChanges = viewModel::hasPendingChanges,
            onClickBack = onClickBack,
            onClickEditImages = {
                onClickEditImages(
                    viewModel.state.artistFormState.info.name.value.text.toString(),
                    it
                )
            },
            onClickRefresh = { viewModel.initialize(force = true) },
            onClickHistory = onClickHistory,
            onClickMerge = onClickMerge,
            onClickSave = viewModel::onClickSave,
            onClickDone = viewModel::onClickDone,
            onClearFormLink = viewModel::onClearFormLink,
            onClickDebugForm = onClickDebugForm,
            onClickSameArtist = viewModel.sameArtistPrompter::onClickSameArtist,
            onDenySameArtist = viewModel.sameArtistPrompter::onDenySameArtist,
            onConfirmSameArtist = viewModel.sameArtistPrompter::onConfirmSameArtist,
            onConfirmMerge = viewModel::onConfirmMerge,
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
        generateFormLink: (forceRegenerate: Boolean) -> Unit,
        hasPendingChanges: () -> Boolean,
        onClickBack: (force: Boolean) -> Unit,
        onClickEditImages: (List<EditImage>) -> Unit,
        onClickRefresh: () -> Unit,
        onClickHistory: () -> Unit,
        onClickMerge: () -> Unit,
        onClickSave: () -> Unit,
        onClickDone: () -> Unit,
        onClearFormLink: () -> Unit,
        onClickDebugForm: (formLink: String) -> Unit,
        onClickSameArtist: (artistId: Uuid) -> Unit,
        onDenySameArtist: () -> Unit,
        onConfirmSameArtist: () -> Unit,
        onConfirmMerge: (Map<ArtistInferenceField, Boolean>) -> Unit,
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
                            snackbarHostState.showSnackbar(message = getString(Res.string.alley_edit_artist_edit_outdated))
                            saveTaskState.clearResult()
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
        var requestedMergingArtist by rememberSaveable { mutableStateOf(false) }
        val previousYearData by state.previousYearData.collectAsStateWithLifecycle()
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val formState = state.artistFormState
                        val name = formState.info.name.value.text.ifBlank {
                            formState.editorState.id.value.text.toString()
                        }
                        val conventionName = stringResource(dataYear.shortName)
                        val booth = formState.info.booth.value.text.toString()
                        val text = if (booth.isNotEmpty()) {
                            stringResource(
                                Res.string.alley_edit_artist_edit_title_editing_booth_name,
                                conventionName,
                                booth,
                                name,
                            )
                        } else {
                            stringResource(
                                Res.string.alley_edit_artist_edit_title_editing_name,
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
                            hasPreviousYearData = { previousYearData != null },
                            onClickRefresh = onClickRefresh,
                            onClickGenerateFormLink = { showFormLinkDialog = true },
                            onClickHistory = onClickHistory,
                            onClickSave = onClickSave,
                            onClickDone = onClickDone,
                            onClickRequestMerge = {
                                requestedMergingArtist = !requestedMergingArtist
                            },
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
                val initialArtist by state.initialArtist.collectAsStateWithLifecycle()
                val artistProgress by state.artistProgress.collectAsStateWithLifecycle()
                val sameArtist by state.sameArtistState.sameArtist.collectAsStateWithLifecycle()
                val form = remember {
                    movableContentOf { modifier: Modifier ->
                        Column(modifier = modifier) {
                            val formMetadata by state.formMetadata.collectAsStateWithLifecycle()
                            if (formMetadata?.hasPendingFormSubmission == true) {
                                PendingFormSubmissionPrompt(hasPendingChanges, onClickMerge)
                            }
                            if (initialArtist == null || artistProgress is JobProgress.Loading) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize().padding(32.dp)
                                ) {
                                    CircularWavyProgressIndicator()
                                }
                            } else {
                                PotentialSameArtists(
                                    inferredArtists = state.sameArtistState.inferredArtists,
                                    onClickSameArtist = onClickSameArtist,
                                )
                                ArtistForm(
                                    initialArtist = { initialArtist },
                                    state = state.artistFormState,
                                    errorState = errorState,
                                    seriesById = seriesById,
                                    seriesPredictions = seriesPredictions,
                                    merchById = merchById,
                                    merchPredictions = merchPredictions,
                                    seriesImage = seriesImage,
                                    forceLocked = !sameArtist.isEmpty(),
                                )
                            }
                        }
                    }
                }

                val sameArtistPrompt = remember {
                    movableContentOf {
                        SameArtistPrompt(
                            sameArtist = sameArtist,
                            onDenySameArtist = onDenySameArtist,
                            onConfirmSameArtist = onConfirmSameArtist,
                        )
                    }
                }

                val showMergingArtist by remember { derivedStateOf { requestedMergingArtist && previousYearData != null } }
                val mergeList = remember {
                    movableContentOf {
                        previousYearData?.let {
                            MergeArtistPrompt(
                                previousYearData = it,
                                onConfirmMerge = {
                                    onConfirmMerge(it)
                                    requestedMergingArtist = false
                                },
                            )
                        }
                    }
                }

                val forceExpandedIfPossible = !sameArtist.isEmpty() || showMergingArtist
                if (isExpanded &&
                    (state.artistFormState.images.isNotEmpty() || forceExpandedIfPossible)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        form(
                            Modifier.fillMaxHeight()
                                .width(800.dp)
                                .verticalScroll(rememberScrollState())
                        )
                        if (!sameArtist.isEmpty()) {
                            sameArtistPrompt()
                        } else if (showMergingArtist) {
                            mergeList()
                        } else {
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
                            if (!sameArtist.isEmpty()) {
                                sameArtistPrompt()
                            } else if (showMergingArtist) {
                                mergeList()
                            } else {
                                ImagePager(
                                    images = state.artistFormState.images,
                                    pagerState = imagePagerState,
                                    sharedElementId = state.artistFormState.editorState.id.value.text.toString(),
                                    onClickPage = {
                                        // TODO: Open images screen
                                    },
                                )
                            }

                            form(Modifier.fillMaxWidth())

                            if (initialArtist != null && artistProgress !is JobProgress.Loading) {
                                EditImagesButton(
                                    images = state.artistFormState.images,
                                    onClickEdit = { onClickEditImages(state.artistFormState.images.toList()) },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }
                }
            }

            if (showFormLinkDialog) {
                val formMetadata by state.formMetadata.collectAsStateWithLifecycle()
                val formLink by state.formLink.collectAsStateWithLifecycle()
                FormLinkDialog(
                    formMetadata = formMetadata,
                    formLink = { formLink },
                    onDismiss = {
                        if (formLink != null) {
                            onClearFormLink()
                        }
                        showFormLinkDialog = false
                    },
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
        hasPendingChanges: () -> Boolean,
        hasPreviousYearData: () -> Boolean,
        onClickRefresh: () -> Unit,
        onClickGenerateFormLink: () -> Unit,
        onClickHistory: () -> Unit,
        onClickSave: () -> Unit,
        onClickDone: () -> Unit,
        onClickRequestMerge: () -> Unit,
    ) {
        if (hasPreviousYearData()) {
            TooltipIconButton(
                icon = Icons.Default.Merge,
                tooltipText = stringResource(Res.string.alley_edit_artist_action_request_merge_tooltip),
                onClick = onClickRequestMerge,
            )
        }

        RefreshButton(hasPendingChanges, onClickRefresh)

        TooltipIconButton(
            icon = Icons.Default.AddLink,
            tooltipText = stringResource(Res.string.alley_edit_artist_action_generate_link_tooltip),
            onClick = onClickGenerateFormLink,
        )

        HistoryButton(hasPendingChanges, onClickHistory)

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
    private fun RefreshButton(hasPendingChanges: () -> Boolean, onClickRefresh: () -> Unit) {
        var showDialog by remember { mutableStateOf(false) }
        TooltipIconButton(
            icon = Icons.Default.Refresh,
            tooltipText = stringResource(Res.string.alley_edit_artist_refresh_action_view_tooltip),
            onClick = {
                if (hasPendingChanges()) {
                    showDialog = true
                } else {
                    onClickRefresh()
                }
            },
        )

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(stringResource(Res.string.alley_edit_artist_refresh_abandon_title)) },
                text = { Text(stringResource(Res.string.alley_edit_artist_refresh_abandon_description)) },
                confirmButton = {
                    TextButton(onClick = {
                        onClickRefresh()
                        showDialog = false
                    }) {
                        Text(stringResource(Res.string.alley_edit_artist_refresh_action_abandon))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(stringResource(Res.string.alley_edit_artist_refresh_action_cancel))
                    }
                }
            )
        }
    }

    @Composable
    private fun HistoryButton(hasPendingChanges: () -> Boolean, onClickHistory: () -> Unit) {
        var showDialog by remember { mutableStateOf(false) }
        TooltipIconButton(
            icon = Icons.Default.History,
            tooltipText = stringResource(Res.string.alley_edit_artist_history_action_view_tooltip),
            onClick = {
                if (hasPendingChanges()) {
                    showDialog = true
                } else {
                    onClickHistory()
                }
            },
        )

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(stringResource(Res.string.alley_edit_artist_history_abandon_title)) },
                text = { Text(stringResource(Res.string.alley_edit_artist_history_abandon_description)) },
                confirmButton = {
                    TextButton(onClick = {
                        onClickHistory()
                        showDialog = false
                    }) {
                        Text(stringResource(Res.string.alley_edit_artist_history_action_abandon))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(stringResource(Res.string.alley_edit_artist_history_action_cancel))
                    }
                }
            )
        }
    }

    @Composable
    private fun PendingFormSubmissionPrompt(
        hasPendingChanges: () -> Boolean,
        onClickMerge: () -> Unit,
    ) {
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

                var showDialog by remember { mutableStateOf(false) }
                FilledTonalButton(onClick = {
                    if (hasPendingChanges()) {
                        showDialog = true
                    } else {
                        onClickMerge()
                    }
                }) {
                    Text(stringResource(Res.string.alley_edit_artist_edit_pending_form_warning_action_open))
                }

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text(stringResource(Res.string.alley_edit_artist_edit_pending_form_abandon_title)) },
                        text = { Text(stringResource(Res.string.alley_edit_artist_edit_pending_form_abandon_description)) },
                        confirmButton = {
                            TextButton(onClick = {
                                onClickMerge()
                                showDialog = false
                            }) {
                                Text(stringResource(Res.string.alley_edit_artist_edit_pending_form_action_abandon))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text(stringResource(Res.string.alley_edit_artist_edit_pending_form_action_cancel))
                            }
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun FormLinkDialog(
        formMetadata: State.FormMetadata?,
        formLink: () -> String?,
        onDismiss: () -> Unit,
        onClickGenerate: (forceRegenerate: Boolean) -> Unit,
        onClickDebugForm: (formLink: String) -> Unit,
    ) {
        NavigationBackHandler(
            state = rememberNavigationEventState(NavigationEventInfo.None),
            onBackCompleted = onDismiss,
        )

        var loading by remember(formLink()) { mutableStateOf(false) }

        @Suppress("SimplifyBooleanWithConstants")
        val isDebug = PlatformSpecificConfig.type == PlatformType.DESKTOP || BuildKonfig.isWasmDebug
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = if (formMetadata?.hasFormLink == true) {
                {
                    Icon(imageVector = Icons.Default.Warning, null)
                }
            } else null,
            title = { Text(stringResource(Res.string.alley_edit_artist_edit_form_link_title)) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // TODO: Surface errors
                    val formLink = formLink()
                    if (formLink == null) {
                        if (formMetadata?.hasFormLink == true) {
                            Text(stringResource(Res.string.alley_edit_artist_edit_form_link_description_exists))
                        } else {
                            Text(stringResource(Res.string.alley_edit_artist_edit_form_link_description))
                        }
                    } else {
                        Text(stringResource(Res.string.alley_edit_artist_edit_form_link_description_generated))
                        OutlinedTextField(value = formLink, onValueChange = {}, readOnly = true)

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            val clipboardManager = LocalClipboardManager.current
                            FilledTonalButton(onClick = {
                                try {
                                    clipboardManager.setText(AnnotatedString(formLink))
                                } catch (_: Throwable) {
                                }
                            }) {
                                Text(stringResource(Res.string.alley_edit_artist_edit_form_link_action_copy))
                            }
                            if (isDebug) {
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
                if (formLink() == null) {
                    TextButton(
                        onClick = {
                            if (!loading && (countdown <= 0 || isDebug)) {
                                val forceRegenerate = formMetadata?.hasFormLink == true
                                loading = true
                                onClickGenerate(forceRegenerate)
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
                            androidx.compose.animation.AnimatedVisibility(
                                visible = loading,
                                enter = fadeIn(),
                                exit = fadeOut(),
                            ) {
                                CircularWavyProgressIndicator()
                            }
                        }
                    }
                } else {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(Res.string.alley_edit_artist_edit_form_link_action_cancel))
                    }
                }
            },
            dismissButton = if (formLink() == null) {
                {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(Res.string.alley_edit_artist_edit_form_link_action_cancel))
                    }
                }
            } else null,
            properties = DialogProperties(dismissOnClickOutside = false),
        )
    }

    @Stable
    class State(
        val artistProgress: StateFlow<JobProgress<Unit>>,
        val initialArtist: StateFlow<ArtistDatabaseEntry.Impl?>,
        val previousYearData: StateFlow<ArtistPreviousYearData?>,
        val artistFormState: ArtistFormState,
        val formMetadata: StateFlow<FormMetadata?>,
        val formLink: StateFlow<String?>,
        val saveTaskState: TaskState<ArtistSave.Response>,
        val sameArtistState: SameArtistPrompter.State,
    ) {
        data class FormMetadata(
            val hasPendingFormSubmission: Boolean,
            val hasFormLink: Boolean,
        )
    }
}
