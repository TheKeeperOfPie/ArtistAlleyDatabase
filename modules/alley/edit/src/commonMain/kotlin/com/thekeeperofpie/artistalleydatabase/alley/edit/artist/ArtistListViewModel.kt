package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistSummary
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
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.milliseconds

@AssistedInject
class ArtistListViewModel(
    private val artistCache: ArtistCache,
    private val artistEntryDao: ArtistEntryDao,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val query by savedStateHandle.saveable(saver = TextFieldState.Saver.Fixed) { TextFieldState() }
    private val dataYear = savedStateHandle.getMutableStateFlow("dataYear", DataYear.LATEST)
    private val sortBy = savedStateHandle.getMutableStateFlow("sortBy", ArtistListSortBy.BOOTH)
    private val tab = savedStateHandle.getMutableStateFlow("tab", ArtistListTab.ALL)
    private val artistEntries = dataYear.flatMapLatest(artistCache::artists)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val debouncedQuery = snapshotFlow { query.text.toString() }
        .debounce(500.milliseconds)

    private val missingLinks = artistEntries.mapLatest {
        it.filter { it.socialLinks.isEmpty() && it.storeLinks.isEmpty() && it.portfolioLinks.isEmpty() && it.catalogLinks.isEmpty() }
    }.shareIn(viewModelScope, SharingStarted.Lazily, 1)

    private val ArtistSummary.missingTags
        get() = (seriesInferred.isEmpty() || merchInferred.isEmpty()) &&
                (seriesConfirmed.isEmpty() && merchConfirmed.isEmpty())

    private val missingUpdatedInferred = artistEntries.mapLatest {
        it.filter {
            (it.seriesInferred.isEmpty() || it.merchInferred.isEmpty()) &&
                    (it.seriesConfirmed.isEmpty() && it.merchConfirmed.isEmpty())
        }
    }.shareIn(viewModelScope, SharingStarted.Lazily, 1)

    private val missingInferred =
        combine(
            missingUpdatedInferred,
            dataYear.mapLatest {
                artistEntryDao.getAllEntries(it).associateBy { it.id }
            },
        ) { remoteEntries, databaseEntries ->
            remoteEntries.filter { remoteEntry ->
                val databaseEntry = databaseEntries[remoteEntry.id]
                databaseEntry == null || databaseEntry.missingTags
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
                    ArtistListTab.MISSING_UPDATED_INFERRED -> missingUpdatedInferred
                    ArtistListTab.MISSING_CONFIRMED -> missingConfirmed
                },
                debouncedQuery,
                ::Pair
            )
        }
        .mapLatest { (entries, query) ->
            if (query.isBlank()) {
                return@mapLatest entries
            }

            entries.filter {
                it.id.toString().contains(query, ignoreCase = true) ||
                        it.name.contains(query, ignoreCase = true) ||
                        it.booth?.contains(query, ignoreCase = true) == true
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

    fun refresh() = artistCache.refresh()

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle): ArtistListViewModel
    }
}
