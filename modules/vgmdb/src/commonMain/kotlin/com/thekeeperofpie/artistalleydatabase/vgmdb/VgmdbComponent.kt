package com.thekeeperofpie.artistalleydatabase.vgmdb

import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntryDao
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.VgmdbArtistDao
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
interface VgmdbComponent {

    @SingleIn(AppScope::class)
    @Provides
    fun provideAlbumEntryDao(database: VgmdbDatabase): AlbumEntryDao = database.albumEntryDao()

    @SingleIn(AppScope::class)
    @Provides
    fun provideArtistEntryDao(database: VgmdbDatabase): VgmdbArtistDao = database.artistEntryDao()
}
