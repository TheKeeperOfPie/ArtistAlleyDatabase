package com.thekeeperofpie.artistalleydatabase.anime.recommendation.media

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_recommendations_error_loading
import com.anilist.data.fragment.MediaAndRecommendationsRecommendation
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.anime.recommendations.RecommendationSortOption
import com.thekeeperofpie.artistalleydatabase.anime.recommendations.RecommendationStatusController
import com.thekeeperofpie.artistalleydatabase.anime.recommendations.RecommendationToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListViewModel
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.transformIf
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.selectedOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class MediaRecommendationsViewModel(
    aniListApi: AuthedAniListApi,
    private val mediaListStatusController: MediaListStatusController,
    private val recommendationStatusController: RecommendationStatusController,
    private val ignoreController: IgnoreController,
    private val settings: AnimeSettings,
    favoritesController: FavoritesController,
    featureOverrideProvider: FeatureOverrideProvider,
    @Assisted savedStateHandle: SavedStateHandle,
    navigationTypeMap: NavigationTypeMap,
) : HeaderAndListViewModel<MediaRecommendationsScreen.Entry, MediaAndRecommendationsRecommendation,
        MediaRecommendationEntry, RecommendationSortOption, MediaRecommendationSortFilterController.FilterParams>(
    aniListApi = aniListApi,
    loadingErrorTextRes = Res.string.anime_recommendations_error_loading
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
                settings.mediaFilteringData(),
            ) { mediaListUpdates, recommendationUpdates, _, filteringData ->
                pagingData.mapNotNull {
                    val newMedia = applyMediaFiltering(
                        statuses = mediaListUpdates,
                        ignoreController = ignoreController,
                        filteringData = filteringData,
                        entry = it.media,
                        filterableData = it.media.mediaFilterable,
                        copy = { copy(mediaFilterable = it) },
                    ) ?: return@mapNotNull null
                    val filtered = it.copy(media = newMedia)
                    val recommendationUpdate =
                        recommendationUpdates[it.mediaId to it.recommendation.mediaRecommendation.id.toString()]
                    val userRating = recommendationUpdate?.rating ?: it.userRating
                    filtered.transformIf(userRating != it.userRating) {
                        copy(userRating = userRating)
                    }
                }
            }
        }
}
