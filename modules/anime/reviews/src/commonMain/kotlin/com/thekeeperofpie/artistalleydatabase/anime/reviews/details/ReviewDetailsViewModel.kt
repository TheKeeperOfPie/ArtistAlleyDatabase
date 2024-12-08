package com.thekeeperofpie.artistalleydatabase.anime.reviews.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.anime.reviews.generated.resources.Res
import artistalleydatabase.modules.anime.reviews.generated.resources.anime_review_details_error_rating
import artistalleydatabase.modules.anime.reviews.generated.resources.anime_reviews_error_loading_details
import com.anilist.data.type.ReviewRating
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.favorites.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorites.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.reviews.ReviewDestinations
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.flowForRefreshableContent
import com.thekeeperofpie.artistalleydatabase.utils_compose.foldPreviousResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class ReviewDetailsViewModel(
    private val aniListApi: AuthedAniListApi,
    favoritesController: FavoritesController,
    @Assisted savedStateHandle: SavedStateHandle,
    navigationTypeMap: NavigationTypeMap,
) : ViewModel() {

    private val destination =
        savedStateHandle.toDestination<ReviewDestinations.ReviewDetails>(navigationTypeMap)
    private val reviewId = destination.reviewId

    val viewer = aniListApi.authedUser

    private val userRatingUpdates = MutableSharedFlow<ReviewRating>(1, 1)

    // TODO: Refresh isn't exposed to the user
    private val refresh = RefreshFlow()

    var entry =
        flowForRefreshableContent(refresh, Res.string.anime_reviews_error_loading_details) {
            flowFromSuspend {
                aniListApi.reviewDetails(reviewId)
                    .let(ReviewDetailsScreen::Entry)
            }
        }
            .flowOn(CustomDispatchers.Companion.IO)
            .stateIn(viewModelScope, SharingStarted.Companion.Eagerly, LoadingResult.Companion.loading())

    val userRating = entry.flatMapLatest {
        flow {
            emit(
                LoadingResult(
                    loading = true,
                    success = false,
                    result = it.result?.review?.userRating
                )
            )
            userRatingUpdates
                .flatMapLatest {
                    flow {
                        emit(LoadingResult(loading = true, success = false, result = it))
                        emit(LoadingResult.Companion.success(aniListApi.rateReview(reviewId, it)))
                    }
                }
                .collect(::emit)
        }
    }
        .catch { emit(LoadingResult.Companion.error(Res.string.anime_review_details_error_rating, it)) }
        .foldPreviousResult()
        .stateIn(viewModelScope, SharingStarted.Companion.Eagerly, LoadingResult.Companion.loading())

    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)

    init {
        favoritesToggleHelper.initializeTracking(
            scope = viewModelScope,
            entryToId = { it.review.media?.id.toString() },
            entry = { entry.map { it.result } },
            entryToType = { it.review.media?.type.toFavoriteType() },
            entryToFavorite = { it.review.media?.isFavourite == true },
        )
    }

    fun onUserRating(upvote: Boolean?) {
        val rating = when (upvote) {
            true -> ReviewRating.UP_VOTE
            false -> ReviewRating.DOWN_VOTE
            null -> ReviewRating.NO_VOTE
        }
        userRatingUpdates.tryEmit(rating)
    }
}
