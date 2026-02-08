package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.edit.generated.resources.Res
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
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_form_merge_action_save
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_form_merge_outdated
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_form_merge_timestamp_prefix
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_form_merge_title_fandom
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_form_merge_title_host_table_fandom
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.edit.form.FormMergeBehavior
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.rallies.StampRallyForm
import com.thekeeperofpie.artistalleydatabase.alley.edit.rallies.StampRallyFormState
import com.thekeeperofpie.artistalleydatabase.alley.edit.rallies.rememberErrorState
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ContentSavingBox
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ScrollableSideBySide
import com.thekeeperofpie.artistalleydatabase.alley.models.HistoryListDiff
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyEntryDiff
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.alley.ui.theme.AlleyTheme
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TableMin
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.GenericTaskErrorEffect
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalDateTimeFormatter
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationResults
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationResults
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal object StampRallyFormMergeScreen {

    val RESULT_KEY = NavigationResults.Key<Unit>("ArtistFormMergeScreen")

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        artistId: Uuid,
        stampRallyId: String,
        graph: ArtistAlleyEditGraph,
        onClickBack: (force: Boolean) -> Unit,
        viewModel: StampRallyFormMergeViewModel = viewModel {
            graph.stampRallyFormMergeViewModelFactory.create(
                dataYear,
                artistId,
                stampRallyId,
                createSavedStateHandle()
            )
        },
    ) {
        val stampRallyWithFormEntry by viewModel.entry.collectAsStateWithLifecycle()
        val seriesById by viewModel.tagAutocomplete.seriesById.collectAsStateWithLifecycle()
        val merchById by viewModel.tagAutocomplete.merchById.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }
        val saveTaskState = viewModel.saveTaskState
        StampRallyFormMergeScreen(
            dataYear = dataYear,
            stampRallyId = stampRallyId,
            snackbarHostState = snackbarHostState,
            entry = { stampRallyWithFormEntry?.run { stampRally to formDiff } },
            saving = { saveTaskState.showBlockingLoadingIndicator },
            seriesById = { seriesById },
            merchById = { merchById },
            seriesImage = viewModel::seriesImage,
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
                        is BackendRequest.StampRallyCommitForm.Response.Failed -> {
                            snackbarHostState.showSnackbar(message = result.errorMessage)
                            saveTaskState.clearResult()
                        }
                        is BackendRequest.StampRallyCommitForm.Response.Outdated -> {
                            snackbarHostState.showSnackbar(message = getString(Res.string.alley_edit_stamp_rally_form_merge_outdated))
                            saveTaskState.clearResult()
                        }
                        is BackendRequest.StampRallyCommitForm.Response.Success -> {
                            saveTaskState.clearResult()
                            navigationResults[RESULT_KEY] = Unit
                            onClickBack(true)
                        }
                    }
                }
        }
    }

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        stampRallyId: String,
        snackbarHostState: SnackbarHostState,
        entry: () -> Pair<StampRallyDatabaseEntry?, StampRallyEntryDiff>?,
        saving: () -> Boolean,
        seriesById: () -> Map<String, SeriesInfo>,
        merchById: () -> Map<String, MerchInfo>,
        seriesImage: (SeriesInfo) -> String?,
        onClickBack: (force: Boolean) -> Unit,
        onClickSave: (List<EditImage>, StampRallyDatabaseEntry) -> Unit,
    ) {
        val entry = entry()
        val initialStampRally = entry()?.first
        val formDiff = entry()?.second
        val fieldState = rememberFieldState(formDiff)
        val seriesByIdMap = seriesById()
        val merchByIdMap = merchById()
        val stampRallyFormState by remember(entry, fieldState, seriesByIdMap, merchByIdMap) {
            derivedStateOf {
                formDiff ?: return@derivedStateOf null
                fieldState.applyChanges(
                    base = initialStampRally ?: StampRallyDatabaseEntry.empty(dataYear, stampRallyId),
                    seriesById = seriesByIdMap,
                    merchById = merchByIdMap,
                    diff = formDiff,
                )
            }
        }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val conventionName = stringResource(dataYear.shortName)
                        val fandom = initialStampRally?.fandom ?: initialStampRally?.id.orEmpty()
                        val hostTable = initialStampRally?.hostTable.orEmpty()
                        val text = if (hostTable.isEmpty()) {
                            stringResource(
                                Res.string.alley_edit_stamp_rally_form_merge_title_fandom,
                                conventionName,
                                fandom,
                            )
                        } else {
                            stringResource(
                                Res.string.alley_edit_stamp_rally_form_merge_title_host_table_fandom,
                                conventionName,
                                hostTable,
                                fandom,
                            )
                        }
                        Text(text = text)
                    },
                    navigationIcon = { ArrowBackIconButton(onClick = { onClickBack(true) }) },
                    actions = {
                        TooltipIconButton(
                            icon = Icons.Default.Save,
                            tooltipText = stringResource(Res.string.alley_edit_stamp_rally_form_merge_action_save),
                            onClick = {
                                stampRallyFormState?.captureDatabaseEntry(dataYear)?.let {
                                    onClickSave(it.first, it.second)
                                }
                            },
                        )
                    },
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = Modifier.fillMaxWidth()
        ) { scaffoldPadding ->
            ContentSavingBox(
                saving = saving(),
                modifier = Modifier.fillMaxWidth().padding(scaffoldPadding)
            ) {
                val modifier = Modifier.widthIn(max = 1200.dp).align(Alignment.TopCenter)
                if (stampRallyFormState == null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = modifier.padding(32.dp)
                    ) {
                        CircularWavyProgressIndicator()
                    }
                } else {
                    val fieldsList = remember {
                        movableContentOf {
                            FieldsList(fieldState = fieldState, diff = formDiff)
                        }
                    }
                    ScrollableSideBySide(
                        showSecondary = { true },
                        primary = {
                            StampRallyPreview(
                                initialStampRally = { initialStampRally },
                                stampRallyFormState = stampRallyFormState,
                                formTimestamp = formDiff?.timestamp,
                                seriesById = seriesById,
                                seriesImage = seriesImage,
                                merchById = merchById,
                            )
                        },
                        secondary = { fieldsList() },
                        secondaryExpanded = { fieldsList() },
                    )
                }
            }
        }
    }

    @Composable
    private fun StampRallyPreview(
        initialStampRally: () -> StampRallyDatabaseEntry?,
        stampRallyFormState: StampRallyFormState?,
        formTimestamp: Instant?,
        seriesById: () -> Map<String, SeriesInfo>,
        merchById: () -> Map<String, MerchInfo>,
        seriesImage: (SeriesInfo) -> String?,
        modifier: Modifier = Modifier,
    ) {
        if (stampRallyFormState != null) {
            Column {
                if (formTimestamp != null) {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = LocalContentColor.current.copy(alpha = 0.6f))) {
                                    append(stringResource(Res.string.alley_edit_stamp_rally_form_merge_timestamp_prefix))
                                }
                                append(' ')
                                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                    append(
                                        LocalDateTimeFormatter.current
                                            .formatDateTime(formTimestamp)
                                    )
                                }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
                StampRallyForm(
                    initialStampRally = initialStampRally,
                    state = stampRallyFormState,
                    errorState = rememberErrorState(stampRallyFormState),
                    seriesById = seriesById,
                    seriesPredictions = { emptyFlow() },
                    merchById = merchById,
                    merchPredictions = { emptyFlow() },
                    seriesImage = seriesImage,
                    modifier = modifier.fillMaxWidth(),
                )
            }
        }
    }

    @Composable
    private fun FieldsList(
        fieldState: FieldState,
        diff: StampRallyEntryDiff?,
        modifier: Modifier = Modifier,
    ) {
        Column(modifier = modifier.fillMaxWidth()) {
            // TODO: Add rally specific form notes?
            val groupState = when {
                fieldState.values.all { it } -> ToggleableState.On
                fieldState.values.any { it } -> ToggleableState.Indeterminate
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
                    fieldState.keys.toSet().forEach {
                        fieldState[it] = newValue
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            StampRallyField.entries.forEach { field ->
                if (!fieldState.keys.contains(field)) return@forEach
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

                    val fieldText = when (field) {
                        StampRallyField.FANDOM -> diff?.fandom
                        StampRallyField.HOST_TABLE -> diff?.hostTable
                        StampRallyField.TABLES_ADDED -> diff?.tables?.added?.joinToString()
                        StampRallyField.TABLES_REMOVED -> diff?.tables?.deleted?.joinToString()
                        StampRallyField.LINKS_ADDED -> diff?.links?.added?.joinToString()
                        StampRallyField.LINKS_REMOVED -> diff?.links?.deleted?.joinToString()
                        StampRallyField.TABLE_MIN -> diff?.tableMin?.toString()
                        StampRallyField.PRIZE -> diff?.prize
                        StampRallyField.PRIZE_LIMIT -> diff?.prizeLimit?.toString()
                        StampRallyField.SERIES_ADDED -> diff?.series?.added?.joinToString()
                        StampRallyField.SERIES_REMOVED -> diff?.series?.deleted?.joinToString()
                        StampRallyField.MERCH_ADDED -> diff?.merch?.added?.joinToString()
                        StampRallyField.MERCH_REMOVED -> diff?.merch?.deleted?.joinToString()
                        StampRallyField.NOTES -> diff?.notes
                    }.orEmpty()
                    Text(
                        text = fieldText,
                        color = if (field.isRemoved) {
                            AlleyTheme.colorScheme.negative
                        } else {
                            Color.Unspecified
                        },
                    )
                }
            }
        }
    }

    @Stable
    private class FieldState(private val map: SnapshotStateMap<StampRallyField, Boolean>) {
        val keys get() = map.keys
        val values get() = map.values
        operator fun get(field: StampRallyField) = map[field] ?: false
        operator fun set(field: StampRallyField, checked: Boolean) = map.set(field, checked)

        fun applyChanges(
            base: StampRallyDatabaseEntry,
            seriesById: Map<String, SeriesInfo>,
            merchById: Map<String, MerchInfo>,
            diff: StampRallyEntryDiff,
        ): StampRallyFormState {
            fun <T> applyDiff(
                base: T,
                diff: T?,
                field: StampRallyField,
            ) = if (this[field]) {
                diff ?: base
            } else {
                base
            }

            fun applyDiff(
                base: List<String>,
                diff: HistoryListDiff?,
                added: StampRallyField,
                deleted: StampRallyField,
            ): List<String> {
                val base = base.toMutableSet()
                if (this[deleted]) base.removeAll(diff?.deleted.orEmpty().toSet())
                if (this[added]) base.addAll(diff?.added.orEmpty().toSet())
                return base.toMutableList()
            }

            val tables = applyDiff(
                base.tables,
                diff.tables,
                StampRallyField.TABLES_ADDED,
                StampRallyField.TABLES_REMOVED,
            )
            val tableMin = applyDiff(base.tableMin, diff.tableMin, StampRallyField.TABLE_MIN)

            val stampRally = base.copy(
                fandom = applyDiff(base.fandom, diff.fandom, StampRallyField.FANDOM),
                hostTable = applyDiff(base.hostTable, diff.hostTable, StampRallyField.HOST_TABLE),
                tables = tables,
                links = applyDiff(
                    base.links,
                    diff.links,
                    StampRallyField.LINKS_ADDED,
                    StampRallyField.LINKS_REMOVED,
                ),
                tableMin = tableMin,
                totalCost = when (tableMin) {
                    TableMin.Any -> null
                    TableMin.Free -> 0
                    TableMin.Other -> null
                    TableMin.Paid -> null
                    is TableMin.Price -> tableMin.usd * tables.size.toLong()
                    null -> null
                },
                prize = applyDiff(base.prize, diff.prize, StampRallyField.PRIZE),
                prizeLimit = applyDiff(
                    base.prizeLimit,
                    diff.prizeLimit,
                    StampRallyField.PRIZE_LIMIT
                ),
                series = applyDiff(
                    base.series,
                    diff.series,
                    StampRallyField.SERIES_ADDED,
                    StampRallyField.SERIES_REMOVED,
                ),
                merch = applyDiff(
                    base.merch,
                    diff.merch,
                    StampRallyField.MERCH_ADDED,
                    StampRallyField.MERCH_REMOVED,
                ),
                notes = diff.notes ?: base.notes,
            )
            return StampRallyFormState().applyDatabaseEntry(
                stampRally = stampRally,
                seriesById = seriesById,
                merchById = merchById,
                mergeBehavior = FormMergeBehavior.REPLACE,
            )
        }
    }

    @Composable
    private fun rememberFieldState(diff: StampRallyEntryDiff?): FieldState {
        val map = rememberSaveable(diff) {
            mutableStateMapOf<StampRallyField, Boolean>().apply {
                if (diff == null) return@apply
                StampRallyField.entries.forEach {
                    val include = when (it) {
                        StampRallyField.FANDOM -> diff.fandom != null
                        StampRallyField.HOST_TABLE -> diff.hostTable != null
                        StampRallyField.TABLES_ADDED -> diff.tables?.added != null
                        StampRallyField.TABLES_REMOVED -> diff.tables?.deleted != null
                        StampRallyField.LINKS_ADDED -> diff.links?.added != null
                        StampRallyField.LINKS_REMOVED -> diff.links?.deleted != null
                        StampRallyField.TABLE_MIN -> diff.tableMin != null
                        StampRallyField.PRIZE -> diff.prize != null
                        StampRallyField.PRIZE_LIMIT -> diff.prizeLimit != null
                        StampRallyField.SERIES_ADDED -> diff.series?.added != null
                        StampRallyField.SERIES_REMOVED -> diff.series?.deleted != null
                        StampRallyField.MERCH_ADDED -> diff.merch?.added != null
                        StampRallyField.MERCH_REMOVED -> diff.merch?.deleted != null
                        StampRallyField.NOTES -> diff.notes != null
                    }
                    if (!include) return@forEach
                    this[it] = it.isRemoved
                }
            }
        }
        return remember(map) { FieldState(map) }
    }

    private enum class StampRallyField(val label: StringResource) {
        FANDOM(Res.string.alley_edit_stamp_rally_field_label_fandom),
        HOST_TABLE(Res.string.alley_edit_stamp_rally_field_label_host_table),
        TABLES_ADDED(Res.string.alley_edit_stamp_rally_field_label_tables),
        TABLES_REMOVED(Res.string.alley_edit_stamp_rally_field_label_tables),
        LINKS_ADDED(Res.string.alley_edit_stamp_rally_field_label_links),
        LINKS_REMOVED(Res.string.alley_edit_stamp_rally_field_label_links),
        TABLE_MIN(Res.string.alley_edit_stamp_rally_field_label_table_min),
        PRIZE(Res.string.alley_edit_stamp_rally_field_label_prize),
        PRIZE_LIMIT(Res.string.alley_edit_stamp_rally_field_label_prize_limit),
        SERIES_ADDED(Res.string.alley_edit_stamp_rally_field_label_series),
        SERIES_REMOVED(Res.string.alley_edit_stamp_rally_field_label_series),
        MERCH_ADDED(Res.string.alley_edit_stamp_rally_field_label_merch),
        MERCH_REMOVED(Res.string.alley_edit_stamp_rally_field_label_merch),
        NOTES(Res.string.alley_edit_stamp_rally_field_label_notes),
        ;

        val isRemoved: Boolean
            get() = when (this) {
                FANDOM,
                HOST_TABLE,
                TABLES_ADDED,
                LINKS_ADDED,
                TABLE_MIN,
                PRIZE,
                PRIZE_LIMIT,
                SERIES_ADDED,
                MERCH_ADDED,
                NOTES,
                    -> false
                TABLES_REMOVED,
                LINKS_REMOVED,
                SERIES_REMOVED,
                MERCH_REMOVED,
                    -> true
            }
    }
}
