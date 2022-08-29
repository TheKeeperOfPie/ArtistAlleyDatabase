package com.thekeeperofpie.artistalleydatabase

import android.app.Application
import androidx.room.Room
import androidx.work.WorkManager
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.anilist.AniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.AniListJson
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.art.autocomplete.Autocompleter
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryDataConverter
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryDetailsDao
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
    fun provideScopedApplication(application: Application) =
        application as ScopedApplication

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
    fun provideSettingsProvider(application: Application, appJson: AppJson) =
        SettingsProvider(application, appJson)

    @Provides
    fun provideAutocompleter(
        artEntryDao: ArtEntryDetailsDao,
        aniListJson: AniListJson,
        aniListApi: AniListApi,
        characterRepository: CharacterRepository,
        mediaRepository: MediaRepository,
        artEntryDataConverter: ArtEntryDataConverter,
    ) = Autocompleter(
        artEntryDao,
        aniListJson,
        aniListApi,
        characterRepository,
        mediaRepository,
        artEntryDataConverter
    )

    @Provides
    fun provideAniListJson(appJson: AppJson) = AniListJson(appJson.json)
}