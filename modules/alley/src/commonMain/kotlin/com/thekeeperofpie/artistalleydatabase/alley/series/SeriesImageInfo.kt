package com.thekeeperofpie.artistalleydatabase.alley.series

import kotlin.uuid.Uuid

data class SeriesImageInfo(
    val id: String,
    val uuid: Uuid,
    val aniListId: Long?,
    val wikipediaId: Long?,
)
