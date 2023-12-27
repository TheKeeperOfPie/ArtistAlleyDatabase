package com.thekeeperofpie.artistalleydatabase.anime2anime.game

import com.anilist.Anime2AnimeCountQuery
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime2anime.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class GameVariantUserList(
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
    override suspend fun loadStartAndTargetIds(): LoadingResult<Pair<Int, Int>> {
        // TODO: Using only first() will force the cache response if one exists
        val userListResult = userMediaListController.anime(false)
            .first { it.success && it.result?.isNotEmpty() == true }
        if (!userListResult.success) {
            return userListResult.transformResult { null }
        }

        val userList = userListResult.result.orEmpty()
        val startId = userList.flatMap { it.entries }.random().media.id
            ?: return LoadingResult.error(R.string.anime2anime_error_could_not_fetch_media)
        val targetId = userList.flatMap { it.entries }.filterNot { it.media.id == startId }
            .randomOrNull()?.media?.id
            ?: return LoadingResult.error(R.string.anime2anime_error_could_not_fetch_media)
        return LoadingResult.success(startId to targetId)
    }
}
