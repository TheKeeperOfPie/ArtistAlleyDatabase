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
import artistalleydatabase.modules.art.generated.resources.art_entry_artists_header
import artistalleydatabase.modules.art.generated.resources.art_entry_characters_header
import artistalleydatabase.modules.art.generated.resources.art_entry_notes_header
import artistalleydatabase.modules.art.generated.resources.art_entry_series_header
import artistalleydatabase.modules.art.generated.resources.art_entry_tags_header
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryComponent
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.LongTextSection
import com.thekeeperofpie.artistalleydatabase.entry.form.MultiTextSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.animateEnterExit
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.renderMaybeInSharedTransitionScopeOverlay
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.ComposeSaver
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
        val viewModel = viewModel {
            artEntryComponent.artEntryDetailsViewModel2Factory.create(createSavedStateHandle())
        }
        val scaffoldState = rememberBottomSheetScaffoldState()
        BottomSheetScaffold(sheetContent = {
            BottomSheet(
                state = viewModel.state,
                series = viewModel.series,
                seriesPredictions = viewModel::series,
                characters = viewModel.characters,
                characterPredictions = { _ -> viewModel.characterPredictions },
                artists = viewModel.artists,
                artistPredictions = { _ -> viewModel.artistPredictions },
                tags = viewModel.tags,
                tagPredictions = { _ -> viewModel.tagPredictions },
                sourceState = viewModel.sourceState,
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
        artists: SnapshotStateList<EntryForm2.MultiTextState.Entry>,
        artistPredictions: suspend (String) -> Flow<List<EntryForm2.MultiTextState.Entry>>,
        tags: SnapshotStateList<EntryForm2.MultiTextState.Entry>,
        tagPredictions: suspend (String) -> Flow<List<EntryForm2.MultiTextState.Entry>>,
        sourceState: SourceDropdown.State,
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
                    trailingIcon = { /* TODO */ null },
                    entryPredictions = seriesPredictions,
                    onNavigate = { onEvent(Event.Navigate(it)) },
                    items = series,
                    onItemCommitted = { series += EntryForm2.MultiTextState.Entry.Custom(it) },
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
                    trailingIcon = { /* TODO */ null },
                    entryPredictions = characterPredictions,
                    onNavigate = { onEvent(Event.Navigate(it)) },
                    items = characters,
                    onItemCommitted = { characters += EntryForm2.MultiTextState.Entry.Custom(it) },
                    removeLastItem = { characters.removeLastOrNull()?.text },
                )

                MultiTextSection(
                    state = state.artists,
                    headerText = {
                        Text(
                            pluralStringResource(
                                Res.plurals.art_entry_artists_header,
                                artists.size,
                            )
                        )
                    },
                    trailingIcon = { null },
                    entryPredictions = artistPredictions,
                    onNavigate = { onEvent(Event.Navigate(it)) },
                    items = artists,
                    onItemCommitted = { artists += EntryForm2.MultiTextState.Entry.Custom(it) },
                    removeLastItem = { artists.removeLastOrNull()?.text },
                )

                SourceDropdown(state = sourceState)

                MultiTextSection(
                    state = state.tags,
                    headerText = {
                        Text(
                            pluralStringResource(
                                Res.plurals.art_entry_tags_header,
                                tags.size,
                            )
                        )
                    },
                    trailingIcon = { null },
                    entryPredictions = tagPredictions,
                    onNavigate = { onEvent(Event.Navigate(it)) },
                    items = tags,
                    onItemCommitted = { tags += EntryForm2.MultiTextState.Entry.Custom(it) },
                    removeLastItem = { tags.removeLastOrNull()?.text },
                )

                LongTextSection(
                    state = state.notes,
                    headerText = { Text(stringResource(Res.string.art_entry_notes_header)) },
                )
            }
        }
    }

    @Composable
    fun rememberState() = rememberSaveable(saver = State.Saver) { State() }

    @Stable
    class State(
        val series: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val characters: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val artists: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val tags: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val notes: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
    ) {
        object Saver : ComposeSaver<State, Any> {
            override fun SaverScope.save(value: State): List<Any?> {
                return listOf(
                    with(EntryForm2.SingleTextState.Saver) { save(value.series) },
                    with(EntryForm2.SingleTextState.Saver) { save(value.characters) },
                    with(EntryForm2.SingleTextState.Saver) { save(value.artists) },
                    with(EntryForm2.SingleTextState.Saver) { save(value.tags) },
                    with(EntryForm2.SingleTextState.Saver) { save(value.notes) },
                )
            }

            override fun restore(value: Any): State {
                val (series, characters, artists, tags, notes) = value as List<*>
                return State(
                    series = with(EntryForm2.SingleTextState.Saver) { restore(series!!) }!!,
                    characters = with(EntryForm2.SingleTextState.Saver) { restore(characters!!) }!!,
                    artists = with(EntryForm2.SingleTextState.Saver) { restore(artists!!) }!!,
                    tags = with(EntryForm2.SingleTextState.Saver) { restore(tags!!) }!!,
                    notes = with(EntryForm2.SingleTextState.Saver) { restore(notes!!) }!!,
                )
            }
        }
    }

    sealed interface Event {
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
        artists = remember { SnapshotStateList() },
        artistPredictions = { flowOf(emptyList()) },
        tags = remember { SnapshotStateList() },
        tagPredictions = { flowOf(emptyList()) },
        sourceState = remember { SourceDropdown.State() },
        bottomSheetState = rememberStandardBottomSheetState(),
        onEvent = {},
    )
}
