package com.thekeeperofpie.artistalleydatabase.anilist

import android.app.Application
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntryDao
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
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
    fun provideAniListApi(application: Application) = AniListApi(application)

    @Provides
    fun provideAniListDataConverter(aniListJson: AniListJson) = AniListDataConverter(aniListJson)

    @Provides
    fun provideAniListAutocompleter(
        aniListJson: AniListJson,
        aniListApi: AniListApi,
        characterRepository: CharacterRepository,
        mediaRepository: MediaRepository,
        aniListDataConverter: AniListDataConverter,
    ) = AniListAutocompleter(
        aniListJson,
        aniListApi,
        characterRepository,
        mediaRepository,
        aniListDataConverter
    )

    @Provides
    fun provideMediaEntryDao(database: AniListDatabase) = database.mediaEntryDao()

    @Provides
    fun provideMediaRepository(
        application: ScopedApplication,
        mediaEntryDao: MediaEntryDao,
        aniListApi: AniListApi
    ) = MediaRepository(application, mediaEntryDao, aniListApi)

    @Provides
    fun provideCharacterEntryDao(database: AniListDatabase) = database.characterEntryDao()

    @Provides
    fun provideCharacterRepository(
        application: ScopedApplication,
        characterEntryDao: CharacterEntryDao,
        aniListApi: AniListApi
    ) = CharacterRepository(application, characterEntryDao, aniListApi)
}