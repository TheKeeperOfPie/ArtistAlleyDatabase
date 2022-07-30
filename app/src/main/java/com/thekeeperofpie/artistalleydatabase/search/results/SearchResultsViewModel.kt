package com.thekeeperofpie.artistalleydatabase.search.results

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.art.grid.ArtEntryGridModel
import com.thekeeperofpie.artistalleydatabase.art.grid.ArtEntryGridViewModel
import com.thekeeperofpie.artistalleydatabase.json.AppJson
import com.thekeeperofpie.artistalleydatabase.search.advanced.AdvancedSearchRepository
import com.thekeeperofpie.artistalleydatabase.search.advanced.ArtEntryAdvancedSearchDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchResultsViewModel @Inject constructor(
    application: Application,
    private val artEntrySearchDao: ArtEntryAdvancedSearchDao,
    private val appJson: AppJson,
    private val searchRepository: AdvancedSearchRepository,
) : ArtEntryGridViewModel(application, artEntrySearchDao) {

    lateinit var queryId: String

    var loading by mutableStateOf(false)
    val entries = MutableStateFlow(PagingData.empty<ArtEntryGridModel>())
    var errorResource by mutableStateOf<Pair<Int, Exception?>?>(null)

    fun initialize(queryId: String) {
        if (this::queryId.isInitialized) return
        this.queryId = queryId

        viewModelScope.launch(Dispatchers.IO) {
            val query = searchRepository.findQuery(queryId)
            if (query == null) {
                errorResource = R.string.error_loading_entries to null
            } else {
                Pager(PagingConfig(pageSize = 20)) { artEntrySearchDao.search(query) }
                    .flow.cachedIn(viewModelScope)
                    .map {
                        Log.d("SearchDebug", "result = $it")
                        it.map {
                            Log.d("SearchDebug", "result = $it")
                            ArtEntryGridModel.buildFromEntry(application, appJson, it)
                        }
                    }
                    .onEach {
                        if (loading) {
                            launch(Dispatchers.Main) {
                                loading = false
                            }
                        }
                    }
                    .collect(entries)
            }
        }
    }

    fun onDeleteSelected() {
        super.deleteSelected()
    }
}