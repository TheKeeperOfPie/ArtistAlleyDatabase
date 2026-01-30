package com.thekeeperofpie.artistalleydatabase.alley.rallies

import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TableMin
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

object StampRallyWithUserDataProvider : PreviewParameterProvider<StampRallyWithUserData> {
    override val values = sequenceOf(
        StampRallyWithUserData(
            stampRally = StampRallyDatabaseEntry(
                year = DataYear.ANIME_EXPO_2025,
                id = "stampRallyId",
                fandom = "Vocaloid",
                hostTable = "C39",
                tables = listOf("C39, C40, C41"),
                links = listOf("https://example.org/stampRally"),
                tableMin = TableMin.Price(10),
                totalCost = 30,
                prize = null,
                prizeLimit = 100,
                series = emptyList(),
                notes = null,
                images = emptyList(),
                confirmed = true,
                editorNotes = null,
                lastEditor = null,
                lastEditTime = null,
            ),
            userEntry = StampRallyUserEntry(
                stampRallyId = "stampRallyId",
                favorite = false,
                ignored = false,
            ),
        )
    )

}
