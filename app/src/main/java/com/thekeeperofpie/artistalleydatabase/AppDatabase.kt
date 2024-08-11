@file:Suppress("ClassName")

package com.thekeeperofpie.artistalleydatabase

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
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
    version = 7,
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
        Converters.BigDecimalConverter::class,
        Converters.InstantConverter::class,
        Converters.IntListConverter::class,
        Converters.LocalDateConverter::class,
        Converters.StringListConverter::class,
        Converters.StringMapConverter::class,
    ]
)
abstract class AppDatabase : RoomDatabase(), AniListDatabase, ArtEntryDatabase, CdEntryDatabase,
    MusicalArtistDatabase, VgmdbDatabase, AnimeDatabase

object Migration_6_7 : Migration(6, 7){

    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `_new_art_entries` (`id` TEXT NOT NULL, `artists` TEXT NOT NULL, `sourceType` TEXT, `sourceValue` TEXT, `seriesSerialized` TEXT NOT NULL, `seriesSearchable` TEXT NOT NULL, `charactersSerialized` TEXT NOT NULL, `charactersSearchable` TEXT NOT NULL, `tags` TEXT NOT NULL, `price` TEXT, `lastEditTime` INTEGER, `imageWidth` INTEGER, `imageHeight` INTEGER, `printWidth` INTEGER, `printHeight` INTEGER, `notes` TEXT, `artistsLocked` INTEGER, `sourceLocked` INTEGER, `seriesLocked` INTEGER, `charactersLocked` INTEGER, `tagsLocked` INTEGER, `notesLocked` INTEGER, `printSizeLocked` INTEGER, PRIMARY KEY(`id`))")
        connection.execSQL("INSERT INTO `_new_art_entries` (`id`,`artists`,`sourceType`,`sourceValue`,`seriesSerialized`,`seriesSearchable`,`charactersSerialized`,`charactersSearchable`,`tags`,`price`,`lastEditTime`,`imageWidth`,`imageHeight`,`printWidth`,`printHeight`,`notes`,`artistsLocked`,`sourceLocked`,`seriesLocked`,`charactersLocked`,`tagsLocked`,`notesLocked`,`printSizeLocked`) SELECT `id`,`artists`,`sourceType`,`sourceValue`,`seriesSerialized`,`seriesSearchable`,`charactersSerialized`,`charactersSearchable`,`tags`,`price`,`lastEditTime`,`imageWidth`,`imageHeight`,`printWidth`,`printHeight`,`notes`,`artistsLocked`,`sourceLocked`,`seriesLocked`,`charactersLocked`,`tagsLocked`,`notesLocked`,`printSizeLocked` FROM `art_entries`")
        connection.execSQL("DROP TABLE `art_entries`")
        connection.execSQL("ALTER TABLE `_new_art_entries` RENAME TO `art_entries`")

        connection.execSQL("CREATE TABLE IF NOT EXISTS `_new_cd_entries` (`id` TEXT NOT NULL, `catalogId` TEXT, `titles` TEXT NOT NULL, `performers` TEXT NOT NULL, `performersSearchable` TEXT NOT NULL, `composers` TEXT NOT NULL, `composersSearchable` TEXT NOT NULL, `seriesSerialized` TEXT NOT NULL, `seriesSearchable` TEXT NOT NULL, `charactersSerialized` TEXT NOT NULL, `charactersSearchable` TEXT NOT NULL, `discs` TEXT NOT NULL, `tags` TEXT NOT NULL, `price` TEXT, `lastEditTime` INTEGER, `imageWidth` INTEGER, `imageHeight` INTEGER, `notes` TEXT, `catalogIdLocked` INTEGER, `titlesLocked` INTEGER, `performersLocked` INTEGER, `composersLocked` INTEGER, `seriesLocked` INTEGER, `charactersLocked` INTEGER, `discsLocked` INTEGER, `tagsLocked` INTEGER, `priceLocked` INTEGER, `notesLocked` INTEGER, PRIMARY KEY(`id`))")
        connection.execSQL("INSERT INTO `_new_cd_entries` (`id`,`catalogId`,`titles`,`performers`,`performersSearchable`,`composers`,`composersSearchable`,`seriesSerialized`,`seriesSearchable`,`charactersSerialized`,`charactersSearchable`,`discs`,`tags`,`price`,`lastEditTime`,`imageWidth`,`imageHeight`,`notes`,`catalogIdLocked`,`titlesLocked`,`performersLocked`,`composersLocked`,`seriesLocked`,`charactersLocked`,`discsLocked`,`tagsLocked`,`priceLocked`,`notesLocked`) SELECT `id`,`catalogId`,`titles`,`performers`,`performersSearchable`,`composers`,`composersSearchable`,`seriesSerialized`,`seriesSearchable`,`charactersSerialized`,`charactersSearchable`,`discs`,`tags`,`price`,`lastEditTime`,`imageWidth`,`imageHeight`,`notes`,`catalogIdLocked`,`titlesLocked`,`performersLocked`,`composersLocked`,`seriesLocked`,`charactersLocked`,`discsLocked`,`tagsLocked`,`priceLocked`,`notesLocked` FROM `cd_entries`")
        connection.execSQL("DROP TABLE `cd_entries`")
        connection.execSQL("ALTER TABLE `_new_cd_entries` RENAME TO `cd_entries`")

        connection.execSQL("DROP TABLE `art_entries_fts`")
        connection.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS `art_entries_fts` USING FTS4(`id` TEXT NOT NULL, `artists` TEXT NOT NULL, `sourceType` TEXT, `sourceValue` TEXT, `seriesSerialized` TEXT NOT NULL, `seriesSearchable` TEXT NOT NULL, `charactersSerialized` TEXT NOT NULL, `charactersSearchable` TEXT NOT NULL, `tags` TEXT NOT NULL, `price` TEXT, `lastEditTime` INTEGER, `imageWidth` INTEGER, `imageHeight` INTEGER, `printWidth` INTEGER, `printHeight` INTEGER, `notes` TEXT, `artistsLocked` INTEGER, `sourceLocked` INTEGER, `seriesLocked` INTEGER, `charactersLocked` INTEGER, `tagsLocked` INTEGER, `notesLocked` INTEGER, `printSizeLocked` INTEGER, content=`art_entries`)")
        connection.execSQL("INSERT INTO `art_entries_fts` (`id`,`artists`,`sourceType`,`sourceValue`,`seriesSerialized`,`seriesSearchable`,`charactersSerialized`,`charactersSearchable`,`tags`,`price`,`lastEditTime`,`imageWidth`,`imageHeight`,`printWidth`,`printHeight`,`notes`,`artistsLocked`,`sourceLocked`,`seriesLocked`,`charactersLocked`,`tagsLocked`,`notesLocked`,`printSizeLocked`,`docid`) SELECT `id`,`artists`,`sourceType`,`sourceValue`,`seriesSerialized`,`seriesSearchable`,`charactersSerialized`,`charactersSearchable`,`tags`,`price`,`lastEditTime`,`imageWidth`,`imageHeight`,`printWidth`,`printHeight`,`notes`,`artistsLocked`,`sourceLocked`,`seriesLocked`,`charactersLocked`,`tagsLocked`,`notesLocked`,`printSizeLocked`,`rowid` FROM `art_entries`")

        connection.execSQL("DROP TABLE `cd_entries_fts`")
        connection.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS `cd_entries_fts` USING FTS4(`id` TEXT NOT NULL, `catalogId` TEXT, `titles` TEXT NOT NULL, `performers` TEXT NOT NULL, `performersSearchable` TEXT NOT NULL, `composers` TEXT NOT NULL, `composersSearchable` TEXT NOT NULL, `seriesSerialized` TEXT NOT NULL, `seriesSearchable` TEXT NOT NULL, `charactersSerialized` TEXT NOT NULL, `charactersSearchable` TEXT NOT NULL, `discs` TEXT NOT NULL, `tags` TEXT NOT NULL, `price` TEXT, `lastEditTime` INTEGER, `imageWidth` INTEGER, `imageHeight` INTEGER, `notes` TEXT, `catalogIdLocked` INTEGER, `titlesLocked` INTEGER, `performersLocked` INTEGER, `composersLocked` INTEGER, `seriesLocked` INTEGER, `charactersLocked` INTEGER, `discsLocked` INTEGER, `tagsLocked` INTEGER, `priceLocked` INTEGER, `notesLocked` INTEGER, content=`cd_entries`)")
        connection.execSQL("INSERT INTO `cd_entries_fts` (`id`,`catalogId`,`titles`,`performers`,`performersSearchable`,`composers`,`composersSearchable`,`seriesSerialized`,`seriesSearchable`,`charactersSerialized`,`charactersSearchable`,`discs`,`tags`,`price`,`lastEditTime`,`imageWidth`,`imageHeight`,`notes`,`catalogIdLocked`,`titlesLocked`,`performersLocked`,`composersLocked`,`seriesLocked`,`charactersLocked`,`discsLocked`,`tagsLocked`,`priceLocked`,`notesLocked`,`docid`) SELECT `id`,`catalogId`,`titles`,`performers`,`performersSearchable`,`composers`,`composersSearchable`,`seriesSerialized`,`seriesSearchable`,`charactersSerialized`,`charactersSearchable`,`discs`,`tags`,`price`,`lastEditTime`,`imageWidth`,`imageHeight`,`notes`,`catalogIdLocked`,`titlesLocked`,`performersLocked`,`composersLocked`,`seriesLocked`,`charactersLocked`,`discsLocked`,`tagsLocked`,`priceLocked`,`notesLocked`,`rowid` FROM `cd_entries`")
    }
}
