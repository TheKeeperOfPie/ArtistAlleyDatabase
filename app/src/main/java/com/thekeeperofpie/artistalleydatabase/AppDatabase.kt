@file:Suppress("OPT_IN_USAGE")

package com.thekeeperofpie.artistalleydatabase

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntry
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntryDao
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryFts
import com.thekeeperofpie.artistalleydatabase.utils.Converters

@Database(
    entities = [ArtEntry::class, ArtEntryFts::class, MediaEntry::class],
    exportSchema = false,
    version = 1
)
@TypeConverters(
    value = [
        Converters.DateConverter::class,
        Converters.StringListConverter::class,
        Converters.BigDecimalConverter::class
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun artEntryDao(): ArtEntryDao
    abstract fun mediaEntryDao(): MediaEntryDao
}
