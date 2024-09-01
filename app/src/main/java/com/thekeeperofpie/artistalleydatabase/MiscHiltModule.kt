package com.thekeeperofpie.artistalleydatabase

import android.app.Application
import com.thekeeperofpie.artistalleydatabase.image.crop.CropController
import com.thekeeperofpie.artistalleydatabase.image.crop.CropSettings
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.serialization.AppJson
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkClient
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import javax.inject.Singleton

/**
 * Dumping ground for dependencies which need to eventually be migrated to multiplatform.
 */
@Module
@InstallIn(SingletonComponent::class)
class MiscHiltModule {

    @Provides
    fun provideCropController(
        scope: CoroutineScope,
        application: Application,
        appFileSystem: AppFileSystem,
        settings: CropSettings,
    ) = CropController(scope, application, appFileSystem, settings)

    @Provides
    @Singleton
    fun provideApplicationComponent(
        application: Application,
        networkClient: NetworkClient,
        applicationScope: ApplicationScope,
        httpClient: HttpClient,
        vgmdbDatabase: VgmdbDatabase,
        json: Json,
    ) = ApplicationComponent::class.create(
        application = application,
        networkClient = networkClient,
        applicationScope = applicationScope,
        httpClient = httpClient,
        vgmdbDatabase = vgmdbDatabase,
        json = json,
    )

    @Provides
    fun provideJson(appJson: AppJson) = appJson.json

    @Provides
    fun provideArtistRepository(applicationComponent: ApplicationComponent) =
        applicationComponent.artistRepository

    @Provides
    fun provideAlbumRepository(applicationComponent: ApplicationComponent) =
        applicationComponent.albumRepository

    @Provides
    fun provideAlbumEntryDao(applicationComponent: ApplicationComponent) =
        applicationComponent.albumEntryDao

    @Provides
    fun provideVgmdbDataConverter(applicationComponent: ApplicationComponent) =
        applicationComponent.vgmdbDataConverter

    @Provides
    fun provideVgmdbArtistDao(applicationComponent: ApplicationComponent) =
        applicationComponent.vgmdbArtistDao

    @Provides
    fun provideVgmdbApi(applicationComponent: ApplicationComponent) = applicationComponent.vgmdbApi

    @Provides
    fun provideVgmdbAutocompleter(applicationComponent: ApplicationComponent) =
        applicationComponent.vgmdbAutocompleter
}
