package com.thekeeperofpie.artistalleydatabase.anime.review.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_review_details_error_rating
import artistalleydatabase.modules.anime.generated.resources.anime_reviews_error_loading_details
import com.anilist.type.ReviewRating
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.StringResource

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class ReviewDetailsViewModel(
    private val aniListApi: AuthedAniListApi,
    favoritesController: FavoritesController,
    @Assisted savedStateHandle: SavedStateHandle,
    navigationTypeMap: NavigationTypeMap,
) : ViewModel() {

    private val destination = savedStateHandle.toDestination<AnimeDestination.ReviewDetails>(navigationTypeMap)
    private val reviewId = destination.reviewId

    val viewer = aniListApi.authedUser
    var loading by mutableStateOf(true)
        private set

    var entry by mutableStateOf<ReviewDetailsScreen.Entry?>(null)

    var error by mutableStateOf<Pair<StringResource, Throwable?>?>(null)

    var userRating by mutableStateOf(ReviewRating.NO_VOTE)
    private val userRatingUpdates = MutableSharedFlow<ReviewRating>(1, 1)

    // TODO: Refresh isn't exposed to the user
    private val refresh = RefreshFlow()

    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            refresh.updates
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
                        error = Res.string.anime_reviews_error_loading_details to
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
                            error = Res.string.anime_review_details_error_rating to
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
