package com.thekeeperofpie.artistalleydatabase.anime

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_characters_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_staff_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_view_all_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_user_favorite_anime_user
import artistalleydatabase.modules.anime.generated.resources.anime_user_favorite_anime_you
import artistalleydatabase.modules.anime.generated.resources.anime_user_favorite_characters_user
import artistalleydatabase.modules.anime.generated.resources.anime_user_favorite_characters_you
import artistalleydatabase.modules.anime.generated.resources.anime_user_favorite_manga_user
import artistalleydatabase.modules.anime.generated.resources.anime_user_favorite_manga_you
import artistalleydatabase.modules.anime.generated.resources.anime_user_favorite_staff_user
import artistalleydatabase.modules.anime.generated.resources.anime_user_favorite_staff_you
import artistalleydatabase.modules.anime.generated.resources.anime_user_favorite_studios_user
import artistalleydatabase.modules.anime.generated.resources.anime_user_favorite_studios_you
import artistalleydatabase.modules.anime.generated.resources.anime_user_followers_user
import artistalleydatabase.modules.anime.generated.resources.anime_user_followers_you
import artistalleydatabase.modules.anime.generated.resources.anime_user_following_user
import artistalleydatabase.modules.anime.generated.resources.anime_user_following_you
import com.anilist.data.type.MediaListStatus
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.activity.AnimeActivityComposables
import com.thekeeperofpie.artistalleydatabase.anime.activity.AnimeActivityScreen
import com.thekeeperofpie.artistalleydatabase.anime.activity.AnimeMediaDetailsActivityViewModel
import com.thekeeperofpie.artistalleydatabase.anime.activity.activitiesSection
import com.thekeeperofpie.artistalleydatabase.anime.activity.details.ActivityDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.character.charactersSection
import com.thekeeperofpie.artistalleydatabase.anime.character.details.CharacterDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.character.media.CharacterMediasScreen
import com.thekeeperofpie.artistalleydatabase.anime.forum.ForumComposables
import com.thekeeperofpie.artistalleydatabase.anime.forum.ForumRootScreen
import com.thekeeperofpie.artistalleydatabase.anime.forum.ForumSearchScreen
import com.thekeeperofpie.artistalleydatabase.anime.forum.forumThreadsSection
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadScreen
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.comment.ForumThreadCommentTreeScreen
import com.thekeeperofpie.artistalleydatabase.anime.history.MediaHistoryScreen
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeIgnoreScreen
import com.thekeeperofpie.artistalleydatabase.anime.list.AnimeUserListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaCompactWithTagsEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeader
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.activity.MediaActivitiesScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.characters.MediaCharactersScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.data.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.notifications.NotificationsScreen
import com.thekeeperofpie.artistalleydatabase.anime.recommendations.RecommendationComposables
import com.thekeeperofpie.artistalleydatabase.anime.recommendations.RecommendationsScreen
import com.thekeeperofpie.artistalleydatabase.anime.recommendations.media.MediaRecommendationsScreen
import com.thekeeperofpie.artistalleydatabase.anime.recommendations.recommendationsSection
import com.thekeeperofpie.artistalleydatabase.anime.review.ReviewComposables
import com.thekeeperofpie.artistalleydatabase.anime.review.ReviewsScreen
import com.thekeeperofpie.artistalleydatabase.anime.review.details.ReviewDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.review.media.MediaReviewsScreen
import com.thekeeperofpie.artistalleydatabase.anime.review.reviewsSection
import com.thekeeperofpie.artistalleydatabase.anime.schedule.AiringScheduleScreen
import com.thekeeperofpie.artistalleydatabase.anime.search.MediaSearchScreen
import com.thekeeperofpie.artistalleydatabase.anime.seasonal.SeasonalScreen
import com.thekeeperofpie.artistalleydatabase.anime.songs.AnimeSongComposables
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.staff.character.StaffCharactersScreen
import com.thekeeperofpie.artistalleydatabase.anime.staff.staffSection
import com.thekeeperofpie.artistalleydatabase.anime.studio.StudioMediasScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.UserHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.user.UserHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.user.favorite.UserFavoriteCharactersScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.favorite.UserFavoriteMediaScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.favorite.UserFavoriteStaffScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.favorite.UserFavoriteStudiosScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.follow.UserListScreen
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryComponent
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryNavigator
import com.thekeeperofpie.artistalleydatabase.cds.cdsSection
import com.thekeeperofpie.artistalleydatabase.cds.grid.CdEntryGridModel
import com.thekeeperofpie.artistalleydatabase.monetization.UnlockScreen
import com.thekeeperofpie.artistalleydatabase.utils_compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElementComposable
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementComposable
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object AnimeNavigator {

    fun initialize(
        navHostController: NavHostController,
        navGraphBuilder: NavGraphBuilder,
        navigationTypeMap: NavigationTypeMap,
        upIconOption: UpIconOption?,
        onClickAuth: () -> Unit,
        onClickSettings: () -> Unit,
        onClickShowLastCrash: () -> Unit,
        animeComponent: AnimeComponent,
        cdEntryComponent: CdEntryComponent,
    ) {
        navGraphBuilder.sharedElementComposable(
            route = AnimeNavDestinations.HOME.id,
            deepLinks = listOf(navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/" }),
        ) {
            val viewModel = viewModel { animeComponent.animeRootViewModel() }
            AnimeRootScreen(
                upIconOption = upIconOption,
                viewModel = viewModel,
                onClickAuth = onClickAuth,
                onSubmitAuthToken = viewModel::onSubmitAuthToken,
                onClickSettings = onClickSettings,
                onClickShowLastCrash = onClickShowLastCrash,
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.SearchMedia>(navigationTypeMap) {
            val destination = it.toRoute<AnimeDestination.SearchMedia>()
            MediaSearchScreen(
                title = destination.title,
                upIconOption = UpIconOption.Back(navHostController),
                tagId = destination.tagId,
                genre = destination.genre,
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.UserList>(
            navigationTypeMap = navigationTypeMap,
        ) {
            val destination = it.toRoute<AnimeDestination.UserList>()
            UserMediaListScreen(
                userId = destination.userId,
                userName = destination.userName,
                mediaType = destination.mediaType,
                upIconOption = UpIconOption.Back(navHostController),
                mediaListStatus = destination.mediaListStatus,
                scrollStateSaver = ScrollStateSaver(),
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.MediaDetails>(
            navigationTypeMap = navigationTypeMap,
            deepLinks = listOf(
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/anime/{mediaId}" },
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/anime/{mediaId}/.*" },
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/manga/{mediaId}" },
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/manga/{mediaId}/.*" },
            ),
            enterTransition = {
                val destination = targetState.toRoute<AnimeDestination.MediaDetails>()
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    // If there's a shared element, delay the slide up to allow
                    // previous screen's exit animations to run
                    animationSpec = if (destination.sharedTransitionKey == null) {
                        spring(visibilityThreshold = IntOffset.VisibilityThreshold)
                    } else {
                        tween(delayMillis = 200, easing = EaseOutExpo)
                    },
                )
            }
        ) {
            val destination = it.toRoute<AnimeDestination.MediaDetails>()
            val mediaDetailsViewModel =
                viewModel { animeComponent.animeMediaDetailsViewModel(createSavedStateHandle()) }
            val headerValues = MediaHeaderValues(
                params = destination.headerParams,
                media = { mediaDetailsViewModel.state.mediaEntry.result?.media },
                favoriteUpdate = { mediaDetailsViewModel.favoritesToggleHelper.favorite },
            )

            val charactersViewModel = viewModel {
                animeComponent.animeCharactersViewModel(
                    createSavedStateHandle(),
                    mediaDetailsViewModel,
                )
            }
            val charactersDeferred =
                charactersViewModel.charactersDeferred.collectAsLazyPagingItems()

            val staffViewModel = viewModel {
                animeComponent.animeStaffViewModel(
                    createSavedStateHandle(),
                    mediaDetailsViewModel,
                )
            }
            val staff = staffViewModel.staff.collectAsLazyPagingItems()

            val songsViewModel =
                viewModel { animeComponent.animeSongsViewModel(mediaDetailsViewModel) }
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                lifecycleOwner.lifecycle.addObserver(songsViewModel)
                onDispose { lifecycleOwner.lifecycle.removeObserver(songsViewModel) }
            }

            DisposableEffect(lifecycleOwner) {
                onDispose { songsViewModel.animeSongsCollapseAll() }
            }

            val cdsViewModel =
                viewModel { cdEntryComponent.cdsFromMediaViewModel(createSavedStateHandle()) }

            val viewer by mediaDetailsViewModel.viewer.collectAsState()

            @Suppress("UNCHECKED_CAST")
            val recommendationsViewModel = viewModel {
                animeComponent
                    .animeMediaDetailsRecommendationsViewModelFactory(createSavedStateHandle())
                    .create(mediaDetailsViewModel.recommendations(), mediaDetailsViewModel)
            }

            val activitiesViewModel = viewModel {
                animeComponent.animeMediaDetailsActivityViewModel(mediaDetailsViewModel)
            }
            val activities = activitiesViewModel.activities
            val (activityTab, onActivityTabChange) = rememberSaveable(viewer, activities) {
                mutableStateOf(
                    if (activities?.following.isNullOrEmpty()) {
                        AnimeMediaDetailsActivityViewModel.ActivityTab.GLOBAL
                    } else {
                        AnimeMediaDetailsActivityViewModel.ActivityTab.FOLLOWING
                    }
                )
            }

            val forumThreadsViewModel = viewModel {
                animeComponent.animeForumThreadsViewModel(
                    createSavedStateHandle(),
                    mediaDetailsViewModel,
                )
            }

            val reviewsViewModel = viewModel {
                animeComponent.animeMediaDetailsReviewsViewModel(mediaDetailsViewModel)
            }

            val navigationCallback = LocalNavigationCallback.current

            val sharedTransitionKey = destination.sharedTransitionKey
            val mediaTitle = mediaDetailsViewModel.state.mediaEntry.result?.media?.title
                ?.primaryTitle()
            val coverImageState = rememberCoilImageState(headerValues.coverImage)
            val media = mediaDetailsViewModel.state.mediaEntry.result?.media
            fun mediaHeaderParams() = MediaHeaderParams(
                title = mediaTitle,
                coverImage = coverImageState.toImageState(),
                media = media,
                favorite = mediaDetailsViewModel.favoritesToggleHelper.favorite
                    ?: media?.isFavourite,
            )

            val recommendations by recommendationsViewModel.recommendations.collectAsState()
            AnimeMediaDetailsScreen(
                viewModel = mediaDetailsViewModel,
                upIconOption = UpIconOption.Back(navHostController),
                headerValues = headerValues,
                sharedTransitionKey = sharedTransitionKey,
                coverImageState = coverImageState,
                charactersCount = {
                    charactersDeferred.itemCount
                        .coerceAtLeast(charactersViewModel.charactersInitial.size)
                },
                charactersSection = { entry ->
                    charactersSection(
                        titleRes = Res.string.anime_media_details_characters_label,
                        charactersInitial = charactersViewModel.charactersInitial,
                        charactersDeferred = { charactersDeferred },
                        mediaId = entry.mediaId,
                        mediaHeaderParams = mediaHeaderParams(),
                        viewAllContentDescriptionTextRes = Res.string.anime_media_details_view_all_content_description,
                    )
                },
                staffCount = { staff.itemCount },
                staffSection = {
                    staffSection(
                        titleRes = Res.string.anime_media_details_staff_label,
                        staffList = staff,
                    )
                },
                songsSectionMetadata = if (!songsViewModel.enabled) null else AnimeMediaDetailsScreen.SectionIndexInfo.SectionMetadata.ListSection(
                    items = songsViewModel.animeSongs?.entries,
                    aboveFold = AnimeSongComposables.SONGS_ABOVE_FOLD,
                    hasMore = false,
                ),
                songsSection = { expanded, onExpandedChange ->
                    AnimeSongComposables.songsSection(
                        viewModel = songsViewModel,
                        songsExpanded = expanded,
                        onSongsExpandedChange = onExpandedChange,
                    )
                },
                cdsSectionMetadata = object :
                    AnimeMediaDetailsScreen.SectionIndexInfo.SectionMetadata {
                    val hasCds = cdsViewModel.cdEntries.isNotEmpty()
                    override fun count(viewer: AniListViewer?, expanded: Boolean) =
                        if (hasCds) 2 else 0
                },
                cdsSection = {
                    cdsSection(
                        cdEntries = cdsViewModel.cdEntries,
                        onClickEntry = { _, entry ->
                            navigationCallback.onCdEntryClick(
                                model = entry,
                                imageCornerDp = 12.dp,
                            )
                        }
                    )
                },
                recommendationsSectionMetadata = AnimeMediaDetailsScreen.SectionIndexInfo.SectionMetadata.ListSection(
                    items = recommendations?.recommendations,
                    aboveFold = RecommendationComposables.RECOMMENDATIONS_ABOVE_FOLD,
                    hasMore = recommendations?.hasMore != false,
                ),
                recommendationsSection = { expanded, onExpandedChange, onClickListEdit ->
                    recommendationsSection(
                        entry = recommendations,
                        expanded = expanded,
                        onExpandedChange = onExpandedChange,
                        onClickViewAll = {
                            navigationCallback.navigate(
                                AnimeDestination.MediaRecommendations(
                                    mediaId = mediaDetailsViewModel.state.mediaId,
                                    headerParams = mediaHeaderParams(),
                                )
                            )
                        },
                        mediaId = { it.entry.media.id.toString() },
                        mediaRow = { item, modifier ->
                            AnimeMediaListRow(
                                entry = item.entry,
                                viewer = viewer,
                                modifier = modifier,
                                onClickListEdit = onClickListEdit,
                                recommendation = item.data,
                                onUserRecommendationRating = recommendationsViewModel.recommendationToggleHelper::toggle
                            )
                        },
                    )
                },
                activitiesSectionMetadata = AnimeMediaDetailsScreen.SectionIndexInfo.SectionMetadata.ListSection(
                    items = if (activityTab == AnimeMediaDetailsActivityViewModel.ActivityTab.FOLLOWING) {
                        activities?.following
                    } else {
                        activities?.global
                    },
                    aboveFold = AnimeActivityComposables.ACTIVITIES_ABOVE_FOLD,
                    hasMore = true,
                    addOneForViewer = true,
                ),
                activitiesSection = { expanded, onExpandedChanged, onClickListEdit ->
                    activitiesSection(
                        viewer = viewer,
                        onActivityStatusUpdate = activitiesViewModel.activityToggleHelper::toggle,
                        activityTab = activityTab,
                        activities = if (activityTab == AnimeMediaDetailsActivityViewModel.ActivityTab.FOLLOWING) {
                            activities?.following
                        } else {
                            activities?.global
                        },
                        onActivityTabChange = onActivityTabChange,
                        expanded = expanded,
                        onExpandedChange = onExpandedChanged,
                        onClickListEdit = onClickListEdit,
                        onClickViewAll = {
                            navigationCallback.navigate(
                                AnimeDestination.MediaActivities(
                                    mediaId = mediaDetailsViewModel.state.mediaId,
                                    showFollowing = activityTab == AnimeMediaDetailsActivityViewModel.ActivityTab.FOLLOWING,
                                    headerParams = mediaHeaderParams(),
                                )
                            )
                        },
                    )
                },
                forumThreadsSectionMetadata = AnimeMediaDetailsScreen.SectionIndexInfo.SectionMetadata.ListSection(
                    items = forumThreadsViewModel.forumThreads.result,
                    aboveFold = ForumComposables.FORUM_THREADS_ABOVE_FOLD,
                    hasMore = true,
                ),
                forumThreadsSection = { expanded, onExpandedChanged ->
                    forumThreadsSection(
                        viewer = viewer,
                        forumThreads = forumThreadsViewModel.forumThreads.result,
                        expanded = expanded,
                        onExpandedChange = onExpandedChanged,
                        onClickViewAll = {
                            val entry = mediaDetailsViewModel.state.mediaEntry.result
                            navigationCallback.navigate(
                                AnimeDestination.ForumSearch(
                                    title = entry?.media?.title?.userPreferred
                                        ?.let(AnimeDestination.ForumSearch.Title::Custom),
                                    mediaCategoryId = mediaDetailsViewModel.state.mediaId,
                                )
                            )
                        },
                        onStatusUpdate = forumThreadsViewModel.threadToggleHelper::toggle,
                        requestLoad = forumThreadsViewModel::requestLoad,
                        loading = forumThreadsViewModel.forumThreads.loading,
                    )
                },
                reviewsSectionMetadata = AnimeMediaDetailsScreen.SectionIndexInfo.SectionMetadata.ListSection(
                    items = reviewsViewModel.reviews?.reviews,
                    aboveFold = ReviewComposables.REVIEWS_ABOVE_FOLD,
                    hasMore = reviewsViewModel.reviews?.hasMore ?: false,
                ),
                reviewsSection = { expanded, onExpandedChange ->
                    reviewsSection(
                        entry = reviewsViewModel.reviews,
                        expanded = expanded,
                        onExpandedChange = onExpandedChange,
                        onClickViewAll = {
                            navigationCallback.navigate(
                                AnimeDestination.MediaReviews(
                                    mediaId = mediaDetailsViewModel.state.mediaId,
                                    headerParams = mediaHeaderParams()
                                )
                            )
                        },
                        onReviewClick = { navigationCallback, item ->
                            navigationCallback.navigate(
                                AnimeDestination.ReviewDetails(
                                    reviewId = item.id.toString(),
                                    headerParams = mediaHeaderParams(),
                                )
                            )
                        },
                    )
                },
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.CharacterDetails>(
            navigationTypeMap = navigationTypeMap,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "${AniListUtils.ANILIST_BASE_URL}/character/{characterId}"
                },
                navDeepLink {
                    uriPattern = "${AniListUtils.ANILIST_BASE_URL}/character/{characterId}/.*"
                },
            ),
        ) {
            val viewModel =
                viewModel { animeComponent.animeCharacterDetailsViewModel(createSavedStateHandle()) }
            val destination = it.toRoute<AnimeDestination.CharacterDetails>()
            val headerValues = CharacterHeaderValues(
                params = destination.headerParams,
                character = { viewModel.entry.result?.character },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            SharedTransitionKeyScope(destination.sharedTransitionScopeKey) {
                CharacterDetailsScreen(
                    viewModel = viewModel,
                    upIconOption = UpIconOption.Back(navHostController),
                    headerValues = headerValues,
                )
            }
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.CharacterMedias>(
            navigationTypeMap = navigationTypeMap
        ) {
            val destination = it.toRoute<AnimeDestination.CharacterMedias>()
            val viewModel =
                viewModel { animeComponent.characterMediasViewModel(createSavedStateHandle()) }
            val headerValues = CharacterHeaderValues(
                params = destination.headerParams,
                character = { viewModel.entry.result?.character },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            CharacterMediasScreen(
                viewModel = viewModel,
                upIconOption = UpIconOption.Back(navHostController),
                headerValues = headerValues,
                sharedTransitionKey = destination.sharedTransitionKey,
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.StaffDetails>(
            navigationTypeMap = navigationTypeMap,
            deepLinks = listOf(
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/staff/{staffId}" },
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/staff/{staffId}/.*" },
            ),
        ) {
            val viewModel =
                viewModel { animeComponent.staffDetailsViewModel(createSavedStateHandle()) }
            val destination = it.toRoute<AnimeDestination.StaffDetails>()
            val headerValues = StaffHeaderValues(
                params = destination.headerParams,
                staff = { viewModel.entry?.staff },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            StaffDetailsScreen(
                viewModel = viewModel,
                upIconOption = UpIconOption.Back(navHostController),
                headerValues = headerValues,
                sharedTransitionKey = destination.sharedTransitionKey,
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.StaffCharacters>(
            navigationTypeMap = navigationTypeMap,
        ) {
            val viewModel =
                viewModel { animeComponent.staffCharactersViewModel(createSavedStateHandle()) }
            val destination = it.toRoute<AnimeDestination.StaffCharacters>()
            val headerValues = StaffHeaderValues(
                params = destination.headerParams,
                staff = { viewModel.entry.result?.staff },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            StaffCharactersScreen(
                viewModel = viewModel,
                upIconOption = UpIconOption.Back(navHostController),
                headerValues = headerValues,
                sharedTransitionKey = destination.sharedTransitionKey,
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.User>(
            navigationTypeMap = navigationTypeMap,
            deepLinks = listOf(
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/user/{userId}" },
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/user/{userId}/.*" },
            ),
        ) {
            val viewModel =
                viewModel { animeComponent.aniListUserViewModel(createSavedStateHandle()) }
            val destination = it.toRoute<AnimeDestination.User>()
            val headerValues = UserHeaderValues(
                params = destination.headerParams,
                user = { viewModel.entry?.user },
            )
            AniListUserScreen(
                viewModel = viewModel,
                upIconOption = UpIconOption.Back(navHostController),
                headerValues = headerValues,
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.Ignored>(navigationTypeMap) {
            AnimeIgnoreScreen(upIconOption = UpIconOption.Back(navHostController))
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.AiringSchedule>(navigationTypeMap) {
            AiringScheduleScreen(
                onClickBack = { navHostController.navigateUp() },
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.Seasonal>(navigationTypeMap) {
            SeasonalScreen(upIconOption = UpIconOption.Back(navHostController))
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.Activity>(navigationTypeMap) {
            AnimeActivityScreen(animeComponent = animeComponent)
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.Notifications>(navigationTypeMap) {
            NotificationsScreen(upIconOption = UpIconOption.Back(navHostController))
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.MediaCharacters>(
            navigationTypeMap = navigationTypeMap,
        ) {
            val viewModel =
                viewModel { animeComponent.mediaCharactersViewModel(createSavedStateHandle()) }
            val destination = it.toRoute<AnimeDestination.MediaCharacters>()
            val headerValues = MediaHeaderValues(
                params = destination.headerParams,
                media = { viewModel.entry.result?.media },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            MediaCharactersScreen(
                viewModel = viewModel,
                upIconOption = UpIconOption.Back(navHostController),
                headerValues = headerValues,
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.MediaReviews>(
            navigationTypeMap = navigationTypeMap,
        ) {
            val viewModel =
                viewModel { animeComponent.mediaReviewsViewModel(createSavedStateHandle()) }
            val destination = it.toRoute<AnimeDestination.MediaReviews>()
            val headerValues = MediaHeaderValues(
                params = destination.headerParams,
                media = { viewModel.entry.result?.media },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            MediaReviewsScreen(
                viewModel = viewModel,
                upIconOption = UpIconOption.Back(navHostController),
                headerValues = headerValues,
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.Reviews>(navigationTypeMap) {
            ReviewsScreen(
                upIconOption = UpIconOption.Back(navHostController),
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.Recommendations>(navigationTypeMap) {
            val viewModel = viewModel {
                animeComponent.recommendationsViewModelFactory(AnimeDestination::MediaDetails)
                    .create(MediaCompactWithTagsEntry.Provider)
            }
            val editViewModel = viewModel { animeComponent.mediaEditViewModel() }
            MediaEditBottomSheetScaffold(viewModel = editViewModel) {
                RecommendationsScreen(
                    upIconOption = UpIconOption.Back(navHostController),
                    viewModel = viewModel,
                    mediaRows = { media, mediaRecommendation ->
                        SharedTransitionKeyScope(
                            "recommendation",
                            media?.media?.id.toString(),
                            mediaRecommendation?.media?.id.toString(),
                        ) {
                            AnimeMediaCompactListRow(
                                viewer = viewModel.viewer.collectAsState().value,
                                entry = media,
                                modifier = Modifier.padding(
                                    start = 8.dp,
                                    end = 8.dp,
                                    bottom = 8.dp
                                ),
                                onClickListEdit = {
                                    if (media != null) {
                                        editViewModel.initialize(media.media)
                                    }
                                },
                            )

                            AnimeMediaCompactListRow(
                                viewer = viewModel.viewer.collectAsState().value,
                                entry = mediaRecommendation,
                                modifier = Modifier.padding(horizontal = 8.dp),
                                onClickListEdit = {
                                    if (mediaRecommendation != null) {
                                        editViewModel.initialize(mediaRecommendation.media)
                                    }
                                },
                            )
                        }
                    },
                    userRoute = { userId, userName, imageState, sharedTransitionKey ->
                        AnimeDestination.User(
                            userId = userId,
                            sharedTransitionKey = sharedTransitionKey,
                            headerParams = UserHeaderParams(
                                name = userName,
                                bannerImage = null,
                                coverImage = imageState,
                            )
                        )
                    },
                    modifier = Modifier.padding(it)
                )
            }
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.MediaRecommendations>(
            navigationTypeMap = navigationTypeMap,
        ) {
            val viewModel = viewModel {
                animeComponent.mediaRecommendationsViewModelFactory(
                    createSavedStateHandle()
                        .toDestination<AnimeDestination.MediaRecommendations>(navigationTypeMap)
                        .mediaId
                ).create(MediaPreviewEntry.Provider)
            }
            val destination = it.toRoute<AnimeDestination.MediaRecommendations>()
            val headerValues = MediaHeaderValues(
                params = destination.headerParams,
                media = { viewModel.entry.result?.media },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            val viewer by viewModel.viewer.collectAsState()
            val editViewModel = viewModel { animeComponent.mediaEditViewModel() }
            MediaEditBottomSheetScaffold(
                state = { editViewModel.state },
                eventSink = editViewModel::onEvent,
            ) {
                SortFilterBottomScaffold(state = { viewModel.sortFilterController.state }) {
                    val gridState = rememberLazyGridState()
                    viewModel.sortFilterController.ImmediateScrollResetEffect(gridState)
                    MediaRecommendationsScreen(
                        gridState = gridState,
                        onRefresh = viewModel::refresh,
                        items = viewModel.items.collectAsLazyPagingItems(),
                        mediaHeader = { progress ->
                            val entry = viewModel.entry
                            val media = entry.result?.media
                            MediaHeader(
                                viewer = viewer,
                                upIconOption = UpIconOption.Back(navHostController),
                                mediaId = viewModel.mediaId,
                                mediaType = media?.type,
                                titles = entry.result?.titlesUnique,
                                episodes = media?.episodes,
                                format = media?.format,
                                averageScore = media?.averageScore,
                                popularity = media?.popularity,
                                progress = progress,
                                headerValues = headerValues,
                                onFavoriteChanged = {
                                    viewModel.favoritesToggleHelper.set(
                                        headerValues.type.toFavoriteType(),
                                        viewModel.mediaId,
                                        it,
                                    )
                                },
                                enableCoverImageSharedElement = false
                            )
                        },
                        mediaRow = { entry, modifier ->
                            AnimeMediaListRow(
                                entry = entry?.media,
                                viewer = viewer,
                                modifier = modifier,
                                onClickListEdit = editViewModel::initialize,
                                recommendation = entry?.recommendationData,
                                onUserRecommendationRating =
                                    viewModel.recommendationToggleHelper::toggle,
                            )
                        },
                        modifier = Modifier.padding(it)
                    )
                }
            }
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.MediaActivities>(
            navigationTypeMap = navigationTypeMap,
        ) {
            val viewModel =
                viewModel { animeComponent.mediaActivitiesViewModel(createSavedStateHandle()) }
            val destination = it.toRoute<AnimeDestination.MediaActivities>()
            val headerValues = MediaHeaderValues(
                params = destination.headerParams,
                media = { viewModel.entry.result?.data?.media },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            MediaActivitiesScreen(
                viewModel = viewModel,
                upIconOption = UpIconOption.Back(navHostController),
                headerValues = headerValues,
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.ReviewDetails>(
            navigationTypeMap = navigationTypeMap,
            deepLinks = listOf(
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/review/{reviewId}" },
                navDeepLink {
                    uriPattern = "${AniListUtils.ANILIST_BASE_URL}/review/{reviewId}/.*"
                },
            ),
        ) {
            val viewModel =
                viewModel { animeComponent.reviewDetailsViewModel(createSavedStateHandle()) }
            val destination = it.toRoute<AnimeDestination.ReviewDetails>()
            val headerValues = MediaHeaderValues(
                params = destination.headerParams,
                media = { viewModel.entry?.review?.media },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            SharedTransitionKeyScope(destination.sharedTransitionScopeKey) {
                ReviewDetailsScreen(
                    viewModel = viewModel,
                    upIconOption = UpIconOption.Back(navHostController),
                    headerValues = headerValues,
                )
            }
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.StudioMedias>(
            navigationTypeMap = navigationTypeMap,
            deepLinks = listOf(
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/studio/{studioId}" },
                navDeepLink {
                    uriPattern = "${AniListUtils.ANILIST_BASE_URL}/studio/{studioId}/.*"
                },
            ),
        ) {
            val destination = it.toRoute<AnimeDestination.StudioMedias>()
            val viewModel =
                viewModel { animeComponent.studioMediasViewModel(createSavedStateHandle()) }

            StudioMediasScreen(
                upIconOption = UpIconOption.Back(navHostController),
                viewModel = viewModel,
                name = { viewModel.entry.result?.studio?.name ?: destination.name ?: "" },
                favorite = {
                    viewModel.favoritesToggleHelper.favorite
                        ?: viewModel.entry.result?.studio?.isFavourite
                        ?: destination.favorite
                },
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.ActivityDetails>(
            navigationTypeMap = navigationTypeMap,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "${AniListUtils.ANILIST_BASE_URL}/activity/{activityId}"
                },
                navDeepLink {
                    uriPattern = "${AniListUtils.ANILIST_BASE_URL}/activity/{activityId}/.*"
                },
            ),
        ) {
            val destination = it.toRoute<AnimeDestination.ActivityDetails>()
            // TODO: Shared element doesn't actually work
            SharedTransitionKeyScope(destination.sharedTransitionScopeKey) {
                ActivityDetailsScreen(
                    animeComponent = animeComponent,
                    upIconOption = UpIconOption.Back(navHostController),
                )
            }
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.FeatureTiers>(navigationTypeMap) {
            UnlockScreen(
                upIconOption = UpIconOption.Back(navHostController),
                viewModel = viewModel { animeComponent.unlockScreenViewModel() },
                onClickSettings = null,
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.Forum>(navigationTypeMap) {
            ForumRootScreen(
                viewModel = viewModel { animeComponent.forumRootScreenViewModel() },
                upIconOption = UpIconOption.Back(navHostController),
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.ForumSearch>(
            navigationTypeMap = navigationTypeMap,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern =
                        "${AniListUtils.ANILIST_BASE_URL}/forum/recent?category={categoryId}"
                },
                navDeepLink {
                    uriPattern =
                        "${AniListUtils.ANILIST_BASE_URL}/forum/recent?media={mediaCategoryId}"
                },
            ),
        ) {
            val destination = it.toRoute<AnimeDestination.ForumSearch>()
            ForumSearchScreen(
                viewModel = viewModel { animeComponent.forumSearchViewModel(createSavedStateHandle()) },
                upIconOption = UpIconOption.Back(navHostController),
                title = destination.title,
            )
        }

        // TODO: Forum deep links
        navGraphBuilder.sharedElementComposable<AnimeDestination.ForumThread>(
            navigationTypeMap = navigationTypeMap,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "${AniListUtils.ANILIST_BASE_URL}/forum/thread/{threadId}"
                },
                navDeepLink {
                    uriPattern = "${AniListUtils.ANILIST_BASE_URL}/forum/thread/{threadId}/.*"
                },
            ),
        ) {
            ForumThreadScreen(
                upIconOption = UpIconOption.Back(navHostController),
                title = it.arguments?.getString("title"),
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.ForumThreadComment>(
            navigationTypeMap = navigationTypeMap,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern =
                        "${AniListUtils.ANILIST_BASE_URL}/forum/thread/{threadId}/comment/{commentId}"
                },
                navDeepLink {
                    uriPattern =
                        "${AniListUtils.ANILIST_BASE_URL}/forum/thread/{threadId}/comment/{commentId}/.*"
                },
            ),
        ) {
            ForumThreadCommentTreeScreen(
                upIconOption = UpIconOption.Back(navHostController),
                title = it.arguments?.getString("title"),
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.MediaHistory>(navigationTypeMap) {
            MediaHistoryScreen(
                upIconOption = UpIconOption.Back(navHostController),
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.UserFollowing>(navigationTypeMap) {
            val destination = it.toRoute<AnimeDestination.UserFollowing>()
            val viewModel =
                viewModel { animeComponent.userListViewModelFollowing(createSavedStateHandle()) }
            UserListScreen(
                upIconOption = UpIconOption.Back(navHostController),
                viewModel = viewModel,
                title = {
                    if (destination.userId == null) {
                        stringResource(Res.string.anime_user_following_you)
                    } else {
                        stringResource(
                            Res.string.anime_user_following_user,
                            destination.userName.orEmpty()
                        )
                    }
                },
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.UserFollowers>(navigationTypeMap) {
            val destination = it.toRoute<AnimeDestination.UserFollowers>()
            val viewModel =
                viewModel { animeComponent.userListViewModelFollowers(createSavedStateHandle()) }
            UserListScreen(
                upIconOption = UpIconOption.Back(navHostController),
                viewModel = viewModel,
                title = {
                    if (destination.userId == null) {
                        stringResource(Res.string.anime_user_followers_you)
                    } else {
                        stringResource(
                            Res.string.anime_user_followers_user,
                            destination.userName.orEmpty()
                        )
                    }
                },
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.UserFavoriteMedia>(
            navigationTypeMap
        ) {
            val destination = it.toRoute<AnimeDestination.UserFavoriteMedia>()
            val viewModel =
                viewModel { animeComponent.userFavoriteMediaViewModel(createSavedStateHandle()) }
            UserFavoriteMediaScreen(
                upIconOption = UpIconOption.Back(navHostController),
                viewModel = viewModel,
                title = {
                    if (viewModel.mediaType == MediaType.ANIME) {
                        if (destination.userId == null) {
                            stringResource(Res.string.anime_user_favorite_anime_you)
                        } else {
                            stringResource(
                                Res.string.anime_user_favorite_anime_user,
                                destination.userName.orEmpty()
                            )
                        }
                    } else {
                        if (destination.userId == null) {
                            stringResource(Res.string.anime_user_favorite_manga_you)
                        } else {
                            stringResource(
                                Res.string.anime_user_favorite_manga_user,
                                destination.userName.orEmpty()
                            )
                        }
                    }
                },
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.UserFavoriteCharacters>(
            navigationTypeMap
        ) {
            val destination = it.toRoute<AnimeDestination.UserFavoriteCharacters>()
            UserFavoriteCharactersScreen(
                upIconOption = UpIconOption.Back(navHostController),
                title = {
                    if (destination.userId == null) {
                        stringResource(Res.string.anime_user_favorite_characters_you)
                    } else {
                        stringResource(
                            Res.string.anime_user_favorite_characters_user,
                            destination.userName.orEmpty()
                        )
                    }
                },
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.UserFavoriteStaff>(
            navigationTypeMap
        ) {
            val destination = it.toRoute<AnimeDestination.UserFavoriteStaff>()
            UserFavoriteStaffScreen(
                upIconOption = UpIconOption.Back(navHostController),
                title = {
                    if (destination.userId == null) {
                        stringResource(Res.string.anime_user_favorite_staff_you)
                    } else {
                        stringResource(
                            Res.string.anime_user_favorite_staff_user,
                            destination.userName.orEmpty()
                        )
                    }
                },
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.UserFavoriteStudios>(
            navigationTypeMap
        ) {
            val destination = it.toRoute<AnimeDestination.UserFavoriteStudios>()
            UserFavoriteStudiosScreen(
                upIconOption = UpIconOption.Back(navHostController),
                title = {
                    if (destination.userId == null) {
                        stringResource(Res.string.anime_user_favorite_studios_you)
                    } else {
                        stringResource(
                            Res.string.anime_user_favorite_studios_user,
                            destination.userName.orEmpty(),
                        )
                    }
                },
            )
        }
    }

    @Composable
    fun UserMediaListScreen(
        userId: String?,
        userName: String?,
        mediaType: MediaType,
        upIconOption: UpIconOption?,
        scrollStateSaver: ScrollStateSaver,
        mediaListStatus: MediaListStatus? = null,
        bottomNavigationState: BottomNavigationState? = null,
    ) {
        val animeComponent = LocalAnimeComponent.current
        val viewModel = viewModel(key = mediaType.rawValue) {
            animeComponent.animeUserListViewModel(
                createSavedStateHandle(),
                userId,
                userName,
                mediaType,
                mediaListStatus
            )
        }
        AnimeUserListScreen(
            upIconOption = upIconOption,
            viewModel = viewModel,
            scrollStateSaver = scrollStateSaver,
            bottomNavigationState = bottomNavigationState,
        )
    }

    class NavigationCallback(
        // Null to make previews easier
        private val navHostController: NavHostController? = null,
        private val cdEntryNavigator: CdEntryNavigator? = null,
    ) {
        fun onCdEntryClick(model: CdEntryGridModel, imageCornerDp: Dp?) {
            navHostController?.let {
                cdEntryNavigator?.onCdEntryClick(it, listOf(model.id.valueId), imageCornerDp)
            }
        }

        fun navigate(route: String) = navHostController?.navigate(route)

        inline fun <reified T : Any> navigate(route: T) = navigateInternal(route)

        fun <T : Any> navigateInternal(route: T) = navHostController?.navigate<T>(route)

        fun navigateUp() = navHostController?.navigateUp()
    }
}

val LocalNavigationCallback = staticCompositionLocalOf { AnimeNavigator.NavigationCallback(null) }
