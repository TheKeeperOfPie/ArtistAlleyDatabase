package com.thekeeperofpie.artistalleydatabase.vgmdb.artist

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity("artist_entries")
data class ArtistEntry(
    @PrimaryKey
    val id: String,
    val names: Map<String, String> = emptyMap(),
    val coverArt: String? = null,
) {
    val coverMedium get() = coverArt
    val coverFull by lazy { coverArt?.replace("medium-media", "media") }
    val coverThumb by lazy { coverArt?.replace("medium-media", "thumb-media") }
}