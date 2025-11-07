package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import kotlinx.serialization.Serializable

@Serializable
data class SeriesInfo(
    val id: String,
    val uuid: String,
    val notes: String?,
    val aniListId: Long?,
    val aniListType: String?,
    val wikipediaId: Long?,
    val titlePreferred: String,
    val titleEnglish: String,
    val titleRomaji: String,
    val titleNative: String,
)
