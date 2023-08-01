package com.thekeeperofpie.artistalleydatabase.anime

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
        navigationCallback: AnimeNavigator.NavigationCallback,
        onClickSettings: () -> Unit,
    ) {
        var selectedScreen by rememberSaveable(stateSaver = object :
            Saver<NavDestinations, String> {
            override fun restore(value: String) =
                NavDestinations.values().find { it.id == value } ?: NavDestinations.HOME

            override fun SaverScope.save(value: NavDestinations) = value.id
        }) { mutableStateOf(NavDestinations.HOME) }

        val scrollBehavior = navigationBarEnterAlwaysScrollBehavior()
        val bottomNavigationState = BottomNavigationState(scrollBehavior)
        val needsAuth by viewModel.needsAuth.collectAsState(true)

        @Suppress("UnusedMaterial3ScaffoldPaddingParameter")
        Scaffold(
            snackbarHost = {
                val appUpdateChecker = LocalAppUpdateChecker.current
                val snackbarHostState = remember { SnackbarHostState() }
                appUpdateChecker?.applySnackbarState(snackbarHostState)
                SnackbarHost(hostState = snackbarHostState)
            },
            bottomBar = {
                val unlocked by viewModel.unlocked.collectAsState(initial = false)
                EnterAlwaysNavigationBar(
                    scrollBehavior = scrollBehavior,
                    modifier = Modifier.height(56.dp)
                ) {
                    NavDestinations.values()
                        .filter { !it.requiresAuth || !needsAuth }
                        .filter { !it.requiresUnlock || unlocked }
                        .filter { it != NavDestinations.UNLOCK || !unlocked }
                        .forEach { destination ->
                            NavigationBarItem(
                                icon = { Icon(destination.icon, contentDescription = null) },
                                selected = selectedScreen == destination,
                                onClick = {
                                    if (selectedScreen == destination &&
                                        destination == NavDestinations.ANIME
                                    ) {
                                        // TODO: Support manga only lists, need to store ignored IDs
                                        //  in separate settings field
                                        navigationCallback.onIgnoreListOpen(MediaType.ANIME)
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
                        NavDestinations.HOME -> AnimeHomeScreen(
                            upIconOption = upIconOption,
                            navigationCallback = navigationCallback,
                            scrollStateSaver = ScrollStateSaver.fromMap(
                                NavDestinations.HOME.id,
                                scrollPositions
                            ),
                            bottomNavigationState = bottomNavigationState,
                        )
                        NavDestinations.ANIME -> AnimeNavigator.UserListScreen(
                            userId = null,
                            userName = null,
                            mediaType = MediaType.ANIME,
                            upIconOption = upIconOption,
                            navigationCallback = navigationCallback,
                            scrollStateSaver = ScrollStateSaver.fromMap(
                                NavDestinations.ANIME.id,
                                scrollPositions
                            ),
                            bottomNavigationState = bottomNavigationState,
                        )
                        NavDestinations.MANGA -> AnimeNavigator.UserListScreen(
                            userId = null,
                            userName = null,
                            mediaType = MediaType.MANGA,
                            upIconOption = upIconOption,
                            navigationCallback = navigationCallback,
                            scrollStateSaver = ScrollStateSaver.fromMap(
                                NavDestinations.MANGA.id,
                                scrollPositions
                            ),
                            bottomNavigationState = bottomNavigationState,
                        )
                        NavDestinations.SEARCH -> {
                            val viewModel = hiltViewModel<AnimeSearchViewModel>().apply {
                                initialize(defaultMediaSort = MediaSortOption.SEARCH_MATCH)
                            }
                            AnimeSearchScreen(
                                upIconOption = upIconOption,
                                viewModel = viewModel,
                                navigationCallback = navigationCallback,
                                scrollStateSaver = ScrollStateSaver.fromMap(
                                    NavDestinations.SEARCH.id,
                                    scrollPositions
                                ),
                                bottomNavigationState = bottomNavigationState,
                            )
                        }
                        NavDestinations.PROFILE -> AniListViewerProfileScreen(
                            upIconOption = upIconOption,
                            needsAuth = { needsAuth },
                            onClickAuth = onClickAuth,
                            onSubmitAuthToken = onSubmitAuthToken,
                            onClickSettings = onClickSettings,
                            navigationCallback = navigationCallback,
                            bottomNavigationState = bottomNavigationState,
                        )
                        NavDestinations.UNLOCK -> UnlockScreen(
                            upIconOption = upIconOption,
                            bottomNavigationState = bottomNavigationState,
                            onClickSettings = onClickSettings,
                        )
                    }
                }
            }
        }
    }

    private enum class NavDestinations(
        val id: String,
        val icon: ImageVector,
        @StringRes val textRes: Int,
        val requiresAuth: Boolean = false,
        val requiresUnlock: Boolean = false,
    ) {
        HOME(
            "home",
            Icons.Filled.Home,
            R.string.anime_screen_home,
        ),
        ANIME(
            "anime_list",
            Icons.Filled.VideoLibrary,
            R.string.anime_screen_anime,
            requiresAuth = true,
            requiresUnlock = true,
        ),
        MANGA(
            "manga_list",
            Icons.Filled.LibraryBooks,
            R.string.anime_screen_manga,
            requiresAuth = true,
            requiresUnlock = true,
        ),
        SEARCH("anime_search", Icons.Filled.Search, R.string.anime_screen_search),
        PROFILE(
            id = "anime_profile",
            icon = Icons.Filled.Person,
            textRes = R.string.anime_screen_profile,
            requiresUnlock = true,
        ),
        UNLOCK("anime_unlock", Icons.Filled.Lock, R.string.anime_screen_unlock),
    }
}
