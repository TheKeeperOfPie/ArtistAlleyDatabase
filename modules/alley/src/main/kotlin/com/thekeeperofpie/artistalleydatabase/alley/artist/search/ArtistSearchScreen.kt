package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thekeeperofpie.artistalleydatabase.alley.LocalStableRandomSeed
import com.thekeeperofpie.artistalleydatabase.alley.R
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.ui.ArtistListRow
import com.thekeeperofpie.artistalleydatabase.compose.ScrollStateSaver
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortAndFilterComposables.SortSection
import com.thekeeperofpie.artistalleydatabase.compose.filter.selectedOption
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
object ArtistSearchScreen {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    operator fun invoke(
        onClickBack: (() -> Unit)?,
        onEntryClick: (ArtistEntryGridModel, Int) -> Unit,
        scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(
            rememberStandardBottomSheetState(
                confirmValueChange = { it != SheetValue.Hidden },
                skipHiddenState = true,
            )
        ),
        scrollStateSaver: ScrollStateSaver,
        onClickMap: (() -> Unit)? = null,
    ) {
        val viewModel = hiltViewModel<ArtistSearchViewModel>()
        val listState = scrollStateSaver.lazyListState()
        var seen by remember { mutableStateOf(false) }
        LaunchedEffect(
            viewModel.query,
            viewModel.sortOptions.selectedOption(ArtistSearchSortOption.RANDOM),
            viewModel.sortAscending.collectAsState().value,
            viewModel.showOnlyFavorites,
            viewModel.showOnlyWithCatalog,
            viewModel.showOnlyConfirmedTags.collectAsState().value,
        ) {
            if (seen) {
                delay(500.milliseconds)
                listState.scrollToItem(0, 0)
            } else {
                seen = true
            }
        }

        CompositionLocalProvider(LocalStableRandomSeed provides viewModel.randomSeed) {
            val showGridByDefault by viewModel.showGridByDefault.collectAsState()
            val showRandomCatalogImage by viewModel.showRandomCatalogImage.collectAsState()
            val displayType = SearchScreen.DisplayType.fromSerializedValue(
                viewModel.displayType.collectAsState().value
            )
            SearchScreen(
                viewModel = viewModel,
                title = { viewModel.lockedSeries ?: viewModel.lockedMerch },
                actions = if (onClickMap == null) { null } else {
                    {
                        IconButton(onClick = onClickMap) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = stringResource(R.string.alley_open_in_map),
                            )
                        }
                    }
                },
                onClickBack = onClickBack,
                entriesSize = { viewModel.entriesSize },
                scaffoldState = scaffoldState,
                bottomSheet = {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .heightIn(min = 320.dp)
                    ) {
                        HorizontalDivider()
                        var sortExpanded by remember { mutableStateOf(false) }
                        val sortAscending by viewModel.sortAscending.collectAsState()
                        SortSection(
                            headerTextRes = R.string.alley_sort_label,
                            expanded = { sortExpanded },
                            onExpandedChange = { sortExpanded = it },
                            sortOptions = { viewModel.sortOptions },
                            onSortClick = viewModel::onSortClick,
                            sortAscending = { sortAscending },
                            onSortAscendingChange = {
                                viewModel.onSortAscendingToggle(!sortAscending)
                            }
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.alley_filter_favorites),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                                    .weight(1f)
                            )

                            Switch(
                                checked = viewModel.showOnlyFavorites,
                                onCheckedChange = { viewModel.showOnlyFavorites = it },
                                modifier = Modifier.padding(end = 16.dp),
                            )
                        }

                        HorizontalDivider()

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.alley_filter_only_catalogs),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                                    .weight(1f)
                            )

                            Switch(
                                checked = viewModel.showOnlyWithCatalog,
                                onCheckedChange = { viewModel.showOnlyWithCatalog = it },
                                modifier = Modifier.padding(end = 16.dp),
                            )
                        }

                        HorizontalDivider()

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.alley_filter_show_grid_by_default),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                                    .weight(1f)
                            )

                            Switch(
                                checked = showGridByDefault,
                                onCheckedChange = viewModel::onShowGridByDefaultToggle,
                                modifier = Modifier.padding(end = 16.dp),
                            )
                        }

                        HorizontalDivider()

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.alley_filter_show_random_catalog_image),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                                    .weight(1f)
                            )

                            Switch(
                                checked = showRandomCatalogImage,
                                onCheckedChange = viewModel::onShowRandomCatalogImageToggle,
                                modifier = Modifier.padding(end = 16.dp),
                            )
                        }

                        HorizontalDivider()

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.alley_filter_show_only_confirmed_tags),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                                    .weight(1f)
                            )

                            val showOnlyConfirmedTags by viewModel.showOnlyConfirmedTags.collectAsState()
                            Switch(
                                checked = showOnlyConfirmedTags,
                                onCheckedChange = viewModel::onShowOnlyConfirmedTagsToggle,
                                modifier = Modifier.padding(end = 16.dp),
                            )
                        }

                        HorizontalDivider()

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.alley_filter_show_ignored),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                                    .weight(1f)
                            )

                            Switch(
                                checked = viewModel.showIgnored,
                                onCheckedChange = { viewModel.showIgnored = it },
                                modifier = Modifier.padding(end = 16.dp),
                            )
                        }

                        HorizontalDivider()

                        Spacer(Modifier.height(80.dp))
                    }
                },
                showGridByDefault = { showGridByDefault },
                showRandomCatalogImage = { showRandomCatalogImage },
                displayType = { displayType },
                onDisplayTypeToggle = viewModel::onDisplayTypeToggle,
                listState = listState,
                onFavoriteToggle = viewModel::onFavoriteToggle,
                onIgnoredToggle = viewModel::onIgnoredToggle,
                onEntryClick = onEntryClick,
                shouldShowCount = {
                    viewModel.query.isNotEmpty()
                            || viewModel.showOnlyFavorites
                            || viewModel.showOnlyWithCatalog
                            || viewModel.lockedSeries != null
                            || viewModel.lockedMerch != null
                },
                itemToSharedElementId = { it.value.id },
                itemRow = { entry, onFavoriteToggle, modifier ->
                    ArtistListRow(entry, onFavoriteToggle, modifier)
                }
            )
        }
    }
}
