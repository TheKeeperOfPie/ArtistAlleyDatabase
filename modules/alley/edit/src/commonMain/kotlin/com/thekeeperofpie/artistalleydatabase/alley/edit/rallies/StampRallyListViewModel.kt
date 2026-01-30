package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistListScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistListSortBy
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.Fixed
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.milliseconds

@AssistedInject
class StampRallyListViewModel(
    private val stampRallyCache: StampRallyCache,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val query by savedStateHandle.saveable(saver = TextFieldState.Saver.Fixed) { TextFieldState() }
    private val dataYear = savedStateHandle.getMutableStateFlow("dataYear", DataYear.LATEST)
    private val sortBy = savedStateHandle.getMutableStateFlow("sortBy", StampRallyListSortBy.HOST)
    private val stampRallyEntries = dataYear.flatMapLatest(stampRallyCache::stampRallies)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val debouncedQuery = snapshotFlow { query.text.toString() }
        .debounce(500.milliseconds)

    private val entries = combine(stampRallyEntries, debouncedQuery, ::Pair)
        .mapLatest { (entries, query) ->
            if (query.isBlank()) {
                return@mapLatest entries
            }

            entries.filter {
                it.id.toString().contains(query, ignoreCase = true) ||
                        it.hostTable?.contains(query, ignoreCase = true) == true ||
                        it.fandom?.contains(query, ignoreCase = true) == true
            }
        }
        .flatMapLatest { artists ->
            sortBy.mapLatest {
                when (it) {
                    StampRallyListSortBy.HOST -> artists.sortedBy { it.hostTable }
                    StampRallyListSortBy.FANDOM -> artists.sortedWith(
                        compareBy(String.CASE_INSENSITIVE_ORDER) { it.fandom.orEmpty() }
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    internal val state = StampRallyListScreen.State(
        query = query,
        dataYear = dataYear,
        sortBy = sortBy,
        entries = entries,
    )

    fun refresh() = stampRallyCache.refresh()

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle): StampRallyListViewModel
    }
}
