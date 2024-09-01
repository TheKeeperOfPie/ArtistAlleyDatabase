package com.thekeeperofpie.artistalleydatabase

import android.app.Application
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkClient
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbApi
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbAutocompleter
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbComponent
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDataConverter
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDatabase
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntryDao
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumRepository
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistRepository
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.VgmdbArtistDao
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
@SingletonScope
abstract class ApplicationComponent(
    @get:Provides val application: Application,
    @get:Provides val networkClient: NetworkClient,
    @get:Provides val applicationScope: ApplicationScope,
    @get:Provides val httpClient: HttpClient,
    @get:Provides val vgmdbDatabase: VgmdbDatabase,
    @get:Provides val json: Json,
) : VgmdbComponent {

    abstract val artistRepository: ArtistRepository
    abstract val albumRepository: AlbumRepository
    abstract val albumEntryDao: AlbumEntryDao
    abstract val vgmdbDataConverter: VgmdbDataConverter
    abstract val vgmdbArtistDao: VgmdbArtistDao
    abstract val vgmdbApi: VgmdbApi
    abstract val vgmdbAutocompleter: VgmdbAutocompleter

    @Provides
    @SingletonScope
    fun provideWebScraper() = networkClient.webScraper
}
