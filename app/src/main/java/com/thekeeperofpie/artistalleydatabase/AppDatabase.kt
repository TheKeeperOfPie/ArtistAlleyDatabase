package com.thekeeperofpie.artistalleydatabase

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.thekeeperofpie.artistalleydatabase.android_utils.Converters
import com.thekeeperofpie.artistalleydatabase.anilist.AniListDatabase
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntry
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntryFts
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntry
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDatabase
import com.thekeeperofpie.artistalleydatabase.anime.history.AnimeMediaHistoryEntry
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreEntry
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDatabase
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryFts
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntry
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDatabase
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryFts
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtist
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistDatabase
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDatabase
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.VgmdbArtist

@Database(
    entities = [
        ArtEntry::class,
        ArtEntryFts::class,
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
    exportSchema = true,
    version = 6,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
    ]
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
abstract class AppDatabase : RoomDatabase(), AniListDatabase, ArtEntryDatabase, CdEntryDatabase,
    MusicalArtistDatabase, VgmdbDatabase, AnimeDatabase
