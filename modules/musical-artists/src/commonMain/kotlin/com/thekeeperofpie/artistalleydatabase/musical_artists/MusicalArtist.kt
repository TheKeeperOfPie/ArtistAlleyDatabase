package com.thekeeperofpie.artistalleydatabase.musical_artists

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity("musical_artists")
data class MusicalArtist(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: Type,
    val image: String? = null,
) {
    enum class Type {
        CUSTOM,
        VGMDB,
    }
}