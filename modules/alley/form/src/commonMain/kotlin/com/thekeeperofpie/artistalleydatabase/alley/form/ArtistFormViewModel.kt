package com.thekeeperofpie.artistalleydatabase.alley.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistFormState
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistInference
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyFormDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.form.ArtistFormAccessKey
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.tags.TagAutocomplete
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendFormRequest
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.series.toImageInfo
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.ExclusiveProgressJob
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.mapLatestNotNull
import com.thekeeperofpie.artistalleydatabase.utils.launch
import com.thekeeperofpie.artistalleydatabase.utils_compose.ExclusiveTask
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.uuid.Uuid

@AssistedInject
class ArtistFormViewModel(
    artistInference: ArtistInference,
    dispatchers: CustomDispatchers,
    editDatabase: AlleyEditDatabase,
    private val formDatabase: AlleyFormDatabase,
    seriesImagesStore: SeriesImagesStore,
    private val tagAutocomplete: TagAutocomplete,
    @Assisted private val dataYear: DataYear,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val saveTask = ExclusiveTask(viewModelScope, ::save)
    private var progress = savedStateHandle.getMutableStateFlow(
        json = Json,
        key = "progress",
    ) { ArtistFormScreen.State.Progress.LOADING }
    private val artist =
        savedStateHandle.getMutableStateFlow<ArtistDatabaseEntry.Impl?>(Json, "artist", null)
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
        previousYearData = previousYearData,
        progress = progress,
        formState = savedStateHandle.saveable(
            key = "formState",
            saver = ArtistFormScreen.State.FormState.Saver,
        ) {
            ArtistFormScreen.State.FormState()
        },
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
            val artist = formDatabase.loadArtist(dataYear)
            if (artist == null) {
                progress.value = ArtistFormScreen.State.Progress.BAD_AUTH
                return@withContext
            }
            this@ArtistFormViewModel.artist.value = artist
            state.applyDatabaseEntry(
                artist = artist,
                seriesById = tagAutocomplete.seriesById.first(),
                merchById = tagAutocomplete.merchById.first(),
                mergeBehavior = ArtistFormState.MergeBehavior.REPLACE,
            )

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
            CapturedState(
                images = images,
                artist = artist,
                formNotes = state.formState.formNotes.value.text.toString(),
            )
        }
    }

    fun onConfirmMerge(fieldState: Map<ArtistFormScreen.ArtistField, Boolean>) {
        val artist = artist.value ?: return
        val previousYearData = previousYearData.value ?: return
        val seriesById =
            tagAutocomplete.seriesById.replayCache.firstOrNull()?.takeIf { it.isNotEmpty() }
                ?: return
        val merchById =
            tagAutocomplete.merchById.replayCache.firstOrNull()?.takeIf { it.isNotEmpty() }
                ?: return

        val formEntry = state.captureDatabaseEntry(artist).second
        val entryToMerge = formEntry
            .copy(
                summary = previousYearData.summary.takeIf {
                    fieldState[ArtistFormScreen.ArtistField.SUMMARY] ?: false
                },
                links = previousYearData.links.takeIf {
                    fieldState[ArtistFormScreen.ArtistField.LINKS] ?: false
                }.orEmpty(),
                storeLinks = previousYearData.storeLinks.takeIf {
                    fieldState[ArtistFormScreen.ArtistField.STORE_LINKS] ?: false
                }.orEmpty(),
                seriesInferred = previousYearData.seriesInferred
                    .takeIf { fieldState[ArtistFormScreen.ArtistField.SERIES] ?: false }
                    .orEmpty(),
                merchInferred = previousYearData.merchInferred
                    .takeIf { fieldState[ArtistFormScreen.ArtistField.MERCH] ?: false }
                    .orEmpty(),
            )
        state.applyDatabaseEntry(
            artist = entryToMerge,
            seriesById = seriesById,
            merchById = merchById,
            mergeBehavior = ArtistFormState.MergeBehavior.APPEND,
        )
    }

    fun onSubmitPrivateKey(privateKey: String) {
        progress.value = ArtistFormScreen.State.Progress.LOADING
        ArtistFormAccessKey.setKey(privateKey)
        artistJob.launch()
    }

    private suspend fun save(data: CapturedState): BackendFormRequest.ArtistSave.Response =
        // TODO: Image support
        formDatabase.saveArtist(
            dataYear = dataYear,
            before = artist.value!!,
            after = data.artist,
            formNotes = data.formNotes,
        )

    data class CapturedState(
        val images: List<EditImage>,
        val artist: ArtistDatabaseEntry.Impl,
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
