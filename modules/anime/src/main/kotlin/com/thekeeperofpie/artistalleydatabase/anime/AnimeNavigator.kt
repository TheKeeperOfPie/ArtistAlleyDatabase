package com.thekeeperofpie.artistalleydatabase.anime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
import com.anilist.fragment.CharacterNavigationData
import com.anilist.fragment.MediaNavigationData
import com.anilist.fragment.MediaPreviewWithDescription
import com.anilist.fragment.StaffNavigationData
import com.anilist.fragment.UserFavoriteMediaNode
import com.anilist.fragment.UserNavigationData
import com.anilist.type.MediaSeason
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.Either
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.activity.AnimeActivityScreen
import com.thekeeperofpie.artistalleydatabase.anime.character.AnimeCharacterDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeIgnoreScreen
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreViewModel
import com.thekeeperofpie.artistalleydatabase.anime.list.AnimeUserListScreen
import com.thekeeperofpie.artistalleydatabase.anime.list.AnimeUserListViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeMediaFilterController
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsScreen
import com.thekeeperofpie.artistalleydatabase.anime.schedule.AiringScheduleScreen
import com.thekeeperofpie.artistalleydatabase.anime.search.AnimeSearchScreen
import com.thekeeperofpie.artistalleydatabase.anime.search.AnimeSearchViewModel
import com.thekeeperofpie.artistalleydatabase.anime.seasonal.SeasonalScreen
import com.thekeeperofpie.artistalleydatabase.anime.seasonal.SeasonalViewModel
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffDetailsViewModel
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
                    + "?mediaId={mediaId}"
                    + "&title={title}"
                    + "&subtitleFormatRes={subtitleFormatRes}"
                    + "&subtitleStatusRes={subtitleStatusRes}"
                    + "&subtitleSeason={subtitleSeason}"
                    + "&subtitleSeasonYear={subtitleSeasonYear}"
                    + "&nextEpisode={nextEpisode}"
                    + "&nextEpisodeAiringAt={nextEpisodeAiringAt}"
                    + "&coverImage={coverImage}"
                    + "&coverImageWidthToHeightRatio={coverImageWidthToHeightRatio}"
                    + "&color={color}"
                    + "&bannerImage={bannerImage}",
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
            ) + listOf(
                "title",
                "subtitleFormatRes",
                "subtitleStatusRes",
                "subtitleSeason",
                "subtitleSeasonYear",
                "nextEpisode",
                "nextEpisodeAiringAt",
                "coverImage",
                "coverImageWidthToHeightRatio",
                "bannerImage",
                "color",
            ).map {
                navArgument(it) {
                    type = NavType.StringType
                    nullable = true
                }
            },
        ) {
            val arguments = it.arguments!!
            val title = arguments.getString("title")
            val mediaId = arguments.getString("mediaId")!!
            val coverImage = arguments.getString("coverImage")
            val coverImageWidthToHeightRatio = arguments.getString("coverImageWidthToHeightRatio")
                ?.toFloatOrNull() ?: 1f
            val bannerImage = arguments.getString("bannerImage")
            val subtitleFormatRes =
                arguments.getString("subtitleFormatRes")?.toIntOrNull()
            val subtitleStatusRes =
                arguments.getString("subtitleStatusRes")?.toIntOrNull()
            val subtitleSeason = arguments.getString("subtitleSeason")?.let { season ->
                MediaSeason.values().find { it.rawValue == season }
            }
            val subtitleSeasonYear =
                arguments.getString("subtitleSeasonYear")?.toIntOrNull()
            val nextEpisode = arguments.getString("nextEpisode")?.toIntOrNull()
            val nextEpisodeAiringAt =
                arguments.getString("nextEpisodeAiringAt")?.toIntOrNull()
            val color = arguments.getString("color")
                ?.toIntOrNull()
                ?.let(::Color)

            val viewModel = hiltViewModel<AnimeMediaDetailsViewModel>().apply {
                initialize(mediaId)
            }

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
                color = {
                    viewModel.entry?.media?.coverImage?.color
                        ?.let(ComposeColorUtils::hexToColor)
                        ?: color
                },
                coverImage = {
                    viewModel.entry?.media?.coverImage?.extraLarge ?: coverImage
                },
                coverImageWidthToHeightRatio = coverImageWidthToHeightRatio,
                bannerImage = {
                    viewModel.entry?.media?.bannerImage ?: bannerImage
                },
                title = {
                    viewModel.entry?.media?.title?.userPreferred ?: title ?: ""
                },
                subtitle = {
                    viewModel.entry?.media?.let {
                        listOfNotNull(
                            stringResource(it.format.toTextRes()),
                            stringResource(it.status.toTextRes()),
                            MediaUtils.formatSeasonYear(
                                it.season,
                                it.seasonYear
                            ),
                        ).joinToString(separator = " - ")
                    } ?: listOfNotNull(
                        subtitleFormatRes?.let { stringResource(it) },
                        subtitleStatusRes?.let { stringResource(it) },
                        MediaUtils.formatSeasonYear(
                            subtitleSeason,
                            subtitleSeasonYear
                        ),
                    )
                        .joinToString(separator = " - ")
                        .ifEmpty { null }
                },
                nextEpisode = {
                    viewModel.entry?.media?.nextAiringEpisode?.episode
                        ?: nextEpisode
                },
                nextEpisodeAiringAt = {
                    viewModel.entry?.media?.nextAiringEpisode?.airingAt
                        ?: nextEpisodeAiringAt
                },
                entry = {
                    viewModel.entry?.media?.let {
                        AnimeMediaDetailsScreen.Entry(mediaId, it)
                    }
                },
                onGenreLongClick = { /*TODO*/ },
                onCharacterLongClick = { /*TODO*/ },
                onStaffLongClick = { /*TODO*/ },
                onTagLongClick = { /*TODO*/ },
                navigationCallback = navigationCallback,
            )
        }

        navGraphBuilder.composable(
            route = AnimeNavDestinations.CHARACTER_DETAILS.id
                    + "?characterId={characterId}"
                    + "&name={name}"
                    + "&coverImage={coverImage}"
                    + "&coverImageWidthToHeightRatio={coverImageWidthToHeightRatio}",
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
            ) + listOf(
                "name",
                "coverImage",
                "coverImageWidthToHeightRatio",
            ).map {
                navArgument(it) {
                    type = NavType.StringType
                    nullable = true
                }
            },
        ) {
            val arguments = it.arguments!!
            val characterId = arguments.getString("characterId")!!
            val name = arguments.getString("name")
            val coverImage = arguments.getString("coverImage")
            val coverImageWidthToHeightRatio = arguments.getString("coverImageWidthToHeightRatio")
                ?.toFloatOrNull() ?: 1f

            val viewModel = hiltViewModel<AnimeCharacterDetailsViewModel>().apply {
                initialize(characterId)
            }

            CharacterDetailsScreen(
                viewModel = viewModel,
                coverImage = { viewModel.entry?.character?.image?.large ?: coverImage },
                coverImageWidthToHeightRatio = coverImageWidthToHeightRatio,
                title = { viewModel.entry?.character?.name?.userPreferred ?: name ?: "" },
                navigationCallback = navigationCallback,
            )
        }

        navGraphBuilder.composable(
            route = AnimeNavDestinations.STAFF_DETAILS.id
                    + "?staffId={staffId}"
                    + "&name={name}"
                    + "&coverImage={coverImage}"
                    + "&coverImageWidthToHeightRatio={coverImageWidthToHeightRatio}",
            deepLinks = listOf(
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/staff/{staffId}" },
                navDeepLink { uriPattern = "${AniListUtils.ANILIST_BASE_URL}/staff/{staffId}/.*" },
            ),
            arguments = listOf(
                navArgument("staffId") {
                    type = NavType.StringType
                    nullable = false
                }
            ) + listOf(
                "name",
                "coverImage",
                "coverImageWidthToHeightRatio",
            ).map {
                navArgument(it) {
                    type = NavType.StringType
                    nullable = true
                }
            },
        ) {
            val arguments = it.arguments!!
            val staffId = arguments.getString("staffId")!!
            val name = arguments.getString("name")
            val coverImage = arguments.getString("coverImage")
            val coverImageWidthToHeightRatio = arguments.getString("coverImageWidthToHeightRatio")
                ?.toFloatOrNull() ?: 1f

            val viewModel = hiltViewModel<StaffDetailsViewModel>().apply {
                initialize(staffId)
            }

            StaffDetailsScreen(
                viewModel = viewModel,
                coverImage = { viewModel.entry?.staff?.image?.large ?: coverImage },
                coverImageWidthToHeightRatio = coverImageWidthToHeightRatio,
                title = { viewModel.entry?.staff?.name?.userPreferred ?: name ?: "" },
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
    }

    fun onTagClick(navHostController: NavHostController, tagId: String, tagName: String) {
        navHostController.navigate(
            AnimeNavDestinations.SEARCH.id +
                    "?title=$tagName&tagId=$tagId"
        )
    }

    fun onMediaClick(
        navHostController: NavHostController,
        entry: AnimeMediaListRow.Entry,
        imageWidthToHeightRatio: Float,
    ) = navHostController.navigate(
        AnimeNavDestinations.MEDIA_DETAILS.id +
                "?mediaId=${entry.id!!.valueId}" +
                "&title=${entry.title}" +
                "&subtitleFormatRes=${entry.subtitleFormatRes}" +
                "&subtitleStatusRes=${entry.subtitleStatusRes}" +
                "&subtitleSeason=${entry.subtitleSeason}" +
                "&subtitleSeasonYear=${entry.subtitleSeasonYear}" +
                "&nextEpisode=${entry.nextAiringEpisode?.episode}" +
                "&nextEpisodeAiringAt=${entry.nextAiringEpisode?.airingAt}" +
                "&bannerImage=${entry.imageBanner}" +
                "&coverImage=${entry.imageExtraLarge}" +
                "&coverImageWidthToHeightRatio=$imageWidthToHeightRatio" +
                "&color=${entry.color?.toArgb()}"
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
    ) = navHostController.navigate(
        AnimeNavDestinations.CHARACTER_DETAILS.id +
                "?characterId=${character.id}" +
                "&name=${character.name?.userPreferred}" +
                "&coverImage=${character.image?.large}" +
                "&coverImageWidthToHeightRatio=$imageWidthToHeightRatio"
    )

    fun onStaffClick(
        navHostController: NavHostController,
        character: StaffNavigationData,
        imageWidthToHeightRatio: Float,
    ) = navHostController.navigate(
        AnimeNavDestinations.STAFF_DETAILS.id +
                "?staffId=${character.id}" +
                "&name=${character.name?.userPreferred}" +
                "&coverImage=${character.image?.large}" +
                "&coverImageWidthToHeightRatio=$imageWidthToHeightRatio"
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

        fun onMediaClick(media: AnimeMediaListRow.Entry, imageWidthToHeightRatio: Float) {
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

        fun onTagClick(id: String, name: String) {
            navHostController?.let { onTagClick(it, id, name) }
        }

        fun onUserListClick(userId: String, userName: String?, mediaType: MediaType?) {
            navHostController?.let { onUserListClick(it, userId, userName, mediaType) }
        }

        fun onUserClick(userNavigationData: UserNavigationData, imageWidthToHeightRatio: Float) {
            navHostController?.let { onUserClick(it, userNavigationData, imageWidthToHeightRatio) }
        }

        fun onCharacterClick(character: CharacterNavigationData, imageWidthToHeightRatio: Float) {
            navHostController?.let { onCharacterClick(it, character, imageWidthToHeightRatio) }
        }

        fun onCharacterLongClick(id: String) {
            // TODO
        }

        fun onStaffClick(staff: StaffNavigationData, imageWidthToHeightRatio: Float) {
            navHostController?.let { onStaffClick(it, staff, imageWidthToHeightRatio) }
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

        fun navigate(route: String) = navHostController?.navigate(route)

        fun popUp() = navHostController?.popBackStack()
    }
}
