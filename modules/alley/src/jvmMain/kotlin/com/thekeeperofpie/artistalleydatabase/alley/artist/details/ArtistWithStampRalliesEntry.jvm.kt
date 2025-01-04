package com.thekeeperofpie.artistalleydatabase.alley.artist.details

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyArtistConnection
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntry

actual data class ArtistWithStampRalliesEntry(
    @Embedded actual val artist: ArtistEntry,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            StampRallyArtistConnection::class,
            parentColumn = "artistId",
            entityColumn = "stampRallyId",
        )
    )
    actual val stampRallies: List<StampRallyEntry>,
)
