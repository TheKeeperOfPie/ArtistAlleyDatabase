package com.thekeeperofpie.artistalleydatabase.anime

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysNavigationBar
import com.thekeeperofpie.artistalleydatabase.compose.NavigationBarItem
import com.thekeeperofpie.artistalleydatabase.compose.ScrollStateSaver
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.compose.navigationBarEnterAlwaysScrollBehavior

object AnimeHomeScreen {

    @Composable
    operator fun invoke(
        onClickNav: () -> Unit,
        needAuth: () -> Boolean,
        onClickAuth: () -> Unit,
        onSubmitAuthToken: (String) -> Unit,
        navigationCallback: AnimeNavigator.NavigationCallback,
        errorRes: () -> Pair<Int, Exception?>? = { null },
        onErrorDismiss: () -> Unit = { },
    ) {
        var selectedScreen by rememberSaveable(stateSaver = object :
            Saver<NavDestinations, String> {
            override fun restore(value: String) =
                NavDestinations.values().find { it.id == value } ?: NavDestinations.SEARCH

            override fun SaverScope.save(value: NavDestinations) = value.id
        }) { mutableStateOf(NavDestinations.SEARCH) }

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
                EnterAlwaysNavigationBar(scrollBehavior = scrollBehavior) {
                    NavDestinations.values().forEach { destination ->
                        val onLongClick = if (destination == NavDestinations.ANIME) {
                            {
                                navigationCallback.onIgnoreListOpen(MediaType.ANIME)
                            }
                            // TODO: Support manga only lists, need to store ignored IDs in
                            //  separate settings field
                        } else null
                        NavigationBarItem(
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(stringResource(destination.textRes)) },
                            selected = selectedScreen == destination,
                            onClick = { selectedScreen = destination },
                            onLongClick = onLongClick,
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
                if (needAuth()) {
                    AuthPrompt(onClickAuth = onClickAuth, onSubmitAuthToken = onSubmitAuthToken)
                } else {
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
                            NavDestinations.PROFILE -> AnimeNavigator.UserScreen(
                                userId = null,
                                navigationCallback = navigationCallback,
                                bottomNavigationState = bottomNavigationState,
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

    private enum class NavDestinations(
        val id: String,
        val icon: ImageVector,
        @StringRes val textRes: Int,
    ) {
        ANIME("anime_list", Icons.Filled.VideoLibrary, R.string.anime_screen_anime),
        MANGA("manga_list", Icons.Filled.LibraryBooks, R.string.anime_screen_manga),
        SEARCH("anime_search", Icons.Filled.Search, R.string.anime_screen_search),
        PROFILE("anime_profile", Icons.Filled.Person, R.string.anime_screen_profile),
    }
}
