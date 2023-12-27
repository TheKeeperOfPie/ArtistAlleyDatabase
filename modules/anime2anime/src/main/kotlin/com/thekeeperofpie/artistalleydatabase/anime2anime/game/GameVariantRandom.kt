package com.thekeeperofpie.artistalleydatabase.anime2anime.game

import com.anilist.Anime2AnimeCountQuery
import com.anilist.Anime2AnimeRandomAnimeQuery
import com.anilist.type.MediaStatus
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime2anime.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class GameVariantRandom(
    api: AuthedAniListApi,
    mediaListStatusController: MediaListStatusController,
    userMediaListController: UserMediaListController,
    ignoreController: IgnoreController,
    settings: AnimeSettings,
    scope: CoroutineScope,
    refresh: Flow<Long>,
    animeCountResponse: suspend () -> Anime2AnimeCountQuery.Data.SiteStatistics.Anime.Node?,
    onClearText: () -> Unit
) : GameVariant<Unit>(
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
        private const val MIN_MEDIA_POPULARITY = 10000

        context (GameVariant<*>)
        suspend fun loadRandom(
            random: Random,
            totalAnimeCount: Int,
            applyPopularityFilter: Boolean,
            applyFinishedFilter: Boolean,
            applyMinCharacterFilter: Boolean,
            applyMinStaffFilter: Boolean,
        ): LoadingResult<Pair<Int, Int>> {
            // TODO: Error handling if no anime returned
            val startAndEndAnime = (0 until 2).map {
                randomAnime(
                    random = random,
                    totalAnimeCount = totalAnimeCount,
                    applyPopularityFilter = applyPopularityFilter,
                    applyFinishedFilter = applyFinishedFilter,
                    applyMinCharacterFilter = applyMinCharacterFilter,
                    applyMinStaffFilter = applyMinStaffFilter,
                )
            }
            val startAnime = startAndEndAnime.getOrNull(0)
                ?: return LoadingResult.error(R.string.anime2anime_error_could_not_fetch_media)
            val targetAnime = startAndEndAnime.getOrNull(1)
                ?: return LoadingResult.error(R.string.anime2anime_error_could_not_fetch_media)

            // TODO: Handle really rare occurrence of getting the same show
            if (startAnime.id == targetAnime.id) {
                return LoadingResult.error(R.string.anime2anime_error_could_not_fetch_media)
            }
            return LoadingResult.success(startAnime.id to targetAnime.id)
        }

        context (GameVariant<*>)
        private suspend fun randomAnime(
            random: Random,
            totalAnimeCount: Int,
            applyPopularityFilter: Boolean,
            applyFinishedFilter: Boolean,
            applyMinCharacterFilter: Boolean,
            applyMinStaffFilter: Boolean,
        ): Anime2AnimeRandomAnimeQuery.Data.Page.Medium? {
            var attempts = 0
            var randomAnime: Anime2AnimeRandomAnimeQuery.Data.Page.Medium? = null
            while (randomAnime == null && attempts++ < 5) {
                val randomPage = random.nextInt(1, totalAnimeCount / 25)
                randomAnime = api.anime2AnimeRandomAnime(randomPage, 5).getOrNull()
                    ?.asSequence()
                    ?.filterNotNull()
                    ?.filter { !applyPopularityFilter || (it.popularity ?: 0) >= MIN_MEDIA_POPULARITY }
                    ?.filter { !applyFinishedFilter || it.status == MediaStatus.FINISHED }
                    ?.filter {
                        !applyMinCharacterFilter || (it.characters?.pageInfo?.hasNextPage ?: false)
                    }
                    ?.filter { !applyMinStaffFilter || (it.staff?.pageInfo?.hasNextPage ?: false) }
                    ?.firstOrNull()
            }
            return randomAnime
        }
    }

    override fun options() = Unit

    override suspend fun loadStartAndTargetIds(options: Unit): LoadingResult<Pair<Int, Int>> {
        return loadRandom(
            random = Random,
            totalAnimeCount = animeCountAndDate().first,
            // TODO: Difficulty levels
            applyPopularityFilter = false,
            applyFinishedFilter = false,
            applyMinCharacterFilter = false,
            applyMinStaffFilter = false,
        )
    }
}
