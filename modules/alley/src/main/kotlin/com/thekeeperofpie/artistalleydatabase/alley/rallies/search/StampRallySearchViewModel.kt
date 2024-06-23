package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.R
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryGridModel
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
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
import javax.inject.Inject
import kotlin.math.absoluteValue
import kotlin.random.Random


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StampRallySearchViewModel @Inject constructor(
    private val application: Application,
    private val stampRallyEntryDao: StampRallyEntryDao,
    private val settings: ArtistAlleySettings,
) : EntrySearchViewModel<StampRallySearchQuery, StampRallyEntryGridModel>() {

    val fandomSection = EntrySection.LongText(headerRes = R.string.alley_search_option_fandom)

    // TODO: Multi-option
    val tablesSection = EntrySection.LongText(headerRes = R.string.alley_search_option_tables)
    val artistsSection = EntrySection.LongText(headerRes = R.string.alley_search_option_artist)

    override val sections = listOf(fandomSection, tablesSection, artistsSection)

    var sortOptions by mutableStateOf(run {
        val values = StampRallySearchSortOption.entries
        val option = settings.stampRalliesSortOption.value.let { stampRalliesSortOption ->
            values.find { it.name == stampRalliesSortOption } ?: StampRallySearchSortOption.RANDOM
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

    val sortAscending = settings.stampRalliesSortAscending
    val showGridByDefault = settings.showGridByDefault
    val showRandomCatalogImage = settings.showRandomCatalogImage
    val displayType = settings.displayType

    var showOnlyFavorites by mutableStateOf(false)
    var showIgnored by mutableStateOf(true)
    var showOnlyIgnored by mutableStateOf(false)

    var entriesSize by mutableIntStateOf(0)
        private set

    val randomSeed = Random.nextInt().absoluteValue
    private val mutationUpdates = MutableSharedFlow<StampRallyEntry>(5, 5)

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            mutationUpdates.collectLatest {
                stampRallyEntryDao.insertEntries(it)
            }
        }

        viewModelScope.launch(CustomDispatchers.Main) {
            stampRallyEntryDao.getEntriesSizeFlow()
                .flowOn(CustomDispatchers.IO)
                .collect { entriesSize = it }
        }
    }

    override fun searchOptions() = snapshotFlow {
        StampRallySearchQuery(
            fandom = fandomSection.value.trim(),
            tables = tablesSection.value.trim(),
            sortOption = sortOptions.selectedOption(StampRallySearchSortOption.RANDOM),
            sortAscending = sortAscending.value,
            showOnlyFavorites = showOnlyFavorites,
            showIgnored = showIgnored,
            showOnlyIgnored = showOnlyIgnored,
            randomSeed = randomSeed,
        )
    }

    override fun mapQuery(
        query: String,
        options: StampRallySearchQuery,
    ) = Pager(PagingConfig(pageSize = 20)) {
        trackPagingSource { stampRallyEntryDao.search(query, options) }
    }.flow
        .flowOn(CustomDispatchers.IO)
        .map {
            it.filter { !it.ignored || options.showIgnored }
                .filter { it.ignored || !options.showOnlyIgnored }
        }
        .map { it.map { StampRallyEntryGridModel.buildFromEntry(application, it) } }
        .cachedIn(viewModelScope)

    fun onFavoriteToggle(entry: StampRallyEntryGridModel, favorite: Boolean) {
        mutationUpdates.tryEmit(entry.value.copy(favorite = favorite))
    }

    fun onIgnoredToggle(entry: StampRallyEntryGridModel, ignored: Boolean) {
        mutationUpdates.tryEmit(entry.value.copy(ignored = ignored))
    }

    fun onDisplayTypeToggle(displayType: SearchScreen.DisplayType) {
        settings.displayType.value = displayType.name
    }

    fun onSortClick(option: StampRallySearchSortOption) {
        var newOption = option
        val values = StampRallySearchSortOption.entries
        val existingOptions = sortOptions
        if (existingOptions.first { it.state == FilterIncludeExcludeState.INCLUDE }
                .value == option) {
            newOption = values[(values.indexOf(option) + 1) % values.size]
        }

        settings.stampRalliesSortOption.value = newOption.name
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
        settings.stampRalliesSortAscending.value = ascending
    }

    fun onShowGridByDefaultToggle(showGridByDefault: Boolean) {
        settings.showGridByDefault.value = showGridByDefault
    }

    fun onShowRandomCatalogImageToggle(showRandomCatalogImage: Boolean) {
        settings.showRandomCatalogImage.value = showRandomCatalogImage
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
