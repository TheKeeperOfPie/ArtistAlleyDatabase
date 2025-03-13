package com.thekeeperofpie.artistalleydatabase.alley

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Approval
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
        onArtistClick: (ArtistEntryGridModel, Int) -> Unit,
        onSeriesClick: (String) -> Unit,
        onMerchClick: (String) -> Unit,
    ) {
        val artistsScaffoldState = rememberBottomSheetScaffoldState()
        val scrollPositions = ScrollStateSaver.scrollPositions()
        val mapTransformState = MapScreen.rememberTransformState()
        var currentDestination by rememberSaveable { mutableStateOf(Destination.ARTISTS) }
        Column {
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                when (currentDestination) {
                    Destination.ARTISTS -> {
                        val sortViewModel =
                            viewModel { component.artistSortFilterViewModel(createSavedStateHandle()) }
                        val viewModel = viewModel {
                            component.artistSearchViewModel(
                                createSavedStateHandle().apply { this["isRoot"] = true },
                                sortViewModel.state.filterParams,
                            )
                        }
                        ArtistSearchScreen(
                            viewModel = viewModel,
                            sortViewModel = sortViewModel,
                            onClickBack = null,
                            scaffoldState = artistsScaffoldState,
                            scrollStateSaver = ScrollStateSaver.fromMap(
                                Destination.ARTISTS.name,
                                scrollPositions,
                            ),
                        )
                    }
                    Destination.BROWSE -> {
                        val viewModel = viewModel { component.tagsViewModel() }
                        val dataYearHeaderState =
                            rememberDataYearHeaderState(viewModel.dataYear, null)
                        BrowseScreen(
                            dataYearHeaderState = dataYearHeaderState,
                            tagsViewModel = viewModel,
                            onSeriesClick = { onSeriesClick(it.name) },
                            onMerchClick = { onMerchClick(it.name) },
                        )
                    }
                    Destination.FAVORITES -> {
                        val artistSortViewModel =
                            viewModel { component.artistSortFilterViewModel(createSavedStateHandle()) }
                        val stampRallySortViewModel =
                            viewModel {
                                component.stampRallySortFilterViewModel(
                                    createSavedStateHandle()
                                )
                            }
                        val favoritesViewModel = viewModel {
                            component.favoritesViewModel(
                                createSavedStateHandle(),
                                artistSortViewModel.state.filterParams,
                                stampRallySortViewModel.state.filterParams,
                            )
                        }
                        FavoritesScreen(
                            favoritesViewModel = favoritesViewModel,
                            artistSortViewModel = artistSortViewModel,
                            stampRallySortViewModel = stampRallySortViewModel,
                            scrollStateSaver = ScrollStateSaver.fromMap(
                                Destination.FAVORITES.name,
                                scrollPositions,
                            ),
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
                        val sortViewModel = viewModel {
                            component.stampRallySortFilterViewModel(createSavedStateHandle())
                        }
                        val viewModel = viewModel {
                            component.stampRallySearchViewModel(sortViewModel.state.filterParams)
                        }
                        StampRallySearchScreen(
                            viewModel = viewModel,
                            sortViewModel = sortViewModel,
                            scrollStateSaver = ScrollStateSaver.fromMap(
                                Destination.STAMP_RALLIES.name,
                                scrollPositions,
                            ),
                        )
                    }
                }
            }
            NavigationBar(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Destination.entries.forEach {
                    NavigationBarItem(
                        icon = {
                            Icon(
                                it.icon,
                                contentDescription = stringResource(it.textRes)
                            )
                        },
                        label = { Text(stringResource(it.textRes)) },
                        selected = it == currentDestination,
                        onClick = { currentDestination = it },
                    )
                }
            }
        }
        // TODO: Doesn't work on wasmJs, might be due to version mismatch
//        NavigationSuiteScaffold(
//            navigationSuiteItems = {
//                    item(
//                        icon = {
//                            Icon(
//                                it.icon,
//                                contentDescription = stringResource(it.textRes)
//                            )
//                        },
//                        label = { Text(stringResource(it.textRes)) },
//                        selected = it == currentDestination,
//                        onClick = { currentDestination = it }
//                    )
//                }
//            }
//        ) {
    }

    enum class Destination(val icon: ImageVector, val textRes: StringResource) {
        ARTISTS(Icons.Default.Brush, Res.string.alley_nav_bar_artists),
        BROWSE(Icons.AutoMirrored.Default.List, Res.string.alley_nav_bar_browse),
        FAVORITES(Icons.Default.Favorite, Res.string.alley_nav_bar_favorites),
        MAP(Icons.Default.Map, Res.string.alley_nav_bar_map),
        STAMP_RALLIES(Icons.Default.Approval, Res.string.alley_nav_bar_stamp_rallies),
    }
}
