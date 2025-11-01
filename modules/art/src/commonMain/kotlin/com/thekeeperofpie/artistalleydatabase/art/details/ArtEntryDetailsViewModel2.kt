package com.thekeeperofpie.artistalleydatabase.art.details

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.anilist.AniListAutocompleter2
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ReadOnlyStateFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.StateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class, SavedStateHandleSaveableApi::class)
@Inject
class ArtEntryDetailsViewModel2(
    private val artEntryDao: ArtEntryDetailsDao,
    internal val autocompleter: AniListAutocompleter2,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // TODO: Improving state saving to strip out derived fields
    val series by savedStateHandle.saveable(saver = StateUtils.snapshotListJsonSaver()) {
        SnapshotStateList<EntryForm2.MultiTextState.Entry>()
    }
    val characters by savedStateHandle.saveable(saver = StateUtils.snapshotListJsonSaver()) {
        SnapshotStateList<EntryForm2.MultiTextState.Entry>()
    }
    val artists by savedStateHandle.saveable(saver = StateUtils.snapshotListJsonSaver()) {
        SnapshotStateList<EntryForm2.MultiTextState.Entry>()
    }
    val tags by savedStateHandle.saveable(saver = StateUtils.snapshotListJsonSaver()) {
        SnapshotStateList<EntryForm2.MultiTextState.Entry>()
    }
    val sourceState by savedStateHandle.saveable(saver = SourceDropdown.State.Saver) {
        SourceDropdown.State()
    }

    val state by savedStateHandle.saveable(saver = ArtEntryDetailsScreen.State.Saver) {
        ArtEntryDetailsScreen.State()
    }

    val characterPredictions = autocompleter.characters(
        charactersState = state.characters,
        seriesContent = series,
        entryCharactersLocal = artEntryDao::queryCharacters,
    ).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val artistPredictions: StateFlow<List<EntryForm2.MultiTextState.Entry>> =
        ReadOnlyStateFlow(emptyList())

    val tagPredictions: StateFlow<List<EntryForm2.MultiTextState.Entry>> =
        ReadOnlyStateFlow(emptyList())

    suspend fun series(query: String) =
        autocompleter.series(query, artEntryDao::querySeries)
}
