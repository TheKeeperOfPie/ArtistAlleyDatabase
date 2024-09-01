package com.thekeeperofpie.artistalleydatabase.vgmdb.album

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity("album_entries")
data class AlbumEntry(
    @PrimaryKey
    val id: String,
    val catalogId: String?,
    val names: Map<String, String> = emptyMap(),
    val coverArt: String? = null,
    val performers: List<String> = emptyList(),
    val composers: List<String> = emptyList(),

    /** Encoded list of [DiscEntry] */
    val discs: List<String> = emptyList(),
) {
    val coverMedium get() = coverArt
    val coverFull by lazy { coverArt?.replace("medium-media", "media") }
    val coverThumb by lazy { coverArt?.replace("medium-media", "thumb-media") }
}