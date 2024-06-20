package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.R
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.compose.filter.selectedOption
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.entry.search.EntrySearchViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject
import kotlin.math.absoluteValue
import kotlin.random.Random


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ArtistSearchViewModel @Inject constructor(
    private val application: Application,
    private val artistEntryDao: ArtistEntryDao,
    private val settings: ArtistAlleySettings,
    savedStateHandle: SavedStateHandle,
) : EntrySearchViewModel<ArtistSearchQuery, ArtistEntryGridModel>() {

    @Serializable
    data class InternalRoute(
        val series: String? = null,
        val merch: String? = null,
    )

    private val route = savedStateHandle.toRoute<InternalRoute>()
    val lockedSeries = route.series
    val lockedMerch = route.merch

    val boothSection = EntrySection.LongText(headerRes = R.string.alley_search_option_booth)
    val artistSection = EntrySection.LongText(headerRes = R.string.alley_search_option_artist)
    val summarySection =
        EntrySection.LongText(headerRes = R.string.alley_search_option_summary)
    val seriesSection = EntrySection.MultiText(
        R.string.alley_series_header_zero,
        R.string.alley_series_header_one,
        R.string.alley_series_header_many,
    )
    val merchSection = EntrySection.MultiText(
        R.string.alley_search_option_merch_zero,
        R.string.alley_search_option_merch_one,
        R.string.alley_search_option_merch_many,
    )

    override val sections = listOf(boothSection, artistSection, summarySection, seriesSection, merchSection)

    var sortOptions by mutableStateOf(run {
        val values = ArtistSearchSortOption.entries
        val option = settings.artistsSortOption.let { artistsSortOption ->
            values.find { it.name == artistsSortOption } ?: ArtistSearchSortOption.RANDOM
        }
        values.map {
            SortEntry(
                value = it,
                state = if (it == option) {
                    FilterIncludeExcludeState.INCLUDE
                } else {
                    FilterIncludeExcludeState.DEFAULT
                }
            )
        }
    })
        private set

    var sortAscending by mutableStateOf(settings.artistsSortAscending)
        private set

    var showOnlyFavorites by mutableStateOf(false)
    var showOnlyWithCatalog by mutableStateOf(false)
    var showGridByDefault by mutableStateOf(settings.showGridByDefault)
        private set
    var showIgnored by mutableStateOf(true)
    var showOnlyIgnored by mutableStateOf(false)

    var entriesSize by mutableStateOf(0)
        private set

    private val randomSeed = Random.nextInt().absoluteValue
    private val mutationUpdates = MutableSharedFlow<ArtistEntry>(5, 5)

    var displayType by mutableStateOf(
        settings.displayType.let { displayType ->
            SearchScreen.DisplayType.entries.find { it.name == displayType }
                ?: SearchScreen.DisplayType.CARD
        }
    )
        private set

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            mutationUpdates.collectLatest {
                artistEntryDao.insertEntries(it)
            }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            artistEntryDao.getEntriesSizeFlow()
                .collect { entriesSize = it }
        }
    }

    override fun searchOptions() = snapshotFlow {
        val seriesContents = seriesSection.finalContents()
        ArtistSearchQuery(
            booth = boothSection.value.trim(),
            artist = artistSection.value.trim(),
            summary = summarySection.value.trim(),
            series = seriesContents.filterIsInstance<EntrySection.MultiText.Entry.Custom>()
                .map { it.serializedValue }
                .filterNot(String::isBlank),
            seriesById = seriesContents
                .filterIsInstance<EntrySection.MultiText.Entry.Prefilled<*>>()
                .mapNotNull(AniListUtils::mediaId),
            merch = merchSection.finalContents().map { it.serializedValue },
            sortOption = sortOptions.selectedOption(ArtistSearchSortOption.RANDOM),
            sortAscending = sortAscending,
            showOnlyFavorites = showOnlyFavorites,
            showOnlyWithCatalog = showOnlyWithCatalog,
            showIgnored = showIgnored,
            showOnlyIgnored = showOnlyIgnored,
            randomSeed = randomSeed,
            lockedSeries = lockedSeries,
            lockedMerch = lockedMerch,
        )
    }

    override fun mapQuery(
        query: String,
        options: ArtistSearchQuery,
    ) = Pager(PagingConfig(pageSize = 20)) {
        trackPagingSource { artistEntryDao.search(query, options) }
    }.flow
        .flowOn(CustomDispatchers.IO)
        .map {
            it.filter { !it.ignored || options.showIgnored }
                .filter { it.ignored || !options.showOnlyIgnored }
        }
        .map { it.map { ArtistEntryGridModel.buildFromEntry(application, it) } }
        .cachedIn(viewModelScope)

    fun onFavoriteToggle(entry: ArtistEntryGridModel, favorite: Boolean) {
        mutationUpdates.tryEmit(entry.value.copy(favorite = favorite))
    }

    fun onIgnoredToggle(entry: ArtistEntryGridModel, ignored: Boolean) {
        mutationUpdates.tryEmit(entry.value.copy(ignored = ignored))
    }

    fun onDisplayTypeToggle(displayType: SearchScreen.DisplayType) {
        this.displayType = displayType
        settings.displayType = displayType.name
    }

    fun onSortClick(option: ArtistSearchSortOption) {
        var newOption = option
        val values = ArtistSearchSortOption.values()
        val existingOptions = sortOptions
        if (existingOptions.first { it.state == FilterIncludeExcludeState.INCLUDE }
                .value == option) {
            newOption = values[(values.indexOf(option) + 1) % values.size]
        }

        settings.artistsSortOption = newOption.name
        sortOptions = values.map {
            SortEntry(
                value = it,
                state = if (it == newOption) {
                    FilterIncludeExcludeState.INCLUDE
                } else {
                    FilterIncludeExcludeState.DEFAULT
                }
            )
        }
    }

    fun onSortAscendingToggle(ascending: Boolean) {
        sortAscending = ascending
        settings.artistsSortAscending = ascending
    }

    fun onShowGridByDefaultToggle(show: Boolean) {
        showGridByDefault = show
        settings.showGridByDefault = show
    }

    private data class FilterParams(
        val sortOptions: List<SortEntry<ArtistSearchSortOption>>,
        val sortAscending: Boolean,
        val showOnlyFavorites: Boolean,
        val showOnlyWithCatalog: Boolean,
        val showIgnored: Boolean,
        val showOnlyIgnored: Boolean,
    )
}
