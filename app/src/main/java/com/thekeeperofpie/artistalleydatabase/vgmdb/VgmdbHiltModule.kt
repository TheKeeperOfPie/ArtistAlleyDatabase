package com.thekeeperofpie.artistalleydatabase.vgmdb

import com.thekeeperofpie.artistalleydatabase.AppDatabase
import com.thekeeperofpie.artistalleydatabase.CustomApplication
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
    fun provideAlbumEntryDao(appDatabase: AppDatabase) = appDatabase.albumEntryDao()

    @Provides
    fun provideArtistEntryDao(appDatabase: AppDatabase) = appDatabase.artistEntryDao()

    @Provides
    fun provideVgmdbApi(
        albumEntryDao: AlbumEntryDao,
        artistEntryDao: ArtistEntryDao,
        vgmdbJson: VgmdbJson
    ) = VgmdbApi(albumEntryDao, artistEntryDao, vgmdbJson)

    @Provides
    fun provideAlbumRepository(
        application: CustomApplication,
        albumEntryDao: AlbumEntryDao,
        vgmdbApi: VgmdbApi
    ) = AlbumRepository(application, albumEntryDao, vgmdbApi)

    @Provides
    fun provideArtistRepository(
        application: CustomApplication,
        artistEntryDao: ArtistEntryDao,
        vgmdbApi: VgmdbApi
    ) = ArtistRepository(application, artistEntryDao, vgmdbApi)
}