package com.thekeeperofpie.artistalleydatabase.alley.app

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.thekeeperofpie.artistalley.BuildConfig
import com.thekeeperofpie.artistalleydatabase.alley.details.ArtistDetailsScreen
import com.thekeeperofpie.artistalleydatabase.alley.details.ArtistDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.alley.search.ArtistAlleySearchScreen
import com.thekeeperofpie.artistalleydatabase.compose.LocalAnimatedVisibilityScope
import com.thekeeperofpie.artistalleydatabase.compose.LocalSharedTransitionScope
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalAnimationApi::class, ExperimentalSharedTransitionApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    enum class Destinations {
        HOME, ARTIST_DETAILS
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Theme {
                Surface {
                    val navController = rememberNavController()
                    Column(modifier = Modifier.fillMaxSize()) {
                        SharedTransitionLayout(modifier = Modifier.weight(1f)) {
                            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                                NavHost(navController, Destinations.HOME.name) {
                                    sharedElementComposable(Destinations.HOME.name) {
                                        ArtistAlleySearchScreen(
                                            onEntryClick = { entry, imageIndex ->
                                                navController.navigate(
                                                    "${Destinations.ARTIST_DETAILS.name}/"
                                                            + entry.id.valueId
                                                            + "?imageIndex=${imageIndex}"
                                                )
                                            }
                                        )
                                    }

                                    sharedElementComposable(
                                        route = "${Destinations.ARTIST_DETAILS.name}/{id}"
                                                + "?imageIndex={imageIndex}",
                                        arguments = listOf(
                                            navArgument("id") {
                                                type = NavType.StringType
                                                nullable = false
                                            },
                                            navArgument("imageIndex") {
                                                type = NavType.StringType
                                                nullable = true
                                            },
                                        ),
                                        enterTransition = {
                                            slideIntoContainer(
                                                AnimatedContentTransitionScope.SlideDirection.Up
                                            )
                                        },
                                        exitTransition = {
                                            slideOutOfContainer(
                                                AnimatedContentTransitionScope.SlideDirection.Down
                                            )
                                        },
                                    ) {
                                        val arguments = it.arguments!!
                                        val id = arguments.getString("id")!!
                                        val imageIndex = arguments.getString("imageIndex", null)
                                            .toIntOrNull() ?: 0
                                        val viewModel =
                                            hiltViewModel<ArtistDetailsViewModel>().apply {
                                                initialize(id)
                                            }
                                        ArtistDetailsScreen(
                                            viewModel,
                                            onClickBack = { navController.navigateUp() },
                                            initialImageIndex = imageIndex,
                                        )
                                    }
                                }
                            }
                        }

                        if (BuildConfig.DEBUG) {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary)
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = "DEBUG",
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Theme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        content: @Composable () -> Unit,
    ) {
        val context = LocalContext.current

        val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            if (darkTheme) darkColorScheme() else lightColorScheme()
        }

        val view = LocalView.current
        if (!view.isInEditMode) {
            SideEffect {
                (view.context as Activity).window.statusBarColor = colorScheme.primary.toArgb()
                WindowCompat.getInsetsController(
                    (view.context as Activity).window,
                    view
                ).isAppearanceLightStatusBars = darkTheme
            }
        }

        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }

    private fun NavGraphBuilder.sharedElementComposable(
        route: String,
        arguments: List<NamedNavArgument> = emptyList(),
        enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
        exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
        content: @Composable (NavBackStackEntry) -> Unit,
    ) = composable(
        route = route,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
    ) {
        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
            content(it)
        }
    }
}
