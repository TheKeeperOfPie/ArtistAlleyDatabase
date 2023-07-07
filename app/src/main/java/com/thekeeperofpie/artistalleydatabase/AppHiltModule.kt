package com.thekeeperofpie.artistalleydatabase

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.room.Room
import androidx.security.crypto.MasterKey
import androidx.work.WorkManager
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.CryptoUtils
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.anilist.AniListDatabase
import com.thekeeperofpie.artistalleydatabase.anilist.AniListJson
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDatabase
import com.thekeeperofpie.artistalleydatabase.art.persistence.ArtSettings
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDatabase
import com.thekeeperofpie.artistalleydatabase.entry.EntrySettings
import com.thekeeperofpie.artistalleydatabase.json.AppMoshi
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistDatabase
import com.thekeeperofpie.artistalleydatabase.network_utils.NetworkSettings
import com.thekeeperofpie.artistalleydatabase.settings.SettingsProvider
import com.thekeeperofpie.artistalleydatabase.utils.PendingIntentRequestCodes
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDatabase
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbJson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppHiltModule {

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
    fun provideAppDatabase(application: Application) =
        Room.databaseBuilder(application, AppDatabase::class.java, "appDatabase")
            .build()

    @Singleton
    @Provides
    fun provideAniListDatabase(appDatabase: AppDatabase): AniListDatabase = appDatabase

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
    fun provideWorkManager(application: Application) = WorkManager.getInstance(application)

    @Singleton
    @Provides
    fun provideAppMoshi() = AppMoshi()

    @Singleton
    @Provides
    fun provideMoshi(appMoshi: AppMoshi) = appMoshi.moshi

    @Singleton
    @Provides
    fun provideAppJson() = AppJson()

    @Singleton
    @Provides
    fun provideSettingsProvider(
        scopedApplication: ScopedApplication,
        masterKey: MasterKey,
        appJson: AppJson
    ) = SettingsProvider(
        application = scopedApplication.app,
        masterKey = masterKey,
        appJson = appJson,
        crashNotificationContentIntent = PendingIntent.getActivity(
            scopedApplication.app,
            PendingIntentRequestCodes.INFO_CRASH_MAIN_ACTIVITY_OPEN.code,
            Intent(scopedApplication.mainActivityInternalAction).apply {
                data = Uri.parse("${scopedApplication.app.packageName}:///${AppNavDestinations.CRASH.id}")
                setClass(scopedApplication.app, MainActivity::class.java)
                putExtra(
                    MainActivity.STARTING_NAV_DESTINATION,
                    AppNavDestinations.CRASH.id,
                )
            },
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
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
    fun provideArtSettings(settingsProvider: SettingsProvider) = settingsProvider as ArtSettings

    @Singleton
    @Provides
    fun provideEntrySettings(settingsProvider: SettingsProvider) = settingsProvider as EntrySettings

    @Singleton
    @Provides
    fun provideNetworkSettings(settingsProvider: SettingsProvider) =
        settingsProvider as NetworkSettings

    @Singleton
    @Provides
    fun provideAnimeSettings(settingsProvider: SettingsProvider) =
        settingsProvider as AnimeSettings
}
