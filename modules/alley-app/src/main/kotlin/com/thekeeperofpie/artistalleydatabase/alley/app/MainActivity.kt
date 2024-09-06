package com.thekeeperofpie.artistalleydatabase.alley.app

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.thekeeperofpie.artistalley.BuildConfig
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyScreen
import com.thekeeperofpie.artistalleydatabase.alley.Destinations
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.details.ArtistDetailsScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.map.ArtistMapScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.details.StampRallyDetailsScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.map.StampRallyMapScreen
import com.thekeeperofpie.artistalleydatabase.alley.tags.map.TagMapScreen
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalSharedTransitionScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementComposable
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@OptIn(
    ExperimentalAnimationApi::class, ExperimentalSharedTransitionApi::class,
    ExperimentalMaterial3Api::class
)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var navigationTypeMap: NavigationTypeMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Theme {
                Surface {
                    val navController = rememberNavController()
                    val onArtistClick = { entry: ArtistEntryGridModel, imageIndex: Int ->
                        navController.navigate(
                            Destinations.ArtistDetails(entry.id.valueId, imageIndex.toString())
                        )
                    }
                    Column(modifier = Modifier.fillMaxSize()) {
                        SharedTransitionLayout(modifier = Modifier.weight(1f)) {
                            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                                NavHost(navController, Destinations.Home) {
                                    sharedElementComposable<Destinations.Home>(
                                        navigationTypeMap = navigationTypeMap,
                                        enterTransition = null,
                                        exitTransition = null,
                                    ) {
                                        ArtistAlleyScreen(
                                            onArtistClick = onArtistClick,
                                            onStampRallyClick = { entry, imageIndex ->
                                                navController.navigate(
                                                    Destinations.StampRallyDetails(
                                                        entry.value.id,
                                                        imageIndex.toString(),
                                                    )
                                                )
                                            },
                                            onSeriesClick = {
                                                navController.navigate(
                                                    Destinations.Series(it.name)
                                                )
                                            },
                                            onMerchClick = {
                                                navController.navigate(
                                                    Destinations.Merch(it.name)
                                                )
                                            },
                                        )
                                    }

                                    sharedElementComposable<Destinations.ArtistDetails>(
                                        navigationTypeMap = navigationTypeMap,
                                    ) {
                                        val route = it.toRoute<Destinations.ArtistDetails>()
                                        ArtistDetailsScreen(
                                            onClickBack = navController::navigateUp,
                                            onSeriesClick = {
                                                navController.navigate(
                                                    Destinations.Series(it.text)
                                                )
                                            },
                                            onMerchClick = {
                                                navController.navigate(
                                                    Destinations.Merch(it)
                                                )
                                            },
                                            onStampRallyClick = {
                                                navController.navigate(
                                                    Destinations.StampRallyDetails(it.id)
                                                )
                                            },
                                            onArtistMapClick = {
                                                navController.navigate(
                                                    Destinations.ArtistMap(route.id)
                                                )
                                            }
                                        )
                                    }

                                    sharedElementComposable<Destinations.ArtistMap>(
                                        navigationTypeMap = navigationTypeMap,
                                    ) {
                                        ArtistMapScreen(
                                            onClickBack = navController::navigateUp,
                                            onArtistClick = onArtistClick,
                                        )
                                    }

                                    sharedElementComposable<Destinations.StampRallyDetails>(
                                        navigationTypeMap = navigationTypeMap,
                                    ) {
                                        val route = it.toRoute<Destinations.StampRallyDetails>()
                                        StampRallyDetailsScreen(
                                            onClickBack = navController::navigateUp,
                                            onArtistClick = {
                                                navController.navigate(
                                                    Destinations.ArtistDetails(it.id)
                                                )
                                            },
                                            onStampRallyMapClick = {
                                                navController.navigate(
                                                    Destinations.StampRallyMap(route.id)
                                                )
                                            }
                                        )
                                    }

                                    sharedElementComposable<Destinations.StampRallyMap>(
                                        navigationTypeMap = navigationTypeMap,
                                    ) {
                                        StampRallyMapScreen(
                                            onClickBack = navController::navigateUp,
                                            onArtistClick = { entry, imageIndex ->
                                                navController.navigate(
                                                    Destinations.ArtistDetails(
                                                        entry.value.id,
                                                        imageIndex.toString(),
                                                    )
                                                )
                                            },
                                        )
                                    }

                                    sharedElementComposable<Destinations.Series>(
                                        navigationTypeMap = navigationTypeMap,
                                    ) {
                                        val route = it.toRoute<Destinations.Series>()
                                        ArtistSearchScreen(
                                            onClickBack = navController::navigateUp,
                                            onEntryClick = onArtistClick,
                                            scrollStateSaver = ScrollStateSaver(),
                                            onClickMap = {
                                                navController.navigate(
                                                    Destinations.SeriesMap(route.series)
                                                )
                                            },
                                        )
                                    }

                                    sharedElementComposable<Destinations.Merch>(
                                        navigationTypeMap = navigationTypeMap,
                                    ) {
                                        val route = it.toRoute<Destinations.Merch>()
                                        ArtistSearchScreen(
                                            onClickBack = navController::navigateUp,
                                            onEntryClick = onArtistClick,
                                            scrollStateSaver = ScrollStateSaver(),
                                            onClickMap = {
                                                navController.navigate(
                                                    Destinations.MerchMap(route.merch)
                                                )
                                            },
                                        )
                                    }

                                    sharedElementComposable<Destinations.SeriesMap>(
                                        navigationTypeMap = navigationTypeMap,
                                    ) {
                                        TagMapScreen(
                                            onClickBack = navController::navigateUp,
                                            onArtistClick = onArtistClick,
                                        )
                                    }

                                    sharedElementComposable<Destinations.MerchMap>(
                                        navigationTypeMap = navigationTypeMap,
                                    ) {
                                        TagMapScreen(
                                            onClickBack = navController::navigateUp,
                                            onArtistClick = onArtistClick,
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
}
