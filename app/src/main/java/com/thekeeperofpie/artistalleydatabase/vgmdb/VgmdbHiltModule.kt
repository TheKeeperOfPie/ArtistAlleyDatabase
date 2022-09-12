package com.thekeeperofpie.artistalleydatabase.vgmdb

import com.thekeeperofpie.artistalleydatabase.AppDatabase
import com.thekeeperofpie.artistalleydatabase.CustomApplication
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntryDao
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class VgmdbHiltModule {

    @Provides
    fun provideVgmdbApi(albumEntryDao: AlbumEntryDao) = VgmdbApi(albumEntryDao)

    @Provides
    fun provideAlbumEntryDao(appDatabase: AppDatabase) = appDatabase.albumEntryDao()

    @Provides
    fun provideAlbumRepository(
        application: CustomApplication,
        albumEntryDao: AlbumEntryDao,
        vgmdbApi: VgmdbApi
    ) = AlbumRepository(application, albumEntryDao, vgmdbApi)
}