package com.thekeeperofpie.artistalleydatabase.vgmdb

import androidx.annotation.WorkerThread
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection.MultiText.Entry
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.Either
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

    suspend fun queryArtistsLocal(
        query: String,
        queryLocal: suspend (query: String) -> List<String>
    ) = queryLocal(query).map {
        val either = vgmdbJson.parseArtistColumn(it)
        if (either is Either.Right) {
            val placeholder = vgmdbDataConverter.artistPlaceholder(either.value)
            val artistId = either.value.id
            artistRepository.getEntry(artistId)
                .filterNotNull()
                .map { vgmdbDataConverter.artistEntry(it, manualChoice = true) }
                .filterNotNull()
                .startWith(placeholder)
        } else {
            flowOf(Entry.Custom(it))
        }
    }

    suspend fun queryArtistsNetwork(query: String) = flow {
        emit(emptyList())
        vgmdbApi.searchArtists(query).map {
            flow { vgmdbApi.getArtist(it.id)?.let { emit(it) } }
                .map { vgmdbDataConverter.artistEntry(it, manualChoice = true) }
                .startWith(vgmdbDataConverter.artistPlaceholder(it))
        }.let { combine(it) { it.toList() } }
            .let { emitAll(it) }
    }

    @WorkerThread
    suspend fun fillArtistField(artist: ArtistColumnEntry) = artistRepository.getEntry(artist.id)
        .filterNotNull()
        .map { vgmdbDataConverter.artistEntry(it, artist.manuallyChosen) }
}
