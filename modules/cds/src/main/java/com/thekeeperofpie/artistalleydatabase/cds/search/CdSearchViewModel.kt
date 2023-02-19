package com.thekeeperofpie.artistalleydatabase.cds.search

import android.app.Application
import androidx.annotation.MainThread
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.thekeeperofpie.artistalleydatabase.cds.R
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.cds.grid.CdEntryGridModel
import com.thekeeperofpie.artistalleydatabase.entry.EntryUtils
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridSelectionController
import com.thekeeperofpie.artistalleydatabase.entry.search.EntrySearchOption
import com.thekeeperofpie.artistalleydatabase.entry.search.EntrySearchViewModel
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
            it.forEach {
                EntryUtils.getImageFile(application, it.id).delete()
                cdEntryDao.delete(it.id.valueId)
            }
        }

    private val catalogIdsOption = EntrySearchOption(R.string.cd_search_option_catalog_ids)
    private val titlesOption = EntrySearchOption(R.string.cd_search_option_titles)
    private val performersOption = EntrySearchOption(R.string.cd_search_option_performers)
    private val composersOption = EntrySearchOption(R.string.cd_search_option_composers)
    private val seriesOption = EntrySearchOption(R.string.cd_search_option_series)
    private val charactersOption = EntrySearchOption(R.string.cd_search_option_characters)
    private val discsOption = EntrySearchOption(R.string.cd_search_option_discs)
    private val tagsOption = EntrySearchOption(R.string.cd_search_option_tags)
    private val notesOption = EntrySearchOption(R.string.cd_search_option_notes)
    private val otherOption = EntrySearchOption(R.string.cd_search_option_other)
    private val lockedOption = EntrySearchOption(R.string.cd_search_option_locked)
    private val unlockedOption = EntrySearchOption(R.string.cd_search_option_unlocked)

    override val options = listOf(
        catalogIdsOption,
        titlesOption,
        performersOption,
        composersOption,
        seriesOption,
        charactersOption,
        discsOption,
        tagsOption,
        notesOption,
        otherOption,
        lockedOption,
        unlockedOption,
    )

    @MainThread
    override fun buildQueryWrapper(query: String?) = CdSearchQuery(
        query = query.orEmpty(),
        includeCatalogIds = catalogIdsOption.enabled,
        includeTitles = titlesOption.enabled,
        includePerformers = performersOption.enabled,
        includeComposers = composersOption.enabled,
        includeSeries = seriesOption.enabled,
        includeCharacters = charactersOption.enabled,
        includeDiscs = discsOption.enabled,
        includeTags = tagsOption.enabled,
        includeNotes = notesOption.enabled,
        includeOther = otherOption.enabled,
        locked = lockedOption.enabled,
        unlocked = unlockedOption.enabled,
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