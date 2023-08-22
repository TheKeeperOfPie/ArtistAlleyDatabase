package com.thekeeperofpie.artistalleydatabase.anime.recommendation.media

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.anilist.fragment.MediaAndRecommendationsRecommendation
import com.hoc081098.flowext.combine
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.transformIf
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationEntry
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationStatusController
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationsSortOption
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListViewModel
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapNotNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MediaRecommendationsViewModel @Inject constructor(
    aniListApi: AuthedAniListApi,
    private val mediaListStatusController: MediaListStatusController,
    private val recommendationStatusController: RecommendationStatusController,
    private val ignoreController: IgnoreController,
    private val settings: AnimeSettings,
    favoritesController: FavoritesController,
) : HeaderAndListViewModel<MediaRecommendationsScreen.Entry, MediaAndRecommendationsRecommendation,
        RecommendationEntry, RecommendationsSortOption>(
    aniListApi = aniListApi,
    sortOptionEnum = RecommendationsSortOption::class,
    sortOptionEnumDefault = RecommendationsSortOption.RATING,
    loadingErrorTextRes = R.string.anime_recommendations_error_loading
) {
    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)

    val recommendationToggleHelper =
        RecommendationToggleHelper(aniListApi, recommendationStatusController, viewModelScope)

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

    override fun makeEntry(item: MediaAndRecommendationsRecommendation) =
        RecommendationEntry(mediaId = headerId, recommendation = item)

    override fun entryId(entry: RecommendationEntry) = entry.recommendation.id.toString()

    override suspend fun initialRequest(
        headerId: String,
        sortOption: RecommendationsSortOption,
        sortAscending: Boolean,
    ) = MediaRecommendationsScreen.Entry(
        aniListApi.mediaAndRecommendations(
            mediaId = headerId,
            sort = sortOption.toApiValue(sortAscending)
        )
    )

    override suspend fun pagedRequest(
        entry: MediaRecommendationsScreen.Entry,
        page: Int,
        sortOption: RecommendationsSortOption,
        sortAscending: Boolean,
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
        flatMapLatest { pagingData ->
            combine(
                mediaListStatusController.allChanges(),
                recommendationStatusController.allChanges(),
                ignoreController.updates(),
                settings.showIgnored,
                settings.showAdult,
                settings.showLessImportantTags,
                settings.showSpoilerTags,
            ) { mediaListUpdates, recommendationUpdates, _, showIgnored, showAdult, showLessImportantTags, showSpoilerTags ->
                pagingData.mapNotNull {
                    val mediaPreview = it.recommendation.mediaRecommendation
                    applyMediaFiltering(
                        statuses = mediaListUpdates,
                        ignoreController = ignoreController,
                        showAdult = showAdult,
                        showIgnored = showIgnored,
                        showLessImportantTags = showLessImportantTags,
                        showSpoilerTags = showSpoilerTags,
                        entry = it,
                        transform = { it },
                        media = mediaPreview,
                        copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
                            copy(
                                mediaListStatus = mediaListStatus,
                                progress = progress,
                                progressVolumes = progressVolumes,
                                scoreRaw = scoreRaw,
                                ignored = ignored,
                                showLessImportantTags = showLessImportantTags,
                                showSpoilerTags = showSpoilerTags,
                            )
                        },
                    )?.let {
                        val recommendationUpdate =
                            recommendationUpdates[it.mediaId to it.recommendation.mediaRecommendation.id.toString()]
                        val userRating = recommendationUpdate?.rating ?: it.userRating
                        it.transformIf(userRating != it.userRating) {
                            copy(userRating = userRating)
                        }
                    }
                }
            }
        }
}
