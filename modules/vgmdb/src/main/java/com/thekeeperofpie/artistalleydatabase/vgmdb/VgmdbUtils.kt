package com.thekeeperofpie.artistalleydatabase.vgmdb

import com.thekeeperofpie.artistalleydatabase.form.EntrySection.MultiText.Entry
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumColumnEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistColumnEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistEntry

object VgmdbUtils {

    fun albumId(entry: Entry) = when (val value = (entry as? Entry.Prefilled<*>)?.value) {
        is AlbumEntry -> value.id
        is AlbumColumnEntry -> value.id
        else -> null
    }

    fun artistId(entry: Entry) = when (val value = (entry as? Entry.Prefilled<*>)?.value) {
        is ArtistEntry -> value.id
        is ArtistColumnEntry -> value.id
        else -> null
    }
}