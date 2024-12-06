package com.thekeeperofpie.artistalleydatabase.anime.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.data.fragment.MediaCompactWithTags
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
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
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class ReviewsViewModel<MediaEntry>(
    private val aniListApi: AuthedAniListApi,
    private val settings: MediaDataSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    private val mediaListStatusController: MediaListStatusController,
    private val ignoreController: IgnoreController,
    @Assisted private val mediaEntryProvider: MediaEntryProvider<MediaCompactWithTags, MediaEntry>,
    @Assisted private val mediaDetailsRoute: MediaDetailsRoute,
) : ViewModel() {

    val preferredMediaType get() = settings.preferredMediaType.value

    val viewer = aniListApi.authedUser

    val anime = MutableStateFlow(PagingData.Companion.empty<ReviewEntry<MediaEntry>>())
    val manga = MutableStateFlow(PagingData.Companion.empty<ReviewEntry<MediaEntry>>())

    // Shares the sort option between the tabs
    val sortSection = ReviewSortFilterController.sortSection()

    val sortFilterControllerAnime = ReviewSortFilterController(
        scope = viewModelScope,
        aniListApi = aniListApi,
        settings,
        featureOverrideProvider,
        mediaType = MediaType.ANIME,
        sortSection = sortSection,
        mediaDetailsRoute = mediaDetailsRoute,
    )

    val sortFilterControllerManga = ReviewSortFilterController(
        scope = viewModelScope,
        aniListApi = aniListApi,
        settings,
        featureOverrideProvider,
        mediaType = MediaType.MANGA,
        sortSection = sortSection,
        mediaDetailsRoute = mediaDetailsRoute,
    )

    init {
        collectReviews(MediaType.ANIME, anime, sortFilterControllerAnime)
        collectReviews(MediaType.MANGA, manga, sortFilterControllerManga)
    }

    private fun collectReviews(
        type: MediaType,
        reviews: MutableStateFlow<PagingData<ReviewEntry<MediaEntry>>>,
        sortFilterController: ReviewSortFilterController,
    ) {
        viewModelScope.launch(CustomDispatchers.Companion.Main) {
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
                        ReviewEntry(it, mediaEntryProvider.mediaEntry(it.media))
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
                                    filterableData = mediaEntryProvider.mediaFilterable(it.media),
                                    copy = { mediaEntryProvider.copyMediaEntry(this, it) },
                                ) ?: return@mapNotNull null
                            )
                        }
                    }
                }
                .cachedIn(viewModelScope)
                .flowOn(CustomDispatchers.Companion.IO)
                .collectLatest(reviews::emit)
        }
    }

    @Inject
    class Factory(
        private val aniListApi: AuthedAniListApi,
        private val settings: MediaDataSettings,
        private val featureOverrideProvider: FeatureOverrideProvider,
        private val mediaListStatusController: MediaListStatusController,
        private val ignoreController: IgnoreController,
    ) {
        fun <MediaEntry> create(
            mediaEntryProvider: MediaEntryProvider<MediaCompactWithTags, MediaEntry>,
            mediaDetailsRoute: MediaDetailsRoute,
        ) = ReviewsViewModel(
            aniListApi = aniListApi,
            settings = settings,
            featureOverrideProvider = featureOverrideProvider,
            mediaListStatusController = mediaListStatusController,
            ignoreController = ignoreController,
            mediaEntryProvider = mediaEntryProvider,
            mediaDetailsRoute = mediaDetailsRoute,
        )
    }
}
