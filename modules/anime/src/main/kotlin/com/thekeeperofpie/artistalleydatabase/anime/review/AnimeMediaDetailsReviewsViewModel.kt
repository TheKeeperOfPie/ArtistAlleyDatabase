package com.thekeeperofpie.artistalleydatabase.anime.review

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.MediaDetails2Query
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.orEmpty
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnimeMediaDetailsReviewsViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
) : ViewModel() {

    var reviews by mutableStateOf<ReviewsEntry?>(null)
        private set

    private var initialized = false

    fun initialize(mediaDetailsViewModel: AnimeMediaDetailsViewModel) {
        if (initialized) return
        initialized = true

        viewModelScope.launch(CustomDispatchers.Main) {
            snapshotFlow { mediaDetailsViewModel.entry2.result?.media }
                .mapLatest {
                    ReviewsEntry(
                        reviews = it?.reviews?.nodes?.filterNotNull()?.toImmutableList().orEmpty(),
                        hasMore = it?.reviews?.pageInfo?.hasNextPage ?: true,
                    )
                }
                .collectLatest { reviews = it }
        }
    }

    data class ReviewsEntry(
        val reviews: ImmutableList<MediaDetails2Query.Data.Media.Reviews.Node>,
        val hasMore: Boolean,
    )
}
