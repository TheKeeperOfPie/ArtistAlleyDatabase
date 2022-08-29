package com.thekeeperofpie.artistalleydatabase.art.search

import android.app.Application
import androidx.annotation.MainThread
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.art.R
import com.thekeeperofpie.artistalleydatabase.art.grid.ArtEntryGridModel
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
open class ArtSearchViewModel @Inject constructor(
    protected val application: Application,
    protected val artEntryDao: ArtEntryDao,
    protected val appJson: AppJson,
) : EntrySearchViewModel<ArtSearchQuery, ArtEntryGridModel>() {

    override val entryGridSelectionController =
        EntryGridSelectionController<ArtEntryGridModel>({ viewModelScope }) {
            val toDelete = it.map { it.value }
            toDelete.forEach {
                ArtEntryUtils.getImageFile(application, it.id).delete()
            }
            artEntryDao.delete(toDelete)
        }

    private val artistsOption = EntrySearchOption(R.string.art_search_option_artists)
    private val sourceOption = EntrySearchOption(R.string.art_search_option_source)
    private val seriesOption = EntrySearchOption(R.string.art_search_option_series)
    private val charactersOption = EntrySearchOption(R.string.art_search_option_characters)
    private val tagsOption = EntrySearchOption(R.string.art_search_option_tags)
    private val notesOption = EntrySearchOption(R.string.art_search_option_notes)
    private val otherOption = EntrySearchOption(R.string.art_search_option_other)
    private val lockedOption = EntrySearchOption(R.string.art_search_option_locked)
    private val unlockedOption = EntrySearchOption(R.string.art_search_option_unlocked)

    override val options = listOf(
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

    @MainThread
    override fun buildQueryWrapper(query: String?) = ArtSearchQuery(
        query = query.orEmpty(),
        includeArtists = artistsOption.enabled,
        includeSources = sourceOption.enabled,
        includeSeries = seriesOption.enabled,
        includeCharacters = charactersOption.enabled,
        includeTags = tagsOption.enabled,
        includeNotes = notesOption.enabled,
        locked = lockedOption.enabled,
        unlocked = unlockedOption.enabled,
    )

    override fun mapQuery(query: ArtSearchQuery?): Flow<PagingData<ArtEntryGridModel>> =
        Pager(PagingConfig(pageSize = 20)) { artEntryDao.getEntries(query ?: ArtSearchQuery()) }
            .flow.cachedIn(viewModelScope)
            .onEach {
                viewModelScope.launch(Dispatchers.Main) {
                    // TODO: There must be a better way to sync selected items
                    clearSelected()
                }
            }
            .map {
                it.map {
                    ArtEntryGridModel.buildFromEntry(application, appJson, it)
                }
            }
}