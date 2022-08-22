package com.thekeeperofpie.artistalleydatabase.art

import com.thekeeperofpie.artistalleydatabase.AppDatabase
import com.thekeeperofpie.artistalleydatabase.art.json.ArtJson
import com.thekeeperofpie.artistalleydatabase.utils.AppJson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ArtEntryHiltModule {

    @Provides
    fun provideArtJson(appJson: AppJson) = ArtJson(appJson.json)

    @Provides
    fun provideArtEntryDao(appDatabase: AppDatabase) = appDatabase.artEntryDao()

    @Provides
    fun provideArtEntryEditDao(appDatabase: AppDatabase) = appDatabase.artEntryEditDao()

    @Provides
    fun provideArtEntryDetailsDao(appDatabase: AppDatabase) = appDatabase.artEntryDetailsDao()

    @Provides
    fun provideArtEntryBrowseDao(appDatabase: AppDatabase) = appDatabase.artEntryBrowseDao()

    @Provides
    fun provideArtEntryAdvancedSearchDao(appDatabase: AppDatabase) =
        appDatabase.artEntryAdvancedSearchDao()
}