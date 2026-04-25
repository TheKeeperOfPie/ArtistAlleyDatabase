package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference.ArtistInference
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference.ArtistInferenceField
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference.ArtistInferenceUtils
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference.SameArtistPrompter
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.form.FormMergeBehavior
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.ImageUploader
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.ImageUtils
import com.thekeeperofpie.artistalleydatabase.alley.edit.tags.TagAutocomplete
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.series.toImageInfo
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.ConsoleLogger
import com.thekeeperofpie.artistalleydatabase.utils.ExclusiveProgressJob
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.launch
import com.thekeeperofpie.artistalleydatabase.utils_compose.ExclusiveTask
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.replaceAll
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.uuid.Uuid

@AssistedInject
class ArtistEditViewModel(
    private val artistInference: ArtistInference,
    private val database: AlleyEditDatabase,
    private val dispatchers: CustomDispatchers,
    private val imageUploader: ImageUploader,
    seriesImagesStore: SeriesImagesStore,
    val tagAutocomplete: TagAutocomplete,
    @Assisted private val dataYear: DataYear,
    @Assisted private val artistId: Uuid,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val saveTask: ExclusiveTask<Triple<List<EditImage>, ArtistDatabaseEntry.Impl, Boolean>, BackendRequest.ArtistSave.Response> =
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

    private val artistFormState = savedStateHandle.saveable(
        key = "artistFormState",
        saver = ArtistFormState.Saver,
        init = { ArtistFormState(artistId) },
    )

    val sameArtistPrompter = SameArtistPrompter(
        scope = viewModelScope,
        artistInference = artistInference,
        artistFormState = artistFormState,
        dispatchers = dispatchers,
        savedStateHandle = savedStateHandle,
    )

    private val previousYearData =
        snapshotFlow { artistFormState.editorState.id.value.text.toString() }
            .mapLatest(Uuid::parseOrNull)
            .mapLatest { it?.let { artistInference.getPreviousYearData(it) } }
            .flowOn(dispatchers.io)
            .stateIn(viewModelScope, SharingStarted.Lazily, null)
    private val deleteJob = ExclusiveProgressJob(viewModelScope, ::delete)

    val state = ArtistEditScreen.State(
        artistProgress = artistJob.state,
        initialArtist = artist,
        previousYearData = previousYearData,
        artistFormState = artistFormState,
        formMetadata = formMetadata,
        formLink = formLink,
        saveTaskState = saveTask.state,
        sameArtistState = sameArtistPrompter.state,
        deleteProgress = deleteJob.state,
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
        withContext(dispatchers.main) {
            Snapshot.withMutableSnapshot {
                this@ArtistEditViewModel.artist.value = artist
                state.artistFormState.applyDatabaseEntry(
                    artist = artist,
                    seriesById = tagAutocomplete.seriesById.first(),
                    merchById = tagAutocomplete.merchById.first(),
                    mergeBehavior = FormMergeBehavior.REPLACE,
                )
                state.artistFormState.images
                    .replaceAll(artist.images.map(ImageUtils::toEditImage))
                formMetadata.value = ArtistEditScreen.State.FormMetadata(
                    hasPendingFormSubmission = response.hasPendingFormSubmission,
                    hasFormLink = response.hasFormLink,
                )
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

    fun onConfirmMerge(fieldState: Map<ArtistInferenceField, Boolean>) {
        if (fieldState.isEmpty()) return
        val previousYearData = previousYearData.value ?: return
        val seriesById =
            tagAutocomplete.seriesById.replayCache.firstOrNull()?.takeIf { it.isNotEmpty() }
                ?: return
        val merchById =
            tagAutocomplete.merchById.replayCache.firstOrNull()?.takeIf { it.isNotEmpty() }
                ?: return

        val formEntry = artistFormState.captureDatabaseEntry(
            dataYear = dataYear,
            verifiedArtist = false, // Shouldn't be used
        ).second

        val mergeEntry = ArtistInferenceUtils.mergeEntry(
            formEntry = formEntry,
            previousYearData = previousYearData,
            fieldState = fieldState,
        )
        artistFormState.applyDatabaseEntry(
            artist = mergeEntry,
            seriesById = seriesById,
            merchById = merchById,
            mergeBehavior = FormMergeBehavior.APPEND,
        )
    }

    fun onConfirmDelete() {
        val artist = artist.value ?: return
        deleteJob.launch { artist }
    }

    private suspend fun loadFormLink(forceRegenerate: Boolean) = withContext(dispatchers.io) {
        formLink.value = database.generateFormLink(dataYear, artistId, forceRegenerate)
    }

    private suspend fun delete(artist: ArtistDatabaseEntry.Impl) = withContext(dispatchers.io) {
        database.deleteArtist(dataYear, artist)
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

            val initialArtist = artist.value
            val hasChanged = ArtistDatabaseEntry.hasChanged(initialArtist, databaseEntry)
            if (!isManual && !hasChanged && images.none { it is EditImage.LocalImage }) {
                // Don't save if no data has changed
                return@withContext BackendRequest.ArtistSave.Response.Success
            }
            val imagesResult = imageUploader.uploadImages(
                dataYear = dataYear,
                artistId = Uuid.parse(triple.second.id),
                artistImages = images,
                stampRallyImages = emptyMap(),
            )

            val (artistCatalogImages, uploadedImages) = when (imagesResult) {
                ImageUploader.UploadResult.Empty -> emptyList<CatalogImage>() to emptyMap()
                is ImageUploader.UploadResult.Error ->
                    return@withContext BackendRequest.ArtistSave.Response.Failed(imagesResult.message)
                is ImageUploader.UploadResult.Success ->
                    imagesResult.artistCatalogImages to imagesResult.uploadedImages
            }

            val newArtistImages = state.artistFormState.images.toList()
                .map { uploadedImages[it] ?: it }
            state.artistFormState.images.replaceAll(newArtistImages)

            val updatedArtist = databaseEntry.copy(_images = artistCatalogImages)
            database.saveArtist(
                dataYear = dataYear,
                initial = initialArtist,
                updated = updatedArtist,
            ).also {
                when (it) {
                    is BackendRequest.ArtistSave.Response.Success -> {
                        hasLoaded = false
                        artist.value = updatedArtist
                        if (!isManual) {
                            state.artistFormState.applyDatabaseEntry(
                                artist = updatedArtist,
                                seriesById = tagAutocomplete.seriesById.first(),
                                merchById = tagAutocomplete.merchById.first(),
                            )
                        }
                    }
                    is BackendRequest.ArtistSave.Response.Failed -> Unit
                    is BackendRequest.ArtistSave.Response.Outdated ->
                        ConsoleLogger.log(
                            "initial = ${Json.encodeToString(initialArtist)}, " +
                                    "updated = ${Json.encodeToString(updatedArtist)}, " +
                                    "expected = ${Json.encodeToString(it.current)}"
                        )
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
