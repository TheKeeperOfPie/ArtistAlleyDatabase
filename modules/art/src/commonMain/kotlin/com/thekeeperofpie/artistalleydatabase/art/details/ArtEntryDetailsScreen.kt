package com.thekeeperofpie.artistalleydatabase.art.details

import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.art.generated.resources.Res
import artistalleydatabase.modules.art.generated.resources.art_entry_characters_header
import artistalleydatabase.modules.art.generated.resources.art_entry_notes_header
import artistalleydatabase.modules.art.generated.resources.art_entry_series_header
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryComponent
import com.thekeeperofpie.artistalleydatabase.art.details.SourceDropdown.rememberState
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryFormSection
import com.thekeeperofpie.artistalleydatabase.entry.form.LongTextSection
import com.thekeeperofpie.artistalleydatabase.entry.form.MultiTextSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.animateEnterExit
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.renderInSharedTransitionScopeOverlay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

object ArtEntryDetailsScreen {

    @Composable
    operator fun invoke(
        artEntryComponent: ArtEntryComponent,
        onEvent: (Event) -> Unit,
    ) {
        val viewModel =
            viewModel { artEntryComponent.artEntryDetailsViewModel2(createSavedStateHandle()) }
        val scaffoldState = rememberBottomSheetScaffoldState()
        BottomSheetScaffold(sheetContent = {
            BottomSheet(
                state = viewModel.state,
                seriesPredictions = viewModel::series,
                characterPredictions = { _ -> viewModel.characterPredictions },
                bottomSheetState = scaffoldState.bottomSheetState,
                onEvent = onEvent,
                modifier = Modifier.weight(1f, fill = false)
            )
        }) {
            // TODO
        }
    }


    @Composable
    internal fun BottomSheet(
        state: State,
        seriesPredictions: suspend (String) -> Flow<List<EntryFormSection.MultiText.Entry>>,
        characterPredictions: suspend (String) -> Flow<List<EntryFormSection.MultiText.Entry>>,
        bottomSheetState: SheetState,
        onEvent: (Event) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val scrollState = rememberScrollState()
        val bottomSheetTargetValue = bottomSheetState.targetValue
        LaunchedEffect(bottomSheetTargetValue) {
            if (bottomSheetTargetValue == SheetValue.PartiallyExpanded) {
                scrollState.animateScrollTo(0)
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 1f)
                .animateEnterExit(
                    enter = slideInVertically { it * 2 },
                    // TODO: Exit doesn't work
                    exit = slideOutVertically { it * 2 },
                )
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            EntryForm2 {
                val focusRequester = remember { FocusRequester() }
                MultiTextSection(
                    state = state.series,
                    headerText = {
                        Text(
                            pluralStringResource(
                                Res.plurals.art_entry_series_header,
                                state.series.content.size,
                            )
                        )
                    },
                    focusRequester = focusRequester,
                    trailingIcon = { /* TODO */ null },
                    entryPredictions = seriesPredictions,
                    onNavigate = { onEvent(Event.Navigate(it)) },
                    onFocusChanged = { if (it) onEvent(Event.SectionFocused) },
                )

                MultiTextSection(
                    state = state.characters,
                    headerText = {
                        Text(
                            pluralStringResource(
                                Res.plurals.art_entry_characters_header,
                                state.characters.content.size,
                            )
                        )
                    },
                    focusRequester = focusRequester,
                    trailingIcon = { /* TODO */ null },
                    entryPredictions = characterPredictions,
                    onNavigate = { onEvent(Event.Navigate(it)) },
                    onFocusChanged = { if (it) onEvent(Event.SectionFocused) },
                )

                SourceDropdown(
                    state = rememberState(),
                    focusRequester = focusRequester,
                    onFocusChanged = { if (it) onEvent(Event.SectionFocused) },
                )

                LongTextSection(
                    state = state.notes,
                    headerText = { Text(stringResource(Res.string.art_entry_notes_header)) },
                    focusRequester = focusRequester,
                    onFocusChanged = { if (it) onEvent(Event.SectionFocused) },
                )
            }
        }
    }

    @Stable
    class State(
        val series: EntryFormSection.MultiText = EntryFormSection.MultiText(),
        val characters: EntryFormSection.MultiText = EntryFormSection.MultiText(),
        val notes: EntryFormSection.LongText = EntryFormSection.LongText(),
    ) {
        companion object {
            @Serializable
            data class SavedState(
                val series: EntryFormSection.MultiText.SavedState,
                val characters: EntryFormSection.MultiText.SavedState,
                val notes: EntryFormSection.LongText.SavedState,
            )

            val Saver = Saver<State, String>(
                save = {
                    Json.encodeToString(
                        SavedState(
                            series = it.series.toSavedState(),
                            characters = it.characters.toSavedState(),
                            notes = it.notes.toSavedState(),
                        )
                    )
                },
                restore = {
                    val savedState = Json.decodeFromString<SavedState>(it)
                    State(
                        series = savedState.series.toMultiText(),
                        characters = savedState.characters.toMultiText(),
                        notes = savedState.notes.toLongText(),
                    )
                },
            )
        }
    }

    sealed interface Event {
        data object SectionFocused : Event
        data class Navigate(val entry: EntryFormSection.MultiText.Entry) : Event
    }
}

@Preview(showBackground = true)
@Composable
private fun ArtEntryDetailsScreenPreview() {
    ArtEntryDetailsScreen.BottomSheet(
        state = ArtEntryDetailsScreen.State(),
        seriesPredictions = { flowOf(emptyList()) },
        characterPredictions = { flowOf(emptyList()) },
        bottomSheetState = rememberStandardBottomSheetState(),
        onEvent = {},
    )
}
