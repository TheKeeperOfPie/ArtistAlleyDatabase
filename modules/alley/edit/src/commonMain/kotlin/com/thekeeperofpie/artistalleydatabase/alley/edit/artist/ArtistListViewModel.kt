package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.milliseconds

@AssistedInject
class ArtistListViewModel(
    database: AlleyEditDatabase,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val query by savedStateHandle.saveable(saver = TextFieldState.Saver) { TextFieldState() }
    private val dataYear = savedStateHandle.getMutableStateFlow("dataYear", DataYear.LATEST)
    private val sortBy = savedStateHandle.getMutableStateFlow("sortBy", ArtistListSortBy.BOOTH)
    private val tab = savedStateHandle.getMutableStateFlow("tab", ArtistListTab.ALL)
    private val refreshFlow = RefreshFlow()
    private val artistEntries = combine(dataYear, refreshFlow.updates, ::Pair)
        .mapLatest { (dataYear) -> database.loadArtists(dataYear) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val debouncedQuery = snapshotFlow { query.text.toString() }
        .debounce(500.milliseconds)

    private val missingLinks = artistEntries.mapLatest {
        it.filter { it.links.isEmpty() && it.storeLinks.isEmpty() && it.catalogLinks.isEmpty() }
    }.shareIn(viewModelScope, SharingStarted.Lazily, 1)

    private val missingInferred = artistEntries.mapLatest {
        it.filter {
            (it.seriesInferred.isEmpty() || it.merchInferred.isEmpty()) &&
                    (it.seriesConfirmed.isEmpty() && it.merchConfirmed.isEmpty())
        }
    }.shareIn(viewModelScope, SharingStarted.Lazily, 1)

    private val missingConfirmed = artistEntries.mapLatest {
        it.filter { it.images.isNotEmpty() && (it.seriesConfirmed.isEmpty() || it.merchConfirmed.isEmpty()) }
    }.shareIn(viewModelScope, SharingStarted.Lazily, 1)

    private val entries = tab
        .flatMapLatest {
            combine(
                when (it) {
                    ArtistListTab.ALL -> artistEntries
                    ArtistListTab.MISSING_LINKS -> missingLinks
                    ArtistListTab.MISSING_INFERRED -> missingInferred
                    ArtistListTab.MISSING_CONFIRMED -> missingConfirmed
                }, debouncedQuery, ::Pair
            )
        }
        .mapLatest { (entries, query) ->
            if (query.isBlank()) {
                return@mapLatest entries
            }

            entries.filter {
                it.id.toString().contains(query) ||
                        it.name?.contains(query) == true ||
                        it.booth?.contains(query) == true
            }
        }
        .flatMapLatest { artists ->
            sortBy.mapLatest {
                when (it) {
                    ArtistListSortBy.BOOTH -> artists.sortedBy { it.booth }
                    ArtistListSortBy.NAME -> artists.sortedWith(
                        compareBy(String.CASE_INSENSITIVE_ORDER) { it.name.orEmpty() }
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    internal val state = ArtistListScreen.State(
        query = query,
        dataYear = dataYear,
        sortBy = sortBy,
        tab = tab,
        entries = entries,
    )

    fun refresh() = refreshFlow.refresh()

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle): ArtistListViewModel
    }
}
