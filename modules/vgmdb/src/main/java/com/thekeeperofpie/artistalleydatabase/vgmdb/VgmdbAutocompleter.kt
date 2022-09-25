package com.thekeeperofpie.artistalleydatabase.vgmdb

import androidx.annotation.WorkerThread
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.android_utils.Either
import com.thekeeperofpie.artistalleydatabase.form.EntrySection
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistColumnEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class VgmdbAutocompleter(
    private val vgmdbApi: VgmdbApi,
    private val vgmdbJson: VgmdbJson,
    private val vgmdbDataConverter: VgmdbDataConverter,
    private val artistRepository: ArtistRepository,
) {

    suspend fun queryPerformersLocal(
        query: String,
        queryLocal: suspend (query: String) -> List<String>
    ) = queryLocal(query).map {
        val either = vgmdbJson.parseArtistColumn(it)
        if (either is Either.Right) {
            artistRepository.getEntry(either.value.id)
                .filterNotNull()
                .map(vgmdbDataConverter::artistEntry)
                .filterNotNull()
                .startWith(vgmdbDataConverter.artistPlaceholder(either.value))
        } else {
            flowOf(EntrySection.MultiText.Entry.Custom(it))
        }
    }

    suspend fun queryPerformersNetwork(query: String) = flow {
        emit(emptyList())
        vgmdbApi.searchArtists(query).map {
            flow { vgmdbApi.getArtist(it.id)?.let { emit(it) } }
                .map(vgmdbDataConverter::artistEntry)
                .startWith(vgmdbDataConverter.artistPlaceholder(it))
        }.let { combine(it) { it.toList() } }
            .let { emitAll(it) }
    }

    suspend fun queryComposersLocal(
        query: String,
        queryLocal: suspend (query: String) -> List<String>
    ) = queryLocal(query).map {
        val either = vgmdbJson.parseArtistColumn(it)
        if (either is Either.Right) {
            artistRepository.getEntry(either.value.id)
                .filterNotNull()
                .map(vgmdbDataConverter::artistEntry)
                .filterNotNull()
                .startWith(vgmdbDataConverter.artistPlaceholder(either.value))
        } else {
            flowOf(EntrySection.MultiText.Entry.Custom(it))
        }
    }

    suspend fun queryComposersNetwork(query: String) = flow {
        emit(emptyList())
        vgmdbApi.searchArtists(query).map {
            flow { vgmdbApi.getArtist(it.id)?.let { emit(it) } }
                .map(vgmdbDataConverter::artistEntry)
                .startWith(vgmdbDataConverter.artistPlaceholder(it))
        }.let { combine(it) { it.toList() } }
            .let { emitAll(it) }
    }

    @WorkerThread
    suspend fun fillArtistField(artist: ArtistColumnEntry) =
        artistRepository.getEntry(artist.id)
            .filterNotNull()
            .map { vgmdbDataConverter.artistEntry(it, artist.manuallyChosen) }
}