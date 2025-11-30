package com.thekeeperofpie.artistalleydatabase.alley.edit.series

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_action_save_content_description
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_error_aniList_type
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_aniList_id
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_aniList_type
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_canonical
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_external_link
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_source_type
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
import com.thekeeperofpie.artistalleydatabase.alley.models.network.SeriesSave
import com.thekeeperofpie.artistalleydatabase.alley.series.textRes
import com.thekeeperofpie.artistalleydatabase.entry.form.DropdownSection
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.SingleTextSection
import com.thekeeperofpie.artistalleydatabase.entry.form.rememberLinkValidator
import com.thekeeperofpie.artistalleydatabase.entry.form.rememberLongValidator
import com.thekeeperofpie.artistalleydatabase.entry.form.rememberUuidValidator
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.SeriesSource
import com.thekeeperofpie.artistalleydatabase.utils.JobProgress
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.OneTimeEffect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
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
                SeriesColumn.WIKIPEDIA_ID -> state.wikipediaId.focusRequester
                SeriesColumn.EXTERNAL_LINK -> state.link.focusRequester
                SeriesColumn.UUID -> state.uuid.focusRequester
            }?.requestFocus()
        }

        SeriesEditScreen(
            state = viewModel.state,
            onClickBack = onClickBack,
            onClickSave = viewModel::onClickSave,
        )
    }

    @Composable
    operator fun invoke(
        state: State,
        onClickBack: (force: Boolean) -> Unit,
        onClickSave: () -> Unit,
    ) {
        val snackbarHostState = remember { SnackbarHostState() }
        LaunchedEffect(Unit) {
            state.savingState.collectLatest {
                if (it is JobProgress.Finished.Result<*>) {
                    when (val result = it.value as? SeriesSave.Response.Result) {
                        is SeriesSave.Response.Result.Failed ->
                            snackbarHostState.showSnackbar(message = result.throwable.message.orEmpty())
                        is SeriesSave.Response.Result.Outdated -> {
                            // TODO
                        }
                        SeriesSave.Response.Result.Success -> {
                            state.savingState.value = JobProgress.Idle
                            onClickBack(true)
                        }
                        null -> Unit
                    }
                }
            }
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(state.id.value.text.toString()) },
                    navigationIcon = { ArrowBackIconButton(onClick = { onClickBack(true) }) },
                    actions = {
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
                    EntryForm2(modifier = Modifier.width(600.dp)) {
                        SingleTextSection(
                            state = state.id,
                            title = Res.string.alley_edit_series_header_canonical,
                            previousFocus = null,
                            nextFocus = state.uuid.focusRequester,
                        )

                        val uuidErrorMessage by rememberUuidValidator(state.uuid)
                        SingleTextSection(
                            state = state.uuid,
                            title = Res.string.alley_edit_series_header_uuid,
                            previousFocus = state.id.focusRequester,
                            nextFocus = state.aniListId.focusRequester,
                            errorText = { uuidErrorMessage },
                        )

                        val aniListIdErrorMessage by rememberLongValidator(state.aniListId)
                        SingleTextSection(
                            state = state.aniListId,
                            title = Res.string.alley_edit_series_header_aniList_id,
                            previousFocus = state.uuid.focusRequester,
                            nextFocus = state.aniListType.focusRequester,
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
                            previousFocus = state.aniListId.focusRequester,
                            nextFocus = state.wikipediaId.focusRequester,
                        )

                        val wikipediaIdErrorMessage by rememberLongValidator(state.wikipediaId)
                        SingleTextSection(
                            state = state.wikipediaId,
                            title = Res.string.alley_edit_series_header_wikipedia_id,
                            previousFocus = state.aniListType.focusRequester,
                            nextFocus = state.source.focusRequester,
                            errorText = { wikipediaIdErrorMessage },
                        )
                        DropdownSection(
                            state = state.source,
                            headerText = {
                                Text(stringResource(Res.string.alley_edit_series_header_source_type))
                            },
                            options = SeriesSource.entries,
                            optionToText = { stringResource(it.textRes) },
                            previousFocus = state.wikipediaId.focusRequester,
                            nextFocus = state.titleEnglish.focusRequester,
                        )
                        SingleTextSection(
                            state = state.titleEnglish,
                            title = Res.string.alley_edit_series_header_title_english,
                            previousFocus = state.source.focusRequester,
                            nextFocus = state.titleRomaji.focusRequester,
                        )
                        SingleTextSection(
                            state = state.titleRomaji,
                            title = Res.string.alley_edit_series_header_title_romaji,
                            previousFocus = state.titleEnglish.focusRequester,
                            nextFocus = state.titleNative.focusRequester,
                        )
                        SingleTextSection(
                            state = state.titleNative,
                            title = Res.string.alley_edit_series_header_title_native,
                            previousFocus = state.titleRomaji.focusRequester,
                            nextFocus = state.titlePreferred.focusRequester,
                        )
                        SingleTextSection(
                            state = state.titlePreferred,
                            title = Res.string.alley_edit_series_header_title_preferred,
                            previousFocus = state.titleNative.focusRequester,
                            nextFocus = state.link.focusRequester,
                        )

                        val linkErrorMessage by rememberLinkValidator(state.link)
                        SingleTextSection(
                            state = state.link,
                            title = Res.string.alley_edit_series_header_external_link,
                            previousFocus = state.titlePreferred.focusRequester,
                            nextFocus = state.notes.focusRequester,
                            errorText = { linkErrorMessage },
                        )
                        SingleTextSection(
                            state = state.notes,
                            title = Res.string.alley_edit_series_header_notes,
                            previousFocus = state.link.focusRequester,
                            nextFocus = null,
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
        val link: EntryForm2.SingleTextState,
        val notes: EntryForm2.SingleTextState,
        val savingState: MutableStateFlow<JobProgress>,
    )
}
