package com.thekeeperofpie.artistalleydatabase.vgmdb

import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils_network.WebScraper
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntryDao
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumRepository
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistRepository
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.VgmdbArtistDao
import io.ktor.client.HttpClient
import me.tatarka.inject.annotations.Provides

interface VgmdbComponent {

    @SingletonScope
    @Provides
    fun provideAlbumEntryDao(database: VgmdbDatabase) = database.albumEntryDao()

    @SingletonScope
    @Provides
    fun provideArtistEntryDao(database: VgmdbDatabase) = database.artistEntryDao()

    @SingletonScope
    @Provides
    fun provideVgmdbApi(
        albumEntryDao: AlbumEntryDao,
        vgmdbArtistDao: VgmdbArtistDao,
        vgmdbJson: VgmdbJson,
        httpClient: HttpClient,
        webScraper: WebScraper,
    ) = VgmdbApi(albumEntryDao, vgmdbArtistDao, vgmdbJson, httpClient, webScraper)

    @SingletonScope
    @Provides
    fun provideVgmdbDataConverter(vgmdbJson: VgmdbJson) = VgmdbDataConverter(vgmdbJson)

    @SingletonScope
    @Provides
    fun provideVgmdbAutocompleter(
        vgmdbApi: VgmdbApi,
        vgmdbJson: VgmdbJson,
        vgmdbDataConverter: VgmdbDataConverter,
        artistRepository: ArtistRepository,
    ) = VgmdbAutocompleter(vgmdbApi, vgmdbJson, vgmdbDataConverter, artistRepository)

    @SingletonScope
    @Provides
    fun provideAlbumRepository(
        scope: ApplicationScope,
        albumEntryDao: AlbumEntryDao,
        vgmdbApi: VgmdbApi
    ) = AlbumRepository(scope, albumEntryDao, vgmdbApi)

    @SingletonScope
    @Provides
    fun provideArtistRepository(
        scope: ApplicationScope,
        vgmdbArtistDao: VgmdbArtistDao,
        vgmdbApi: VgmdbApi
    ) = ArtistRepository(scope, vgmdbArtistDao, vgmdbApi)
}
