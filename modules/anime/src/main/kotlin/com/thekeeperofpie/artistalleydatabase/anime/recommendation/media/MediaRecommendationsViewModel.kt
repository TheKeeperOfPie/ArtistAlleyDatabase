package com.thekeeperofpie.artistalleydatabase.anime.recommendation.media

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.anilist.fragment.MediaAndRecommendationsRecommendation
import com.hoc081098.flowext.combine
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationSortOption
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationStatusController
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListViewModel
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapNotNull
import com.thekeeperofpie.artistalleydatabase.compose.filter.selectedOption
import com.thekeeperofpie.artistalleydatabase.compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.transformIf
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
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
    featureOverrideProvider: FeatureOverrideProvider,
    savedStateHandle: SavedStateHandle,
    navigationTypeMap: NavigationTypeMap,
) : HeaderAndListViewModel<MediaRecommendationsScreen.Entry, MediaAndRecommendationsRecommendation,
        MediaRecommendationEntry, RecommendationSortOption, MediaRecommendationSortFilterController.FilterParams>(
    aniListApi = aniListApi,
    loadingErrorTextRes = R.string.anime_recommendations_error_loading
) {
    private val destination = savedStateHandle.toDestination<AnimeDestination.MediaRecommendations>(navigationTypeMap)
    val mediaId = destination.mediaId
    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)

    val recommendationToggleHelper =
        RecommendationToggleHelper(aniListApi, recommendationStatusController, viewModelScope)

    override val sortFilterController =
        MediaRecommendationSortFilterController(viewModelScope, settings, featureOverrideProvider)

    init {
        favoritesToggleHelper.initializeTracking(
            scope = viewModelScope,
            entry = { snapshotFlow { entry.result } },
            entryToId = { it.media.id.toString() },
            entryToType = { it.media.type.toFavoriteType() },
            entryToFavorite = { it.media.isFavourite },
        )
    }

    override fun makeEntry(item: MediaAndRecommendationsRecommendation) =
        MediaRecommendationEntry(mediaId = mediaId, recommendation = item)

    override fun entryId(entry: MediaRecommendationEntry) = entry.recommendation.id.toString()

    override suspend fun initialRequest(
        filterParams: MediaRecommendationSortFilterController.FilterParams?,
    ) = MediaRecommendationsScreen.Entry(aniListApi.mediaAndRecommendations(mediaId = mediaId))

    override suspend fun pagedRequest(
        page: Int,
        filterParams: MediaRecommendationSortFilterController.FilterParams?,
    ) = aniListApi.mediaAndRecommendationsPage(
        mediaId = mediaId,
        sort = filterParams!!.sort.selectedOption(RecommendationSortOption.RATING)
            .toApiValue(filterParams.sortAscending),
        page = page,
    ).media.recommendations.run { pageInfo to nodes }

    override fun Flow<PagingData<MediaRecommendationEntry>>.transformFlow() =
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
