package com.thekeeperofpie.artistalleydatabase.anime.review

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.data.MediaDetailsQuery
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class AnimeMediaDetailsReviewsViewModel(
    @Assisted mediaDetailsViewModel: AnimeMediaDetailsViewModel,
) : ViewModel() {

    var reviews by mutableStateOf<ReviewsEntry?>(null)
        private set

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            snapshotFlow { mediaDetailsViewModel.entry.result?.media }
                .mapLatest {
                    ReviewsEntry(
                        reviews = it?.reviews?.nodes?.filterNotNull().orEmpty(),
                        hasMore = it?.reviews?.pageInfo?.hasNextPage ?: true,
                    )
                }
                .collectLatest { reviews = it }
        }
    }

    data class ReviewsEntry(
        val reviews: List<MediaDetailsQuery.Data.Media.Reviews.Node>,
        val hasMore: Boolean,
    )
}
