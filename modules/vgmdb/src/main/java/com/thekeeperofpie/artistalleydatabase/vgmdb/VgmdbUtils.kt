package com.thekeeperofpie.artistalleydatabase.vgmdb

import com.thekeeperofpie.artistalleydatabase.form.EntrySection.MultiText.Entry
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumColumnEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistColumnEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistEntry

object VgmdbUtils {

    fun albumId(entry: Entry) = when ((entry as? Entry.Prefilled<*>)?.value) {
        is AlbumEntry -> entry.id
        is AlbumColumnEntry -> entry.id
        else -> null
    }

    fun artistId(entry: Entry) = when ((entry as? Entry.Prefilled<*>)?.value) {
        is ArtistEntry -> entry.id
        is ArtistColumnEntry -> entry.id
        else -> null
    }
}