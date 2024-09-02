package com.thekeeperofpie.artistalleydatabase.musical_artists

import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import me.tatarka.inject.annotations.Provides

interface MusicalArtistComponent {

    @SingletonScope
    @Provides
    fun provideMusicalArtistDao(database: MusicalArtistDatabase) = database.musicalArtistDao()
}
