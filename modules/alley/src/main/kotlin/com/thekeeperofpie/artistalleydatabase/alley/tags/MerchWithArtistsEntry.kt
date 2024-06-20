package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry

data class MerchWithArtistsEntry(
    @Embedded val merch: MerchEntry,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            ArtistMerchConnection::class,
            parentColumn = "merchId",
            entityColumn = "artistId",
        )
    )
    val artists: List<ArtistEntry>,
)
