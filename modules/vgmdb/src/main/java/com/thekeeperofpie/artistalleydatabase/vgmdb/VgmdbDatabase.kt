package com.thekeeperofpie.artistalleydatabase.vgmdb

import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntryDao
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistEntryDao

interface VgmdbDatabase {
    fun albumEntryDao(): AlbumEntryDao
    fun artistEntryDao(): ArtistEntryDao
}