package com.thekeeperofpie.artistalleydatabase.alley.edit.remote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistFormState
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.form.FormMergeBehavior
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.tags.TagAutocomplete
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistRemoteEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.series.toImageInfo
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.ExclusiveTask
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlin.time.Instant
import kotlin.uuid.Uuid

@AssistedInject
class RemoteArtistDataHistoryMergeViewModel(
    private val database: AlleyEditDatabase,
    private val dispatchers: CustomDispatchers,
    seriesImagesStore: SeriesImagesStore,
    val tagAutocomplete: TagAutocomplete,
    @Assisted private val dataYear: DataYear,
    @Assisted private val id: ArtistRemoteEntry.Id,
    @Assisted private val timestamp: Instant,
) : ViewModel() {
    internal val entry = flowFromSuspend {
        database.loadRemoteArtistDataHistory(dataYear, id, timestamp)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Not saved since this is supposed to be read-only
    internal val entryInfo =
        entry.mapLatest {
            val artistId = it?.confirmedId
            if (artistId == null) {
                ArtistFormState(Uuid.random()) to null
            } else {
                val artist = database.loadArtist(dataYear, artistId)
                val entryInfo = RemoteArtistDataMergeScreen.EntryInfo(
                    artistId = artistId,
                    artist = artist,
                    previousEntry = null,
                    diff = RemoteArtistDataDiff.diff(
                        artist = artist,
                        previousEntry = null,
                        currentEntry = it,
                    )
                )
                if (artist == null) {
                    ArtistFormState(Uuid.random()) to entryInfo
                } else {
                    ArtistFormState(Uuid.parse(artist.id))
                        .applyDatabaseEntry(
                            artist = artist,
                            seriesById = tagAutocomplete.seriesById.first(),
                            merchById = tagAutocomplete.merchById.first(),
                            mergeBehavior = FormMergeBehavior.REPLACE,
                        ) to entryInfo
                }
            }
        }
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val imageLoader = SeriesImageLoader(dispatchers, viewModelScope, seriesImagesStore)
    private val saveTask: ExclusiveTask<SaveData, BackendRequest.SaveRemoteArtistData.Response> =
        ExclusiveTask(viewModelScope, ::save)
    val saveTaskState get() = saveTask.state

    fun seriesImage(info: SeriesInfo) = imageLoader.getSeriesImage(info.toImageInfo())

    fun onClickSave(images: List<EditImage>, updated: ArtistDatabaseEntry.Impl) {
        val entry = entry.value ?: return
        val initial = entryInfo.value?.second?.artist
        saveTask.triggerManual {
            SaveData(
                images = images,
                initial = initial,
                updated = updated,
                entry = entry,
            )
        }
    }

    private suspend fun save(data: SaveData) =
        withContext(dispatchers.io) {
            database.saveRemoteArtistData(
                dataYear = dataYear,
                initial = data.initial,
                updated = data.updated,
                entry = data.entry,
                isHistory = true,
            )
        }

    private data class SaveData(
        val images: List<EditImage>,
        val initial: ArtistDatabaseEntry.Impl?,
        val updated: ArtistDatabaseEntry.Impl,
        val entry: ArtistRemoteEntry,
    )

    @AssistedFactory
    interface Factory {
        fun create(
            dataYear: DataYear,
            id: ArtistRemoteEntry.Id,
            timestamp: Instant,
        ): RemoteArtistDataHistoryMergeViewModel
    }
}
