package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.edit.AlleyEditDestination
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
import com.thekeeperofpie.artistalleydatabase.entry.EntryLockState
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@AssistedInject
class ArtistEditViewModel(
    database: AlleyEditDatabase,
    @Assisted route: AlleyEditDestination.ArtistEdit,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val artist = flowFromSuspend { database.loadArtist(route.dataYear, route.artistId) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val textState by savedStateHandle.saveable(saver = ArtistEditScreen.State.TextState.Saver) { ArtistEditScreen.State.TextState() }
    val state = ArtistEditScreen.State(
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
    )

    private var hasLoaded by savedStateHandle.saved { false }
    private val seriesById = flowFromSuspend { database.loadSeries() }
        .shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    private val merchById = flowFromSuspend { database.loadMerch() }
        .shareIn(viewModelScope, SharingStarted.Eagerly, 1)

    init {
        if (!hasLoaded) {
            viewModelScope.launch {
                val artist = database.loadArtist(route.dataYear, route.artistId) ?: return@launch
                val seriesById = seriesById.first()
                val merchById = merchById.first()
                Snapshot.withMutableSnapshot {
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

                    state.links += artist.links.map(LinkModel::parse).sortedBy { it.logo }
                    state.storeLinks += artist.storeLinks.map(LinkModel::parse).sortedBy { it.logo }
                    state.catalogLinks += artist.catalogLinks.map(LinkModel::parse)
                        .sortedBy { it.logo }
                    state.commissions += artist.commissions

                    state.seriesInferred += artist.seriesInferred.mapNotNull { seriesById[it] }
                    state.seriesConfirmed += artist.seriesConfirmed.mapNotNull { seriesById[it] }
                    state.merchInferred += artist.merchInferred.mapNotNull { merchById[it] }
                    state.merchConfirmed += artist.merchConfirmed.mapNotNull { merchById[it] }
                }
                hasLoaded = true
            }
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

    @AssistedFactory
    interface Factory {
        fun create(
            route: AlleyEditDestination.ArtistEdit,
            savedStateHandle: SavedStateHandle,
        ): ArtistEditViewModel
    }
}
