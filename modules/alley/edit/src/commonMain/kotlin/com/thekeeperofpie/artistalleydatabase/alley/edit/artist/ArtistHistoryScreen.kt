package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_booth
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_catalog_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_commissions
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_editor_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_merch_confirmed
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_merch_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_name
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_portfolio_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_series_confirmed
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_series_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_social_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_status
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_store_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_summary
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_action_apply_changes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_action_refresh_tooltip
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_action_return_to_history_tooltip
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_action_revert_tooltip
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_select_all
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_title
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.edit.form.FormMergeBehavior
import com.thekeeperofpie.artistalleydatabase.alley.edit.history.HistoryCardHeader
import com.thekeeperofpie.artistalleydatabase.alley.edit.history.HistoryEntryCard
import com.thekeeperofpie.artistalleydatabase.alley.edit.history.HistoryListChangeRow
import com.thekeeperofpie.artistalleydatabase.alley.edit.history.HistorySingleChangeRow
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ContentSavingBox
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistHistoryEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.JobProgress
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationResults
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationResults
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.uuid.Uuid

object ArtistHistoryScreen {

    val RESULT_KEY = NavigationResults.Key<Unit>("ArtistHistoryScreen")

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        artistId: Uuid,
        graph: ArtistAlleyEditGraph,
        onClickBack: (force: Boolean) -> Unit,
        viewModel: ArtistHistoryViewModel = viewModel {
            graph.artistHistoryViewModelFactory.create(dataYear, artistId)
        },
    ) {
        val history by viewModel.history.collectAsStateWithLifecycle()
        val seriesById by viewModel.tagAutocomplete.seriesById.collectAsStateWithLifecycle()
        val merchById by viewModel.tagAutocomplete.merchById.collectAsStateWithLifecycle()
        val initialArtist by viewModel.initial.collectAsStateWithLifecycle()
        ArtistHistoryScreen(
            history = { history },
            dataYear = dataYear,
            artistId = artistId,
            initialArtist = { initialArtist },
            seriesById = { seriesById },
            merchById = { merchById },
            saveProgress = viewModel.saveProgress,
            seriesImage = viewModel::seriesImage,
            onClickBack = onClickBack,
            onClickRefresh = viewModel::onClickRefresh,
            onApplied = viewModel::onApplied,
        )
    }

    @Composable
    operator fun invoke(
        history: () -> List<ArtistHistoryEntryWithDiff>,
        dataYear: DataYear,
        artistId: Uuid,
        initialArtist: () -> ArtistDatabaseEntry.Impl?,
        seriesById: () -> Map<String, SeriesInfo>,
        merchById: () -> Map<String, MerchInfo>,
        saveProgress: MutableStateFlow<JobProgress<BackendRequest.ArtistSave.Response>>,
        seriesImage: (SeriesInfo) -> String?,
        onClickBack: (force: Boolean) -> Unit,
        onClickRefresh: () -> Unit,
        onApplied: (ArtistHistoryEntry) -> Unit,
    ) {
        val snackbarHostState = remember { SnackbarHostState() }
        val navigationResults = LocalNavigationResults.current
        LaunchedEffect(navigationResults) {
            saveProgress.collectLatest {
                if (it is JobProgress.Finished.Result<BackendRequest.ArtistSave.Response>) {
                    when (val result = it.value) {
                        is BackendRequest.ArtistSave.Response.Failed ->
                            snackbarHostState.showSnackbar(message = result.errorMessage)
                        is BackendRequest.ArtistSave.Response.Outdated -> {
                            // TODO
                        }
                        is BackendRequest.ArtistSave.Response.Success -> {
                            saveProgress.value = JobProgress.Idle()
                            navigationResults[RESULT_KEY] = Unit
                            onClickBack(true)
                        }
                    }
                }
            }
        }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val history = history()
                        val name = remember(history) {
                            history.firstNotNullOfOrNull { it.entry.name }.orEmpty()
                        }
                        Text(
                            stringResource(
                                Res.string.alley_edit_artist_history_title,
                                stringResource(dataYear.shortName),
                                name,
                            )
                        )
                    },
                    navigationIcon = { ArrowBackIconButton(onClick = { onClickBack(true) }) },
                    actions = {
                        TooltipIconButton(
                            icon = Icons.Default.Refresh,
                            tooltipText = stringResource(Res.string.alley_edit_artist_history_action_refresh_tooltip),
                            onClick = onClickRefresh,
                        )
                    },
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = Modifier.fillMaxWidth()
        ) { scaffoldPadding ->
            val saveProgress by saveProgress.collectAsStateWithLifecycle()
            ContentSavingBox(
                saving = saveProgress is JobProgress.Loading<*>,
                modifier = Modifier.fillMaxWidth().padding(scaffoldPadding)
            ) {
                Row(modifier = Modifier.widthIn(max = 1200.dp).align(Alignment.TopCenter)) {
                    var selectedIndex by rememberSaveable(history) { mutableIntStateOf(0) }
                    var activeRevert by rememberSaveable(history) { mutableStateOf<Int?>(null) }

                    val timelineListState = rememberLazyListState()
                    val activeRevertPair by produceState<Pair<ArtistHistoryEntryWithDiff, ArtistDatabaseEntry.Impl>?>(
                        initialValue = null,
                        key1 = history,
                    ) {
                        snapshotFlow { history() to activeRevert }
                            .mapLatest { (history, activeRevertIndex) ->
                                if (activeRevertIndex == null) {
                                    null
                                } else {
                                    history[activeRevertIndex] to ArtistHistoryEntry.rebuild(
                                        dataYear = dataYear,
                                        artistId = artistId,
                                        list = history.drop(activeRevertIndex).map { it.entry },
                                    )
                                }
                            }
                            .collectLatest { value = it }
                    }
                    val activeRevertPairValue = activeRevertPair
                    if (activeRevertPairValue != null) {
                        val fieldState = key(activeRevertPairValue) {
                            rememberFieldState(activeRevertPairValue.first)
                        }
                        val rebuiltEntry = activeRevertPairValue.second
                        val artist by remember {
                            derivedStateOf {
                                val initial = initialArtist() ?: return@derivedStateOf null
                                val changes = fieldState.applyChanges(rebuiltEntry)
                                ArtistHistoryEntry.applyOver(initial, changes)
                            }
                        }

                        ArtistPreview(
                            initialArtist = initialArtist,
                            artist = { artist },
                            seriesById = seriesById(),
                            merchById = merchById(),
                            seriesImage = seriesImage,
                            modifier = Modifier.weight(1f)
                        )

                        RevertFieldsList(
                            initialArtist = initialArtist,
                            fieldState = fieldState,
                            entryWithDiff = activeRevertPairValue.first,
                            rebuiltEntry = activeRevertPairValue.second,
                            onActiveRevertCleared = { activeRevert = null },
                            onApplied = onApplied,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        val artist by remember {
                            derivedStateOf {
                                ArtistHistoryEntry.rebuild(
                                    dataYear = dataYear,
                                    artistId = artistId,
                                    list = history().drop(selectedIndex).map { it.entry },
                                )
                            }
                        }
                        ArtistPreview(
                            initialArtist = initialArtist,
                            artist = { artist },
                            seriesById = seriesById(),
                            merchById = merchById(),
                            seriesImage = seriesImage,
                            modifier = Modifier.weight(1f)
                        )

                        HistoryTimeline(
                            history = history,
                            listState = timelineListState,
                            selectedIndex = { selectedIndex },
                            onSelectedIndexChange = { selectedIndex = it },
                            onRevertSelected = { activeRevert = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ArtistPreview(
        initialArtist: () -> ArtistDatabaseEntry.Impl?,
        artist: () -> ArtistDatabaseEntry?,
        seriesById: Map<String, SeriesInfo>,
        merchById: Map<String, MerchInfo>,
        seriesImage: (SeriesInfo) -> String?,
        modifier: Modifier = Modifier,
    ) {
        Column(modifier = modifier) {
            val artistFormState by produceState<ArtistFormState?>(
                initialValue = null,
                seriesById,
                merchById,
            ) {
                if (seriesById.isNotEmpty() && merchById.isNotEmpty()) {
                    snapshotFlow { artist() }
                        .filterNotNull()
                        .mapLatest {
                            withContext(PlatformDispatchers.IO) {
                                ArtistFormState().applyDatabaseEntry(
                                    artist = it,
                                    seriesById = seriesById,
                                    merchById = merchById,
                                    mergeBehavior = FormMergeBehavior.REPLACE,
                                )
                            }
                        }
                        .collectLatest { value = it }
                }
            }

            val formState = artistFormState
            if (formState != null) {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    ArtistForm(
                        initialArtist = initialArtist,
                        state = formState,
                        errorState = rememberErrorState(formState),
                        seriesById = { seriesById },
                        seriesPredictions = { emptyFlow() },
                        merchById = { merchById },
                        merchPredictions = { emptyFlow() },
                        seriesImage = seriesImage,
                        forceLocked = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }

    @Composable
    private fun HistoryTimeline(
        history: () -> List<ArtistHistoryEntryWithDiff>,
        listState: LazyListState,
        selectedIndex: () -> Int,
        onSelectedIndexChange: (Int) -> Unit,
        onRevertSelected: (Int) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier,
        ) {
            itemsIndexed(history()) { index, entry ->
                HistoryEntryCard(
                    entryWithDiff = entry,
                    selected = selectedIndex() == index,
                    onClick = { onSelectedIndexChange(index) },
                    additionalActions = if (index == 0) null else {
                        {
                            TooltipIconButton(
                                icon = Icons.AutoMirrored.Default.Undo,
                                tooltipText = stringResource(Res.string.alley_edit_artist_history_action_revert_tooltip),
                                onClick = { onRevertSelected(index) },
                            )
                        }
                    },
                )
            }
        }
    }

    @Composable
    private fun HistoryEntryCard(
        entryWithDiff: ArtistHistoryEntryWithDiff,
        selected: Boolean,
        onClick: (() -> Unit)? = null,
        additionalActions: (@Composable () -> Unit)? = null,
    ) {
        HistoryEntryCard(
            selected = selected,
            onClick = onClick,
            header = {
                val entry = entryWithDiff.entry
                HistoryCardHeader(
                    editor = entry.lastEditor,
                    timestamp = entry.timestamp,
                    formTimestamp = entry.formTimestamp,
                    additionalActions = additionalActions,
                )
            },
            content = {
                val entry = entryWithDiff.entry
                HistorySingleChangeRow(
                    label = Res.string.alley_edit_artist_field_label_status,
                    entry.status?.title?.let { stringResource(it) })
                HistorySingleChangeRow(
                    label = Res.string.alley_edit_artist_field_label_booth,
                    entry.booth
                )
                HistorySingleChangeRow(label = Res.string.alley_edit_artist_field_label_name, entry.name)
                HistorySingleChangeRow(
                    label = Res.string.alley_edit_artist_field_label_summary,
                    entry.summary
                )
                HistorySingleChangeRow(
                    label = Res.string.alley_edit_artist_field_label_notes,
                    entry.notes
                )
                HistorySingleChangeRow(
                    label = Res.string.alley_edit_artist_field_label_editor_notes,
                    entry.editorNotes
                )

                HistoryListChangeRow(
                    label = Res.string.alley_edit_artist_field_label_social_links,
                    entryWithDiff.socialLinksDiff,
                )
                HistoryListChangeRow(
                    label = Res.string.alley_edit_artist_field_label_store_links,
                    entryWithDiff.storeLinksDiff,
                )
                HistoryListChangeRow(
                    label = Res.string.alley_edit_artist_field_label_portfolio_links,
                    entryWithDiff.portfolioLinksDiff,
                )
                HistoryListChangeRow(
                    label = Res.string.alley_edit_artist_field_label_catalog_links,
                    entryWithDiff.catalogLinksDiff,
                )
                HistoryListChangeRow(
                    label = Res.string.alley_edit_artist_field_label_commissions,
                    entryWithDiff.commissionsDiff,
                )

                HistoryListChangeRow(
                    label = Res.string.alley_edit_artist_field_label_series_inferred,
                    entryWithDiff.seriesInferredDiff,
                )
                HistoryListChangeRow(
                    label = Res.string.alley_edit_artist_field_label_series_confirmed,
                    entryWithDiff.seriesConfirmedDiff,
                )
                HistoryListChangeRow(
                    label = Res.string.alley_edit_artist_field_label_merch_inferred,
                    entryWithDiff.merchInferredDiff,
                )
                HistoryListChangeRow(
                    label = Res.string.alley_edit_artist_field_label_merch_confirmed,
                    entryWithDiff.merchConfirmedDiff,
                )
            },
        )
    }

    @Composable
    private fun RevertFieldsList(
        initialArtist: () -> ArtistDatabaseEntry.Impl?,
        fieldState: FieldState,
        entryWithDiff: ArtistHistoryEntryWithDiff,
        rebuiltEntry: ArtistDatabaseEntry.Impl,
        onActiveRevertCleared: () -> Unit,
        onApplied: (ArtistHistoryEntry) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        NavigationBackHandler(
            state = rememberNavigationEventState(NavigationEventInfo.None),
            isBackEnabled = true,
            onBackCompleted = onActiveRevertCleared,
        )
        Column(modifier = modifier.fillMaxWidth()) {
            HistoryEntryCard(
                entryWithDiff = entryWithDiff,
                selected = true,
                additionalActions = {
                    TooltipIconButton(
                        icon = Icons.Default.Close,
                        tooltipText = stringResource(Res.string.alley_edit_artist_history_action_return_to_history_tooltip),
                        onClick = onActiveRevertCleared,
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            val groupState = when {
                fieldState.map.values.all { it } -> ToggleableState.On
                fieldState.map.values.any { it } -> ToggleableState.Indeterminate
                else -> ToggleableState.Off
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
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
                )
                Text(stringResource(Res.string.alley_edit_artist_history_select_all))
            }

            val initialArtist = initialArtist()
            ArtistField.entries.forEach { field ->
                val fieldText = when (field) {
                    ArtistField.STATUS -> rebuiltEntry.status
                        .takeIf { it != initialArtist?.status }
                        ?.let { stringResource(it.title) }
                    ArtistField.BOOTH -> rebuiltEntry.booth.takeIf { it != initialArtist?.booth }
                    ArtistField.NAME -> rebuiltEntry.name.takeIf { it != initialArtist?.name }
                    ArtistField.SUMMARY -> rebuiltEntry.summary.takeIf { it != initialArtist?.summary }
                    ArtistField.SOCIAL_LINKS -> rebuiltEntry.socialLinks.takeIf { it != initialArtist?.socialLinks }
                        ?.joinToString()
                    ArtistField.STORE_LINKS -> rebuiltEntry.storeLinks
                        .takeIf { it != initialArtist?.storeLinks }
                        ?.joinToString()
                    ArtistField.PORTFOLIO_LINKS -> rebuiltEntry.portfolioLinks
                        .takeIf { it != initialArtist?.portfolioLinks }
                        ?.joinToString()
                    ArtistField.CATALOG_LINKS -> rebuiltEntry.catalogLinks
                        .takeIf { it != initialArtist?.catalogLinks }
                        ?.joinToString()
                    ArtistField.NOTES -> rebuiltEntry.notes.takeIf { it != initialArtist?.notes }
                    ArtistField.COMMISSIONS -> rebuiltEntry.commissions
                        .takeIf { it != initialArtist?.commissions }
                        ?.joinToString()
                    ArtistField.SERIES_INFERRED -> rebuiltEntry.seriesInferred
                        .takeIf { it != initialArtist?.seriesInferred }
                        ?.joinToString()
                    ArtistField.SERIES_CONFIRMED -> rebuiltEntry.seriesConfirmed
                        .takeIf { it != initialArtist?.seriesConfirmed }
                        ?.joinToString()
                    ArtistField.MERCH_INFERRED -> rebuiltEntry.merchInferred
                        .takeIf { it != initialArtist?.merchInferred }
                        ?.joinToString()
                    ArtistField.MERCH_CONFIRMED -> rebuiltEntry.merchConfirmed
                        .takeIf { it != initialArtist?.merchConfirmed }
                        ?.joinToString()
                    ArtistField.EDITOR_NOTES -> rebuiltEntry.editorNotes
                        .takeIf { it != initialArtist?.editorNotes }
                }

                if (fieldText != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = fieldState[field],
                            onCheckedChange = { fieldState[field] = it },
                        )
                        Text(stringResource(field.label))
                        Text(fieldText)
                    }
                }
            }

            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                FilledTonalButton(
                    onClick = { onApplied(fieldState.applyChanges(rebuiltEntry)) },
                ) {
                    Text(stringResource(Res.string.alley_edit_artist_history_action_apply_changes))
                }
            }
        }
    }

    @Stable
    private class FieldState(val map: SnapshotStateMap<ArtistField, Boolean>) {
        operator fun get(field: ArtistField) = map[field]!!
        operator fun set(field: ArtistField, checked: Boolean) = map.set(field, checked)

        fun applyChanges(entry: ArtistDatabaseEntry.Impl) = ArtistHistoryEntry(
            status = entry.status.takeIf { this[ArtistField.STATUS] },
            booth = entry.booth.takeIf { this[ArtistField.BOOTH] },
            name = entry.name.takeIf { this[ArtistField.NAME] },
            summary = if (this[ArtistField.SUMMARY]) {
                entry.summary.orEmpty()
            } else {
                null
            },
            socialLinks = entry.socialLinks.takeIf { this[ArtistField.SOCIAL_LINKS] },
            storeLinks = entry.storeLinks.takeIf { this[ArtistField.STORE_LINKS] },
            portfolioLinks = entry.portfolioLinks.takeIf { this[ArtistField.PORTFOLIO_LINKS] },
            catalogLinks = entry.catalogLinks.takeIf { this[ArtistField.CATALOG_LINKS] },
            notes = if (this[ArtistField.NOTES]) {
                entry.notes.orEmpty()
            } else {
                null
            },
            commissions = entry.commissions.takeIf { this[ArtistField.COMMISSIONS] },
            seriesInferred = entry.seriesInferred.takeIf { this[ArtistField.SERIES_INFERRED] },
            seriesConfirmed = entry.seriesConfirmed.takeIf { this[ArtistField.SERIES_CONFIRMED] },
            merchInferred = entry.merchInferred.takeIf { this[ArtistField.MERCH_INFERRED] },
            merchConfirmed = entry.merchConfirmed.takeIf { this[ArtistField.MERCH_CONFIRMED] },
            images = null,
            editorNotes = if (this[ArtistField.EDITOR_NOTES]) {
                entry.editorNotes.orEmpty()
            } else {
                null
            },
            lastEditor = null,
            timestamp = Clock.System.now(),
            formTimestamp = null,
        )
    }

    @Composable
    private fun rememberFieldState(entryWithDiff: ArtistHistoryEntryWithDiff): FieldState {
        val map = rememberSaveable(entryWithDiff) {
            mutableStateMapOf<ArtistField, Boolean>().apply {
                ArtistField.entries.forEach {
                    val entry = entryWithDiff.entry
                    this[it] = when (it) {
                        ArtistField.STATUS -> entry.status != null
                        ArtistField.BOOTH -> entry.booth != null
                        ArtistField.NAME -> entry.name != null
                        ArtistField.SUMMARY -> entry.summary != null
                        ArtistField.SOCIAL_LINKS -> entry.socialLinks != null
                        ArtistField.STORE_LINKS -> entry.storeLinks != null
                        ArtistField.PORTFOLIO_LINKS -> entry.portfolioLinks != null
                        ArtistField.CATALOG_LINKS -> entry.catalogLinks != null
                        ArtistField.NOTES -> entry.notes != null
                        ArtistField.COMMISSIONS -> entry.commissions != null
                        ArtistField.SERIES_INFERRED -> entry.seriesInferred != null
                        ArtistField.SERIES_CONFIRMED -> entry.seriesConfirmed != null
                        ArtistField.MERCH_INFERRED -> entry.merchInferred != null
                        ArtistField.MERCH_CONFIRMED -> entry.merchConfirmed != null
                        ArtistField.EDITOR_NOTES -> entry.editorNotes != null
                    }
                }
            }
        }
        return remember(map) { FieldState(map) }
    }

    private enum class ArtistField(val label: StringResource) {
        STATUS(Res.string.alley_edit_artist_field_label_status),
        BOOTH(Res.string.alley_edit_artist_field_label_booth),
        NAME(Res.string.alley_edit_artist_field_label_name),
        SUMMARY(Res.string.alley_edit_artist_field_label_summary),
        SOCIAL_LINKS(Res.string.alley_edit_artist_field_label_social_links),
        STORE_LINKS(Res.string.alley_edit_artist_field_label_store_links),
        PORTFOLIO_LINKS(Res.string.alley_edit_artist_field_label_portfolio_links),
        CATALOG_LINKS(Res.string.alley_edit_artist_field_label_catalog_links),
        NOTES(Res.string.alley_edit_artist_field_label_notes),
        COMMISSIONS(Res.string.alley_edit_artist_field_label_commissions),
        SERIES_INFERRED(Res.string.alley_edit_artist_field_label_series_inferred),
        SERIES_CONFIRMED(Res.string.alley_edit_artist_field_label_series_confirmed),
        MERCH_INFERRED(Res.string.alley_edit_artist_field_label_merch_inferred),
        MERCH_CONFIRMED(Res.string.alley_edit_artist_field_label_merch_confirmed),
        EDITOR_NOTES(Res.string.alley_edit_artist_field_label_editor_notes),
    }
}
