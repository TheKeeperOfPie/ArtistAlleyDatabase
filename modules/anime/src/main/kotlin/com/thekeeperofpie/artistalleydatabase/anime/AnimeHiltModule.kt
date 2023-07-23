package com.thekeeperofpie.artistalleydatabase.anime

import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityStatusController
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaTagsController
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import org.chromium.net.CronetEngine
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnimeHiltModule {

    @Singleton
    @Provides
    fun provideAppMediaPlayer(scopedApplication: ScopedApplication, cronetEngine: CronetEngine) =
        AppMediaPlayer(scopedApplication, cronetEngine)

    @Singleton
    @Provides
    fun provideAnimeMediaIgnoreList(animeSettings: AnimeSettings) =
        AnimeMediaIgnoreList(animeSettings)

    @Singleton
    @Provides
    fun provideAnimeNewsController(
        scopedApplication: ScopedApplication,
        okHttpClient: OkHttpClient,
        animeSettings: AnimeSettings,
    ) = AnimeNewsController(scopedApplication, okHttpClient, animeSettings)

    @Singleton
    @Provides
    fun provideMediaListStatusController() = MediaListStatusController()

    @Singleton
    @Provides
    fun provideMediaTagsController(
        scopedApplication: ScopedApplication,
        aniListApi: AuthedAniListApi,
    ) = MediaTagsController(scopedApplication, aniListApi)

    @Singleton
    @Provides
    fun provideUserMediaListController(
        scopedApplication: ScopedApplication,
        aniListApi: AuthedAniListApi,
        ignoreList: AnimeMediaIgnoreList,
        statusController: MediaListStatusController,
        settings: AnimeSettings,
    ) = UserMediaListController(
        scopedApplication = scopedApplication,
        aniListApi = aniListApi,
        ignoreList = ignoreList,
        statusController = statusController,
        settings = settings,
    )

    @Singleton
    @Provides
    fun provideFavoritesController() = FavoritesController()

    @Singleton
    @Provides
    fun provideActivityStatusController() = ActivityStatusController()
}
