package com.thekeeperofpie.artistalleydatabase.alley.app

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
    version = 1,
)
@TypeConverters(
    value = [
        Converters.StringListConverter::class,
    ]
)
abstract class ArtistAlleyAppDatabase : RoomDatabase(), ArtistAlleyDatabase
