package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistTableAutocomplete
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.rallies.StampRallyInference
import com.thekeeperofpie.artistalleydatabase.alley.edit.tags.TagAutocomplete
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
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
class StampRallyFormMergeViewModel(
    private val database: AlleyEditDatabase,
    private val dispatchers: CustomDispatchers,
    private val seriesImageLoader: SeriesImageLoader,
    val tagAutocomplete: TagAutocomplete,
    artistTableAutocomplete: ArtistTableAutocomplete,
    private val rallyInference: StampRallyInference,
    @Assisted private val dataYear: DataYear,
    @Assisted private val artistId: Uuid,
    @Assisted stampRallyId: String,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val entry =
        flowFromSuspend { database.loadStampRallyWithFormEntry(dataYear, artistId, stampRallyId) }
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val tablesByBooth = artistTableAutocomplete.tablesByBooth(dataYear)

    private val saveTask = ExclusiveTask(viewModelScope, ::save)
    private val deleteTask: ExclusiveTask<DeleteData, BackendRequest.StampRallyDeleteFromForm.Response> =
        ExclusiveTask(viewModelScope, ::delete)
    val saveTaskState get() = saveTask.state
    val deleteTaskState get() = deleteTask.state

    fun seriesImage(info: SeriesInfo) = seriesImageLoader.getSeriesImage(info)

    fun inferRallies(tables: List<String>, seriesIds: List<String>) =
        rallyInference.inferRallies(
            dataYear = dataYear,
            input = StampRallyInference.Input(
                tables = tables.toSet(),
                seriesIds = seriesIds.toSet(),
            )
        )

    fun onClickSave(
        images: List<EditImage>,
        updated: StampRallyDatabaseEntry,
        openEditAfter: Boolean,
    ) {
        val entry = entry.value ?: return
        saveTask.triggerManual {
            SaveData(
                images = images,
                initial = entry.stampRally,
                updated = updated,
                formEntryTimestamp = entry.formDiff.timestamp,
                openEditAfter = openEditAfter,
            )
        }
    }

    fun onConfirmDelete() {
        val entry = entry.value ?: return
        val stampRally = entry.stampRally ?: return
        deleteTask.triggerManual {
            DeleteData(
                initial = stampRally,
                formEntryTimestamp = entry.formDiff.timestamp,
            )
        }
    }

    private suspend fun save(data: SaveData) =
        withContext(dispatchers.io) {
            database.saveStampRallyAndClearFormEntry(
                dataYear = dataYear,
                artistId = artistId,
                initial = data.initial,
                updated = data.updated.copy(
                    images = data.images.map(EditImage::toCatalogImage)
                ),
                formEntryTimestamp = data.formEntryTimestamp,
            ) to data.updated.id.takeIf { data.openEditAfter }?.let(Uuid::parse)
        }

    private suspend fun delete(data: DeleteData) =
        withContext(dispatchers.io) {
            database.deleteStampRallyAndClearFormEntry(
                dataYear = dataYear,
                artistId = artistId,
                expected = data.initial,
                formEntryTimestamp = data.formEntryTimestamp,
            )
        }

    private data class SaveData(
        val images: List<EditImage>,
        val initial: StampRallyDatabaseEntry?,
        val updated: StampRallyDatabaseEntry,
        val formEntryTimestamp: Instant,
        val openEditAfter: Boolean,
    )

    private data class DeleteData(
        val initial: StampRallyDatabaseEntry,
        val formEntryTimestamp: Instant,
    )

    @AssistedFactory
    interface Factory {
        fun create(
            dataYear: DataYear,
            artistId: Uuid,
            stampRallyId: String,
            savedStateHandle: SavedStateHandle,
        ): StampRallyFormMergeViewModel
    }
}
