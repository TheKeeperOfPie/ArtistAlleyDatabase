package com.thekeeperofpie.artistalleydatabase.anime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import androidx.paging.compose.collectAsLazyPagingItems
import com.anilist.fragment.UserNavigationData
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.Either
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.activity.AnimeActivityComposables
import com.thekeeperofpie.artistalleydatabase.anime.activity.AnimeActivityScreen
import com.thekeeperofpie.artistalleydatabase.anime.activity.AnimeMediaDetailsActivityViewModel
import com.thekeeperofpie.artistalleydatabase.anime.activity.activitiesSection
import com.thekeeperofpie.artistalleydatabase.anime.activity.details.ActivityDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.character.AnimeCharactersViewModel
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.character.charactersSection
import com.thekeeperofpie.artistalleydatabase.anime.character.details.AnimeCharacterDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.character.details.CharacterDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.character.media.CharacterMediasScreen
import com.thekeeperofpie.artistalleydatabase.anime.character.media.CharacterMediasViewModel
import com.thekeeperofpie.artistalleydatabase.anime.forum.AnimeForumThreadsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.forum.ForumComposables
import com.thekeeperofpie.artistalleydatabase.anime.forum.ForumRootScreen
import com.thekeeperofpie.artistalleydatabase.anime.forum.ForumSearchScreen
import com.thekeeperofpie.artistalleydatabase.anime.forum.forumThreadsSection
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadScreen
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.comment.ForumThreadCommentTreeScreen
import com.thekeeperofpie.artistalleydatabase.anime.history.MediaHistoryScreen
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeIgnoreScreen
import com.thekeeperofpie.artistalleydatabase.anime.list.AnimeUserListScreen
import com.thekeeperofpie.artistalleydatabase.anime.list.AnimeUserListViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.media.activity.MediaActivitiesScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.activity.MediaActivitiesViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.characters.MediaCharactersScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.characters.MediaCharactersViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsScreen
import com.thekeeperofpie.artistalleydatabase.anime.notifications.NotificationsScreen
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.AnimeMediaDetailsRecommendationsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationComposables
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationsScreen
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.media.MediaRecommendationsScreen
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.media.MediaRecommendationsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.recommendationsSection
import com.thekeeperofpie.artistalleydatabase.anime.review.AnimeMediaDetailsReviewsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.review.ReviewComposables
import com.thekeeperofpie.artistalleydatabase.anime.review.ReviewsScreen
import com.thekeeperofpie.artistalleydatabase.anime.review.details.ReviewDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.review.details.ReviewDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.review.media.MediaReviewsScreen
import com.thekeeperofpie.artistalleydatabase.anime.review.media.MediaReviewsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.review.reviewsSection
import com.thekeeperofpie.artistalleydatabase.anime.schedule.AiringScheduleScreen
import com.thekeeperofpie.artistalleydatabase.anime.search.AnimeSearchViewModel
import com.thekeeperofpie.artistalleydatabase.anime.search.MediaSearchScreen
import com.thekeeperofpie.artistalleydatabase.anime.seasonal.SeasonalScreen
import com.thekeeperofpie.artistalleydatabase.anime.songs.AnimeSongComposables
import com.thekeeperofpie.artistalleydatabase.anime.songs.AnimeSongsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.staff.AnimeStaffViewModel
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.staff.character.StaffCharactersScreen
import com.thekeeperofpie.artistalleydatabase.anime.staff.character.StaffCharactersViewModel
import com.thekeeperofpie.artistalleydatabase.anime.staff.staffSection
import com.thekeeperofpie.artistalleydatabase.anime.studio.StudioMediasScreen
import com.thekeeperofpie.artistalleydatabase.anime.studio.StudioMediasViewModel
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserViewModel
import com.thekeeperofpie.artistalleydatabase.anime.user.UserHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.user.favorite.UserFavoriteCharactersScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.favorite.UserFavoriteMediaScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.favorite.UserFavoriteMediaViewModel
import com.thekeeperofpie.artistalleydatabase.anime.user.favorite.UserFavoriteStaffScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.favorite.UserFavoriteStudiosScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.follow.UserListScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.follow.UserListViewModel
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryNavigator
import com.thekeeperofpie.artistalleydatabase.cds.CdsFromMediaViewModel
import com.thekeeperofpie.artistalleydatabase.cds.cdsSection
import com.thekeeperofpie.artistalleydatabase.cds.grid.CdEntryGridModel
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.ScrollStateSaver
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.navArguments
import com.thekeeperofpie.artistalleydatabase.compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.sharedElementComposable
import com.thekeeperofpie.artistalleydatabase.monetization.UnlockScreen
import kotlin.reflect.KClass

object AnimeNavigator {

    fun initialize(
        navHostController: NavHostController,
        navGraphBuilder: NavGraphBuilder,
        navigationTypeMap: NavigationTypeMap,
        upIconOption: UpIconOption?,
        onClickAuth: () -> Unit,
        onClickSettings: () -> Unit,
        onClickShowLastCrash: () -> Unit,
    ) {
        navGraphBuilder.sharedElementComposable(
            route = AnimeNavDestinations.HOME.id,
            deepLinks = listOf(navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/" }),
        ) {
            val viewModel = hiltViewModel<AnimeRootViewModel>()
            AnimeRootScreen(
                upIconOption = upIconOption,
                viewModel = viewModel,
                onClickAuth = onClickAuth,
                onSubmitAuthToken = viewModel::onSubmitAuthToken,
                onClickSettings = onClickSettings,
                onClickShowLastCrash = onClickShowLastCrash,
            )
        }

        navGraphBuilder.sharedElementComposable(
            route = AnimeNavDestinations.SEARCH_MEDIA.id
                    + "?title={title}"
                    + "&titleRes={titleRes}"
                    + "&tagId={tagId}"
                    + "&genre={genre}"
                    + "&mediaType={mediaType}"
                    + "&sort={sort}"
                    + "&year={year}",
            arguments = navArguments(
                "title",
                "titleRes",
                "tagId",
                "genre",
                "mediaType",
                "sort",
                "year",
            ) {
                type = NavType.StringType
                nullable = true
            }
        ) {
            val title = it.arguments?.getString("title")
            val titleRes = it.arguments?.getString("titleRes")?.toIntOrNull()
            val tagId = it.arguments?.getString("tagId")
            val genre = it.arguments?.getString("genre")
            val mediaType = it.arguments?.getString("mediaType")
                ?.let { MediaType.safeValueOf(it).takeUnless { it == MediaType.UNKNOWN__ } }
                ?: MediaType.ANIME
            val sort = it.arguments?.getString("sort")
                ?.let {
                    try {
                        MediaSortOption.valueOf(it)
                    } catch (ignored: Throwable) {
                        null
                    }
                }
                ?: MediaSortOption.TRENDING
            val year = it.arguments?.getString("year")?.toIntOrNull()
            val viewModel = hiltViewModel<AnimeSearchViewModel>().apply {
                initialize(
                    defaultMediaSort = sort,
                    genre = genre,
                    year = year,
                    searchType = if (mediaType == MediaType.MANGA) {
                        AnimeSearchViewModel.SearchType.MANGA
                    } else {
                        AnimeSearchViewModel.SearchType.ANIME
                    },
                    // TODO: Explicitly pass lockSort
                    lockSort = tagId == null && genre == null,
                )
            }
            MediaSearchScreen(
                title = if (titleRes != null) {
                    Either.Left(titleRes)
                } else {
                    Either.Right(title.orEmpty())
                },
                upIconOption = UpIconOption.Back(navHostController),
                viewModel = viewModel,
                tagId = tagId,
                genre = genre,
            )
        }

        navGraphBuilder.sharedElementComposable(
            route = AnimeNavDestinations.USER_LIST.id +
                    "?userId={userId}" +
                    "&userName={userName}" +
                    "&mediaType={mediaType}" +
                    "&mediaListStatus={mediaListStatus}",
            arguments = navArguments(
                "userId",
                "userName",
                "mediaType",
                "mediaListStatus",
            ) {
                type = NavType.StringType
                nullable = true
            },
        ) {
            val arguments = it.arguments!!
            val userId = arguments.getString("userId")
            val userName = arguments.getString("userName")
            val mediaType = arguments.getString("mediaType")
                ?.let { MediaType.safeValueOf(it).takeUnless { it == MediaType.UNKNOWN__ } }
                ?: MediaType.ANIME
            val mediaListStatus = arguments.getString("mediaListStatus")
                ?.let {
                    MediaListStatus.safeValueOf(it).takeUnless { it == MediaListStatus.UNKNOWN__ }
                }
            UserMediaListScreen(
                userId = userId,
                userName = userName,
                mediaType = mediaType,
                upIconOption = UpIconOption.Back(navHostController),
                mediaListStatus = mediaListStatus,
                scrollStateSaver = ScrollStateSaver.fromMap(
                    AnimeNavDestinations.USER_LIST.id,
                    ScrollStateSaver.scrollPositions(),
                ),
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestinations.MediaDetails>(
            navigationTypeMap = navigationTypeMap,
            deepLinks = listOf(
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/anime/{mediaId}" },
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/anime/{mediaId}/.*" },
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/manga/{mediaId}" },
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/manga/{mediaId}/.*" },
            ),
        ) {
            val destination = it.toRoute<AnimeDestinations.MediaDetails>()
            val mediaDetailsViewModel = hiltViewModel<AnimeMediaDetailsViewModel>()
            val headerValues = MediaHeaderValues(
                params = destination.headerParams,
                media = { mediaDetailsViewModel.entry.result?.media },
                favoriteUpdate = { mediaDetailsViewModel.favoritesToggleHelper.favorite },
            )

            val charactersViewModel = hiltViewModel<AnimeCharactersViewModel>()
                .apply { initialize(mediaDetailsViewModel) }
            val charactersDeferred =
                charactersViewModel.charactersDeferred.collectAsLazyPagingItems()

            val staffViewModel = hiltViewModel<AnimeStaffViewModel>()
                .apply { initialize(mediaDetailsViewModel) }
            val staff = staffViewModel.staff.collectAsLazyPagingItems()

            val songsViewModel = hiltViewModel<AnimeSongsViewModel>()
                .apply { initialize(mediaDetailsViewModel) }
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                lifecycleOwner.lifecycle.addObserver(songsViewModel)
                onDispose { lifecycleOwner.lifecycle.removeObserver(songsViewModel) }
            }

            DisposableEffect(lifecycleOwner) {
                onDispose { songsViewModel.animeSongsCollapseAll() }
            }

            val cdsViewModel = hiltViewModel<CdsFromMediaViewModel>()

            val viewer by mediaDetailsViewModel.viewer.collectAsState()

            val recommendationsViewModel =
                hiltViewModel<AnimeMediaDetailsRecommendationsViewModel>()
                    .apply { initialize(mediaDetailsViewModel) }

            val activitiesViewModel = hiltViewModel<AnimeMediaDetailsActivityViewModel>()
                .apply { initialize(mediaDetailsViewModel) }
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

            val forumThreadsViewModel = hiltViewModel<AnimeForumThreadsViewModel>()
                .apply { initialize(mediaDetailsViewModel) }

            val reviewsViewModel = hiltViewModel<AnimeMediaDetailsReviewsViewModel>()
                .apply { initialize(mediaDetailsViewModel) }

            val navigationCallback = LocalNavigationCallback.current

            val sharedElementKey = destination.sharedElementKey
            val mediaTitle = mediaDetailsViewModel.entry.result?.media?.title?.primaryTitle()
            AnimeMediaDetailsScreen(
                viewModel = mediaDetailsViewModel,
                upIconOption = UpIconOption.Back(navHostController),
                headerValues = headerValues,
                sharedElementKey = sharedElementKey,
                charactersCount = {
                    charactersDeferred.itemCount
                        .coerceAtLeast(charactersViewModel.charactersInitial.size)
                },
                charactersSection = { screenKey, entry, coverImageWidthToHeightRatio ->
                    charactersSection(
                        screenKey = screenKey,
                        titleRes = R.string.anime_media_details_characters_label,
                        charactersInitial = charactersViewModel.charactersInitial,
                        charactersDeferred = { charactersDeferred },
                        mediaId = entry.mediaId,
                        media = entry.media,
                        mediaFavorite = mediaDetailsViewModel.favoritesToggleHelper.favorite,
                        mediaCoverImageWidthToHeightRatio = coverImageWidthToHeightRatio,
                        viewAllContentDescriptionTextRes = R.string.anime_media_details_view_all_content_description,
                    )
                },
                staffCount = { staff.itemCount },
                staffSection = {
                    staffSection(
                        screenKey = it,
                        titleRes = R.string.anime_media_details_staff_label,
                        staffList = staff,
                    )
                },
                songsSectionMetadata = if (!songsViewModel.enabled) null else AnimeMediaDetailsScreen.SectionIndexInfo.SectionMetadata.ListSection(
                    items = songsViewModel.animeSongs?.entries,
                    aboveFold = AnimeSongComposables.SONGS_ABOVE_FOLD,
                    hasMore = false,
                ),
                songsSection = { screenKey, expanded, onExpandedChange ->
                    AnimeSongComposables.songsSection(
                        screenKey = screenKey,
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
                cdsSection = { screenKey ->
                    cdsSection(
                        screenKey = screenKey,
                        cdEntries = cdsViewModel.cdEntries,
                        onClickEntry = { _, entry ->
                            navigationCallback.onCdEntryClick(
                                model = entry,
                                imageCornerDp = 12.dp,
                            )
                        }
                    )
                },
                requestLoadMedia2 = mediaDetailsViewModel::requestLoadMedia2,
                recommendationsSectionMetadata = AnimeMediaDetailsScreen.SectionIndexInfo.SectionMetadata.ListSection(
                    items = recommendationsViewModel.recommendations?.recommendations,
                    aboveFold = RecommendationComposables.RECOMMENDATIONS_ABOVE_FOLD,
                    hasMore = recommendationsViewModel.recommendations?.hasMore ?: true,
                ),
                recommendationsSection = { screenKey, expanded, onExpandedChange, onClickListEdit, coverImageWidthToHeightRatio ->
                    val entry = recommendationsViewModel.recommendations
                    recommendationsSection(
                        screenKey = screenKey,
                        viewer = viewer,
                        entry = entry,
                        expanded = expanded,
                        onExpandedChange = onExpandedChange,
                        onClickListEdit = onClickListEdit,
                        onClickViewAll = {
                            val media = mediaDetailsViewModel.entry.result?.media
                            it.navigate(
                                AnimeDestinations.MediaRecommendations(
                                    mediaId = mediaDetailsViewModel.mediaId,
                                    headerParams = MediaHeaderParams(
                                        title = mediaTitle,
                                        coverImageWidthToHeightRatio = coverImageWidthToHeightRatio(),
                                        media = media,
                                        favorite = mediaDetailsViewModel.favoritesToggleHelper.favorite
                                            ?: media?.isFavourite,
                                    )
                                )
                            )
                        },
                        onUserRecommendationRating = recommendationsViewModel.recommendationToggleHelper::toggle,
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
                activitiesSection = { screenKey, expanded, onExpandedChanged, onClickListEdit, coverImageWidthToHeightRatio ->
                    activitiesSection(
                        screenKey = screenKey,
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
                            val entry = mediaDetailsViewModel.entry.result
                            if (entry != null) {
                                it.navigate(
                                    AnimeDestinations.MediaActivities(
                                        mediaId = mediaDetailsViewModel.mediaId,
                                        showFollowing = activityTab == AnimeMediaDetailsActivityViewModel.ActivityTab.FOLLOWING,
                                        headerParams = MediaHeaderParams(
                                            title = mediaTitle,
                                            coverImageWidthToHeightRatio = coverImageWidthToHeightRatio(),
                                            media = entry.media,
                                            favorite = mediaDetailsViewModel.favoritesToggleHelper.favorite
                                                ?: entry.media.isFavourite,
                                        )
                                    )
                                )
                            }
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
                            val entry = mediaDetailsViewModel.entry.result
                            it.onForumMediaCategoryClick(
                                entry?.media?.title?.userPreferred,
                                mediaDetailsViewModel.mediaId
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
                reviewsSection = { screenKey, expanded, onExpandedChange, coverImageWidthToHeightRatio ->
                    reviewsSection(
                        screenKey = screenKey,
                        entry = reviewsViewModel.reviews,
                        expanded = expanded,
                        onExpandedChange = onExpandedChange,
                        onClickViewAll = {
                            val media = mediaDetailsViewModel.entry.result?.media
                            it.navigate(
                                AnimeDestinations.MediaReviews(
                                    mediaId = mediaDetailsViewModel.mediaId,
                                    headerParams = MediaHeaderParams(
                                        title = mediaTitle,
                                        coverImageWidthToHeightRatio = coverImageWidthToHeightRatio(),
                                        media = media,
                                        favorite = mediaDetailsViewModel.favoritesToggleHelper.favorite
                                            ?: media?.isFavourite,
                                    )
                                )
                            )
                        },
                        onReviewClick = { navigationCallback, item ->
                            val media = mediaDetailsViewModel.entry.result?.media
                            navigationCallback.navigate(
                                AnimeDestinations.ReviewDetails(
                                    reviewId = item.id.toString(),
                                    headerParams = MediaHeaderParams(
                                        title = mediaTitle,
                                        coverImageWidthToHeightRatio = coverImageWidthToHeightRatio(),
                                        media = media,
                                        favorite = mediaDetailsViewModel.favoritesToggleHelper.favorite
                                            ?: media?.isFavourite,
                                    )
                                )
                            )
                        },
                    )
                },
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestinations.CharacterDetails>(
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
            val viewModel = hiltViewModel<AnimeCharacterDetailsViewModel>()
            val destination = it.toRoute<AnimeDestinations.CharacterDetails>()
            val headerValues = CharacterHeaderValues(
                params = destination.headerParams,
                character = { viewModel.entry.result?.character },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            CharacterDetailsScreen(
                viewModel = viewModel,
                upIconOption = UpIconOption.Back(navHostController),
                headerValues = headerValues,
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestinations.CharacterMedias>(
            navigationTypeMap = navigationTypeMap
        ) {
            val destination = it.toRoute<AnimeDestinations.CharacterMedias>()
            val viewModel = hiltViewModel<CharacterMediasViewModel>()
            val headerValues = CharacterHeaderValues(
                params = destination.headerParams,
                character = { viewModel.entry.result?.character },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            CharacterMediasScreen(
                viewModel = viewModel,
                upIconOption = UpIconOption.Back(navHostController),
                headerValues = headerValues,
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestinations.StaffDetails>(
            navigationTypeMap = navigationTypeMap,
            deepLinks = listOf(
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/staff/{staffId}" },
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/staff/{staffId}/.*" },
            ),
        ) {
            val viewModel = hiltViewModel<StaffDetailsViewModel>()
            val destination = it.toRoute<AnimeDestinations.StaffDetails>()
            val headerValues = StaffHeaderValues(
                params = destination.headerParams,
                staff = { viewModel.entry?.staff },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            StaffDetailsScreen(
                viewModel = viewModel,
                upIconOption = UpIconOption.Back(navHostController),
                headerValues = headerValues,
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestinations.StaffCharacters>(
            navigationTypeMap = navigationTypeMap,
        ) {
            val viewModel = hiltViewModel<StaffCharactersViewModel>()
            val destination = it.toRoute<AnimeDestinations.StaffCharacters>()
            val headerValues = StaffHeaderValues(
                params = destination.headerParams,
                staff = { viewModel.entry.result?.staff },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            StaffCharactersScreen(
                viewModel = viewModel,
                upIconOption = UpIconOption.Back(navHostController),
                headerValues = headerValues,
            )
        }

        navGraphBuilder.sharedElementComposable(
            route = AnimeNavDestinations.USER.id
                    + "?userId={userId}${UserHeaderValues.routeSuffix}",
            deepLinks = listOf(
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/user/{userId}" },
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/user/{userId}/.*" },
            ),
            arguments = navArguments("userId") {
                type = NavType.StringType
                nullable = true
            } + UserHeaderValues.navArguments(),
        ) {
            val viewModel = hiltViewModel<AniListUserViewModel>()
            val headerValues = UserHeaderValues(it.arguments) { viewModel.entry?.user }
            AniListUserScreen(
                viewModel = viewModel,
                upIconOption = UpIconOption.Back(navHostController),
                headerValues = headerValues,
            )
        }

        navGraphBuilder.sharedElementComposable(
            route = AnimeNavDestinations.IGNORED.id + "?mediaType={mediaType}",
            arguments = navArguments("mediaType") {
                type = NavType.StringType
                nullable = true
            },
        ) {
            AnimeIgnoreScreen(UpIconOption.Back(navHostController))
        }

        navGraphBuilder.sharedElementComposable(route = AnimeNavDestinations.AIRING_SCHEDULE.id) {
            AiringScheduleScreen(
                onClickBack = { navHostController.navigateUp() },
            )
        }

        navGraphBuilder.sharedElementComposable(
            route = "${AnimeNavDestinations.SEASONAL.id}?type={type}",
            arguments = navArguments("type") {
                type = NavType.StringType
                nullable = true
            },
        ) {
            SeasonalScreen(upIconOption = UpIconOption.Back(navHostController))
        }

        navGraphBuilder.sharedElementComposable(AnimeNavDestinations.NEWS.id) {
            AnimeNewsScreen()
        }

        navGraphBuilder.sharedElementComposable(AnimeNavDestinations.ACTIVITY.id) {
            AnimeActivityScreen()
        }

        navGraphBuilder.sharedElementComposable(AnimeNavDestinations.NOTIFICATIONS.id) {
            NotificationsScreen(upIconOption = UpIconOption.Back(navHostController))
        }

        navGraphBuilder.sharedElementComposable<AnimeDestinations.MediaCharacters>(
            navigationTypeMap = navigationTypeMap,
        ) {
            val viewModel = hiltViewModel<MediaCharactersViewModel>()
            val destination = it.toRoute<AnimeDestinations.MediaCharacters>()
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

        navGraphBuilder.sharedElementComposable<AnimeDestinations.MediaReviews>(
            navigationTypeMap = navigationTypeMap,
        ) {
            val viewModel = hiltViewModel<MediaReviewsViewModel>()
            val destination = it.toRoute<AnimeDestinations.MediaReviews>()
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

        navGraphBuilder.sharedElementComposable(route = AnimeNavDestinations.REVIEWS.id) {
            ReviewsScreen(
                upIconOption = UpIconOption.Back(navHostController),
            )
        }

        navGraphBuilder.sharedElementComposable(route = AnimeNavDestinations.RECOMMENDATIONS.id) {
            RecommendationsScreen(
                upIconOption = UpIconOption.Back(navHostController),
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestinations.MediaRecommendations>(
            navigationTypeMap = navigationTypeMap,
        ) {
            val viewModel = hiltViewModel<MediaRecommendationsViewModel>()
            val destination = it.toRoute<AnimeDestinations.MediaRecommendations>()
            val headerValues = MediaHeaderValues(
                params = destination.headerParams,
                media = { viewModel.entry.result?.media },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            MediaRecommendationsScreen(
                viewModel = viewModel,
                upIconOption = UpIconOption.Back(navHostController),
                headerValues = headerValues,
            )
        }

        navGraphBuilder.sharedElementComposable<AnimeDestinations.MediaActivities>(
            navigationTypeMap = navigationTypeMap,
        ) {
            val viewModel = hiltViewModel<MediaActivitiesViewModel>()
            val destination = it.toRoute<AnimeDestinations.MediaActivities>()
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

        navGraphBuilder.sharedElementComposable<AnimeDestinations.ReviewDetails>(
            navigationTypeMap = navigationTypeMap,
            deepLinks = listOf(
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/review/{reviewId}" },
                navDeepLink {
                    uriPattern = "${AniListUtils.ANILIST_BASE_URL}/review/{reviewId}/.*"
                },
            ),
        ) {
            val viewModel = hiltViewModel<ReviewDetailsViewModel>()
            val destination = it.toRoute<AnimeDestinations.ReviewDetails>()
            val headerValues = MediaHeaderValues(
                params = destination.headerParams,
                media = { viewModel.entry?.review?.media },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            ReviewDetailsScreen(
                viewModel = viewModel,
                upIconOption = UpIconOption.Back(navHostController),
                headerValues = headerValues,
            )
        }

        navGraphBuilder.sharedElementComposable(
            route = AnimeNavDestinations.STUDIO_MEDIAS.id
                    + "?studioId={studioId}&name={name}&favorite={favorite}",
            deepLinks = listOf(
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/studio/{studioId}" },
                navDeepLink {
                    uriPattern = "${AniListUtils.ANILIST_BASE_URL}/studio/{studioId}/.*"
                },
            ),
            arguments = listOf(
                navArgument("studioId") {
                    type = NavType.StringType
                    nullable = false
                },
            ) + navArguments("name", "favorite") {
                type = NavType.StringType
                nullable = true
            },
        ) {
            val arguments = it.arguments!!
            val name = arguments.getString("name")
            val favorite = arguments.getString("favorite")?.toBooleanStrictOrNull()

            val viewModel = hiltViewModel<StudioMediasViewModel>()

            StudioMediasScreen(
                upIconOption = UpIconOption.Back(navHostController),
                viewModel = viewModel,
                name = { viewModel.entry.result?.studio?.name ?: name ?: "" },
                favorite = {
                    viewModel.favoritesToggleHelper.favorite
                        ?: viewModel.entry.result?.studio?.isFavourite
                        ?: favorite
                },
            )
        }

        navGraphBuilder.sharedElementComposable(
            route = AnimeNavDestinations.ACTIVITY_DETAILS.id + "?activityId={activityId}",
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "${AniListUtils.ANILIST_BASE_URL}/activity/{activityId}"
                },
                navDeepLink {
                    uriPattern = "${AniListUtils.ANILIST_BASE_URL}/activity/{activityId}/.*"
                },
            ),
            arguments = listOf(
                navArgument("activityId") {
                    type = NavType.StringType
                    nullable = false
                },
            ),
        ) {
            ActivityDetailsScreen(upIconOption = UpIconOption.Back(navHostController))
        }

        navGraphBuilder.sharedElementComposable(route = AnimeNavDestinations.FEATURE_TIERS.id) {
            UnlockScreen(
                upIconOption = UpIconOption.Back(navHostController),
                onClickSettings = null,
            )
        }

        navGraphBuilder.sharedElementComposable(route = AnimeNavDestinations.FORUM.id) {
            ForumRootScreen(
                upIconOption = UpIconOption.Back(navHostController),
            )
        }

        navGraphBuilder.sharedElementComposable(
            route = AnimeNavDestinations.FORUM_SEARCH.id
                    + "?title={title}"
                    + "&titleRes={titleRes}"
                    + "&sort={sort}"
                    + "&categoryId={categoryId}"
                    + "&mediaCategoryId={mediaCategoryId}",
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
            arguments = listOf("title", "titleRes", "sort", "categoryId", "mediaCategoryId")
                .map {
                    navArgument(it) {
                        type = NavType.StringType
                        nullable = true
                    }
                },
        ) {
            ForumSearchScreen(
                upIconOption = UpIconOption.Back(navHostController),
                title = it.arguments?.getString("title")
                    ?: it.arguments?.getString("titleRes")
                        ?.toIntOrNull()
                        ?.let { stringResource(it) },
            )
        }

        // TODO: Forum deep links
        navGraphBuilder.sharedElementComposable(
            route = AnimeNavDestinations.FORUM_THREAD.id
                    + "?threadId={threadId}"
                    + "&title={title}",
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "${AniListUtils.ANILIST_BASE_URL}/forum/thread/{threadId}"
                },
                navDeepLink {
                    uriPattern = "${AniListUtils.ANILIST_BASE_URL}/forum/thread/{threadId}/.*"
                },
            ),
            arguments = listOf(
                navArgument("threadId") {
                    type = NavType.StringType
                },
                navArgument("title") {
                    type = NavType.StringType
                    nullable = true
                },
            ),
        ) {
            ForumThreadScreen(
                upIconOption = UpIconOption.Back(navHostController),
                title = it.arguments?.getString("title"),
            )
        }

        navGraphBuilder.sharedElementComposable(
            route = AnimeNavDestinations.FORUM_THREAD_COMMENT.id
                    + "?threadId={threadId}"
                    + "&commentId={commentId}"
                    + "&title={title}",
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
            arguments = listOf(
                navArgument("threadId") {
                    type = NavType.StringType
                },
                navArgument("commentId") {
                    type = NavType.StringType
                },
                navArgument("title") {
                    type = NavType.StringType
                    nullable = true
                },
            ),
        ) {
            ForumThreadCommentTreeScreen(
                upIconOption = UpIconOption.Back(navHostController),
                title = it.arguments?.getString("title"),
            )
        }

        navGraphBuilder.sharedElementComposable(
            route = AnimeNavDestinations.MEDIA_HISTORY.id + "?mediaType={mediaType}",
            arguments = listOf(
                navArgument("mediaType") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) {
            MediaHistoryScreen(
                upIconOption = UpIconOption.Back(navHostController),
            )
        }

        navGraphBuilder.sharedElementComposable(
            route = AnimeNavDestinations.USER_FOLLOWING.id
                    + "?userId={userId}"
                    + "&userName={userName}",
            arguments = listOf("userId", "userName").map {
                navArgument(it) {
                    type = NavType.StringType
                    nullable = true
                }
            },
        ) {
            val userId = it.arguments?.getString("userId")
            val viewModel = hiltViewModel<UserListViewModel.Following>()
            UserListScreen(
                screenKey = AnimeNavDestinations.USER_FOLLOWING.id,
                upIconOption = UpIconOption.Back(navHostController),
                viewModel = viewModel,
                title = {
                    if (userId == null) {
                        stringResource(R.string.anime_user_following_you)
                    } else {
                        stringResource(
                            R.string.anime_user_following_user,
                            it.arguments?.getString("userName").orEmpty()
                        )
                    }
                },
            )
        }

        navGraphBuilder.sharedElementComposable(
            route = AnimeNavDestinations.USER_FOLLOWERS.id
                    + "?userId={userId}"
                    + "&userName={userName}",
            arguments = listOf("userId", "userName").map {
                navArgument(it) {
                    type = NavType.StringType
                    nullable = true
                }
            },
        ) {
            val userId = it.arguments?.getString("userId")
            val viewModel = hiltViewModel<UserListViewModel.Followers>()
            UserListScreen(
                screenKey = AnimeNavDestinations.USER_FOLLOWERS.id,
                upIconOption = UpIconOption.Back(navHostController),
                viewModel = viewModel,
                title = {
                    if (userId == null) {
                        stringResource(R.string.anime_user_followers_you)
                    } else {
                        stringResource(
                            R.string.anime_user_followers_user,
                            it.arguments?.getString("userName").orEmpty()
                        )
                    }
                },
            )
        }

        navGraphBuilder.sharedElementComposable(
            route = AnimeNavDestinations.USER_FAVORITE_MEDIA.id
                    + "?userId={userId}"
                    + "&userName={userName}"
                    + "&mediaType={mediaType}",
            arguments = listOf("userId", "userName", "mediaType").map {
                navArgument(it) {
                    type = NavType.StringType
                    nullable = true
                }
            },
        ) {
            val userId = it.arguments?.getString("userId")
            val viewModel: UserFavoriteMediaViewModel = hiltViewModel()
            val userName = it.arguments?.getString("userName").orEmpty()
            UserFavoriteMediaScreen(
                upIconOption = UpIconOption.Back(navHostController),
                viewModel = viewModel,
                title = {
                    if (viewModel.mediaType == MediaType.ANIME) {
                        if (userId == null) {
                            stringResource(R.string.anime_user_favorite_anime_you)
                        } else {
                            stringResource(R.string.anime_user_favorite_anime_user, userName)
                        }
                    } else {
                        if (userId == null) {
                            stringResource(R.string.anime_user_favorite_manga_you)
                        } else {
                            stringResource(R.string.anime_user_favorite_manga_user, userName)
                        }
                    }
                },
            )
        }

        navGraphBuilder.sharedElementComposable(
            route = AnimeNavDestinations.USER_FAVORITE_CHARACTERS.id
                    + "?userId={userId}"
                    + "&userName={userName}",
            arguments = listOf("userId", "userName").map {
                navArgument(it) {
                    type = NavType.StringType
                    nullable = true
                }
            },
        ) {
            val userId = it.arguments?.getString("userId")
            val userName = it.arguments?.getString("userName").orEmpty()
            UserFavoriteCharactersScreen(
                upIconOption = UpIconOption.Back(navHostController),
                title = {
                    if (userId == null) {
                        stringResource(R.string.anime_user_favorite_characters_you)
                    } else {
                        stringResource(R.string.anime_user_favorite_characters_user, userName)
                    }
                },
            )
        }

        navGraphBuilder.sharedElementComposable(
            route = AnimeNavDestinations.USER_FAVORITE_STAFF.id
                    + "?userId={userId}"
                    + "&userName={userName}",
            arguments = listOf("userId", "userName").map {
                navArgument(it) {
                    type = NavType.StringType
                    nullable = true
                }
            },
        ) {
            val userId = it.arguments?.getString("userId")
            val userName = it.arguments?.getString("userName").orEmpty()
            UserFavoriteStaffScreen(
                upIconOption = UpIconOption.Back(navHostController),
                title = {
                    if (userId == null) {
                        stringResource(R.string.anime_user_favorite_staff_you)
                    } else {
                        stringResource(R.string.anime_user_favorite_staff_user, userName)
                    }
                },
            )
        }

        navGraphBuilder.sharedElementComposable(
            route = AnimeNavDestinations.USER_FAVORITE_STUDIOS.id
                    + "?userId={userId}"
                    + "&userName={userName}",
            arguments = listOf("userId", "userName").map {
                navArgument(it) {
                    type = NavType.StringType
                    nullable = true
                }
            },
        ) {
            val userId = it.arguments?.getString("userId")
            val userName = it.arguments?.getString("userName").orEmpty()
            UserFavoriteStudiosScreen(
                upIconOption = UpIconOption.Back(navHostController),
                title = {
                    if (userId == null) {
                        stringResource(R.string.anime_user_favorite_studios_you)
                    } else {
                        stringResource(R.string.anime_user_favorite_studios_user, userName)
                    }
                },
            )
        }
    }

    fun onTagClick(
        navHostController: NavHostController,
        mediaType: MediaType,
        tagId: String,
        tagName: String,
    ) {
        navHostController.navigate(
            AnimeNavDestinations.SEARCH_MEDIA.id +
                    "?title=$tagName&tagId=$tagId&mediaType=${mediaType.name}"
        )
    }

    fun onGenreClick(navHostController: NavHostController, mediaType: MediaType, genre: String) {
        navHostController.navigate(
            AnimeNavDestinations.SEARCH_MEDIA.id +
                    "?title=$genre&genre=$genre&mediaType=${mediaType.name}"
        )
    }

    fun onUserClick(
        navHostController: NavHostController,
        user: UserNavigationData,
        imageWidthToHeightRatio: Float,
    ) = navHostController.navigate(
        AnimeNavDestinations.USER.id +
                "?userId=${user.id}" +
                UserHeaderValues.routeSuffix(user, imageWidthToHeightRatio)
    )

    fun onUserListClick(
        navHostController: NavHostController,
        userId: String,
        userName: String?,
        mediaType: MediaType?,
        mediaListStatus: MediaListStatus?,
    ) {
        navHostController.navigate(
            AnimeNavDestinations.USER_LIST.id +
                    "?userId=$userId" +
                    "&userName=$userName" +
                    "&mediaType=${mediaType?.rawValue}" +
                    "&mediaListStatus=${mediaListStatus?.rawValue}"
        )
    }

    fun onStudioClick(
        navHostController: NavHostController,
        studioId: String,
        name: String,
    ) = navHostController.navigate(
        AnimeNavDestinations.STUDIO_MEDIAS.id +
                "?studioId=$studioId&name=$name"
    )

    fun onActivityDetailsClick(
        navHostController: NavHostController,
        activityId: String,
    ) = navHostController.navigate(
        AnimeNavDestinations.ACTIVITY_DETAILS.id + "?activityId=$activityId"
    )

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
        val viewModel = hiltViewModel<AnimeUserListViewModel>(key = mediaType.rawValue)
            .apply { initialize(userId, userName, mediaType, mediaListStatus) }
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
        private val languageOptionMedia: AniListLanguageOption = AniListLanguageOption.DEFAULT,
        private val languageOptionCharacters: AniListLanguageOption = AniListLanguageOption.DEFAULT,
        private val languageOptionStaff: AniListLanguageOption = AniListLanguageOption.DEFAULT,
    ) {

        fun onTagClick(mediaType: MediaType, id: String, name: String) {
            navHostController?.let { onTagClick(it, mediaType, id, name) }
        }

        fun onUserListClick(userId: String, userName: String?, mediaType: MediaType?) {
            navHostController?.let {
                onUserListClick(
                    navHostController = it,
                    userId = userId,
                    userName = userName,
                    mediaType = mediaType,
                    mediaListStatus = null,
                )
            }
        }

        fun onUserClick(userNavigationData: UserNavigationData, imageWidthToHeightRatio: Float) {
            navHostController?.let { onUserClick(it, userNavigationData, imageWidthToHeightRatio) }
        }

        fun onCharacterLongClick(id: String) {
            // TODO
        }

        fun onStaffLongClick(id: String) {
            // TODO
        }

        fun onStudioClick(id: String, name: String) =
            navHostController?.let { onStudioClick(it, id, name) }

        fun onGenreClick(mediaType: MediaType, genre: String) {
            navHostController?.let { onGenreClick(it, mediaType, genre) }
        }

        fun onClickViewIgnored(mediaType: MediaType? = null) {
            navHostController?.navigate(
                AnimeNavDestinations.IGNORED.id
                        + "?mediaType=${mediaType?.rawValue}"
            )
        }

        fun onClickViewMediaHistory(mediaType: MediaType? = null) {
            navHostController?.navigate(
                AnimeNavDestinations.MEDIA_HISTORY.id
                        + "?mediaType=${mediaType?.rawValue}"
            )
        }

        fun onCdEntryClick(model: CdEntryGridModel, imageCornerDp: Dp?) {
            navHostController?.let {
                cdEntryNavigator?.onCdEntryClick(it, listOf(model.id.valueId), imageCornerDp)
            }
        }

        fun onNotificationsClick() {
            navHostController?.navigate(AnimeNavDestinations.NOTIFICATIONS.id)
        }

        fun onAiringScheduleClick() {
            navHostController?.navigate(AnimeNavDestinations.AIRING_SCHEDULE.id)
        }

        fun onSeasonalClick() {
            navHostController?.navigate(AnimeNavDestinations.SEASONAL.id)
        }

        fun onActivityDetailsClick(activityId: String) {
            navHostController?.let {
                onActivityDetailsClick(it, activityId)
            }
        }

        fun onForumRootClick() = navHostController?.navigate(AnimeNavDestinations.FORUM.id)

        fun onForumSearchClick() = navHostController?.navigate(AnimeNavDestinations.FORUM_SEARCH.id)

        fun onForumCategoryClick(categoryName: String, categoryId: String) {
            navHostController?.navigate(
                AnimeNavDestinations.FORUM_SEARCH.id
                        + "?title=$categoryName&categoryId=$categoryId"
            )
        }

        fun onForumMediaCategoryClick(mediaCategoryName: String?, mediaCategoryId: String) {
            navHostController?.navigate(
                AnimeNavDestinations.FORUM_SEARCH.id
                        + "?title=$mediaCategoryName&mediaCategoryId=$mediaCategoryId"
            )
        }

        fun onForumThreadClick(title: String?, threadId: String) {
            navHostController?.navigate(
                AnimeNavDestinations.FORUM_THREAD.id
                        + "?title=$title&threadId=$threadId"
            )
        }

        fun onForumThreadCommentClick(title: String?, threadId: String, commentId: String) {
            navHostController?.navigate(
                AnimeNavDestinations.FORUM_THREAD_COMMENT.id
                        + "?title=$title&threadId=$threadId&commentId=$commentId"
            )
        }

        fun onFollowingClick(userId: String?, userName: String?) {
            navHostController?.navigate(
                AnimeNavDestinations.USER_FOLLOWING.id
                        + "?userId=$userId&userName=$userName"
            )
        }

        fun onFollowersClick(userId: String?, userName: String?) {
            navHostController?.navigate(
                AnimeNavDestinations.USER_FOLLOWERS.id
                        + "?userId=$userId&userName=$userName"
            )
        }

        fun onUserFavoriteMediaClick(userId: String?, userName: String?, mediaType: MediaType) {
            navHostController?.navigate(
                AnimeNavDestinations.USER_FAVORITE_MEDIA.id
                        + "?userId=$userId&userName=$userName&mediaType=${mediaType.rawValue}"
            )
        }

        fun onUserFavoriteCharactersClick(userId: String?, userName: String?) {
            navHostController?.navigate(
                AnimeNavDestinations.USER_FAVORITE_CHARACTERS.id
                        + "?userId=$userId&userName=$userName"
            )
        }

        fun onUserFavoriteStaffClick(userId: String?, userName: String?) {
            navHostController?.navigate(
                AnimeNavDestinations.USER_FAVORITE_STAFF.id
                        + "?userId=$userId&userName=$userName"
            )
        }

        fun onUserFavoriteStudiosClick(userId: String?, userName: String?) {
            navHostController?.navigate(
                AnimeNavDestinations.USER_FAVORITE_STUDIOS.id
                        + "?userId=$userId&userName=$userName"
            )
        }

        fun navigate(route: String) = navHostController?.navigate(route)

        inline fun <reified T : Any> navigate(route: T) = navigate(T::class, route)

        fun <T : Any> navigate(clazz: KClass<T>, route: T) {
            navHostController?.navigate<T>(route)
        }

        fun navigateUp() = navHostController?.navigateUp()
    }
}

val LocalNavigationCallback = staticCompositionLocalOf { AnimeNavigator.NavigationCallback(null) }
