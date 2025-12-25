package com.thekeeperofpie.artistalleydatabase.alley.edit.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.ArtistSave
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlin.time.Instant
import kotlin.uuid.Uuid

@AssistedInject
class ArtistFormMergeViewModel(
    private val database: AlleyEditDatabase,
    private val dispatchers: CustomDispatchers,
    seriesImagesStore: SeriesImagesStore,
    @Assisted private val dataYear: DataYear,
    @Assisted artistId: Uuid,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val entry = flowFromSuspend { database.loadArtistWithFormEntry(dataYear, artistId) }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
    val seriesById = flowFromSuspend { database.loadSeries() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())
    val merchById = flowFromSuspend { database.loadMerch() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())
    private val imageLoader = SeriesImageLoader(dispatchers, viewModelScope, seriesImagesStore)
    private val saveTask: ExclusiveTask<SaveData, BackendRequest.ArtistCommitForm.Response> =
        ExclusiveTask(viewModelScope, ::save)
    val saveTaskState get() = saveTask.state

    fun seriesImage(info: SeriesInfo) = imageLoader.getSeriesImage(info.toImageInfo())

    fun onClickSave(images: List<EditImage>, updated: ArtistDatabaseEntry.Impl) {
        val entry = entry.value ?: return
        saveTask.triggerManual {
            SaveData(
                images = images,
                initial = entry.artist,
                updated = updated,
                formEntryTimestamp = entry.formDiff.timestamp,
            )
        }
    }

    private suspend fun save(data: SaveData) =
        withContext(dispatchers.io) {
            // TODO: Image support
            database.saveArtistAndClearFormEntry(
                dataYear = dataYear,
                initial = data.initial,
                updated = data.updated,
                formEntryTimestamp = data.formEntryTimestamp,
            )
        }

    private data class SaveData(
        val images: List<EditImage>,
        val initial: ArtistDatabaseEntry.Impl,
        val updated: ArtistDatabaseEntry.Impl,
        val formEntryTimestamp: Instant,
    )

    @AssistedFactory
    interface Factory {
        fun create(
            dataYear: DataYear,
            artistId: Uuid,
            savedStateHandle: SavedStateHandle,
        ): ArtistFormMergeViewModel
    }
}
