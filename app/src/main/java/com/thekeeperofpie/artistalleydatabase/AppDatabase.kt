@file:Suppress("OPT_IN_USAGE")

package com.thekeeperofpie.artistalleydatabase

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.thekeeperofpie.artistalleydatabase.android_utils.Converters
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntry
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntryDao
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntry
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntryDao
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryFts
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.browse.ArtEntryBrowseDao
import com.thekeeperofpie.artistalleydatabase.cds.CdEntry
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryFts
import com.thekeeperofpie.artistalleydatabase.edit.ArtEntryEditDao
import com.thekeeperofpie.artistalleydatabase.search.advanced.ArtEntryAdvancedSearchDao

@Database(
    entities = [
        ArtEntry::class,
        ArtEntryFts::class,
        CdEntry::class,
        CdEntryFts::class,
        MediaEntry::class,
        CharacterEntry::class
    ],
    exportSchema = false,
    version = 1
)
@TypeConverters(
    value = [
        Converters.DateConverter::class,
        Converters.StringListConverter::class,
        Converters.IntListConverter::class,
        Converters.BigDecimalConverter::class
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun artEntryDao(): ArtEntryDao
    abstract fun artEntryEditDao(): ArtEntryEditDao
    abstract fun artEntryDetailsDao(): ArtEntryDetailsDao
    abstract fun artEntryBrowseDao(): ArtEntryBrowseDao
    abstract fun artEntryAdvancedSearchDao(): ArtEntryAdvancedSearchDao
    abstract fun cdEntryDao(): CdEntryDao
    abstract fun cdEntryDetailsDao(): CdEntryDetailsDao
    abstract fun mediaEntryDao(): MediaEntryDao
    abstract fun characterEntryDao(): CharacterEntryDao
}
