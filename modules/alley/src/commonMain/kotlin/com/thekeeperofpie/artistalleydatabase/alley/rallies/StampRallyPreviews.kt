package com.thekeeperofpie.artistalleydatabase.alley.rallies

import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

object StampRallyWithUserDataProvider : PreviewParameterProvider<StampRallyWithUserData> {
    override val values = sequenceOf(
        StampRallyWithUserData(
            stampRally = StampRallyEntry(
                year = DataYear.ANIME_EXPO_2025,
                id = "stampRallyId",
                fandom = "Vocaloid",
                hostTable = "C39",
                tables = listOf("C39, C40, C41"),
                links = listOf("https://example.org/stampRally"),
                _tableMin = 10,
                totalCost = 30,
                prize = null,
                prizeLimit = 100,
                series = emptyList(),
                notes = null,
                counter = 1,
                images = emptyList(),
                confirmed = true,
            ),
            userEntry = StampRallyUserEntry(
                stampRallyId = "stampRallyId",
                favorite = false,
                ignored = false,
            ),
        )
    )

}
