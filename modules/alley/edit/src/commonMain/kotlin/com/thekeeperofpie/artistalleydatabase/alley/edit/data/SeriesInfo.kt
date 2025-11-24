package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesRowInfo
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.SeriesSource
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class SeriesInfo(
    override val id: String,
    override val uuid: Uuid,
    override val notes: String?,
    override val aniListId: Long?,
    override val aniListType: String?,
    override val wikipediaId: Long?,
    val source: SeriesSource?,
    override val titlePreferred: String,
    override val titleEnglish: String,
    override val titleRomaji: String,
    override val titleNative: String,
    override val link: String?,
) : SeriesRowInfo
