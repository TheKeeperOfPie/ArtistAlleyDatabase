package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistHistoryEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
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
import kotlin.uuid.Uuid

@AssistedInject
class ArtistHistoryViewModel(
    private val database: AlleyEditDatabase,
    dispatchers: CustomDispatchers,
    seriesImagesStore: SeriesImagesStore,
    @Assisted private val dataYear: DataYear,
    @Assisted private val artistId: Uuid,
) : ViewModel() {
    private val refreshFlow = RefreshFlow()

    val initial = refreshFlow.updates
        .mapLatest { database.loadArtist(dataYear, artistId) }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
    val history = refreshFlow.updates
        .mapLatest { database.loadArtistHistory(dataYear, artistId) }
        .mapLatest(ArtistHistoryEntryWithDiff::calculateDiffs)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val seriesById = flowFromSuspend { database.loadSeries() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())
    val merchById = flowFromSuspend { database.loadMerch() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())
    private val imageLoader = SeriesImageLoader(dispatchers, viewModelScope, seriesImagesStore)

    fun seriesImage(info: SeriesInfo) = imageLoader.getSeriesImage(info.toImageInfo())

    private val saveJob = ExclusiveProgressJob(viewModelScope, ::save)
    val saveProgress = saveJob.state

    fun onClickRefresh() = refreshFlow.refresh()

    fun onApplied(entry: ArtistHistoryEntry) {
        val initial = initial.value ?: return
        saveJob.launch {
            initial.copy(
                status = entry.status ?: initial.status,
                booth = entry.booth ?: initial.booth,
                name = entry.name ?: initial.name,
                summary = entry.summary ?: initial.summary,
                socialLinks = entry.socialLinks ?: initial.socialLinks,
                storeLinks = entry.storeLinks ?: initial.storeLinks,
                portfolioLinks = entry.portfolioLinks ?: initial.portfolioLinks,
                catalogLinks = entry.catalogLinks ?: initial.catalogLinks,
                notes = entry.notes ?: initial.notes,
                commissions = entry.commissions ?: initial.commissions,
                seriesInferred = entry.seriesInferred ?: initial.seriesInferred,
                seriesConfirmed = entry.seriesConfirmed ?: initial.seriesConfirmed,
                merchInferred = entry.merchInferred ?: initial.merchInferred,
                merchConfirmed = entry.merchConfirmed ?: initial.merchConfirmed,
                images = entry.images ?: initial.images,
                editorNotes = entry.editorNotes ?: initial.editorNotes,
                lastEditor = null,
                lastEditTime = Clock.System.now(),
            )
        }
    }

    private suspend fun save(artist: ArtistDatabaseEntry.Impl) =
        database.saveArtist(dataYear, initial.value, artist)

    @AssistedFactory
    interface Factory {
        fun create(
            dataYear: DataYear,
            artistId: Uuid,
        ): ArtistHistoryViewModel
    }
}
