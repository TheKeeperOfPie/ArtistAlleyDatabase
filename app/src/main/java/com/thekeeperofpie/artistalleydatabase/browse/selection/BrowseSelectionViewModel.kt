package com.thekeeperofpie.artistalleydatabase.browse.selection

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryColumn
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.grid.ArtEntryGridModel
import com.thekeeperofpie.artistalleydatabase.art.grid.ArtEntryGridViewModel
import com.thekeeperofpie.artistalleydatabase.json.AppJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrowseSelectionViewModel @Inject constructor(
    application: Application,
    artEntryDao: ArtEntryDao,
    private val appJson: AppJson,
) : ArtEntryGridViewModel(application, artEntryDao) {

    lateinit var column: ArtEntryColumn

    var loading by mutableStateOf(false)
    val entries = MutableStateFlow(PagingData.empty<ArtEntryGridModel>())

    fun initialize(column: ArtEntryColumn, query: String) {
        if (this::column.isInitialized) return
        this.column = column

        viewModelScope.launch(Dispatchers.IO) {
            Pager(PagingConfig(pageSize = 20)) {
                when (column) {
                    ArtEntryColumn.ARTISTS -> artEntryDao.getArtist(query)
                    ArtEntryColumn.SOURCE -> TODO()
                    ArtEntryColumn.SERIES -> artEntryDao.getSeries(query)
                    ArtEntryColumn.CHARACTERS -> artEntryDao.getCharacter(query)
                    ArtEntryColumn.TAGS -> artEntryDao.getTag(query)
                }
            }
                .flow.cachedIn(viewModelScope)
                .map {
                    it.filter {
                        when (column) {
                            ArtEntryColumn.ARTISTS -> it.artists.contains(query)
                            ArtEntryColumn.SOURCE -> TODO()
                            ArtEntryColumn.SERIES -> it.series.any { it.contains(query) }
                            ArtEntryColumn.CHARACTERS -> it.characters.any { it.contains(query) }
                            ArtEntryColumn.TAGS -> it.tags.contains(query)
                        }
                    }
                        .map { ArtEntryGridModel.buildFromEntry(application, appJson, it) }
                }
                .onEach {
                    launch(Dispatchers.Main) {
                        loading = false
                    }
                }
                .collect(entries)
        }
    }

    fun onDeleteSelected() {
        super.deleteSelected()
    }
}