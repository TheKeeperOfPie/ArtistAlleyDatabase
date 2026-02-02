package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies

import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.form.FormMergeBehavior
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.PlatformImageCache
import com.thekeeperofpie.artistalleydatabase.alley.edit.tags.TagAutocomplete
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.series.toImageInfo
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.ExclusiveProgressJob
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.launch
import com.thekeeperofpie.artistalleydatabase.utils_compose.ExclusiveTask
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.replaceAll
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

@AssistedInject
class StampRallyEditViewModel(
    private val database: AlleyEditDatabase,
    private val dispatchers: CustomDispatchers,
    seriesImagesStore: SeriesImagesStore,
    val tagAutocomplete: TagAutocomplete,
    @Assisted private val dataYear: DataYear,
    @Assisted private val stampRallyId: String,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val saveTask: ExclusiveTask<Triple<List<EditImage>, StampRallyDatabaseEntry, Boolean>, BackendRequest.StampRallySave.Response> =
        ExclusiveTask(viewModelScope, ::save)
    private val stampRally =
        savedStateHandle.getMutableStateFlow<StampRallyDatabaseEntry?>(Json, "stampRally", null)
    private val stampRallyJob: ExclusiveProgressJob<Unit, Unit> =
        ExclusiveProgressJob(viewModelScope, ::loadStampRallyInfo)

    private val stampRallyFormState = savedStateHandle.saveable(
        key = "stampRallyFormState",
        saver = StampRallyFormState.Saver,
        init = { StampRallyFormState(stampRallyId) },
    )
    private val deleteJob = ExclusiveProgressJob(viewModelScope, ::delete)

    val state = StampRallyEditScreen.State(
        stampRallyProgress = stampRallyJob.state,
        initialStampRally = stampRally,
        stampRallyFormState = stampRallyFormState,
        saveTaskState = saveTask.state,
        deleteProgress = deleteJob.state,
    )

    private var hasLoaded by savedStateHandle.saved { false }
    private val imageLoader = SeriesImageLoader(dispatchers, viewModelScope, seriesImagesStore)

    fun initialize(force: Boolean = false) {
        if (!hasLoaded || force) {
            stampRallyJob.launch()
        }
    }

    private suspend fun loadStampRallyInfo() = withContext(dispatchers.io) {
        val stampRally = database.loadStampRally(dataYear, stampRallyId)
        if (stampRally == null) {
            hasLoaded = true
            return@withContext
        }
        val images = database.loadStampRallyImages(dataYear, stampRally)
        withContext(dispatchers.main) {
            Snapshot.withMutableSnapshot {
                this@StampRallyEditViewModel.stampRally.value = stampRally
                state.stampRallyFormState.applyDatabaseEntry(
                    stampRally = stampRally,
                    seriesById = tagAutocomplete.seriesById.first(),
                    merchById = tagAutocomplete.merchById.first(),
                    mergeBehavior = FormMergeBehavior.REPLACE,
                )

                if (images.isNotEmpty()) {
                    state.stampRallyFormState.images.replaceAll(images)
                }
            }
        }
        hasLoaded = true
    }

    fun seriesPredictions(query: String) = tagAutocomplete.seriesPredictions(query)
    fun merchPredictions(query: String) = tagAutocomplete.merchPredictions(query)

    fun seriesImage(info: SeriesInfo) = imageLoader.getSeriesImage(info.toImageInfo())

    fun onClickSave() = saveTask.triggerAuto { captureDatabaseEntry(false) }
    fun onClickDone() = saveTask.triggerManual { captureDatabaseEntry(true) }

    fun hasPendingChanges(): Boolean = stampRally.value.let {
        it != null && StampRallyDatabaseEntry.hasChanged(
            before = it,
            after = state.stampRallyFormState.captureDatabaseEntry(dataYear).second
        )
    }

    fun onConfirmDelete() {
        val stampRally = stampRally.value ?: return
        deleteJob.launch { stampRally }
    }

    private suspend fun delete(stampRally: StampRallyDatabaseEntry) = withContext(dispatchers.io) {
        database.deleteStampRally(dataYear, stampRally)
    }

    private fun captureDatabaseEntry(
        isManual: Boolean,
    ): Triple<List<EditImage>, StampRallyDatabaseEntry, Boolean> {
        val (images, databaseEntry) = state.stampRallyFormState.captureDatabaseEntry(dataYear = dataYear)
        return Triple(images, databaseEntry, isManual)
    }

    private suspend fun save(triple: Triple<List<EditImage>, StampRallyDatabaseEntry, Boolean>) =
        withContext(dispatchers.io) {
            val (images, databaseEntry, isManual) = triple

            val hasChanged = StampRallyDatabaseEntry.hasChanged(stampRally.value, databaseEntry)
            if (!isManual && !hasChanged && images.none { it is EditImage.LocalImage }) {
                // Don't save if no data has changed
                return@withContext BackendRequest.StampRallySave.Response.Success
            }
            val finalImages = images.mapNotNull {
                when (it) {
                    is EditImage.DatabaseImage,
                    is EditImage.NetworkImage,
                        -> it
                    is EditImage.LocalImage -> {
                        // TODO: Error handling
                        val file = PlatformImageCache[it.key] ?: return@mapNotNull null
                        database.uploadImage(
                            dataYear = dataYear,
                            stampRallyId = databaseEntry.id,
                            platformFile = file,
                            id = it.key.value,
                        )
                    }
                }
            }

            val updatedStampRally = databaseEntry.copy(images = finalImages.map {
                CatalogImage(name = it.name, width = it.width, height = it.height)
            })
            database.saveStampRally(
                dataYear = dataYear,
                initial = stampRally.value,
                updated = updatedStampRally,
            ).also {
                if (it is BackendRequest.StampRallySave.Response.Success) {
                    hasLoaded = false
                    stampRally.value = updatedStampRally
                    if (!isManual) {
                        state.stampRallyFormState.applyDatabaseEntry(
                            stampRally = updatedStampRally,
                            seriesById = tagAutocomplete.seriesById.first(),
                            merchById = tagAutocomplete.merchById.first(),
                        )
                        state.stampRallyFormState.images.replaceAll(finalImages)
                    }
                }
            }
        }

    @AssistedFactory
    interface Factory {
        fun create(
            dataYear: DataYear,
            stampRallyId: String,
            savedStateHandle: SavedStateHandle,
        ): StampRallyEditViewModel
    }
}
