package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
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
import com.thekeeperofpie.artistalleydatabase.entry.EntryLockState
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.ExclusiveProgressJob
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
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
import kotlin.uuid.Uuid

@AssistedInject
class ArtistEditViewModel(
    private val database: AlleyEditDatabase,
    private val dispatchers: CustomDispatchers,
    seriesImagesStore: SeriesImagesStore,
    @Assisted private val dataYear: DataYear,
    @Assisted private val artistId: Uuid,
    @Assisted val mode: ArtistEditScreen.Mode,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val saveTask: ExclusiveTask<Triple<List<EditImage>, ArtistDatabaseEntry.Impl, Boolean>, ArtistSave.Response> =
        ExclusiveTask(viewModelScope, ::save)
    private val formLink = savedStateHandle.getMutableStateFlow<String?>("formLink", null)
    private val artist =
        savedStateHandle.getMutableStateFlow<ArtistDatabaseEntry.Impl?>(Json, "artist", null)
    val state = ArtistEditScreen.State(
        initialArtist = artist,
        artistFormState = savedStateHandle.saveable(
            key = "artistFormState",
            saver = ArtistFormState.Saver,
        ) {
            ArtistFormState().apply {
                editorState.id.value.setTextAndPlaceCursorAtEnd(artistId.toString())
                editorState.id.lockState = EntryLockState.LOCKED
            }
        },
        formLink = formLink,
        saveTaskState = saveTask.state,
    )

    private var hasLoaded by savedStateHandle.saved { mode == ArtistEditScreen.Mode.ADD }
    private val imageLoader = SeriesImageLoader(dispatchers, viewModelScope, seriesImagesStore)
    private val tagAutocomplete = TagAutocomplete(viewModelScope, database, dispatchers)

    private val artistJob = ExclusiveProgressJob(viewModelScope, ::loadArtistInfo)
    private val formLinkJob = ExclusiveProgressJob(viewModelScope, ::loadFormLink)

    fun initialize(force: Boolean = false) {
        if (!hasLoaded || force) {
            artistJob.launch()
        }
    }

    private suspend fun loadArtistInfo() = withContext(PlatformDispatchers.IO) {
        val artist = database.loadArtist(dataYear, artistId)
        if (artist == null) {
            hasLoaded = true
            return@withContext
        }
        this@ArtistEditViewModel.artist.value = artist
        state.artistFormState.applyDatabaseEntry(
            artist = artist,
            seriesById = tagAutocomplete.seriesById.first(),
            merchById = tagAutocomplete.merchById.first(),
            force = true,
        )

        val images = database.loadArtistImages(dataYear, artist)
        if (images.isNotEmpty()) {
            state.artistFormState.images.replaceAll(images)
        }
        hasLoaded = true
    }

    fun seriesPredictions(query: String) = tagAutocomplete.seriesPredictions(query)
    fun merchPredictions(query: String) = tagAutocomplete.merchPredictions(query)

    fun seriesImage(info: SeriesInfo) = imageLoader.getSeriesImage(info.toImageInfo())

    fun onClickSave() = saveTask.triggerAuto { captureDatabaseEntry(false) }
    fun onClickDone() = saveTask.triggerManual { captureDatabaseEntry(true) }

    fun generateFormLink() = formLinkJob.launch()

    private suspend fun loadFormLink() = withContext(dispatchers.io) {
        formLink.value = database.generateFormLink(dataYear, artistId)
    }

    private fun captureDatabaseEntry(
        isManual: Boolean,
    ): Triple<List<EditImage>, ArtistDatabaseEntry.Impl, Boolean> {
        val (images, databaseEntry) = state.artistFormState.captureDatabaseEntry(dataYear)
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
                            force = false,
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
            mode: ArtistEditScreen.Mode,
            savedStateHandle: SavedStateHandle,
        ): ArtistEditViewModel
    }
}
