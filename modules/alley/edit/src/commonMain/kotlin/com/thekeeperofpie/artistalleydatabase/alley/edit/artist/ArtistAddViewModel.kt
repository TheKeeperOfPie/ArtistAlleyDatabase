package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_add_error_loading_merge
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.PlatformImageCache
import com.thekeeperofpie.artistalleydatabase.alley.edit.tags.TagAutocomplete
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.ArtistSave
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.series.toImageInfo
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.ExclusiveTask
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.replaceAll
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

@AssistedInject
class ArtistAddViewModel(
    private val artistInference: ArtistInference,
    private val database: AlleyEditDatabase,
    private val dispatchers: CustomDispatchers,
    seriesImagesStore: SeriesImagesStore,
    @Assisted private val dataYear: DataYear,
    @Assisted private val artistId: Uuid,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val saveTask: ExclusiveTask<Triple<List<EditImage>, ArtistDatabaseEntry.Impl, Boolean>, ArtistSave.Response> =
        ExclusiveTask(viewModelScope, ::save)
    private val artist =
        savedStateHandle.getMutableStateFlow<ArtistDatabaseEntry.Impl?>("artist", null)
    private val mergingArtistId =
        savedStateHandle.getMutableStateFlow<Uuid?>("mergingArtistId", null)
    private val mergingArtist = mergingArtistId
        .flatMapLatest {
            if (it == null) {
                flowOf(LoadingResult.empty())
            } else {
                flow {
                    emit(LoadingResult.loading())
                    val artist = artistInference.getArtistForMerge(it)
                    emit(
                        if (artist == null) {
                            LoadingResult.error(Res.string.alley_edit_artist_add_error_loading_merge)
                        } else {
                            LoadingResult.success(artist)
                        }
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, LoadingResult.empty())
    private val artistFormState = savedStateHandle.saveable(
        key = "artistFormState",
        saver = ArtistFormState.Saver,
        init = { ArtistFormState(artistId) },
    )

    private val inferredArtists =
        snapshotFlow { ArtistInference.Input.captureState(artistFormState) }
            .mapLatest { artistInference.inferArtist(it) }
            .flowOn(dispatchers.io)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val state = ArtistAddScreen.State(
        initialArtist = artist,
        artistFormState = artistFormState,
        inferredArtists = inferredArtists,
        mergingArtist = mergingArtist,
        saveTaskState = saveTask.state,
    )

    private val imageLoader = SeriesImageLoader(dispatchers, viewModelScope, seriesImagesStore)
    val tagAutocomplete = TagAutocomplete(viewModelScope, database, dispatchers)

    fun seriesPredictions(query: String) = tagAutocomplete.seriesPredictions(query)
    fun merchPredictions(query: String) = tagAutocomplete.merchPredictions(query)

    fun seriesImage(info: SeriesInfo) = imageLoader.getSeriesImage(info.toImageInfo())

    fun onClickSave() = saveTask.triggerAuto { captureDatabaseEntry(false) }
    fun onClickDone() = saveTask.triggerManual { captureDatabaseEntry(true) }

    fun onClickMerge(artistId: Uuid) {
        mergingArtistId.value = artistId
    }

    internal fun onConfirmMerge(fieldState: Map<ArtistAddScreen.ArtistField, Boolean>) {
        val artist = mergingArtist.value.result ?: return
        val seriesById =
            tagAutocomplete.seriesById.replayCache.firstOrNull()?.takeIf { it.isNotEmpty() }
                ?: return
        val merchById =
            tagAutocomplete.merchById.replayCache.firstOrNull()?.takeIf { it.isNotEmpty() }
                ?: return

        val formEntry = artistFormState.captureDatabaseEntry(dataYear).second
        val entryToMerge = formEntry
            .copy(
                name = artist.name.takeIf {
                    fieldState[ArtistAddScreen.ArtistField.NAME] ?: false
                } ?: formEntry.name,
                links = artist.links.takeIf {
                    fieldState[ArtistAddScreen.ArtistField.LINKS] ?: false
                }.orEmpty(),
                storeLinks = artist.storeLinks.takeIf {
                    fieldState[ArtistAddScreen.ArtistField.STORE_LINKS] ?: false
                }.orEmpty(),
                catalogLinks = artist.catalogLinks.takeIf {
                    fieldState[ArtistAddScreen.ArtistField.CATALOG_LINKS] ?: false
                }.orEmpty(),
                seriesInferred = artist.seriesInferred.takeIf {
                    fieldState[ArtistAddScreen.ArtistField.SERIES_INFERRED] ?: false
                }.orEmpty(),
                seriesConfirmed = artist.seriesConfirmed.takeIf {
                    fieldState[ArtistAddScreen.ArtistField.SERIES_CONFIRMED] ?: false
                }.orEmpty(),
                merchInferred = artist.merchInferred.takeIf {
                    fieldState[ArtistAddScreen.ArtistField.MERCH_INFERRED] ?: false
                }.orEmpty(),
                merchConfirmed = artist.merchConfirmed.takeIf {
                    fieldState[ArtistAddScreen.ArtistField.MERCH_CONFIRMED] ?: false
                }.orEmpty(),
            )
        artistFormState.applyDatabaseEntry(
            artist = entryToMerge,
            seriesById = seriesById,
            merchById = merchById,
        )
        mergingArtistId.value = null
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
            savedStateHandle: SavedStateHandle,
        ): ArtistAddViewModel
    }
}
