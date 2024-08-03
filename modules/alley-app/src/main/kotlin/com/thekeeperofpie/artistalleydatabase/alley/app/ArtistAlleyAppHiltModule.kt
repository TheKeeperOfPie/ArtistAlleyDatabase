package com.thekeeperofpie.artistalleydatabase.alley.app

import android.app.Application
import androidx.room.Room
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyDatabase
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.android_utils.CryptoUtils
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.anilist.AniListSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ArtistAlleyAppHiltModule {

    @Singleton
    @Provides
    fun provideArtistAlleyAppDatabase(application: Application) =
        Room.databaseBuilder(
            application,
            ArtistAlleyAppDatabase::class.java,
            "artistAlleyAppDatabase"
        )
            .fallbackToDestructiveMigration(true)
            .addMigrations(ArtistAlleyAppDatabase.Version_6_7, ArtistAlleyAppDatabase.Version_7_8)
            .build()

    @Singleton
    @Provides
    fun provideArtistAlleyDatabase(database: ArtistAlleyAppDatabase): ArtistAlleyDatabase = database

    @Singleton
    @Provides
    fun provideArtistAlleyAppSettings(scopedApplication: ScopedApplication) =
        ArtistAlleyAppSettings(scopedApplication)

    @Singleton
    @Provides
    fun provideArtistAlleySettings(
        artistAlleyAppSettings: ArtistAlleyAppSettings,
    ): ArtistAlleySettings = artistAlleyAppSettings

    @Singleton
    @Provides
    fun provideAniListSettings(artistAlleyAppSettings: ArtistAlleyAppSettings): AniListSettings =
        artistAlleyAppSettings

    @Singleton
    @Provides
    fun provideMasterKey(application: Application) = CryptoUtils.masterKey(application)

    @Singleton
    @Provides
    fun provideScopedApplication(application: Application) = application as ScopedApplication
}
