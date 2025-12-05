package com.thekeeperofpie.artistalleydatabase.alley.models

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.SeriesSource
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class SeriesInfo(
    val id: String,
    val uuid: Uuid,
    val notes: String?,
    val aniListId: Long?,
    val aniListType: AniListType,
    val wikipediaId: Long?,
    val source: SeriesSource,
    val titlePreferred: String,
    val titleEnglish: String,
    val titleRomaji: String,
    val titleNative: String,
    val synonyms: List<String>,
    val link: String?,
)
