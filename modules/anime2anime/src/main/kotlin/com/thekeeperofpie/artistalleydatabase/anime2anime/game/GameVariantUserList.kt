package com.thekeeperofpie.artistalleydatabase.anime2anime.game

import androidx.compose.ui.res.stringResource
import com.anilist.Anime2AnimeCountQuery
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.filter.SortFilterSection
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime2anime.R
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterEntry
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
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
    onClearText: () -> Unit,
) : GameVariant<GameVariantUserList.Options>(
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

    private val listStatusSection = SortFilterSection.Filter(
        titleRes = R.string.anime2anime_filter_list_status_label,
        titleDropdownContentDescriptionRes = R.string.anime2anime_filter_list_status_content_description,
        includeExcludeIconContentDescriptionRes = R.string.anime2anime_filter_list_status_chip_state_content_description,
        values = listOf(
            MediaListStatus.CURRENT,
            MediaListStatus.PLANNING,
            MediaListStatus.COMPLETED,
            MediaListStatus.DROPPED,
            MediaListStatus.PAUSED,
            MediaListStatus.REPEATING,
        ),
        valueToText = { stringResource(it.value.toTextRes(MediaType.ANIME)) },
    ).apply {
        filterOptions = filterOptions.map {
            if (it.value == MediaListStatus.DROPPED) {
                it.copy(state = FilterIncludeExcludeState.EXCLUDE)
            } else {
                it
            }
        }
    }

    override val options = listOf(listStatusSection)

    override fun options() = Options(
        listStatuses = listStatusSection.filterOptions,
    )

    override suspend fun loadStartAndTargetIds(options: Options): LoadingResult<Pair<Int, Int>> {
        // TODO: Using only first() will force the cache response if one exists
        val userListResult = userMediaListController.anime(false)
            .first { it.success && it.result?.isNotEmpty() == true }
        if (!userListResult.success) {
            return userListResult.transformResult { null }
        }

        val userList = userListResult.result.orEmpty()
        val media = userList.flatMap { it.entries }.let {
            FilterIncludeExcludeState.applyFiltering(
                listStatusSection.filterOptions,
                it,
                { listOfNotNull(it.mediaListStatus) },
                mustContainAll = false,
            )
        }
        val startId = media.random().media.id
            ?: return LoadingResult.error(R.string.anime2anime_error_could_not_find_media)
        val targetId = media.filterNot { it.media.id == startId }
            .randomOrNull()?.media?.id
            ?: return LoadingResult.error(R.string.anime2anime_error_could_not_find_media)
        return LoadingResult.success(startId to targetId)
    }

    data class Options(
        val listStatuses: List<FilterEntry<MediaListStatus>>,
    )
}
