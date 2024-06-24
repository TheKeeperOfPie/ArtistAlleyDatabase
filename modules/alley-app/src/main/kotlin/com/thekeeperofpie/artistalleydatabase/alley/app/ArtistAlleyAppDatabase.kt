@file:Suppress("ClassName")

package com.thekeeperofpie.artistalleydatabase.alley.app

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
import com.thekeeperofpie.artistalleydatabase.android_utils.Converters

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
    version = 6,
    autoMigrations = [
        AutoMigration(1, 2),
        AutoMigration(2, 3),
        AutoMigration(3, 4),
        AutoMigration(4, 5),
        AutoMigration(5, 6),
        AutoMigration(1, 6),
    ]
)
@TypeConverters(
    value = [
        Converters.StringListConverter::class,
    ]
)
abstract class ArtistAlleyAppDatabase : RoomDatabase(), ArtistAlleyDatabase
