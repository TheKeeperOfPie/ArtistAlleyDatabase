package com.thekeeperofpie.artistalleydatabase.alley.edit.series

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_open_link_content_description
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_action_save_content_description
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_edit_delete_action_cancel
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_edit_delete_action_confirm
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_edit_delete_action_delete
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_edit_delete_failed
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_edit_delete_failed_outdated
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_edit_delete_text
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_edit_delete_title
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_error_aniList_type
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_aniList_id
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_aniList_type
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_canonical
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_external_link
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_source_type
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_synonyms
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_title_english
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_title_native
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_title_preferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_title_romaji
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_uuid
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_wikipedia_id
import com.thekeeperofpie.artistalleydatabase.alley.edit.AlleyEditDestination
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ContentSavingBox
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.GenericExitDialog
import com.thekeeperofpie.artistalleydatabase.alley.models.AniListType
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.alley.series.textRes
import com.thekeeperofpie.artistalleydatabase.entry.form.DropdownSection
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.MultiTextSection
import com.thekeeperofpie.artistalleydatabase.entry.form.SingleTextSection
import com.thekeeperofpie.artistalleydatabase.entry.form.rememberLinkValidator
import com.thekeeperofpie.artistalleydatabase.entry.form.rememberLongValidator
import com.thekeeperofpie.artistalleydatabase.entry.form.rememberUuidValidator
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.SeriesSource
import com.thekeeperofpie.artistalleydatabase.utils.JobProgress
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.OneTimeEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

object SeriesEditScreen {

    @Composable
    operator fun invoke(
        seriesId: Uuid,
        initialInfo: AlleyEditDestination.SeriesEdit?,
        graph: ArtistAlleyEditGraph,
        onClickBack: (force: Boolean) -> Unit,
        viewModel: SeriesEditViewModel = viewModel {
            graph.seriesEditViewModelFactory.create(
                seriesId = seriesId,
                editInfo = initialInfo,
                savedStateHandle = createSavedStateHandle(),
            )
        },
    ) {
        val state = viewModel.state
        OneTimeEffect(state) {
            when (initialInfo?.seriesColumn) {
                null,
                SeriesColumn.IMAGE,
                    -> null
                SeriesColumn.CANONICAL -> state.id.focusRequester
                SeriesColumn.NOTES -> state.notes.focusRequester
                SeriesColumn.ANILIST_ID -> state.aniListId.focusRequester
                SeriesColumn.ANILIST_TYPE -> {
                    state.aniListType.expanded = true
                    state.aniListType.focusRequester
                }
                SeriesColumn.SOURCE_TYPE -> {
                    state.source.expanded = true
                    state.source.focusRequester
                }
                SeriesColumn.TITLE_ENGLISH -> state.titleEnglish.focusRequester
                SeriesColumn.TITLE_ROMAJI -> state.titleRomaji.focusRequester
                SeriesColumn.TITLE_NATIVE -> state.titleNative.focusRequester
                SeriesColumn.TITLE_PREFERRED -> state.titlePreferred.focusRequester
                SeriesColumn.SYNONYMS -> state.synonymsValue.focusRequester
                SeriesColumn.WIKIPEDIA_ID -> state.wikipediaId.focusRequester
                SeriesColumn.EXTERNAL_LINK -> state.link.focusRequester
                SeriesColumn.UUID -> state.uuid.focusRequester
            }?.requestFocus()
        }

        SeriesEditScreen(
            state = viewModel.state,
            onClickBack = onClickBack,
            onConfirmDelete = viewModel::onConfirmDelete.takeIf { viewModel.initialSeries != null },
            onClickSave = viewModel::onClickSave,
        )
    }

    @Composable
    operator fun invoke(
        state: State,
        onClickBack: (force: Boolean) -> Unit,
        onConfirmDelete: (() -> Unit)? = null,
        onClickSave: () -> Unit,
    ) {
        val snackbarHostState = remember { SnackbarHostState() }
        LaunchedEffect(Unit) {
            state.savingState.collectLatest {
                if (it is JobProgress.Finished.Result<BackendRequest.SeriesSave.Response>) {
                    when (val result = it.value) {
                        is BackendRequest.SeriesSave.Response.Failed ->
                            snackbarHostState.showSnackbar(message = result.errorMessage)
                        is BackendRequest.SeriesSave.Response.Outdated -> {
                            // TODO
                        }
                        BackendRequest.SeriesSave.Response.Success -> {
                            state.savingState.value = JobProgress.Idle()
                            onClickBack(true)
                        }
                    }
                }
            }
        }

        DeleteProgressEffect(state.deleteProgress, snackbarHostState, onClickBack)

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(state.id.value.text.toString()) },
                    navigationIcon = { ArrowBackIconButton(onClick = { onClickBack(true) }) },
                    actions = {
                        if (onConfirmDelete != null) {
                            DeleteButton(onConfirmDelete = onConfirmDelete)
                        }
                        IconButton(onClick = onClickSave) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = stringResource(Res.string.alley_edit_series_action_save_content_description),
                            )
                        }
                    },
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { scaffoldPadding ->
            val jobProgress by state.savingState.collectAsStateWithLifecycle()
            ContentSavingBox(
                saving = jobProgress is JobProgress.Loading,
                modifier = Modifier.padding(scaffoldPadding)
            ) {
                Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxWidth()) {
                    EntryForm2(
                        focusState = EntryForm2.rememberFocusState(
                            listOf(
                                state.id,
                                state.uuid,
                                state.aniListId,
                                state.aniListType,
                                state.wikipediaId,
                                state.source,
                                state.titleEnglish,
                                state.titleRomaji,
                                state.titleNative,
                                state.titlePreferred,
                                state.synonymsValue,
                                state.link,
                                state.notes,
                            )
                        ),
                        modifier = Modifier.width(600.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        SingleTextSection(
                            state = state.id,
                            headerText = { Text(stringResource(Res.string.alley_edit_series_header_canonical)) },
                        )

                        val uuidErrorMessage by rememberUuidValidator(state.uuid)
                        SingleTextSection(
                            state = state.uuid,
                            headerText = { Text(stringResource(Res.string.alley_edit_series_header_uuid)) },
                            errorText = { uuidErrorMessage },
                        )

                        val aniListIdErrorMessage by rememberLongValidator(state.aniListId)
                        SingleTextSection(
                            state = state.aniListId,
                            headerText = { Text(stringResource(Res.string.alley_edit_series_header_aniList_id)) },
                            errorText = { aniListIdErrorMessage },
                        )

                        // TODO: Previous/next focus
                        val aniListTypeErrorMessage by rememberAniListTypeValidator(
                            aniListIdState = state.aniListId,
                            aniListTypeState = state.aniListType,
                        )
                        DropdownSection(
                            state = state.aniListType,
                            headerText = {
                                Text(stringResource(Res.string.alley_edit_series_header_aniList_type))
                            },
                            options = AniListType.entries,
                            optionToText = { stringResource(it.textRes) },
                            errorText = { aniListTypeErrorMessage },
                        )

                        val wikipediaIdErrorMessage by rememberLongValidator(state.wikipediaId)
                        SingleTextSection(
                            state = state.wikipediaId,
                            headerText = { Text(stringResource(Res.string.alley_edit_series_header_wikipedia_id)) },
                            errorText = { wikipediaIdErrorMessage },
                        )
                        DropdownSection(
                            state = state.source,
                            headerText = {
                                Text(stringResource(Res.string.alley_edit_series_header_source_type))
                            },
                            options = SeriesSource.entries,
                            optionToText = { stringResource(it.textRes) },
                        )
                        SingleTextSection(
                            state = state.titleEnglish,
                            headerText = { Text(stringResource(Res.string.alley_edit_series_header_title_english)) },
                        )
                        SingleTextSection(
                            state = state.titleRomaji,
                            headerText = { Text(stringResource(Res.string.alley_edit_series_header_title_romaji)) },
                        )
                        SingleTextSection(
                            state = state.titleNative,
                            headerText = { Text(stringResource(Res.string.alley_edit_series_header_title_native)) },
                        )
                        SingleTextSection(
                            state = state.titlePreferred,
                            headerText = { Text(stringResource(Res.string.alley_edit_series_header_title_preferred)) },
                        )
                        MultiTextSection(
                            state = state.synonymsValue,
                            headerText = { Text(stringResource(Res.string.alley_edit_series_header_synonyms)) },
                            items = state.synonyms,
                            onItemCommitted = {
                                state.synonyms.add(it)
                                state.synonymsValue.value.clearText()
                            },
                            removeLastItem = { state.synonyms.removeLastOrNull() },
                            item = { _, synonym ->
                                Text(
                                    text = synonym,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            },
                        )

                        val linkErrorMessage by rememberLinkValidator(state.link)
                        SingleTextSection(
                            state = state.link,
                            headerText = { Text(stringResource(Res.string.alley_edit_series_header_external_link)) },
                            trailingIcon = {
                                AnimatedVisibility(visible = linkErrorMessage == null) {
                                    val uriHandler = LocalUriHandler.current
                                    IconButton(onClick = {
                                        try {
                                            uriHandler.openUri(state.link.value.text.toString())
                                        } catch (_: Throwable) {
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Default.OpenInNew,
                                            contentDescription = stringResource(Res.string.alley_edit_open_link_content_description),
                                        )
                                    }
                                }
                            },
                            errorText = { linkErrorMessage },
                        )
                        SingleTextSection(
                            state = state.notes,
                            headerText = { Text(stringResource(Res.string.alley_edit_series_header_notes)) },
                        )
                    }
                }
            }

            GenericExitDialog(
                onClickBack = { onClickBack(true) },
                onClickSave = onClickSave,
            )
        }
    }

    @Composable
    private fun rememberAniListTypeValidator(
        aniListIdState: EntryForm2.SingleTextState,
        aniListTypeState: EntryForm2.DropdownState,
    ): androidx.compose.runtime.State<String?> {
        val errorMessage = stringResource(Res.string.alley_edit_series_error_aniList_type)
        return remember(aniListIdState, aniListTypeState, errorMessage) {
            derivedStateOf {
                val id = aniListIdState.value.text
                if (id.isBlank() && aniListTypeState.selectedIndex != AniListType.NONE.ordinal) {
                    errorMessage
                } else {
                    null
                }
            }
        }
    }

    @Composable
    private fun DeleteButton(onConfirmDelete: () -> Unit, modifier: Modifier = Modifier) {
        var showDialog by remember { mutableStateOf(false) }

        IconButton(onClick = { showDialog = true }, modifier = modifier) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(Res.string.alley_edit_series_edit_delete_action_delete),
            )
        }

        var loading by remember { mutableStateOf(false) }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(stringResource(Res.string.alley_edit_series_edit_delete_title)) },
                text = { Text(stringResource(Res.string.alley_edit_series_edit_delete_text)) },
                confirmButton = {
                    val countdown by produceState(5) {
                        (4 downTo 0).forEach {
                            delay(1.seconds)
                            value = it
                        }
                    }
                    TextButton(
                        onClick = {
                            if (countdown <= 0) {
                                loading = true
                                onConfirmDelete()
                            }
                        },
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val textAlpha by animateFloatAsState(if (countdown <= 0) 1f else 0f)
                            Text(
                                text = stringResource(Res.string.alley_edit_series_edit_delete_action_confirm),
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
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(stringResource(Res.string.alley_edit_series_edit_delete_action_cancel))
                    }
                },
            )
        }
    }

    @Composable
    fun DeleteProgressEffect(
        deleteProgress: StateFlow<JobProgress<BackendRequest.SeriesDelete.Response>>,
        snackbarHostState: SnackbarHostState,
        onClickBack: (force: Boolean) -> Unit,
    ) {
        LaunchedEffect(deleteProgress) {
            deleteProgress.collectLatest {
                when (it) {
                    is JobProgress.Finished.Result<BackendRequest.SeriesDelete.Response> ->
                        when (it.value) {
                            is BackendRequest.SeriesDelete.Response.Failed -> snackbarHostState.showSnackbar(
                                message = getString(Res.string.alley_edit_series_edit_delete_failed)
                            )
                            is BackendRequest.SeriesDelete.Response.Outdated -> snackbarHostState.showSnackbar(
                                message = getString(Res.string.alley_edit_series_edit_delete_failed_outdated)
                            )
                            BackendRequest.SeriesDelete.Response.Success -> onClickBack(true)
                        }
                    is JobProgress.Finished.UnhandledError<*> ->
                        snackbarHostState.showSnackbar(message = getString(Res.string.alley_edit_series_edit_delete_failed))
                    is JobProgress.Idle<*>,
                    is JobProgress.Loading<*>,
                        -> Unit
                }
            }
        }
    }

    @Stable
    class State(
        val id: EntryForm2.SingleTextState,
        val uuid: EntryForm2.SingleTextState,
        val aniListId: EntryForm2.SingleTextState,
        val aniListType: EntryForm2.DropdownState,
        val wikipediaId: EntryForm2.SingleTextState,
        val source: EntryForm2.DropdownState,
        val titleEnglish: EntryForm2.SingleTextState,
        val titleRomaji: EntryForm2.SingleTextState,
        val titleNative: EntryForm2.SingleTextState,
        val titlePreferred: EntryForm2.SingleTextState,
        val synonyms: SnapshotStateList<String>,
        val synonymsValue: EntryForm2.SingleTextState,
        val link: EntryForm2.SingleTextState,
        val notes: EntryForm2.SingleTextState,
        val savingState: MutableStateFlow<JobProgress<BackendRequest.SeriesSave.Response>>,
        val deleteProgress: StateFlow<JobProgress<BackendRequest.SeriesDelete.Response>>,
    )
}
