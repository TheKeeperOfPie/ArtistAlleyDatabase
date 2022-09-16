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
    val picture: String? = null,
) {
    val pictureFull get() = picture
    val pictureThumb by lazy { picture?.replace("media", "thumb-media") }
}