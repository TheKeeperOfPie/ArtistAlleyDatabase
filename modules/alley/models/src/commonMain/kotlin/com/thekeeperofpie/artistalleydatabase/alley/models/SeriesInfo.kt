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
    val faked: Boolean = false,
) {
    companion object {
        fun fake(id: String) = SeriesInfo(
            id = id,
            uuid = Utils.uuidFromRandomBytes(id),
            notes = null,
            aniListId = null,
            aniListType = AniListType.NONE,
            wikipediaId = null,
            source = SeriesSource.NONE,
            titlePreferred = id,
            titleEnglish = id,
            titleRomaji = id,
            titleNative = id,
            synonyms = emptyList(),
            link = null,
            faked = true,
        )
    }
}
