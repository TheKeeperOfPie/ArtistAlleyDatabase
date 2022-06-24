package com.thekeeperofpie.artistalleydatabase.search

import android.app.Application
import androidx.annotation.MainThread
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val application: Application,
    private val artEntryDao: ArtEntryDao,
) : ViewModel() {

    val query = MutableStateFlow(QueryWrapper())

    val results = MutableStateFlow(PagingData.empty<ArtEntryModel>())

    val selectedEntries = mutableStateMapOf<Int, ArtEntryModel>()

    private val artistsOption = SearchScreen.Option(R.string.search_option_artists)
    private val sourceOption = SearchScreen.Option(R.string.search_option_source)
    private val seriesOption = SearchScreen.Option(R.string.search_option_series)
    private val charactersOption = SearchScreen.Option(R.string.search_option_characters)
    private val tagsOption = SearchScreen.Option(R.string.search_option_tags)
    private val notesOption = SearchScreen.Option(R.string.search_option_notes)
    private val otherOption = SearchScreen.Option(R.string.search_option_other)
    private val lockedOption = SearchScreen.Option(R.string.search_option_locked)
    private val unlockedOption = SearchScreen.Option(R.string.search_option_unlocked)

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
                Pager(PagingConfig(pageSize = 20)) {
                    if (it.value.isEmpty()) {
                        artEntryDao.getEntries()
                    } else {
                        artEntryDao.getEntries(it)
                    }
                }
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

    fun clearSelected() {
        synchronized(selectedEntries) {
            selectedEntries.clear()
        }
    }

    fun selectEntry(index: Int, entry: ArtEntryModel) {
        synchronized(selectedEntries) {
            if (selectedEntries.containsKey(index)) {
                selectedEntries.remove(index)
            } else {
                selectedEntries.put(index, entry)
            }
        }
    }

    fun deleteSelected() {
        synchronized(selectedEntries) {
            viewModelScope.launch(Dispatchers.IO) {
                val toDelete: List<ArtEntry>
                withContext(Dispatchers.Main) {
                    toDelete = selectedEntries.values.map { it.value }
                    selectedEntries.clear()
                }
                toDelete.forEach {
                    ArtEntryUtils.getImageFile(application, it.id).delete()
                }
                artEntryDao.delete(toDelete)
            }
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