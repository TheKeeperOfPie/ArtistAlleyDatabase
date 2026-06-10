package com.thekeeperofpie.artistalleydatabase.alley.rallies

import androidx.compose.runtime.Composable
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_prize_limit_unknown
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyEntryAnimeExpo2026
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import org.jetbrains.compose.resources.stringResource

@Composable
fun StampRallyDatabaseEntry.prizeLimitText() = prizeLimit?.toString()
    ?: stringResource(Res.string.alley_stamp_rally_prize_limit_unknown)

val StampRallyDatabaseEntry.startTableOrDefault: String?
    get() = startTables.minByOrNull { tables.indexOf(it) } ?: tables.firstOrNull()

val StampRallyEntryAnimeExpo2026.startTableOrDefault: String?
    get() = startTables?.minByOrNull { tables.indexOf(it) } ?: tables.firstOrNull()

internal object StampRallyUtils  {

    fun imageSubquery(idField: String) = """(
            SELECT
                json_group_array (
                    json_object (
                        'id', s.id,
                        'aniListId', s.aniListId,
                        'wikipediaId', s.wikipediaId,
                        'tmdbId', s.tmdbId,
                        'tmdbType', s.tmdbType,
                        'steamId', s.steamId,
                        'steamImagePath', s.steamImagePath,
                        'openLibraryId', s.openLibraryId
                    )
                )
            FROM
                seriesEntry s
                JOIN stampRallySeriesConnection sc ON sc.seriesId = s.id
            WHERE
                sc.stampRallyId = $idField
        )""".trimIndent()
}
