package com.thekeeperofpie.artistalleydatabase.anilist

import android.app.Application
import androidx.security.crypto.MasterKey
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntryDao
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntryDao
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListOAuthStore
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApiWrapper
import com.thekeeperofpie.artistalleydatabase.network_utils.NetworkAuthProvider
import com.thekeeperofpie.artistalleydatabase.network_utils.NetworkSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AniListHiltModule {

    @Singleton
    @Provides
    fun provideAniListOAuthStore(application: Application, masterKey: MasterKey) =
        AniListOAuthStore(application, masterKey)

    @Singleton
    @Provides
    fun provideAniListApi(
        application: ScopedApplication,
        networkSettings: NetworkSettings,
        okHttpClient: OkHttpClient,
    ) = AniListApi(application, networkSettings, okHttpClient)

    @Singleton
    @Provides
    fun provideAuthedAniListApi(
        scopedApplication: ScopedApplication,
        aniListOAuthStore: AniListOAuthStore,
        networkSettings: NetworkSettings,
        okHttpClient: OkHttpClient,
        featureOverrideProvider: FeatureOverrideProvider,
    ) = if (featureOverrideProvider.isReleaseBuild) {
        AuthedAniListApiWrapper(
            scopedApplication,
            aniListOAuthStore,
            networkSettings,
            okHttpClient,
        )
    } else {
        AuthedAniListApi(
            scopedApplication,
            aniListOAuthStore,
            networkSettings,
            okHttpClient,
        )
    }

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
        aniListApi: AniListApi,
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
        aniListApi: AniListApi,
    ) = CharacterRepository(application, appJson, characterEntryDao, aniListApi)

    @Singleton
    @Provides
    @StringKey(AniListUtils.GRAPHQL_API_HOST)
    @IntoMap
    fun provideAniListNetworkAuthProvider(
        oAuthStore: AniListOAuthStore,
    ): NetworkAuthProvider = oAuthStore
}
