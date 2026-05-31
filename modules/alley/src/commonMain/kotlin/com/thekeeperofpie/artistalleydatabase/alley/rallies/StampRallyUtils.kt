package com.thekeeperofpie.artistalleydatabase.alley.rallies

import androidx.compose.runtime.Composable
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_prize_limit_unknown
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import org.jetbrains.compose.resources.stringResource

@Composable
fun StampRallyDatabaseEntry.prizeLimitText() = prizeLimit?.toString()
    ?: stringResource(Res.string.alley_stamp_rally_prize_limit_unknown)

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
