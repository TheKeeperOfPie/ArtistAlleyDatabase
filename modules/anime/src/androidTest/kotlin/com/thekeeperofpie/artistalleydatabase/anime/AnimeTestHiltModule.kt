package com.thekeeperofpie.artistalleydatabase.anime

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.thekeeperofpie.artistalleydatabase.android_utils.Converters
import com.thekeeperofpie.artistalleydatabase.anilist.AniListDatabase
import com.thekeeperofpie.artistalleydatabase.anilist.AniListJson
import com.thekeeperofpie.artistalleydatabase.anilist.AniListSettings
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntry
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntryFts
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntry
import com.thekeeperofpie.artistalleydatabase.anime.history.AnimeMediaHistoryEntry
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreEntry
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntry
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDatabase
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryFts
import com.thekeeperofpie.artistalleydatabase.entry.EntrySettings
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationOverrideProvider
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationSettings
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtist
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistDatabase
import com.thekeeperofpie.artistalleydatabase.news.NewsSettings
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.AppJson
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkSettings
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDatabase
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbJson
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.VgmdbArtist
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// TODO: Separate these into specific modules
@Module
@InstallIn(SingletonComponent::class)
abstract class AnimeTestHiltModule {

    companion object {
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
        fun provideTestSettingsProvider() = TestSettingsProvider()

        @Singleton
        @Provides
        fun provideTestOverrideProvider() = TestOverrideProvider()
    }

    @Singleton
    @Binds
    abstract fun provideNetworkSettings(testSettingsProvider: TestSettingsProvider): NetworkSettings

    @Singleton
    @Binds
    abstract fun provideAniListSettings(testSettingsProvider: TestSettingsProvider): AniListSettings

    @Singleton
    @Binds
    abstract fun provideAnimeSettings(testSettingsProvider: TestSettingsProvider): AnimeSettings

    @Singleton
    @Binds
    abstract fun provideMonetizationSettings(testSettingsProvider: TestSettingsProvider): MonetizationSettings

    @Singleton
    @Binds
    abstract fun provideNewsSettings(testSettingsProvider: TestSettingsProvider): NewsSettings

    @Singleton
    @Binds
    abstract fun provideEntrySettings(testSettings: TestSettingsProvider): EntrySettings

    @Singleton
    @Binds
    abstract fun provideFeatureOverrideProvider(testOverrideProvider: TestOverrideProvider): FeatureOverrideProvider

    @Singleton
    @Binds
    abstract fun provideMonetizationOverrideProvider(testOverrideProvider: TestOverrideProvider): MonetizationOverrideProvider

    @Singleton
    @Binds
    abstract fun provideAniListDatabase(testDatabase: TestDatabase): AniListDatabase

    @Singleton
    @Binds
    abstract fun provideAnimeDatabase(testDatabase: TestDatabase): AnimeDatabase

    @Singleton
    @Binds
    abstract fun provideCdEntryDatabase(testDatabase: TestDatabase): CdEntryDatabase

    @Singleton
    @Binds
    abstract fun provideMusicalArtistDatabase(testDatabase: TestDatabase): MusicalArtistDatabase

    @Singleton
    @Binds
    abstract fun provideVgmdbDatabase(testDatabase: TestDatabase): VgmdbDatabase

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
            Converters.BigDecimalConverter::class,
            Converters.InstantConverter::class,
            Converters.IntListConverter::class,
            Converters.LocalDateConverter::class,
            Converters.StringListConverter::class,
            Converters.StringMapConverter::class,
        ]
    )
    abstract class TestDatabase : RoomDatabase(), AniListDatabase, AnimeDatabase, CdEntryDatabase,
        MusicalArtistDatabase, VgmdbDatabase
}
