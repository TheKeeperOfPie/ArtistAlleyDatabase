package com.thekeeperofpie.artistalleydatabase.anime

import android.util.Pair
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.paging.compose.collectAsLazyPagingItems
import com.anilist.type.MediaSeason
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
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
import com.thekeeperofpie.artistalleydatabase.compose.ColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText

@OptIn(ExperimentalAnimationApi::class)
object AnimeHomeScreen {

    @Composable
    operator fun invoke(
        onClickNav: () -> Unit,
        needAuth: () -> Boolean,
        onClickAuth: () -> Unit,
        onSubmitAuthToken: (String) -> Unit,
        selectedSubIndex: () -> Int = { 0 },
        errorRes: () -> Pair<Int, Exception?>? = { null },
        onErrorDismiss: () -> Unit = { },
    ) {
        Scaffold(
            snackbarHost = {
                SnackbarErrorText(
                    errorRes()?.first,
                    errorRes()?.second,
                    onErrorDismiss = onErrorDismiss
                )
            },
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                if (needAuth()) {
                    AuthPrompt(onClickAuth = onClickAuth, onSubmitAuthToken = onSubmitAuthToken)
                } else {
                    val navController = rememberAnimatedNavController()
                    fun onTagClick(tagId: String, tagName: String) {
                        navController.navigate(
                            AnimeNavDestinations.SEARCH.id +
                                    "?title=$tagName&tagId=$tagId"
                        )
                    }

                    fun onMediaClick(entry: AnimeMediaListRow.Entry) {
                        navController.navigate(
                            "animeDetails?title=${entry.title}" +
                                    "&subtitleFormatRes=${entry.subtitleFormatRes}" +
                                    "&subtitleStatusRes=${entry.subtitleStatusRes}" +
                                    "&subtitleSeason=${entry.subtitleSeason}" +
                                    "&subtitleSeasonYear=${entry.subtitleSeasonYear}" +
                                    "&nextEpisode=${entry.nextAiringEpisode?.episode}" +
                                    "&nextEpisodeAiringAt=${entry.nextAiringEpisode?.airingAt}" +
                                    "&mediaId=${entry.id!!.valueId}" +
                                    "&bannerImage=${entry.imageBanner}" +
                                    "&coverImage=${entry.imageExtraLarge}" +
                                    "&color=${entry.color?.toArgb()}"
                        )
                    }

                    AnimatedNavHost(
                        navController = navController,
                        startDestination = AnimeNavDestinations.values()[selectedSubIndex()].id
                    ) {
                        composable(AnimeNavDestinations.LIST.id) {
                            val viewModel = hiltViewModel<AnimeUserListViewModel>()
                                .apply { initialize() }
                            AnimeUserListScreen(
                                onClickNav = onClickNav,
                                query = { viewModel.query.collectAsState().value },
                                onQueryChange = viewModel::onQuery,
                                filterData = { viewModel.filterData() },
                                onRefresh = viewModel::onRefresh,
                                content = { viewModel.content },
                                tagShown = { viewModel.tagShown },
                                onTagDismiss = viewModel::onTagDismiss,
                                onTagClick = ::onTagClick,
                                onTagLongClick = viewModel::onTagLongClick,
                                onMediaClick = ::onMediaClick,
                            )
                        }
                        composable(
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
                            val tagId = it.arguments?.getString("tagId")
                            val viewModel = hiltViewModel<AnimeSearchViewModel>().apply {
                                initialize(AnimeMediaFilterController.InitialParams(tagId = tagId))
                            }
                            AnimeSearchScreen(
                                onClickNav = {
                                    if (title == null) {
                                        onClickNav()
                                    } else {
                                        navController.popBackStack()
                                    }
                                },
                                isRoot = { title == null },
                                title = { title },
                                query = { viewModel.query.collectAsState().value },
                                onQueryChange = viewModel::onQuery,
                                filterData = { viewModel.filterData() },
                                onRefresh = viewModel::onRefresh,
                                content = { viewModel.content.collectAsLazyPagingItems() },
                                tagShown = { viewModel.tagShown },
                                onTagDismiss = viewModel::onTagDismiss,
                                onTagClick = ::onTagClick,
                                onTagLongClick = viewModel::onTagLongClick,
                                onMediaClick = ::onMediaClick,
                            )
                        }
                        composable(
                            route = "animeDetails"
                                    + "?title={title}"
                                    + "&subtitleFormatRes={subtitleFormatRes}"
                                    + "&subtitleStatusRes={subtitleStatusRes}"
                                    + "&subtitleSeason={subtitleSeason}"
                                    + "&subtitleSeasonYear={subtitleSeasonYear}"
                                    + "&nextEpisode={nextEpisode}"
                                    + "&nextEpisodeAiringAt={nextEpisodeAiringAt}"
                                    + "&mediaId={mediaId}"
                                    + "&coverImage={coverImage}"
                                    + "&color={color}"
                                    + "&bannerImage={bannerImage}",
                            arguments = listOf(
                                navArgument("title") {
                                    type = NavType.StringType
                                    nullable = true
                                },
                                navArgument("subtitleFormatRes") {
                                    type = NavType.StringType
                                    nullable = true
                                },
                                navArgument("subtitleStatusRes") {
                                    type = NavType.StringType
                                    nullable = true
                                },
                                navArgument("subtitleSeason") {
                                    type = NavType.StringType
                                    nullable = true
                                },
                                navArgument("subtitleSeasonYear") {
                                    type = NavType.StringType
                                    nullable = true
                                },
                                navArgument("nextEpisode") {
                                    type = NavType.StringType
                                    nullable = true
                                },
                                navArgument("nextEpisodeAiringAt") {
                                    type = NavType.StringType
                                    nullable = true
                                },
                                navArgument("mediaId") {
                                    type = NavType.StringType
                                    nullable = false
                                },
                                navArgument("coverImage") {
                                    type = NavType.StringType
                                    nullable = true
                                },
                                navArgument("bannerImage") {
                                    type = NavType.StringType
                                    nullable = true
                                },
                                navArgument("color") {
                                    type = NavType.StringType
                                    nullable = true
                                },
                            ),
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
                            val subtitleSeason =
                                MediaSeason.safeValueOf(arguments.getString("subtitleSeason") ?: "")
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
                                onClickBack = navController::popBackStack,
                                loading = { viewModel.loading.collectAsState().value },
                                color = {
                                    mediaAsState.value?.coverImage?.color
                                        ?.let(ColorUtils::hexToColor)
                                        ?: color
                                },
                                coverImage = {
                                    mediaAsState.value?.coverImage?.extraLarge ?: coverImage
                                },
                                bannerImage = { mediaAsState.value?.bannerImage ?: bannerImage },
                                title = { mediaAsState.value?.title?.userPreferred ?: title ?: "" },
                                subtitle = {
                                    mediaAsState.value?.let {
                                        listOfNotNull(
                                            stringResource(it.format.toTextRes()),
                                            stringResource(it.status.toTextRes()),
                                            MediaUtils.formatSeasonYear(it.season, it.seasonYear),
                                        ).joinToString(separator = " - ")
                                    } ?: listOfNotNull(
                                        subtitleFormatRes?.let { stringResource(it) },
                                        subtitleStatusRes?.let { stringResource(it) },
                                        MediaUtils.formatSeasonYear(
                                            subtitleSeason,
                                            subtitleSeasonYear
                                        ),
                                    ).joinToString(separator = " - ")
                                },
                                nextEpisode = {
                                    mediaAsState.value?.nextAiringEpisode?.episode ?: nextEpisode
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
                                onGenreClicked = { TODO() },
                                onGenreLongClicked = { TODO() },
                                onCharacterClicked = { TODO() },
                                onCharacterLongClicked = { TODO() },
                                onStaffClicked = { TODO() },
                                onStaffLongClicked = { TODO() },
                                onTagClicked = ::onTagClick,
                                onTagLongClicked = { TODO() },
                                trailerPlaybackPosition = { viewModel.trailerPlaybackPosition },
                                onTrailerPlaybackPositionUpdate = {
                                    viewModel.trailerPlaybackPosition = it
                                },
                                onMediaClicked = ::onMediaClick,
                                errorRes = { viewModel.errorResource.collectAsState().value },
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun AuthPrompt(onClickAuth: () -> Unit, onSubmitAuthToken: (String) -> Unit) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.anime_auth_prompt))
            TextButton(onClick = onClickAuth) {
                Text(stringResource(R.string.anime_auth_button))
            }
            Text(stringResource(R.string.anime_auth_prompt_paste))

            var value by remember { mutableStateOf("") }
            TextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier
                    .sizeIn(minWidth = 200.dp, minHeight = 200.dp)
                    .padding(16.dp),
            )

            TextButton(onClick = {
                val token = value
                value = ""
                onSubmitAuthToken(token)
            }) {
                Text(stringResource(UtilsStringR.confirm))
            }
        }
    }
}
