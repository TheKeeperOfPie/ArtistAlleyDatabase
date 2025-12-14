package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.SearchUtils
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.PlatformImageCache
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.ArtistSave
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.series.toImageInfo
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.entry.EntryLockState
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistStatus
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.ExclusiveProgressJob
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.StateUtils
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
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Clock
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
    private val textState by savedStateHandle.saveable(saver = ArtistFormState.TextState.Saver) {
        ArtistFormState.TextState().apply {
            id.value.setTextAndPlaceCursorAtEnd(artistId.toString())
            id.lockState = EntryLockState.LOCKED
        }
    }
    private val saveJob = ExclusiveProgressJob(viewModelScope, ::save)
    private val artistMetadata by savedStateHandle.saveable(saver = ArtistFormState.Metadata.Saver) {
        ArtistFormState.Metadata()
    }
    val state = ArtistEditScreen.State(
        ArtistFormState(
            metadata = artistMetadata,
            images = savedStateHandle.saveable(
                "images",
                saver = StateUtils.snapshotListJsonSaver()
            ) { SnapshotStateList() },
            links = savedStateHandle.saveable(
                "links",
                saver = StateUtils.snapshotListJsonSaver()
            ) { SnapshotStateList() },
            storeLinks = savedStateHandle.saveable(
                "storeLinks",
                saver = StateUtils.snapshotListJsonSaver()
            ) { SnapshotStateList() },
            catalogLinks = savedStateHandle.saveable(
                "catalogLinks",
                saver = StateUtils.snapshotListJsonSaver()
            ) { SnapshotStateList() },
            commissions = savedStateHandle.saveable(
                "commissions",
                saver = StateUtils.snapshotListJsonSaver()
            ) { SnapshotStateList() },
            seriesInferred = savedStateHandle.saveable(
                "seriesInferred",
                saver = StateUtils.snapshotListJsonSaver()
            ) { SnapshotStateList() },
            seriesConfirmed = savedStateHandle.saveable(
                "seriesConfirmed",
                saver = StateUtils.snapshotListJsonSaver()
            ) { SnapshotStateList() },
            merchInferred = savedStateHandle.saveable(
                "merchInferred",
                saver = StateUtils.snapshotListJsonSaver()
            ) { SnapshotStateList() },
            merchConfirmed = savedStateHandle.saveable(
                "merchConfirmed",
                saver = StateUtils.snapshotListJsonSaver()
            ) { SnapshotStateList() },
            textState = textState,
        ),
        savingState = saveJob.state,
    )

    private var hasLoaded by savedStateHandle.saved { mode == ArtistEditScreen.Mode.ADD }
    private val seriesById = flowFromSuspend { database.loadSeries() }
        .shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    private val merchById = flowFromSuspend { database.loadMerch() }
        .shareIn(viewModelScope, SharingStarted.Eagerly, 1)
    private val imageLoader = SeriesImageLoader(dispatchers, viewModelScope, seriesImagesStore)

    private var artist: ArtistDatabaseEntry.Impl? = null

    fun initialize() {
        if (!hasLoaded) {
            viewModelScope.launch {
                loadArtistInfo(artistId)
                hasLoaded = true
            }
        }
    }

    private suspend fun loadArtistInfo(artistId: Uuid) = withContext(PlatformDispatchers.IO) {
        val artist = database.loadArtist(dataYear, artistId) ?: return@withContext
        this@ArtistEditViewModel.artist = artist
        state.artistFormState.applyDatabaseEntry(artist, seriesById.first(), merchById.first())

        val images = database.loadArtistImages(dataYear, artist)
        if (images.isNotEmpty()) {
            state.artistFormState.images.replaceAll(images)
        }
    }

    fun seriesPredictions(query: String) = if (query.length < 3) {
        flowOf(emptyList())
    } else {
        seriesById.flatMapLatest {
            flow {
                SearchUtils.incrementallyPartition(
                    values = it.values,
                    { it.titlePreferred.contains(query, ignoreCase = true) },
                    { it.titleRomaji.contains(query, ignoreCase = true) },
                    { it.titleEnglish.contains(query, ignoreCase = true) },
                    { it.synonyms.any { it.contains(query, ignoreCase = true) } },
                )
            }
        }.flowOn(dispatchers.io)
    }

    fun merchPredictions(query: String) =
        merchById
            .mapLatest {
                it.values
                    .filter { it.name.contains(query, ignoreCase = true) }
                    .sortedBy { it.name }
            }
            .flowOn(PlatformDispatchers.IO)

    fun seriesImage(info: SeriesInfo) = imageLoader.getSeriesImage(info.toImageInfo())

    fun onClickSave() = saveJob.launch(::captureDatabaseEntry)

    private fun captureDatabaseEntry(): Pair<List<EditImage>, ArtistDatabaseEntry.Impl> {
        val textState = state.artistFormState.textState
        val id = Uuid.parse(textState.id.value.text.toString())
        val status = ArtistStatus.entries[textState.status.selectedIndex]

        val booth = textState.booth.value.text.toString()
        val name = textState.name.value.text.toString()
        val summary = textState.summary.value.text.toString()

        val artistFormState = state.artistFormState
        // TODO: Include pending value?
        val links = artistFormState.links.toList().map { it.link }
            .plus(textState.links.value.text.toString().takeIf { it.isNotBlank() })
            .filterNotNull()
            .distinct()
        val storeLinks = artistFormState.storeLinks.toList().map { it.link }
            .plus(textState.storeLinks.value.text.toString().takeIf { it.isNotBlank() })
            .filterNotNull()
            .distinct()
        val catalogLinks = artistFormState.catalogLinks.toList()
            .plus(textState.catalogLinks.value.text.toString().takeIf { it.isNotBlank() })
            .filterNotNull()
            .distinct()

        val notes = textState.notes.value.text.toString()
        val editorNotes = textState.editorNotes.value.text.toString()
        val commissions = artistFormState.commissions.toList().map { it.serializedValue }
            .plus(textState.commissions.value.text.toString().takeIf { it.isNotBlank() })
            .filterNotNull()
            .distinct()
        val seriesInferred = artistFormState.seriesInferred.toList().map { it.id }
        val seriesConfirmed = artistFormState.seriesConfirmed.toList().map { it.id }
        val merchInferred = artistFormState.merchInferred.toList().map { it.name }
        val merchConfirmed = artistFormState.merchConfirmed.toList().map { it.name }

        val images = artistFormState.images.toList()
        return images to ArtistDatabaseEntry.Impl(
            year = dataYear,
            id = id.toString(),
            status = status,
            booth = booth,
            name = name,
            summary = summary,
            links = links,
            storeLinks = storeLinks,
            catalogLinks = catalogLinks,
            driveLink = null,
            notes = notes,
            commissions = commissions,
            seriesInferred = seriesInferred,
            seriesConfirmed = seriesConfirmed,
            merchInferred = merchInferred,
            merchConfirmed = merchConfirmed,
            images = emptyList(),
            counter = 1,
            editorNotes = editorNotes,
            lastEditor = null, // This is filled on the backend
            lastEditTime = Clock.System.now(),
        )
    }

    private suspend fun save(pair: Pair<List<EditImage>, ArtistDatabaseEntry.Impl>) =
        withContext(dispatchers.io) {
            val (images, databaseEntry) = pair
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

            database.saveArtist(
                dataYear = dataYear,
                initial = artist,
                updated = databaseEntry.copy(images = finalImages.map {
                    CatalogImage(name = it.name, width = it.width, height = it.height)
                }),
            ).also {
                if (it is ArtistSave.Response.Result.Success) {
                    hasLoaded = false
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
