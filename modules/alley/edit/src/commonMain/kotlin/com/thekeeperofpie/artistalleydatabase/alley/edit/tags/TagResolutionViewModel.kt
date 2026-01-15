package com.thekeeperofpie.artistalleydatabase.alley.edit.tags

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistCache
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistSummary
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@AssistedInject
class TagResolutionViewModel(
    private val artistCache: ArtistCache,
    dispatchers: CustomDispatchers,
    tagAutocomplete: TagAutocomplete,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val unknownSeriesAndArtists: StateFlow<List<Pair<String, List<ArtistSummary>>>> =
        combine(tagAutocomplete.seriesById, artistCache.artistsAnimeExpo2026, ::Pair)
            .mapLatest { (seriesById, artists) ->
                val unknown = mutableMapOf<String, MutableList<ArtistSummary>>()
                artists.forEach { artist ->
                    val unknownSeries =
                        artist.seriesInferred.filterNot { seriesById.contains(it) } +
                                artist.seriesConfirmed.filterNot { seriesById.contains(it) }
                    unknownSeries.forEach {
                        unknown.getOrPut(it) { mutableListOf() }.add(artist)
                    }
                }
                unknown.entries.map { it.toPair() }
                    .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.first })
            }
            .flowOn(dispatchers.io)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val unknownMerchAndArtists: StateFlow<List<Pair<String, List<ArtistSummary>>>> =
        combine(tagAutocomplete.merchById, artistCache.artistsAnimeExpo2026, ::Pair)
            .mapLatest { (merchById, artists) ->
                val unknown = mutableMapOf<String, MutableList<ArtistSummary>>()
                artists.forEach { artist ->
                    val unknownMerch =
                        artist.merchInferred.filterNot { merchById.contains(it) } +
                                artist.merchConfirmed.filterNot { merchById.contains(it) }
                    unknownMerch.forEach {
                        unknown.getOrPut(it) { mutableListOf() }.add(artist)
                    }
                }
                unknown.entries.map { it.toPair() }
                    .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.first })
            }
            .flowOn(dispatchers.io)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun onRefresh() = artistCache.refresh()

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle): TagResolutionViewModel
    }
}
