package com.thekeeperofpie.artistalleydatabase.anime

import android.app.Application
import androidx.navigation.NavType
import androidx.security.crypto.MasterKey
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
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaGenreDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaTagDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaGenresController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaLicensorsController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaTagsController
import com.thekeeperofpie.artistalleydatabase.anime.notifications.NotificationsController
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationStatusController
import com.thekeeperofpie.artistalleydatabase.markdown.Markdown
import com.thekeeperofpie.artistalleydatabase.media.MediaPlayer
import com.thekeeperofpie.artistalleydatabase.news.AnimeNewsController
import com.thekeeperofpie.artistalleydatabase.news.NewsSettings
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.serialization.AppJson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.ktor.client.HttpClient
import okhttp3.OkHttpClient
import javax.inject.Singleton
import kotlin.reflect.KType

@Module
@InstallIn(SingletonComponent::class)
object AnimeHiltModule {

    @Singleton
    @Provides
    fun provideMediaPlayer(
        scope: ApplicationScope,
        application: Application,
        okHttpClient: OkHttpClient,
        featureOverrideProvider: FeatureOverrideProvider,
    ) = MediaPlayer(
        scope = scope,
        application = application,
        okHttpClient = okHttpClient,
        enableCache = featureOverrideProvider.enableAppMediaPlayerCache,
    )

    @Singleton
    @Provides
    fun provideIgnoreController(
        scope: ApplicationScope,
        ignoreDao: AnimeIgnoreDao,
        settings: AnimeSettings,
    ) = IgnoreController(scope, ignoreDao, settings)

    @Singleton
    @Provides
    fun provideAnimeNewsController(
        scope: ApplicationScope,
        httpClient: HttpClient,
        newsSettings: NewsSettings,
    ) = AnimeNewsController(scope, httpClient, newsSettings)

    @Singleton
    @Provides
    fun provideMediaListStatusController() = MediaListStatusController()

    @Singleton
    @Provides
    fun provideMediaTagsController(
        scope: ApplicationScope,
        aniListApi: AuthedAniListApi,
    ) = MediaTagsController(scope, aniListApi)

    @Singleton
    @Provides
    fun provideMediaGenresController(
        scope: ApplicationScope,
        aniListApi: AuthedAniListApi,
    ) = MediaGenresController(scope, aniListApi)

    @Singleton
    @Provides
    fun provideUserMediaListController(
        application: Application,
        scope: ApplicationScope,
        aniListApi: AuthedAniListApi,
        ignoreController: IgnoreController,
        statusController: MediaListStatusController,
        settings: AnimeSettings,
        appJson: AppJson,
        masterKey: MasterKey,
    ) = UserMediaListController(
        application = application,
        scope = scope,
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
        scope: ApplicationScope,
        aniListApi: AuthedAniListApi,
    ) = MediaLicensorsController(scope, aniListApi)

    @Singleton
    @Provides
    fun provideForumThreadStatusController() = ForumThreadStatusController()

    @Singleton
    @Provides
    fun provideForumThreadCommentStatusController() = ForumThreadCommentStatusController()

    @Singleton
    @Provides
    fun provideNotificationsController(
        scope: ApplicationScope,
        aniListApi: AuthedAniListApi,
    ) = NotificationsController(scope, aniListApi)

    @Singleton
    @Provides
    fun provideRecommendationStatusController() = RecommendationStatusController()

    @Singleton
    @Provides
    fun provideMarkdown(application: Application) = Markdown(application)

    @Singleton
    @Provides
    fun provideAnimeHistoryDao(database: AnimeDatabase) = database.animeHistoryDao()

    @Singleton
    @Provides
    fun provideAnimeIgnoreDao(database: AnimeDatabase) = database.animeIgnoreDao()

    @Singleton
    @Provides
    fun provideHistoryController(
        scope: ApplicationScope,
        historyDao: AnimeHistoryDao,
        settings: AnimeSettings,
    ) = HistoryController(scope, historyDao, settings)

    @Singleton
    @Provides
    @IntoSet
    fun provideNavigationTypeMap(): @JvmSuppressWildcards Map<KType, NavType<*>> =
        AnimeDestination.typeMap
}
