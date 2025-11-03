package com.thekeeperofpie.artistalleydatabase.anime.recommendations.media

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import artistalleydatabase.modules.anime.recommendations.generated.resources.Res
import artistalleydatabase.modules.anime.recommendations.generated.resources.anime_recommendations_error_loading
import com.anilist.data.fragment.MediaAndRecommendationsRecommendation
import com.anilist.data.fragment.MediaPreview
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.favorites.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorites.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.recommendations.RecommendationStatusController
import com.thekeeperofpie.artistalleydatabase.anime.recommendations.RecommendationToggleHelper
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.transformIf
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilteredViewModel
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapNotNull
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class MediaRecommendationsViewModel<MediaEntry>(
    private val aniListApi: AuthedAniListApi,
    private val mediaListStatusController: MediaListStatusController,
    private val recommendationStatusController: RecommendationStatusController,
    private val ignoreController: IgnoreController,
    private val settings: MediaDataSettings,
    favoritesController: FavoritesController,
    @Assisted val mediaId: String,
    @Assisted mediaRecommendationsSortFilterViewModel: MediaRecommendationsSortFilterViewModel,
    @Assisted private val mediaEntryProvider: MediaEntryProvider<MediaPreview, MediaEntry>,
) : SortFilteredViewModel<MediaRecommendationsScreen.Entry, MediaAndRecommendationsRecommendation,
        MediaRecommendationEntry<MediaEntry>, MediaRecommendationsSortFilterViewModel.FilterParams>(
    loadingErrorTextRes = Res.string.anime_recommendations_error_loading
) {
    val viewer = aniListApi.authedUser
    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)

    val recommendationToggleHelper =
        RecommendationToggleHelper(aniListApi, recommendationStatusController, viewModelScope)

    override val filterParams = mediaRecommendationsSortFilterViewModel.state.filterParams

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
        MediaRecommendationEntry(
            mediaId = mediaId,
            recommendation = item,
            media = mediaEntryProvider.mediaEntry(item.mediaRecommendation),
        )

    override fun entryId(entry: MediaRecommendationEntry<MediaEntry>) =
        entry.recommendation.id.toString()

    override suspend fun initialRequest(
        filterParams: MediaRecommendationsSortFilterViewModel.FilterParams?,
    ) = MediaRecommendationsScreen.Entry(aniListApi.mediaAndRecommendations(mediaId = mediaId))

    override suspend fun request(
        filterParams: MediaRecommendationsSortFilterViewModel.FilterParams?,
    ): Flow<PagingData<MediaAndRecommendationsRecommendation>> =
        AniListPager { page ->
            aniListApi.mediaAndRecommendationsPage(
                mediaId = mediaId,
                sort = filterParams!!.sort.toApiValue(filterParams.sortAscending),
                page = page,
            ).media.recommendations.run { pageInfo to nodes }
        }

    override fun Flow<PagingData<MediaRecommendationEntry<MediaEntry>>>.transformFlow() =
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
                        filterableData = mediaEntryProvider.mediaFilterable(it.media),
                        copy = { mediaEntryProvider.copyMediaEntry(this, it) },
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

    @AssistedInject
    class TypedFactory(
        private val aniListApi: AuthedAniListApi,
        private val mediaListStatusController: MediaListStatusController,
        private val recommendationStatusController: RecommendationStatusController,
        private val ignoreController: IgnoreController,
        private val settings: MediaDataSettings,
        private val favoritesController: FavoritesController,
        @Assisted private val mediaId: String,
        @Assisted private val mediaRecommendationsSortFilterViewModel: MediaRecommendationsSortFilterViewModel,
    ) {
        fun <MediaEntry> create(mediaEntryProvider: MediaEntryProvider<MediaPreview, MediaEntry>) =
            MediaRecommendationsViewModel(
                aniListApi = aniListApi,
                mediaListStatusController = mediaListStatusController,
                recommendationStatusController = recommendationStatusController,
                ignoreController = ignoreController,
                settings = settings,
                favoritesController = favoritesController,
                mediaId = mediaId,
                mediaRecommendationsSortFilterViewModel = mediaRecommendationsSortFilterViewModel,
                mediaEntryProvider = mediaEntryProvider,
            )

        @AssistedFactory
        interface Factory {
            fun create(
                mediaId: String,
                mediaRecommendationsSortFilterViewModel: MediaRecommendationsSortFilterViewModel,
            ): TypedFactory
        }
    }
}
