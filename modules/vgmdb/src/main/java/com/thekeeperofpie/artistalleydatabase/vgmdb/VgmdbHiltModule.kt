package com.thekeeperofpie.artistalleydatabase.vgmdb

import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntryDao
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumRepository
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistRepository
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
        albumEntryDao: AlbumEntryDao,
        artistEntryDao: ArtistEntryDao,
        vgmdbJson: VgmdbJson
    ) = VgmdbApi(albumEntryDao, artistEntryDao, vgmdbJson)

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
        artistEntryDao: ArtistEntryDao,
        vgmdbApi: VgmdbApi
    ) = ArtistRepository(application, artistEntryDao, vgmdbApi)
}