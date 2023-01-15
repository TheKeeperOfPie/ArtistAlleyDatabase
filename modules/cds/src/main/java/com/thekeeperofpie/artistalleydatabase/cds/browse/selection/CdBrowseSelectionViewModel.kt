package com.thekeeperofpie.artistalleydatabase.cds.browse.selection

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
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryBrowseDao
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryColumn
import com.thekeeperofpie.artistalleydatabase.cds.grid.CdEntryGridModel
import com.thekeeperofpie.artistalleydatabase.cds.grid.CdEntryGridViewModel
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDataConverter
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistColumnEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CdBrowseSelectionViewModel @Inject constructor(
    application: Application,
    private val cdEntryBrowseDao: CdEntryBrowseDao,
    private val appJson: AppJson,
    private val vgmdbDataConverter: VgmdbDataConverter,
) : CdEntryGridViewModel(application, cdEntryBrowseDao) {

    lateinit var column: CdEntryColumn

    var loading by mutableStateOf(false)
    val entries = MutableStateFlow(PagingData.empty<CdEntryGridModel>())

    fun initialize(column: CdEntryColumn, queryIdOrString: Either<String, String>) {
        if (this::column.isInitialized) return
        this.column = column

        viewModelScope.launch(Dispatchers.IO) {
            val queryValue = queryIdOrString.eitherValueUnchecked().toString()
            Pager(PagingConfig(pageSize = 20)) {
                when (column) {
                    CdEntryColumn.PERFORMERS -> cdEntryBrowseDao.getPerformer(queryValue)
                }
            }
                .flow.cachedIn(viewModelScope)
                .map {
                    it.filter {
                        when (column) {
                            CdEntryColumn.PERFORMERS -> when (queryIdOrString) {
                                is Either.Left -> {
                                    it.performers.map(vgmdbDataConverter::databaseToArtistColumn)
                                        .filterIsInstance<Either.Right<String, ArtistColumnEntry>>()
                                        .any {
                                            it.value.id == queryIdOrString.value
                                        }
                                }
                                is Either.Right -> {
                                    val queryString = queryIdOrString.value
                                    it.performers.map(vgmdbDataConverter::databaseToArtistColumn)
                                        .any {
                                            if (it is Either.Left) {
                                                it.value == queryString
                                            } else {
                                                it.rightOrNull()!!.names
                                                    .any { it.value == queryString }
                                            }
                                        }
                                }
                            }
                        }
                    }
                        .map { CdEntryGridModel.buildFromEntry(application, appJson, it) }
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