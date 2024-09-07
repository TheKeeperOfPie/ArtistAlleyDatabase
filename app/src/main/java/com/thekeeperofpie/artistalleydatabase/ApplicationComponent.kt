package com.thekeeperofpie.artistalleydatabase

import android.app.Application
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
import com.thekeeperofpie.artistalleydatabase.anime.AnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDatabase
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.history.HistoryController
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaGenreDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaTagDialogController
import com.thekeeperofpie.artistalleydatabase.anime.notifications.NotificationsController
import com.thekeeperofpie.artistalleydatabase.anime2anime.Anime2AnimeComponent
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryComponent
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryNavigator
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDatabase
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.art.persistence.ArtSettings
import com.thekeeperofpie.artistalleydatabase.browse.BrowseComponent
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryComponent
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryNavigator
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDatabase
import com.thekeeperofpie.artistalleydatabase.data.DataConverter
import com.thekeeperofpie.artistalleydatabase.image.crop.CropSettings
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.markdown.Markdown
import com.thekeeperofpie.artistalleydatabase.media.MediaPlayer
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationController
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationOverrideProvider
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistComponent
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistDao
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistDatabase
import com.thekeeperofpie.artistalleydatabase.news.NewsSettings
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkClient
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkSettings
import com.thekeeperofpie.artistalleydatabase.utils_room.DatabaseSyncer
import com.thekeeperofpie.artistalleydatabase.utils_room.Exporter
import com.thekeeperofpie.artistalleydatabase.utils_room.Importer
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
    @get:Provides val aniListSettings: AniListSettings,
    @get:Provides val aniListDatabase: AniListDatabase,
    @get:Provides val networkSettings: NetworkSettings,
    @get:Provides val httpInterceptors: Set<HttpInterceptor>,
    @get:Provides val featureOverrideProvider: FeatureOverrideProvider,
    @get:Provides val aniListOAuthStore: AniListOAuthStore,
    @get:Provides val platformOAuthStore: PlatformOAuthStore,
    @get:Provides val appFileSystem: AppFileSystem,
    @get:Provides val cdEntryDatabase: CdEntryDatabase,
    @get:Provides val cropSettings: CropSettings,
    @get:Provides val artEntryDatabase: ArtEntryDatabase,
    @get:Provides val artSettings: ArtSettings,
    @get:Provides val animeDatabase: AnimeDatabase,
    @get:Provides val animeSettings: AnimeSettings,
    @get:Provides val navigationTypeMap: NavigationTypeMap,
    @get:Provides val newsSettings: NewsSettings,
    @get:Provides val monetizationSettings: MonetizationSettings,
    @get:Provides val mediaPlayer: MediaPlayer,
) : AniListComponent, AnimeComponent, Anime2AnimeComponent, ArtEntryComponent, BrowseComponent,
    CdEntryComponent, MusicalArtistComponent, VgmdbComponent {

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
    abstract val cdEntryNavigator: CdEntryNavigator
    abstract val cdEntryDao: CdEntryDao
    abstract val importers: Set<Importer>
    abstract val exporters: Set<Exporter>
    abstract val databaseSyncers: Set<DatabaseSyncer>
    abstract val artEntryNavigator: ArtEntryNavigator
    abstract val artEntryDetailsDao: ArtEntryDetailsDao
    abstract val historyController: HistoryController

    abstract val monetizationController: MonetizationController
    abstract val mediaTagDialogController: MediaTagDialogController
    abstract val mediaGenreDialogController: MediaGenreDialogController
    abstract val markdown: Markdown
    abstract val notificationsController: NotificationsController
    abstract val ignoreController: IgnoreController

    protected val AppMonetizationOverrideProvider.bind: MonetizationOverrideProvider
        @Provides get() = this

    @Provides
    @SingletonScope
    fun provideWebScraper() = networkClient.webScraper
}
