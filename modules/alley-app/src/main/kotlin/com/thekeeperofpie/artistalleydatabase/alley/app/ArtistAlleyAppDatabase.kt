package com.thekeeperofpie.artistalleydatabase.alley.app

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyDatabase
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryFts
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyArtistConnection
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryFts
import com.thekeeperofpie.artistalleydatabase.android_utils.Converters

@Database(
    entities = [
        ArtistEntry::class,
        ArtistEntryFts::class,
        StampRallyEntry::class,
        StampRallyEntryFts::class,
        StampRallyArtistConnection::class,
    ],
    exportSchema = true,
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = Version1to2::class),
    ]
)
@TypeConverters(
    value = [
        Converters.StringListConverter::class,
    ]
)
abstract class ArtistAlleyAppDatabase : RoomDatabase(), ArtistAlleyDatabase

@DeleteColumn.Entries(
    DeleteColumn(
        tableName = "artist_entries",
        columnName = "store"
    ),
    DeleteColumn(
        tableName = "artist_entries_fts",
        columnName = "store"
    ),
    DeleteColumn(
        tableName = "artist_entries",
        columnName = "catalog"
    ),
    DeleteColumn(
        tableName = "artist_entries_fts",
        columnName = "catalog"
    ),
)
class Version1to2 : AutoMigrationSpec
