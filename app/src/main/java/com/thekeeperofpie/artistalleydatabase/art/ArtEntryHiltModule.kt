package com.thekeeperofpie.artistalleydatabase.art

import com.thekeeperofpie.artistalleydatabase.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ArtEntryHiltModule {

    @Provides
    fun provideArtEntryDao(appDatabase: AppDatabase) = appDatabase.artEntryDao()

    @Provides
    fun provideArtEntryEditDao(appDatabase: AppDatabase) = appDatabase.artEntryEditDao()
}