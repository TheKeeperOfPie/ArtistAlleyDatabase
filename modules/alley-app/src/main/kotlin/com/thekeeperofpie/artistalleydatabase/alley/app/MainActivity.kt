package com.thekeeperofpie.artistalleydatabase.alley.app

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.mxalbert.sharedelements.SharedElementsRoot
import com.thekeeperofpie.artistalleydatabase.alley.details.ArtistDetailsScreen
import com.thekeeperofpie.artistalleydatabase.alley.details.ArtistDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.alley.search.ArtistAlleySearchScreen
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalAnimationApi::class)
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
                    val navController = rememberAnimatedNavController()
                    SharedElementsRoot {
                        AnimatedNavHost(navController, Destinations.HOME.name) {
                            composable(Destinations.HOME.name) {
                                ArtistAlleySearchScreen(
                                    onEntryClick = {
                                        navController.navigate("${Destinations.ARTIST_DETAILS.name}/" + it.id.valueId)
                                    }
                                )
                            }

                            composable(
                                route = "${Destinations.ARTIST_DETAILS.name}/{id}",
                                arguments = listOf(
                                    navArgument("id") {
                                        type = NavType.StringType
                                        nullable = false
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
                                val viewModel = hiltViewModel<ArtistDetailsViewModel>().apply {
                                    initialize(id)
                                }
                                ArtistDetailsScreen(
                                    viewModel,
                                    onClickBack = { navController.popBackStack() },
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
        content: @Composable () -> Unit
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
}
