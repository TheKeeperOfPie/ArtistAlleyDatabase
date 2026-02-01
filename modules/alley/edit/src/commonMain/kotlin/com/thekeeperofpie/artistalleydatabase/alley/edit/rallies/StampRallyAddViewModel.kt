package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
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
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.ExclusiveTask
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.replaceAll
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@AssistedInject
class StampRallyAddViewModel(
    private val database: AlleyEditDatabase,
    private val dispatchers: CustomDispatchers,
    val tagAutocomplete: TagAutocomplete,
    seriesImagesStore: SeriesImagesStore,
    @Assisted private val dataYear: DataYear,
    @Assisted stampRallyId: String,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val saveTask: ExclusiveTask<Triple<List<EditImage>, StampRallyDatabaseEntry, Boolean>, BackendRequest.StampRallySave.Response> =
        ExclusiveTask(viewModelScope, ::save)
    private val imageLoader = SeriesImageLoader(dispatchers, viewModelScope, seriesImagesStore)

    private val stampRallyFormState = savedStateHandle.saveable(
        key = "stampRallyFormState",
        saver = StampRallyFormState.Saver,
        init = { StampRallyFormState(stampRallyId) },
    )

    val state = StampRallyAddScreen.State(
        stampRallyFormState = stampRallyFormState,
        saveTaskState = saveTask.state,
    )

    fun seriesPredictions(query: String) = tagAutocomplete.seriesPredictions(query)
    fun merchPredictions(query: String) = tagAutocomplete.merchPredictions(query)

    fun seriesImage(info: SeriesInfo) = imageLoader.getSeriesImage(info.toImageInfo())

    fun onClickSave() = saveTask.triggerAuto { captureDatabaseEntry(false) }
    fun onClickDone() = saveTask.triggerManual { captureDatabaseEntry(true) }

    private fun captureDatabaseEntry(
        isManual: Boolean,
    ): Triple<List<EditImage>, StampRallyDatabaseEntry, Boolean> {
        val (images, databaseEntry) = state.stampRallyFormState
            .captureDatabaseEntry(dataYear = dataYear)
        return Triple(images, databaseEntry, isManual)
    }

    private suspend fun save(triple: Triple<List<EditImage>, StampRallyDatabaseEntry, Boolean>) =
        withContext(dispatchers.io) {
            val (images, databaseEntry, isManual) = triple

            val hasChanged = StampRallyDatabaseEntry.hasChanged(null, databaseEntry)
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
                initial = null,
                updated = updatedStampRally,
            ).also {
                if (it is BackendRequest.StampRallySave.Response.Success) {
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
        ): StampRallyAddViewModel
    }
}
