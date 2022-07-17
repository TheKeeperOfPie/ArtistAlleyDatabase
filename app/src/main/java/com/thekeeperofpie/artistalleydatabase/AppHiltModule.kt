package com.thekeeperofpie.artistalleydatabase

import android.app.Application
import androidx.room.Room
import androidx.work.WorkManager
import com.thekeeperofpie.artistalleydatabase.anilist.AniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryDataConverter
import com.thekeeperofpie.artistalleydatabase.autocomplete.Autocompleter
import com.thekeeperofpie.artistalleydatabase.json.AppJson
import com.thekeeperofpie.artistalleydatabase.json.AppMoshi
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
    fun provideAppDatabase(application: Application) =
        Room.databaseBuilder(application, AppDatabase::class.java, "appDatabase")
            .build()

    @Provides
    fun provideWorkManager(application: Application) = WorkManager.getInstance(application)

    @Provides
    fun provideAppMoshi() = AppMoshi()

    @Provides
    fun provideAppJson() = AppJson()

    @Provides
    fun provideSettingsProvider(application: Application, appMoshi: AppMoshi) =
        SettingsProvider(application, appMoshi)

    @Provides
    fun provideAutocompleter(
        artEntryDao: ArtEntryDao,
        appMoshi: AppMoshi,
        aniListApi: AniListApi,
        characterRepository: CharacterRepository,
        mediaRepository: MediaRepository,
        artEntryDataConverter: ArtEntryDataConverter,
    ) = Autocompleter(
        artEntryDao,
        appMoshi,
        aniListApi,
        characterRepository,
        mediaRepository,
        artEntryDataConverter
    )

    @Provides
    fun provideArtEntryDataConverter(appMoshi: AppMoshi) = ArtEntryDataConverter(appMoshi)
}