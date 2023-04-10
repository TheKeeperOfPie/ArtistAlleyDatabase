package com.thekeeperofpie.artistalleydatabase.anime

import android.util.Pair
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.paging.compose.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.anime.list.AnimeUserListScreen
import com.thekeeperofpie.artistalleydatabase.anime.list.AnimeUserListViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaFilterController
import com.thekeeperofpie.artistalleydatabase.anime.search.AnimeSearchScreen
import com.thekeeperofpie.artistalleydatabase.anime.search.AnimeSearchViewModel
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText

object AnimeHomeScreen {

    @Composable
    operator fun invoke(
        onClickNav: () -> Unit,
        needAuth: () -> Boolean,
        onClickAuth: () -> Unit,
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
                    AuthPrompt(onClickAuth)
                } else {

                    val navController = rememberNavController()
                    fun onTagClick(tagId: String, tagName: String) {
                        navController.navigate(
                            AnimeNavDestinations.SEARCH.id +
                                    "?title=$tagName&tagId=$tagId"
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
                                onTagClick = ::onTagClick
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
                                initialize(AnimeMediaFilterController.InitialParams(tagId))
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
                                onTagClick = ::onTagClick
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun AuthPrompt(onClickAuth: () -> Unit) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(id = R.string.anime_auth_prompt))
            TextButton(onClick = onClickAuth) {
                Text(stringResource(id = R.string.anime_auth_button))
            }
        }
    }
}