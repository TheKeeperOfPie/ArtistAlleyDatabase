package com.thekeeperofpie.artistalleydatabase.alley.series

import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.SeriesUserEntry

data class SeriesWithUserData(
    val series: SeriesEntry,
    val userEntry: SeriesUserEntry,
)
