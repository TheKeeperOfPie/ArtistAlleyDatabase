package com.thekeeperofpie.artistalleydatabase.anime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
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
import com.anilist.fragment.MediaPreview
import com.anilist.fragment.MediaPreviewWithDescription
import com.anilist.fragment.StaffHeaderData
import com.anilist.fragment.StaffNavigationData
import com.anilist.fragment.UserNavigationData
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.Either
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.activity.AnimeActivityScreen
import com.thekeeperofpie.artistalleydatabase.anime.activity.details.ActivityDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.activity.details.ActivityDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.character.CharactersScreen
import com.thekeeperofpie.artistalleydatabase.anime.character.CharactersViewModel
import com.thekeeperofpie.artistalleydatabase.anime.character.details.AnimeCharacterDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.character.details.CharacterDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.character.media.CharacterMediasScreen
import com.thekeeperofpie.artistalleydatabase.anime.character.media.CharacterMediasViewModel
import com.thekeeperofpie.artistalleydatabase.anime.forum.ForumRootScreen
import com.thekeeperofpie.artistalleydatabase.anime.forum.ForumSearchScreen
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadScreen
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.comment.ForumThreadCommentTreeScreen
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeIgnoreScreen
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreViewModel
import com.thekeeperofpie.artistalleydatabase.anime.list.AnimeUserListScreen
import com.thekeeperofpie.artistalleydatabase.anime.list.AnimeUserListViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.media.activity.MediaActivitiesScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.activity.MediaActivitiesViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsScreen
import com.thekeeperofpie.artistalleydatabase.anime.notifications.NotificationsScreen
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationsScreen
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.review.ReviewsScreen
import com.thekeeperofpie.artistalleydatabase.anime.review.ReviewsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.review.details.ReviewDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.review.details.ReviewDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.schedule.AiringScheduleScreen
import com.thekeeperofpie.artistalleydatabase.anime.search.AnimeSearchViewModel
import com.thekeeperofpie.artistalleydatabase.anime.search.MediaSearchScreen
import com.thekeeperofpie.artistalleydatabase.anime.seasonal.SeasonalScreen
import com.thekeeperofpie.artistalleydatabase.anime.seasonal.SeasonalViewModel
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.staff.character.StaffCharactersScreen
import com.thekeeperofpie.artistalleydatabase.anime.staff.character.StaffCharactersViewModel
import com.thekeeperofpie.artistalleydatabase.anime.studio.StudioMediasScreen
import com.thekeeperofpie.artistalleydatabase.anime.studio.StudioMediasViewModel
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserViewModel
import com.thekeeperofpie.artistalleydatabase.anime.user.UserHeaderValues
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryNavigator
import com.thekeeperofpie.artistalleydatabase.cds.grid.CdEntryGridModel
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.ScrollStateSaver
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.monetization.UnlockScreen

object AnimeNavigator {

    fun initialize(
        navHostController: NavHostController,
        navGraphBuilder: NavGraphBuilder,
        upIconOption: UpIconOption?,
        onClickAuth: () -> Unit,
        onClickSettings: () -> Unit,
    ) {
        navGraphBuilder.composable(
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
            )
        }

        navGraphBuilder.composable(
            route = AnimeNavDestinations.SEARCH_MEDIA.id
                    + "?title={title}"
                    + "&titleRes={titleRes}"
                    + "&tagId={tagId}"
                    + "&genre={genre}"
                    + "&mediaType={mediaType}"
                    + "&sort={sort}",
            arguments = listOf(
                "title",
                "titleRes",
                "tagId",
                "genre",
                "mediaType",
                "sort",
            ).map {
                navArgument(it) {
                    type = NavType.StringType
                    nullable = true
                }
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
            val viewModel = hiltViewModel<AnimeSearchViewModel>().apply {
                initialize(
                    defaultMediaSort = sort,
                    tagId = tagId,
                    genre = genre,
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
            val headerValues = MediaHeaderValues(
                arguments = arguments,
                media = { viewModel.entry.result?.media },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

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
                upIconOption = UpIconOption.Back(navHostController),
                headerValues = headerValues,
                onCharacterLongClick = { /*TODO*/ },
                onStaffLongClick = { /*TODO*/ },
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
            val headerValues = CharacterHeaderValues(
                arguments,
                character = { viewModel.entry.result?.character },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            CharacterDetailsScreen(
                viewModel = viewModel,
                upIconOption = UpIconOption.Back(navHostController),
                headerValues = headerValues,
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
            val headerValues = CharacterHeaderValues(
                arguments,
                character = { viewModel.entry?.character },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            CharacterMediasScreen(
                viewModel = viewModel,
                upIconOption = UpIconOption.Back(navHostController),
                headerValues = headerValues,
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

            val headerValues = StaffHeaderValues(
                arguments,
                staff = { viewModel.entry?.staff },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            StaffDetailsScreen(
                viewModel = viewModel,
                upIconOption = UpIconOption.Back(navHostController),
                headerValues = headerValues,
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
            val headerValues = StaffHeaderValues(
                arguments,
                staff = { viewModel.entry?.staff },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            StaffCharactersScreen(
                viewModel = viewModel,
                upIconOption = UpIconOption.Back(navHostController),
                headerValues = headerValues,
            )
        }

        navGraphBuilder.composable(
            route = AnimeNavDestinations.USER.id
                    + "?userId={userId}${UserHeaderValues.routeSuffix}",
            deepLinks = listOf(
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/user/{userId}" },
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/user/{userId}/.*" },
            ),
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    nullable = true
                },
            ) + UserHeaderValues.navArguments(),
        ) {
            val userId = it.arguments?.getString("userId")
            val viewModel = hiltViewModel<AniListUserViewModel>()
                .apply { initialize(userId) }
            val headerValues = UserHeaderValues(it.arguments) { viewModel.entry?.user }
            AniListUserScreen(
                viewModel = viewModel,
                upIconOption = upIconOption,
                headerValues = headerValues,
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
                onClickBack = { navHostController.navigateUp() },
                titleRes = if (mediaType == MediaType.ANIME) {
                    R.string.anime_media_ignore_title_anime
                } else {
                    R.string.anime_media_ignore_title_manga
                },
                viewModel = viewModel,
            )
        }

        navGraphBuilder.composable(route = AnimeNavDestinations.AIRING_SCHEDULE.id) {
            AiringScheduleScreen(
                onClickBack = { navHostController.navigateUp() },
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
            )
        }

        navGraphBuilder.composable(AnimeNavDestinations.NEWS.id) {
            AnimeNewsScreen()
        }

        navGraphBuilder.composable(AnimeNavDestinations.ACTIVITY.id) {
            AnimeActivityScreen()
        }

        navGraphBuilder.composable(AnimeNavDestinations.NOTIFICATIONS.id) {
            NotificationsScreen(upIconOption = UpIconOption.Back(navHostController))
        }

        navGraphBuilder.composable(
            route = AnimeNavDestinations.MEDIA_CHARACTERS.id
                    + "?mediaId={mediaId}&mediaType={mediaType}${MediaHeaderValues.routeSuffix}",
            arguments = listOf(
                navArgument("mediaId") {
                    type = NavType.StringType
                    nullable = false
                },
                navArgument("mediaType") {
                    type = NavType.StringType
                    nullable = true
                },
            ) + MediaHeaderValues.navArguments()
        ) {
            val arguments = it.arguments!!
            val mediaId = arguments.getString("mediaId")!!

            val viewModel = hiltViewModel<CharactersViewModel>()
                .apply { initialize(mediaId) }
            val headerValues = MediaHeaderValues(
                arguments = arguments,
                media = { viewModel.entry?.media },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            CharactersScreen(
                viewModel = viewModel,
                upIconOption = UpIconOption.Back(navHostController),
                headerValues = headerValues,
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

            val viewModel = hiltViewModel<ReviewsViewModel>()
                .apply { initialize(mediaId) }
            val headerValues = MediaHeaderValues(
                arguments = arguments,
                media = { viewModel.entry?.media },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            ReviewsScreen(
                viewModel = viewModel,
                upIconOption = UpIconOption.Back(navHostController),
                headerValues = headerValues,
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

            val viewModel = hiltViewModel<RecommendationsViewModel>()
                .apply { initialize(mediaId) }
            val headerValues = MediaHeaderValues(
                arguments = arguments,
                media = { viewModel.entry?.media },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            RecommendationsScreen(
                viewModel = viewModel,
                upIconOption = UpIconOption.Back(navHostController),
                headerValues = headerValues,
            )
        }

        navGraphBuilder.composable(
            route = AnimeNavDestinations.MEDIA_ACTIVITIES.id
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

            val viewModel = hiltViewModel<MediaActivitiesViewModel>().apply { initialize(mediaId) }
            val headerValues = MediaHeaderValues(
                arguments = arguments,
                media = { viewModel.entry?.data?.media },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            MediaActivitiesScreen(
                viewModel = viewModel,
                upIconOption = UpIconOption.Back(navHostController),
                headerValues = headerValues,
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

            val viewModel = hiltViewModel<ReviewDetailsViewModel>()
                .apply { initialize(reviewId) }
            val headerValues = MediaHeaderValues(
                arguments = arguments,
                media = { viewModel.entry?.review?.media },
                favoriteUpdate = { viewModel.favoritesToggleHelper.favorite },
            )

            ReviewDetailsScreen(
                viewModel = viewModel,
                upIconOption = UpIconOption.Back(navHostController),
                headerValues = headerValues,
            )
        }

        navGraphBuilder.composable(
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
                navArgument("name") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("favorite") {
                    type = NavType.StringType
                    nullable = true
                },
            ),
        ) {
            val arguments = it.arguments!!
            val studioId = arguments.getString("studioId")!!
            val name = arguments.getString("name")
            val favorite = arguments.getString("favorite")?.toBooleanStrictOrNull()

            val viewModel = hiltViewModel<StudioMediasViewModel>().apply { initialize(studioId) }

            StudioMediasScreen(
                upIconOption = UpIconOption.Back(navHostController),
                viewModel = viewModel,
                name = { viewModel.entry?.studio?.name ?: name ?: "" },
                favorite = {
                    viewModel.favoritesToggleHelper.favorite
                        ?: viewModel.entry?.studio?.isFavourite
                        ?: favorite
                },
            )
        }

        navGraphBuilder.composable(
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
            val arguments = it.arguments!!
            val activityId = arguments.getString("activityId")!!

            val viewModel =
                hiltViewModel<ActivityDetailsViewModel>().apply { initialize(activityId) }

            ActivityDetailsScreen(
                upIconOption = UpIconOption.Back(navHostController),
                viewModel = viewModel,
            )
        }

        navGraphBuilder.composable(route = AnimeNavDestinations.FEATURE_TIERS.id) {
            UnlockScreen(
                upIconOption = UpIconOption.Back(navHostController),
                onClickSettings = null,
            )
        }

        navGraphBuilder.composable(route = AnimeNavDestinations.FORUM.id) {
            ForumRootScreen(
                upIconOption = UpIconOption.Back(navHostController),
            )
        }

        navGraphBuilder.composable(
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
        navGraphBuilder.composable(
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

        navGraphBuilder.composable(
            route = AnimeNavDestinations.FORUM_THREAD_COMMENT.id
                    + "?threadId={threadId}"
                    + "&commentId={commentId}"
                    + "&title={title}",
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "${AniListUtils.ANILIST_BASE_URL}/forum/thread/{threadId}/comment/{commentId}"
                },
                navDeepLink {
                    uriPattern = "${AniListUtils.ANILIST_BASE_URL}/forum/thread/{threadId}/comment/{commentId}/.*"
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

    fun onMediaClick(
        navHostController: NavHostController,
        media: MediaPreview,
        languageOption: AniListLanguageOption,
        favorite: Boolean?,
        imageWidthToHeightRatio: Float,
    ) = navHostController.navigate(
        AnimeNavDestinations.MEDIA_DETAILS.id +
                "?mediaId=${media.id}" +
                MediaHeaderValues.routeSuffix(
                    media = media,
                    languageOption = languageOption,
                    favorite = favorite,
                    imageWidthToHeightRatio = imageWidthToHeightRatio,
                )
    )

    fun onMediaCharactersClick(
        navHostController: NavHostController,
        entry: AnimeMediaDetailsScreen.Entry,
        languageOption: AniListLanguageOption,
        favorite: Boolean?,
        imageWidthToHeightRatio: Float,
    ) = navHostController.navigate(
        AnimeNavDestinations.MEDIA_CHARACTERS.id +
                "?mediaId=${entry.mediaId}" +
                MediaHeaderValues.routeSuffix(
                    media = entry.media,
                    languageOption = languageOption,
                    favorite = favorite,
                    imageWidthToHeightRatio = imageWidthToHeightRatio,
                )
    )

    fun onMediaReviewsClick(
        navHostController: NavHostController,
        entry: AnimeMediaDetailsScreen.Entry,
        languageOption: AniListLanguageOption,
        favorite: Boolean?,
        imageWidthToHeightRatio: Float,
    ) = navHostController.navigate(
        AnimeNavDestinations.MEDIA_REVIEWS.id +
                "?mediaId=${entry.mediaId}" +
                MediaHeaderValues.routeSuffix(
                    media = entry.media,
                    languageOption = languageOption,
                    favorite = favorite,
                    imageWidthToHeightRatio = imageWidthToHeightRatio,
                )
    )

    fun onMediaRecommendationsClick(
        navHostController: NavHostController,
        entry: AnimeMediaDetailsScreen.Entry,
        languageOption: AniListLanguageOption,
        favorite: Boolean?,
        imageWidthToHeightRatio: Float,
    ) = navHostController.navigate(
        AnimeNavDestinations.MEDIA_RECOMMENDATIONS.id +
                "?mediaId=${entry.mediaId}" +
                MediaHeaderValues.routeSuffix(
                    media = entry.media,
                    languageOption = languageOption,
                    favorite = favorite,
                    imageWidthToHeightRatio = imageWidthToHeightRatio,
                )
    )

    fun onMediaActivitiesClick(
        navHostController: NavHostController,
        entry: AnimeMediaDetailsScreen.Entry,
        languageOption: AniListLanguageOption,
        favorite: Boolean?,
        imageWidthToHeightRatio: Float,
    ) = navHostController.navigate(
        AnimeNavDestinations.MEDIA_ACTIVITIES.id +
                "?mediaId=${entry.mediaId}" +
                MediaHeaderValues.routeSuffix(
                    media = entry.media,
                    languageOption = languageOption,
                    favorite = favorite,
                    imageWidthToHeightRatio = imageWidthToHeightRatio,
                )
    )

    fun onMediaClick(
        navHostController: NavHostController,
        media: MediaNavigationData,
        languageOption: AniListLanguageOption,
        imageWidthToHeightRatio: Float?,
    ) = navHostController.navigate(
        AnimeNavDestinations.MEDIA_DETAILS.id +
                "?mediaId=${media.id}" +
                "&title=${media.title?.primaryTitle(languageOption)}" +
                "&coverImage=${media.coverImage?.extraLarge}" +
                imageWidthToHeightRatio?.let { "&coverImageWidthToHeightRatio=$it" }.orEmpty()
    )

    fun onCharacterClick(
        navHostController: NavHostController,
        character: CharacterNavigationData,
        languageOption: AniListLanguageOption,
        favorite: Boolean?,
        imageWidthToHeightRatio: Float,
        color: Color?,
    ) = navHostController.navigate(
        AnimeNavDestinations.CHARACTER_DETAILS.id +
                "?characterId=${character.id}" +
                CharacterHeaderValues.routeSuffix(
                    character,
                    languageOption,
                    favorite,
                    imageWidthToHeightRatio,
                    color
                )
    )

    fun onCharacterMediasClick(
        navHostController: NavHostController,
        character: CharacterHeaderData,
        languageOption: AniListLanguageOption,
        favorite: Boolean?,
        imageWidthToHeightRatio: Float,
        color: Color?,
    ) = navHostController.navigate(
        AnimeNavDestinations.CHARACTER_MEDIAS.id +
                "?characterId=${character.id}" +
                CharacterHeaderValues.routeSuffix(
                    character,
                    languageOption,
                    favorite ?: character.isFavourite,
                    imageWidthToHeightRatio,
                    color,
                )
    )

    fun onStaffClick(
        navHostController: NavHostController,
        staff: StaffNavigationData,
        languageOption: AniListLanguageOption,
        favorite: Boolean?,
        imageWidthToHeightRatio: Float,
        color: Color?,
    ) = navHostController.navigate(
        AnimeNavDestinations.STAFF_DETAILS.id +
                "?staffId=${staff.id}" +
                StaffHeaderValues.routeSuffix(
                    staff = staff,
                    languageOption = languageOption,
                    favorite = favorite,
                    imageWidthToHeightRatio = imageWidthToHeightRatio,
                    color = color,
                )
    )

    fun onStaffClick(
        navHostController: NavHostController,
        staff: StaffHeaderData,
        languageOption: AniListLanguageOption,
        favorite: Boolean?,
        imageWidthToHeightRatio: Float,
        color: Color?,
    ) = navHostController.navigate(
        AnimeNavDestinations.STAFF_DETAILS.id +
                "?staffId=${staff.id}" +
                StaffHeaderValues.routeSuffix(
                    staff = staff,
                    languageOption = languageOption,
                    favorite = favorite ?: staff.isFavourite,
                    imageWidthToHeightRatio = imageWidthToHeightRatio,
                    color = color,
                )
    )

    fun onStaffCharactersClick(
        navHostController: NavHostController,
        staff: StaffHeaderData,
        languageOption: AniListLanguageOption,
        favorite: Boolean?,
        imageWidthToHeightRatio: Float,
        color: Color?,
    ) = navHostController.navigate(
        AnimeNavDestinations.STAFF_CHARACTERS.id +
                "?staffId=${staff.id}" +
                StaffHeaderValues.routeSuffix(
                    staff = staff,
                    languageOption = languageOption,
                    favorite = favorite ?: staff.isFavourite,
                    imageWidthToHeightRatio = imageWidthToHeightRatio,
                    color = color,
                )
    )

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
        languageOption: AniListLanguageOption,
        favorite: Boolean?,
        imageWidthToHeightRatio: Float,
    ) = navHostController.navigate(
        AnimeNavDestinations.REVIEW_DETAILS.id +
                "?reviewId=$reviewId" +
                MediaHeaderValues.routeSuffix(
                    media,
                    languageOption,
                    favorite ?: media?.isFavourite,
                    imageWidthToHeightRatio,
                )
    )

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
    fun UserListScreen(
        userId: String?,
        userName: String?,
        mediaType: MediaType,
        upIconOption: UpIconOption?,
        scrollStateSaver: ScrollStateSaver,
        bottomNavigationState: BottomNavigationState? = null,
    ) {
        val viewModel = hiltViewModel<AnimeUserListViewModel>(key = mediaType.rawValue)
            .apply { initialize(userId, userName, mediaType) }
        AnimeUserListScreen(
            upIconOption = upIconOption,
            mediaType = mediaType,
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
        fun onMediaClick(
            media: AnimeMediaListRow.Entry,
            favorite: Boolean?,
            imageWidthToHeightRatio: Float,
        ) {
            navHostController?.let {
                onMediaClick(
                    navHostController = it,
                    media = media.media,
                    languageOption = languageOptionMedia,
                    favorite = favorite,
                    imageWidthToHeightRatio = imageWidthToHeightRatio
                )
            }
        }

        fun onMediaClick(
            media: MediaPreviewWithDescription,
            imageWidthToHeightRatio: Float? = null,
        ) {
            navHostController?.let {
                onMediaClick(
                    navHostController = it,
                    media = media,
                    languageOption = languageOptionMedia,
                    imageWidthToHeightRatio = imageWidthToHeightRatio
                )
            }
        }

        fun onMediaClick(media: MediaNavigationData, imageWidthToHeightRatio: Float) {
            navHostController?.let {
                onMediaClick(
                    navHostController = it,
                    media = media,
                    languageOption = languageOptionMedia,
                    imageWidthToHeightRatio = imageWidthToHeightRatio
                )
            }
        }

        fun onMediaCharactersClick(
            media: AnimeMediaDetailsScreen.Entry,
            favorite: Boolean?,
            imageWidthToHeightRatio: Float,
        ) {
            navHostController?.let {
                onMediaCharactersClick(
                    navHostController = it,
                    entry = media,
                    languageOption = languageOptionMedia,
                    favorite = favorite,
                    imageWidthToHeightRatio = imageWidthToHeightRatio
                )
            }
        }

        fun onMediaReviewsClick(
            media: AnimeMediaDetailsScreen.Entry,
            favorite: Boolean?,
            imageWidthToHeightRatio: Float,
        ) {
            navHostController?.let {
                onMediaReviewsClick(
                    navHostController = it,
                    entry = media,
                    languageOption = languageOptionMedia,
                    favorite = favorite,
                    imageWidthToHeightRatio = imageWidthToHeightRatio
                )
            }
        }

        fun onMediaRecommendationsClick(
            media: AnimeMediaDetailsScreen.Entry,
            favorite: Boolean?,
            imageWidthToHeightRatio: Float,
        ) {
            navHostController?.let {
                onMediaRecommendationsClick(
                    navHostController = it,
                    entry = media,
                    languageOption = languageOptionMedia,
                    favorite = favorite,
                    imageWidthToHeightRatio = imageWidthToHeightRatio
                )
            }
        }

        fun onMediaActivitiesClick(
            media: AnimeMediaDetailsScreen.Entry,
            favorite: Boolean?,
            imageWidthToHeightRatio: Float,
        ) {
            navHostController?.let {
                onMediaActivitiesClick(
                    navHostController = it,
                    entry = media,
                    languageOption = languageOptionMedia,
                    favorite = favorite,
                    imageWidthToHeightRatio = imageWidthToHeightRatio
                )
            }
        }

        fun onTagClick(mediaType: MediaType, id: String, name: String) {
            navHostController?.let { onTagClick(it, mediaType, id, name) }
        }

        fun onUserListClick(userId: String, userName: String?, mediaType: MediaType?) {
            navHostController?.let { onUserListClick(it, userId, userName, mediaType) }
        }

        fun onUserClick(userNavigationData: UserNavigationData, imageWidthToHeightRatio: Float) {
            navHostController?.let { onUserClick(it, userNavigationData, imageWidthToHeightRatio) }
        }

        fun onCharacterClick(
            character: CharacterNavigationData,
            favorite: Boolean?,
            imageWidthToHeightRatio: Float,
            color: Color?,
        ) {
            navHostController?.let {
                onCharacterClick(
                    navHostController = it,
                    character = character,
                    languageOption = languageOptionCharacters,
                    favorite = favorite,
                    imageWidthToHeightRatio = imageWidthToHeightRatio,
                    color = color
                )
            }
        }

        fun onCharacterMediasClick(
            character: CharacterHeaderData,
            favorite: Boolean?,
            imageWidthToHeightRatio: Float,
            color: Color?,
        ) {
            navHostController?.let {
                onCharacterMediasClick(
                    navHostController = it,
                    character = character,
                    languageOption = languageOptionCharacters,
                    favorite = favorite,
                    imageWidthToHeightRatio = imageWidthToHeightRatio,
                    color = color
                )
            }
        }

        fun onCharacterLongClick(id: String) {
            // TODO
        }

        fun onStaffClick(
            staff: StaffNavigationData,
            favorite: Boolean?,
            imageWidthToHeightRatio: Float,
            color: Color?,
        ) {
            navHostController?.let {
                onStaffClick(
                    navHostController = it,
                    staff = staff,
                    languageOption = languageOptionStaff,
                    favorite = favorite,
                    imageWidthToHeightRatio = imageWidthToHeightRatio,
                    color = color,
                )
            }
        }

        fun onStaffCharactersClick(
            staff: StaffHeaderData,
            favorite: Boolean?,
            imageWidthToHeightRatio: Float,
            color: Color?,
        ) {
            navHostController?.let {
                onStaffCharactersClick(
                    navHostController = it,
                    staff = staff,
                    languageOption = languageOptionStaff,
                    favorite = favorite,
                    imageWidthToHeightRatio = imageWidthToHeightRatio,
                    color = color,
                )
            }
        }

        fun onStaffLongClick(id: String) {
            // TODO
        }

        fun onStudioClick(id: String, name: String) =
            navHostController?.let { onStudioClick(it, id, name) }

        fun onGenreClick(mediaType: MediaType, genre: String) {
            navHostController?.let { onGenreClick(it, mediaType, genre) }
        }

        fun onIgnoreListOpen(mediaType: MediaType?) {
            navHostController?.navigate(AnimeNavDestinations.IGNORED.id + "?mediaType=${mediaType?.rawValue}")
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

        fun onReviewClick(
            reviewId: String,
            media: MediaHeaderData?,
            favorite: Boolean?,
            imageWidthToHeightRatio: Float,
        ) {
            navHostController?.let {
                onReviewClick(
                    navHostController = it,
                    reviewId = reviewId,
                    media = media,
                    languageOption = languageOptionMedia,
                    favorite = favorite,
                    imageWidthToHeightRatio = imageWidthToHeightRatio,
                )
            }
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

        fun navigate(route: String) = navHostController?.navigate(route)

        fun navigateUp() = navHostController?.navigateUp()
    }
}

val LocalNavigationCallback = staticCompositionLocalOf { AnimeNavigator.NavigationCallback(null) }
