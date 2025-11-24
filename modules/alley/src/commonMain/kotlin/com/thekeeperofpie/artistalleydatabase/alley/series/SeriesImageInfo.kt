package com.thekeeperofpie.artistalleydatabase.alley.series

import com.thekeeperofpie.artistalleydatabase.alley.data.SeriesEntry
import kotlin.uuid.Uuid

data class SeriesImageInfo(
    val id: String,
    val uuid: Uuid,
    val aniListId: Long?,
    val aniListType: String?,
    val wikipediaId: Long?,
)

fun SeriesEntry.toImageInfo() = SeriesImageInfo(
    id = id,
    uuid = Uuid.parse(uuid),
    aniListId = aniListId,
    aniListType = aniListType,
    wikipediaId = wikipediaId,
)
