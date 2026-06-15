package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies.links

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.rallies.StampRallyCache
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@AssistedInject
class StampRallyLinksQueueViewModel(
    private val database: AlleyEditDatabase,
    stampRallyCache: StampRallyCache,
    @Assisted private val dataYear: DataYear,
) : ViewModel() {

    private val refreshFlow = RefreshFlow()
    val queue = refreshFlow.updates
        .mapLatest { database.loadStampRalliesQueue(dataYear) }
        .combine(stampRallyCache.stampRallies(dataYear), ::Pair)
        .mapLatest { (entries, stampRallies) ->
            entries.map {
                StampRallyLinksQueueScreen.Entry(
                    link = it.link,
                    booths = it.booths.sorted(),
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun refresh() = refreshFlow.refresh()

    fun deleteEntry(entry: StampRallyLinksQueueScreen.Entry) {
        viewModelScope.launch {
            database.deleteStampRallyQueueEntry(dataYear, entry.link)
            refreshFlow.refresh()
        }
    }
}
