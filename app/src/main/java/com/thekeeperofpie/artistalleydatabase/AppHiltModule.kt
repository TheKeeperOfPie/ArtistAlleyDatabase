package com.thekeeperofpie.artistalleydatabase

import android.app.Application
import androidx.room.Room
import androidx.security.crypto.MasterKey
import com.thekeeperofpie.anichive.BuildConfig
import com.thekeeperofpie.anichive.R
import com.thekeeperofpie.artistalleydatabase.android_utils.AppMetadataProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.CryptoUtils
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.anilist.AniListDatabase
import com.thekeeperofpie.artistalleydatabase.anilist.AniListJson
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDatabase
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDatabase
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDatabase
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationController
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationOverrideProvider
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistDatabase
import com.thekeeperofpie.artistalleydatabase.settings.SettingsProvider
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.serialization.AppJson
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDatabase
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbJson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppHiltModule {

    @Singleton
    @Provides
    fun provideMasterKey(application: Application) = CryptoUtils.masterKey(application)

    @Singleton
    @Provides
    fun provideCustomApplication(application: Application) =
        application as CustomApplication

    @Singleton
    @Provides
    fun provideScopedApplication(application: Application) =
        application as ScopedApplication

    @Singleton
    @Provides
    fun provideApplicationScope(scopedApplication: ScopedApplication): ApplicationScope =
        scopedApplication.scope

    @Singleton
    @Provides
    fun provideAppFileSystem(application: Application) = AppFileSystem(application)

    @Singleton
    @Provides
    fun provideAppDatabase(application: Application) =
        Room.databaseBuilder(application, AppDatabase::class.java, "appDatabase")
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .addMigrations(Migration_6_7)
            .build()

    @Singleton
    @Provides
    fun provideAniListDatabase(appDatabase: AppDatabase): AniListDatabase = appDatabase

    @Singleton
    @Provides
    fun provideAnimeDatabase(appDatabase: AppDatabase): AnimeDatabase = appDatabase

    @Singleton
    @Provides
    fun provideArtEntryDatabase(appDatabase: AppDatabase): ArtEntryDatabase = appDatabase

    @Singleton
    @Provides
    fun provideCdEntryDatabase(appDatabase: AppDatabase): CdEntryDatabase = appDatabase

    @Singleton
    @Provides
    fun provideMusicalArtistDatabase(appDatabase: AppDatabase): MusicalArtistDatabase = appDatabase

    @Singleton
    @Provides
    fun provideVgmdbDatabase(appDatabase: AppDatabase): VgmdbDatabase = appDatabase

    @Singleton
    @Provides
    fun provideSettingsProvider(
        scopedApplication: ScopedApplication,
        masterKey: MasterKey,
        appJson: AppJson,
        featureOverrideProvider: FeatureOverrideProvider,
    ) = SettingsProvider(
        application = scopedApplication.app,
        masterKey = masterKey,
        appJson = appJson,
        featureOverrideProvider = featureOverrideProvider,
    ).apply {
        initialize(scopedApplication.scope)
    }

    @Singleton
    @Provides
    fun provideAniListJson(appJson: AppJson) = AniListJson(appJson.json)

    @Singleton
    @Provides
    fun provideVgmdbJson(appJson: AppJson) = VgmdbJson(appJson.json)

    @Singleton
    @Provides
    fun provideFeatureOverrideProvider(): FeatureOverrideProvider = AppFeatureOverrideProvider()

    @Singleton
    @Provides
    fun provideMonetizationFeatureOverrideProvider(
        scopedApplication: ScopedApplication,
        aniListApi: AuthedAniListApi,
    ): MonetizationOverrideProvider = AppMonetizationOverrideProvider(scopedApplication, aniListApi)

    @Singleton
    @Provides
    fun provideAppMetadataProvider(): AppMetadataProvider = object : AppMetadataProvider {
        override val versionCode = BuildConfig.VERSION_CODE
        override val versionName = BuildConfig.VERSION_NAME
        override val appIconDrawableRes = R.mipmap.ic_launcher
    }

    @Singleton
    @Provides
    fun provideMonetizationController(
        settings: MonetizationSettings,
        overrideProvider: MonetizationOverrideProvider,
    ) = MonetizationController(settings, overrideProvider)
}
