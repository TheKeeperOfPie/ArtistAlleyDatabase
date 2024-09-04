package com.thekeeperofpie.artistalleydatabase.anime.review.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.type.ReviewRating
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReviewDetailsViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
    favoritesController: FavoritesController,
    savedStateHandle: SavedStateHandle,
    navigationTypeMap: NavigationTypeMap,
) : ViewModel() {

    private val destination = savedStateHandle.toDestination<AnimeDestination.ReviewDetails>(navigationTypeMap)
    private val reviewId = destination.reviewId

    val viewer = aniListApi.authedUser
    var loading by mutableStateOf(true)
        private set

    var entry by mutableStateOf<ReviewDetailsScreen.Entry?>(null)

    var error by mutableStateOf<Pair<Int, Throwable?>?>(null)

    var userRating by mutableStateOf(ReviewRating.NO_VOTE)
    private val userRatingUpdates = MutableSharedFlow<ReviewRating>(1, 1)

    // TODO: Refresh isn't exposed to the user
    private val refresh = MutableStateFlow(-1L)

    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            refresh
                .mapLatest {
                    aniListApi.reviewDetails(reviewId)
                        .let(ReviewDetailsScreen::Entry)
                        .let(Result.Companion::success)
                }
                .flowOn(CustomDispatchers.IO)
                .catch { emit(Result.failure(it)) }
                .collectLatest {
                    loading = false
                    if (it.isFailure) {
                        error = R.string.anime_reviews_error_loading_details to
                                it.exceptionOrNull()
                    } else {
                        entry = it.getOrThrow().also {
                            it.review.userRating?.let {
                                userRating = it
                            }
                        }
                    }
                }
        }

        viewModelScope.launch(CustomDispatchers.Main) {
            userRatingUpdates
                .mapLatest { Result.success(aniListApi.rateReview(reviewId, it)) }
                .catch { emit(Result.failure(it)) }
                .collectLatest {
                    withContext(CustomDispatchers.Main) {
                        if (it.isFailure) {
                            error = R.string.anime_review_details_error_rating to
                                    it.exceptionOrNull()
                        } else {
                            userRating = it.getOrThrow()
                        }
                    }
                }
        }
        favoritesToggleHelper.initializeTracking(
            scope = viewModelScope,
            entryToId = { it.review.media?.id.toString() },
            entry = { snapshotFlow { entry } },
            entryToType = { it.review.media?.type.toFavoriteType() },
            entryToFavorite = { it.review.media?.isFavourite ?: false },
        )
    }

    fun onUserRating(upvote: Boolean?) {
        userRating = when (upvote) {
            true -> ReviewRating.UP_VOTE
            false -> ReviewRating.DOWN_VOTE
            null -> ReviewRating.NO_VOTE
        }
        userRatingUpdates.tryEmit(userRating)
    }
}
