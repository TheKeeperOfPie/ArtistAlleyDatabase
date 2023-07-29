package com.thekeeperofpie.artistalleydatabase.search.results

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
import com.thekeeperofpie.anichive.R
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.art.grid.ArtEntryGridModel
import com.thekeeperofpie.artistalleydatabase.art.grid.ArtEntryGridViewModel
import com.thekeeperofpie.artistalleydatabase.art.search.ArtEntryAdvancedSearchDao
import com.thekeeperofpie.artistalleydatabase.search.advanced.AdvancedSearchRepository
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
                        it.filter {
                            if (query.seriesById.isNotEmpty()
                                && it.series(appJson).none { query.seriesById.contains(it.id) }
                            ) {
                                return@filter false
                            }

                            if (query.charactersById.isNotEmpty()
                                && it.characters(appJson)
                                    .none { query.charactersById.contains(it.id) }
                            ) {
                                return@filter false
                            }

                            true
                        }
                            .map {
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
