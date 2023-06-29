package com.thekeeperofpie.artistalleydatabase.alley.app

import android.app.Application
import androidx.room.Room
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyDatabase
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleySettings
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
        ).build()

    @Singleton
    @Provides
    fun provideArtistAlleyDatabase(database: ArtistAlleyAppDatabase): ArtistAlleyDatabase = database

    @Singleton
    @Provides
    fun provideArtistAlleyAppSettings(application: Application) =
        ArtistAlleyAppSettings(application)

    @Singleton
    @Provides
    fun provideArtistAlleySettings(
        artistAlleyAppSettings: ArtistAlleyAppSettings
    ): ArtistAlleySettings = artistAlleyAppSettings
}
