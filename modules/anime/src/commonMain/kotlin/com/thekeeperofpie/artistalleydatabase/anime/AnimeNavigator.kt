package com.thekeeperofpie.artistalleydatabase.anime

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_characters_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_staff_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_view_all_content_description
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_anime_user
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_anime_you
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_characters_user
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_characters_you
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_manga_user
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_manga_you
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_staff_user
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_staff_you
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_studios_user
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_favorite_studios_you
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_followers_user
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_followers_you
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_following_user
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_following_you
import com.anilist.data.type.MediaListStatus
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.activities.ActivityDestinations
import com.thekeeperofpie.artistalleydatabase.anime.activities.ActivityList
import com.thekeeperofpie.artistalleydatabase.anime.activities.ActivityTab
import com.thekeeperofpie.artistalleydatabase.anime.activities.AnimeActivityComposables
import com.thekeeperofpie.artistalleydatabase.anime.activities.activitiesSection
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterDestinations
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterListRow
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterSmallCard
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.characters.charactersSection
import com.thekeeperofpie.artistalleydatabase.anime.characters.rememberImageStateBelowInnerImage
import com.thekeeperofpie.artistalleydatabase.anime.forums.ForumComposables
import com.thekeeperofpie.artistalleydatabase.anime.forums.ForumDestinations
import com.thekeeperofpie.artistalleydatabase.anime.forums.forumThreadsSection
import com.thekeeperofpie.artistalleydatabase.anime.history.MediaHistoryScreen
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeIgnoreScreen
import com.thekeeperofpie.artistalleydatabase.anime.list.AnimeUserListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaCompactWithTagsEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeader
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaWithListStatusEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.activity.MediaActivitiesScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.characters.MediaCharactersScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.media.data.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaGridCard
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.characterMediaItems
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.horizontalMediaCardRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.mediaHorizontalRow
import com.thekeeperofpie.artistalleydatabase.anime.notifications.NotificationsScreen
import com.thekeeperofpie.artistalleydatabase.anime.recommendations.RecommendationComposables
import com.thekeeperofpie.artistalleydatabase.anime.recommendations.RecommendationDestinations
import com.thekeeperofpie.artistalleydatabase.anime.recommendations.media.MediaRecommendationsScreen
import com.thekeeperofpie.artistalleydatabase.anime.recommendations.recommendationsSection
import com.thekeeperofpie.artistalleydatabase.anime.reviews.ReviewComposables
import com.thekeeperofpie.artistalleydatabase.anime.reviews.ReviewDestinations
import com.thekeeperofpie.artistalleydatabase.anime.reviews.reviewsSection
import com.thekeeperofpie.artistalleydatabase.anime.schedule.AiringScheduleScreen
import com.thekeeperofpie.artistalleydatabase.anime.search.MediaSearchScreen
import com.thekeeperofpie.artistalleydatabase.anime.seasonal.SeasonalScreen
import com.thekeeperofpie.artistalleydatabase.anime.songs.AnimeSongComposables
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffDestinations
import com.thekeeperofpie.artistalleydatabase.anime.staff.staffSection
import com.thekeeperofpie.artistalleydatabase.anime.studios.StudioDestinations
import com.thekeeperofpie.artistalleydatabase.anime.studios.StudioListRowFragmentEntry
import com.thekeeperofpie.artistalleydatabase.anime.studios.studiosSection
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserViewModel
import com.thekeeperofpie.artistalleydatabase.anime.user.UserHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.user.favorite.UserFavoriteCharactersScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.favorite.UserFavoriteMediaScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.favorite.UserFavoriteStaffScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.favorite.UserFavoriteStudiosScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.follow.UserListScreen
import com.thekeeperofpie.artistalleydatabase.anime.users.UserDestinations
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryComponent
import com.thekeeperofpie.artistalleydatabase.cds.cdsSection
import com.thekeeperofpie.artistalleydatabase.monetization.UnlockScreen
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalSharedTransitionPrefixKeys
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElementComposable
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementComposable
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import org.jetbrains.compose.resources.stringResource
import artistalleydatabase.modules.anime.users.generated.resources.Res as UsersRes

@OptIn(ExperimentalMaterial3Api::class)
object AnimeNavigator {

    fun initialize(
        navigationController: NavigationController,
        navGraphBuilder: NavGraphBuilder,
        navigationTypeMap: NavigationTypeMap,
        upIconOption: UpIconOption?,
        onClickAuth: () -> Unit,
        onClickSettings: () -> Unit,
        onClickShowLastCrash: () -> Unit,
        component: AnimeComponent,
        cdEntryComponent: CdEntryComponent,
        onCdEntryClick: (entryIds: List<String>, imageCornerDp: Dp) -> Unit = { _, _ -> },
    ) {
        navGraphBuilder.sharedElementComposable(
            route = AnimeNavDestinations.HOME.id,
            deepLinks = listOf(navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/" }),
        ) {
            val viewModel = viewModel { component.animeRootViewModel() }
            AnimeRootScreen(
                mediaEditBottomSheetScaffold =
                    MediaEditBottomSheetScaffold.fromComponent(component),
                upIconOption = upIconOption,
                viewModel = viewModel,
                onClickAuth = onClickAuth,
                onSubmitAuthToken = viewModel::onSubmitAuthToken,
                onClickSettings = onClickSettings,
                onClickShowLastCrash = onClickShowLastCrash,
                userRoute = AnimeDestination.User.route,
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.SearchMedia>(navigationTypeMap) {
            val destination = it.toRoute<AnimeDestination.SearchMedia>()
            MediaSearchScreen(
                title = destination.title,
                upIconOption = UpIconOption.Back(navigationController),
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
                upIconOption = UpIconOption.Back(navigationController),
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
                viewModel { component.animeMediaDetailsViewModel(createSavedStateHandle()) }
            val headerValues = MediaHeaderValues(
                params = destination.headerParams,
                media = { mediaDetailsViewModel.state.mediaEntry.result?.media },
                favoriteUpdate = { mediaDetailsViewModel.favoritesToggleHelper.favorite },
            )

            val charactersViewModel = viewModel {
                component.animeMediaDetailsCharactersViewModel(
                    destination.mediaId,
                    mediaDetailsViewModel.characters(),
                )
            }
            val charactersDeferred =
                charactersViewModel.charactersDeferred.collectAsLazyPagingItems()

            val staffViewModel = viewModel {
                component.animeMediaDetailsStaffViewModel(mediaDetailsViewModel.media())
            }
            val staff = staffViewModel.staff.collectAsLazyPagingItems()

            val songsViewModel =
                viewModel { component.animeSongsViewModel(mediaDetailsViewModel) }
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                lifecycleOwner.lifecycle.addObserver(songsViewModel)
                onDispose { lifecycleOwner.lifecycle.removeObserver(songsViewModel) }
            }

            DisposableEffect(lifecycleOwner) {
                onDispose { songsViewModel.animeSongsCollapseAll() }
            }

            val cdsViewModel =
                viewModel { cdEntryComponent.cdsFromMediaViewModel(destination.mediaId) }

            val viewer by mediaDetailsViewModel.viewer.collectAsState()

            @Suppress("UNCHECKED_CAST")
            val recommendationsViewModel = viewModel {
                component
                    .animeMediaDetailsRecommendationsViewModelFactory(destination.mediaId)
                    .create(mediaDetailsViewModel.recommendations(), MediaPreviewEntry.Provider)
            }

            val activitiesViewModel = viewModel {
                component.animeMediaDetailsActivityViewModel(mediaDetailsViewModel.state.mediaId)
            }
            val activities = activitiesViewModel.activities
            val (activityTab, onActivityTabChange) = rememberSaveable(viewer, activities) {
                mutableStateOf(
                    if (activities?.following.isNullOrEmpty()) {
                        ActivityTab.GLOBAL
                    } else {
                        ActivityTab.FOLLOWING
                    }
                )
            }

            val forumThreadsViewModel = viewModel {
                component.animeMediaDetailsForumThreadsViewModel(mediaDetailsViewModel.media())
            }

            val reviewsViewModel = viewModel {
                component.animeMediaDetailsReviewsViewModel(mediaDetailsViewModel.reviews())
            }

            val navigationController = LocalNavigationController.current

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
            val reviewsEntry by reviewsViewModel.reviewsEntry.collectAsState()
            AnimeMediaDetailsScreen(
                viewModel = mediaDetailsViewModel,
                upIconOption = UpIconOption.Back(navigationController),
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
                        viewAllRoute = {
                            AnimeDestination.MediaCharacters(
                                mediaId = entry.mediaId,
                                headerParams = mediaHeaderParams(),
                            )
                        },
                        viewAllContentDescriptionTextRes = Res.string.anime_media_details_view_all_content_description,
                        staffDetailsRoute = StaffDestinations.StaffDetails.route,
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
                            onCdEntryClick(listOf(entry.id.valueId), 12.dp)
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
                            navigationController.navigate(
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
                    items = if (activityTab == ActivityTab.FOLLOWING) {
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
                        activities = if (activityTab == ActivityTab.FOLLOWING) {
                            activities?.following
                        } else {
                            activities?.global
                        },
                        onActivityTabChange = onActivityTabChange,
                        expanded = expanded,
                        onExpandedChange = onExpandedChanged,
                        userRoute = AnimeDestination.User.route,
                        onClickViewAll = {
                            navigationController.navigate(
                                AnimeDestination.MediaActivities(
                                    mediaId = mediaDetailsViewModel.state.mediaId,
                                    showFollowing = activityTab == ActivityTab.FOLLOWING,
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
                            navigationController.navigate(
                                ForumDestinations.ForumSearch(
                                    title = entry?.media?.title?.userPreferred
                                        ?.let(ForumDestinations.ForumSearch.Title::Custom),
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
                    items = reviewsEntry?.reviews,
                    aboveFold = ReviewComposables.REVIEWS_ABOVE_FOLD,
                    hasMore = reviewsEntry?.hasMore == true,
                ),
                reviewsSection = { expanded, onExpandedChange ->
                    reviewsSection(
                        entry = reviewsEntry,
                        expanded = expanded,
                        onExpandedChange = onExpandedChange,
                        userRoute = AnimeDestination.User.route,
                        onClickViewAll = {
                            navigationController.navigate(
                                ReviewDestinations.MediaReviews(
                                    mediaId = mediaDetailsViewModel.state.mediaId,
                                    headerParams = mediaHeaderParams()
                                )
                            )
                        },
                        onReviewClick = { item ->
                            navigationController.navigate(
                                ReviewDestinations.ReviewDetails(
                                    reviewId = item.id.toString(),
                                    headerParams = mediaHeaderParams(),
                                )
                            )
                        },
                    )
                },
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.User>(
            navigationTypeMap = navigationTypeMap,
            deepLinks = listOf(
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/user/{userId}" },
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/user/{userId}/.*" },
            ),
        ) {
            val destination = it.toRoute<AnimeDestination.User>()
            val viewModel = viewModel {
                component.aniListUserViewModelFactory(
                    createSavedStateHandle(),
                    AnimeDestination.MediaDetails.route,
                ).create(
                    mediaEntryProvider = MediaWithListStatusEntry.Provider,
                    studioEntryProvider = StudioListRowFragmentEntry.provider(),
                )
            }
            val entry by viewModel.entry.collectAsState()
            val headerValues = UserHeaderValues(
                params = destination.headerParams,
                user = { entry.result?.user },
            )
            AniListUserScreen(
                component = component,
                viewModel = viewModel,
                upIconOption = UpIconOption.Back(navigationController),
                headerValues = headerValues,
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.Ignored>(navigationTypeMap) {
            AnimeIgnoreScreen(upIconOption = UpIconOption.Back(navigationController))
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.AiringSchedule>(navigationTypeMap) {
            AiringScheduleScreen(
                onClickBack = { navigationController.navigateUp() },
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.Seasonal>(navigationTypeMap) {
            SeasonalScreen(upIconOption = UpIconOption.Back(navigationController))
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.Notifications>(navigationTypeMap) {
            NotificationsScreen(upIconOption = UpIconOption.Back(navigationController))
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.MediaCharacters>(
            navigationTypeMap = navigationTypeMap,
        ) {
            val viewModel =
                viewModel { component.mediaCharactersViewModel(createSavedStateHandle()) }
            val destination = it.toRoute<AnimeDestination.MediaCharacters>()
            val headerValues = MediaHeaderValues(
                params = destination.headerParams,
                media = { viewModel.entry.result?.media },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            MediaCharactersScreen(
                viewModel = viewModel,
                upIconOption = UpIconOption.Back(navigationController),
                headerValues = headerValues,
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.MediaRecommendations>(
            navigationTypeMap = navigationTypeMap,
        ) {
            val viewModel = viewModel {
                component.mediaRecommendationsViewModelFactory(
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
            val editViewModel = viewModel { component.mediaEditViewModel() }
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
                                upIconOption = UpIconOption.Back(navigationController),
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
            val viewModel = viewModel {
                component.mediaActivitiesViewModel(
                    createSavedStateHandle(),
                    AnimeDestination.MediaDetails.route,
                )
            }
            val destination = it.toRoute<AnimeDestination.MediaActivities>()
            val headerValues = MediaHeaderValues(
                params = destination.headerParams,
                media = { viewModel.entry.result?.data?.media },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            MediaActivitiesScreen(
                viewModel = viewModel,
                upIconOption = UpIconOption.Back(navigationController),
                headerValues = headerValues,
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.FeatureTiers>(navigationTypeMap) {
            UnlockScreen(
                upIconOption = UpIconOption.Back(navigationController),
                viewModel = viewModel { component.unlockScreenViewModel() },
                onClickSettings = null,
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.MediaHistory>(navigationTypeMap) {
            MediaHistoryScreen(
                upIconOption = UpIconOption.Back(navigationController),
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.UserFollowing>(navigationTypeMap) {
            val destination = it.toRoute<AnimeDestination.UserFollowing>()
            val viewModel =
                viewModel { component.userListViewModelFollowing(createSavedStateHandle()) }
            UserListScreen(
                upIconOption = UpIconOption.Back(navigationController),
                viewModel = viewModel,
                title = {
                    if (destination.userId == null) {
                        stringResource(UsersRes.string.anime_user_following_you)
                    } else {
                        stringResource(
                            UsersRes.string.anime_user_following_user,
                            destination.userName.orEmpty()
                        )
                    }
                },
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestination.UserFollowers>(navigationTypeMap) {
            val destination = it.toRoute<AnimeDestination.UserFollowers>()
            val viewModel =
                viewModel { component.userListViewModelFollowers(createSavedStateHandle()) }
            UserListScreen(
                upIconOption = UpIconOption.Back(navigationController),
                viewModel = viewModel,
                title = {
                    if (destination.userId == null) {
                        stringResource(UsersRes.string.anime_user_followers_you)
                    } else {
                        stringResource(
                            UsersRes.string.anime_user_followers_user,
                            destination.userName.orEmpty()
                        )
                    }
                },
            )
        }

        navGraphBuilder.sharedElementComposable<UserDestinations.UserFavoriteMedia>(
            navigationTypeMap
        ) {
            val destination = it.toRoute<UserDestinations.UserFavoriteMedia>()
            val viewModel =
                viewModel { component.userFavoriteMediaViewModel(createSavedStateHandle()) }
            UserFavoriteMediaScreen(
                upIconOption = UpIconOption.Back(navigationController),
                viewModel = viewModel,
                title = {
                    if (viewModel.mediaType == MediaType.ANIME) {
                        if (destination.userId == null) {
                            stringResource(UsersRes.string.anime_user_favorite_anime_you)
                        } else {
                            stringResource(
                                UsersRes.string.anime_user_favorite_anime_user,
                                destination.userName.orEmpty()
                            )
                        }
                    } else {
                        if (destination.userId == null) {
                            stringResource(UsersRes.string.anime_user_favorite_manga_you)
                        } else {
                            stringResource(
                                UsersRes.string.anime_user_favorite_manga_user,
                                destination.userName.orEmpty()
                            )
                        }
                    }
                },
            )
        }

        navGraphBuilder.sharedElementComposable<UserDestinations.UserFavoriteCharacters>(
            navigationTypeMap
        ) {
            val destination = it.toRoute<UserDestinations.UserFavoriteCharacters>()
            UserFavoriteCharactersScreen(
                upIconOption = UpIconOption.Back(navigationController),
                title = {
                    if (destination.userId == null) {
                        stringResource(UsersRes.string.anime_user_favorite_characters_you)
                    } else {
                        stringResource(
                            UsersRes.string.anime_user_favorite_characters_user,
                            destination.userName.orEmpty()
                        )
                    }
                },
            )
        }

        navGraphBuilder.sharedElementComposable<UserDestinations.UserFavoriteStaff>(
            navigationTypeMap
        ) {
            val destination = it.toRoute<UserDestinations.UserFavoriteStaff>()
            UserFavoriteStaffScreen(
                upIconOption = UpIconOption.Back(navigationController),
                title = {
                    if (destination.userId == null) {
                        stringResource(UsersRes.string.anime_user_favorite_staff_you)
                    } else {
                        stringResource(
                            UsersRes.string.anime_user_favorite_staff_user,
                            destination.userName.orEmpty()
                        )
                    }
                },
            )
        }

        navGraphBuilder.sharedElementComposable<UserDestinations.UserFavoriteStudios>(
            navigationTypeMap
        ) {
            val destination = it.toRoute<UserDestinations.UserFavoriteStudios>()
            UserFavoriteStudiosScreen(
                upIconOption = UpIconOption.Back(navigationController),
                title = {
                    if (destination.userId == null) {
                        stringResource(UsersRes.string.anime_user_favorite_studios_you)
                    } else {
                        stringResource(
                            UsersRes.string.anime_user_favorite_studios_user,
                            destination.userName.orEmpty(),
                        )
                    }
                },
            )
        }

        val mediaEditBottomSheetScaffold = MediaEditBottomSheetScaffold.fromComponent(component)
        ActivityDestinations.addToGraph(
            navGraphBuilder = navGraphBuilder,
            navigationTypeMap = navigationTypeMap,
            component = component,
            userRoute = AnimeDestination.User.route,
            mediaDetailsRoute = AnimeDestination.MediaDetails.route,
            mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
            mediaRow = { entry, viewer, onClickListEdit, modifier ->
                AnimeMediaCompactListRow(
                    viewer = viewer,
                    entry = entry,
                    onClickListEdit = onClickListEdit,
                    modifier = modifier,
                )
            },
            mediaEntryProvider = MediaCompactWithTagsEntry.Provider,
        )

        CharacterDestinations.addToGraph(
            navGraphBuilder = navGraphBuilder,
            navigationTypeMap = navigationTypeMap,
            component = component,
            mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
            mediaRow = { entry, viewer, label, onClickListEdit, modifier ->
                AnimeMediaListRow(
                    entry = entry,
                    viewer = viewer,
                    label = label,
                    onClickListEdit = onClickListEdit,
                    modifier = modifier
                )
            },
            staffSection = { titleRes, staff, roleLines ->
                staffSection(
                    titleRes = titleRes,
                    staffList = staff,
                    roleLines = roleLines
                )
            },
            mediaEntryProvider = MediaPreviewEntry.Provider,
        )

        ForumDestinations.addToGraph(
            navGraphBuilder = navGraphBuilder,
            navigationTypeMap = navigationTypeMap,
            component = component,
            mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
            mediaDetailsRoute = AnimeDestination.MediaDetails.route,
            userRoute = AnimeDestination.User.route,
            mediaRow = { entry, viewer, onClickListEdit, modifier ->
                AnimeMediaCompactListRow(
                    viewer = viewer,
                    entry = entry,
                    onClickListEdit = onClickListEdit,
                    modifier = modifier,
                )
            },
            mediaEntryProvider = MediaCompactWithTagsEntry.Provider,
        )

        RecommendationDestinations.addToGraph(
            navGraphBuilder = navGraphBuilder,
            navigationTypeMap = navigationTypeMap,
            component = component,
            mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
            mediaDetailsRoute = AnimeDestination.MediaDetails.route,
            userRoute = AnimeDestination.User.route,
            mediaRow = { entry, viewer, onClickListEdit, modifier ->
                AnimeMediaCompactListRow(
                    entry = entry,
                    viewer = viewer,
                    onClickListEdit = onClickListEdit,
                    modifier = modifier
                )
            },
            mediaEntryProvider = MediaCompactWithTagsEntry.Provider,
        )

        ReviewDestinations.addToGraph(
            navGraphBuilder = navGraphBuilder,
            navigationTypeMap = navigationTypeMap,
            component = component,
            mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
            mediaDetailsRoute = AnimeDestination.MediaDetails.route,
            userRoute = AnimeDestination.User.route,
            mediaEntryProvider = MediaCompactWithTagsEntry.Provider,
            mediaTitle = { it.media.title?.primaryTitle() },
            mediaHeaderParams = { entry, title, imageState ->
                MediaHeaderParams(
                    title = title,
                    coverImage = imageState,
                    mediaCompactWithTags = entry.media,
                    favorite = null,
                )
            },
            mediaImageUri = { it?.media?.coverImage?.extraLarge },
            mediaRow = { entry, viewer, coverImageState, onClickListEdit, modifier ->
                AnimeMediaCompactListRow(
                    viewer = viewer,
                    entry = entry,
                    modifier = modifier,
                    onClickListEdit = onClickListEdit,
                    coverImageState = coverImageState,
                )
            },
            mediaHeader = { progress, viewer, media, headerValues, titlesUnique, onFavoriteChanged ->
                val navigationController = LocalNavigationController.current
                val coverImageState = rememberCoilImageState(media?.coverImage?.extraLarge)
                val title = media?.title?.primaryTitle()
                val mediaSharedTransitionKey = media?.id?.toString()
                    ?.let { SharedTransitionKey.makeKeyForId(it) }
                MediaHeader(
                    viewer = viewer,
                    upIconOption = UpIconOption.Back(navigationController),
                    mediaId = media?.id?.toString(),
                    mediaType = media?.type,
                    titles = titlesUnique,
                    episodes = media?.episodes,
                    format = media?.format,
                    averageScore = media?.averageScore,
                    popularity = media?.popularity,
                    progress = progress,
                    headerValues = headerValues,
                    onFavoriteChanged = onFavoriteChanged,
                    coverImageState = coverImageState,
                    enableCoverImageSharedElement = false,
                    onCoverImageClick = {
                        if (media != null) {
                            val imageState = coverImageState.toImageState()
                            navigationController.navigate(
                                AnimeDestination.MediaDetails(
                                    mediaId = media.id.toString(),
                                    title = title,
                                    coverImage = imageState,
                                    sharedTransitionKey = mediaSharedTransitionKey,
                                    headerParams = MediaHeaderParams(
                                        title = title,
                                        coverImage = imageState,
                                        media = media,
                                    )
                                )
                            )
                        }
                    }
                )
            }
        )

        StaffDestinations.addToGraph(
            navGraphBuilder = navGraphBuilder,
            navigationTypeMap = navigationTypeMap,
            component = component,
            mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
            characterRow = { entry, viewer, onClickListEdit ->
                CharacterListRow(
                    entry = entry,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    showRole = true,
                    staffDetailsRoute = StaffDestinations.StaffDetails.route,
                    mediaItems = {
                        characterMediaItems(
                            media = it,
                            viewer = { viewer },
                            onClickListEdit = onClickListEdit,
                        )
                    },
                    showStaff = false,
                )
            },
            charactersSection = { titleRes, viewAllRoute, viewAllContentDescriptionTextRes, characters ->
                charactersSection(
                    titleRes = titleRes,
                    characters = characters,
                    viewAllRoute = viewAllRoute,
                    viewAllContentDescriptionTextRes = viewAllContentDescriptionTextRes,
                    staffDetailsRoute = StaffDestinations.StaffDetails.route,
                )
            },
            characterCard = {
                val image = it.character.image?.large
                val innerImage = it.media?.coverImage?.extraLarge
                val imageState =
                    rememberImageStateBelowInnerImage(image, innerImage)
                val innerImageState = rememberCoilImageState(innerImage)
                val languageOptionMedia = LocalLanguageOptionMedia.current
                val characterName = it.character.name?.primaryName()
                val characterSharedTransitionKey =
                    SharedTransitionKey.makeKeyForId(it.character.id.toString())
                val mediaSharedTransitionKey = it.media?.id?.toString()
                    ?.let { SharedTransitionKey.makeKeyForId(it) }
                val sharedTransitionScopeKey =
                    LocalSharedTransitionPrefixKeys.current
                val navigationController = LocalNavigationController.current
                CharacterSmallCard(
                    sharedTransitionKey = characterSharedTransitionKey,
                    sharedTransitionIdentifier = "character_image",
                    innerSharedTransitionKey = mediaSharedTransitionKey,
                    innerSharedTransitionIdentifier = "media_image",
                    image = image,
                    innerImage = innerImage,
                    imageState = imageState,
                    innerImageState = innerImageState,
                    onClick = {
                        navigationController.navigate(
                            CharacterDestinations.CharacterDetails(
                                characterId = it.character.id.toString(),
                                sharedTransitionScopeKey = sharedTransitionScopeKey,
                                headerParams = CharacterHeaderParams(
                                    name = characterName,
                                    subtitle = null,
                                    favorite = null,
                                    coverImage = imageState.toImageState(),
                                )
                            )
                        )
                    },
                    onClickInnerImage = {
                        it.media?.let {
                            navigationController.navigate(
                                AnimeDestination.MediaDetails(
                                    mediaNavigationData = it,
                                    coverImage = innerImageState.toImageState(),
                                    languageOptionMedia = languageOptionMedia,
                                    sharedTransitionKey = mediaSharedTransitionKey,
                                )
                            )
                        }
                    },
                ) { textColor ->
                    it.role?.let {
                        AutoHeightText(
                            text = stringResource(it.toTextRes()),
                            color = textColor,
                            style = MaterialTheme.typography.bodySmall
                                .copy(lineBreak = LineBreak.Heading),
                            maxLines = 1,
                            minTextSizeSp = 8f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 12.dp, top = 8.dp)
                        )
                    }

                    it.character.name?.primaryName()?.let {
                        AutoHeightText(
                            text = it,
                            color = textColor,
                            style = MaterialTheme.typography.bodyMedium
                                .copy(lineBreak = LineBreak.Heading),
                            minTextSizeSp = 8f,
                            minLines = 2,
                            maxLines = 2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            },
            mediaGridCard = { mediaWithRole, viewer, onClickListEdit ->
                MediaGridCard(
                    entry = mediaWithRole.mediaEntry,
                    viewer = viewer,
                    onClickListEdit = onClickListEdit,
                    modifier = Modifier.width(120.dp),
                    showTypeIcon = true,
                ) { textColor ->
                    mediaWithRole.role?.let {
                        AutoHeightText(
                            text = it,
                            color = textColor,
                            style = MaterialTheme.typography.bodySmall
                                .copy(lineBreak = LineBreak.Heading),
                            minLines = 2,
                            maxLines = 2,
                            minTextSizeSp = 8f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, end = 8.dp, top = 8.dp)
                        )
                    }
                }
            },
            characterEntryProvider = CharacterListRow.Entry.Provider<MediaWithListStatusEntry>(),
            mediaEntryProvider = MediaWithListStatusEntry.Provider,
        )

        StudioDestinations.addToGraph(
            navGraphBuilder = navGraphBuilder,
            navigationTypeMap = navigationTypeMap,
            component = component,
            mediaEditBottomSheetScaffold = mediaEditBottomSheetScaffold,
            mediaEntryProvider = MediaPreviewEntry.Provider,
            mediaRow = { entry, viewer, onClickListEdit, modifier ->
                AnimeMediaListRow(
                    entry = entry,
                    viewer = viewer,
                    modifier = modifier,
                    onClickListEdit = onClickListEdit,
                )
            },
        )
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

    // TODO: Share this elsewhere
    @Composable
    fun AniListUserScreen(
        component: AnimeComponent,
        viewModel: AniListUserViewModel<MediaWithListStatusEntry, StudioListRowFragmentEntry<MediaWithListStatusEntry>>,
        upIconOption: UpIconOption?,
        headerValues: UserHeaderValues,
        showLogOut: Boolean = false,
        bottomNavigationState: BottomNavigationState? = null,
        onClickSettings: (() -> Unit)? = null,
    ) {
        val entry by viewModel.entry.collectAsState()
        val viewer by viewModel.viewer.collectAsState()
        val anime = viewModel.anime.collectAsLazyPagingItems()
        val manga = viewModel.manga.collectAsLazyPagingItems()
        val characters = viewModel.characters.collectAsLazyPagingItems()
        val staff = viewModel.staff.collectAsLazyPagingItems()
        val studios by viewModel.studios.collectAsState()
        val navigationController = LocalNavigationController.current
        AniListUserScreen(
            mediaEditBottomSheetScaffold = MediaEditBottomSheetScaffold.fromComponent(component),
            viewModel = viewModel,
            viewer = { viewer },
            entry = { entry },
            upIconOption = upIconOption,
            headerValues = headerValues,
            anime = anime,
            manga = manga,
            mediaHorizontalRow = { titleRes, entries, viewAllRoute, viewAllContentDescriptionTextRes, onClickListEdit ->
                // TODO: mediaListEntry doesn't load properly for these, figure out a way to show status
                mediaHorizontalRow(
                    viewer = viewer,
                    titleRes = titleRes,
                    entries = entries,
                    forceListEditIcon = true,
                    onClickListEdit = onClickListEdit,
                    onClickViewAll = {
                        navigationController.navigate(viewAllRoute)
                    },
                    viewAllContentDescriptionTextRes = viewAllContentDescriptionTextRes,
                )
            },
            characters = characters,
            charactersSection = { titleRes, characters, viewAllRoute, viewAllContentDescriptionTextRes ->
                charactersSection(
                    titleRes = titleRes,
                    characters = characters,
                    viewAllRoute = viewAllRoute,
                    viewAllContentDescriptionTextRes = viewAllContentDescriptionTextRes,
                    staffDetailsRoute = StaffDestinations.StaffDetails.route,
                )
            },
            staff = staff,
            staffSection = { titleRes, staff, viewAllRoute, viewAllContentDescriptionTextRes ->
                staffSection(
                    titleRes = titleRes,
                    staffList = staff,
                    viewAllRoute = viewAllRoute,
                    viewAllContentDescriptionTextRes = viewAllContentDescriptionTextRes,
                )
            },
            studios = { studios },
            studiosSection = { studios, hasMore, onClickListEdit ->
                studiosSection(
                    studios = studios,
                    hasMore = hasMore,
                    mediaRow = { media ->
                        horizontalMediaCardRow(
                            viewer = { viewer },
                            media = media,
                            onClickListEdit = onClickListEdit,
                            mediaWidth = 64.dp,
                            mediaHeight = 96.dp,
                        )
                    },
                )
            },
            activitySortFilterState = viewModel.activitySortFilterController::state,
            activitySection = { onClickListEdit ->
                val activities = viewModel.activities.collectAsLazyPagingItems()
                ActivityList(
                    viewer = viewer,
                    activities = activities,
                    onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                    showMedia = true,
                    allowUserClick = false,
                    sortFilterState = viewModel.activitySortFilterController::state,
                    userRoute = AnimeDestination.User.route,
                    mediaRow = { entry, modifier ->
                        AnimeMediaCompactListRow(
                            viewer = viewer,
                            entry = entry,
                            onClickListEdit = onClickListEdit,
                            modifier = modifier,
                        )
                    },
                )
            },
            showLogOut = showLogOut,
            onLogOutClick = viewModel::logOut,
            bottomNavigationState = bottomNavigationState,
            onClickSettings = onClickSettings,
        )
    }
}
