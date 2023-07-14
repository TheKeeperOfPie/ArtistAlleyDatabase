package com.thekeeperofpie.artistalleydatabase.anime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.anilist.fragment.CharacterHeaderData
import com.anilist.fragment.CharacterNavigationData
import com.anilist.fragment.MediaHeaderData
import com.anilist.fragment.MediaNavigationData
import com.anilist.fragment.MediaPreviewWithDescription
import com.anilist.fragment.StaffHeaderData
import com.anilist.fragment.StaffNavigationData
import com.anilist.fragment.UserFavoriteMediaNode
import com.anilist.fragment.UserNavigationData
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.Either
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.activity.AnimeActivityScreen
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.character.CharactersScreen
import com.thekeeperofpie.artistalleydatabase.anime.character.CharactersViewModel
import com.thekeeperofpie.artistalleydatabase.anime.character.details.AnimeCharacterDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.character.details.CharacterDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.character.media.CharacterMediasScreen
import com.thekeeperofpie.artistalleydatabase.anime.character.media.CharacterMediasViewModel
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeIgnoreScreen
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreViewModel
import com.thekeeperofpie.artistalleydatabase.anime.list.AnimeUserListScreen
import com.thekeeperofpie.artistalleydatabase.anime.list.AnimeUserListViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeMediaFilterController
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsScreen
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationsScreen
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.review.ReviewsScreen
import com.thekeeperofpie.artistalleydatabase.anime.review.ReviewsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.review.details.ReviewDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.review.details.ReviewDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.schedule.AiringScheduleScreen
import com.thekeeperofpie.artistalleydatabase.anime.search.AnimeSearchScreen
import com.thekeeperofpie.artistalleydatabase.anime.search.AnimeSearchViewModel
import com.thekeeperofpie.artistalleydatabase.anime.seasonal.SeasonalScreen
import com.thekeeperofpie.artistalleydatabase.anime.seasonal.SeasonalViewModel
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.staff.character.StaffCharactersScreen
import com.thekeeperofpie.artistalleydatabase.anime.staff.character.StaffCharactersViewModel
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserViewModel
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryNavigator
import com.thekeeperofpie.artistalleydatabase.cds.grid.CdEntryGridModel
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.ScrollStateSaver
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption

object AnimeNavigator {

    fun initialize(
        navHostController: NavHostController,
        navGraphBuilder: NavGraphBuilder,
        upIconOption: UpIconOption?,
        onClickAuth: () -> Unit,
        onClickSettings: () -> Unit,
        navigationCallback: NavigationCallback,
    ) {
        navGraphBuilder.composable(
            route = AnimeNavDestinations.HOME.id,
            deepLinks = listOf(navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/" }),
        ) {
            val viewModel = hiltViewModel<AnimeRootViewModel>()
            AnimeRootScreen(
                upIconOption = upIconOption,
                needAuth = { viewModel.needsAuth.collectAsState(true).value },
                onClickAuth = onClickAuth,
                onSubmitAuthToken = viewModel::onSubmitAuthToken,
                navigationCallback = navigationCallback,
                onClickSettings = onClickSettings,
            )
        }

        navGraphBuilder.composable(
            route = AnimeNavDestinations.SEARCH.id
                    + "?title={title}"
                    + "&tagId={tagId}",
            arguments = listOf(
                navArgument("title") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("tagId") {
                    type = NavType.StringType
                    nullable = true
                },
            )
        ) {
            val title = it.arguments?.getString("title")
            SearchScreen(
                title = title,
                tagId = it.arguments?.getString("tagId"),
                upIconOption = UpIconOption.Back(navHostController),
                navigationCallback = navigationCallback,
                scrollStateSaver = ScrollStateSaver(),
            )
        }

        navGraphBuilder.composable(
            route = AnimeNavDestinations.USER_LIST.id +
                    "?userId={userId}" +
                    "&userName={userName}" +
                    "&mediaType={mediaType}",
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("userName") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("mediaType") {
                    type = NavType.StringType
                    nullable = true
                }
            ),
        ) {
            val arguments = it.arguments!!
            val userId = arguments.getString("userId")
            val userName = arguments.getString("userName")
            val mediaType = arguments.getString("mediaType")
                ?.let { MediaType.safeValueOf(it).takeUnless { it == MediaType.UNKNOWN__ } }
                ?: MediaType.ANIME
            UserListScreen(
                userId = userId,
                userName = userName,
                mediaType = mediaType,
                upIconOption = UpIconOption.Back(navHostController),
                navigationCallback = navigationCallback,
                scrollStateSaver = ScrollStateSaver.fromMap(
                    AnimeNavDestinations.USER_LIST.id,
                    ScrollStateSaver.scrollPositions(),
                ),
            )
        }

        navGraphBuilder.composable(
            route = AnimeNavDestinations.MEDIA_DETAILS.id
                    + "?mediaId={mediaId}${MediaHeaderValues.routeSuffix}",
            deepLinks = listOf(
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/anime/{mediaId}" },
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/anime/{mediaId}/.*" },
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/manga/{mediaId}" },
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/manga/{mediaId}/.*" },
            ),
            arguments = listOf(
                navArgument("mediaId") {
                    type = NavType.StringType
                    nullable = false
                }
            ) + MediaHeaderValues.navArguments()
        ) {
            val arguments = it.arguments!!
            val mediaId = arguments.getString("mediaId")!!

            val viewModel = hiltViewModel<AnimeMediaDetailsViewModel>()
                .apply { initialize(mediaId) }
            val headerValues = MediaHeaderValues(arguments) { viewModel.entry?.media }

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                lifecycleOwner.lifecycle.addObserver(viewModel)
                onDispose { lifecycleOwner.lifecycle.removeObserver(viewModel) }
            }

            DisposableEffect(LocalLifecycleOwner.current) {
                onDispose { viewModel.animeSongsCollapseAll() }
            }

            AnimeMediaDetailsScreen(
                viewModel = viewModel,
                headerValues = headerValues,
                entry = { viewModel.entry },
                onGenreLongClick = { /*TODO*/ },
                onCharacterLongClick = { /*TODO*/ },
                onStaffLongClick = { /*TODO*/ },
                onTagLongClick = { /*TODO*/ },
                navigationCallback = navigationCallback,
            )
        }

        navGraphBuilder.composable(
            route = AnimeNavDestinations.CHARACTER_DETAILS.id
                    + "?characterId={characterId}${CharacterHeaderValues.routeSuffix}",
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "${AniListUtils.ANILIST_BASE_URL}/character/{characterId}"
                },
                navDeepLink {
                    uriPattern = "${AniListUtils.ANILIST_BASE_URL}/character/{characterId}/.*"
                },
            ),
            arguments = listOf(
                navArgument("characterId") {
                    type = NavType.StringType
                    nullable = false
                }
            ) + CharacterHeaderValues.navArguments(),
        ) {
            val arguments = it.arguments!!
            val characterId = arguments.getString("characterId")!!

            val viewModel = hiltViewModel<AnimeCharacterDetailsViewModel>()
                .apply { initialize(characterId) }
            val headerValues = CharacterHeaderValues(arguments) { viewModel.entry?.character }

            CharacterDetailsScreen(
                viewModel = viewModel,
                headerValues = headerValues,
                navigationCallback = navigationCallback,
            )
        }

        navGraphBuilder.composable(
            route = AnimeNavDestinations.CHARACTER_MEDIAS.id
                    + "?characterId={characterId}${CharacterHeaderValues.routeSuffix}",
            arguments = listOf(
                navArgument("characterId") {
                    type = NavType.StringType
                    nullable = false
                }
            ) + MediaHeaderValues.navArguments()
        ) {
            val arguments = it.arguments!!
            val characterId = arguments.getString("characterId")!!

            val viewModel = hiltViewModel<CharacterMediasViewModel>()
                .apply { initialize(characterId) }
            val headerValues = CharacterHeaderValues(arguments) { viewModel.entry?.character }

            CharacterMediasScreen(
                viewModel = viewModel,
                headerValues = headerValues,
                navigationCallback = navigationCallback,
            )
        }

        navGraphBuilder.composable(
            route = AnimeNavDestinations.STAFF_DETAILS.id
                    + "?staffId={staffId}${StaffHeaderValues.routeSuffix}",
            deepLinks = listOf(
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/staff/{staffId}" },
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/staff/{staffId}/.*" },
            ),
            arguments = listOf(
                navArgument("staffId") {
                    type = NavType.StringType
                    nullable = false
                }
            ) + StaffHeaderValues.navArguments(),
        ) {
            val arguments = it.arguments!!
            val staffId = arguments.getString("staffId")!!
            val viewModel = hiltViewModel<StaffDetailsViewModel>()
                .apply { initialize(staffId) }

            val headerValues = StaffHeaderValues(arguments) { viewModel.entry?.staff }

            StaffDetailsScreen(
                viewModel = viewModel,
                headerValues = headerValues,
                navigationCallback = navigationCallback,
            )
        }

        navGraphBuilder.composable(
            route = AnimeNavDestinations.STAFF_CHARACTERS.id
                    + "?staffId={staffId}${StaffHeaderValues.routeSuffix}",
            arguments = listOf(
                navArgument("staffId") {
                    type = NavType.StringType
                    nullable = false
                }
            ) + StaffHeaderValues.navArguments()
        ) {
            val arguments = it.arguments!!
            val staffId = arguments.getString("staffId")!!

            val viewModel = hiltViewModel<StaffCharactersViewModel>().apply { initialize(staffId) }
            val headerValues = StaffHeaderValues(arguments) { viewModel.entry?.staff }

            StaffCharactersScreen(
                viewModel = viewModel,
                headerValues = headerValues,
                navigationCallback = navigationCallback,
            )
        }

        navGraphBuilder.composable(
            route = AnimeNavDestinations.USER.id
                    + "?userId={userId}",
            deepLinks = listOf(
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/user/{userId}" },
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/user/{userId}/.*" },
            ),
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    nullable = true
                },
            ),
        ) {
            val userId = it.arguments?.getString("userId")
            UserScreen(
                userId = userId,
                navigationCallback = navigationCallback,
            )
        }

        navGraphBuilder.composable(
            route = AnimeNavDestinations.IGNORED.id
                    + "?mediaType={mediaType}",
            arguments = listOf(
                navArgument("mediaType") {
                    type = NavType.StringType
                    nullable = true
                },
            ),
        ) {
            // TODO: Ignored list not actually split by anime and manga
            val mediaType = it.arguments?.getString("mediaType")
                ?.let { MediaType.safeValueOf(it).takeUnless { it == MediaType.UNKNOWN__ } }
                ?: MediaType.ANIME
            val viewModel = hiltViewModel<AnimeMediaIgnoreViewModel>()
                .apply { initialize(mediaType) }
            AnimeIgnoreScreen(
                onClickBack = { navHostController.popBackStack() },
                titleRes = if (mediaType == MediaType.ANIME) {
                    R.string.anime_media_ignore_title_anime
                } else {
                    R.string.anime_media_ignore_title_manga
                },
                viewModel = viewModel,
                navigationCallback = navigationCallback,
            )
        }

        navGraphBuilder.composable(route = AnimeNavDestinations.AIRING_SCHEDULE.id) {
            AiringScheduleScreen(
                onClickBack = { navHostController.popBackStack() },
                navigationCallback = navigationCallback,
            )
        }

        navGraphBuilder.composable(
            route = "${AnimeNavDestinations.SEASONAL.id}?type={type}",
            arguments = listOf(
                navArgument("type") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) {
            val type = it.arguments?.getString("type")?.let {
                try {
                    SeasonalViewModel.Type.valueOf(it)
                } catch (ignored: Throwable) {
                    null
                }
            } ?: SeasonalViewModel.Type.THIS
            val viewModel = hiltViewModel<SeasonalViewModel>()
                .apply { initialize(type) }
            SeasonalScreen(
                viewModel = viewModel,
                upIconOption = UpIconOption.Back(navHostController),
                navigationCallback = navigationCallback,
            )
        }

        navGraphBuilder.composable(AnimeNavDestinations.NEWS.id) {
            AnimeNewsScreen(navigationCallback = navigationCallback)
        }

        navGraphBuilder.composable(AnimeNavDestinations.ACTIVITY.id) {
            AnimeActivityScreen(navigationCallback = navigationCallback)
        }

        navGraphBuilder.composable(
            route = AnimeNavDestinations.MEDIA_CHARACTERS.id
                    + "?mediaId={mediaId}${MediaHeaderValues.routeSuffix}",
            arguments = listOf(
                navArgument("mediaId") {
                    type = NavType.StringType
                    nullable = false
                }
            ) + MediaHeaderValues.navArguments()
        ) {
            val arguments = it.arguments!!
            val mediaId = arguments.getString("mediaId")!!

            val viewModel = hiltViewModel<CharactersViewModel>().apply { initialize(mediaId) }
            val headerValues = MediaHeaderValues(arguments) { viewModel.entry?.media }

            CharactersScreen(
                viewModel = viewModel,
                headerValues = headerValues,
                navigationCallback = navigationCallback,
            )
        }

        navGraphBuilder.composable(
            route = AnimeNavDestinations.MEDIA_REVIEWS.id
                    + "?mediaId={mediaId}${MediaHeaderValues.routeSuffix}",
            arguments = listOf(
                navArgument("mediaId") {
                    type = NavType.StringType
                    nullable = false
                }
            ) + MediaHeaderValues.navArguments()
        ) {
            val arguments = it.arguments!!
            val mediaId = arguments.getString("mediaId")!!

            val viewModel = hiltViewModel<ReviewsViewModel>().apply { initialize(mediaId) }
            val headerValues = MediaHeaderValues(arguments) { viewModel.entry?.media }

            ReviewsScreen(
                viewModel = viewModel,
                headerValues = headerValues,
                navigationCallback = navigationCallback,
            )
        }

        navGraphBuilder.composable(
            route = AnimeNavDestinations.MEDIA_RECOMMENDATIONS.id
                    + "?mediaId={mediaId}${MediaHeaderValues.routeSuffix}",
            arguments = listOf(
                navArgument("mediaId") {
                    type = NavType.StringType
                    nullable = false
                }
            ) + MediaHeaderValues.navArguments()
        ) {
            val arguments = it.arguments!!
            val mediaId = arguments.getString("mediaId")!!

            val viewModel = hiltViewModel<RecommendationsViewModel>().apply { initialize(mediaId) }
            val headerValues = MediaHeaderValues(arguments) { viewModel.entry?.media }

            RecommendationsScreen(
                viewModel = viewModel,
                headerValues = headerValues,
                navigationCallback = navigationCallback,
            )
        }

        navGraphBuilder.composable(
            route = AnimeNavDestinations.REVIEW_DETAILS.id + "?reviewId={reviewId}",
            deepLinks = listOf(
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/review/{reviewId}" },
                navDeepLink {
                    uriPattern = "${AniListUtils.ANILIST_BASE_URL}/review/{reviewId}/.*"
                },
            ),
            arguments = listOf(
                navArgument("reviewId") {
                    type = NavType.StringType
                    nullable = false
                }
            ) + MediaHeaderValues.navArguments()
        ) {
            val arguments = it.arguments!!
            val reviewId = arguments.getString("reviewId")!!

            val viewModel = hiltViewModel<ReviewDetailsViewModel>().apply { initialize(reviewId) }
            val headerValues = MediaHeaderValues(arguments) { viewModel.entry?.review?.media }

            ReviewDetailsScreen(
                viewModel = viewModel,
                headerValues = headerValues,
                navigationCallback = navigationCallback,
            )
        }
    }

    fun onTagClick(navHostController: NavHostController, tagId: String, tagName: String) {
        navHostController.navigate(
            AnimeNavDestinations.SEARCH.id +
                    "?title=$tagName&tagId=$tagId"
        )
    }

    fun onMediaClick(
        navHostController: NavHostController,
        entry: AnimeMediaListRow.Entry<*>,
        imageWidthToHeightRatio: Float,
    ) = navHostController.navigate(
        AnimeNavDestinations.MEDIA_DETAILS.id +
                "?mediaId=${entry.media.id}" +
                MediaHeaderValues.routeSuffix(entry.media, imageWidthToHeightRatio)
    )

    fun onMediaCharactersClick(
        navHostController: NavHostController,
        entry: AnimeMediaDetailsScreen.Entry,
        imageWidthToHeightRatio: Float,
    ) = navHostController.navigate(
        AnimeNavDestinations.MEDIA_CHARACTERS.id +
                "?mediaId=${entry.mediaId}" +
                MediaHeaderValues.routeSuffix(entry.media, imageWidthToHeightRatio)
    )

    fun onMediaReviewsClick(
        navHostController: NavHostController,
        entry: AnimeMediaDetailsScreen.Entry,
        imageWidthToHeightRatio: Float,
    ) = navHostController.navigate(
        AnimeNavDestinations.MEDIA_REVIEWS.id +
                "?mediaId=${entry.mediaId}" +
                MediaHeaderValues.routeSuffix(entry.media, imageWidthToHeightRatio)
    )

    fun onMediaRecommendationsClick(
        navHostController: NavHostController,
        entry: AnimeMediaDetailsScreen.Entry,
        imageWidthToHeightRatio: Float,
    ) = navHostController.navigate(
        AnimeNavDestinations.MEDIA_RECOMMENDATIONS.id +
                "?mediaId=${entry.mediaId}" +
                MediaHeaderValues.routeSuffix(entry.media, imageWidthToHeightRatio)
    )

    fun onMediaClick(
        navHostController: NavHostController,
        media: UserFavoriteMediaNode,
        imageWidthToHeightRatio: Float,
    ) = navHostController.navigate(
        AnimeNavDestinations.MEDIA_DETAILS.id +
                "?mediaId=${media.id}" +
                "&title=${media.title?.userPreferred}" +
                "&coverImage=${media.coverImage?.extraLarge}" +
                "&coverImageWidthToHeightRatio=$imageWidthToHeightRatio" +
                "&color=${media.coverImage?.color?.let(ComposeColorUtils::hexToColor)?.toArgb()}"
    )

    fun onMediaClick(
        navHostController: NavHostController,
        media: MediaNavigationData,
        imageWidthToHeightRatio: Float?,
    ) = navHostController.navigate(
        AnimeNavDestinations.MEDIA_DETAILS.id +
                "?mediaId=${media.id}" +
                "&title=${media.title?.userPreferred}" +
                "&coverImage=${media.coverImage?.extraLarge}" +
                imageWidthToHeightRatio?.let { "&coverImageWidthToHeightRatio=$it" }.orEmpty()
    )

    fun onMediaClick(
        navHostController: NavHostController,
        mediaId: String,
        title: String?,
        image: String?,
        imageWidthToHeightRatio: Float,
    ) = navHostController.navigate(
        AnimeNavDestinations.MEDIA_DETAILS.id +
                "?mediaId=$mediaId" +
                "&title=$title" +
                "&coverImage=$image" +
                "&coverImageWidthToHeightRatio=$imageWidthToHeightRatio"
    )

    fun onCharacterClick(
        navHostController: NavHostController,
        character: CharacterNavigationData,
        imageWidthToHeightRatio: Float,
        color: Color?,
    ) = navHostController.navigate(
        AnimeNavDestinations.CHARACTER_DETAILS.id +
                "?characterId=${character.id}" +
                CharacterHeaderValues.routeSuffix(character, imageWidthToHeightRatio, color)
    )

    fun onCharacterMediasClick(
        navHostController: NavHostController,
        character: CharacterHeaderData,
        imageWidthToHeightRatio: Float,
        color: Color?,
    ) = navHostController.navigate(
        AnimeNavDestinations.CHARACTER_MEDIAS.id +
                "?characterId=${character.id}" +
                CharacterHeaderValues.routeSuffix(character, imageWidthToHeightRatio, color)
    )

    fun onStaffClick(
        navHostController: NavHostController,
        staff: StaffNavigationData,
        imageWidthToHeightRatio: Float,
        color: Color?,
    ) = navHostController.navigate(
        AnimeNavDestinations.STAFF_DETAILS.id +
                "?staffId=${staff.id}" +
                StaffHeaderValues.routeSuffix(staff, imageWidthToHeightRatio, color)
    )

    fun onStaffClick(
        navHostController: NavHostController,
        staff: StaffHeaderData,
        imageWidthToHeightRatio: Float,
        color: Color?,
    ) = navHostController.navigate(
        AnimeNavDestinations.STAFF_DETAILS.id +
                "?staffId=${staff.id}" +
                StaffHeaderValues.routeSuffix(staff, imageWidthToHeightRatio, color)
    )

    fun onStaffCharactersClick(
        navHostController: NavHostController,
        staff: StaffHeaderData,
        imageWidthToHeightRatio: Float,
        color: Color?,
    ) = navHostController.navigate(
        AnimeNavDestinations.STAFF_CHARACTERS.id +
                "?staffId=${staff.id}" +
                StaffHeaderValues.routeSuffix(staff, imageWidthToHeightRatio, color)
    )

    fun onUserClick(
        navHostController: NavHostController,
        user: UserNavigationData,
        imageWidthToHeightRatio: Float,
    ) {
        // TODO: Pass name and image
        navHostController.navigate("${AnimeNavDestinations.USER.id}?userId=${user.id}")
    }

    fun onUserListClick(
        navHostController: NavHostController,
        userId: String,
        userName: String?,
        mediaType: MediaType?
    ) {
        navHostController.navigate(
            AnimeNavDestinations.USER_LIST.id +
                    "?userId=$userId" +
                    "&userName=$userName" +
                    "&mediaType=${mediaType?.rawValue}"
        )
    }

    fun onReviewClick(
        navHostController: NavHostController,
        reviewId: String,
        media: MediaHeaderData?,
        imageWidthToHeightRatio: Float,
    ) = navHostController.navigate(
        AnimeNavDestinations.REVIEW_DETAILS.id +
                "?reviewId=$reviewId" +
                MediaHeaderValues.routeSuffix(media, imageWidthToHeightRatio)
    )

    @Composable
    fun SearchScreen(
        title: String?,
        tagId: String?,
        upIconOption: UpIconOption?,
        navigationCallback: NavigationCallback,
        scrollStateSaver: ScrollStateSaver,
        bottomNavigationState: BottomNavigationState? = null,
    ) {
        val viewModel = hiltViewModel<AnimeSearchViewModel>().apply {
            initialize(
                AnimeMediaFilterController.InitialParams(
                    isAnime = true,
                    tagId = tagId,
                    showListStatusExcludes = true,
                )
            )
        }
        AnimeSearchScreen(
            upIconOption = upIconOption,
            isRoot = title == null,
            title = title?.let { Either.Right(it) },
            viewModel = viewModel,
            navigationCallback = navigationCallback,
            scrollStateSaver = scrollStateSaver,
            bottomNavigationState = bottomNavigationState,
        )
    }

    @Composable
    fun UserScreen(
        userId: String?,
        navigationCallback: NavigationCallback,
        bottomNavigationState: BottomNavigationState? = null,
    ) {
        val viewModel = hiltViewModel<AniListUserViewModel>()
            .apply { initialize(userId) }
        AniListUserScreen(
            viewModel = viewModel,
            navigationCallback = navigationCallback,
            bottomNavigationState = bottomNavigationState,
        )
    }

    @Composable
    fun UserListScreen(
        userId: String?,
        userName: String?,
        mediaType: MediaType,
        upIconOption: UpIconOption?,
        navigationCallback: NavigationCallback,
        scrollStateSaver: ScrollStateSaver,
        bottomNavigationState: BottomNavigationState? = null,
    ) {
        val viewModel = hiltViewModel<AnimeUserListViewModel>(key = mediaType.rawValue)
            .apply { initialize(userId, userName, mediaType) }
        AnimeUserListScreen(
            upIconOption = upIconOption,
            mediaType = mediaType,
            viewModel = viewModel,
            navigationCallback = navigationCallback,
            scrollStateSaver = scrollStateSaver,
            bottomNavigationState = bottomNavigationState,
        )
    }

    class NavigationCallback(
        // Null to make previews easier
        private val navHostController: NavHostController? = null,
        private val cdEntryNavigator: CdEntryNavigator? = null,
        private val onOpenUri: (String) -> Unit = {},
    ) {
        fun onMediaClick(media: UserFavoriteMediaNode, imageWidthToHeightRatio: Float) {
            navHostController?.let { onMediaClick(it, media, imageWidthToHeightRatio) }
        }

        fun onMediaClick(media: AnimeMediaListRow.Entry<*>, imageWidthToHeightRatio: Float) {
            navHostController?.let { onMediaClick(it, media, imageWidthToHeightRatio) }
        }

        fun onMediaClick(
            media: MediaPreviewWithDescription,
            imageWidthToHeightRatio: Float? = null,
        ) {
            navHostController?.let { onMediaClick(it, media, imageWidthToHeightRatio) }
        }

        fun onMediaClick(media: MediaNavigationData, imageWidthToHeightRatio: Float) {
            navHostController?.let { onMediaClick(it, media, imageWidthToHeightRatio) }
        }

        fun onMediaClick(
            id: String,
            title: String?,
            image: String?,
            imageWidthToHeightRatio: Float,
        ) {
            navHostController?.let { onMediaClick(it, id, title, image, imageWidthToHeightRatio) }
        }

        fun onMediaCharactersClick(
            media: AnimeMediaDetailsScreen.Entry,
            imageWidthToHeightRatio: Float
        ) {
            navHostController?.let { onMediaCharactersClick(it, media, imageWidthToHeightRatio) }
        }

        fun onMediaReviewsClick(
            media: AnimeMediaDetailsScreen.Entry,
            imageWidthToHeightRatio: Float
        ) {
            navHostController?.let { onMediaReviewsClick(it, media, imageWidthToHeightRatio) }
        }

        fun onMediaRecommendationsClick(
            media: AnimeMediaDetailsScreen.Entry,
            imageWidthToHeightRatio: Float
        ) {
            navHostController?.let {
                onMediaRecommendationsClick(it, media, imageWidthToHeightRatio)
            }
        }

        fun onTagClick(id: String, name: String) {
            navHostController?.let { onTagClick(it, id, name) }
        }

        fun onUserListClick(userId: String, userName: String?, mediaType: MediaType?) {
            navHostController?.let { onUserListClick(it, userId, userName, mediaType) }
        }

        fun onUserClick(userNavigationData: UserNavigationData, imageWidthToHeightRatio: Float) {
            navHostController?.let { onUserClick(it, userNavigationData, imageWidthToHeightRatio) }
        }

        fun onCharacterClick(
            character: CharacterNavigationData,
            imageWidthToHeightRatio: Float,
            color: Color?,
        ) {
            navHostController?.let {
                onCharacterClick(it, character, imageWidthToHeightRatio, color)
            }
        }

        fun onCharacterMediasClick(
            character: CharacterHeaderData,
            imageWidthToHeightRatio: Float,
            color: Color?,
        ) {
            navHostController?.let {
                onCharacterMediasClick(it, character, imageWidthToHeightRatio, color)
            }
        }

        fun onCharacterLongClick(id: String) {
            // TODO
        }

        fun onStaffClick(
            staff: StaffNavigationData,
            imageWidthToHeightRatio: Float,
            color: Color?,
        ) {
            navHostController?.let { onStaffClick(it, staff, imageWidthToHeightRatio, color) }
        }

        fun onStaffCharactersClick(
            staff: StaffHeaderData,
            imageWidthToHeightRatio: Float,
            color: Color?,
        ) {
            navHostController?.let {
                onStaffCharactersClick(it, staff, imageWidthToHeightRatio, color)
            }
        }

        fun onStaffLongClick(id: String) {
            // TODO
        }

        fun onStudioClick(id: String) = onOpenUri(AniListUtils.studioUrl(id))

        fun onGenreClick(genre: String) {
            // TODO
        }

        fun onIgnoreListOpen(mediaType: MediaType?) {
            navHostController?.navigate(AnimeNavDestinations.IGNORED.id + "?mediaType=${mediaType?.rawValue}")
        }

        fun onCdEntryClick(model: CdEntryGridModel, imageCornerDp: Dp?) {
            navHostController?.let {
                cdEntryNavigator?.onCdEntryClick(it, listOf(model.id.valueId), imageCornerDp)
            }
        }

        fun onAiringScheduleClick() {
            navHostController?.navigate(AnimeNavDestinations.AIRING_SCHEDULE.id)
        }

        fun onReviewClick(
            reviewId: String,
            media: MediaHeaderData?,
            imageWidthToHeightRatio: Float
        ) {
            navHostController?.let { onReviewClick(it, reviewId, media, imageWidthToHeightRatio) }
        }

        fun navigate(route: String) = navHostController?.navigate(route)

        fun popUp() = navHostController?.popBackStack()
    }
}
