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
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.PlatformImageCache
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.entry.EntryLockState
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.StateUtils
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

@AssistedInject
class ArtistEditViewModel(
    dispatchers: CustomDispatchers,
    private val database: AlleyEditDatabase,
    seriesImagesStore: SeriesImagesStore,
    @Assisted private val dataYear: DataYear,
    @Assisted artistId: Uuid,
    @Assisted internal val mode: ArtistEditScreen.Mode,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val textState by savedStateHandle.saveable(saver = ArtistEditScreen.State.TextState.Saver) {
        ArtistEditScreen.State.TextState().apply {
            id.value.setTextAndPlaceCursorAtEnd(artistId.toString())
            id.lockState = EntryLockState.LOCKED
        }
    }
    private val saved = savedStateHandle.getMutableStateFlow<Boolean?>("saved", false)
    val state = ArtistEditScreen.State(
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
        saved = saved,
    )

    private var hasLoaded by savedStateHandle.saved { mode == ArtistEditScreen.Mode.ADD }
    private val seriesById = flowFromSuspend { database.loadSeries() }
        .shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    private val merchById = flowFromSuspend { database.loadMerch() }
        .shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    private var artist: ArtistDatabaseEntry.Impl? = null

    init {
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

        val seriesInferred = artist.seriesInferred.mapNotNull { seriesById[it] }
        val seriesConfirmed = artist.seriesConfirmed.mapNotNull { seriesById[it] }
        val merchInferred = artist.merchInferred.mapNotNull { merchById[it] }
        val merchConfirmed = artist.merchConfirmed.mapNotNull { merchById[it] }

        // TODO: Fill out other fields and store lock state in database
        val textState = state.textState
        artist.booth?.ifBlank { null }?.let {
            textState.booth.value.setTextAndPlaceCursorAtEnd(it)
            textState.booth.lockState = EntryLockState.LOCKED
        }

        textState.name.value.setTextAndPlaceCursorAtEnd(artist.name)
        textState.name.lockState = EntryLockState.LOCKED

        artist.summary?.ifBlank { null }?.let {
            textState.summary.value.setTextAndPlaceCursorAtEnd(it)
            textState.summary.lockState = EntryLockState.LOCKED
        }

        artist.notes?.ifBlank { null }?.let {
            textState.notes.pendingValue.setTextAndPlaceCursorAtEnd(it)
            textState.notes.lockState = EntryLockState.LOCKED
        }

        state.links += links
        state.storeLinks += storeLinks
        state.catalogLinks += artist.catalogLinks
        state.commissions += artist.commissions

        state.seriesInferred += seriesInferred
        state.seriesConfirmed += seriesConfirmed
        state.merchInferred += merchInferred
        state.merchConfirmed += merchConfirmed

        val images = database.loadArtistImages(dataYear, artist)
        if (images.isNotEmpty()) {
            state.images += images
        }
    }

    fun seriesPredictions(query: String) =
        seriesById.mapLatest {
            val matching = mutableListOf<Pair<SeriesInfo, Int>>()
            it.values.forEach {
                val priority = when {
                    it.titlePreferred.contains(query, ignoreCase = true) -> 0
                    it.titleRomaji.contains(query, ignoreCase = true) -> 2
                    it.titleEnglish.contains(query, ignoreCase = true) -> 1
                    it.titleNative.contains(query, ignoreCase = true) -> 3
                    else -> null
                }
                if (priority != null) {
                    matching += it to priority
                }
            }
            matching.sortedBy { it.second }.map { it.first }
        }
            .flowOn(PlatformDispatchers.IO)

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

    // TODO: Saving indicator and exit on done
    fun onClickSave() {
        val textState = state.textState
        val id = try {
            Uuid.parse(textState.id.value.text.toString())
        } catch (_: IllegalArgumentException) {
            // TODO: Show error to user
            return
        }

        val booth = textState.booth.value.text.toString()
        val name = textState.name.value.text.toString()
        val summary = textState.summary.value.text.toString()

        // TODO: Include pending value?
        val links = state.links.toList().map { it.link }
        val storeLinks = state.storeLinks.toList().map { it.link }
        val catalogLinks = state.catalogLinks.toList()

        val notes = textState.notes.pendingValue.text.toString()
        val commissions = state.commissions.toList()
        val seriesInferred = state.seriesInferred.toList().map { it.id }
        val seriesConfirmed = state.seriesConfirmed.toList().map { it.id }
        val merchInferred = state.merchInferred.toList().map { it.name }
        val merchConfirmed = state.merchConfirmed.toList().map { it.name }

        val images = state.images.toList()
        viewModelScope.launch {
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
                            artistId = id,
                            platformFile = file,
                        )
                    }
                }
            }

            database.saveArtist(
                dataYear = dataYear,
                initial = artist,
                updated = ArtistDatabaseEntry.Impl(
                    year = dataYear,
                    id = id.toString(),
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
                    images = finalImages.map {
                        CatalogImage(name = it.name, width = it.width, height = it.height)
                    },
                    counter = 1,
                )
            )
            saved.value = true
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
