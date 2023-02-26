package com.thekeeperofpie.artistalleydatabase.musical_artists

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MusicalArtistHiltModule {

    @Singleton
    @Provides
    fun provideMusicalArtistDao(database: MusicalArtistDatabase) = database.musicalArtistDao()
}