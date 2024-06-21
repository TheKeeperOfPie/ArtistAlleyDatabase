package com.thekeeperofpie.artistalleydatabase.alley.artist.details

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyArtistConnection
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntry

data class ArtistWithStampRalliesEntry(
    @Embedded val artist: ArtistEntry,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            StampRallyArtistConnection::class,
            parentColumn = "artistId",
            entityColumn = "stampRallyId",
        )
    )
    val stampRallies: List<StampRallyEntry>,
)
