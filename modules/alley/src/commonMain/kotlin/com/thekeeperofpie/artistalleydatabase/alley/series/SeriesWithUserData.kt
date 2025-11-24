package com.thekeeperofpie.artistalleydatabase.alley.series

import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.user.SeriesUserEntry

data class SeriesWithUserData(
    val series: SeriesInfo,
    val userEntry: SeriesUserEntry,
)
