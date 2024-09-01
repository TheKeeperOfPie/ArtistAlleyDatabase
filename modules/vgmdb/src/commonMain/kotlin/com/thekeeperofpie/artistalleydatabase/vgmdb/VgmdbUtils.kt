package com.thekeeperofpie.artistalleydatabase.vgmdb

import com.thekeeperofpie.artistalleydatabase.entry.EntrySection.MultiText.Entry
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumColumnEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistColumnEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.VgmdbArtist

object VgmdbUtils {

    fun albumId(entry: Entry) = when (val value = (entry as? Entry.Prefilled<*>)?.value) {
        is AlbumEntry -> value.id
        is AlbumColumnEntry -> value.id
        is SearchResults.AlbumResult -> value.id
        else -> null
    }

    fun artistId(entry: Entry) = when (val value = (entry as? Entry.Prefilled<*>)?.value) {
        is VgmdbArtist -> value.id
        is ArtistColumnEntry -> value.id
        is SearchResults.ArtistResult -> value.id
        else -> null
    }

    fun artistUrl(id: String) = "https://vgmdb.net/artist/$id"
    fun albumUrl(id: String) = "https://vgmdb.net/album/$id"
}
