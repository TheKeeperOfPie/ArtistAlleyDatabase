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
import androidx.compose.material3.Checkbox
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
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_save_and_exit_tooltip
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_add_action_confirm_merge
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_add_potential_duplicates
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_title_adding
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_error_saving_bad_fields
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_catalog_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_merch_confirmed
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_merch_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_name
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_series_confirmed
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_series_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_store_links
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
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
        ArtistAddScreen(
            dataYear = dataYear,
            state = viewModel.state,
            seriesPredictions = viewModel::seriesPredictions,
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
            onConfirmMerge = viewModel::onConfirmMerge,
        )
    }

    @Composable
    internal operator fun invoke(
        dataYear: DataYear,
        state: State,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        onClickBack: (force: Boolean) -> Unit,
        onClickEditImages: (List<EditImage>) -> Unit,
        onClickSave: () -> Unit,
        onClickDone: () -> Unit,
        onClickMerge: (artistId: Uuid) -> Unit,
        onConfirmMerge: (Map<ArtistField, Boolean>) -> Unit,
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
                            seriesPredictions = seriesPredictions,
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
                        val fieldState = rememberFieldState()
                        MergeList(
                            mergingArtist = mergingArtist,
                            fieldState = fieldState,
                            onConfirmMerge = onConfirmMerge,
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
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        locked: Boolean,
        onClickMerge: (artistId: Uuid) -> Unit,
        modifier: Modifier,
    ) {
        val initialArtist by state.initialArtist.collectAsStateWithLifecycle()
        Column(modifier = modifier) {
            PotentialDuplicates(state.inferredArtists, onClickMerge)

            ArtistForm(
                initialArtist = { initialArtist },
                state = state.artistFormState,
                errorState = errorState,
                seriesPredictions = seriesPredictions,
                merchPredictions = merchPredictions,
                seriesImage = seriesImage,
                forceLocked = locked,
            )
        }
    }

    @Composable
    private fun MergeList(
        mergingArtist: LoadingResult<ArtistEntry>,
        fieldState: FieldState,
        onConfirmMerge: (Map<ArtistField, Boolean>) -> Unit,
    ) {
        // TODO: There are 3 instances of a similar UI (history, form merge, add artist merge),
        //  can these share more code?
        Column {
            val artist = mergingArtist.result

            val groupState = when {
                fieldState.map.values.all { it } -> ToggleableState.On
                fieldState.map.values.any { it } -> ToggleableState.Indeterminate
                else -> ToggleableState.Off
            }

            TriStateCheckbox(
                state = groupState,
                onClick = {
                    val newValue = when (groupState) {
                        ToggleableState.On -> false
                        ToggleableState.Off,
                        ToggleableState.Indeterminate,
                            -> true
                    }
                    fieldState.map.keys.toSet().forEach {
                        fieldState[it] = newValue
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            ArtistField.entries.forEach { field ->
                val fieldText = when (field) {
                    ArtistField.NAME -> artist?.name?.ifBlank { null }
                    ArtistField.LINKS -> artist?.links?.ifEmpty { null }?.joinToString("\n")
                    ArtistField.STORE_LINKS -> artist?.storeLinks?.ifEmpty { null }
                        ?.joinToString("\n")
                    ArtistField.CATALOG_LINKS -> artist?.catalogLinks?.ifEmpty { null }
                        ?.joinToString("\n")
                    ArtistField.SERIES_INFERRED -> artist?.seriesInferred?.ifEmpty { null }
                        ?.joinToString()
                    ArtistField.SERIES_CONFIRMED -> artist?.seriesConfirmed?.ifEmpty { null }
                        ?.joinToString()
                    ArtistField.MERCH_INFERRED -> artist?.merchInferred?.ifEmpty { null }
                        ?.joinToString()
                    ArtistField.MERCH_CONFIRMED -> artist?.merchConfirmed?.ifEmpty { null }
                        ?.joinToString()
                }
                if (fieldText != null) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Checkbox(
                                checked = fieldState[field],
                                onCheckedChange = { fieldState[field] = it },
                            )
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

            FilledTonalButton(
                onClick = { onConfirmMerge(fieldState.map.toMap()) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            ) {
                Text(stringResource(Res.string.alley_edit_artist_add_action_confirm_merge))
            }
        }
    }

    @Composable
    fun PotentialDuplicates(
        inferredArtists: StateFlow<List<ArtistInference.MatchResult>>,
        onClickMerge: (artistId: Uuid) -> Unit,
    ) {
        val inferredArtists by inferredArtists.collectAsStateWithLifecycle()
        if (inferredArtists.isNotEmpty()) {
            OutlinedCard(Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)) {
                Text(
                    text = stringResource(Res.string.alley_edit_artist_add_potential_duplicates),
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

    @Stable
    private class FieldState(val map: SnapshotStateMap<ArtistField, Boolean>) {
        operator fun get(field: ArtistField) = map[field] ?: false
        operator fun set(field: ArtistField, checked: Boolean) = map.set(field, checked)
    }

    @Composable
    private fun rememberFieldState(): FieldState {
        val map = rememberSaveable {
            mutableStateMapOf<ArtistField, Boolean>().apply {
                ArtistField.entries.forEach {
                    this[it] = false
                }
            }
        }
        return remember(map) { FieldState(map) }
    }

    internal enum class ArtistField(val label: StringResource) {
        NAME(Res.string.alley_edit_artist_field_label_name),
        LINKS(Res.string.alley_edit_artist_field_label_links),
        STORE_LINKS(Res.string.alley_edit_artist_field_label_store_links),
        CATALOG_LINKS(Res.string.alley_edit_artist_field_label_catalog_links),
        SERIES_INFERRED(Res.string.alley_edit_artist_field_label_series_inferred),
        SERIES_CONFIRMED(Res.string.alley_edit_artist_field_label_series_confirmed),
        MERCH_INFERRED(Res.string.alley_edit_artist_field_label_merch_inferred),
        MERCH_CONFIRMED(Res.string.alley_edit_artist_field_label_merch_confirmed),
    }

    @Stable
    class State(
        val initialArtist: StateFlow<ArtistDatabaseEntry.Impl?>,
        val artistFormState: ArtistFormState,
        val inferredArtists: StateFlow<List<ArtistInference.MatchResult>>,
        val mergingArtist: StateFlow<LoadingResult<ArtistEntry>>,
        val saveTaskState: TaskState<ArtistSave.Response>,
    )
}
