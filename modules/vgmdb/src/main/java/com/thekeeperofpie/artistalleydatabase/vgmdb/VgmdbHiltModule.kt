package com.thekeeperofpie.artistalleydatabase.vgmdb

import android.app.Application
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntryDao
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumRepository
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistRepository
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.VgmdbArtistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class VgmdbHiltModule {

    @Provides
    fun provideAlbumEntryDao(database: VgmdbDatabase) = database.albumEntryDao()

    @Provides
    fun provideArtistEntryDao(database: VgmdbDatabase) = database.artistEntryDao()

    @Provides
    fun provideVgmdbApi(
        application: Application,
        albumEntryDao: AlbumEntryDao,
        vgmdbArtistDao: VgmdbArtistDao,
        vgmdbJson: VgmdbJson
    ) = VgmdbApi(application, albumEntryDao, vgmdbArtistDao, vgmdbJson)

    @Provides
    fun provideVgmdbDataConverter(vgmdbJson: VgmdbJson) = VgmdbDataConverter(vgmdbJson)

    @Provides
    fun provideVgmdbAutocompleter(
        vgmdbApi: VgmdbApi,
        vgmdbJson: VgmdbJson,
        vgmdbDataConverter: VgmdbDataConverter,
        artistRepository: ArtistRepository,
    ) = VgmdbAutocompleter(vgmdbApi, vgmdbJson, vgmdbDataConverter, artistRepository)

    @Provides
    fun provideAlbumRepository(
        application: ScopedApplication,
        albumEntryDao: AlbumEntryDao,
        vgmdbApi: VgmdbApi
    ) = AlbumRepository(application, albumEntryDao, vgmdbApi)

    @Provides
    fun provideArtistRepository(
        application: ScopedApplication,
        vgmdbArtistDao: VgmdbArtistDao,
        vgmdbApi: VgmdbApi
    ) = ArtistRepository(application, vgmdbArtistDao, vgmdbApi)
}