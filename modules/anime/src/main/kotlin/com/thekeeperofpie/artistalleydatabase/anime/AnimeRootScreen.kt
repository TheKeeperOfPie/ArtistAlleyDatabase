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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anime.home.AnimeHomeScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.viewer.AniListViewerProfileScreen
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysNavigationBar
import com.thekeeperofpie.artistalleydatabase.compose.ScrollStateSaver
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.compose.navigationBarEnterAlwaysScrollBehavior

object AnimeRootScreen {

    @Composable
    operator fun invoke(
        onClickNav: () -> Unit,
        needAuth: @Composable () -> Boolean,
        onClickAuth: () -> Unit,
        onSubmitAuthToken: (String) -> Unit,
        navigationCallback: AnimeNavigator.NavigationCallback,
        errorRes: () -> Pair<Int, Exception?>? = { null },
        onErrorDismiss: () -> Unit = { },
    ) {
        var selectedScreen by rememberSaveable(stateSaver = object :
            Saver<NavDestinations, String> {
            override fun restore(value: String) =
                NavDestinations.values().find { it.id == value } ?: NavDestinations.HOME

            override fun SaverScope.save(value: NavDestinations) = value.id
        }) { mutableStateOf(NavDestinations.HOME) }

        val scrollBehavior = navigationBarEnterAlwaysScrollBehavior()
        val bottomNavigationState = BottomNavigationState(scrollBehavior)

        @Suppress("UnusedMaterial3ScaffoldPaddingParameter")
        Scaffold(
            snackbarHost = {
                SnackbarErrorText(
                    errorRes()?.first,
                    errorRes()?.second,
                    onErrorDismiss = onErrorDismiss
                )
            },
            bottomBar = {
                val needsAuth = needAuth()
                EnterAlwaysNavigationBar(
                    scrollBehavior = scrollBehavior,
                    modifier = Modifier.height(56.dp)
                ) {
                    NavDestinations.values().filter { !it.needsAuth || !needsAuth }
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
                            onClickNav = onClickNav,
                            navigationCallback = navigationCallback,
                            scrollStateSaver = ScrollStateSaver.fromMap(
                                NavDestinations.HOME.id,
                                scrollPositions
                            ),
                            bottomNavigationState = bottomNavigationState,
                        )
                        NavDestinations.ANIME -> AnimeNavigator.UserListScreen(
                            userId = null,
                            mediaType = MediaType.ANIME,
                            onClickNav = onClickNav,
                            showDrawerHandle = true,
                            navigationCallback = navigationCallback,
                            scrollStateSaver = ScrollStateSaver.fromMap(
                                NavDestinations.ANIME.id,
                                scrollPositions
                            ),
                            bottomNavigationState = bottomNavigationState,
                        )
                        NavDestinations.MANGA -> AnimeNavigator.UserListScreen(
                            userId = null,
                            mediaType = MediaType.MANGA,
                            onClickNav = onClickNav,
                            showDrawerHandle = true,
                            navigationCallback = navigationCallback,
                            scrollStateSaver = ScrollStateSaver.fromMap(
                                NavDestinations.MANGA.id,
                                scrollPositions
                            ),
                            bottomNavigationState = bottomNavigationState,
                        )
                        NavDestinations.SEARCH -> AnimeNavigator.SearchScreen(
                            title = null,
                            tagId = null,
                            onClickNav = onClickNav,
                            navigationCallback = navigationCallback,
                            scrollStateSaver = ScrollStateSaver.fromMap(
                                NavDestinations.SEARCH.id,
                                scrollPositions
                            ),
                            bottomNavigationState = bottomNavigationState,
                        )
                        NavDestinations.PROFILE -> AniListViewerProfileScreen(
                            needAuth = needAuth,
                            onClickAuth = onClickAuth,
                            onSubmitAuthToken = onSubmitAuthToken,
                            navigationCallback = navigationCallback,
                            bottomNavigationState = bottomNavigationState,
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
        val needsAuth: Boolean = false,
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
            needsAuth = true,
        ),
        MANGA(
            "manga_list",
            Icons.Filled.LibraryBooks,
            R.string.anime_screen_manga,
            needsAuth = true,
        ),
        SEARCH("anime_search", Icons.Filled.Search, R.string.anime_screen_search),
        PROFILE("anime_profile", Icons.Filled.Person, R.string.anime_screen_profile),
    }
}
