package com.thekeeperofpie.artistalleydatabase.anime.reviews

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.anilist.data.fragment.MediaCompactWithTags
import com.anilist.data.fragment.MediaHeaderData
import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.reviews.details.ReviewDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.reviews.media.MediaReviewsScreen
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserRoute
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.ImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementComposable
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import kotlinx.serialization.Serializable

object ReviewDestinations {

    @Serializable
    data object Reviews : NavDestination

    @Serializable
    data class ReviewDetails(
        val reviewId: String,
        val sharedTransitionScopeKey: String? = null,
        val headerParams: MediaHeaderParams? = null,
    ) : NavDestination

    @Serializable
    data class MediaReviews(
        val mediaId: String,
        val sharedElementKey: String? = null,
        val headerParams: MediaHeaderParams? = null,
    ) : NavDestination

    fun <MediaEntry : Any> addToGraph(
        navGraphBuilder: NavGraphBuilder,
        navigationTypeMap: NavigationTypeMap,
        component: ReviewsComponent,
        mediaDetailsRoute: MediaDetailsRoute,
        userRoute: UserRoute,
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        mediaEntryProvider: MediaEntryProvider<MediaCompactWithTags, MediaEntry>,
        mediaTitle: @Composable (MediaEntry) -> String?,
        mediaHeaderParams: (MediaEntry, title: String?, ImageState) -> MediaHeaderParams,
        mediaImageUri: (MediaEntry?) -> String?,
        mediaRow: @Composable (
            MediaEntry?,
            viewer: AniListViewer?,
            coverImageState: CoilImageState,
            onClickListEdit: (MediaNavigationData) -> Unit,
            Modifier,
        ) -> Unit,
        mediaHeader: @Composable (
            progress: Float,
            viewer: AniListViewer?,
            MediaHeaderData?,
            MediaHeaderValues,
            titlesUnique: List<String>?,
            onFavoriteChanged: (Boolean) -> Unit,
        ) -> Unit,
    ) {
        navGraphBuilder.sharedElementComposable<Reviews>(navigationTypeMap) {
            val animeSortFilterViewModel = viewModel {
                component.reviewsSortFilterViewModel(
                    createSavedStateHandle(),
                    mediaDetailsRoute,
                    MediaType.ANIME,
                )
            }
            val mangaSortFilterViewModel = viewModel {
                component.reviewsSortFilterViewModel(
                    createSavedStateHandle(),
                    mediaDetailsRoute,
                    MediaType.MANGA,
                )
            }
            val viewModel = viewModel {
                component.reviewsViewModelFactory(
                    animeSortFilterViewModel,
                    mangaSortFilterViewModel,
                ).create(mediaEntryProvider)
            }
            val viewer by viewModel.viewer.collectAsState()
            val animeSelectedMedia by animeSortFilterViewModel.media.collectAsStateWithLifecycle()
            val mangaSelectedMedia by mangaSortFilterViewModel.media.collectAsStateWithLifecycle()
            ReviewsScreen(
                mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
                upIconOption = UpIconOption.Back(LocalNavigationController.current),
                sortFilterStateAnime = animeSortFilterViewModel.state,
                sortFilterStateManga = mangaSortFilterViewModel.state,
                selectedMediaAnime = { animeSelectedMedia },
                selectedMediaManga = { mangaSelectedMedia },
                anime = viewModel.anime,
                manga = viewModel.manga,
                preferredMediaType = viewModel.preferredMediaType,
                userRoute = userRoute,
                mediaTitle = mediaTitle,
                mediaHeaderParams = mediaHeaderParams,
                mediaImageUri = mediaImageUri,
                mediaRow = { entry, coverImageState, onClickListEdit, modifier ->
                    mediaRow(entry, viewer, coverImageState, onClickListEdit, modifier)
                }
            )
        }

        navGraphBuilder.sharedElementComposable<ReviewDetails>(
            navigationTypeMap = navigationTypeMap,
            deepLinks = listOf(
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/review/{reviewId}" },
                navDeepLink {
                    uriPattern = "${AniListUtils.ANILIST_BASE_URL}/review/{reviewId}/.*"
                },
            ),
        ) {
            val viewModel =
                viewModel { component.reviewDetailsViewModel(createSavedStateHandle()) }
            val destination = it.toRoute<ReviewDetails>()
            val entry by viewModel.entry.collectAsState()
            val headerValues = MediaHeaderValues(
                params = destination.headerParams,
                media = { entry.result?.review?.media },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            SharedTransitionKeyScope(destination.sharedTransitionScopeKey) {
                val viewer by viewModel.viewer.collectAsState()
                val userRating by viewModel.userRating.collectAsState()
                ReviewDetailsScreen(
                    viewer = { viewer },
                    entry = { entry },
                    userRating = { userRating },
                    onUserRating = viewModel::onUserRating,
                    mediaHeader = {
                        val entry = entry
                        val media = entry.result?.review?.media
                        val titlesUnique = entry.result?.titlesUnique
                        val onFavoriteChanged: (Boolean) -> Unit = {
                            viewModel.favoritesToggleHelper.set(
                                headerValues.type.toFavoriteType(),
                                media?.id.toString(),
                                it,
                            )
                        }
                        mediaHeader(
                            it,
                            viewer,
                            media,
                            headerValues,
                            titlesUnique,
                            onFavoriteChanged,
                        )
                    },
                    userRoute = userRoute,
                )
            }
        }

        navGraphBuilder.sharedElementComposable<MediaReviews>(
            navigationTypeMap = navigationTypeMap,
        ) {
            val mediaReviewsSortFilterViewModel = viewModel {
                component.mediaReviewsSortFilterViewModel(createSavedStateHandle())
            }
            val viewModel = viewModel {
                component.mediaReviewsViewModel(
                    createSavedStateHandle(),
                    mediaReviewsSortFilterViewModel,
                )
            }
            val destination = it.toRoute<MediaReviews>()
            val headerValues = MediaHeaderValues(
                params = destination.headerParams,
                media = { viewModel.entry.result?.media },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            val coverImageState = rememberCoilImageState(headerValues.coverImage)
            val viewer by viewModel.viewer.collectAsState()
            MediaReviewsScreen(
                mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
                coverImageState = coverImageState,
                userRoute = userRoute,
                onRefresh = viewModel::refresh,
                entry = viewModel.entry,
                items = viewModel.items.collectAsLazyPagingItems(),
                sortFilterState = mediaReviewsSortFilterViewModel.state,
                mediaHeader = {
                    val entry = viewModel.entry
                    val media = entry.result?.media
                    val titlesUnique = entry.result?.titlesUnique
                    val onFavoriteChanged: (Boolean) -> Unit = {
                        viewModel.favoritesToggleHelper.set(
                            headerValues.type.toFavoriteType(),
                            media?.id.toString(),
                            it,
                        )
                    }
                    mediaHeader(
                        it,
                        viewer,
                        media,
                        headerValues,
                        titlesUnique,
                        onFavoriteChanged,
                    )
                },
                favorite = { viewModel.favoritesToggleHelper.favorite },
            )
        }
    }
}
