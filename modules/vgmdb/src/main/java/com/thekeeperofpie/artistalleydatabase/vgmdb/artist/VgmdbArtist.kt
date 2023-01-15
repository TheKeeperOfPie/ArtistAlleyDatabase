package com.thekeeperofpie.artistalleydatabase.vgmdb.artist

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity("vgmdb_artists")
data class VgmdbArtist(
    @PrimaryKey
    val id: String,
    val names: Map<String, String> = emptyMap(),
    val name: String =
        names["ja-latn"] ?: names["en"] ?: names["jp"] ?: names.values.firstOrNull() ?: "",
    val picture: String? = null,
) {

    val pictureFull get() = picture
    val pictureThumb by lazy { picture?.replace("media", "thumb-media") }
}