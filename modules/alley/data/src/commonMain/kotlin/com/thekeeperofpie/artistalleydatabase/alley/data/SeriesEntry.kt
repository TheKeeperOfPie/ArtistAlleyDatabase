package com.thekeeperofpie.artistalleydatabase.alley.data

import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.SeriesSource
import kotlin.uuid.Uuid

fun SeriesEntry.toSeriesInfo() = SeriesInfo(
    id = id,
    uuid = Uuid.parse(uuid),
    notes = notes,
    aniListId = aniListId,
    aniListType = aniListType,
    wikipediaId = wikipediaId,
    source = source ?: SeriesSource.NONE,
    titlePreferred = titlePreferred,
    titleEnglish = titleEnglish,
    titleRomaji = titleRomaji,
    titleNative = titleNative,
    link = link,
)
