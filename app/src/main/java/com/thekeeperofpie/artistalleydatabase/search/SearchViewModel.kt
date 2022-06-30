package com.thekeeperofpie.artistalleydatabase.search

import android.app.Application
import androidx.annotation.MainThread
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryGridViewModel
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
abstract class SearchViewModel constructor(
    application: Application,
    artEntryDao: ArtEntryDao,
) : ArtEntryGridViewModel(application, artEntryDao) {

    val query = MutableStateFlow(SearchQueryWrapper())

    val results = MutableStateFlow(PagingData.empty<ArtEntryModel>())

    private val artistsOption = SearchOption(R.string.search_option_artists)
    private val sourceOption = SearchOption(R.string.search_option_source)
    private val seriesOption = SearchOption(R.string.search_option_series)
    private val charactersOption = SearchOption(R.string.search_option_characters)
    private val tagsOption = SearchOption(R.string.search_option_tags)
    private val notesOption = SearchOption(R.string.search_option_notes)
    private val otherOption = SearchOption(R.string.search_option_other)
    private val lockedOption = SearchOption(R.string.search_option_locked)
    private val unlockedOption = SearchOption(R.string.search_option_unlocked)

    val options = listOf(
        artistsOption,
        sourceOption,
        seriesOption,
        charactersOption,
        tagsOption,
        notesOption,
        otherOption,
        lockedOption,
        unlockedOption,
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            query.flatMapLatest {
                Pager(PagingConfig(pageSize = 20)) { artEntryDao.getEntries(it) }
                    .flow.cachedIn(viewModelScope)
                    .onEach {
                        viewModelScope.launch(Dispatchers.Main) {
                            // TODO: There must be a better way to sync selected items
                            clearSelected()
                        }
                    }
            }
                .map { it.map { ArtEntryModel(application, it) } }
                .collect(results)
        }
    }

    fun onQuery(query: String) {
        if (this.query.value.value != query) {
            clearSelected()
        }
        this.query.tryEmit(buildQueryWrapper(query))
    }

    fun refreshQuery() {
        clearSelected()
        this.query.tryEmit(buildQueryWrapper(query.value.value))
    }

    @MainThread
    fun buildQueryWrapper(query: String) = SearchQueryWrapper(
        value = query,
        includeArtists = artistsOption.enabled,
        includeSources = sourceOption.enabled,
        includeSeries = seriesOption.enabled,
        includeCharacters = charactersOption.enabled,
        includeTags = tagsOption.enabled,
        includeNotes = notesOption.enabled,
        locked = lockedOption.enabled,
        unlocked = unlockedOption.enabled,
    )
}