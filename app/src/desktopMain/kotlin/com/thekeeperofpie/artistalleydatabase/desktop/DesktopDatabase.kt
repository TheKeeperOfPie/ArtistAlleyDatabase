package com.thekeeperofpie.artistalleydatabase.desktop

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.thekeeperofpie.artistalleydatabase.anilist.AniListDatabase
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntry
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntryFts
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntry
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDatabase
import com.thekeeperofpie.artistalleydatabase.anime.history.AnimeMediaHistoryEntry
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.AnimeMediaIgnoreEntry
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDatabase
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryFts
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntry
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDatabase
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryFts
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtist
import com.thekeeperofpie.artistalleydatabase.utils_room.Converters
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
abstract class DesktopDatabase : RoomDatabase(), AniListDatabase, AnimeDatabase, ArtEntryDatabase,
    CdEntryDatabase, VgmdbDatabase
