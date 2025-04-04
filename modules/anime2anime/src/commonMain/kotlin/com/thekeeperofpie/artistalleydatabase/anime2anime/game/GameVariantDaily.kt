package com.thekeeperofpie.artistalleydatabase.anime2anime.game

import com.anilist.data.Anime2AnimeCountQuery
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime2anime.game.GameVariantRandom.Companion.loadRandom
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ReadOnlyStateFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import kotlinx.coroutines.CoroutineScope
import kotlin.random.Random

class GameVariantDaily(
    api: AuthedAniListApi,
    mediaListStatusController: MediaListStatusController,
    userMediaListController: UserMediaListController,
    ignoreController: IgnoreController,
    settings: AnimeSettings,
    scope: CoroutineScope,
    animeCountResponse: suspend () -> Anime2AnimeCountQuery.Data.SiteStatistics.Anime.Node?,
    onClearText: () -> Unit
) : GameVariant<Unit>(
    api,
    mediaListStatusController,
    userMediaListController,
    ignoreController,
    settings,
    scope,
    animeCountResponse,
    onClearText
) {
    companion object {
        private const val DAILY_END_TRIM = 500
    }

    override val optionsFlow = ReadOnlyStateFlow(Unit)

    override suspend fun loadStartId(options: Unit): LoadingResult<Int> {
        val (animeCount, seed) = animeCountAndDate()
        // Trim the last DAILY_END_TRIM entries so nothing too new is used
        return with(this) {
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

    override suspend fun loadTargetId(options: Unit): LoadingResult<Int> {
        val (animeCount, seed) = animeCountAndDate()
        // Trim the last DAILY_END_TRIM entries so nothing too new is used
        return with(this) {
            loadRandom(
                Random(seed = seed).also {
                    // Skip one value so it doesn't overlap with start
                    // TODO: Can still overlap if the random returns the same value twice in a row
                    it.nextInt()
                },
                totalAnimeCount = animeCount - DAILY_END_TRIM,
                applyPopularityFilter = true,
                applyFinishedFilter = true,
                applyMinCharacterFilter = true,
                applyMinStaffFilter = true,
            )
        }
    }

    override fun resetStartMedia() = Unit // Not applicable
    override fun resetTargetMedia() = Unit // Not applicable
}
