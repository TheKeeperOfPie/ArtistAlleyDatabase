package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_save_and_exit_tooltip
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_add_action_same_artist_confirm
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_add_action_same_artist_deny
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_add_potential_same_artists
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_add_same_artist_confirm_prompt
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_title_adding
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_error_saving_bad_fields
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_merch_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_name
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_series_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_store_links
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.ImagesEditScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ContentSavingBox
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.GenericExitDialog
import com.thekeeperofpie.artistalleydatabase.alley.images.ImageGrid
import com.thekeeperofpie.artistalleydatabase.alley.images.ImagePager
import com.thekeeperofpie.artistalleydatabase.alley.images.rememberImagePagerState
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.ArtistSave
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.alley.ui.currentWindowSizeClass
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.GenericTaskErrorEffect
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.TaskState
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationResultEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

object ArtistAddScreen {

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        artistId: Uuid,
        graph: ArtistAlleyEditGraph,
        onClickBack: (force: Boolean) -> Unit,
        onClickEditImages: (displayName: String, List<EditImage>) -> Unit,
        viewModel: ArtistAddViewModel = viewModel {
            graph.artistAddViewModelFactory.create(
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
        val seriesById by viewModel.tagAutocomplete.seriesById.collectAsStateWithLifecycle(emptyMap())
        val merchById by viewModel.tagAutocomplete.merchById.collectAsStateWithLifecycle(emptyMap())
        ArtistAddScreen(
            dataYear = dataYear,
            state = viewModel.state,
            seriesById = { seriesById },
            seriesPredictions = viewModel::seriesPredictions,
            merchById = { merchById },
            merchPredictions = viewModel::merchPredictions,
            seriesImage = viewModel::seriesImage,
            onClickBack = onClickBack,
            onClickEditImages = {
                onClickEditImages(
                    viewModel.state.artistFormState.info.name.value.text.toString(),
                    it
                )
            },
            onClickSave = viewModel::onClickSave,
            onClickDone = viewModel::onClickDone,
            onClickMerge = viewModel::onClickMerge,
            onDenySameArtist = viewModel::onDenySameArtist,
            onConfirmSameArtist = viewModel::onConfirmSameArtist,
        )
    }

    @Composable
    internal operator fun invoke(
        dataYear: DataYear,
        state: State,
        seriesById: () -> Map<String, SeriesInfo>,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        merchById: () -> Map<String, MerchInfo>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        onClickBack: (force: Boolean) -> Unit,
        onClickEditImages: (List<EditImage>) -> Unit,
        onClickSave: () -> Unit,
        onClickDone: () -> Unit,
        onClickMerge: (artistId: Uuid) -> Unit,
        onDenySameArtist: () -> Unit,
        onConfirmSameArtist: () -> Unit,
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
                        AppBarActions(
                            errorState = errorState,
                            saveTaskState = saveTaskState,
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
                val mergingArtist by state.mergingArtist.collectAsStateWithLifecycle()
                val form = remember {
                    movableContentOf { modifier: Modifier ->
                        Form(
                            state = state,
                            errorState = errorState,
                            seriesById = seriesById,
                            seriesPredictions = seriesPredictions,
                            merchById = merchById,
                            merchPredictions = merchPredictions,
                            seriesImage = seriesImage,
                            locked = !mergingArtist.isEmpty(),
                            onClickMerge = onClickMerge,
                            modifier = modifier,
                        )
                    }
                }
                val mergeList = remember {
                    movableContentOf {
                        MergeList(
                            mergingArtist = mergingArtist,
                            onDenySameArtist = onDenySameArtist,
                            onConfirmSameArtist = onConfirmSameArtist,
                        )
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
                        if (mergingArtist.isEmpty()) {
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
                        } else {
                            mergeList()
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
                            if (mergingArtist.isEmpty()) {
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
                            } else {
                                mergeList()
                            }

                            form(Modifier.fillMaxWidth())
                        }
                    }
                }
            }

            val errorMessage =
                stringResource(Res.string.alley_edit_artist_error_saving_bad_fields)
            GenericExitDialog(
                onClickBack = { onClickBack(true) },
                onClickSave = onClickDone,
                saveErrorMessage = { errorMessage.takeIf { errorState.hasAnyError } },
            )
        }
    }

    @Composable
    private fun AppBarActions(
        errorState: ArtistErrorState,
        saveTaskState: TaskState<ArtistSave.Response>,
        onClickSave: () -> Unit,
        onClickDone: () -> Unit,
    ) {
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
    private fun Form(
        state: State,
        errorState: ArtistErrorState,
        seriesById: () -> Map<String, SeriesInfo>,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        merchById: () -> Map<String, MerchInfo>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        locked: Boolean,
        onClickMerge: (artistId: Uuid) -> Unit,
        modifier: Modifier,
    ) {
        val initialArtist by state.initialArtist.collectAsStateWithLifecycle()
        Column(modifier = modifier) {
            PotentialSameArtists(state.inferredArtists, onClickMerge)

            ArtistForm(
                initialArtist = { initialArtist },
                state = state.artistFormState,
                seriesById = seriesById,
                errorState = errorState,
                seriesPredictions = seriesPredictions,
                merchById = merchById,
                merchPredictions = merchPredictions,
                seriesImage = seriesImage,
                forceLocked = locked,
            )
        }
    }

    @Composable
    private fun MergeList(
        mergingArtist: LoadingResult<ArtistInference.PreviousYearData>,
        onDenySameArtist: () -> Unit,
        onConfirmSameArtist: () -> Unit,
    ) {
        Column {
            val artist = mergingArtist.result
            ArtistField.entries.forEach { field ->
                val fieldText = when (field) {
                    ArtistField.NAME -> artist?.name?.ifBlank { null }
                    ArtistField.LINKS -> artist?.links?.ifEmpty { null }?.joinToString("\n")
                    ArtistField.STORE_LINKS -> artist?.storeLinks?.ifEmpty { null }
                        ?.joinToString("\n")
                    ArtistField.SERIES -> artist?.seriesInferred?.ifEmpty { null }
                        ?.joinToString()
                    ArtistField.MERCH -> artist?.merchInferred?.ifEmpty { null }
                        ?.joinToString()
                }
                if (fieldText != null) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(stringResource(field.label))
                            if (fieldText.length < 40) {
                                Text(text = fieldText)
                            }
                        }
                        if (fieldText.length >= 40) {
                            Text(text = fieldText, modifier = Modifier.padding(start = 80.dp))
                        }
                    }
                }
            }

            OutlinedCard(modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)) {
                Text(
                    text = stringResource(Res.string.alley_edit_artist_add_same_artist_confirm_prompt),
                    modifier = Modifier.padding(16.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
                ) {
                    FilledTonalButton(onClick = { onDenySameArtist() }) {
                        Text(stringResource(Res.string.alley_edit_artist_add_action_same_artist_deny))
                    }
                    FilledTonalButton(onClick = { onConfirmSameArtist() }) {
                        Text(stringResource(Res.string.alley_edit_artist_add_action_same_artist_confirm))
                    }
                }
            }
        }
    }

    @Composable
    fun PotentialSameArtists(
        inferredArtists: StateFlow<List<ArtistInference.MatchResult>>,
        onClickMerge: (artistId: Uuid) -> Unit,
    ) {
        val inferredArtists by inferredArtists.collectAsStateWithLifecycle()
        if (inferredArtists.isNotEmpty()) {
            OutlinedCard(Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)) {
                Text(
                    text = stringResource(Res.string.alley_edit_artist_add_potential_same_artists),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
                ) {
                    inferredArtists.forEach {
                        val via = it.via
                        if (via != null) {
                            TooltipBox(
                                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                    positioning = TooltipAnchorPosition.Below,
                                    spacingBetweenTooltipAndAnchor = 0.dp,
                                ),
                                tooltip = { PlainTooltip { Text(via) } },
                                state = rememberTooltipState(),
                            ) {
                                AssistChip(
                                    onClick = { onClickMerge(it.data.id) },
                                    label = { Text(it.name) },
                                )
                            }
                        } else {
                            AssistChip(
                                onClick = { onClickMerge(it.data.id) },
                                label = { Text(it.name) },
                            )
                        }
                    }
                }
            }
        }
    }

    private enum class ArtistField(val label: StringResource) {
        NAME(Res.string.alley_edit_artist_field_label_name),
        LINKS(Res.string.alley_edit_artist_field_label_links),
        STORE_LINKS(Res.string.alley_edit_artist_field_label_store_links),
        SERIES(Res.string.alley_edit_artist_field_label_series_inferred),
        MERCH(Res.string.alley_edit_artist_field_label_merch_inferred),
    }

    @Stable
    class State(
        val initialArtist: StateFlow<ArtistDatabaseEntry.Impl?>,
        val artistFormState: ArtistFormState,
        val inferredArtists: StateFlow<List<ArtistInference.MatchResult>>,
        val mergingArtist: StateFlow<LoadingResult<ArtistInference.PreviousYearData>>,
        val saveTaskState: TaskState<ArtistSave.Response>,
    )
}
