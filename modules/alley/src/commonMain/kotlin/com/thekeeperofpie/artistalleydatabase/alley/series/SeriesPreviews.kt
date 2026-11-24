package com.thekeeperofpie.artistalleydatabase.alley.series

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.user.SeriesUserEntry
import kotlin.random.Random
import kotlin.uuid.Uuid

object SeriesWithUserDataProvider : PreviewParameterProvider<SeriesWithUserData> {

    private val seriesIds = sequence {
        val random = Random(3939)
        while (true) {
            yield(Uuid.fromLongs(random.nextLong(), random.nextLong()).toString())
        }
    }
    
    override val values = seriesIds.mapIndexed { index, seriesId ->
        val series = SeriesInfo.fake(seriesId)
        SeriesWithUserData(
            series = series,
            userEntry = SeriesUserEntry(
                seriesId = series.uuid.toString(),
                favorite = index % 2 == 1,
                generation = 0,
                updatedAt = null,
            ),
        )
    }
}
