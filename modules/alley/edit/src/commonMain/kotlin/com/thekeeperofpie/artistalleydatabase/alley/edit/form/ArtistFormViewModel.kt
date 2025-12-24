package com.thekeeperofpie.artistalleydatabase.alley.edit.form

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistFormState
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyFormDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.tags.TagAutocomplete
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendFormRequest
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.series.toImageInfo
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.entry.EntryLockState
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.ExclusiveProgressJob
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.launch
import com.thekeeperofpie.artistalleydatabase.utils_compose.ExclusiveTask
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.uuid.Uuid

@AssistedInject
class ArtistFormViewModel(
    dispatchers: CustomDispatchers,
    editDatabase: AlleyEditDatabase,
    private val formDatabase: AlleyFormDatabase,
    seriesImagesStore: SeriesImagesStore,
    @Assisted private val dataYear: DataYear,
    @Assisted private val artistId: Uuid,
    // TODO: Actually read this from the URL
    @Assisted private var privateKey: String,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val saveTask: ExclusiveTask<Pair<List<EditImage>, ArtistDatabaseEntry.Impl>, BackendFormRequest.ArtistSave.Response> =
        ExclusiveTask(viewModelScope, ::save)
    private var progress = savedStateHandle.getMutableStateFlow(
        json = Json,
        key = "progress",
    ) { ArtistFormScreen.State.Progress.LOADING }
    val state = ArtistFormScreen.State(
        progress = progress,
        artistFormState = savedStateHandle.saveable(
            key = "artistFormState",
            saver = ArtistFormState.Saver,
        ) {
            ArtistFormState().apply {
                textState.id.value.setTextAndPlaceCursorAtEnd(artistId.toString())
                textState.id.lockState = EntryLockState.LOCKED
            }
        },
        saveTaskState = saveTask.state,
    )

    private val imageLoader = SeriesImageLoader(dispatchers, viewModelScope, seriesImagesStore)
    private val tagAutocomplete = TagAutocomplete(viewModelScope, editDatabase, dispatchers)

    private var artist: ArtistDatabaseEntry.Impl? = null
    private val artistJob = ExclusiveProgressJob(viewModelScope, ::loadArtistInfo)

    fun initialize() {
        if (progress.value != ArtistFormScreen.State.Progress.LOADED) {
            artistJob.launch()
        }
    }

    private suspend fun loadArtistInfo() = try {
        withContext(PlatformDispatchers.IO) {
            val artist = formDatabase.loadArtist(dataYear, artistId, privateKey)
            if (artist == null) {
                progress.value = ArtistFormScreen.State.Progress.BAD_AUTH
                return@withContext
            }
            this@ArtistFormViewModel.artist = artist
            state.artistFormState.applyDatabaseEntry(
                artist = artist,
                seriesById = tagAutocomplete.seriesById.first(),
                merchById = tagAutocomplete.merchById.first(),
                force = true,
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

    fun onClickDone() =
        saveTask.triggerManual { state.artistFormState.captureDatabaseEntry(dataYear) }

    fun onSubmitPrivateKey(privateKey: String) {
        this.privateKey = privateKey.substringAfter("?${AlleyCryptography.ACCESS_KEY_PARAM}=")
        artistJob.launch()
    }

    private suspend fun save(
        pair: Pair<List<EditImage>, ArtistDatabaseEntry.Impl>,
    ): BackendFormRequest.ArtistSave.Response =
        // TODO: Image support
        formDatabase.saveArtist(dataYear, artistId, privateKey, artist!!, pair.second)

    @AssistedFactory
    interface Factory {
        fun create(
            dataYear: DataYear,
            artistId: Uuid,
            privateKey: String,
            savedStateHandle: SavedStateHandle,
        ): ArtistFormViewModel
    }
}
