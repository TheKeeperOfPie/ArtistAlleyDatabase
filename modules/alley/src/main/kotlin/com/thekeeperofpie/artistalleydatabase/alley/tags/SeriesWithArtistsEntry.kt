package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry

data class SeriesWithArtistsEntry(
    @Embedded val series: SeriesEntry,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            ArtistMerchConnection::class,
            parentColumn = "seriesId",
            entityColumn = "artistId",
        )
    )
    val artists: List<ArtistEntry>,
)
