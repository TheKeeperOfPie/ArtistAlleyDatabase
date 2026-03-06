package com.thekeeperofpie.artistalleydatabase.alley.edit.remote

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
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
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_remote_artist_data_link_ignore
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_remote_artist_data_merge_artist_inference_failed
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_remote_artist_data_merge_timestamp_prefix
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_remote_artist_data_merge_which_artist_action_new_artist
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_remote_artist_data_merge_which_artist_manual_id_action_submit
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_remote_artist_data_merge_which_artist_manual_id_label
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_remote_artist_data_merge_which_artist_prompt
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistForm
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistFormState
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference.ArtistInference
import com.thekeeperofpie.artistalleydatabase.alley.edit.form.FormMergeBehavior
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ContentSavingBox
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ScrollableSideBySide
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkCategory
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistRemoteEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.HistoryListDiff
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.alley.ui.theme.AlleyTheme
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2.rememberFocusState
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.GenericTaskErrorEffect
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalDateTimeFormatter
import com.thekeeperofpie.artistalleydatabase.utils_compose.MinWidthTextField
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationResults
import kotlinx.coroutines.flow.collectLatest
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
        val confirmedArtistId by viewModel.confirmedArtistId.collectAsStateWithLifecycle()
        val inferredArtists by viewModel.inferredArtists.collectAsStateWithLifecycle()
        RemoteArtistDataMergeScreen(
            dataYear = dataYear,
            confirmedArtistId = { confirmedArtistId },
            entry = entry,
            entryInfo = entryInfo?.second,
            snackbarHostState = snackbarHostState,
            saving = { saveTaskState.showBlockingLoadingIndicator },
            seriesById = { seriesById },
            merchById = { merchById },
            seriesImage = viewModel::seriesImage,
            inferredArtists = { inferredArtists },
            onConfirmId = viewModel::onConfirmArtist,
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
        confirmedArtistId: () -> Uuid?,
        entry: ArtistRemoteEntry?,
        entryInfo: EntryInfo?,
        snackbarHostState: SnackbarHostState,
        saving: () -> Boolean,
        seriesById: () -> Map<String, SeriesInfo>,
        merchById: () -> Map<String, MerchInfo>,
        seriesImage: (SeriesInfo) -> String?,
        inferredArtists: () -> LoadingResult<List<ArtistInference.MatchResult>>,
        onConfirmId: (Uuid?) -> Unit,
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
            entryDiff,
            fieldState,
            seriesByIdMap,
            merchByIdMap,
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
                    navigationIcon = {
                        ArrowBackIconButton(onClick = {
                            if (confirmedArtistId() != null) {
                                onConfirmId(null)
                            } else {
                                onClickBack(true)
                            }
                        })
                    },
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
                saving = saving() || artistFormState == null,
                modifier = Modifier.fillMaxWidth().padding(scaffoldPadding)
            ) {
                val secondary = remember(fieldState, entry, initialArtist, entryDiff) {
                    movableContentOf {
                        if (initialArtist != null || confirmedArtistId() != null) {
                            FieldsList(
                                fieldState = fieldState,
                                diff = entryDiff,
                            )
                        } else {
                            RemoteDataSummary(
                                fieldState = fieldState,
                                previous = entryInfo?.previousEntry,
                                current = entry,
                            )
                        }
                    }
                }
                ScrollableSideBySide(
                    showSecondary = { true },
                    primary = {
                        if (initialArtist != null || confirmedArtistId() != null) {
                            ArtistPreview(
                                initialArtist = { initialArtist },
                                artistFormState = artistFormState,
                                timestamp = entry?.timestamp,
                            )
                        } else {
                            ConfirmArtistIdPrompt(
                                confirmedArtistId = confirmedArtistId,
                                inferredArtists = inferredArtists,
                                onConfirmId = onConfirmId,
                            )
                        }
                    },
                    secondary = { secondary() },
                    secondaryExpanded = { secondary() },
                )
            }
        }
    }

    @Composable
    private fun ConfirmArtistIdPrompt(
        confirmedArtistId: () -> Uuid?,
        inferredArtists: () -> LoadingResult<List<ArtistInference.MatchResult>>,
        onConfirmId: (Uuid) -> Unit,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .widthIn(min = 800.dp)
                .width(IntrinsicSize.Min)
                .padding(16.dp)
        ) {
            Text(stringResource(Res.string.alley_edit_remote_artist_data_merge_which_artist_prompt))

            val inferredArtists = inferredArtists()
            if (inferredArtists.loading) {
                CircularWavyProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (!inferredArtists.success) {
                Text(stringResource(Res.string.alley_edit_remote_artist_data_merge_artist_inference_failed))
            } else {
                inferredArtists.result?.forEach {
                    OutlinedCard(onClick = { onConfirmId(it.data.id) }) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(it.name)
                            Text(
                                text = it.data.id.toString(),
                                modifier = Modifier.padding(start = 16.dp)
                            )
                            Text(text = it.via, modifier = Modifier.padding(start = 16.dp))
                        }
                    }
                }

                val manualInput = rememberTextFieldState()
                LaunchedEffect(Unit) {
                    snapshotFlow { confirmedArtistId() }
                        .collectLatest {
                            manualInput.setTextAndPlaceCursorAtEnd(
                                it?.toString().orEmpty()
                            )
                        }
                }

                OutlinedTextField(
                    state = manualInput,
                    label = {
                        Text(stringResource(Res.string.alley_edit_remote_artist_data_merge_which_artist_manual_id_label))
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(
                        16.dp,
                        Alignment.CenterHorizontally
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilledTonalButton(onClick = {
                        Uuid.parseOrNull(manualInput.text.toString())?.let(onConfirmId)
                    }) {
                        Text(stringResource(Res.string.alley_edit_remote_artist_data_merge_which_artist_manual_id_action_submit))
                    }
                    FilledTonalButton(onClick = { onConfirmId(Uuid.random()) }) {
                        Text(stringResource(Res.string.alley_edit_remote_artist_data_merge_which_artist_action_new_artist))
                    }
                }
            }
        }
    }

    @Composable
    private fun RemoteDataSummary(
        fieldState: FieldState,
        previous: ArtistRemoteEntry?,
        current: ArtistRemoteEntry?,
    ) {
        val diff = remember(previous, current) {
            current ?: return@remember null
            RemoteArtistDataDiff.diff(
                artist = null,
                previousEntry = previous,
                currentEntry = current,
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            ArtistDataField.entries.forEach { field ->
                if (!fieldState.keys.contains(field)) return@forEach
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
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

            diff?.otherLinks?.added?.forEach {
                Text(it, Modifier.padding(16.dp))
            }
        }
    }

    @Composable
    private fun ArtistPreview(
        initialArtist: () -> ArtistDatabaseEntry.Impl?,
        artistFormState: ArtistFormState?,
        timestamp: Instant?,
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
                val focusState = rememberFocusState(
                    listOfNotNull(
                        artistFormState.editorState.id,
                        artistFormState.info.booth,
                        artistFormState.info.name,
                        artistFormState.info.summary,
                        artistFormState.links.stateSocialLinks,
                        artistFormState.links.stateStoreLinks,
                        artistFormState.links.statePortfolioLinks,
                        artistFormState.links.stateCommissions,
                    )
                )
                ArtistForm(
                    focusState = focusState,
                    initialArtist = initialArtist,
                    forceLocked = true,
                    modifier = modifier.fillMaxWidth()
                ) {
                    IdSection(state = artistFormState.editorState.id)
                    InfoSections(artistFormState.info)
                    SocialLinksSection(
                        state = artistFormState.links.stateSocialLinks,
                        links = artistFormState.links.socialLinks,
                    )
                    StoreLinksSection(
                        state = artistFormState.links.stateStoreLinks,
                        storeLinks = artistFormState.links.storeLinks,
                    )
                    PortfolioLinksSection(
                        state = artistFormState.links.statePortfolioLinks,
                        portfolioLinks = artistFormState.links.portfolioLinks,
                    )
                    CommissionsSection(
                        state = artistFormState.links.stateCommissions,
                        commissions = artistFormState.links.commissions,
                    )
                }
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

            val uriHandler = LocalUriHandler.current
            diff?.otherLinks?.added?.forEach { link ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                    ) {
                        MinWidthTextField(
                            value = stringResource(
                                fieldState[link]?.textRes
                                    ?: Res.string.alley_edit_remote_artist_data_link_ignore
                            ),
                            onValueChange = {},
                            readOnly = true,
                            minWidth = 100.dp,
                            isError = fieldState[link] == null,
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        )

                        ExposedDropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.alley_edit_remote_artist_data_link_ignore)) },
                                onClick = {
                                    fieldState.remove(link)
                                    expanded = false
                                },
                            )
                            listOf(
                                LinkCategory.SOCIALS,
                                LinkCategory.STORES,
                                LinkCategory.PORTFOLIOS,
                                LinkCategory.COMMISSIONS
                            ).forEach {
                                DropdownMenuItem(
                                    text = { Text(stringResource(it.textRes)) },
                                    onClick = {
                                        fieldState[link] = it
                                        expanded = false
                                    },
                                )
                            }
                        }
                    }
                    Text(
                        text = link,
                        color = if (fieldState[link] == null) {
                            AlleyTheme.colorScheme.negative
                        } else {
                            Color.Unspecified
                        },
                        modifier = Modifier.clickable {
                            try {
                                uriHandler.openUri(link)
                            } catch (_: Throwable) {
                            }
                        },
                    )
                }
            }
        }
    }

    @Stable
    private class FieldState(
        private val map: SnapshotStateMap<ArtistDataField, Boolean>,
        private val otherLinks: SnapshotStateMap<String, LinkCategory>,
    ) {
        val keys get() = map.keys
        val values get() = map.values
        operator fun get(field: ArtistDataField) = map[field] ?: false
        operator fun set(field: ArtistDataField, checked: Boolean) = map.set(field, checked)
        operator fun get(link: String) = otherLinks[link]
        operator fun set(link: String, category: LinkCategory) = otherLinks.set(link, category)
        fun remove(link: String) = otherLinks.remove(link)

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
                ) + otherLinks.filterValues { it == LinkCategory.SOCIALS || it == LinkCategory.SUPPORT }.keys,
                storeLinks = applyDiff(
                    base.storeLinks,
                    diff.storeLinks,
                    ArtistDataField.STORE_LINKS_ADDED,
                    ArtistDataField.STORE_LINKS_REMOVED,
                ) + otherLinks.filterValues { it == LinkCategory.STORES }.keys,
                portfolioLinks = applyDiff(
                    base.portfolioLinks,
                    diff.portfolioLinks,
                    ArtistDataField.PORTFOLIO_LINKS_ADDED,
                    ArtistDataField.PORTFOLIO_LINKS_REMOVED,
                ) + otherLinks.filterValues { it == LinkCategory.PORTFOLIOS }.keys,
                commissions = applyDiff(
                    base.commissions,
                    diff.commissions,
                    ArtistDataField.COMMISSIONS_ADDED,
                    ArtistDataField.COMMISSIONS_REMOVED,
                ) + otherLinks.filterValues { it == LinkCategory.COMMISSIONS }.keys,
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
    private fun rememberFieldState(
        initialArtist: ArtistDatabaseEntry.Impl?,
        diff: RemoteArtistDataDiff?,
    ): FieldState {
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
        val otherLinks = rememberSaveable(initialArtist, diff) {
            SnapshotStateMap<String, LinkCategory>()
        }
        return remember(map, otherLinks) { FieldState(map, otherLinks) }
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
