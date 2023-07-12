package com.thekeeperofpie.artistalleydatabase.anime.review

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.fragment.MediaAndReviewsReview
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListPagingSource
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.utils.enforceUniqueIntIds
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.compose.filter.selectedOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReviewsViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi
) : ViewModel() {

    var mediaId by mutableStateOf("")
        private set

    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()

    var entry by mutableStateOf<ReviewsScreen.Entry?>(null)
        private set

    var reviews = MutableStateFlow(PagingData.empty<MediaAndReviewsReview>())
        private set

    var error by mutableStateOf<Pair<Int, Throwable?>?>(null)

    // TODO: Actually expose sort options?
    var sortOptions by mutableStateOf(
        SortEntry.options(ReviewsSortOption::class).map {
            if (it.value == ReviewsSortOption.RATING) it.copy(state = FilterIncludeExcludeState.INCLUDE) else it
        })
        private set

    var sortAscending by mutableStateOf(false)

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            snapshotFlow {
                mediaId.takeIf { it.isNotEmpty() }?.let {
                    Triple(
                        it,
                        sortOptions.selectedOption(ReviewsSortOption.RATING),
                        sortAscending,
                    )
                }
            }
                .filterNotNull()
                .flowOn(CustomDispatchers.Main)
                .mapLatest { (mediaId, sortOption, sortAscending) ->
                    aniListApi
                        .mediaAndReviews(
                            mediaId = mediaId,
                            sort = sortOption.toApiValue(sortAscending)
                        )
                        .let(ReviewsScreen::Entry)
                        .let(Result.Companion::success)
                }
                .catch { emit(Result.failure(it)) }
                .collectLatest {
                    withContext(CustomDispatchers.Main) {
                        if (it.isFailure) {
                            error = R.string.anime_reviews_error_loading to it.exceptionOrNull()
                        } else {
                            entry = it.getOrThrow()
                        }
                    }
                }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            snapshotFlow { entry }
                .filterNotNull()
                .flatMapLatest {
                    snapshotFlow {
                        Triple(
                            it,
                            sortOptions.selectedOption(ReviewsSortOption.RATING),
                            sortAscending,
                        )
                    }
                }
                .flowOn(CustomDispatchers.Main)
                .flatMapLatest { (entry, sortOption, sortAscending) ->
                    Pager(config = PagingConfig(10)) {
                        AniListPagingSource {
                            if (it == 1) {
                                val result = entry.media.reviews
                                result?.pageInfo to result?.nodes?.filterNotNull().orEmpty()
                            } else {
                                val result = aniListApi.mediaAndReviewsPage(
                                    mediaId = entry.media.id.toString(),
                                    sort = sortOption.toApiValue(sortAscending),
                                    page = it
                                ).media.reviews
                                result?.pageInfo to result?.nodes?.filterNotNull().orEmpty()
                            }
                        }
                    }.flow
                }
                .enforceUniqueIntIds { it.id }
                .cachedIn(viewModelScope)
                .collectLatest(reviews::emit)
        }
    }

    fun initialize(mediaId: String) {
        this.mediaId = mediaId
    }
}
