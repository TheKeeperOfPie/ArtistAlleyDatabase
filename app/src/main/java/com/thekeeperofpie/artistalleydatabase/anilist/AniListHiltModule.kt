package com.thekeeperofpie.artistalleydatabase.anilist

import com.thekeeperofpie.artistalleydatabase.AppDatabase
import com.thekeeperofpie.artistalleydatabase.CustomApplication
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntryDao
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AniListHiltModule {

    @Provides
    fun provideAniListApi() = AniListApi()

    @Provides
    fun provideMediaEntryDao(appDatabase: AppDatabase) = appDatabase.mediaEntryDao()

    @Provides
    fun provideMediaRepository(
        application: CustomApplication,
        mediaEntryDao: MediaEntryDao,
        aniListApi: AniListApi
    ) = MediaRepository(application, mediaEntryDao, aniListApi)
}