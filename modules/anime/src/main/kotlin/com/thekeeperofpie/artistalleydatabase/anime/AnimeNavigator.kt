package com.thekeeperofpie.artistalleydatabase.anime

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.anilist.fragment.UserFavoriteMediaNode
import com.anilist.type.MediaSeason
import com.anilist.type.MediaType
import com.google.accompanist.navigation.animation.composable
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.list.AnimeUserListScreen
import com.thekeeperofpie.artistalleydatabase.anime.list.AnimeUserListViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeMediaFilterController
import com.thekeeperofpie.artistalleydatabase.anime.search.AnimeSearchScreen
import com.thekeeperofpie.artistalleydatabase.anime.search.AnimeSearchViewModel
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.AniListUserViewModel
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.ScrollStateSaver

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
object AnimeNavigator {

    fun initialize(
        navHostController: NavHostController,
        navGraphBuilder: NavGraphBuilder,
        onClickNav: () -> Unit,
        onOpenUri: (String) -> Unit,
    ) {
        val navigationCallback = NavigationCallback(navHostController, onOpenUri)
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
                onClickNav = {
                    if (title == null) {
                        onClickNav()
                    } else {
                        navHostController.popBackStack()
                    }
                },
                navigationCallback = navigationCallback,
                scrollStateSaver = ScrollStateSaver(),
            )
        }

        navGraphBuilder.composable(
            route = "userList?userId={userId}" +
                    "&mediaType={mediaType}",
            arguments = listOf(
                navArgument("userId") {
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
            val mediaType = arguments.getString("mediaType")
                ?.let { MediaType.safeValueOf(it).takeUnless { it == MediaType.UNKNOWN__ } }
                ?: MediaType.ANIME
            UserListScreen(
                userId = userId,
                mediaType = mediaType,
                onClickNav = { navHostController.popBackStack() },
                showDrawerHandle = false,
                navigationCallback = navigationCallback,
                scrollStateSaver = ScrollStateSaver.fromMap(
                    "userList",
                    ScrollStateSaver.scrollPositions(),
                ),
            )
        }

        navGraphBuilder.composable(
            route = "animeDetails"
                    + "?mediaId={mediaId}"
                    + "&title={title}"
                    + "&subtitleFormatRes={subtitleFormatRes}"
                    + "&subtitleStatusRes={subtitleStatusRes}"
                    + "&subtitleSeason={subtitleSeason}"
                    + "&subtitleSeasonYear={subtitleSeasonYear}"
                    + "&nextEpisode={nextEpisode}"
                    + "&nextEpisodeAiringAt={nextEpisodeAiringAt}"
                    + "&coverImage={coverImage}"
                    + "&color={color}"
                    + "&bannerImage={bannerImage}",
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
                "bannerImage",
                "color",
            ).map {
                navArgument(it) {
                    type = NavType.StringType
                    nullable = true
                }
            },
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up)
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down
                )
            },
        ) {
            val arguments = it.arguments!!
            val title = arguments.getString("title")
            val mediaId = arguments.getString("mediaId")!!
            val coverImage = arguments.getString("coverImage")
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

            val mediaAsState = viewModel.media.collectAsState()
            AnimeMediaDetailsScreen(
                loading = { viewModel.loading.collectAsState().value },
                color = {
                    mediaAsState.value?.coverImage?.color
                        ?.let(ComposeColorUtils::hexToColor)
                        ?: color
                },
                coverImage = {
                    mediaAsState.value?.coverImage?.extraLarge ?: coverImage
                },
                bannerImage = {
                    mediaAsState.value?.bannerImage ?: bannerImage
                },
                title = {
                    mediaAsState.value?.title?.userPreferred ?: title ?: ""
                },
                subtitle = {
                    mediaAsState.value?.let {
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
                    mediaAsState.value?.nextAiringEpisode?.episode
                        ?: nextEpisode
                },
                nextEpisodeAiringAt = {
                    mediaAsState.value?.nextAiringEpisode?.airingAt
                        ?: nextEpisodeAiringAt
                },
                entry = {
                    mediaAsState.value?.let {
                        AnimeMediaDetailsScreen.Entry(mediaId, it)
                    }
                },
                mediaPlayer = { viewModel.mediaPlayer },
                animeSongs = { viewModel.animeSongs.collectAsState().value },
                animeSongState = viewModel::getAnimeSongState,
                onAnimeSongPlayClick = viewModel::onAnimeSongPlayAudioClick,
                onAnimeSongProgressUpdate = viewModel::onAnimeSongProgressUpdate,
                onAnimeSongExpandedToggle = viewModel::onAnimeSongExpandedToggle,
                cdEntries = { viewModel.cdEntries.collectAsState().value },
                onGenreLongClicked = { /*TODO*/ },
                onCharacterLongClicked = { /*TODO*/ },
                onStaffLongClicked = { /*TODO*/ },
                onTagLongClicked = { /*TODO*/ },
                navigationCallback = navigationCallback,
                trailerPlaybackPosition = { viewModel.trailerPlaybackPosition },
                onTrailerPlaybackPositionUpdate = {
                    viewModel.trailerPlaybackPosition = it
                },
                listEntry = { viewModel.listEntry.collectAsState().value },
                editData = viewModel.editData,
                scoreFormat = { viewModel.scoreFormat.collectAsState().value },
                onDateChange = viewModel::onDateChange,
                onStatusChange = viewModel::onStatusChange,
                onClickDelete = viewModel::onClickDelete,
                onClickSave = viewModel::onClickSave,
                onEditSheetValueChange = viewModel::onEditSheetValueChange,
                errorRes = { viewModel.errorResource.collectAsState().value },
            )
        }

        navGraphBuilder.composable(
            route = AnimeNavDestinations.PROFILE.id
                    + "?userId={userId}",
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    nullable = true
                },
            )
        ) {
            val userId = it.arguments?.getString("userId")
            UserScreen(
                userId = userId,
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

    fun onMediaClick(navHostController: NavHostController, entry: AnimeMediaListRow.Entry) =
        navHostController.navigate(
            "animeDetails?mediaId=${entry.id!!.valueId}" +
                    "&title=${entry.title}" +
                    "&subtitleFormatRes=${entry.subtitleFormatRes}" +
                    "&subtitleStatusRes=${entry.subtitleStatusRes}" +
                    "&subtitleSeason=${entry.subtitleSeason}" +
                    "&subtitleSeasonYear=${entry.subtitleSeasonYear}" +
                    "&nextEpisode=${entry.nextAiringEpisode?.episode}" +
                    "&nextEpisodeAiringAt=${entry.nextAiringEpisode?.airingAt}" +
                    "&bannerImage=${entry.imageBanner}" +
                    "&coverImage=${entry.imageExtraLarge}" +
                    "&color=${entry.color?.toArgb()}"
        )

    fun onMediaClick(
        navHostController: NavHostController,
        media: UserFavoriteMediaNode,
    ) = navHostController.navigate(
        "animeDetails?mediaId=${media.id}" +
                "&title=${media.title?.userPreferred}" +
                "&coverImage=${media.coverImage?.large}" +
                "&color=${media.coverImage?.color?.let(ComposeColorUtils::hexToColor)?.toArgb()}"
    )

    fun onMediaClick(
        navHostController: NavHostController,
        mediaId: String,
        title: String?,
        image: String?,
    ) = navHostController.navigate(
        "animeDetails" +
                "?mediaId=$mediaId" +
                "&title=$title" +
                "&coverImage=$image"
    )

    fun onUserClick(navHostController: NavHostController, userId: String) {
        // TODO: Pass name and image
        navHostController.navigate("${AnimeNavDestinations.PROFILE.id}?userId=$userId")
    }

    fun onUserListClick(
        navHostController: NavHostController,
        userId: String,
        mediaType: MediaType?
    ) {
        navHostController.navigate(
            "userList" +
                    "?userId=$userId" +
                    "&mediaType=${mediaType?.rawValue}"
        )
    }

    @Composable
    fun SearchScreen(
        title: String?,
        tagId: String?,
        onClickNav: () -> Unit,
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
            onClickNav = onClickNav,
            isRoot = title == null,
            title = title,
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
        mediaType: MediaType,
        onClickNav: () -> Unit,
        showDrawerHandle: Boolean,
        navigationCallback: NavigationCallback,
        scrollStateSaver: ScrollStateSaver,
        bottomNavigationState: BottomNavigationState? = null,
    ) {
        val viewModel = hiltViewModel<AnimeUserListViewModel>(key = mediaType.rawValue)
            .apply { initialize(userId, mediaType) }
        AnimeUserListScreen(
            onClickNav = onClickNav,
            showDrawerHandle = showDrawerHandle,
            viewModel = viewModel,
            navigationCallback = navigationCallback,
            scrollStateSaver = scrollStateSaver,
            bottomNavigationState = bottomNavigationState,
        )
    }

    class NavigationCallback(
        // Null to make previews easier
        private val navHostController: NavHostController? = null,
        private val onOpenUri: (String) -> Unit = {},
    ) {
        fun onMediaClick(media: UserFavoriteMediaNode) {
            navHostController?.let { onMediaClick(it, media) }
        }

        fun onMediaClick(media: AnimeMediaListRow.Entry) {
            navHostController?.let { onMediaClick(it, media) }
        }

        fun onMediaClick(id: String, title: String?, image: String?) {
            navHostController?.let { onMediaClick(it, id, title, image) }
        }

        fun onTagClick(id: String, name: String) {
            navHostController?.let { onTagClick(it, id, name) }
        }

        fun onUserListClick(userId: String, mediaType: MediaType?) {
            navHostController?.let { onUserListClick(it, userId, mediaType) }
        }

        fun onUserClick(userId: String) {
            navHostController?.let { onUserClick(it, userId) }
        }

        fun onCharacterClick(id: String) = onOpenUri(AniListUtils.characterUrl(id))

        fun onCharacterLongClick(id: String) {
            // TODO
        }

        fun onStaffClick(id: String) = onOpenUri(AniListUtils.staffUrl(id))

        fun onStaffLongClick(id: String) {
            // TODO
        }

        fun onStudioClick(id: String) = onOpenUri(AniListUtils.studioUrl(id))

        fun onGenreClick(genre: String) {
            // TODO
        }
    }
}
