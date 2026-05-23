package com.thekeeperofpie.artistalleydatabase.alley.rallies

import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImageInfo
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DatabaseImage

data class StampRallyWithUserData(
    val stampRally: StampRallyDatabaseEntry,
    val userEntry: StampRallyUserEntry,
    val seriesImageInfo: List<SeriesImageInfo> = emptyList(),
    val artistBoothToEmbeds: Map<String, Map<String, DatabaseImage>> = emptyMap(),
)
