package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.PlatformImageCache
import com.thekeeperofpie.artistalleydatabase.alley.edit.tags.TagAutocomplete
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.ArtistSave
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.uuid.Uuid

@AssistedInject
class ArtistEditViewModel(
    private val database: AlleyEditDatabase,
    private val dispatchers: CustomDispatchers,
    seriesImagesStore: SeriesImagesStore,
    val tagAutocomplete: TagAutocomplete,
    @Assisted private val dataYear: DataYear,
    @Assisted private val artistId: Uuid,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val saveTask: ExclusiveTask<Triple<List<EditImage>, ArtistDatabaseEntry.Impl, Boolean>, ArtistSave.Response> =
        ExclusiveTask(viewModelScope, ::save)
    private val formLink = savedStateHandle.getMutableStateFlow<String?>("formLink", null)
    private val artist =
        savedStateHandle.getMutableStateFlow<ArtistDatabaseEntry.Impl?>(Json, "artist", null)
    private val formMetadata =
        savedStateHandle.getMutableStateFlow<ArtistEditScreen.State.FormMetadata?>(
            key = "formMetadata",
            initialValue = null,
        )
    private val artistJob: ExclusiveProgressJob<Unit, Unit> =
        ExclusiveProgressJob(viewModelScope, ::loadArtistInfo)
    val state = ArtistEditScreen.State(
        artistProgress = artistJob.state,
        initialArtist = artist,
        artistFormState = savedStateHandle.saveable(
            key = "artistFormState",
            saver = ArtistFormState.Saver,
            init = { ArtistFormState(artistId) },
        ),
        formMetadata = formMetadata,
        formLink = formLink,
        saveTaskState = saveTask.state,
    )

    private var hasLoaded by savedStateHandle.saved { false }
    private val imageLoader = SeriesImageLoader(dispatchers, viewModelScope, seriesImagesStore)

    private val formLinkJob = ExclusiveProgressJob(viewModelScope, ::loadFormLink)

    fun initialize(force: Boolean = false) {
        if (!hasLoaded || force) {
            artistJob.launch()
        }
    }

    private suspend fun loadArtistInfo() = withContext(dispatchers.io) {
        val response = database.loadArtistWithFormMetadata(dataYear, artistId)
        if (response == null) {
            hasLoaded = true
            return@withContext
        }
        val artist = response.artist
        val images = database.loadArtistImages(dataYear, artist)
        withContext(dispatchers.main) {
            Snapshot.withMutableSnapshot {
                this@ArtistEditViewModel.artist.value = artist
                state.artistFormState.applyDatabaseEntry(
                    artist = artist,
                    seriesById = tagAutocomplete.seriesById.first(),
                    merchById = tagAutocomplete.merchById.first(),
                    mergeBehavior = ArtistFormState.MergeBehavior.REPLACE,
                )
                formMetadata.value = ArtistEditScreen.State.FormMetadata(
                    hasPendingFormSubmission = response.hasPendingFormSubmission,
                    hasFormLink = response.hasFormLink,
                )

                if (images.isNotEmpty()) {
                    state.artistFormState.images.replaceAll(images)
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

    fun hasPendingChanges(): Boolean = artist.value.let {
        it != null && ArtistDatabaseEntry.hasChanged(
            before = it,
            after = state.artistFormState.captureDatabaseEntry(dataYear, it.verifiedArtist).second
        )
    }

    fun generateFormLink(forceRegenerate: Boolean) = formLinkJob.launch { forceRegenerate }
    fun onClearFormLink() {
        formMetadata.update {
            it?.copy(hasFormLink = true) ?: ArtistEditScreen.State.FormMetadata(
                hasPendingFormSubmission = false,
                hasFormLink = true,
            )
        }
        formLink.value = null
    }

    private suspend fun loadFormLink(forceRegenerate: Boolean) = withContext(dispatchers.io) {
        formLink.value = database.generateFormLink(dataYear, artistId, forceRegenerate)
    }

    private fun captureDatabaseEntry(
        isManual: Boolean,
    ): Triple<List<EditImage>, ArtistDatabaseEntry.Impl, Boolean> {
        val (images, databaseEntry) = state.artistFormState.captureDatabaseEntry(
            dataYear = dataYear,
            verifiedArtist = artist.value?.verifiedArtist == true,
        )
        return Triple(images, databaseEntry, isManual)
    }

    private suspend fun save(triple: Triple<List<EditImage>, ArtistDatabaseEntry.Impl, Boolean>) =
        withContext(dispatchers.io) {
            val (images, databaseEntry, isManual) = triple

            val hasChanged = ArtistDatabaseEntry.hasChanged(artist.value, databaseEntry)
            if (!isManual && !hasChanged && images.none { it is EditImage.LocalImage }) {
                // Don't save if no data has changed
                return@withContext ArtistSave.Response.Success
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
                            artistId = Uuid.parse(databaseEntry.id),
                            platformFile = file,
                            id = it.key.value,
                        )
                    }
                }
            }

            val updatedArtist = databaseEntry.copy(images = finalImages.map {
                CatalogImage(name = it.name, width = it.width, height = it.height)
            })
            database.saveArtist(
                dataYear = dataYear,
                initial = artist.value,
                updated = updatedArtist,
            ).also {
                if (it is ArtistSave.Response.Success) {
                    hasLoaded = false
                    artist.value = updatedArtist
                    if (!isManual) {
                        state.artistFormState.applyDatabaseEntry(
                            artist = updatedArtist,
                            seriesById = tagAutocomplete.seriesById.first(),
                            merchById = tagAutocomplete.merchById.first(),
                        )
                        state.artistFormState.images.replaceAll(finalImages)
                    }
                }
            }
        }

    @AssistedFactory
    interface Factory {
        fun create(
            dataYear: DataYear,
            artistId: Uuid,
            savedStateHandle: SavedStateHandle,
        ): ArtistEditViewModel
    }
}
