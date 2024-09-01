package com.thekeeperofpie.artistalleydatabase.vgmdb.album

import kotlinx.serialization.Serializable

@Serializable
data class AlbumColumnEntry(
    val id: String,
    val catalogId: String?,
    val title: String,
)