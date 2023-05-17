package com.thekeeperofpie.artistalleydatabase.anime

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
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
            Saver<AnimeNavDestinations, String> {
            override fun restore(value: String) =
                AnimeNavDestinations.values().find { it.id == value } ?: AnimeNavDestinations.SEARCH

            override fun SaverScope.save(value: AnimeNavDestinations) = value.id
        }) { mutableStateOf(AnimeNavDestinations.SEARCH) }

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
                    AnimeNavDestinations.values().forEach { destination ->
                        val onLongClick = if (destination == AnimeNavDestinations.ANIME) {
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
                            AnimeNavDestinations.ANIME -> AnimeNavigator.UserListScreen(
                                userId = null,
                                mediaType = MediaType.ANIME,
                                onClickNav = onClickNav,
                                showDrawerHandle = true,
                                navigationCallback = navigationCallback,
                                scrollStateSaver = ScrollStateSaver.fromMap(
                                    AnimeNavDestinations.ANIME.id,
                                    scrollPositions
                                ),
                                bottomNavigationState = bottomNavigationState,
                            )
                            AnimeNavDestinations.MANGA -> AnimeNavigator.UserListScreen(
                                userId = null,
                                mediaType = MediaType.MANGA,
                                onClickNav = onClickNav,
                                showDrawerHandle = true,
                                navigationCallback = navigationCallback,
                                scrollStateSaver = ScrollStateSaver.fromMap(
                                    AnimeNavDestinations.MANGA.id,
                                    scrollPositions
                                ),
                                bottomNavigationState = bottomNavigationState,
                            )
                            AnimeNavDestinations.SEARCH -> AnimeNavigator.SearchScreen(
                                title = null,
                                tagId = null,
                                onClickNav = onClickNav,
                                navigationCallback = navigationCallback,
                                scrollStateSaver = ScrollStateSaver.fromMap(
                                    AnimeNavDestinations.SEARCH.id,
                                    scrollPositions
                                ),
                                bottomNavigationState = bottomNavigationState,
                            )
                            AnimeNavDestinations.PROFILE -> AnimeNavigator.UserScreen(
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
}
