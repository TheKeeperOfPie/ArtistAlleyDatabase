package com.thekeeperofpie.artistalleydatabase.alley.edit.series

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistCache
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.tags.TagAutocomplete
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.ArtistSave
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.series.toImageInfo
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.ExclusiveProgressJob
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@AssistedInject
class SeriesResolutionViewModel(
    private val artistCache: ArtistCache,
    private val editDatabase: AlleyEditDatabase,
    dispatchers: CustomDispatchers,
    navigationTypeMap: NavigationTypeMap,
    private val tagAutocomplete: TagAutocomplete,
    seriesImagesStore: SeriesImagesStore,
    @Assisted private val seriesId: String,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val imageLoader = SeriesImageLoader(dispatchers, viewModelScope, seriesImagesStore)
    private val commitJob = ExclusiveProgressJob(viewModelScope, ::commit)

    val artists = flowFromSuspend { loadArtists() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val progress get() = commitJob.state

    fun seriesPredictions(query: String) =
        tagAutocomplete.seriesPredictions(query, allowCustom = false)

    fun seriesImage(info: SeriesInfo) = imageLoader.getSeriesImage(info.toImageInfo())

    fun onClickDone(series: SeriesInfo) = commitJob.launch { series }

    private fun loadArtists() = artistCache.artistsAnimeExpo2026.value.filter {
        it.seriesInferred.contains(seriesId) ||
                it.seriesConfirmed.contains(seriesId)
    }

    private suspend fun commit(series: SeriesInfo): ArtistSave.Response {
        loadArtists().forEach {
            val artist = editDatabase.loadArtist(DataYear.ANIME_EXPO_2026, it.id) ?: return@forEach
            val response = editDatabase.saveArtist(
                dataYear = DataYear.ANIME_EXPO_2026,
                initial = artist,
                updated = artist.copy(
                    seriesInferred = artist.seriesInferred.map { if (it == seriesId) series.id else it },
                    seriesConfirmed = artist.seriesConfirmed.map { if (it == seriesId) series.id else it },
                )
            )
            if (response != ArtistSave.Response.Success) {
                return@commit response
            }
        }

        return ArtistSave.Response.Success
    }

    @AssistedFactory
    interface Factory {
        fun create(seriesId: String, savedStateHandle: SavedStateHandle): SeriesResolutionViewModel
    }
}
