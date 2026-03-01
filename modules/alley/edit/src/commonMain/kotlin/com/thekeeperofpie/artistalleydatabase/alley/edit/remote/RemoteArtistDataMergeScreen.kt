package com.thekeeperofpie.artistalleydatabase.alley.edit.remote

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
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_booth
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_commissions
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_name
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_portfolio_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_social_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_store_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_summary
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_form_merge_action_save
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_form_merge_outdated
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_form_merge_title_booth_name
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_form_merge_title_name
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_remote_artist_data_merge_timestamp_prefix
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistForm
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistFormState
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.rememberErrorState
import com.thekeeperofpie.artistalleydatabase.alley.edit.form.FormMergeBehavior
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ContentSavingBox
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ScrollableSideBySide
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistRemoteEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.HistoryListDiff
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.alley.ui.theme.AlleyTheme
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.GenericTaskErrorEffect
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalDateTimeFormatter
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationResults
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal object RemoteArtistDataMergeScreen {

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        id: ArtistRemoteEntry.Id,
        graph: ArtistAlleyEditGraph,
        onClickBack: (force: Boolean) -> Unit,
        viewModel: RemoteArtistDataMergeViewModel = viewModel {
            graph.remoteArtistDataMergeViewModelFactory.create(
                dataYear = dataYear,
                id = id,
                savedStateHandle = createSavedStateHandle(),
            )
        },
    ) {
        val entry by viewModel.currentEntry.collectAsStateWithLifecycle()
        val entryInfo by viewModel.entryInfo.collectAsStateWithLifecycle()
        val seriesById by viewModel.tagAutocomplete.seriesById.collectAsStateWithLifecycle()
        val merchById by viewModel.tagAutocomplete.merchById.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }
        val saveTaskState = viewModel.saveTaskState
        RemoteArtistDataMergeScreen(
            dataYear = dataYear,
            entry = entry,
            entryInfo = entryInfo?.second,
            snackbarHostState = snackbarHostState,
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
                        is BackendRequest.SaveRemoteArtistData.Response.Failed -> {
                            snackbarHostState.showSnackbar(message = result.errorMessage)
                            saveTaskState.clearResult()
                        }
                        is BackendRequest.SaveRemoteArtistData.Response.Outdated -> {
                            snackbarHostState.showSnackbar(message = getString(Res.string.alley_edit_artist_form_merge_outdated))
                            saveTaskState.clearResult()
                        }
                        is BackendRequest.SaveRemoteArtistData.Response.Success -> {
                            saveTaskState.clearResult()
                            onClickBack(true)
                        }
                    }
                }
        }
    }

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        entry: ArtistRemoteEntry?,
        entryInfo: EntryInfo?,
        snackbarHostState: SnackbarHostState,
        saving: () -> Boolean,
        seriesById: () -> Map<String, SeriesInfo>,
        merchById: () -> Map<String, MerchInfo>,
        seriesImage: (SeriesInfo) -> String?,
        onClickBack: (force: Boolean) -> Unit,
        onClickSave: (List<EditImage>, ArtistDatabaseEntry.Impl) -> Unit,
    ) {
        val initialArtist = entryInfo?.artist
        val entryDiff = entryInfo?.diff
        val fieldState = rememberFieldState(initialArtist, entryDiff)
        val seriesByIdMap = seriesById()
        val merchByIdMap = merchById()
        val artistFormState by remember(
            entryInfo,
            fieldState,
            seriesByIdMap,
            merchByIdMap
        ) {
            derivedStateOf {
                entryDiff ?: return@derivedStateOf null
                fieldState.applyChanges(
                    base = initialArtist ?: ArtistFormState(
                        entryInfo.artistId ?: Uuid.random()
                    ).captureDatabaseEntry(dataYear, false).second,
                    seriesById = seriesByIdMap,
                    merchById = merchByIdMap,
                    diff = entryDiff,
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
                if (artistFormState == null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = modifier.padding(32.dp)
                    ) {
                        CircularWavyProgressIndicator()
                    }
                } else {
                    val fieldsList = remember {
                        movableContentOf {
                            FieldsList(
                                fieldState = fieldState,
                                diff = entryDiff,
                            )
                        }
                    }
                    ScrollableSideBySide(
                        showSecondary = { true },
                        primary = {
                            ArtistPreview(
                                initialArtist = { initialArtist },
                                artistFormState = artistFormState,
                                timestamp = entry?.timestamp,
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
    private fun ArtistPreview(
        initialArtist: () -> ArtistDatabaseEntry.Impl?,
        artistFormState: ArtistFormState?,
        timestamp: Instant?,
        seriesById: () -> Map<String, SeriesInfo>,
        merchById: () -> Map<String, MerchInfo>,
        seriesImage: (SeriesInfo) -> String?,
        modifier: Modifier = Modifier,
    ) {
        if (artistFormState != null) {
            Column {
                if (timestamp != null) {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = LocalContentColor.current.copy(alpha = 0.6f))) {
                                    append(stringResource(Res.string.alley_edit_remote_artist_data_merge_timestamp_prefix))
                                }
                                append(' ')
                                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                    append(
                                        LocalDateTimeFormatter.current
                                            .formatDateTime(timestamp)
                                    )
                                }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
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
                    modifier = modifier.fillMaxWidth(),
                )
            }
        }
    }

    @Composable
    private fun FieldsList(
        fieldState: FieldState,
        diff: RemoteArtistDataDiff?,
        modifier: Modifier = Modifier,
    ) {
        Column(modifier = modifier.fillMaxWidth()) {
            val groupState by remember(fieldState) {
                derivedStateOf {
                    when {
                        fieldState.values.all { it } -> ToggleableState.On
                        fieldState.values.any { it } -> ToggleableState.Indeterminate
                        else -> ToggleableState.Off
                    }
                }
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

            ArtistDataField.entries.forEach { field ->
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
                        ArtistDataField.BOOTH -> diff?.booth
                        ArtistDataField.NAME -> diff?.name
                        ArtistDataField.SUMMARY -> diff?.summary
                        ArtistDataField.SOCIAL_LINKS_ADDED -> diff?.socialLinks?.added?.joinToString()
                        ArtistDataField.SOCIAL_LINKS_REMOVED -> diff?.socialLinks?.deleted?.joinToString()
                        ArtistDataField.STORE_LINKS_ADDED -> diff?.storeLinks?.added?.joinToString()
                        ArtistDataField.STORE_LINKS_REMOVED -> diff?.storeLinks?.deleted?.joinToString()
                        ArtistDataField.PORTFOLIO_LINKS_ADDED -> diff?.portfolioLinks?.added?.joinToString()
                        ArtistDataField.PORTFOLIO_LINKS_REMOVED -> diff?.portfolioLinks?.deleted?.joinToString()
                        ArtistDataField.COMMISSIONS_ADDED -> diff?.commissions?.added?.joinToString()
                        ArtistDataField.COMMISSIONS_REMOVED -> diff?.commissions?.deleted?.joinToString()
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
    private class FieldState(private val map: SnapshotStateMap<ArtistDataField, Boolean>) {
        val keys get() = map.keys
        val values get() = map.values
        operator fun get(field: ArtistDataField) = map[field] ?: false
        operator fun set(field: ArtistDataField, checked: Boolean) = map.set(field, checked)

        fun applyChanges(
            base: ArtistDatabaseEntry.Impl,
            seriesById: Map<String, SeriesInfo>,
            merchById: Map<String, MerchInfo>,
            diff: RemoteArtistDataDiff,
        ): ArtistFormState {
            fun <T> applyDiff(
                base: T,
                diff: T?,
                field: ArtistDataField,
            ) = if (this[field]) {
                diff ?: base
            } else {
                base
            }

            fun applyDiff(
                base: List<String>,
                diff: HistoryListDiff?,
                added: ArtistDataField,
                deleted: ArtistDataField,
            ): List<String> {
                val base = base.toMutableSet()
                if (this[deleted]) base.removeAll(diff?.deleted.orEmpty().toSet())
                if (this[added]) base.addAll(diff?.added.orEmpty().toSet())
                return base.toMutableList()
            }

            val artist = base.copy(
                id = base.id,
                booth = applyDiff(base.booth, diff.booth, ArtistDataField.BOOTH),
                name = applyDiff(base.name, diff.name, ArtistDataField.NAME),
                summary = applyDiff(base.summary, diff.summary, ArtistDataField.SUMMARY),
                socialLinks = applyDiff(
                    base.socialLinks,
                    diff.socialLinks,
                    ArtistDataField.SOCIAL_LINKS_ADDED,
                    ArtistDataField.SOCIAL_LINKS_REMOVED,
                ),
                storeLinks = applyDiff(
                    base.storeLinks,
                    diff.storeLinks,
                    ArtistDataField.STORE_LINKS_ADDED,
                    ArtistDataField.STORE_LINKS_REMOVED,
                ),
                portfolioLinks = applyDiff(
                    base.portfolioLinks,
                    diff.portfolioLinks,
                    ArtistDataField.PORTFOLIO_LINKS_ADDED,
                    ArtistDataField.PORTFOLIO_LINKS_REMOVED,
                ),
                commissions = applyDiff(
                    base.commissions,
                    diff.commissions,
                    ArtistDataField.COMMISSIONS_ADDED,
                    ArtistDataField.COMMISSIONS_REMOVED,
                ),
            )
            return ArtistFormState().applyDatabaseEntry(
                artist = artist,
                seriesById = seriesById,
                merchById = merchById,
                mergeBehavior = FormMergeBehavior.REPLACE,
            )
        }
    }

    @Composable
    private fun rememberFieldState(initialArtist: ArtistDatabaseEntry.Impl?, diff: RemoteArtistDataDiff?): FieldState {
        val map = rememberSaveable(initialArtist, diff) {
            mutableStateMapOf<ArtistDataField, Boolean>().apply {
                if (diff == null) return@apply
                ArtistDataField.entries.forEach {
                    val include = when (it) {
                        ArtistDataField.BOOTH -> diff.booth != null
                        ArtistDataField.NAME -> diff.name != null
                        ArtistDataField.SUMMARY -> diff.summary != null
                        ArtistDataField.SOCIAL_LINKS_ADDED -> diff.socialLinks?.added != null
                        ArtistDataField.SOCIAL_LINKS_REMOVED -> diff.socialLinks?.deleted != null
                        ArtistDataField.STORE_LINKS_ADDED -> diff.storeLinks?.added != null
                        ArtistDataField.STORE_LINKS_REMOVED -> diff.storeLinks?.deleted != null
                        ArtistDataField.PORTFOLIO_LINKS_ADDED -> diff.portfolioLinks?.added != null
                        ArtistDataField.PORTFOLIO_LINKS_REMOVED -> diff.portfolioLinks?.deleted != null
                        ArtistDataField.COMMISSIONS_ADDED -> diff.commissions?.added != null
                        ArtistDataField.COMMISSIONS_REMOVED -> diff.commissions?.deleted != null
                    }
                    if (!include) return@forEach
                    this[it] = initialArtist == null || it.isRemoved
                }
            }
        }
        return remember(map) { FieldState(map) }
    }

    private enum class ArtistDataField(val label: StringResource) {
        BOOTH(Res.string.alley_edit_artist_field_label_booth),
        NAME(Res.string.alley_edit_artist_field_label_name),
        SUMMARY(Res.string.alley_edit_artist_field_label_summary),
        SOCIAL_LINKS_ADDED(Res.string.alley_edit_artist_field_label_social_links),
        SOCIAL_LINKS_REMOVED(Res.string.alley_edit_artist_field_label_social_links),
        STORE_LINKS_ADDED(Res.string.alley_edit_artist_field_label_store_links),
        STORE_LINKS_REMOVED(Res.string.alley_edit_artist_field_label_store_links),
        PORTFOLIO_LINKS_ADDED(Res.string.alley_edit_artist_field_label_portfolio_links),
        PORTFOLIO_LINKS_REMOVED(Res.string.alley_edit_artist_field_label_portfolio_links),
        COMMISSIONS_ADDED(Res.string.alley_edit_artist_field_label_commissions),
        COMMISSIONS_REMOVED(Res.string.alley_edit_artist_field_label_commissions),
        ;

        val isRemoved: Boolean
            get() = when (this) {
                BOOTH,
                NAME,
                SUMMARY,
                SOCIAL_LINKS_ADDED,
                STORE_LINKS_ADDED,
                PORTFOLIO_LINKS_ADDED,
                COMMISSIONS_ADDED,
                    -> false
                SOCIAL_LINKS_REMOVED,
                STORE_LINKS_REMOVED,
                PORTFOLIO_LINKS_REMOVED,
                COMMISSIONS_REMOVED,
                    -> true
            }
    }

    data class EntryInfo(
        val artistId: Uuid?,
        val artist: ArtistDatabaseEntry.Impl?,
        val previousEntry: ArtistRemoteEntry?,
        val diff: RemoteArtistDataDiff,
    )
}
