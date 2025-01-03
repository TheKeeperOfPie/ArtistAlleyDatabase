package com.thekeeperofpie.artistalleydatabase.anime.recommendations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.data.fragment.MediaCompactWithTags
import com.anilist.data.type.RecommendationRating
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.transformIf
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapNotNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class RecommendationsViewModel<MediaEntry>(
    private val aniListApi: AuthedAniListApi,
    private val settings: MediaDataSettings,
    private val mediaListStatusController: MediaListStatusController,
    private val recommendationStatusController: RecommendationStatusController,
    private val ignoreController: IgnoreController,
    @Assisted recommendationsSortFilterViewModel: RecommendationsSortFilterViewModel,
    @Assisted mediaEntryProvider: MediaEntryProvider<MediaCompactWithTags, MediaEntry>,
) : ViewModel() {

    val viewer = aniListApi.authedUser

    val recommendations =
        MutableStateFlow(PagingData.empty<RecommendationEntry<MediaEntry>>())
    val recommendationToggleHelper =
        RecommendationToggleHelper(aniListApi, recommendationStatusController, viewModelScope)

    private val refresh = RefreshFlow()

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            combine(recommendationsSortFilterViewModel.state.filterParams, refresh.updates, ::Pair)
                .flatMapLatest { (filterParams) ->
                    AniListPager {
                        val result = aniListApi.recommendationSearch(
                            sort = filterParams.sort.toApiValue(filterParams.sortAscending),
                            sourceMediaId = filterParams.sourceMediaId,
                            targetMediaId = filterParams.targetMediaId,
                            ratingGreater = filterParams.ratingRange.apiStart,
                            ratingLesser = filterParams.ratingRange.apiEnd,
                            onList = filterParams.onList,
                            page = it,
                        )

                        result.page.pageInfo to result.page.recommendations.filterNotNull()
                    }
                }
                .mapLatest {
                    it.mapOnIO {
                        RecommendationEntry(
                            id = it.id.toString(),
                            user = it.user,
                            media = mediaEntryProvider.mediaEntry(it.media),
                            mediaRecommendation =
                                mediaEntryProvider.mediaEntry(it.mediaRecommendation),
                            data = RecommendationData(
                                it.media.id.toString(),
                                it.mediaRecommendation.id.toString(),
                                it.rating ?: 0,
                                userRating = it.userRating ?: RecommendationRating.NO_RATING,
                            )
                        )
                    }
                }
                .cachedIn(viewModelScope)
                .flatMapLatest { pagingData ->
                    combine(
                        mediaListStatusController.allChanges(),
                        recommendationStatusController.allChanges(),
                        ignoreController.updates(),
                        settings.mediaFilteringData(),
                    ) { mediaStatusUpdates, recommendationUpdates, _, filteringData ->
                        pagingData.mapNotNull {
                            val newMedia = applyMediaFiltering(
                                statuses = mediaStatusUpdates,
                                ignoreController = ignoreController,
                                filteringData = filteringData,
                                entry = it.media,
                                filterableData = mediaEntryProvider.mediaFilterable(it.media),
                                copy = { mediaEntryProvider.copyMediaEntry(this, it) },
                            ) ?: return@mapNotNull null
                            val newMediaRecommendation = applyMediaFiltering(
                                statuses = mediaStatusUpdates,
                                ignoreController = ignoreController,
                                filteringData = filteringData,
                                entry = it.mediaRecommendation,
                                filterableData = mediaEntryProvider.mediaFilterable(it.mediaRecommendation),
                                copy = { mediaEntryProvider.copyMediaEntry(this, it) },
                            ) ?: return@mapNotNull null

                            val filtered = it.copy(
                                media = newMedia,
                                mediaRecommendation = newMediaRecommendation,
                            )
                            val recommendationUpdate =
                                recommendationUpdates[it.data.mediaId to it.data.recommendationMediaId]
                            val userRating = recommendationUpdate?.rating
                                ?: it.data.userRating
                            filtered.transformIf(userRating != it.data.userRating) {
                                copy(
                                    data = data.copy(
                                        userRating = userRating
                                    )
                                )
                            }
                        }
                    }
                }
                .cachedIn(viewModelScope)
                .flowOn(CustomDispatchers.IO)
                .collectLatest(recommendations::emit)
        }
    }

    fun refresh() = refresh.refresh()

    @Inject
    class Factory(
        private val aniListApi: AuthedAniListApi,
        private val settings: MediaDataSettings,
        private val featureOverrideProvider: FeatureOverrideProvider,
        private val mediaListStatusController: MediaListStatusController,
        private val recommendationStatusController: RecommendationStatusController,
        private val ignoreController: IgnoreController,
        @Assisted private val recommendationsSortFilterViewModel: RecommendationsSortFilterViewModel,
    ) {
        fun <MediaEntry> create(
            mediaEntryProvider: MediaEntryProvider<MediaCompactWithTags, MediaEntry>,
        ) = RecommendationsViewModel(
            aniListApi = aniListApi,
            settings = settings,
            mediaListStatusController = mediaListStatusController,
            recommendationStatusController = recommendationStatusController,
            ignoreController = ignoreController,
            recommendationsSortFilterViewModel = recommendationsSortFilterViewModel,
            mediaEntryProvider = mediaEntryProvider,
        )
    }
}
