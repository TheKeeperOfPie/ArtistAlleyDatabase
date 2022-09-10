package com.thekeeperofpie.artistalleydatabase.vgmdb.album

import androidx.room.Embedded
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
    @Embedded(prefix = "picture_")
    val coverArt: String? = null,
    val performers: List<Performer> = emptyList(),
) {

    val coverMedium = coverArt
    val coverFull by lazy { coverArt?.replace("medium-media", "media") }
    val coverThumb by lazy { coverArt?.replace("medium-media", "thumb-media") }

    @Serializable
    data class Performer(
        val id: String,
        val relativeLink: String,
        val names: Map<String, String>,
    )
}