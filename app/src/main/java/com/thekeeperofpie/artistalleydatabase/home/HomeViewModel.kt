package com.thekeeperofpie.artistalleydatabase.home

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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    artEntryDao: ArtEntryDao,
) : ArtEntryGridViewModel(application, artEntryDao) {

    val query = MutableStateFlow(QueryWrapper())

    val results = MutableStateFlow(PagingData.empty<ArtEntryModel>())

    private val artistsOption = HomeScreen.SearchOption(R.string.search_option_artists)
    private val sourceOption = HomeScreen.SearchOption(R.string.search_option_source)
    private val seriesOption = HomeScreen.SearchOption(R.string.search_option_series)
    private val charactersOption = HomeScreen.SearchOption(R.string.search_option_characters)
    private val tagsOption = HomeScreen.SearchOption(R.string.search_option_tags)
    private val notesOption = HomeScreen.SearchOption(R.string.search_option_notes)
    private val otherOption = HomeScreen.SearchOption(R.string.search_option_other)
    private val lockedOption = HomeScreen.SearchOption(R.string.search_option_locked)
    private val unlockedOption = HomeScreen.SearchOption(R.string.search_option_unlocked)

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

    @MainThread
    fun buildQueryWrapper(query: String) = QueryWrapper(
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

    data class QueryWrapper(
        val value: String = "",
        val includeArtists: Boolean = true,
        val includeSources: Boolean = true,
        val includeSeries: Boolean = true,
        val includeCharacters: Boolean = true,
        val includeTags: Boolean = true,
        val includeNotes: Boolean = true,
        val includeOther: Boolean = true,
        val locked: Boolean = true,
        val unlocked: Boolean = true,
    )
}