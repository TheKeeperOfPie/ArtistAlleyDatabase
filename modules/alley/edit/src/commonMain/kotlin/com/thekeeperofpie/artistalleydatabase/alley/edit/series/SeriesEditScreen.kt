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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_action_save_content_description
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
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.GenericExitDialog
import com.thekeeperofpie.artistalleydatabase.alley.series.textRes
import com.thekeeperofpie.artistalleydatabase.entry.form.DropdownSection
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.SingleTextSection
import com.thekeeperofpie.artistalleydatabase.entry.form.rememberLinkValidator
import com.thekeeperofpie.artistalleydatabase.entry.form.rememberLongValidator
import com.thekeeperofpie.artistalleydatabase.entry.form.rememberUuidValidator
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.SeriesSource
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

object SeriesEditScreen {

    @Composable
    operator fun invoke(
        seriesId: Uuid,
        initialInfo: SeriesInfo?,
        graph: ArtistAlleyEditGraph,
        onClickBack: (force: Boolean) -> Unit,
        viewModel: SeriesEditViewModel = viewModel {
            graph.seriesEditViewModelFactory.create(
                seriesId = seriesId,
                series = initialInfo,
                savedStateHandle = createSavedStateHandle(),
            )
        },
    ) {
        SeriesEditScreen(
            state = viewModel.state,
            mode = viewModel.mode,
            onClickBack = onClickBack,
            onClickSave = viewModel::onClickSave,
        )
    }

    @Composable
    operator fun invoke(
        state: State,
        mode: Mode,
        onClickBack: (force: Boolean) -> Unit,
        onClickSave: () -> Unit,
    ) {
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
        ) { scaffoldPadding ->
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier.fillMaxWidth()
                    .padding(scaffoldPadding)
            ) {
                EntryForm2(modifier = Modifier.width(600.dp)) {
                    SingleTextSection(
                        state = state.id,
                        title = Res.string.alley_edit_series_header_canonical,
                        previousFocus = null,
                        nextFocus = state.uuid.focusRequester,
                    )

                    SingleTextSection(
                        state = state.uuid,
                        title = Res.string.alley_edit_series_header_uuid,
                        previousFocus = state.id.focusRequester,
                        nextFocus = state.aniListId.focusRequester,
                        errorValidation = rememberUuidValidator(),
                    )
                    SingleTextSection(
                        state = state.aniListId,
                        title = Res.string.alley_edit_series_header_aniList_id,
                        previousFocus = state.uuid.focusRequester,
                        nextFocus = state.aniListType.focusRequester,
                        errorValidation = rememberLongValidator(),
                    )
                    SingleTextSection(
                        state = state.aniListType,
                        title = Res.string.alley_edit_series_header_aniList_type,
                        previousFocus = state.aniListId.focusRequester,
                        nextFocus = state.wikipediaId.focusRequester,
                    )
                    SingleTextSection(
                        state = state.wikipediaId,
                        title = Res.string.alley_edit_series_header_wikipedia_id,
                        previousFocus = state.aniListType.focusRequester,
                        nextFocus = null, // TODO
                        errorValidation = rememberLongValidator(),
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
                        title = Res.string.alley_edit_series_header_title_english,
                        previousFocus = null,
                        nextFocus = state.titlePreferred.focusRequester,
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
                    SingleTextSection(
                        state = state.link,
                        title = Res.string.alley_edit_series_header_external_link,
                        previousFocus = state.titlePreferred.focusRequester,
                        nextFocus = state.notes.focusRequester,
                        errorValidation = rememberLinkValidator(),
                    )
                    SingleTextSection(
                        state = state.notes,
                        title = Res.string.alley_edit_series_header_notes,
                        previousFocus = state.link.focusRequester,
                        nextFocus = null,
                    )
                }
            }

            GenericExitDialog(
                onClickBack = { onClickBack(true) },
                onClickSave = onClickSave,
            )
        }
    }

    enum class Mode {
        ADD, EDIT
    }

    @Stable
    class State(
        val id: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val uuid: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val aniListId: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val aniListType: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val wikipediaId: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val source: EntryForm2.DropdownState = EntryForm2.DropdownState(),
        val titleEnglish: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val titleRomaji: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val titleNative: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val titlePreferred: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val link: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val notes: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
    )
}
