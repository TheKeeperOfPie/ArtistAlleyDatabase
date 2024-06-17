package com.thekeeperofpie.artistalleydatabase.alley.search

import android.app.Application
import androidx.compose.runtime.getValue
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
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.ArtistSearchQuery
import com.thekeeperofpie.artistalleydatabase.alley.R
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.entry.search.EntrySearchViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
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
        private const val CSV_NAME = "artists.csv"
    }

    val boothSection = EntrySection.LongText(headerRes = R.string.alley_search_option_booth)
    val artistSection = EntrySection.LongText(headerRes = R.string.alley_search_option_artist)
    val descriptionSection =
        EntrySection.LongText(headerRes = R.string.alley_search_option_description)
    val seriesSection = EntrySection.MultiText(
        R.string.alley_series_header_zero,
        R.string.alley_series_header_one,
        R.string.alley_series_header_many,
    )

    override val sections = listOf(boothSection, artistSection, descriptionSection, seriesSection)

    var sortOptions by mutableStateOf(run {
        val values = ArtistAlleySearchSortOption.values()
        val option = settings.artistsSortOption.let { artistsSortOption ->
            values.find { it.name == artistsSortOption } ?: ArtistAlleySearchSortOption.RANDOM
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

    var updateAppUrl by mutableStateOf<String?>(null)

    var entriesSize by mutableStateOf(0)
        private set

    private val randomSeed = Random.nextInt().absoluteValue
    private val mutationUpdates = MutableSharedFlow<ArtistEntry>(5, 5)

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
                                    // Booth,Artist,Summary,Links,Store,Catalog / table,
                                    // Series - Inferred,Merch - Inferred,Notes,Series - Confirmed,
                                    // Merch - Confirmed,Catalog images
                                    val booth = it["Booth"]
                                    val artist = it["Artist"]
                                    val summary = it["Summary"]
                                    val links = it["Links"].split("\n")
                                    val store = it["Store"]
                                    val catalog = it["Catalog / table"]

                                    val regex = Regex(",\\s?")
                                    val seriesInferred = it["Series - Inferred"].split(regex)
                                    val merchInferred = it["Series - Inferred"].split(regex)
                                    val seriesConfirmed = it["Merch - Confirmed"].split(regex)
                                    val merchConfirmed = it["Merch - Confirmed"].split(regex)
                                    ArtistEntry(
                                        id = booth,
                                        booth = booth,
                                        name = artist,
                                        summary = summary,
                                        links = links,
                                        store = store,
                                        catalog = catalog,
                                        seriesInferredSerialized = seriesInferred,
                                        seriesInferredSearchable = seriesInferred,
                                        seriesConfirmedSerialized = seriesConfirmed,
                                        seriesConfirmedSearchable = seriesConfirmed,
                                        merchInferred = merchInferred,
                                        merchConfirmed = merchConfirmed,
                                    )
                                } catch (ignored: Throwable) {
                                    null
                                }
                            }
                            .chunked(20)
                            .forEach { artistEntryDao.insertUpdatedEntries(it) }
                    }
                }
            }

            settings.lastKnownCsvSize = csvSize
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            mutationUpdates.collectLatest {
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

        viewModelScope.launch(CustomDispatchers.IO) {
            artistEntryDao.getEntriesSizeFlow()
                .collect { entriesSize = entriesSize }
        }
    }

    override fun searchOptions() = snapshotFlow {
        val sortOption = sortOptions.firstOrNull { it.state == FilterIncludeExcludeState.INCLUDE }
            ?.value
            ?: ArtistAlleySearchSortOption.RANDOM
        val seriesContents = seriesSection.finalContents()
        ArtistSearchQuery(
            booth = boothSection.value.trim(),
            artist = artistSection.value.trim(),
            description = descriptionSection.value.trim(),
            series = seriesContents.filterIsInstance<EntrySection.MultiText.Entry.Custom>()
                .map { it.serializedValue }
                .filterNot(String::isBlank),
            seriesById = seriesContents
                .filterIsInstance<EntrySection.MultiText.Entry.Prefilled<*>>()
                .mapNotNull(AniListUtils::mediaId),
            sortOption = sortOption,
            sortAscending = sortAscending,
            showOnlyFavorites = showOnlyFavorites,
            showOnlyWithCatalog = showOnlyWithCatalog,
            showIgnored = showIgnored,
            showOnlyIgnored = showOnlyIgnored,
            randomSeed = randomSeed,
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

    fun onDisplayTypeToggle(displayType: ArtistAlleySearchScreen.DisplayType) {
        this.displayType = displayType
        settings.displayType = displayType.name
    }

    fun onSortClick(option: ArtistAlleySearchSortOption) {
        var newOption = option
        val values = ArtistAlleySearchSortOption.values()
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
        val sortOptions: List<SortEntry<ArtistAlleySearchSortOption>>,
        val sortAscending: Boolean,
        val showOnlyFavorites: Boolean,
        val showOnlyWithCatalog: Boolean,
        val showIgnored: Boolean,
        val showOnlyIgnored: Boolean,
    )
}
