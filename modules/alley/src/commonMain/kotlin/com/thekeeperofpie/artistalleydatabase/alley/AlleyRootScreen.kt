package com.thekeeperofpie.artistalleydatabase.alley

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Approval
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_nav_bar_artists
import artistalleydatabase.modules.alley.generated.resources.alley_nav_bar_browse
import artistalleydatabase.modules.alley.generated.resources.alley_nav_bar_favorites
import artistalleydatabase.modules.alley.generated.resources.alley_nav_bar_map
import artistalleydatabase.modules.alley.generated.resources.alley_nav_bar_stamp_rallies
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.browse.BrowseScreen
import com.thekeeperofpie.artistalleydatabase.alley.favorite.FavoritesScreen
import com.thekeeperofpie.artistalleydatabase.alley.map.MapScreen
import com.thekeeperofpie.artistalleydatabase.alley.map.favorites.FavoritesMapScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberDataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object AlleyRootScreen {

    @Composable
    operator fun invoke(
        // TODO: Remove components/ViewModels from UI layer
        component: ArtistAlleyComponent,
        snackbarHostState: SnackbarHostState,
        onArtistClick: (ArtistEntryGridModel, Int) -> Unit,
        onSeriesClick: (String) -> Unit,
        onMerchClick: (String) -> Unit,
    ) {
        val scrollPositions = ScrollStateSaver.scrollPositions()
        val mapTransformState = MapScreen.rememberTransformState()
        var currentDestination by rememberSaveable { mutableStateOf(Destination.ARTISTS) }
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                Destination.entries.forEach {
                    item(
                        icon = {
                            Icon(
                                it.icon,
                                contentDescription = stringResource(it.textRes)
                            )
                        },
                        label = { Text(stringResource(it.textRes)) },
                        selected = it == currentDestination,
                        onClick = { currentDestination = it }
                    )
                }
            }
        ) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(Modifier.fillMaxSize().padding(it)) {
                    when (currentDestination) {
                        Destination.ARTISTS -> {
                            val viewModel = viewModel {
                                component.artistSearchViewModel(
                                    createSavedStateHandle().apply { this["isRoot"] = true },
                                )
                            }
                            ArtistSearchScreen(
                                viewModel = viewModel,
                                sortFilterController = viewModel.sortFilterController,
                                onClickBack = null,
                                scrollStateSaver = ScrollStateSaver.fromMap(
                                    Destination.ARTISTS.name,
                                    scrollPositions,
                                ),
                            )
                        }
                        Destination.BROWSE -> {
                            val viewModel = viewModel {
                                component.tagsViewModel(createSavedStateHandle())
                            }
                            val dataYearHeaderState =
                                rememberDataYearHeaderState(viewModel.dataYear, null)
                            BrowseScreen(
                                dataYearHeaderState = dataYearHeaderState,
                                tagsViewModel = viewModel,
                                seriesFiltersState = { viewModel.seriesFiltersState },
                                onSeriesFilterClick = viewModel::onSeriesFilterClick,
                                onSeriesClick = { onSeriesClick(it.id) },
                                onMerchClick = { onMerchClick(it.name) },
                            )
                        }
                        Destination.FAVORITES -> {
                            val favoritesViewModel = viewModel {
                                component.favoritesViewModel(createSavedStateHandle())
                            }
                            FavoritesScreen(
                                favoritesViewModel = favoritesViewModel,
                                artistSortFilterController = favoritesViewModel.artistSortFilterController,
                                stampRallySortFilterController = favoritesViewModel.stampRallySortFilterController,
                                scrollStateSaver = ScrollStateSaver.fromMap(
                                    Destination.FAVORITES.name,
                                    scrollPositions,
                                ),
                                onNavigateToArtists = { currentDestination = Destination.ARTISTS },
                                onNavigateToRallies = {
                                    currentDestination = Destination.STAMP_RALLIES
                                },
                            )
                        }
                        Destination.MAP -> {
                            val viewModel = viewModel {
                                component.favoritesSortFilterViewModel(createSavedStateHandle())
                            }
                            val mapViewModel =
                                viewModel { component.mapViewModel(createSavedStateHandle()) }
                            FavoritesMapScreen(
                                viewModel = viewModel,
                                mapViewModel = mapViewModel,
                                mapTransformState = mapTransformState,
                                onArtistClick = onArtistClick,
                            )
                        }
                        Destination.STAMP_RALLIES -> {
                            val viewModel = viewModel {
                                component.stampRallySearchViewModel(createSavedStateHandle())
                            }
                            StampRallySearchScreen(
                                viewModel = viewModel,
                                sortFilterState = viewModel.sortFilterController.state,
                                scrollStateSaver = ScrollStateSaver.fromMap(
                                    Destination.STAMP_RALLIES.name,
                                    scrollPositions,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }

    enum class Destination(val icon: ImageVector, val textRes: StringResource) {
        ARTISTS(Icons.Default.Brush, Res.string.alley_nav_bar_artists),
        BROWSE(Icons.AutoMirrored.Default.List, Res.string.alley_nav_bar_browse),
        FAVORITES(Icons.Default.Favorite, Res.string.alley_nav_bar_favorites),
        MAP(Icons.Default.Map, Res.string.alley_nav_bar_map),
        STAMP_RALLIES(Icons.Default.Approval, Res.string.alley_nav_bar_stamp_rallies),
    }
}
