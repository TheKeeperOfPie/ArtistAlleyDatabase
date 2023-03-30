package com.thekeeperofpie.artistalleydatabase.anilist

import android.app.Application
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntryDao
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntryDao
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListOAuthStore
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AniListHiltModule {

    @Singleton
    @Provides
    fun provideAniListOAuthStore(application: Application) = AniListOAuthStore(application)

    @Singleton
    @Provides
    fun provideAniListApi(application: ScopedApplication, aniListCache: AniListCache) =
        AniListApi(application, aniListCache)

    @Singleton
    @Provides
    fun provideAuthedAniListApi(
        aniListCache: AniListCache,
        aniListOAuthStore: AniListOAuthStore
    ) = AuthedAniListApi(aniListCache, aniListOAuthStore)

    @Singleton
    @Provides
    fun provideAniListCache(application: Application) = AniListCache(application)

    @Singleton
    @Provides
    fun provideAniListDataConverter(aniListJson: AniListJson) = AniListDataConverter(aniListJson)

    @Singleton
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

    @Singleton
    @Provides
    fun provideMediaEntryDao(database: AniListDatabase) = database.mediaEntryDao()

    @Singleton
    @Provides
    fun provideMediaRepository(
        application: ScopedApplication,
        mediaEntryDao: MediaEntryDao,
        aniListApi: AniListApi
    ) = MediaRepository(application, mediaEntryDao, aniListApi)

    @Singleton
    @Provides
    fun provideCharacterEntryDao(database: AniListDatabase) = database.characterEntryDao()

    @Singleton
    @Provides
    fun provideCharacterRepository(
        application: ScopedApplication,
        appJson: AppJson,
        characterEntryDao: CharacterEntryDao,
        aniListApi: AniListApi
    ) = CharacterRepository(application, appJson, characterEntryDao, aniListApi)
}