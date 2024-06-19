package com.thekeeperofpie.artistalleydatabase.alley

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ArtistAlleyHiltModule {

    @Provides
    @Singleton
    fun provideArtistEntryDao(database: ArtistAlleyDatabase) = database.artistEntryDao()

    @Provides
    @Singleton
    fun provideStampRallyEntryDao(database: ArtistAlleyDatabase) = database.stampRallyEntryDao()
}
