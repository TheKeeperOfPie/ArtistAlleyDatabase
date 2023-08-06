package com.thekeeperofpie.artistalleydatabase.anime

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anime.home.AnimeHomeScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.search.AnimeSearchScreen
import com.thekeeperofpie.artistalleydatabase.anime.search.AnimeSearchViewModel
import com.thekeeperofpie.artistalleydatabase.anime.user.viewer.AniListViewerProfileScreen
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysNavigationBar
import com.thekeeperofpie.artistalleydatabase.compose.ScrollStateSaver
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.navigationBarEnterAlwaysScrollBehavior
import com.thekeeperofpie.artistalleydatabase.compose.update.LocalAppUpdateChecker
import com.thekeeperofpie.artistalleydatabase.monetization.UnlockScreen

object AnimeRootScreen {

    @Composable
    operator fun invoke(
        upIconOption: UpIconOption?,
        viewModel: AnimeRootViewModel,
        onClickAuth: () -> Unit,
        onSubmitAuthToken: (String) -> Unit,
        onClickSettings: () -> Unit,
    ) {

        val scrollBehavior = navigationBarEnterAlwaysScrollBehavior()
        val bottomNavigationState = BottomNavigationState(scrollBehavior)
        val needsAuth = viewModel.authToken.collectAsState().value == null

        var selectedScreen by rememberSaveable(stateSaver = AnimeRootNavDestination.StateSaver) {
            mutableStateOf(viewModel.persistedSelectedScreen
                .takeIf { !it.requiresAuth || !needsAuth }
                ?.takeIf { !it.requiresUnlock || viewModel.unlocked() }
                ?: AnimeRootNavDestination.HOME
            )
        }

        @Suppress("UnusedMaterial3ScaffoldPaddingParameter")
        Scaffold(
            snackbarHost = {
                val appUpdateChecker = LocalAppUpdateChecker.current
                val snackbarHostState = remember { SnackbarHostState() }
                appUpdateChecker?.applySnackbarState(snackbarHostState)
                SnackbarHost(hostState = snackbarHostState)
            },
            bottomBar = {
                EnterAlwaysNavigationBar(
                    scrollBehavior = scrollBehavior,
                    modifier = Modifier.height(56.dp)
                ) {
                    val unlocked by viewModel.unlocked.collectAsState(initial = false)
                    AnimeRootNavDestination.values()
                        .filter { !it.requiresAuth || !needsAuth }
                        .filter { !it.requiresUnlock || unlocked }
                        .filter { it != AnimeRootNavDestination.UNLOCK || !unlocked }
                        .forEach { destination ->
                            NavigationBarItem(
                                icon = { Icon(destination.icon, contentDescription = null) },
                                selected = selectedScreen == destination,
                                onClick = {
                                    if (selectedScreen == destination &&
                                        destination == AnimeRootNavDestination.ANIME
                                    ) {
                                        // TODO: Support manga only lists, need to store ignored IDs
                                        //  in separate settings field
                                        // TODO: Re-enable ignore
//                                        navigationCallback.onIgnoreListOpen(MediaType.ANIME)
                                    } else {
                                        selectedScreen = destination
                                    }
                                },
                            )
                        }
                }
            },
        ) {
            val scrollPositions = ScrollStateSaver.scrollPositions()
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                AnimatedContent(
                    targetState = selectedScreen,
                    transitionSpec = {
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up)
                            .togetherWith(
                                slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Down
                                )
                            )
                    },
                    label = "Anime home destination transition",
                ) {
                    when (it) {
                        AnimeRootNavDestination.HOME -> AnimeHomeScreen(
                            upIconOption = upIconOption,
                            scrollStateSaver = ScrollStateSaver.fromMap(
                                AnimeRootNavDestination.HOME.id,
                                scrollPositions
                            ),
                            bottomNavigationState = bottomNavigationState,
                        )
                        AnimeRootNavDestination.ANIME -> AnimeNavigator.UserListScreen(
                            userId = null,
                            userName = null,
                            mediaType = MediaType.ANIME,
                            upIconOption = upIconOption,
                            scrollStateSaver = ScrollStateSaver.fromMap(
                                AnimeRootNavDestination.ANIME.id,
                                scrollPositions
                            ),
                            bottomNavigationState = bottomNavigationState,
                        )
                        AnimeRootNavDestination.MANGA -> AnimeNavigator.UserListScreen(
                            userId = null,
                            userName = null,
                            mediaType = MediaType.MANGA,
                            upIconOption = upIconOption,
                            scrollStateSaver = ScrollStateSaver.fromMap(
                                AnimeRootNavDestination.MANGA.id,
                                scrollPositions
                            ),
                            bottomNavigationState = bottomNavigationState,
                        )
                        AnimeRootNavDestination.SEARCH -> {
                            val viewModel = hiltViewModel<AnimeSearchViewModel>().apply {
                                initialize(
                                    defaultMediaSort = MediaSortOption.SEARCH_MATCH,
                                    lockSort = false,
                                )
                            }
                            AnimeSearchScreen(
                                upIconOption = upIconOption,
                                viewModel = viewModel,
                                scrollStateSaver = ScrollStateSaver.fromMap(
                                    AnimeRootNavDestination.SEARCH.id,
                                    scrollPositions
                                ),
                                bottomNavigationState = bottomNavigationState,
                            )
                        }
                        AnimeRootNavDestination.PROFILE -> AniListViewerProfileScreen(
                            upIconOption = upIconOption,
                            needsAuth = { needsAuth },
                            onClickAuth = onClickAuth,
                            onSubmitAuthToken = onSubmitAuthToken,
                            onClickSettings = onClickSettings,
                            bottomNavigationState = bottomNavigationState,
                        )
                        AnimeRootNavDestination.UNLOCK -> UnlockScreen(
                            upIconOption = upIconOption,
                            bottomNavigationState = bottomNavigationState,
                            onClickSettings = onClickSettings,
                        )
                    }
                }
            }
        }
    }
}
