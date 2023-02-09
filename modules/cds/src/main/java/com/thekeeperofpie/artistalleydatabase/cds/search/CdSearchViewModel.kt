package com.thekeeperofpie.artistalleydatabase.cds.search

import android.app.Application
import androidx.annotation.MainThread
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.cds.grid.CdEntryGridModel
import com.thekeeperofpie.artistalleydatabase.cds.utils.CdEntryUtils
import com.thekeeperofpie.artistalleydatabase.form.grid.EntryGridSelectionController
import com.thekeeperofpie.artistalleydatabase.form.search.EntrySearchOption
import com.thekeeperofpie.artistalleydatabase.form.search.EntrySearchViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CdSearchViewModel @Inject constructor(
    private val application: Application,
    private val cdEntryDao: CdEntryDao,
) : EntrySearchViewModel<CdSearchQuery, CdEntryGridModel>() {

    override val entryGridSelectionController =
        EntryGridSelectionController<CdEntryGridModel>({ viewModelScope }) {
            val toDelete = it.map { it.value }
            toDelete.forEach {
                CdEntryUtils.getImageFile(application, it.entryId).delete()
            }
            cdEntryDao.delete(toDelete)
        }

    override val options = listOf<EntrySearchOption>()

    @MainThread
    override fun buildQueryWrapper(query: String?) = CdSearchQuery(
        query = query.orEmpty(),
    )

    override fun mapQuery(query: CdSearchQuery?): Flow<PagingData<CdEntryGridModel>> =
        Pager(PagingConfig(pageSize = 20)) {
            trackPagingSource { cdEntryDao.getEntries(query ?: CdSearchQuery()) }
        }
            .flow.cachedIn(viewModelScope)
            .onEach {
                viewModelScope.launch(Dispatchers.Main) {
                    // TODO: There must be a better way to sync selected items
                    clearSelected()
                }
            }
            .map {
                it.map {
                    CdEntryGridModel.buildFromEntry(application, it)
                }
            }
}