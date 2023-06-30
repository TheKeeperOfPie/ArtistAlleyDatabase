package com.thekeeperofpie.artistalleydatabase.alley.search

import android.app.Application
import androidx.annotation.MainThread
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.ArtistSearchQuery
import com.thekeeperofpie.artistalleydatabase.alley.R
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.entry.search.EntrySearchOption
import com.thekeeperofpie.artistalleydatabase.entry.search.EntrySearchViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.csv.CSVFormat
import java.net.URL
import javax.inject.Inject
import kotlin.math.absoluteValue
import kotlin.random.Random


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ArtistAlleySearchViewModel @Inject constructor(
    private val application: Application,
    private val artistEntryDao: ArtistEntryDao,
    private val settings: ArtistAlleySettings,
) : EntrySearchViewModel<ArtistSearchQuery, ArtistEntryGridModel>() {

    companion object {
        private const val CSV_NAME = "artist-alley.csv"
    }

    private val boothOption = EntrySearchOption(R.string.alley_search_option_booth)
    private val tableNameOption = EntrySearchOption(R.string.alley_search_option_table_name)
    private val artistNamesOption = EntrySearchOption(R.string.alley_search_option_artist_names)
    private val regionOption = EntrySearchOption(R.string.alley_search_option_region)
    private val descriptionOption = EntrySearchOption(R.string.alley_search_option_description)

    override val options = listOf(
        boothOption,
        tableNameOption,
        artistNamesOption,
        regionOption,
        descriptionOption,
    )

    var sortOptions by mutableStateOf(ArtistAlleySearchSortOption.values()
        .map {
            SortEntry(
                it,
                state = if (it == ArtistAlleySearchSortOption.RANDOM) {
                    FilterIncludeExcludeState.INCLUDE
                } else {
                    FilterIncludeExcludeState.DEFAULT
                }
            )
        })

    var sortAscending by mutableStateOf(true)

    var showOnlyFavorites by mutableStateOf(false)
    var showOnlyWithCatalog by mutableStateOf(false)
    var showRegion by mutableStateOf(false)

    var updateAppUrl by mutableStateOf<String?>(null)

    private val randomSeed = Random.nextInt().absoluteValue
    private val favoriteUpdates = MutableSharedFlow<ArtistEntry>(5, 5)

    var displayType by mutableStateOf(
        settings.displayType.let { displayType ->
            ArtistAlleySearchScreen.DisplayType.values().find { it.name == displayType }
                ?: ArtistAlleySearchScreen.DisplayType.CARD
        }
    )
        private set

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            val csvSize = try {
                application.assets.open(CSV_NAME).use { it.available().toLong() }
            } catch (ignored: Throwable) {
                -1
            }

            if (settings.lastKnownCsvSize != csvSize || artistEntryDao.getEntriesSize() == 0) {
                application.assets.open(CSV_NAME).use { input ->
                    input.reader().use { reader ->
                        CSVFormat.RFC4180.builder()
                            .setHeader()
                            .setSkipHeaderRecord(true)
                            .build()
                            .parse(reader)
                            .asSequence()
                            .mapNotNull {
                                try {
                                    // Booth,Table name,Artist names,Region,Summary,Contact,Links,Catalog link,Commissions?,Catalog
                                    val booth = it["Booth"]
                                    val tableName = it["Table name"]
                                    val artistNames = it["Artist names"]
                                        .split("\n\n")
                                    val region = it["Region"]
                                    val description = it["Summary"]
                                    val contactLink = it["Contact"]
                                    val links = it["Links"]
                                    val catalogLink = it["Catalog link"]
                                    val catalog = it["Catalog"]
                                    ArtistEntry(
                                        id = booth,
                                        booth = booth,
                                        tableName = tableName,
                                        artistNames = artistNames,
                                        region = region,
                                        description = description,
                                        contactLink = contactLink,
                                        links = links.split("\n\n").flatMap { it.split("\n") },
                                        catalogLink = catalogLink.split("\n\n")
                                            .flatMap { it.split("\n") } + catalog,
                                    )
                                } catch (ignored: Throwable) {
                                    null
                                }
                            }
                            .chunked(20)
                            .forEach {
                                artistEntryDao.insertEntries(it.map {
                                    it.copy(
                                        favorite = artistEntryDao.getEntry(it.id)?.favorite ?: false
                                    )
                                })
                            }
                    }
                }
            }

            settings.lastKnownCsvSize = csvSize
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            favoriteUpdates.collectLatest {
                artistEntryDao.insertEntries(it)
            }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            try {
                val updateUrl = application.assets.open("update-url.txt")
                    .use { it.reader().readText() }
                val latestVersion =
                    URL(updateUrl).openStream().use { it.reader().readText() }.trim()
                val currentVersion =
                    application.assets.open("version.txt").use { it.reader().readText() }.trim()
                if (latestVersion != currentVersion) {
                    val appUrl = application.assets.open("app-url.txt")
                        .use { it.reader().readText() }
                    if (appUrl.isNotBlank()) {
                        withContext(CustomDispatchers.Main) {
                            updateAppUrl = appUrl
                        }
                    }
                }
            } catch (ignored: Throwable) {
            }
        }
    }

    @MainThread
    override fun buildQueryWrapper(query: String?) = ArtistSearchQuery(
        query = query.orEmpty(),
        includeBooth = boothOption.enabled,
        includeTableName = tableNameOption.enabled,
        includeArtistNames = artistNamesOption.enabled,
        includeRegion = regionOption.enabled,
        includeDescription = descriptionOption.enabled,
    )

    override fun mapQuery(query: ArtistSearchQuery?): Flow<PagingData<ArtistEntryGridModel>> =
        combine(
            snapshotFlow { sortOptions },
            snapshotFlow { sortAscending },
            snapshotFlow { showOnlyFavorites },
            snapshotFlow { showOnlyWithCatalog },
            ::FilterParams
        )
            .flowOn(CustomDispatchers.Main)
            .flatMapLatest { filterParams ->
                val sort =
                    filterParams.sortOptions.firstOrNull { it.state == FilterIncludeExcludeState.INCLUDE }
                        ?.value
                        ?: ArtistAlleySearchSortOption.BOOTH
                Pager(PagingConfig(pageSize = 20)) {
                    trackPagingSource {
                        artistEntryDao.getEntries(
                            query = query ?: ArtistSearchQuery(),
                            sort = sort,
                            sortAscending = filterParams.sortAscending,
                            showOnlyFavorites = showOnlyFavorites,
                            showOnlyWithCatalog = showOnlyWithCatalog,
                            randomSeed = randomSeed,
                        )
                    }
                }.flow.flowOn(CustomDispatchers.IO)
            }
            .map { it.map { ArtistEntryGridModel.buildFromEntry(application, it) } }
            .cachedIn(viewModelScope)

    override fun entriesSize() = artistEntryDao.getEntriesSizeFlow()

    fun onFavoriteToggle(entry: ArtistEntryGridModel, favorite: Boolean) {
        favoriteUpdates.tryEmit(entry.value.copy(favorite = favorite))
    }

    fun onDisplayTypeToggle(displayType: ArtistAlleySearchScreen.DisplayType) {
        this.displayType = displayType
        settings.displayType = displayType.name
    }

    fun onSortClick(option: ArtistAlleySearchSortOption) {
        val existingOptions = sortOptions
        if (existingOptions.first { it.state == FilterIncludeExcludeState.INCLUDE }
                .value == option) {
            return
        }
        sortOptions = existingOptions.toMutableList()
            .apply {
                replaceAll {
                    if (it.value == option) {
                        val newState = if (it.state != FilterIncludeExcludeState.INCLUDE) {
                            FilterIncludeExcludeState.INCLUDE
                        } else {
                            FilterIncludeExcludeState.DEFAULT
                        }
                        it.copy(state = newState)
                    } else it.copy(state = FilterIncludeExcludeState.DEFAULT)
                }
            }
    }

    private data class FilterParams(
        val sortOptions: List<SortEntry<ArtistAlleySearchSortOption>>,
        val sortAscending: Boolean,
        val showOnlyFavorites: Boolean,
        val showOnlyWithCatalog: Boolean,
    )
}
