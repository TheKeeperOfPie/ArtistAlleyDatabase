package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry

actual data class MerchWithArtistsEntry(
    @Embedded actual val merch: MerchEntry,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            ArtistMerchConnection::class,
            parentColumn = "merchId",
            entityColumn = "artistId",
        )
    )
    actual val artists: List<ArtistEntry>,
)
