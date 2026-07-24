package com.thekeeperofpie.artistalleydatabase.alley.edit.series

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistCache
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.tags.EditTagAutocomplete
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.ExclusiveProgressJob
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn

@AssistedInject
class SeriesResolutionViewModel(
    private val artistCache: ArtistCache,
    private val editDatabase: AlleyEditDatabase,
    private val tagAutocomplete: EditTagAutocomplete,
    private val seriesImageLoader: SeriesImageLoader,
    @Assisted private val seriesId: String,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val commitJob = ExclusiveProgressJob(viewModelScope, ::commit)

    val artists = flowFromSuspend { loadArtists() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val progress get() = commitJob.state

    fun seriesPredictions(query: String) =
        tagAutocomplete.seriesPredictions(query, allowCustom = false)

    fun seriesImage(info: SeriesInfo) = seriesImageLoader.getSeriesImage(info)

    fun onClickDone(series: SeriesInfo) = commitJob.launch { series }

    private suspend fun loadArtists() = artistCache.artists(DataYear.LATEST).first().filter {
        it.seriesInferred.contains(seriesId) ||
                it.seriesConfirmed.contains(seriesId)
    }

    private suspend fun commit(series: SeriesInfo): BackendRequest.ArtistSave.Response {
        loadArtists().forEach {
            val artist = editDatabase.loadArtist(DataYear.LATEST, it.id) ?: return@forEach
            val response = editDatabase.saveArtist(
                dataYear = DataYear.LATEST,
                initial = artist,
                updated = artist.copy(
                    seriesInferred = artist.seriesInferred.map { if (it == seriesId) series.id else it },
                    seriesConfirmed = artist.seriesConfirmed.map { if (it == seriesId) series.id else it },
                )
            )
            if (response != BackendRequest.ArtistSave.Response.Success) {
                return@commit response
            }
        }

        return BackendRequest.ArtistSave.Response.Success
    }

    @AssistedFactory
    interface Factory {
        fun create(seriesId: String, savedStateHandle: SavedStateHandle): SeriesResolutionViewModel
    }
}
