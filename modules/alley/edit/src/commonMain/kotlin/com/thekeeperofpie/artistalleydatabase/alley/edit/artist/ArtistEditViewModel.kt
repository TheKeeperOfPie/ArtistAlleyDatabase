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
import com.thekeeperofpie.artistalleydatabase.alley.links.CommissionModel
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
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
    private val formState by savedStateHandle.saveable(saver = ArtistEditScreen.State.FormState.Saver) {
        ArtistEditScreen.State.FormState().apply {
            id.value.setTextAndPlaceCursorAtEnd(artistId.toString())
            id.lockState = EntryLockState.LOCKED
        }
    }
    private val saveJob = ExclusiveProgressJob(viewModelScope, ::captureDatabaseEntry, ::save)
    private val artistMetadata by savedStateHandle.saveable(saver = ArtistEditScreen.State.ArtistMetadata.Saver) {
        ArtistEditScreen.State.ArtistMetadata()
    }
    val state = ArtistEditScreen.State(
        artistMetadata = artistMetadata,
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
        formState = formState,
        savingState = saveJob.state,
    )

    private var hasLoaded by savedStateHandle.saved { mode == ArtistEditScreen.Mode.ADD }
    private val seriesById = flowFromSuspend { database.loadSeries() }
        .shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    private val merchById = flowFromSuspend { database.loadMerch() }
        .shareIn(viewModelScope, SharingStarted.Eagerly, 1)

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
        val seriesById = seriesById.first()
        val merchById = merchById.first()

        val links = artist.links.map(LinkModel::parse).sortedBy { it.logo }
        val storeLinks = artist.storeLinks.map(LinkModel::parse).sortedBy { it.logo }
        val commissions = artist.commissions.map(CommissionModel::parse)

        val seriesInferred = artist.seriesInferred.mapNotNull { seriesById[it] }
        val seriesConfirmed = artist.seriesConfirmed.mapNotNull { seriesById[it] }
        val merchInferred = artist.merchInferred.mapNotNull { merchById[it] }
        val merchConfirmed = artist.merchConfirmed.mapNotNull { merchById[it] }

        val textState = state.formState
        val status = artist.status
        textState.status.selectedIndex = ArtistStatus.entries.indexOf(status)

        val booth = artist.booth.orEmpty()
        if (booth.isNotBlank() || status.shouldStartLocked) {
            textState.booth.value.setTextAndPlaceCursorAtEnd(booth)
            textState.booth.lockState = EntryLockState.LOCKED
        }

        val name = artist.name.orEmpty()
        if (name.isNotBlank() || status.shouldStartLocked) {
            textState.name.value.setTextAndPlaceCursorAtEnd(name)
            textState.name.lockState = EntryLockState.LOCKED
        }

        val summary = artist.summary.orEmpty()
        if (summary.isNotBlank() || status.shouldStartLocked) {
            textState.summary.value.setTextAndPlaceCursorAtEnd(summary)
            textState.summary.lockState = EntryLockState.LOCKED
        }

        val notes = artist.notes.orEmpty()
        if (notes.isNotBlank() || status.shouldStartLocked) {
            textState.notes.value.setTextAndPlaceCursorAtEnd(notes)
            textState.notes.lockState = EntryLockState.LOCKED
        }

        val editorNotes = artist.editorNotes.orEmpty()
        if (editorNotes.isNotBlank() || status.shouldStartLocked) {
            textState.editorNotes.value.setTextAndPlaceCursorAtEnd(editorNotes)
            textState.editorNotes.lockState = EntryLockState.LOCKED
        }

        if (links.isNotEmpty() || status.shouldStartLocked) {
            state.links.replaceAll(links)
            textState.links.lockState = EntryLockState.LOCKED
        }
        if (storeLinks.isNotEmpty() || status.shouldStartLocked) {
            state.storeLinks.replaceAll(storeLinks)
            textState.storeLinks.lockState = EntryLockState.LOCKED
        }
        if (artist.catalogLinks.isNotEmpty() || status.shouldStartLocked) {
            state.catalogLinks.replaceAll(artist.catalogLinks)
            textState.catalogLinks.lockState = EntryLockState.LOCKED
        }
        if (artist.commissions.isNotEmpty() || status.shouldStartLocked) {
            state.commissions.replaceAll(commissions)
            textState.commissions.lockState = EntryLockState.LOCKED
        }

        if (seriesInferred.isNotEmpty() || status.shouldStartLocked) {
            state.seriesInferred.replaceAll(seriesInferred)
            textState.seriesInferred.lockState = EntryLockState.LOCKED
        }
        if (seriesConfirmed.isNotEmpty() || status.shouldStartLocked) {
            state.seriesConfirmed.replaceAll(seriesConfirmed)
            textState.seriesConfirmed.lockState = EntryLockState.LOCKED
        }
        if (merchInferred.isNotEmpty() || status.shouldStartLocked) {
            state.merchInferred.replaceAll(merchInferred)
            textState.merchInferred.lockState = EntryLockState.LOCKED
        }
        if (merchConfirmed.isNotEmpty() || status.shouldStartLocked) {
            state.merchConfirmed.replaceAll(merchConfirmed)
            textState.merchConfirmed.lockState = EntryLockState.LOCKED
        }

        val images = database.loadArtistImages(dataYear, artist)
        if (images.isNotEmpty()) {
            state.images.replaceAll(images)
        }

        artistMetadata.lastEditor = artist.lastEditor
        artistMetadata.lastEditTime = artist.lastEditTime
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

    private val imageLoader = SeriesImageLoader(dispatchers, viewModelScope, seriesImagesStore)
    fun seriesImage(info: SeriesInfo) = imageLoader.getSeriesImage(info.toImageInfo())

    fun onClickSave() = saveJob.launch()

    private fun captureDatabaseEntry(): Pair<List<EditImage>, ArtistDatabaseEntry.Impl> {
        val textState = state.formState
        val id = Uuid.parse(textState.id.value.text.toString())
        val status = ArtistStatus.entries[textState.status.selectedIndex]

        val booth = textState.booth.value.text.toString()
        val name = textState.name.value.text.toString()
        val summary = textState.summary.value.text.toString()

        val formState = state.formState
        // TODO: Include pending value?
        val links = (state.links.toList().map { it.link } +
                formState.links.value.text.toString()
                    .takeIf { it.isNotBlank() }).filterNotNull()
        val storeLinks = (state.storeLinks.toList().map { it.link } +
                formState.storeLinks.value.text.toString()
                    .takeIf { it.isNotBlank() }).filterNotNull()
        val catalogLinks = (state.catalogLinks.toList() +
                formState.catalogLinks.value.text.toString()
                    .takeIf { it.isNotBlank() }).filterNotNull()

        val notes = textState.notes.value.text.toString()
        val editorNotes = textState.editorNotes.value.text.toString()
        val commissions = (state.commissions.toList().map { it.serializedValue } +
                formState.commissions.value.text.toString()
                    .takeIf { it.isNotBlank() }).filterNotNull()
        val seriesInferred = state.seriesInferred.toList().map { it.id }
        val seriesConfirmed = state.seriesConfirmed.toList().map { it.id }
        val merchInferred = state.merchInferred.toList().map { it.name }
        val merchConfirmed = state.merchConfirmed.toList().map { it.name }

        val images = state.images.toList()
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
