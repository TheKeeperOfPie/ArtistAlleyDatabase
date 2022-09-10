package com.thekeeperofpie.artistalleydatabase.vgmdb.album

import kotlinx.serialization.Serializable

@Serializable
class AlbumColumnEntry(
    val id: String,
    val title: String = "",
)