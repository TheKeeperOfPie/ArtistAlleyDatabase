package com.thekeeperofpie.artistalleydatabase.anime

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.paging.compose.collectAsLazyPagingItems
import com.anilist.fragment.UserFavoriteMediaNode
import com.anilist.type.MediaSeason
import com.google.accompanist.navigation.animation.composable
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
import com.thekeeperofpie.artistalleydatabase.compose.ColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.ScrollStateSaver

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
object AnimeNavigator {

    fun initialize(
        navHostController: NavHostController,
        navGraphBuilder: NavGraphBuilder,
        onClickNav: () -> Unit,
    ) {
        val userCallback = object : AniListUserScreen.Callback {
            override fun onMediaClick(media: UserFavoriteMediaNode) {
                onMediaClick(navHostController, media)
            }
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
                onClickNav = {
                    if (title == null) {
                        onClickNav()
                    } else {
                        navHostController.popBackStack()
                    }
                },
                onTagClick = { tagId, tagName -> onTagClick(navHostController, tagId, tagName) },
                onMediaClick = { onMediaClick(navHostController, it) },
                scrollStateSaver = ScrollStateSaver(),
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
                onClickBack = navHostController::popBackStack,
                loading = { viewModel.loading.collectAsState().value },
                color = {
                    mediaAsState.value?.coverImage?.color
                        ?.let(ColorUtils::hexToColor)
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
                onGenreClicked = { /*TODO*/ },
                onGenreLongClicked = { /*TODO*/ },
                onCharacterClicked = { /*TODO*/ },
                onCharacterLongClicked = { /*TODO*/ },
                onStaffClicked = { /*TODO*/ },
                onStaffLongClicked = { /*TODO*/ },
                onTagClicked = { tagId, tagName -> onTagClick(navHostController, tagId, tagName) },
                onTagLongClicked = { /*TODO*/ },
                onUserClick = { onUserClick(navHostController, it) },
                trailerPlaybackPosition = { viewModel.trailerPlaybackPosition },
                onTrailerPlaybackPositionUpdate = {
                    viewModel.trailerPlaybackPosition = it
                },
                onMediaClicked = { onMediaClick(navHostController, it) },
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
                callback = userCallback,
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
                "&color=${media.coverImage?.color?.let(ColorUtils::hexToColor)?.toArgb()}"
    )

    fun onUserClick(navHostController: NavHostController, userId: String) {
        // TODO: Pass name and image
        navHostController.navigate("${AnimeNavDestinations.PROFILE.id}?userId=$userId")
    }

    @Composable
    fun SearchScreen(
        title: String?,
        tagId: String?,
        onClickNav: () -> Unit,
        onTagClick: (tagId: String, tagName: String) -> Unit,
        onMediaClick: (AnimeMediaListRow.Entry) -> Unit,
        scrollStateSaver: ScrollStateSaver,
        nestedScrollConnection: NestedScrollConnection? = null,
        bottomNavBarPadding: @Composable () -> Dp = { 0.dp },
        bottomOffset: @Composable () -> Dp = { 0.dp },
    ) {
        val viewModel = hiltViewModel<AnimeSearchViewModel>().apply {
            initialize(AnimeMediaFilterController.InitialParams(tagId = tagId))
        }
        AnimeSearchScreen(
            nestedScrollConnection = nestedScrollConnection,
            onClickNav = onClickNav,
            isRoot = { title == null },
            title = { title },
            query = { viewModel.query.collectAsState().value },
            onQueryChange = viewModel::onQuery,
            filterData = { viewModel.filterData() },
            onRefresh = viewModel::onRefresh,
            content = { viewModel.content.collectAsLazyPagingItems() },
            tagShown = { viewModel.tagShown },
            onTagDismiss = viewModel::onTagDismiss,
            onTagClick = onTagClick,
            onTagLongClick = viewModel::onTagLongClick,
            onMediaClick = onMediaClick,
            onMediaLongClick = viewModel::onMediaLongClick,
            scrollStateSaver = scrollStateSaver,
            bottomNavBarPadding = bottomNavBarPadding,
            bottomOffset = bottomOffset,
        )
    }

    @Composable
    fun UserScreen(
        userId: String?,
        callback: AniListUserScreen.Callback,
        nestedScrollConnection: NestedScrollConnection? = null,
        bottomNavBarPadding: @Composable () -> Dp = { 0.dp },
    ) {
        val viewModel = hiltViewModel<AniListUserViewModel>()
            .apply { initialize(userId) }
        AniListUserScreen(
            nestedScrollConnection = nestedScrollConnection,
            entry = { viewModel.entry.collectAsState().value },
            viewer = { viewModel.viewer.collectAsState().value },
            callback = callback,
            bottomNavBarPadding = bottomNavBarPadding,
        )
    }
}
