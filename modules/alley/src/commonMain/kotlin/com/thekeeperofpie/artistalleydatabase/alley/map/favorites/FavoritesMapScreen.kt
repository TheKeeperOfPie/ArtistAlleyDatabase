package com.thekeeperofpie.artistalleydatabase.alley.map.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_map_search
import artistalleydatabase.modules.alley.generated.resources.alley_map_search_close
import artistalleydatabase.modules.alley.generated.resources.alley_map_search_tags_clear
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.SeriesPrediction
import com.thekeeperofpie.artistalleydatabase.alley.map.HighlightedTableCell
import com.thekeeperofpie.artistalleydatabase.alley.map.MapScreen
import com.thekeeperofpie.artistalleydatabase.alley.map.MapScreen.ZoomSlider
import com.thekeeperofpie.artistalleydatabase.alley.map.MapViewModel
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.series.name
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.filled.Clear
import com.thekeeperofpie.artistalleydatabase.icons.filled.Search
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterOptionsPanel
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object FavoritesMapScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyGraph,
        mapTransformState: MapScreen.TransformState,
        onArtistClick: (ArtistEntryGridModel, Int) -> Unit,
        viewModel: FavoritesMapViewModel = viewModel {
            graph.favoritesMapViewModelFactory.create(
                createSavedStateHandle()
            )
        },
        mapViewModel: MapViewModel = viewModel {
            graph.mapViewModelFactory.create(createSavedStateHandle())
        },
    ) {
        val tagResults by viewModel.tagResults.collectAsStateWithLifecycle()
        val seriesIdIn by viewModel.seriesIdIn.collectAsStateWithLifecycle()
        val merchIdIn by viewModel.merchIdIn.collectAsStateWithLifecycle()
        val seriesById by viewModel.tagAutocomplete.seriesById.collectAsStateWithLifecycle()
        val merchById by viewModel.tagAutocomplete.merchById.collectAsStateWithLifecycle()
        val highlightedBooths by viewModel.highlightedBooths.collectAsStateWithLifecycle()
        FavoritesMapScreen(
            state = viewModel.state,
            query = viewModel.query,
            tagResults = { tagResults },
            mapTransformState = mapTransformState,
            highlightedBooths = { highlightedBooths },
            seriesIdIn = { seriesIdIn },
            merchIdIn = { merchIdIn },
            seriesById = { seriesById },
            merchById = { merchById },
            onArtistClick = onArtistClick,
            onSeriesSelected = viewModel::onSeriesSelected,
            onMerchSelected = viewModel::onMerchSelected,
            onClearTags = viewModel::onClearTags,
            mapViewModel = mapViewModel,
        )
    }

    @Composable
    operator fun invoke(
        state: SortFilterState<FavoritesMapViewModel.FilterParams>,
        query: TextFieldState,
        tagResults: () -> Pair<List<MerchInfo>, List<SeriesInfo>>,
        mapTransformState: MapScreen.TransformState,
        highlightedBooths: () -> Set<String>,
        seriesIdIn: () -> List<String>,
        merchIdIn: () -> List<String>,
        seriesById: () -> Map<String, SeriesInfo>,
        merchById: () -> Map<String, MerchInfo>,
        onArtistClick: (ArtistEntryGridModel, Int) -> Unit,
        onSeriesSelected: (SeriesInfo, Boolean) -> Unit,
        onMerchSelected: (MerchInfo, Boolean) -> Unit,
        onClearTags: () -> Unit,
        mapViewModel: MapViewModel,
    ) {
        val scaffoldState = rememberBottomSheetScaffoldState()
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 102.dp,
            sheetDragHandle = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        modifier = Modifier.padding(18.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = MaterialTheme.shapes.extraLarge,
                    ) {
                        Box(Modifier.size(width = 32.dp, height = 4.dp))
                    }
                    ZoomSlider(
                        transformState = mapTransformState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 64.dp)
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            },
            sheetContent = {
                SortFilterOptionsPanel(
                    state = state,
                    modifier = Modifier.fillMaxWidth()
                )
            },
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val highlightedBooths = highlightedBooths()
                val isFiltered by remember {
                    derivedStateOf {
                        merchIdIn().isNotEmpty() || seriesIdIn().isNotEmpty()
                    }
                }
                val scope = rememberCoroutineScope()
                var searchExpanded by rememberSaveable { mutableStateOf(false) }
                MapScreen(
                    viewModel = mapViewModel,
                    transformState = mapTransformState,
                    showSlider = false,
                    bottomContentPadding = it.calculateBottomPadding(),
                    modifier = Modifier.pointerInput(Unit) {
                        awaitEachGesture {
                            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                            if (event.changes.any { it.pressed }) {
                                searchExpanded = false
                                if (scaffoldState.bottomSheetState.targetValue != SheetValue.PartiallyExpanded) {
                                    scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                                }
                            }
                        }
                    }
                ) { table ->
                    HighlightedTableCell(
                        mapViewModel = mapViewModel,
                        table = table,
                        highlight = if (isFiltered) {
                            table.booth in highlightedBooths
                        } else {
                            table.favorite
                        },
                        showImages = {
                            mapTransformState.showImages &&
                                    (!isFiltered || table.booth in highlightedBooths)
                        },
                        showText = { mapTransformState.showText },
                        showCatalogHighlight = !isFiltered,
                        onArtistClick = onArtistClick,
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(600.dp)
                        .align(Alignment.TopCenter)
                ) {
                    DockedSearchBar(
                        inputField = {
                            SearchBarDefaults.InputField(
                                state = query,
                                onSearch = {
                                    val (merchResults, seriesResults) = tagResults()
                                    if (merchResults.isNotEmpty()) {
                                        onMerchSelected(merchResults.first(), true)
                                        query.clearText()
                                    } else if (seriesResults.isNotEmpty()) {
                                        onSeriesSelected(seriesResults.first(), true)
                                        query.clearText()
                                    }
                                },
                                expanded = searchExpanded,
                                onExpandedChange = { searchExpanded = it },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = stringResource(Res.string.alley_map_search),
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        searchExpanded = false
                                        query.clearText()
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = stringResource(Res.string.alley_map_search_close),
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        expanded = searchExpanded,
                        onExpandedChange = { searchExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val (merchResults, seriesResults) = tagResults()
                        val listState = rememberLazyListState()
                        LaunchedEffect(merchResults, seriesResults) {
                            listState.scrollToItem(0, 0)
                        }
                        LazyColumn(state = listState) {
                            items(
                                items = merchResults,
                                key = { "merch_${it.name}" },
                                contentType = { "merch" },
                            ) { merch ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onMerchSelected(merch, true)
                                            searchExpanded = false
                                            query.clearText()
                                        }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(text = merch.name)
                                    val notes = merch.notes
                                    if (!notes.isNullOrBlank()) {
                                        Text(
                                            text = notes,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(start = 24.dp)
                                        )
                                    }
                                }
                                HorizontalDivider()
                            }
                            items(
                                items = seriesResults,
                                key = { "series_${it.id}" },
                                contentType = { "series" },
                            ) { series ->
                                SeriesPrediction(
                                    query = query.text.toString(),
                                    series = series,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSeriesSelected(series, true)
                                            searchExpanded = false
                                            query.clearText()
                                        }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                                HorizontalDivider()
                            }
                        }
                    }

                    val seriesIdIn = seriesIdIn()
                    val merchIdIn = merchIdIn()
                    val seriesById = seriesById()
                    val merchById = merchById()
                    val languageOptionMedia = LocalLanguageOptionMedia.current
                    if (merchIdIn.isNotEmpty() || seriesIdIn.isNotEmpty()) {
                        LazyRow(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            item(key = "clear") {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = stringResource(Res.string.alley_map_search_tags_clear),
                                    modifier = Modifier.clickable(onClick = onClearTags)
                                )
                            }
                            items(
                                items = merchIdIn,
                                key = { "merch$it" },
                                contentType = { "merch" }) {
                                val merch = merchById[it]
                                FilterChip(
                                    selected = true,
                                    label = { Text(merch?.name.orEmpty()) },
                                    onClick = { merch?.let { onMerchSelected(it, false) } },
                                )
                            }
                            items(
                                items = seriesIdIn,
                                key = { "series$it" },
                                contentType = { "series" }) {
                                val series = seriesById[it]
                                FilterChip(
                                    selected = true,
                                    label = { Text(series?.name(languageOptionMedia).orEmpty()) },
                                    onClick = { series?.let { onSeriesSelected(it, false) } },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
