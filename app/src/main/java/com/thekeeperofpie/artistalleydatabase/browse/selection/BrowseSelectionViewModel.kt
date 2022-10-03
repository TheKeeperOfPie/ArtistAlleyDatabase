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
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.Either
import com.thekeeperofpie.artistalleydatabase.anilist.AniListDataConverter
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterColumnEntry
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaColumnEntry
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryBrowseDao
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryColumn
import com.thekeeperofpie.artistalleydatabase.art.grid.ArtEntryGridModel
import com.thekeeperofpie.artistalleydatabase.art.grid.ArtEntryGridViewModel
import com.thekeeperofpie.artistalleydatabase.form.EntrySection.MultiText.Entry
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
    private val artEntryBrowseDao: ArtEntryBrowseDao,
    private val appJson: AppJson,
    private val aniListDataConverter: AniListDataConverter,
) : ArtEntryGridViewModel(application, artEntryBrowseDao) {

    lateinit var column: ArtEntryColumn

    var loading by mutableStateOf(false)
    val entries = MutableStateFlow(PagingData.empty<ArtEntryGridModel>())

    fun initialize(column: ArtEntryColumn, queryIdOrString: Either<String, String>) {
        if (this::column.isInitialized) return
        this.column = column

        viewModelScope.launch(Dispatchers.IO) {
            val queryValue = queryIdOrString.eitherValueUnchecked().toString()
            Pager(PagingConfig(pageSize = 20)) {
                when (column) {
                    ArtEntryColumn.ARTISTS -> artEntryBrowseDao.getArtist(queryValue)
                    ArtEntryColumn.SOURCE -> TODO()
                    ArtEntryColumn.SERIES -> artEntryBrowseDao.getSeries(queryValue)
                    ArtEntryColumn.CHARACTERS -> artEntryBrowseDao.getCharacter(queryValue)
                    ArtEntryColumn.TAGS -> artEntryBrowseDao.getTag(queryValue)
                }
            }
                .flow.cachedIn(viewModelScope)
                .map {
                    it.filter {
                        when (column) {
                            ArtEntryColumn.ARTISTS -> it.artists.contains(queryValue)
                            ArtEntryColumn.SOURCE -> TODO()
                            ArtEntryColumn.SERIES -> when (queryIdOrString) {
                                is Either.Left -> it.series.asSequence()
                                    .map(aniListDataConverter::databaseToSeriesEntry)
                                    .filterIsInstance<Entry.Prefilled<MediaColumnEntry>>()
                                    .any { it.value.id == queryIdOrString.value }
                                is Either.Right -> {
                                    it.series.any { it.contains(queryIdOrString.value) }
                                }
                            }
                            ArtEntryColumn.CHARACTERS -> when (queryIdOrString) {
                                is Either.Left -> it.characters.asSequence()
                                    .map(aniListDataConverter::databaseToCharacterEntry)
                                    .filterIsInstance<Entry.Prefilled<CharacterColumnEntry>>()
                                    .any { it.value.id == queryIdOrString.value }
                                is Either.Right -> {
                                    it.characters.any { it.contains(queryIdOrString.value) }
                                }
                            }
                            ArtEntryColumn.TAGS -> it.tags.contains(queryValue)
                        }
                    }
                        .map { ArtEntryGridModel.buildFromEntry(application, appJson, it) }
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

    fun onDeleteSelected() {
        super.deleteSelected()
    }
}