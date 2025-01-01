package com.thekeeperofpie.artistalleydatabase.anime2anime.game

import artistalleydatabase.modules.anime2anime.generated.resources.Res
import artistalleydatabase.modules.anime2anime.generated.resources.anime2anime_error_could_not_find_media
import artistalleydatabase.modules.anime2anime.generated.resources.anime2anime_filter_list_status_chip_state_content_description
import artistalleydatabase.modules.anime2anime.generated.resources.anime2anime_filter_list_status_content_description
import artistalleydatabase.modules.anime2anime.generated.resources.anime2anime_filter_list_status_label
import com.anilist.data.Anime2AnimeCountQuery
import com.anilist.data.type.MediaListStatus
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toMediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toTextRes
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.debounceState
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds

class GameVariantUserList(
    api: AuthedAniListApi,
    mediaListStatusController: MediaListStatusController,
    userMediaListController: UserMediaListController,
    ignoreController: IgnoreController,
    settings: AnimeSettings,
    scope: CoroutineScope,
    animeCountResponse: suspend () -> Anime2AnimeCountQuery.Data.SiteStatistics.Anime.Node?,
    onClearText: () -> Unit,
) : GameVariant<GameVariantUserList.Options>(
    api,
    mediaListStatusController,
    userMediaListController,
    ignoreController,
    settings,
    scope,
    animeCountResponse,
    onClearText
) {

    // TODO: Wire up to SavedStateHandle
    private val listStatusIn = MutableStateFlow(emptySet<MediaListStatus>())
    private val listStatusNotIn = MutableStateFlow(setOf(MediaListStatus.DROPPED))
    private val listStatusSection = SortFilterSectionState.Filter(
        title = Res.string.anime2anime_filter_list_status_label,
        titleDropdownContentDescription = Res.string.anime2anime_filter_list_status_content_description,
        includeExcludeIconContentDescription = Res.string.anime2anime_filter_list_status_chip_state_content_description,
        options = MutableStateFlow(
            listOf(
                MediaListStatus.CURRENT,
                MediaListStatus.PLANNING,
                MediaListStatus.COMPLETED,
                MediaListStatus.DROPPED,
                MediaListStatus.PAUSED,
                MediaListStatus.REPEATING,
            )
        ),
        filterIn = listStatusIn,
        filterNotIn = listStatusNotIn,
        valueToText = { stringResource(it.toTextRes(MediaType.ANIME)) },
    )

    override val options = listOf(listStatusSection)

    override val optionsFlow by lazy {
        combineStates(listStatusIn, listStatusNotIn) { listStatusIn, listStatusNotIn ->
            Options(listStatusIn = listStatusIn, listStatusNotIn = listStatusNotIn)
        }
            .debounceState(scope, 1.seconds)
    }

    override suspend fun loadStartId(options: Options) = loadRandomUserMediaId(options)
    override suspend fun loadTargetId(options: Options) = loadRandomUserMediaId(options)

    override fun resetStartMedia() = refreshStart()
    override fun resetTargetMedia() = refreshTarget()

    private suspend fun loadRandomUserMediaId(options: Options): LoadingResult<Int> {
        // TODO: Using only first() will force the cache response if one exists
        val userListResult = userMediaListController.anime(false)
            .first { it.success && it.result?.isNotEmpty() == true }
        if (!userListResult.success) {
            return userListResult.transformResult { null }
        }

        val userList = userListResult.result.orEmpty()
        val media = userList.flatMap { it.entries }.let {
            FilterIncludeExcludeState.applyFiltering(
                includes = options.listStatusIn,
                excludes = options.listStatusNotIn,
                list = it,
                { listOfNotNull(it.mediaFilterable.mediaListStatus?.toMediaListStatus()) },
                mustContainAll = false,
            )
        }
        // TODO: Handle same start/target media
        return media.randomOrNull()?.media?.id?.let(LoadingResult.Companion::success)
            ?: LoadingResult.error(Res.string.anime2anime_error_could_not_find_media)
    }

    data class Options(
        val listStatusIn: Set<MediaListStatus>,
        val listStatusNotIn: Set<MediaListStatus>,
    )
}
