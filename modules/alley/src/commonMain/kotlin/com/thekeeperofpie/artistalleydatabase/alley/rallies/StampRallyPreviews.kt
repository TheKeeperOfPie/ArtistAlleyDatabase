package com.thekeeperofpie.artistalleydatabase.alley.rallies

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.thekeeperofpie.artistalleydatabase.alley.models.Booth
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TableMin
import kotlin.random.Random
import kotlin.uuid.Uuid

object StampRallyWithUserDataProvider : PreviewParameterProvider<StampRallyWithUserData> {

    private val ids = sequence {
        val random = Random(39)
        while (true) {
            yield(Uuid.fromLongs(random.nextLong(), random.nextLong()).toString())
        }
    }

    private val booths = Booth.fromStringOrNull("C39")!!
        .rangeTo(Booth.fromStringOrNull("C99")!!)
        .asSequence()

    override val values = ids.zip(booths)
        .map { [stampRallyId, booth] ->
            StampRallyWithUserData(
                stampRally = StampRallyDatabaseEntry(
                    year = DataYear.LATEST,
                    id = stampRallyId,
                    fandom = "Vocaloid",
                    hostTable = booth.toString(),
                    tables = listOf(booth, booth + 1, booth + 2).map(Booth::toString),
                    startTables = setOf(booth.toString()),
                    endTables = setOf((booth + 2).toString()),
                    links = listOf("https://example.org/stampRally"),
                    tableMin = TableMin.Price(10),
                    totalCost = 30,
                    prize = null,
                    prizeLimit = 100,
                    prizeMerch = emptyList(),
                    series = emptyList(),
                    merch = emptyList(),
                    notes = null,
                    images = emptyList(),
                    confirmed = true,
                    editorNotes = null,
                    lastEditor = null,
                    lastEditTime = null,
                ),
                userEntry = StampRallyUserEntry(
                    stampRallyId = stampRallyId,
                    favorite = false,
                    ignored = false,
                    generation = 0,
                    updatedAt = null,
                ),
            )
        }
}
