package com.thekeeperofpie.artistalleydatabase

import android.app.Application
import androidx.room.Room
import androidx.work.WorkManager
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.anilist.AniListDatabase
import com.thekeeperofpie.artistalleydatabase.anilist.AniListJson
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryAddViewModel
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDatabase
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDatabase
import com.thekeeperofpie.artistalleydatabase.json.AppMoshi
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDatabase
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbJson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AppHiltModule {

    @Provides
    fun provideCustomApplication(application: Application) =
        application as CustomApplication

    @Provides
    fun provideScopedApplication(application: Application) =
        application as ScopedApplication

    @Provides
    fun provideAppDatabase(application: Application) =
        Room.databaseBuilder(application, AppDatabase::class.java, "appDatabase")
            .build()

    @Provides
    fun provideAniListDatabase(appDatabase: AppDatabase): AniListDatabase = appDatabase

    @Provides
    fun provideArtEntryDatabase(appDatabase: AppDatabase): ArtEntryDatabase = appDatabase

    @Provides
    fun provideCdEntryDatabase(appDatabase: AppDatabase): CdEntryDatabase = appDatabase

    @Provides
    fun provideVgmdbDatabase(appDatabase: AppDatabase): VgmdbDatabase = appDatabase

    @Provides
    fun provideWorkManager(application: Application) = WorkManager.getInstance(application)

    @Provides
    fun provideAppMoshi() = AppMoshi()

    @Provides
    fun provideMoshi(appMoshi: AppMoshi) = appMoshi.moshi

    @Provides
    fun provideAppJson() = AppJson()

    @Provides
    fun provideSettingsProvider(application: Application, appJson: AppJson) =
        SettingsProvider(application, appJson)

    @Provides
    fun provideAniListJson(appJson: AppJson) = AniListJson(appJson.json)

    @Provides
    fun provideVgmdbJson(appJson: AppJson) = VgmdbJson(appJson.json)

    @Provides
    fun provideArtAddEntryViewModelPersister(settingsProvider: SettingsProvider) =
        settingsProvider as ArtEntryAddViewModel.Persister
}