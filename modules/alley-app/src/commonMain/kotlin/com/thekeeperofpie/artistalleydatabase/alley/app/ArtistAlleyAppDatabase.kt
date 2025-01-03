package com.thekeeperofpie.artistalleydatabase.alley.app

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyDatabase
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryFts
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyArtistConnection
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryFts
import com.thekeeperofpie.artistalleydatabase.alley.tags.ArtistMerchConnection
import com.thekeeperofpie.artistalleydatabase.alley.tags.ArtistSeriesConnection
import com.thekeeperofpie.artistalleydatabase.alley.tags.MerchEntry
import com.thekeeperofpie.artistalleydatabase.alley.tags.MerchEntryFts
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesEntryFts
import com.thekeeperofpie.artistalleydatabase.utils_room.Converters

@Database(
    entities = [
        ArtistEntry::class,
        ArtistEntryFts::class,
        ArtistSeriesConnection::class,
        ArtistMerchConnection::class,
        StampRallyEntry::class,
        StampRallyEntryFts::class,
        StampRallyArtistConnection::class,
        SeriesEntry::class,
        SeriesEntryFts::class,
        MerchEntry::class,
        MerchEntryFts::class,
    ],
    exportSchema = true,
    version = 8,
    autoMigrations = [
        AutoMigration(1, 2),
        AutoMigration(2, 3),
        AutoMigration(3, 4),
        AutoMigration(4, 5),
        AutoMigration(5, 6),
    ]
)
@TypeConverters(
    value = [
        Converters.StringListConverter::class,
    ]
)
abstract class ArtistAlleyAppDatabase : RoomDatabase(), ArtistAlleyDatabase {

    object Version_6_7 : Migration(6, 7) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL("""ALTER TABLE `stamp_rally_entries` DROP COLUMN `minimumPerTable`""")
            connection.execSQL("ALTER TABLE `stamp_rally_entries` ADD COLUMN `prizeLimit` INTEGER DEFAULT NULL")
            connection.execSQL("ALTER TABLE `stamp_rally_entries` ADD COLUMN `tableMin` INTEGER DEFAULT NULL")
            connection.execSQL("DROP TABLE stamp_rally_entries_fts")
            connection.execSQL(
                """
                CREATE VIRTUAL TABLE IF NOT EXISTS `stamp_rally_entries_fts` USING FTS4(`id` TEXT NOT NULL, `fandom` TEXT NOT NULL COLLATE NOCASE, `hostTable` TEXT NOT NULL COLLATE NOCASE, `tables` TEXT NOT NULL, `links` TEXT NOT NULL, `tableMin` INTEGER, `prizeLimit` INTEGER, `favorite` INTEGER NOT NULL, `ignored` INTEGER NOT NULL, `notes` TEXT, content=`stamp_rally_entries`)
                """.trimIndent()
            )
        }
    }

    object Version_7_8 : Migration(7, 8) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL("ALTER TABLE `stamp_rally_entries` ADD COLUMN `totalCost` INTEGER DEFAULT NULL")
            connection.execSQL("DROP TABLE stamp_rally_entries_fts")
            connection.execSQL(
                """
                CREATE VIRTUAL TABLE IF NOT EXISTS `stamp_rally_entries_fts` USING FTS4(`id` TEXT NOT NULL, `fandom` TEXT NOT NULL COLLATE NOCASE, `hostTable` TEXT NOT NULL COLLATE NOCASE, `tables` TEXT NOT NULL, `links` TEXT NOT NULL, `tableMin` INTEGER, `totalCost` INTEGER, `prizeLimit` INTEGER, `favorite` INTEGER NOT NULL, `ignored` INTEGER NOT NULL, `notes` TEXT, content=`stamp_rally_entries`)
                """.trimIndent()
            )
        }
    }
}
