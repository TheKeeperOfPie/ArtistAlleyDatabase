package com.thekeeperofpie.artistalleydatabase.anime.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.data.MediaDetailsQuery
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
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
}
