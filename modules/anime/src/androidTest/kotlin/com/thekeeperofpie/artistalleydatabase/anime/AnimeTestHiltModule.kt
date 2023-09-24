package com.thekeeperofpie.artistalleydatabase.anime

import android.app.Application
import android.net.Uri
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.Converters
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.anilist.AniListDatabase
import com.thekeeperofpie.artistalleydatabase.anilist.AniListJson
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.AniListSettings
import com.thekeeperofpie.artistalleydatabase.anilist.VoiceActorLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntry
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntryFts
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntry
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.history.AnimeMediaHistoryEntry
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.FilterData
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsNetworkCategory
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsNetworkRegion
import com.thekeeperofpie.artistalleydatabase.anime.news.CrunchyrollNewsCategory
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntry
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDatabase
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryFts
import com.thekeeperofpie.artistalleydatabase.entry.EntrySettings
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationOverrideProvider
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtist
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistDatabase
import com.thekeeperofpie.artistalleydatabase.network_utils.NetworkSettings
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDatabase
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbJson
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.VgmdbArtist
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Singleton

// TODO: Separate these into specific modules
@Module
@InstallIn(SingletonComponent::class)
class AnimeTestHiltModule {

    @Singleton
    @Provides
    fun provideNetworkSettings(): NetworkSettings {
        return object : NetworkSettings {
            override val networkLoggingLevel =
                MutableStateFlow(NetworkSettings.NetworkLoggingLevel.NONE)
            override val enableNetworkCaching = MutableStateFlow(false)
        }
    }

    @Singleton
    @Provides
    fun provideAniListSettings(): AniListSettings {
        return object : AniListSettings {
            override val aniListViewer = MutableStateFlow<AniListViewer?>(null)
        }
    }

    @Singleton
    @Provides
    fun provideAnimeSettings(): AnimeSettings {
        return object : AnimeSettings {
            override val savedAnimeFilters = MutableStateFlow(emptyMap<String, FilterData>())
            override val showAdult = MutableStateFlow(false)
            override val collapseAnimeFiltersOnClose = MutableStateFlow(false)
            override val showLessImportantTags = MutableStateFlow(false)
            override val showSpoilerTags = MutableStateFlow(false)
            override val animeNewsNetworkRegion =
                MutableStateFlow(AnimeNewsNetworkRegion.USA_CANADA)
            override val animeNewsNetworkCategoriesIncluded =
                MutableStateFlow(emptyList<AnimeNewsNetworkCategory>())
            override val animeNewsNetworkCategoriesExcluded =
                MutableStateFlow(emptyList<AnimeNewsNetworkCategory>())
            override val crunchyrollNewsCategoriesIncluded =
                MutableStateFlow(emptyList<CrunchyrollNewsCategory>())
            override val crunchyrollNewsCategoriesExcluded =
                MutableStateFlow(emptyList<CrunchyrollNewsCategory>())
            override val preferredMediaType = MutableStateFlow(MediaType.ANIME)
            override val mediaViewOption = MutableStateFlow(MediaViewOption.SMALL_CARD)
            override val rootNavDestination = MutableStateFlow(AnimeRootNavDestination.HOME)
            override val mediaHistoryEnabled = MutableStateFlow(false)
            override val mediaHistoryMaxEntries = MutableStateFlow(100)
            override val mediaIgnoreEnabled = MutableStateFlow(false)
            override val mediaIgnoreHide = MutableStateFlow(false)
            override val showIgnored = mediaIgnoreHide.map(Boolean::not)
            override val languageOptionMedia = MutableStateFlow(AniListLanguageOption.DEFAULT)
            override val languageOptionCharacters = MutableStateFlow(AniListLanguageOption.DEFAULT)
            override val languageOptionStaff = MutableStateFlow(AniListLanguageOption.DEFAULT)
            override val languageOptionVoiceActor =
                MutableStateFlow(VoiceActorLanguageOption.JAPANESE)
            override val showFallbackVoiceActor = MutableStateFlow(false)
            override val currentMediaListSizeAnime = MutableStateFlow(0)
            override val currentMediaListSizeManga = MutableStateFlow(0)
            override val lastCrash = MutableStateFlow("")
            override val lastCrashShown = MutableStateFlow(false)
        }
    }

    @Singleton
    @Provides
    fun provideMonetizationSettings(): MonetizationSettings {
        return object : MonetizationSettings {
            override val adsEnabled = MutableStateFlow(false)
            override val subscribed = MutableStateFlow(false)
            override val unlockAllFeatures = MutableStateFlow(false)
        }
    }

    @Singleton
    @Provides
    fun provideEntrySettings(): EntrySettings {
        return object : EntrySettings {
            override val cropDocumentUri = MutableStateFlow<Uri?>(null)
        }
    }

    @Singleton
    @Provides
    fun provideFeatureOverrideProvider(): FeatureOverrideProvider {
        return object : FeatureOverrideProvider {
            override val isReleaseBuild = false
        }
    }

    @Singleton
    @Provides
    fun provideMonetizationOverrideProvider(): MonetizationOverrideProvider {
        return object : MonetizationOverrideProvider {
            override val overrideUnlock = MutableStateFlow(false)
        }
    }

    @Singleton
    @Provides
    fun provideAniListJson(appJson: AppJson) = AniListJson(appJson.json)

    @Singleton
    @Provides
    fun provideVgmdbJson(appJson: AppJson) = VgmdbJson(appJson.json)

    @Singleton
    @Provides
    fun provideTestDatabase(application: Application) =
        Room.inMemoryDatabaseBuilder(application, TestDatabase::class.java).build()

    @Singleton
    @Provides
    fun provideAniListDatabase(testDatabase: TestDatabase) = testDatabase as AniListDatabase

    @Singleton
    @Provides
    fun provideAnimeDatabase(testDatabase: TestDatabase) = testDatabase as AnimeDatabase

    @Singleton
    @Provides
    fun provideCdEntryDatabase(testDatabase: TestDatabase) = testDatabase as CdEntryDatabase

    @Singleton
    @Provides
    fun provideMusicalArtistDatabase(testDatabase: TestDatabase) =
        testDatabase as MusicalArtistDatabase

    @Singleton
    @Provides
    fun provideVgmdbDatabase(testDatabase: TestDatabase) = testDatabase as VgmdbDatabase

    @Database(
        entities = [
            CdEntry::class,
            CdEntryFts::class,
            MediaEntry::class,
            CharacterEntry::class,
            CharacterEntryFts::class,
            AlbumEntry::class,
            VgmdbArtist::class,
            MusicalArtist::class,
            AnimeMediaHistoryEntry::class,
            AnimeMediaIgnoreEntry::class,
        ],
        exportSchema = false,
        version = 1,
    )
    @TypeConverters(
        value = [
            Converters.DateConverter::class,
            Converters.StringListConverter::class,
            Converters.IntListConverter::class,
            Converters.BigDecimalConverter::class,
            Converters.StringMapConverter::class,
        ]
    )
    abstract class TestDatabase : RoomDatabase(), AniListDatabase, AnimeDatabase, CdEntryDatabase,
        MusicalArtistDatabase, VgmdbDatabase
}
