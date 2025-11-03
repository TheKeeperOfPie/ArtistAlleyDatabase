package com.thekeeperofpie.artistalleydatabase.art.browse.selection

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
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryBrowseDao
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryColumn
import com.thekeeperofpie.artistalleydatabase.art.grid.ArtEntryGridModel
import com.thekeeperofpie.artistalleydatabase.art.grid.ArtEntryGridViewModel
import com.thekeeperofpie.artistalleydatabase.data.Character
import com.thekeeperofpie.artistalleydatabase.data.Series
import com.thekeeperofpie.artistalleydatabase.utils.Either
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Inject
class ArtBrowseSelectionViewModel(
    appFileSystem: AppFileSystem,
    private val artEntryBrowseDao: ArtEntryBrowseDao,
    private val json: Json,
) : ArtEntryGridViewModel(appFileSystem, artEntryBrowseDao) {

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
                                is Either.Left -> it.series(json)
                                    .filterIsInstance<Series.AniList>()
                                    .any { it.id == queryIdOrString.value }
                                is Either.Right -> {
                                    it.series(json)
                                        .any { it.text.contains(queryIdOrString.value) }
                                }
                            }
                            ArtEntryColumn.CHARACTERS -> when (queryIdOrString) {
                                is Either.Left -> it.characters(json)
                                    .filterIsInstance<Character.AniList>()
                                    .any { it.id == queryIdOrString.value }
                                is Either.Right -> {
                                    it.characters(json)
                                        .any { it.text.contains(queryIdOrString.value) }
                                }
                            }
                            ArtEntryColumn.TAGS -> it.tags.contains(queryValue)
                        }
                    }
                        .map { ArtEntryGridModel.buildFromEntry(appFileSystem, json, it) }
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
