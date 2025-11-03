package com.thekeeperofpie.artistalleydatabase.anime.reviews

import androidx.lifecycle.SavedStateHandle
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
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapNotNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class ReviewsViewModel<MediaEntry>(
    private val aniListApi: AuthedAniListApi,
    private val settings: MediaDataSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    private val mediaListStatusController: MediaListStatusController,
    private val ignoreController: IgnoreController,
    // TODO: Sharing sort option was lost during ViewModel refactor
    @Assisted private val animeSortFilterViewModel: ReviewsSortFilterViewModel,
    @Assisted private val mangaSortFilterViewModel: ReviewsSortFilterViewModel,
    @Assisted private val mediaEntryProvider: MediaEntryProvider<MediaCompactWithTags, MediaEntry>,
) : ViewModel() {

    val preferredMediaType get() = settings.preferredMediaType.value

    val viewer = aniListApi.authedUser

    val anime = MutableStateFlow(PagingData.empty<ReviewEntry<MediaEntry>>())
    val manga = MutableStateFlow(PagingData.empty<ReviewEntry<MediaEntry>>())

    init {
        collectReviews(MediaType.ANIME, anime, animeSortFilterViewModel)
        collectReviews(MediaType.MANGA, manga, mangaSortFilterViewModel)
    }

    private fun collectReviews(
        type: MediaType,
        reviews: MutableStateFlow<PagingData<ReviewEntry<MediaEntry>>>,
        sortFilterViewModel: ReviewsSortFilterViewModel,
    ) {
        viewModelScope.launch(CustomDispatchers.Main) {
            sortFilterViewModel.state.filterParams
                .flatMapLatest { filterParams ->
                    AniListPager {
                        val result = aniListApi.reviewSearch(
                            sort = filterParams.sort.toApiValue(filterParams.sortAscending),
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
                .flowOn(CustomDispatchers.IO)
                .collectLatest(reviews::emit)
        }
    }

    @AssistedInject
    class TypedFactory(
        private val aniListApi: AuthedAniListApi,
        private val settings: MediaDataSettings,
        private val featureOverrideProvider: FeatureOverrideProvider,
        private val mediaListStatusController: MediaListStatusController,
        private val ignoreController: IgnoreController,
        @Assisted("anime") private val animeSortFilterViewModel: ReviewsSortFilterViewModel,
        @Assisted("manga") private val mangaSortFilterViewModel: ReviewsSortFilterViewModel,
    ) {
        fun <MediaEntry> create(
            mediaEntryProvider: MediaEntryProvider<MediaCompactWithTags, MediaEntry>,
        ) = ReviewsViewModel(
            aniListApi = aniListApi,
            settings = settings,
            featureOverrideProvider = featureOverrideProvider,
            mediaListStatusController = mediaListStatusController,
            ignoreController = ignoreController,
            mediaEntryProvider = mediaEntryProvider,
            animeSortFilterViewModel = animeSortFilterViewModel,
            mangaSortFilterViewModel = mangaSortFilterViewModel,
        )

        @AssistedFactory
        interface Factory {
            fun create(
                @Assisted("anime") animeSortFilterViewModel: ReviewsSortFilterViewModel,
                @Assisted("manga") mangaSortFilterViewModel: ReviewsSortFilterViewModel,
            ): TypedFactory
        }
    }
}
