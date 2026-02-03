package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.tags.TagAutocomplete
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyHistoryEntry
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.series.toImageInfo
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.ExclusiveProgressJob
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Clock

@AssistedInject
class StampRallyHistoryViewModel(
    private val database: AlleyEditDatabase,
    dispatchers: CustomDispatchers,
    seriesImagesStore: SeriesImagesStore,
    val tagAutocomplete: TagAutocomplete,
    @Assisted private val dataYear: DataYear,
    @Assisted private val stampRallyId: String,
) : ViewModel() {
    private val refreshFlow = RefreshFlow()

    val initial = refreshFlow.updates
        .mapLatest { database.loadStampRally(dataYear, stampRallyId) }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
    val history = refreshFlow.updates
        .mapLatest { database.loadStampRallyHistory(dataYear, stampRallyId) }
        .mapLatest(StampRallyHistoryEntryWithDiff::calculateDiffs)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    private val imageLoader = SeriesImageLoader(dispatchers, viewModelScope, seriesImagesStore)

    fun seriesImage(info: SeriesInfo) = imageLoader.getSeriesImage(info.toImageInfo())

    private val saveJob = ExclusiveProgressJob(viewModelScope, ::save)
    val saveProgress = saveJob.state

    fun onClickRefresh() = refreshFlow.refresh()

    fun onApplied(entry: StampRallyHistoryEntry) {
        val initial = initial.value ?: return
        saveJob.launch {
            initial.copy(
                fandom = entry.fandom ?: initial.fandom,
                hostTable = entry.hostTable ?: initial.hostTable,
                tables = entry.tables ?: initial.tables,
                links = entry.links ?: initial.links,
                tableMin = entry.tableMin ?: initial.tableMin,
                totalCost = entry.totalCost ?: initial.totalCost,
                prize = entry.prize ?: initial.prize,
                prizeLimit = entry.prizeLimit ?: initial.prizeLimit,
                series = entry.series ?: initial.series,
                merch = entry.merch ?: initial.merch,
                notes = entry.notes ?: initial.notes,
                images = entry.images ?: initial.images,
                confirmed = entry.confirmed ?: initial.confirmed,
                editorNotes = entry.editorNotes ?: initial.editorNotes,
                lastEditor = null,
                lastEditTime = Clock.System.now(),
            )
        }
    }

    private suspend fun save(stampRally: StampRallyDatabaseEntry) =
        database.saveStampRally(dataYear, initial.value, stampRally)

    @AssistedFactory
    interface Factory {
        fun create(
            dataYear: DataYear,
            stampRallyId: String,
        ): StampRallyHistoryViewModel
    }
}
