package com.thekeeperofpie.artistalleydatabase.alley.edit.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@AssistedInject
class ArtistCatalogsQueueViewModel(
    private val database: AlleyEditDatabase,
    @Assisted private val dataYear: DataYear,
) : ViewModel() {

    private val refreshFlow = RefreshFlow()
    val queue = refreshFlow.updates
        .mapLatest {
            database.loadArtistCatalogsQueue(dataYear)
                .map { (artistId, booth, link) ->
                    ArtistCatalogsQueueScreen.Catalog(
                        artistId = artistId,
                        booth = booth,
                        link = link,
                    )
                }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun refresh() = refreshFlow.refresh()

    fun deleteEntry(catalog: ArtistCatalogsQueueScreen.Catalog) {
        viewModelScope.launch {
            database.queueArtistCatalog(dataYear, catalog.artistId, catalog.booth, null)
            refreshFlow.refresh()
        }
    }
}
