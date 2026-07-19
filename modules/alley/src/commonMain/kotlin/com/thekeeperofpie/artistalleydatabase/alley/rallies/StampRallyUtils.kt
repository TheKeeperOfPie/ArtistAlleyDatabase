package com.thekeeperofpie.artistalleydatabase.alley.rallies

import androidx.compose.runtime.Composable
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_prize_limit_unknown
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyEntryAnimeExpo2026
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import org.jetbrains.compose.resources.stringResource

@Composable
fun StampRallyDatabaseEntry.prizeLimitText() = prizeLimit?.toString()
    ?: stringResource(Res.string.alley_stamp_rally_prize_limit_unknown)

val StampRallyDatabaseEntry.startTableOrDefault: String?
    get() = startTables.minByOrNull { tables.indexOf(it) } ?: tables.firstOrNull()

val StampRallyEntryAnimeExpo2026.startTableOrDefault: String?
    get() = startTables?.minByOrNull { tables.indexOf(it) } ?: tables.firstOrNull()

internal object StampRallyUtils  {

    fun imageSubquery(rowIdField: String, dataYear: DataYear) = """(
            SELECT
                json_group_array (
                    json_object (
                        'id', seriesEntry.id,
                        'aniListId', seriesEntry.aniListId,
                        'wikipediaId', seriesEntry.wikipediaId,
                        'tmdbId', seriesEntry.tmdbId,
                        'tmdbType', seriesEntry.tmdbType,
                        'steamId', seriesEntry.steamId,
                        'steamImagePath', seriesEntry.steamImagePath,
                        'openLibraryId', seriesEntry.openLibraryId
                    )
                )
            FROM
                seriesEntry
                JOIN stampRallySeriesConnection ON stampRallySeriesConnection.seriesId = seriesEntry.id
            WHERE
                stampRallySeriesConnection.stampRallyRowId = $rowIdField
                AND stampRallySeriesConnection.dataYear = '${dataYear.serializedName}'
        )""".trimIndent()
}
