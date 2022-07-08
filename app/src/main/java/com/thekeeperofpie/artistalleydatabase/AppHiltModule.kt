package com.thekeeperofpie.artistalleydatabase

import android.app.Application
import androidx.room.Room
import androidx.work.WorkManager
import com.thekeeperofpie.artistalleydatabase.anilist.AniListApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AppHiltModule {

    @Provides
    fun provideAppDatabase(application: Application) =
        Room.databaseBuilder(application, AppDatabase::class.java, "appDatabase")
            .build()

    @Provides
    fun provideWorkManager(application: Application) = WorkManager.getInstance(application)

    @Provides
    fun provideSettingsProvider(application: Application) = SettingsProvider(application)

    @Provides
    fun provideAniListApi() = AniListApi()
}