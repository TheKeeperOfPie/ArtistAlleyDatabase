package com.thekeeperofpie.artistalleydatabase.musical_artists

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

interface MusicalArtistComponent {

    @SingleIn(AppScope::class)
    @Provides
    fun provideMusicalArtistDao(database: MusicalArtistDatabase): MusicalArtistDao =
        database.musicalArtistDao()
}
