package com.thekeeperofpie.artistalleydatabase.alley.rallies

import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyUserEntry
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

object StampRallyWithUserDataProvider : PreviewParameterProvider<StampRallyWithUserData> {
    override val values = sequenceOf(
        StampRallyWithUserData(
            stampRally = StampRallyEntry(
                year = DataYear.YEAR_2025,
                id = "stampRallyId",
                fandom = "Vocaloid",
                hostTable = "C39",
                tables = listOf("C39, C40, C41"),
                links = listOf("https://example.org/stampRally"),
                tableMin = 10,
                totalCost = 30,
                prizeLimit = 100,
                notes = null,
                counter = 1,
            ),
            userEntry = StampRallyUserEntry(
                stampRallyId = "stampRallyId",
                favorite = false,
                ignored = false,
            ),
        )
    )

}
