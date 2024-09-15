package com.thekeeperofpie.artistalleydatabase.anime.recommendation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.type.RecommendationRating
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaCompactWithTagsEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.transformIf
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.selectedOption
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
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class RecommendationsViewModel(
    private val aniListApi: AuthedAniListApi,
    private val settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    private val mediaListStatusController: MediaListStatusController,
    private val recommendationStatusController: RecommendationStatusController,
    private val ignoreController: IgnoreController,
) : ViewModel() {

    val viewer = aniListApi.authedUser

    val sortFilterController = RecommendationSortFilterController(
        scope = viewModelScope,
        aniListApi = aniListApi,
        settings,
        featureOverrideProvider,
    )

    val recommendations = MutableStateFlow(PagingData.empty<RecommendationEntry>())
    val recommendationToggleHelper =
        RecommendationToggleHelper(aniListApi, recommendationStatusController, viewModelScope)

    private val refresh = MutableStateFlow(-1L)

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            sortFilterController.filterParams
                .flatMapLatest { filterParams ->
                    AniListPager {
                        val result = aniListApi.recommendationSearch(
                            sort = filterParams.sort
                                .selectedOption(RecommendationSortOption.ID)
                                .toApiValue(filterParams.sortAscending),
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
                            media = MediaCompactWithTagsEntry(it.media),
                            mediaRecommendation = MediaCompactWithTagsEntry(it.mediaRecommendation),
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
                                filterableData = it.media.mediaFilterable,
                                copy = { copy(mediaFilterable = it) },
                            ) ?: return@mapNotNull null
                            val newMediaRecommendation = applyMediaFiltering(
                                statuses = mediaStatusUpdates,
                                ignoreController = ignoreController,
                                filteringData = filteringData,
                                entry = it.mediaRecommendation,
                                filterableData = it.mediaRecommendation.mediaFilterable,
                                copy = { copy(mediaFilterable = it) }
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

    fun refresh() {
        refresh.value = Clock.System.now().toEpochMilliseconds()
    }
}
