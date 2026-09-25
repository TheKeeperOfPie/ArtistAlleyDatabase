package com.thekeeperofpie.artistalleydatabase.alley.rallies

import com.thekeeperofpie.artistalleydatabase.alley.images.AlleyImageUtils
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImageInfo
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DatabaseImage

data class StampRallyWithUserData(
    val stampRally: StampRallyDatabaseEntry,
    val userEntry: StampRallyUserEntry,
    val seriesImageInfo: List<SeriesImageInfo> = emptyList(),
    private val _artistBoothsToProfileImages: Map<String, DatabaseImage?> = emptyMap(),
) {
    val images = AlleyImageUtils.getRallyImages(
        year = stampRally.year,
        images = stampRally.images,
    )

    val artistBoothsToProfileImages = _artistBoothsToProfileImages.mapNotNull {
        it.key to AlleyImageUtils.getProfileImage(stampRally.year, it.value)
    }
}
