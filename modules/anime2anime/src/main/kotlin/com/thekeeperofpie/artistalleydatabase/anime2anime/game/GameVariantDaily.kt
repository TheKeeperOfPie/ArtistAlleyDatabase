package com.thekeeperofpie.artistalleydatabase.anime2anime.game

import com.anilist.Anime2AnimeCountQuery
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime2anime.game.GameVariantRandom.Companion.loadRandom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class GameVariantDaily(
    api: AuthedAniListApi,
    mediaListStatusController: MediaListStatusController,
    userMediaListController: UserMediaListController,
    ignoreController: IgnoreController,
    settings: AnimeSettings,
    scope: CoroutineScope,
    refresh: Flow<Long>,
    animeCountResponse: suspend () -> Anime2AnimeCountQuery.Data.SiteStatistics.Anime.Node?,
    onClearText: () -> Unit
) : GameVariant(
    api,
    mediaListStatusController,
    userMediaListController,
    ignoreController,
    settings,
    scope,
    refresh,
    animeCountResponse,
    onClearText
) {
    companion object {
        private const val DAILY_END_TRIM = 500
    }

    override suspend fun loadStartAndTargetIds(): LoadingResult<Pair<Int, Int>> {
        val (animeCount, seed) = animeCountAndDate()
        // Trim the last DAILY_END_TRIM entries so nothing too new is used
        return with(this as GameVariant) {
            loadRandom(
                Random(seed = seed),
                totalAnimeCount = animeCount - DAILY_END_TRIM,
                applyPopularityFilter = true,
                applyFinishedFilter = true,
                applyMinCharacterFilter = true,
                applyMinStaffFilter = true,
            )
        }
    }
}
