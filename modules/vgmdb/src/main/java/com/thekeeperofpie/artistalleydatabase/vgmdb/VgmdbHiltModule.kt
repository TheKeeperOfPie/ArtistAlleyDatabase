package com.thekeeperofpie.artistalleydatabase.vgmdb

import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntryDao
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumRepository
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistRepository
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.VgmdbArtistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class VgmdbHiltModule {

    @Singleton
    @Provides
    fun provideAlbumEntryDao(database: VgmdbDatabase) = database.albumEntryDao()

    @Singleton
    @Provides
    fun provideArtistEntryDao(database: VgmdbDatabase) = database.artistEntryDao()

    @Singleton
    @Provides
    fun provideVgmdbApi(
        albumEntryDao: AlbumEntryDao,
        vgmdbArtistDao: VgmdbArtistDao,
        vgmdbJson: VgmdbJson,
        okHttpClient: OkHttpClient,
    ) = VgmdbApi(albumEntryDao, vgmdbArtistDao, vgmdbJson, okHttpClient)

    @Singleton
    @Provides
    fun provideVgmdbDataConverter(vgmdbJson: VgmdbJson) = VgmdbDataConverter(vgmdbJson)

    @Singleton
    @Provides
    fun provideVgmdbAutocompleter(
        vgmdbApi: VgmdbApi,
        vgmdbJson: VgmdbJson,
        vgmdbDataConverter: VgmdbDataConverter,
        artistRepository: ArtistRepository,
    ) = VgmdbAutocompleter(vgmdbApi, vgmdbJson, vgmdbDataConverter, artistRepository)

    @Singleton
    @Provides
    fun provideAlbumRepository(
        application: ScopedApplication,
        albumEntryDao: AlbumEntryDao,
        vgmdbApi: VgmdbApi
    ) = AlbumRepository(application, albumEntryDao, vgmdbApi)

    @Singleton
    @Provides
    fun provideArtistRepository(
        application: ScopedApplication,
        vgmdbArtistDao: VgmdbArtistDao,
        vgmdbApi: VgmdbApi
    ) = ArtistRepository(application, vgmdbArtistDao, vgmdbApi)
}
