package com.thekeeperofpie.artistalleydatabase.anime.recommendation

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.anilist.fragment.MediaAndRecommendationsRecommendation
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaStatusChanges
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class RecommendationsViewModel @Inject constructor(
    aniListApi: AuthedAniListApi,
    private val statusController: MediaListStatusController,
    private val ignoreList: AnimeMediaIgnoreList,
    private val settings: AnimeSettings,
    favoritesController: FavoritesController,
) : HeaderAndListViewModel<RecommendationsScreen.Entry, MediaAndRecommendationsRecommendation,
        RecommendationEntry, RecommendationsSortOption>(
    aniListApi = aniListApi,
    sortOptionEnum = RecommendationsSortOption::class,
    sortOptionEnumDefault = RecommendationsSortOption.RATING,
    loadingErrorTextRes = R.string.anime_recommendations_error_loading
) {
    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)

    override fun initialize(headerId: String) {
        super.initialize(headerId)
        favoritesToggleHelper.initializeTracking(
            viewModel = this,
            entry = { snapshotFlow { entry } },
            entryToId = { it.media.id.toString() },
            entryToType = { it.media.type.toFavoriteType() },
            entryToFavorite = { it.media.isFavourite },
        )
    }

    override fun makeEntry(item: MediaAndRecommendationsRecommendation) = RecommendationEntry(item)

    override fun entryId(entry: RecommendationEntry) = entry.recommendation.id.toString()

    override suspend fun initialRequest(
        headerId: String,
        sortOption: RecommendationsSortOption,
        sortAscending: Boolean
    ) = RecommendationsScreen.Entry(
        aniListApi.mediaAndRecommendations(
            mediaId = headerId,
            sort = sortOption.toApiValue(sortAscending)
        )
    )

    override suspend fun pagedRequest(
        entry: RecommendationsScreen.Entry,
        page: Int,
        sortOption: RecommendationsSortOption,
        sortAscending: Boolean
    ) = if (page == 1) {
        val result = entry.media.recommendations
        result?.pageInfo to result?.nodes?.filterNotNull().orEmpty()
    } else {
        val result = aniListApi.mediaAndRecommendationsPage(
            mediaId = entry.media.id.toString(),
            sort = sortOption.toApiValue(sortAscending),
            page = page,
        ).media.recommendations
        result?.pageInfo to result?.nodes?.filterNotNull().orEmpty()
    }

    override fun Flow<PagingData<RecommendationEntry>>.transformFlow() =
        applyMediaStatusChanges(
            statusController = statusController,
            ignoreList = ignoreList,
            settings = settings,
            media = { it.recommendation.mediaRecommendation },
            copy = { mediaListStatus, progress, progressVolumes, ignored, showLessImportantTags, showSpoilerTags ->
                copy(
                    mediaListStatus = mediaListStatus,
                    progress = progress,
                    progressVolumes = progressVolumes,
                    ignored = ignored,
                    showLessImportantTags = showLessImportantTags,
                    showSpoilerTags = showSpoilerTags,
                )
            },
        )

    fun onMediaLongClick(entry: AnimeMediaListRow.Entry<*>) =
        ignoreList.toggle(entry.media.id.toString())
}
