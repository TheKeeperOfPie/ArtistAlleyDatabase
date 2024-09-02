package com.thekeeperofpie.artistalleydatabase

import android.app.Application
import androidx.security.crypto.MasterKey
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.thekeeperofpie.artistalleydatabase.anilist.AniListAutocompleter
import com.thekeeperofpie.artistalleydatabase.anilist.AniListComponent
import com.thekeeperofpie.artistalleydatabase.anilist.AniListDataConverter
import com.thekeeperofpie.artistalleydatabase.anilist.AniListDatabase
import com.thekeeperofpie.artistalleydatabase.anilist.AniListSettings
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntryDao
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntryDao
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListOAuthStore
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.PlatformOAuthStore
import com.thekeeperofpie.artistalleydatabase.data.DataConverter
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistComponent
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistDao
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistDatabase
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkClient
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkSettings
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
    @get:Provides val musicalArtistDatabase: MusicalArtistDatabase,
    @get:Provides val masterKey: MasterKey,
    @get:Provides val aniListSettings: AniListSettings,
    @get:Provides val aniListDatabase: AniListDatabase,
    @get:Provides val networkSettings: NetworkSettings,
    @get:Provides val httpInterceptors: Set<HttpInterceptor>,
    @get:Provides val featureOverrideProvider: FeatureOverrideProvider,
    @get:Provides val aniListOAuthStore: AniListOAuthStore,
    @get:Provides val platformOAuthStore: PlatformOAuthStore,
) : AniListComponent, MusicalArtistComponent, VgmdbComponent {

    abstract val artistRepository: ArtistRepository
    abstract val albumRepository: AlbumRepository
    abstract val albumEntryDao: AlbumEntryDao
    abstract val vgmdbDataConverter: VgmdbDataConverter
    abstract val vgmdbArtistDao: VgmdbArtistDao
    abstract val vgmdbApi: VgmdbApi
    abstract val vgmdbAutocompleter: VgmdbAutocompleter
    abstract val musicalArtistDao: MusicalArtistDao
    abstract val dataConverter: DataConverter
    abstract val characterRepository: CharacterRepository
    abstract val mediaRepository: MediaRepository
    abstract val characterEntryDao: CharacterEntryDao
    abstract val mediaEntryDao: MediaEntryDao
    abstract val aniListDataConverter: AniListDataConverter
    abstract val authedAniListApi: AuthedAniListApi
    abstract val aniListAutocompleter: AniListAutocompleter

    @Provides
    @SingletonScope
    fun provideWebScraper() = networkClient.webScraper
}
