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
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.ImageUploader
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.ImageUtils
import com.thekeeperofpie.artistalleydatabase.alley.edit.tags.TagAutocomplete
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.series.toImageInfo
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DatabaseImage
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
    private val imageUploader: ImageUploader,
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
        withContext(dispatchers.main) {
            Snapshot.withMutableSnapshot {
                this@StampRallyEditViewModel.stampRally.value = stampRally
                state.stampRallyFormState.applyDatabaseEntry(
                    stampRally = stampRally,
                    seriesById = tagAutocomplete.seriesById.first(),
                    merchById = tagAutocomplete.merchById.first(),
                    mergeBehavior = FormMergeBehavior.REPLACE,
                )
                state.stampRallyFormState.images
                    .replaceAll(stampRally.images.map(ImageUtils::toEditImage))
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

            val stampRallyId = triple.second.id
            val imagesResult = imageUploader.uploadImages(
                dataYear = dataYear,
                artistId = null,
                artistImages = emptyList(),
                stampRallyImages = mapOf(stampRallyId to triple.first),
            )

            val (stampRallyCatalogImages, uploadedImages) = when (imagesResult) {
                ImageUploader.UploadResult.Empty -> emptyList<DatabaseImage>() to emptyMap()
                is ImageUploader.UploadResult.Error ->
                    return@withContext BackendRequest.StampRallySave.Response.Failed(imagesResult.message)
                is ImageUploader.UploadResult.Success ->
                    imagesResult.stampRallyCatalogImages[stampRallyId].orEmpty() to imagesResult.uploadedImages
            }

            val newStampRallyImages = state.stampRallyFormState.images.toList()
                .map { uploadedImages[it] ?: it }
            state.stampRallyFormState.images.replaceAll(newStampRallyImages)

            val updatedStampRally = databaseEntry.copy(images = stampRallyCatalogImages)
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
