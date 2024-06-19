package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thekeeperofpie.artistalleydatabase.alley.R
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryGridModel
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortAndFilterComposables.SortSection
import com.thekeeperofpie.artistalleydatabase.compose.sharedBounds
import com.thekeeperofpie.artistalleydatabase.compose.sharedElement

@OptIn(ExperimentalMaterial3Api::class)
object StampRallySearchScreen {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    operator fun invoke(
        onEntryClick: (StampRallyEntryGridModel, Int) -> Unit,
    ) {
        val viewModel = hiltViewModel<StampRallySearchViewModel>()
        val listState = rememberLazyListState()
        var seen by remember { mutableStateOf(false) }
        LaunchedEffect(viewModel.query, viewModel.sortOptions, viewModel.sortAscending) {
            if (seen) {
                listState.animateScrollToItem(0, 0)
            } else {
                seen = true
            }
        }

        SearchScreen(
            viewModel = viewModel,
            entriesSize = { viewModel.entriesSize },
            bottomSheet = {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .heightIn(min = 320.dp)
                ) {
                    HorizontalDivider()
                    var sortExpanded by remember { mutableStateOf(false) }
                    SortSection(
                        headerTextRes = R.string.alley_sort_label,
                        expanded = { sortExpanded },
                        onExpandedChange = { sortExpanded = it },
                        sortOptions = { viewModel.sortOptions },
                        onSortClick = viewModel::onSortClick,
                        sortAscending = { viewModel.sortAscending },
                        onSortAscendingChange = {
                            viewModel.onSortAscendingToggle(!viewModel.sortAscending)
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
                            checked = viewModel.showGridByDefault,
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
            showGridByDefault = viewModel::showGridByDefault,
            displayType = viewModel::displayType,
            onDisplayTypeToggle = viewModel::onDisplayTypeToggle,
            listState = listState,
            onFavoriteToggle = viewModel::onFavoriteToggle,
            onIgnoredToggle = viewModel::onIgnoredToggle,
            onEntryClick = onEntryClick,
            shouldShowCount = {
                viewModel.query.isNotEmpty()
                        || viewModel.showOnlyFavorites
                        || viewModel.showOnlyWithCatalog
            },
            itemRow = { entry, onFavoriteToggle, modifier ->
                StampRallyListRow(entry, onFavoriteToggle, modifier)
            }
        )
    }

    @Composable
    private fun StampRallyListRow(
        entry: StampRallyEntryGridModel,
        onFavoriteToggle: (Boolean) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val stampRally = entry.value
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier
                .fillMaxWidth()
                .sharedBounds("container", stampRally.id, zIndexInOverlay = 1f)
        ) {
            Text(
                text = stampRally.hostTable,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .sharedBounds("hostTable", stampRally.id, zIndexInOverlay = 1f)
                    .padding(start = 16.dp, top = 12.dp, bottom = 12.dp)
            )

            Text(
                text = stampRally.fandom,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .weight(1f)
                    .sharedBounds("fandom", stampRally.id, zIndexInOverlay = 1f)
                    .padding(vertical = 12.dp)
            )

            val favorite = entry.favorite
            IconButton(onClick = { onFavoriteToggle(!favorite) }) {
                Icon(
                    imageVector = if (favorite) {
                        Icons.Filled.Favorite
                    } else {
                        Icons.Filled.FavoriteBorder
                    },
                    contentDescription = stringResource(
                        R.string.alley_stamp_rally_favorite_icon_content_description
                    ),
                    modifier = Modifier.sharedElement("favorite", stampRally.id, zIndexInOverlay = 1f)
                )
            }
        }
    }
}
