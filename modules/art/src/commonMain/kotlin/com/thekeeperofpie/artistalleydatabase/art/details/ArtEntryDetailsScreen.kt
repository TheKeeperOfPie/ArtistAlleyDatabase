package com.thekeeperofpie.artistalleydatabase.art.details

import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.LongTextSection
import com.thekeeperofpie.artistalleydatabase.entry.form.MultiTextSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.animateEnterExit
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.renderMaybeInSharedTransitionScopeOverlay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalSharedTransitionApi::class)
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
                series = viewModel.series,
                seriesPredictions = viewModel::series,
                characters = viewModel.characters,
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
        series: SnapshotStateList<EntryForm2.MultiTextState.Entry>,
        seriesPredictions: suspend (String) -> Flow<List<EntryForm2.MultiTextState.Entry>>,
        characters: SnapshotStateList<EntryForm2.MultiTextState.Entry>,
        characterPredictions: suspend (String) -> Flow<List<EntryForm2.MultiTextState.Entry>>,
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
                .renderMaybeInSharedTransitionScopeOverlay(zIndexInOverlay = 1f)
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
                                series.size,
                            )
                        )
                    },
                    focusRequester = focusRequester,
                    onFocusChanged = { if (it) onEvent(Event.SectionFocused) },
                    trailingIcon = { /* TODO */ null },
                    entryPredictions = seriesPredictions,
                    onNavigate = { onEvent(Event.Navigate(it)) },
                    items = series,
                    onItemCommitted = { series += it },
                    removeLastItem = { series.removeLastOrNull()?.text },
                )

                MultiTextSection(
                    state = state.characters,
                    headerText = {
                        Text(
                            pluralStringResource(
                                Res.plurals.art_entry_characters_header,
                                characters.size,
                            )
                        )
                    },
                    focusRequester = focusRequester,
                    onFocusChanged = { if (it) onEvent(Event.SectionFocused) },
                    trailingIcon = { /* TODO */ null },
                    entryPredictions = characterPredictions,
                    onNavigate = { onEvent(Event.Navigate(it)) },
                    items = characters,
                    onItemCommitted = { characters += it },
                    removeLastItem = { characters.removeLastOrNull()?.text },
                )

                SourceDropdown(
                    state = SourceDropdown.rememberState(),
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

    @Composable
    fun rememberState() = rememberSaveable(saver = State.Saver) { State() }

    @Stable
    class State(
        val series: EntryForm2.PendingTextState = EntryForm2.PendingTextState(),
        val characters: EntryForm2.PendingTextState = EntryForm2.PendingTextState(),
        val notes: EntryForm2.PendingTextState = EntryForm2.PendingTextState(),
    ) {
        object Saver : androidx.compose.runtime.saveable.Saver<State, Any> {
            override fun SaverScope.save(value: State): Any? {
                return listOf(
                    with(EntryForm2.PendingTextState.Saver) { save(value.series) },
                    with(EntryForm2.PendingTextState.Saver) { save(value.characters) },
                    with(EntryForm2.PendingTextState.Saver) { save(value.notes) },
                )
            }

            override fun restore(value: Any): State? {
                val (series, characters, notes) = value as List<*>
                return State(
                    series = with(EntryForm2.PendingTextState.Saver) { restore(series!!) }!!,
                    characters = with(EntryForm2.PendingTextState.Saver) { restore(characters!!) }!!,
                    notes = with(EntryForm2.PendingTextState.Saver) { restore(notes!!) }!!,
                )
            }
        }
    }

    sealed interface Event {
        data object SectionFocused : Event
        data class Navigate(val entry: EntryForm2.MultiTextState.Entry) : Event
    }
}

@Preview(showBackground = true)
@Composable
private fun ArtEntryDetailsScreenPreview() {
    ArtEntryDetailsScreen.BottomSheet(
        state = ArtEntryDetailsScreen.rememberState(),
        series = remember { SnapshotStateList() },
        seriesPredictions = { flowOf(emptyList()) },
        characters = remember { SnapshotStateList() },
        characterPredictions = { flowOf(emptyList()) },
        bottomSheetState = rememberStandardBottomSheetState(),
        onEvent = {},
    )
}
