package com.thekeeperofpie.artistalleydatabase.alley

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Approval
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_nav_bar_artists
import artistalleydatabase.modules.alley.generated.resources.alley_nav_bar_browse
import artistalleydatabase.modules.alley.generated.resources.alley_nav_bar_map
import artistalleydatabase.modules.alley.generated.resources.alley_nav_bar_stamp_rallies
import artistalleydatabase.modules.alley.generated.resources.alley_open_update
import artistalleydatabase.modules.alley.generated.resources.alley_update_notice
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.browse.BrowseScreen
import com.thekeeperofpie.artistalleydatabase.alley.map.MapScreen
import com.thekeeperofpie.artistalleydatabase.alley.map.favorites.FavoritesMapScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.tags.MerchEntry
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object ArtistAlleyScreen {

    @Composable
    operator fun invoke(
        // TODO: Remove components/ViewModels from UI layer
        component: ArtistAlleyComponent,
        updateAppUrl: () -> String?,
        onArtistClick: (ArtistEntryGridModel, Int) -> Unit,
        onStampRallyClick: (StampRallyEntryGridModel, Int) -> Unit,
        onSeriesClick: (SeriesEntry) -> Unit,
        onMerchClick: (MerchEntry) -> Unit,
    ) {
        val updateNotice = stringResource(Res.string.alley_update_notice)
        val updateOpenUpdate = stringResource(Res.string.alley_open_update)
        val uriHandler = LocalUriHandler.current
        val artistsScaffoldState = rememberBottomSheetScaffoldState()

        val updateAppUrl = updateAppUrl()
        LaunchedEffect(updateAppUrl) {
            if (updateAppUrl != null) {
                val result = artistsScaffoldState.snackbarHostState.showSnackbar(
                    message = updateNotice,
                    withDismissAction = true,
                    actionLabel = updateOpenUpdate,
                )
                if (result == SnackbarResult.ActionPerformed) {
                    uriHandler.openUri(updateAppUrl)
                }
            }
        }
        val scrollPositions = ScrollStateSaver.scrollPositions()
        val mapTransformState = MapScreen.rememberTransformState()
        var currentDestination by rememberSaveable { mutableStateOf(Destinations.ARTISTS) }
        Column {
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                when (currentDestination) {
                    Destinations.ARTISTS -> {
                        val sortViewModel =
                            viewModel { component.artistSortFilterViewModel(createSavedStateHandle()) }
                        val viewModel = viewModel {
                            component.artistSearchViewModel(
                                createSavedStateHandle(),
                                sortViewModel.state.filterParams,
                            )
                        }
                        ArtistSearchScreen(
                            viewModel = viewModel,
                            sortViewModel = sortViewModel,
                            onClickBack = null,
                            onEntryClick = onArtistClick,
                            scaffoldState = artistsScaffoldState,
                            scrollStateSaver = ScrollStateSaver.fromMap(
                                Destinations.ARTISTS.name,
                                scrollPositions,
                            ),
                        )
                    }
                    Destinations.BROWSE -> {
                        val tagsViewModel = viewModel { component.tagsViewModel() }
                        BrowseScreen(
                            tagsViewModel = tagsViewModel,
                            onSeriesClick = onSeriesClick,
                            onMerchClick = onMerchClick,
                        )
                    }
                    Destinations.MAP -> {
                        val viewModel = viewModel {
                            component.favoritesSortFilterViewModel()
                        }
                        val mapViewModel = viewModel { component.mapViewModel() }
                        FavoritesMapScreen(
                            viewModel = viewModel,
                            mapViewModel = mapViewModel,
                            mapTransformState = mapTransformState,
                            onArtistClick = onArtistClick,
                        )
                    }
                    Destinations.STAMP_RALLIES -> {
                        val sortViewModel = viewModel {
                            component.stampRallySortFilterViewModel(createSavedStateHandle())
                        }
                        val viewModel = viewModel {
                            component.stampRallySearchViewModel(sortViewModel.state.filterParams)
                        }
                        StampRallySearchScreen(
                            viewModel = viewModel,
                            sortViewModel = sortViewModel,
                            onEntryClick = onStampRallyClick,
                            scrollStateSaver = ScrollStateSaver.fromMap(
                                Destinations.STAMP_RALLIES.name,
                                scrollPositions,
                            ),
                        )
                    }
                }
            }
            NavigationBar(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Destinations.entries.forEach {
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
//            navigationSuiteItems = {c
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

    enum class Destinations(val icon: ImageVector, val textRes: StringResource) {
        ARTISTS(Icons.Default.Brush, Res.string.alley_nav_bar_artists),
        BROWSE(Icons.AutoMirrored.Default.List, Res.string.alley_nav_bar_browse),
        MAP(Icons.Default.Map, Res.string.alley_nav_bar_map),
        STAMP_RALLIES(Icons.Default.Approval, Res.string.alley_nav_bar_stamp_rallies),
    }
}
