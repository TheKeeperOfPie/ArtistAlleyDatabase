package com.thekeeperofpie.artistalleydatabase.anime

import android.util.Pair
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.paging.compose.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.anime.list.AnimeUserListScreen
import com.thekeeperofpie.artistalleydatabase.anime.list.AnimeUserListViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeMediaFilterController
import com.thekeeperofpie.artistalleydatabase.anime.search.AnimeSearchScreen
import com.thekeeperofpie.artistalleydatabase.anime.search.AnimeSearchViewModel
import com.thekeeperofpie.artistalleydatabase.compose.ColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText

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

                    val navController = rememberNavController()
                    fun onTagClick(tagId: String, tagName: String) {
                        navController.navigate(
                            AnimeNavDestinations.SEARCH.id +
                                    "?title=$tagName&tagId=$tagId"
                        )
                    }

                    fun onMediaClick(entry: AnimeMediaListRow.Entry) {
                        navController.navigate(
                            "animeDetails?title=${entry.title}" +
                                    "&mediaId=${entry.id!!.valueId}" +
                                    "&bannerImage=${entry.imageBanner}" +
                                    "&coverImage=${entry.imageExtraLarge}" +
                                    "&color=${entry.color?.toArgb()}"
                        )
                    }

                    NavHost(
                        navController = navController,
                        startDestination = AnimeNavDestinations.values()[selectedSubIndex()].id
                    ) {
                        composable(AnimeNavDestinations.LIST.id) {
                            val viewModel = hiltViewModel<AnimeUserListViewModel>()
                                .apply { initialize() }
                            AnimeUserListScreen(
                                onClickNav = onClickNav,
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
                                    + "&mediaId={mediaId}"
                                    + "&coverImage={coverImage}"
                                    + "&color={color}"
                                    + "&bannerImage={bannerImage}",
                            arguments = listOf(
                                navArgument("title") {
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
                            )
                        ) {
                            val arguments = it.arguments!!
                            val title = arguments.getString("title")
                            val mediaId = arguments.getString("mediaId")!!
                            val coverImage = arguments.getString("coverImage")
                            val bannerImage = arguments.getString("bannerImage")
                            val color = arguments.getString("color")
                                ?.toIntOrNull()
                                ?.let(::Color)

                            val viewModel = hiltViewModel<AnimeMediaDetailsViewModel>().apply {
                                initialize(mediaId)
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
                                entry = { mediaAsState.value?.let(AnimeMediaDetailsScreen::Entry) },
                                onGenreClicked = { TODO() },
                                onGenreLongClicked = { TODO() },
                                onCharacterClicked = { TODO() },
                                onCharacterLongClicked = { TODO() },
                                onTagClicked = ::onTagClick,
                                onTagLongClicked = { TODO() },
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