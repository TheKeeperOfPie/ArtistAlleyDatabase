package com.thekeeperofpie.artistalleydatabase.anime.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.data.MediaDetailsQuery
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
@AssistedInject
class AnimeMediaDetailsReviewsViewModel(
    @Assisted reviews: Flow<MediaDetailsQuery.Data.Media.Reviews?>,
) : ViewModel() {
    val reviewsEntry = reviews
        .mapLatest {
            ReviewsEntry(
                reviews = it?.nodes?.filterNotNull().orEmpty(),
                hasMore = it?.pageInfo?.hasNextPage != false,
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    @AssistedFactory
    interface Factory {
        fun create(
            reviews: Flow<MediaDetailsQuery.Data.Media.Reviews?>,
        ): AnimeMediaDetailsReviewsViewModel
    }
}
