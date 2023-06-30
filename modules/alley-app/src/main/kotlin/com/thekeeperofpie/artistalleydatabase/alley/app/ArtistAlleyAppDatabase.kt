package com.thekeeperofpie.artistalleydatabase.alley.app

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteTable
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyDatabase
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntryFts
import com.thekeeperofpie.artistalleydatabase.android_utils.Converters

@Database(
    entities = [
        ArtistEntry::class,
        ArtistEntryFts::class,
    ],
    exportSchema = true,
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
    ]
)
@TypeConverters(
    value = [
        Converters.StringListConverter::class,
    ]
)
abstract class ArtistAlleyAppDatabase : RoomDatabase(), ArtistAlleyDatabase
