package com.thekeeperofpie.artistalleydatabase.anime

import android.app.Application
import androidx.security.crypto.MasterKey
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityReplyStatusController
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityStatusController
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadCommentStatusController
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadStatusController
import com.thekeeperofpie.artistalleydatabase.anime.history.AnimeHistoryDao
import com.thekeeperofpie.artistalleydatabase.anime.history.HistoryController
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeIgnoreDao
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.markdown.AniListSpoilerPlugin
import com.thekeeperofpie.artistalleydatabase.anime.markdown.AniListTempPlugin
import com.thekeeperofpie.artistalleydatabase.anime.markdown.CoilImagesMarkwonPlugin
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaGenreDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaTagDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaGenresController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaLicensorsController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaTagsController
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsController
import com.thekeeperofpie.artistalleydatabase.anime.notifications.NotificationsController
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationStatusController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.noties.markwon.Markwon
import io.noties.markwon.SoftBreakAddsNewLinePlugin
import io.noties.markwon.core.CorePlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TableAwareMovementMethod
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import io.noties.markwon.movement.MovementMethodPlugin
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnimeHiltModule {

    @Singleton
    @Provides
    fun provideAppMediaPlayer(
        scopedApplication: ScopedApplication,
        okHttpClient: OkHttpClient,
        featureOverrideProvider: FeatureOverrideProvider,
    ) = AppMediaPlayer(scopedApplication, okHttpClient, featureOverrideProvider)

    @Singleton
    @Provides
    fun provideIgnoreController(
        scopedApplication: ScopedApplication,
        ignoreDao: AnimeIgnoreDao,
        settings: AnimeSettings,
    ) = IgnoreController(scopedApplication, ignoreDao, settings)

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
    fun provideMediaGenresController(
        scopedApplication: ScopedApplication,
        aniListApi: AuthedAniListApi,
    ) = MediaGenresController(scopedApplication, aniListApi)

    @Singleton
    @Provides
    fun provideUserMediaListController(
        scopedApplication: ScopedApplication,
        aniListApi: AuthedAniListApi,
        ignoreController: IgnoreController,
        statusController: MediaListStatusController,
        settings: AnimeSettings,
        appJson: AppJson,
        masterKey: MasterKey,
    ) = UserMediaListController(
        scopedApplication = scopedApplication,
        aniListApi = aniListApi,
        ignoreController = ignoreController,
        statusController = statusController,
        settings = settings,
        appJson = appJson,
        masterKey = masterKey,
    )

    @Singleton
    @Provides
    fun provideFavoritesController() = FavoritesController()

    @Singleton
    @Provides
    fun provideActivityStatusController() = ActivityStatusController()

    @Singleton
    @Provides
    fun provideActivityReplyStatusController() = ActivityReplyStatusController()

    @Singleton
    @Provides
    fun provideMediaTagDialogController(mediaTagsController: MediaTagsController) =
        MediaTagDialogController(mediaTagsController)

    @Singleton
    @Provides
    fun provideMediaGenreDialogController() = MediaGenreDialogController()

    @Singleton
    @Provides
    fun provideMediaLicensorsController(
        scopedApplication: ScopedApplication,
        aniListApi: AuthedAniListApi,
    ) = MediaLicensorsController(scopedApplication, aniListApi)

    @Singleton
    @Provides
    fun provideForumThreadStatusController() = ForumThreadStatusController()

    @Singleton
    @Provides
    fun provideForumThreadCommentStatusController() = ForumThreadCommentStatusController()

    @Singleton
    @Provides
    fun provideNotificationsController(
        scopedApplication: ScopedApplication,
        aniListApi: AuthedAniListApi,
    ) = NotificationsController(scopedApplication, aniListApi)

    @Singleton
    @Provides
    fun provideRecommendationStatusController() = RecommendationStatusController()

    @Singleton
    @Provides
    fun provideMarkwon(application: Application) = Markwon.builderNoCore(application)
        .usePlugin(CorePlugin.create())
        .usePlugin(SoftBreakAddsNewLinePlugin.create())
        .usePlugin(HtmlPlugin.create().apply {
            allowNonClosedTags(true)
            addHandler(AniListTempPlugin.CenterAlignTagHandler)
        })
        .usePlugin(LinkifyPlugin.create())
        .usePlugin(TablePlugin.create(application))
        .usePlugin(MovementMethodPlugin.create(TableAwareMovementMethod.create()))
        .usePlugin(StrikethroughPlugin.create())
        .usePlugin(AniListTempPlugin)
        .usePlugin(AniListSpoilerPlugin)
        .usePlugin(CoilImagesMarkwonPlugin.create(application))
        .build()

    @Singleton
    @Provides
    fun provideAnimeHistoryDao(database: AnimeDatabase) = database.animeHistoryDao()

    @Singleton
    @Provides
    fun provideAnimeIgnoreDao(database: AnimeDatabase) = database.animeIgnoreDao()

    @Singleton
    @Provides
    fun provideHistoryController(
        scopedApplication: ScopedApplication,
        historyDao: AnimeHistoryDao,
        settings: AnimeSettings,
    ) = HistoryController(scopedApplication, historyDao, settings)
}
