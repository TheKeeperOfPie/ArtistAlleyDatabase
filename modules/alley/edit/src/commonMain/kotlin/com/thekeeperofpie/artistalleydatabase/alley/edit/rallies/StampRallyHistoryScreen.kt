package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies

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
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_field_label_editor_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_field_label_fandom
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_field_label_host_table
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_field_label_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_field_label_merch
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_field_label_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_field_label_prize
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_field_label_prize_limit
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_field_label_series
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_field_label_table_min
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_field_label_tables
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_history_action_apply_changes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_history_action_refresh_tooltip
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_history_action_return_to_history_tooltip
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_history_action_revert_tooltip
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_history_select_all
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_history_title
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.edit.form.FormMergeBehavior
import com.thekeeperofpie.artistalleydatabase.alley.edit.history.HistoryCardHeader
import com.thekeeperofpie.artistalleydatabase.alley.edit.history.HistoryEntryCard
import com.thekeeperofpie.artistalleydatabase.alley.edit.history.HistoryListChangeRow
import com.thekeeperofpie.artistalleydatabase.alley.edit.history.HistorySingleChangeRow
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ContentSavingBox
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyHistoryEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TableMin
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

object StampRallyHistoryScreen {

    val RESULT_KEY = NavigationResults.Key<Unit>("StampRallyHistoryScreen")

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        stampRallyId: String,
        graph: ArtistAlleyEditGraph,
        onClickBack: (force: Boolean) -> Unit,
        viewModel: StampRallyHistoryViewModel = viewModel {
            graph.stampRallyHistoryViewModelFactory.create(dataYear, stampRallyId)
        },
    ) {
        val history by viewModel.history.collectAsStateWithLifecycle()
        val seriesById by viewModel.tagAutocomplete.seriesById.collectAsStateWithLifecycle(emptyMap())
        val merchById by viewModel.tagAutocomplete.merchById.collectAsStateWithLifecycle(emptyMap())
        val initialStampRally by viewModel.initial.collectAsStateWithLifecycle()
        StampRallyHistoryScreen(
            history = { history },
            dataYear = dataYear,
            stampRallyId = stampRallyId,
            initialStampRally = { initialStampRally },
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
        history: () -> List<StampRallyHistoryEntryWithDiff>,
        dataYear: DataYear,
        stampRallyId: String,
        initialStampRally: () -> StampRallyDatabaseEntry?,
        seriesById: () -> Map<String, SeriesInfo>,
        merchById: () -> Map<String, MerchInfo>,
        saveProgress: MutableStateFlow<JobProgress<BackendRequest.StampRallySave.Response>>,
        seriesImage: (SeriesInfo) -> String?,
        onClickBack: (force: Boolean) -> Unit,
        onClickRefresh: () -> Unit,
        onApplied: (StampRallyHistoryEntry) -> Unit,
    ) {
        val snackbarHostState = remember { SnackbarHostState() }
        val navigationResults = LocalNavigationResults.current
        LaunchedEffect(navigationResults) {
            saveProgress.collectLatest {
                if (it is JobProgress.Finished.Result<BackendRequest.StampRallySave.Response>) {
                    when (val result = it.value) {
                        is BackendRequest.StampRallySave.Response.Failed ->
                            snackbarHostState.showSnackbar(message = result.errorMessage)
                        is BackendRequest.StampRallySave.Response.Outdated -> {
                            // TODO
                        }
                        is BackendRequest.StampRallySave.Response.Success -> {
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
                        val fandom = remember(history) {
                            history.firstNotNullOfOrNull { it.entry.fandom }.orEmpty()
                        }
                        Text(
                            stringResource(
                                Res.string.alley_edit_stamp_rally_history_title,
                                stringResource(dataYear.shortName),
                                fandom,
                            )
                        )
                    },
                    navigationIcon = { ArrowBackIconButton(onClick = { onClickBack(true) }) },
                    actions = {
                        TooltipIconButton(
                            icon = Icons.Default.Refresh,
                            tooltipText = stringResource(Res.string.alley_edit_stamp_rally_history_action_refresh_tooltip),
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
                    val activeRevertPair by produceState<Pair<StampRallyHistoryEntryWithDiff, StampRallyDatabaseEntry>?>(
                        initialValue = null,
                        key1 = history,
                    ) {
                        snapshotFlow { history() to activeRevert }
                            .mapLatest { (history, activeRevertIndex) ->
                                if (activeRevertIndex == null) {
                                    null
                                } else {
                                    history[activeRevertIndex] to StampRallyHistoryEntry.rebuild(
                                        dataYear = dataYear,
                                        stampRallyId = stampRallyId,
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
                        val stampRally by remember {
                            derivedStateOf {
                                val initial = initialStampRally() ?: return@derivedStateOf null
                                val changes = fieldState.applyChanges(rebuiltEntry)
                                StampRallyHistoryEntry.applyOver(initial, changes)
                            }
                        }

                        StampRallyPreview(
                            initialStampRally = initialStampRally,
                            stampRally = { stampRally },
                            seriesById = seriesById(),
                            merchById = merchById(),
                            seriesImage = seriesImage,
                            modifier = Modifier.weight(1f)
                        )

                        RevertFieldsList(
                            initialStampRally = initialStampRally,
                            fieldState = fieldState,
                            entryWithDiff = activeRevertPairValue.first,
                            rebuiltEntry = activeRevertPairValue.second,
                            onActiveRevertCleared = { activeRevert = null },
                            onApplied = onApplied,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        val stampRally by remember {
                            derivedStateOf {
                                StampRallyHistoryEntry.rebuild(
                                    dataYear = dataYear,
                                    stampRallyId = stampRallyId,
                                    list = history().drop(selectedIndex).map { it.entry },
                                )
                            }
                        }
                        StampRallyPreview(
                            initialStampRally = initialStampRally,
                            stampRally = { stampRally },
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
    private fun StampRallyPreview(
        initialStampRally: () -> StampRallyDatabaseEntry?,
        stampRally: () -> StampRallyDatabaseEntry?,
        seriesById: Map<String, SeriesInfo>,
        merchById: Map<String, MerchInfo>,
        seriesImage: (SeriesInfo) -> String?,
        modifier: Modifier = Modifier,
    ) {
        Column(modifier = modifier) {
            val stampRallyFormState by produceState<StampRallyFormState?>(
                initialValue = null,
                seriesById,
                merchById,
            ) {
                if (seriesById.isNotEmpty() && merchById.isNotEmpty()) {
                    snapshotFlow { stampRally() }
                        .filterNotNull()
                        .mapLatest {
                            withContext(PlatformDispatchers.IO) {
                                StampRallyFormState().applyDatabaseEntry(
                                    stampRally = it,
                                    seriesById = seriesById,
                                    merchById = merchById,
                                    mergeBehavior = FormMergeBehavior.REPLACE,
                                )
                            }
                        }
                        .collectLatest { value = it }
                }
            }

            val formState = stampRallyFormState
            if (formState != null) {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    StampRallyForm(
                        initialStampRally = initialStampRally,
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
        history: () -> List<StampRallyHistoryEntryWithDiff>,
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
                                tooltipText = stringResource(Res.string.alley_edit_stamp_rally_history_action_revert_tooltip),
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
        entryWithDiff: StampRallyHistoryEntryWithDiff,
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
                    label = Res.string.alley_edit_stamp_rally_field_label_fandom,
                    entry.fandom
                )
                HistorySingleChangeRow(
                    label = Res.string.alley_edit_stamp_rally_field_label_host_table,
                    entry.hostTable
                )
                HistoryListChangeRow(
                    label = Res.string.alley_edit_stamp_rally_field_label_tables,
                    entryWithDiff.tablesDiff
                )
                HistoryListChangeRow(
                    label = Res.string.alley_edit_stamp_rally_field_label_links,
                    entryWithDiff.linksDiff
                )
                HistorySingleChangeRow(
                    label = Res.string.alley_edit_stamp_rally_field_label_table_min,
                    entry.tableMin?.toString()
                )
                HistorySingleChangeRow(
                    label = Res.string.alley_edit_stamp_rally_field_label_prize,
                    entry.prize
                )
                HistorySingleChangeRow(
                    label = Res.string.alley_edit_stamp_rally_field_label_prize_limit,
                    entry.prizeLimit?.toString()
                )
                HistoryListChangeRow(
                    label = Res.string.alley_edit_stamp_rally_field_label_series,
                    entryWithDiff.seriesDiff
                )
                HistoryListChangeRow(
                    label = Res.string.alley_edit_stamp_rally_field_label_merch,
                    entryWithDiff.merchDiff
                )
                HistorySingleChangeRow(
                    label = Res.string.alley_edit_stamp_rally_field_label_notes,
                    entry.notes
                )
                HistorySingleChangeRow(
                    label = Res.string.alley_edit_stamp_rally_field_label_editor_notes,
                    entry.editorNotes
                )
            },
        )
    }

    @Composable
    private fun RevertFieldsList(
        initialStampRally: () -> StampRallyDatabaseEntry?,
        fieldState: FieldState,
        entryWithDiff: StampRallyHistoryEntryWithDiff,
        rebuiltEntry: StampRallyDatabaseEntry,
        onActiveRevertCleared: () -> Unit,
        onApplied: (StampRallyHistoryEntry) -> Unit,
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
                        tooltipText = stringResource(Res.string.alley_edit_stamp_rally_history_action_return_to_history_tooltip),
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
                Text(stringResource(Res.string.alley_edit_stamp_rally_history_select_all))
            }

            val initialStampRally = initialStampRally()
            StampRallyField.entries.forEach { field ->
                val fieldText = when (field) {
                    StampRallyField.FANDOM -> rebuiltEntry.fandom.takeIf { it != initialStampRally?.fandom }
                    StampRallyField.HOST_TABLE -> rebuiltEntry.hostTable.takeIf { it != initialStampRally?.hostTable }
                    StampRallyField.TABLES -> rebuiltEntry.tables.takeIf { it != initialStampRally?.tables }?.joinToString()
                    StampRallyField.LINKS -> rebuiltEntry.links.takeIf { it != initialStampRally?.links }?.joinToString()
                    StampRallyField.TABLE_MIN -> rebuiltEntry.tableMin.takeIf { it != initialStampRally?.tableMin }?.toString()
                    StampRallyField.PRIZE -> rebuiltEntry.prize.takeIf { it != initialStampRally?.prize }
                    StampRallyField.PRIZE_LIMIT -> rebuiltEntry.prizeLimit.takeIf { it != initialStampRally?.prizeLimit }?.toString()
                    StampRallyField.SERIES -> rebuiltEntry.series.takeIf { it != initialStampRally?.series }?.joinToString()
                    StampRallyField.MERCH -> rebuiltEntry.merch.takeIf { it != initialStampRally?.merch }?.joinToString()
                    StampRallyField.NOTES -> rebuiltEntry.notes.takeIf { it != initialStampRally?.notes }
                    StampRallyField.EDITOR_NOTES -> rebuiltEntry.editorNotes.takeIf { it != initialStampRally?.editorNotes }
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
                    Text(stringResource(Res.string.alley_edit_stamp_rally_history_action_apply_changes))
                }
            }
        }
    }

    @Stable
    private class FieldState(val map: SnapshotStateMap<StampRallyField, Boolean>) {
        operator fun get(field: StampRallyField) = map[field]!!
        operator fun set(field: StampRallyField, checked: Boolean) = map.set(field, checked)

        fun applyChanges(entry: StampRallyDatabaseEntry): StampRallyHistoryEntry {
            val tableMin = entry.tableMin.takeIf { this[StampRallyField.TABLE_MIN] }
            val tables = entry.tables.takeIf { this[StampRallyField.TABLES] }
            return StampRallyHistoryEntry(
                fandom = entry.fandom.takeIf { this[StampRallyField.FANDOM] },
                hostTable = entry.hostTable.takeIf { this[StampRallyField.HOST_TABLE] },
                tables = tables,
                links = entry.links.takeIf { this[StampRallyField.LINKS] },
                tableMin = tableMin,
                prize = entry.prize.takeIf { this[StampRallyField.PRIZE] },
                prizeLimit = entry.prizeLimit.takeIf { this[StampRallyField.PRIZE_LIMIT] },
                series = entry.series.takeIf { this[StampRallyField.SERIES] },
                merch = entry.merch.takeIf { this[StampRallyField.MERCH] },
                notes = entry.notes.takeIf { this[StampRallyField.NOTES] },
                editorNotes = entry.editorNotes.takeIf { this[StampRallyField.EDITOR_NOTES] },
                totalCost = when (tableMin) {
                    TableMin.Any -> null
                    TableMin.Free -> 0
                    TableMin.Other -> null
                    TableMin.Paid -> null
                    is TableMin.Price -> tableMin.usd * (tables?.size?.toLong() ?: 0)
                    null -> null
                },
                confirmed = false,
                images = null,
                lastEditor = null,
                timestamp = Clock.System.now(),
                formTimestamp = null,
            )
        }
    }

    @Composable
    private fun rememberFieldState(entryWithDiff: StampRallyHistoryEntryWithDiff): FieldState {
        val map = rememberSaveable(entryWithDiff) {
            mutableStateMapOf<StampRallyField, Boolean>().apply {
                StampRallyField.entries.forEach {
                    val entry = entryWithDiff.entry
                    this[it] = when (it) {
                        StampRallyField.FANDOM -> entry.fandom != null
                        StampRallyField.HOST_TABLE -> entry.hostTable != null
                        StampRallyField.TABLES -> entry.tables != null
                        StampRallyField.LINKS -> entry.links != null
                        StampRallyField.TABLE_MIN -> entry.tableMin != null
                        StampRallyField.PRIZE -> entry.prize != null
                        StampRallyField.PRIZE_LIMIT -> entry.prizeLimit != null
                        StampRallyField.SERIES -> entry.series != null
                        StampRallyField.MERCH -> entry.merch != null
                        StampRallyField.NOTES -> entry.notes != null
                        StampRallyField.EDITOR_NOTES -> entry.editorNotes != null
                    }
                }
            }
        }
        return remember(map) { FieldState(map) }
    }

    private enum class StampRallyField(val label: StringResource) {
        FANDOM(Res.string.alley_edit_stamp_rally_field_label_fandom),
        HOST_TABLE(Res.string.alley_edit_stamp_rally_field_label_host_table),
        TABLES(Res.string.alley_edit_stamp_rally_field_label_tables),
        LINKS(Res.string.alley_edit_stamp_rally_field_label_links),
        TABLE_MIN(Res.string.alley_edit_stamp_rally_field_label_table_min),
        PRIZE(Res.string.alley_edit_stamp_rally_field_label_prize),
        PRIZE_LIMIT(Res.string.alley_edit_stamp_rally_field_label_prize_limit),
        SERIES(Res.string.alley_edit_stamp_rally_field_label_series),
        MERCH(Res.string.alley_edit_stamp_rally_field_label_merch),
        NOTES(Res.string.alley_edit_stamp_rally_field_label_notes),
        EDITOR_NOTES(Res.string.alley_edit_stamp_rally_field_label_editor_notes),
    }
}
