package com.thekeeperofpie.artistalleydatabase.alley.edit.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularWavyProgressIndicator
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_booth
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_catalog_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_commissions
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_merch_confirmed
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_merch_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_name
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_portfolio_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_series_confirmed
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_series_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_social_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_store_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_summary
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_form_merge_action_save
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_form_merge_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_form_merge_outdated
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_form_merge_title_booth_name
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_form_merge_title_name
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistForm
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistFormState
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.rememberErrorState
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ContentSavingBox
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistEntryDiff
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.alley.ui.theme.AlleyTheme
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.GenericTaskErrorEffect
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationResults
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationResults
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

internal object ArtistFormMergeScreen {

    val RESULT_KEY = NavigationResults.Key<Unit>("ArtistFormMergeScreen")

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        artistId: Uuid,
        graph: ArtistAlleyEditGraph,
        onClickBack: (force: Boolean) -> Unit,
        viewModel: ArtistFormMergeViewModel = viewModel {
            graph.artistFormMergeViewModelFactory.create(
                dataYear,
                artistId,
                createSavedStateHandle()
            )
        },
    ) {
        val artistWithFormEntry by viewModel.entry.collectAsStateWithLifecycle()
        val seriesById by viewModel.seriesById.collectAsStateWithLifecycle()
        val merchById by viewModel.merchById.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }
        val saveTaskState = viewModel.saveTaskState
        ArtistFormMergeScreen(
            dataYear = dataYear,
            artistId = artistId,
            snackbarHostState = snackbarHostState,
            entry = { artistWithFormEntry?.run { artist to formDiff } },
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
                        is BackendRequest.ArtistCommitForm.Response.Failed -> {
                            snackbarHostState.showSnackbar(message = result.errorMessage)
                            saveTaskState.clearResult()
                        }
                        is BackendRequest.ArtistCommitForm.Response.Outdated -> {
                            snackbarHostState.showSnackbar(message = getString(Res.string.alley_edit_artist_form_merge_outdated))
                            saveTaskState.clearResult()
                        }
                        is BackendRequest.ArtistCommitForm.Response.Success -> {
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
        artistId: Uuid,
        snackbarHostState: SnackbarHostState,
        entry: () -> Pair<ArtistDatabaseEntry.Impl, ArtistEntryDiff>?,
        saving: () -> Boolean,
        seriesById: () -> Map<String, SeriesInfo>,
        merchById: () -> Map<String, MerchInfo>,
        seriesImage: (SeriesInfo) -> String?,
        onClickBack: (force: Boolean) -> Unit,
        onClickSave: (List<EditImage>, ArtistDatabaseEntry.Impl) -> Unit,
    ) {

        val entry = entry()
        val initialArtist = entry()?.first
        val formDiff = entry()?.second
        val fieldState = rememberFieldState(formDiff)
        val seriesByIdMap = seriesById()
        val merchByIdMap = merchById()
        val artistFormState by remember(entry, fieldState, seriesByIdMap, merchByIdMap) {
            derivedStateOf {
                initialArtist ?: return@derivedStateOf null
                formDiff ?: return@derivedStateOf null
                fieldState.applyChanges(
                    base = initialArtist,
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
                        val name = initialArtist?.name ?: initialArtist?.id.orEmpty()
                        val booth = initialArtist?.booth.orEmpty()
                        val text = if (booth.isEmpty()) {
                            stringResource(
                                Res.string.alley_edit_artist_form_merge_title_name,
                                conventionName,
                                name,
                            )
                        } else {
                            stringResource(
                                Res.string.alley_edit_artist_form_merge_title_booth_name,
                                conventionName,
                                booth,
                                name,
                            )
                        }
                        Text(text = text)
                    },
                    navigationIcon = { ArrowBackIconButton(onClick = { onClickBack(true) }) },
                    actions = {
                        TooltipIconButton(
                            icon = Icons.Default.Save,
                            tooltipText = stringResource(Res.string.alley_edit_artist_form_merge_action_save),
                            onClick = {
                                artistFormState?.captureDatabaseEntry(dataYear, true)?.let {
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
                if (initialArtist == null || artistFormState == null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = modifier.padding(32.dp)
                    ) {
                        CircularWavyProgressIndicator()
                    }
                } else {
                    Row(modifier = modifier) {
                        ArtistPreview(
                            initialArtist = { initialArtist },
                            artistFormState = artistFormState,
                            seriesById = seriesById,
                            seriesImage = seriesImage,
                            merchById = merchById,
                            modifier = Modifier.weight(1f)
                        )

                        FieldsList(
                            fieldState = fieldState,
                            diff = formDiff,
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
        artistFormState: ArtistFormState?,
        seriesById: () -> Map<String, SeriesInfo>,
        merchById: () -> Map<String, MerchInfo>,
        seriesImage: (SeriesInfo) -> String?,
        modifier: Modifier = Modifier,
    ) {
        if (artistFormState != null) {
            Column(modifier.verticalScroll(rememberScrollState())) {
                ArtistForm(
                    initialArtist = initialArtist,
                    state = artistFormState,
                    errorState = rememberErrorState(artistFormState),
                    seriesById = seriesById,
                    seriesPredictions = { emptyFlow() },
                    merchById = merchById,
                    merchPredictions = { emptyFlow() },
                    seriesImage = seriesImage,
                    forceLocked = true,
                    showStatus = false,
                    showEditorNotes = false,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    @Composable
    private fun FieldsList(
        fieldState: FieldState,
        diff: ArtistEntryDiff?,
        modifier: Modifier = Modifier,
    ) {
        Column(modifier = modifier.fillMaxWidth()) {
            val formNotes = diff?.formNotes
            if (!formNotes.isNullOrBlank()) {
                OutlinedCard(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(Res.string.alley_edit_artist_form_merge_notes),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
                    )
                    Text(
                        text = formNotes,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

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

            ArtistField.entries.forEach { field ->
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
                        ArtistField.BOOTH -> diff?.booth
                        ArtistField.NAME -> diff?.name
                        ArtistField.SUMMARY -> diff?.summary
                        ArtistField.SOCIAL_LINKS_ADDED -> diff?.socialLinks?.added?.joinToString()
                        ArtistField.SOCIAL_LINKS_REMOVED -> diff?.socialLinks?.removed?.joinToString()
                        ArtistField.STORE_LINKS_ADDED -> diff?.storeLinks?.added?.joinToString()
                        ArtistField.STORE_LINKS_REMOVED -> diff?.storeLinks?.removed?.joinToString()
                        ArtistField.PORTFOLIO_LINKS_ADDED -> diff?.portfolioLinks?.added?.joinToString()
                        ArtistField.PORTFOLIO_LINKS_REMOVED -> diff?.portfolioLinks?.removed?.joinToString()
                        ArtistField.CATALOG_LINKS_ADDED -> diff?.catalogLinks?.added?.joinToString()
                        ArtistField.CATALOG_LINKS_REMOVED -> diff?.catalogLinks?.removed?.joinToString()
                        ArtistField.NOTES -> diff?.notes
                        ArtistField.COMMISSIONS_ADDED -> diff?.commissions?.added?.joinToString()
                        ArtistField.COMMISSIONS_REMOVED -> diff?.commissions?.removed?.joinToString()
                        ArtistField.SERIES_INFERRED_ADDED -> diff?.seriesInferred?.added?.joinToString()
                        ArtistField.SERIES_INFERRED_REMOVED -> diff?.seriesInferred?.removed?.joinToString()
                        ArtistField.SERIES_CONFIRMED_ADDED -> diff?.seriesConfirmed?.added?.joinToString()
                        ArtistField.SERIES_CONFIRMED_REMOVED -> diff?.seriesConfirmed?.removed?.joinToString()
                        ArtistField.MERCH_INFERRED_ADDED -> diff?.merchInferred?.added?.joinToString()
                        ArtistField.MERCH_INFERRED_REMOVED -> diff?.merchInferred?.removed?.joinToString()
                        ArtistField.MERCH_CONFIRMED_ADDED -> diff?.merchConfirmed?.added?.joinToString()
                        ArtistField.MERCH_CONFIRMED_REMOVED -> diff?.merchConfirmed?.removed?.joinToString()
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
    private class FieldState(private val map: SnapshotStateMap<ArtistField, Boolean>) {
        val keys get() = map.keys
        val values get() = map.values
        operator fun get(field: ArtistField) = map[field] ?: false
        operator fun set(field: ArtistField, checked: Boolean) = map.set(field, checked)

        fun applyChanges(
            base: ArtistDatabaseEntry.Impl,
            seriesById: Map<String, SeriesInfo>,
            merchById: Map<String, MerchInfo>,
            diff: ArtistEntryDiff,
        ): ArtistFormState {
            fun <T> applyDiff(
                base: T,
                diff: T?,
                field: ArtistField,
            ) = if (this[field]) {
                diff ?: base
            } else {
                base
            }

            fun applyDiff(
                base: List<String>,
                diff: ArtistEntryDiff.Diff?,
                added: ArtistField,
                removed: ArtistField,
            ): List<String> {
                val base = base.toMutableSet()
                if (this[removed]) base.removeAll(diff?.removed.orEmpty().toSet())
                if (this[added]) base.addAll(diff?.added.orEmpty().toSet())
                return base.toMutableList()
            }

            val artist = base.copy(
                booth = applyDiff(base.booth, diff.booth, ArtistField.BOOTH),
                name = applyDiff(base.name, diff.name, ArtistField.NAME),
                summary = applyDiff(base.summary, diff.summary, ArtistField.SUMMARY),
                socialLinks = applyDiff(
                    base.socialLinks,
                    diff.socialLinks,
                    ArtistField.SOCIAL_LINKS_ADDED,
                    ArtistField.SOCIAL_LINKS_REMOVED,
                ),
                storeLinks = applyDiff(
                    base.storeLinks,
                    diff.storeLinks,
                    ArtistField.STORE_LINKS_ADDED,
                    ArtistField.STORE_LINKS_REMOVED,
                ),
                portfolioLinks = applyDiff(
                    base.portfolioLinks,
                    diff.portfolioLinks,
                    ArtistField.PORTFOLIO_LINKS_ADDED,
                    ArtistField.PORTFOLIO_LINKS_REMOVED,
                ),
                catalogLinks = applyDiff(
                    base.catalogLinks,
                    diff.catalogLinks,
                    ArtistField.CATALOG_LINKS_ADDED,
                    ArtistField.CATALOG_LINKS_REMOVED,
                ),
                notes = diff.notes ?: base.notes,
                commissions = applyDiff(
                    base.commissions,
                    diff.commissions,
                    ArtistField.COMMISSIONS_ADDED,
                    ArtistField.COMMISSIONS_REMOVED,
                ),
                seriesInferred = applyDiff(
                    base.seriesInferred,
                    diff.seriesInferred,
                    ArtistField.SERIES_INFERRED_ADDED,
                    ArtistField.SERIES_INFERRED_REMOVED,
                ),
                seriesConfirmed = applyDiff(
                    base.seriesConfirmed,
                    diff.seriesConfirmed,
                    ArtistField.SERIES_CONFIRMED_ADDED,
                    ArtistField.SERIES_CONFIRMED_REMOVED,
                ),
                merchInferred = applyDiff(
                    base.merchInferred,
                    diff.merchInferred,
                    ArtistField.MERCH_INFERRED_ADDED,
                    ArtistField.MERCH_INFERRED_REMOVED,
                ),
                merchConfirmed = applyDiff(
                    base.merchConfirmed,
                    diff.merchConfirmed,
                    ArtistField.MERCH_CONFIRMED_ADDED,
                    ArtistField.MERCH_CONFIRMED_REMOVED,
                ),
            )
            return ArtistFormState().applyDatabaseEntry(
                artist = artist,
                seriesById = seriesById,
                merchById = merchById,
                mergeBehavior = ArtistFormState.MergeBehavior.REPLACE,
            )
        }
    }

    @Composable
    private fun rememberFieldState(diff: ArtistEntryDiff?): FieldState {
        val map = rememberSaveable(diff) {
            mutableStateMapOf<ArtistField, Boolean>().apply {
                if (diff == null) return@apply
                ArtistField.entries.forEach {
                    val include = when (it) {
                        ArtistField.BOOTH -> diff.booth != null
                        ArtistField.NAME -> diff.name != null
                        ArtistField.SUMMARY -> diff.summary != null
                        ArtistField.SOCIAL_LINKS_ADDED -> diff.socialLinks?.added != null
                        ArtistField.SOCIAL_LINKS_REMOVED -> diff.socialLinks?.removed != null
                        ArtistField.STORE_LINKS_ADDED -> diff.storeLinks?.added != null
                        ArtistField.STORE_LINKS_REMOVED -> diff.storeLinks?.removed != null
                        ArtistField.PORTFOLIO_LINKS_ADDED -> diff.portfolioLinks?.added != null
                        ArtistField.PORTFOLIO_LINKS_REMOVED -> diff.portfolioLinks?.removed != null
                        ArtistField.CATALOG_LINKS_ADDED -> diff.catalogLinks?.added != null
                        ArtistField.CATALOG_LINKS_REMOVED -> diff.catalogLinks?.removed != null
                        ArtistField.NOTES -> diff.notes != null
                        ArtistField.COMMISSIONS_ADDED -> diff.commissions?.added != null
                        ArtistField.COMMISSIONS_REMOVED -> diff.commissions?.removed != null
                        ArtistField.SERIES_INFERRED_ADDED -> diff.seriesInferred?.added != null
                        ArtistField.SERIES_INFERRED_REMOVED -> diff.seriesInferred?.removed != null
                        ArtistField.SERIES_CONFIRMED_ADDED -> diff.seriesConfirmed?.added != null
                        ArtistField.SERIES_CONFIRMED_REMOVED -> diff.seriesConfirmed?.removed != null
                        ArtistField.MERCH_INFERRED_ADDED -> diff.merchInferred?.added != null
                        ArtistField.MERCH_INFERRED_REMOVED -> diff.merchInferred?.removed != null
                        ArtistField.MERCH_CONFIRMED_ADDED -> diff.merchConfirmed?.added != null
                        ArtistField.MERCH_CONFIRMED_REMOVED -> diff.merchConfirmed?.removed != null
                    }
                    if (!include) return@forEach
                    this[it] = it.isRemoved
                }
            }
        }
        return remember(map) { FieldState(map) }
    }

    private enum class ArtistField(val label: StringResource) {
        BOOTH(Res.string.alley_edit_artist_field_label_booth),
        NAME(Res.string.alley_edit_artist_field_label_name),
        SUMMARY(Res.string.alley_edit_artist_field_label_summary),
        SOCIAL_LINKS_ADDED(Res.string.alley_edit_artist_field_label_social_links),
        SOCIAL_LINKS_REMOVED(Res.string.alley_edit_artist_field_label_social_links),
        STORE_LINKS_ADDED(Res.string.alley_edit_artist_field_label_store_links),
        STORE_LINKS_REMOVED(Res.string.alley_edit_artist_field_label_store_links),
        PORTFOLIO_LINKS_ADDED(Res.string.alley_edit_artist_field_label_portfolio_links),
        PORTFOLIO_LINKS_REMOVED(Res.string.alley_edit_artist_field_label_portfolio_links),
        CATALOG_LINKS_ADDED(Res.string.alley_edit_artist_field_label_catalog_links),
        CATALOG_LINKS_REMOVED(Res.string.alley_edit_artist_field_label_catalog_links),
        NOTES(Res.string.alley_edit_artist_field_label_notes),
        COMMISSIONS_ADDED(Res.string.alley_edit_artist_field_label_commissions),
        COMMISSIONS_REMOVED(Res.string.alley_edit_artist_field_label_commissions),
        SERIES_INFERRED_ADDED(Res.string.alley_edit_artist_field_label_series_inferred),
        SERIES_INFERRED_REMOVED(Res.string.alley_edit_artist_field_label_series_inferred),
        SERIES_CONFIRMED_ADDED(Res.string.alley_edit_artist_field_label_series_confirmed),
        SERIES_CONFIRMED_REMOVED(Res.string.alley_edit_artist_field_label_series_confirmed),
        MERCH_INFERRED_ADDED(Res.string.alley_edit_artist_field_label_merch_inferred),
        MERCH_INFERRED_REMOVED(Res.string.alley_edit_artist_field_label_merch_inferred),
        MERCH_CONFIRMED_ADDED(Res.string.alley_edit_artist_field_label_merch_confirmed),
        MERCH_CONFIRMED_REMOVED(Res.string.alley_edit_artist_field_label_merch_confirmed),
        ;

        val isRemoved: Boolean
            get() = when (this) {
                BOOTH,
                NAME,
                SUMMARY,
                SOCIAL_LINKS_ADDED,
                STORE_LINKS_ADDED,
                PORTFOLIO_LINKS_ADDED,
                CATALOG_LINKS_ADDED,
                NOTES,
                COMMISSIONS_ADDED,
                SERIES_INFERRED_ADDED,
                SERIES_CONFIRMED_ADDED,
                MERCH_INFERRED_ADDED,
                MERCH_CONFIRMED_ADDED,
                    -> false
                SOCIAL_LINKS_REMOVED,
                STORE_LINKS_REMOVED,
                PORTFOLIO_LINKS_REMOVED,
                CATALOG_LINKS_REMOVED,
                COMMISSIONS_REMOVED,
                SERIES_INFERRED_REMOVED,
                SERIES_CONFIRMED_REMOVED,
                MERCH_INFERRED_REMOVED,
                MERCH_CONFIRMED_REMOVED,
                    -> true
            }
    }
}
