package com.thekeeperofpie.artistalleydatabase.alley.edit.artist.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.tags.TagAutocomplete
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
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
class ArtistFormHistoryViewModel(
    private val database: AlleyEditDatabase,
    private val dispatchers: CustomDispatchers,
    seriesImagesStore: SeriesImagesStore,
    val tagAutocomplete: TagAutocomplete,
    @Assisted private val dataYear: DataYear,
    @Assisted artistId: Uuid,
    @Assisted formTimestamp: Instant,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val entry = flowFromSuspend {
        database.loadArtistWithHistoricalFormEntry(
            dataYear,
            artistId,
            formTimestamp
        )
    }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
    private val imageLoader = SeriesImageLoader(dispatchers, viewModelScope, seriesImagesStore)
    private val saveTask = ExclusiveTask(viewModelScope, ::save)
    val saveTaskState get() = saveTask.state

    fun seriesImage(info: SeriesInfo) = imageLoader.getSeriesImage(info.toImageInfo())

    fun onClickSave(
        images: List<EditImage>,
        updated: ArtistDatabaseEntry.Impl,
        openArtistEditAfter: Boolean,
    ) {
        val entry = entry.value ?: return
        saveTask.triggerManual {
            SaveData(
                images = images,
                initial = entry.artist,
                updated = updated,
                formEntryTimestamp = entry.formDiff.timestamp,
                openArtistEditAfter = openArtistEditAfter,
            )
        }
    }

    private suspend fun save(data: SaveData) =
        withContext(dispatchers.io) {
            database.saveArtistAndClearFormEntry(
                dataYear = dataYear,
                initial = data.initial,
                // History doesn't support restoring images because older images may have been
                // deleted to save storage space
                updated = data.updated.copy(_images = data.initial.images),
                formEntryTimestamp = data.formEntryTimestamp,
            ) to data.updated.id.takeIf { data.openArtistEditAfter }?.let(Uuid::parse)
        }

    private data class SaveData(
        val images: List<EditImage>,
        val initial: ArtistDatabaseEntry.Impl,
        val updated: ArtistDatabaseEntry.Impl,
        val formEntryTimestamp: Instant,
        val openArtistEditAfter: Boolean,
    )

    @AssistedFactory
    interface Factory {
        fun create(
            dataYear: DataYear,
            artistId: Uuid,
            formTimestamp: Instant,
            savedStateHandle: SavedStateHandle,
        ): ArtistFormHistoryViewModel
    }
}
