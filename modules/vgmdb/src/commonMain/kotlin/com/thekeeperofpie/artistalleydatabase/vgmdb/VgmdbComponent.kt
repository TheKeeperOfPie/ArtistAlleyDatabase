package com.thekeeperofpie.artistalleydatabase.vgmdb

import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import me.tatarka.inject.annotations.Provides

@SingletonScope
interface VgmdbComponent {

    @SingletonScope
    @Provides
    fun provideAlbumEntryDao(database: VgmdbDatabase) = database.albumEntryDao()

    @SingletonScope
    @Provides
    fun provideArtistEntryDao(database: VgmdbDatabase) = database.artistEntryDao()
}
