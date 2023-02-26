package com.thekeeperofpie.artistalleydatabase.data

import com.thekeeperofpie.artistalleydatabase.anilist.AniListDataConverter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataHiltModule {

    @Singleton
    @Provides
    fun provideDataConverter(aniListDataConverter: AniListDataConverter) =
        DataConverter(aniListDataConverter)
}