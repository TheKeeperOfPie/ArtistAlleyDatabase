package com.thekeeperofpie.artistalleydatabase.art.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.anilist.AniListAutocompleter2
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDetailsDao
import kotlinx.coroutines.flow.SharingStarted
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

    val state by savedStateHandle.saveable(saver = ArtEntryDetailsScreen.State.Saver) {
        ArtEntryDetailsScreen.State()
    }

    val characterPredictions = autocompleter.characters(
        charactersState = state.characters,
        seriesState = state.series,
        artEntryDao::queryCharacters,
    ).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    suspend fun series(query: String) =
        autocompleter.series(query, artEntryDao::querySeries)
}
