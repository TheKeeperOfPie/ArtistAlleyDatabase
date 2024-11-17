package com.thekeeperofpie.artistalleydatabase.anime.review

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.data.type.MediaType
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
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class ReviewsViewModel(
    private val aniListApi: AuthedAniListApi,
    private val settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    private val mediaListStatusController: MediaListStatusController,
    private val ignoreController: IgnoreController,
) : ViewModel() {

    val viewer = aniListApi.authedUser

    // TODO: Audit do not load other tabs initially
    var selectedType by mutableStateOf(settings.preferredMediaType.value)

    val anime = MutableStateFlow(PagingData.empty<ReviewEntry>())
    val manga = MutableStateFlow(PagingData.empty<ReviewEntry>())

    // Shares the sort option between the tabs
    val sortSection = ReviewSortFilterController.sortSection()

    private val sortFilterControllerAnime = ReviewSortFilterController(
        scope = viewModelScope,
        aniListApi = aniListApi,
        settings,
        featureOverrideProvider,
        mediaType = MediaType.ANIME,
        sortSection = sortSection,
    )

    private val sortFilterControllerManga = ReviewSortFilterController(
        scope = viewModelScope,
        aniListApi = aniListApi,
        settings,
        featureOverrideProvider,
        mediaType = MediaType.MANGA,
        sortSection = sortSection,
    )

    init {
        collectReviews(MediaType.ANIME, anime, sortFilterControllerAnime)
        collectReviews(MediaType.MANGA, manga, sortFilterControllerManga)
    }

    fun sortFilterController() = if (selectedType == MediaType.ANIME) {
        sortFilterControllerAnime
    } else {
        sortFilterControllerManga
    }

    private fun collectReviews(
        type: MediaType,
        reviews: MutableStateFlow<PagingData<ReviewEntry>>,
        sortFilterController: ReviewSortFilterController,
    ) {
        viewModelScope.launch(CustomDispatchers.Main) {
            sortFilterController.filterParams
                .flatMapLatest { filterParams ->
                    AniListPager {
                        val result = aniListApi.reviewSearch(
                            sort = filterParams.sort
                                .selectedOption(ReviewSortOption.CREATED_AT)
                                .toApiValue(filterParams.sortAscending),
                            mediaType = type,
                            mediaId = filterParams.mediaId,
                            page = it,
                        )

                        result.page.pageInfo to result.page.reviews.filterNotNull()
                    }
                }
                .mapLatest {
                    it.mapOnIO {
                        ReviewEntry(it, MediaCompactWithTagsEntry(it.media))
                    }
                }
                .cachedIn(viewModelScope)
                .flatMapLatest { pagingData ->
                    combine(
                        mediaListStatusController.allChanges(),
                        ignoreController.updates(),
                        settings.mediaFilteringData(),
                    ) { mediaStatusUpdates, _, filteringData ->
                        pagingData.mapNotNull {
                            it.copy(
                                media = applyMediaFiltering(
                                    statuses = mediaStatusUpdates,
                                    ignoreController = ignoreController,
                                    filteringData = filteringData,
                                    entry = it.media,
                                    filterableData = it.media.mediaFilterable,
                                    copy = { copy(mediaFilterable = it) },
                                ) ?: return@mapNotNull null
                            )
                        }
                    }
                }
                .cachedIn(viewModelScope)
                .flowOn(CustomDispatchers.IO)
                .collectLatest(reviews::emit)
        }
    }
}
