package com.thekeeperofpie.artistalleydatabase.anime

import android.app.Application
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityReplyStatusController
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityStatusController
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.markdown.AniListSpoilerPlugin
import com.thekeeperofpie.artistalleydatabase.anime.markdown.AniListTempPlugin
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaTagDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaLicensorsController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaTagsController
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.noties.markwon.Markwon
import io.noties.markwon.PrecomputedTextSetterCompat
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TableAwareMovementMethod
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.coil.CoilImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import io.noties.markwon.movement.MovementMethodPlugin
import okhttp3.OkHttpClient
import org.chromium.net.CronetEngine
import java.util.concurrent.Executors
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
    fun provideAnimeMediaIgnoreList(
        animeSettings: AnimeSettings,
        featureOverrideProvider: FeatureOverrideProvider,
    ) = AnimeMediaIgnoreList(animeSettings, featureOverrideProvider)

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

    @Singleton
    @Provides
    fun provideActivityReplyStatusController() = ActivityReplyStatusController()

    @Singleton
    @Provides
    fun provideMediaTagDialogController(mediaTagsController: MediaTagsController) =
        MediaTagDialogController(mediaTagsController)

    @Singleton
    @Provides
    fun provideMediaLicensorsController(
        scopedApplication: ScopedApplication,
        aniListApi: AuthedAniListApi,
    ) = MediaLicensorsController(scopedApplication, aniListApi)

    @Singleton
    @Provides
    fun provideMarkwon(application: Application) = Markwon.builder(application)
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
        .usePlugin(CoilImagesPlugin.create(application))
        .textSetter(PrecomputedTextSetterCompat.create(Executors.newCachedThreadPool()))
        .build()
}
