package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesRowInfo
import kotlinx.serialization.Serializable

@Serializable
data class SeriesInfo(
    override val id: String,
    override val uuid: String,
    override val notes: String?,
    override val aniListId: Long?,
    override val aniListType: String?,
    override val wikipediaId: Long?,
    override val titlePreferred: String,
    override val titleEnglish: String,
    override val titleRomaji: String,
    override val titleNative: String,
    override val link: String?,
) : SeriesRowInfo
