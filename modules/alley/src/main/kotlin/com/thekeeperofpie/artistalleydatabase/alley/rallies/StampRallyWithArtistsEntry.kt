package com.thekeeperofpie.artistalleydatabase.alley.rallies

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry

data class StampRallyWithArtistsEntry(
    @Embedded val stampRally: StampRallyEntry,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            StampRallyArtistConnection::class,
            parentColumn = "stampRallyId",
            entityColumn = "artistId",
        )
    )
    val artists: List<ArtistEntry>,
)
