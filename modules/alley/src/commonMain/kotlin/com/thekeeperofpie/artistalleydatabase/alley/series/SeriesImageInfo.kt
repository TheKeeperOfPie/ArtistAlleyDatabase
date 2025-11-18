package com.thekeeperofpie.artistalleydatabase.alley.series

import com.thekeeperofpie.artistalleydatabase.alley.data.SeriesEntry

data class SeriesImageInfo(
    val id: String,
    val uuid: String,
    val aniListId: Long?,
    val aniListType: String?,
    val wikipediaId: Long?,
)

fun SeriesEntry.toImageInfo() = SeriesImageInfo(
    id = id,
    uuid = uuid,
    aniListId = aniListId,
    aniListType = aniListType,
    wikipediaId = wikipediaId,
)
