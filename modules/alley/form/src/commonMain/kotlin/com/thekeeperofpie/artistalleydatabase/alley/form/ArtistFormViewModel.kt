package com.thekeeperofpie.artistalleydatabase.alley.form

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.form.ArtistFormAccessKey
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference.ArtistInference
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference.ArtistInferenceField
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference.ArtistInferenceUtils
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyFormDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.form.FormMergeBehavior
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.ImageUploader
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.ImageUtils
import com.thekeeperofpie.artistalleydatabase.alley.edit.rallies.StampRallyFormState
import com.thekeeperofpie.artistalleydatabase.alley.edit.tags.FormTagAutocomplete
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistEntryDiff
import com.thekeeperofpie.artistalleydatabase.alley.models.ListDiff
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendFormRequest
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.series.toImageInfo
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.entry.EntryLockState
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.ExclusiveProgressJob
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.mapLatestNotNull
import com.thekeeperofpie.artistalleydatabase.utils.launch
import com.thekeeperofpie.artistalleydatabase.utils_compose.ExclusiveTask
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.StateUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.replaceAll
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

@AssistedInject
class ArtistFormViewModel(
    artistInference: ArtistInference,
    dispatchers: CustomDispatchers,
    private val formDatabase: AlleyFormDatabase,
    seriesImagesStore: SeriesImagesStore,
    val tagAutocomplete: FormTagAutocomplete,
    private val imageUploader: ImageUploader,
    @Assisted private val dataYear: DataYear,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val saveTask = ExclusiveTask(viewModelScope, ::save)
    private var progress = savedStateHandle.getMutableStateFlow(
        json = Json,
        key = "progress",
    ) { ArtistFormScreen.State.Progress.LOADING }
    private val artist =
        savedStateHandle.getMutableStateFlow<ArtistDatabaseEntry.Impl?>("artist", null)
    private val rallies =
        savedStateHandle.getMutableStateFlow<List<StampRallyDatabaseEntry>>("rallies", emptyList())
    private val initialFormDiff =
        savedStateHandle.getMutableStateFlow<ArtistEntryDiff?>("initialFormDiff", null)
    private val previousYearData =
        artist.mapLatestNotNull { it?.id?.let(Uuid::parseOrNull) }
            .mapLatest {
                // These are used when merging, wait for them to be available before offering merge
                tagAutocomplete.seriesById.first()
                tagAutocomplete.merchById.first()
                artistInference.getPreviousYearData(it)
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val state = ArtistFormScreen.State(
        initialArtist = artist,
        initialRallies = rallies,
        previousYearData = previousYearData,
        progress = progress,
        stampRallyStates = savedStateHandle.saveable(
            key = "stampRallyStates",
            saver = StateUtils.SnapshotListSaver(StampRallyFormState.Saver),
        ) {
            SnapshotStateList()
        },
        artistFormState = savedStateHandle.saveable(
            key = "formState",
            saver = ArtistFormScreen.State.FormState.Saver,
        ) {
            ArtistFormScreen.State.FormState()
        },
        initialFormDiff = initialFormDiff,
        saveTaskState = saveTask.state,
    )

    private val imageLoader = SeriesImageLoader(dispatchers, viewModelScope, seriesImagesStore)

    private val artistJob = ExclusiveProgressJob(viewModelScope, ::loadArtistInfo)

    fun initialize() {
        if (progress.value != ArtistFormScreen.State.Progress.LOADED) {
            artistJob.launch()
        }
    }

    private suspend fun loadArtistInfo() = try {
        withContext(PlatformDispatchers.IO) {
            val response = formDatabase.loadArtist(dataYear)
            if (response == null) {
                progress.value = ArtistFormScreen.State.Progress.BAD_AUTH
                return@withContext
            }
            val baseArtist = response.artist
            this@ArtistFormViewModel.artist.value = baseArtist
            this@ArtistFormViewModel.rallies.value = response.stampRallies

            fun <T> applyDiff(
                base: List<T>,
                diff: ListDiff<T>?,
            ): List<T> {
                val base = base.toMutableSet()
                base.removeAll(diff?.deleted.orEmpty().toSet())
                base.addAll(diff?.added.orEmpty().toSet())
                return base.toMutableList()
            }

            val artistFormDiff = response.artistFormDiff
            val artist = if (artistFormDiff == null) {
                baseArtist
            } else {
                baseArtist.copy(
                    _images = applyDiff(baseArtist.images, artistFormDiff.images),
                    booth = artistFormDiff.booth ?: baseArtist.booth,
                    name = artistFormDiff.name ?: baseArtist.name,
                    summary = artistFormDiff.summary ?: baseArtist.summary,
                    socialLinks = applyDiff(baseArtist.socialLinks, artistFormDiff.socialLinks),
                    storeLinks = applyDiff(baseArtist.storeLinks, artistFormDiff.storeLinks),
                    portfolioLinks = applyDiff(
                        baseArtist.portfolioLinks,
                        artistFormDiff.portfolioLinks
                    ),
                    catalogLinks = applyDiff(baseArtist.catalogLinks, artistFormDiff.catalogLinks),
                    notes = artistFormDiff.notes ?: baseArtist.notes,
                    commissions = applyDiff(baseArtist.commissions, artistFormDiff.commissions),
                    seriesInferred = applyDiff(
                        baseArtist.seriesInferred,
                        artistFormDiff.seriesInferred
                    ),
                    seriesConfirmed =
                        applyDiff(baseArtist.seriesConfirmed, artistFormDiff.seriesConfirmed),
                    merchInferred = applyDiff(
                        baseArtist.merchInferred,
                        artistFormDiff.merchInferred
                    ),
                    merchConfirmed = applyDiff(
                        baseArtist.merchConfirmed,
                        artistFormDiff.merchConfirmed
                    ),
                )
            }

            val (seriesById, merchById) = (withTimeoutOrNull(5.seconds) {
                tagAutocomplete.seriesById.first { it.isNotEmpty() } to
                        tagAutocomplete.merchById.first { it.isNotEmpty() }
            } ?: (emptyMap<String, SeriesInfo>() to emptyMap()))
            state.applyDatabaseEntry(
                artist = artist,
                seriesById = seriesById,
                merchById = merchById,
                mergeBehavior = FormMergeBehavior.REPLACE,
            )
            state.artistFormState.images.replaceAll(artist.images.map(ImageUtils::toEditImage))

            val existingStampRallyStates = state.stampRallyStates.toList()
            val emptyStampRallyDatabaseEntry by lazy {
                StampRallyFormState("").captureDatabaseEntry(dataYear).second
            }
            val newStampRallyStates =
                (response.stampRallies.map { it.id } + response.stampRallyFormDiffs.map { it.id })
                    .distinct()
                    .map { stampRallyId ->
                        response.stampRallies.find { it.id == stampRallyId }
                            ?: emptyStampRallyDatabaseEntry.copy(id = stampRallyId)
                    }
                    .map { baseStampRally ->
                        val existingState =
                            existingStampRallyStates.find { it.editorState.id.value.text.toString() == baseStampRally.id }
                        val baseState = existingState ?: StampRallyFormState(baseStampRally.id)
                        val stampRallyFormDiff =
                            response.stampRallyFormDiffs.find { it.id == baseStampRally.id }
                        val stampRally = if (stampRallyFormDiff == null) {
                            baseStampRally
                        } else {
                            // TODO: hostTable isn't handled, remove in favor of index 0?
                            val tables = applyDiff(baseStampRally.tables, stampRallyFormDiff.tables)
                            baseStampRally.copy(
                                images = applyDiff(
                                    baseStampRally.images,
                                    stampRallyFormDiff.images,
                                ),
                                fandom = stampRallyFormDiff.fandom ?: baseStampRally.fandom,
                                hostTable = tables.firstOrNull().orEmpty(),
                                tables = tables,
                                links = applyDiff(baseStampRally.links, stampRallyFormDiff.links),
                                tableMin = stampRallyFormDiff.tableMin ?: baseStampRally.tableMin,
                                prize = stampRallyFormDiff.prize ?: baseStampRally.prize,
                                prizeLimit = stampRallyFormDiff.prizeLimit
                                    ?: baseStampRally.prizeLimit,
                                series = applyDiff(
                                    baseStampRally.series,
                                    stampRallyFormDiff.series
                                ),
                                merch = applyDiff(baseStampRally.merch, stampRallyFormDiff.merch),
                            )
                        }
                        baseState.applyDatabaseEntry(
                            stampRally = stampRally,
                            seriesById = tagAutocomplete.seriesById.first(),
                            merchById = tagAutocomplete.merchById.first(),
                            mergeBehavior = FormMergeBehavior.REPLACE,
                        )
                        baseState.images.replaceAll(stampRally.images.map(ImageUtils::toEditImage))
                        baseState.editorState.deleted = stampRallyFormDiff?.deleted == true
                        baseState
                    }
            state.stampRallyStates.replaceAll(newStampRallyStates)

            artistFormDiff?.formNotes?.let {
                state.artistFormState.formNotes.value.setTextAndPlaceCursorAtEnd(it)
                state.artistFormState.formNotes.lockState = EntryLockState.LOCKED
            }
            initialFormDiff.value = artistFormDiff

            // TODO: Support images?
            progress.value = ArtistFormScreen.State.Progress.LOADED
        }
    } catch (_: Throwable) {
        // Differentiate between 403 and generic error
        progress.value = ArtistFormScreen.State.Progress.BAD_AUTH
    }

    fun seriesPredictions(query: String) = tagAutocomplete.seriesPredictions(query)
    fun merchPredictions(query: String) = tagAutocomplete.merchPredictions(query)

    fun seriesImage(info: SeriesInfo) = imageLoader.getSeriesImage(info.toImageInfo())

    fun onClickDone() {
        val artist = artist.value ?: return
        saveTask.triggerManual {
            val (images, artist) = state.captureDatabaseEntry(artist)
            val stampRallyData = state.stampRallyStates.toList()
                .map { it.captureDatabaseEntry(dataYear) }
            CapturedState(
                artistImages = images,
                artist = artist,
                stampRallyImages = stampRallyData.associate { it.second.id to it.first },
                stampRallyEntries = stampRallyData.map { it.second },
                deletedRallyIds = state.stampRallyStates.filter { it.editorState.deleted }
                    .map { it.editorState.id.value.text.toString() },
                formNotes = state.artistFormState.formNotes.value.text.toString(),
            )
        }
    }

    fun onConfirmMerge(fieldState: Map<ArtistInferenceField, Boolean>) {
        if (fieldState.isEmpty()) return
        val artist = artist.value ?: return
        val previousYearData = previousYearData.value ?: return
        val seriesById =
            tagAutocomplete.seriesById.replayCache.firstOrNull()?.takeIf { it.isNotEmpty() }
                ?: return
        val merchById =
            tagAutocomplete.merchById.replayCache.firstOrNull()?.takeIf { it.isNotEmpty() }
                ?: return

        val formEntry = state.captureDatabaseEntry(artist).second
        val mergeEntry = ArtistInferenceUtils.mergeEntry(
            formEntry = formEntry,
            previousYearData = previousYearData,
            fieldState = fieldState,
        )
        state.applyDatabaseEntry(
            artist = mergeEntry,
            seriesById = seriesById,
            merchById = merchById,
            mergeBehavior = FormMergeBehavior.APPEND,
        )
    }

    fun onSubmitPrivateKey(privateKey: String) {
        progress.value = ArtistFormScreen.State.Progress.LOADING
        ArtistFormAccessKey.setKey(privateKey)
        artistJob.launch()
    }

    fun onClickEditAgain() {
        progress.value = ArtistFormScreen.State.Progress.LOADING
        artistJob.launch()
    }

    private suspend fun save(data: CapturedState): ArtistFormScreen.State.SaveResult {
        val beforeArtist = artist.value!!
        val beforeStampRallies = rallies.value

        // For new rallies whose ID is not final yet, strip their images and restore after saving
        val stampRallyImages = data.stampRallyImages.toMutableMap()
        val afterRallies = data.stampRallyEntries.toMutableList()
        val deferredStampRallyImages = Array<List<EditImage>?>(afterRallies.size) { emptyList() }
        for (index in 0 until afterRallies.size) {
            val entry = afterRallies[index]
            val isNewRally = beforeStampRallies.none { it.id == entry.id }
            if (isNewRally) {
                deferredStampRallyImages[index] = stampRallyImages.remove(entry.id)
                afterRallies[index] = afterRallies[index].copy(images = emptyList())
            }
        }

        // TODO: Show incremental progress to the user
        val imagesResult = imageUploader.uploadImages(
            dataYear = dataYear,
            artistId = Uuid.parse(beforeArtist.id),
            artistImages = data.artistImages,
            stampRallyImages = stampRallyImages,
        )

        val (artistCatalogImages, stampRallyCatalogImages, uploadedImages) = when (imagesResult) {
            ImageUploader.UploadResult.Empty -> Triple(emptyList(), emptyMap(), emptyMap())
            is ImageUploader.UploadResult.Error -> {
                progress.value = ArtistFormScreen.State.Progress.LOADED
                return ArtistFormScreen.State.SaveResult.ImageUploadFailed(imagesResult.message)
            }
            is ImageUploader.UploadResult.Success -> {
                Triple(
                    imagesResult.artistCatalogImages,
                    imagesResult.stampRallyCatalogImages,
                    imagesResult.uploadedImages,
                )
            }
        }

        val newArtistImages = state.artistFormState.images.toList()
            .map { uploadedImages[it] ?: it }
        state.artistFormState.images.replaceAll(newArtistImages)

        val stampRallyStates = state.stampRallyStates.toList()
        stampRallyStates.forEach {
            val newStampRallyImages = it.images.toList()
                .map { uploadedImages[it] ?: it }
            it.images.replaceAll(newStampRallyImages)
        }

        val afterArtist = data.artist.copy(_images = artistCatalogImages)
        val afterStampRallies = afterRallies.map {
            it.copy(images = stampRallyCatalogImages[it.id].orEmpty())
        }
        var artistResult = formDatabase.saveArtist(
            dataYear = dataYear,
            beforeArtist = beforeArtist,
            afterArtist = afterArtist,
            beforeStampRallies = beforeStampRallies,
            afterStampRallies = afterStampRallies,
            deletedRallyIds = data.deletedRallyIds,
            formNotes = data.formNotes,
        )

        if (artistResult is BackendFormRequest.ArtistSave.Response.Success &&
            deferredStampRallyImages.any { !it.isNullOrEmpty() }
        ) {
            val (newArtistResult, reattachErrorMessage) = reattachDeferredStampRallyImages(
                beforeArtist = beforeArtist,
                afterArtist = afterArtist,
                beforeStampRallies = beforeStampRallies,
                initialArtistResult = artistResult,
                deferredStampRallyImages = deferredStampRallyImages,
                formNotes = data.formNotes,
            )
            if (newArtistResult == null) {
                progress.value = ArtistFormScreen.State.Progress.LOADED
                return ArtistFormScreen.State.SaveResult.ImageUploadFailed(reattachErrorMessage)
            }

            artistResult = newArtistResult
        }

        return if (artistResult is BackendFormRequest.ArtistSave.Response.Failed) {
            progress.value = ArtistFormScreen.State.Progress.LOADED
            ArtistFormScreen.State.SaveResult.ArtistSaveFailed(artistResult.errorMessage)
        } else {
            progress.value = ArtistFormScreen.State.Progress.DONE
            ArtistFormScreen.State.SaveResult.Success
        }
    }

    private suspend fun reattachDeferredStampRallyImages(
        beforeArtist: ArtistDatabaseEntry.Impl,
        afterArtist: ArtistDatabaseEntry.Impl,
        beforeStampRallies: List<StampRallyDatabaseEntry>,
        initialArtistResult: BackendFormRequest.ArtistSave.Response.Success,
        deferredStampRallyImages: Array<List<EditImage>?>,
        formNotes: String,
    ): Pair<BackendFormRequest.ArtistSave.Response?, String> {
        val reattachedImages = mutableMapOf<String, List<EditImage>>()
        deferredStampRallyImages.forEachIndexed { index, images ->
            if (!images.isNullOrEmpty()) {
                val key =
                    initialArtistResult.stampRallies.getOrNull(index)?.id ?: return@forEachIndexed
                reattachedImages[key] = images
            }
        }

        val reattachedImagesResult = imageUploader.uploadImages(
            dataYear = dataYear,
            artistId = Uuid.parse(beforeArtist.id),
            artistImages = emptyList(),
            stampRallyImages = reattachedImages,
        )

        val (reattachedStampRallyCatalogImages, reattachedUploadedImages) = when (reattachedImagesResult) {
            ImageUploader.UploadResult.Empty -> emptyMap<String, List<CatalogImage>>() to emptyMap()
            is ImageUploader.UploadResult.Error -> return null to reattachedImagesResult.message
            is ImageUploader.UploadResult.Success ->
                reattachedImagesResult.stampRallyCatalogImages to reattachedImagesResult.uploadedImages
        }

        val stampRallyStates = state.stampRallyStates.toList()
        stampRallyStates.forEach {
            val newStampRallyImages = it.images.toList()
                .map { reattachedUploadedImages[it] ?: it }
            it.images.replaceAll(newStampRallyImages)
        }
        return formDatabase.saveArtist(
            dataYear = dataYear,
            beforeArtist = beforeArtist,
            afterArtist = afterArtist,
            beforeStampRallies = beforeStampRallies.mapIndexed { index, rally ->
                // Copy the finalized IDs, otherwise they won't line up on the backend
                rally.copy(id = initialArtistResult.stampRallies.getOrNull(index)?.id ?: rally.id)
            },
            afterStampRallies = initialArtistResult.stampRallies.map { entry ->
                entry.copy(images = entry.images.ifEmpty { reattachedStampRallyCatalogImages[entry.id].orEmpty() })
            },
            deletedRallyIds = emptyList(),
            formNotes = formNotes,
        ) to ""
    }

    data class CapturedState(
        val artistImages: List<EditImage>,
        val artist: ArtistDatabaseEntry.Impl,
        val stampRallyImages: Map<String, List<EditImage>>,
        val stampRallyEntries: List<StampRallyDatabaseEntry>,
        val deletedRallyIds: List<String>,
        val formNotes: String,
    )

    @AssistedFactory
    interface Factory {
        fun create(
            dataYear: DataYear,
            savedStateHandle: SavedStateHandle,
        ): ArtistFormViewModel
    }
}
