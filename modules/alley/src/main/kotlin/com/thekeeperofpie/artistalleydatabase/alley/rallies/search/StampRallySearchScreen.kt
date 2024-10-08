package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thekeeperofpie.artistalleydatabase.alley.LocalStableRandomSeed
import com.thekeeperofpie.artistalleydatabase.alley.R
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.rallies.prizeLimitText
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedBounds
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterOptionsPanel
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
object StampRallySearchScreen {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    operator fun invoke(
        onEntryClick: (StampRallyEntryGridModel, Int) -> Unit,
        scrollStateSaver: ScrollStateSaver,
    ) {
        val viewModel = hiltViewModel<StampRallySearchViewModel>()
        val gridState = scrollStateSaver.lazyStaggeredGridState()
        val sortFilterController = viewModel.sortFilterController
        sortFilterController.ImmediateScrollResetEffect(gridState)

        CompositionLocalProvider(LocalStableRandomSeed provides viewModel.randomSeed) {
            val showGridByDefault by sortFilterController.gridByDefaultSection.property.collectAsState()
            val showRandomCatalogImage by sortFilterController.randomCatalogImageSection.property.collectAsState()
            val forceOneDisplayColumn by sortFilterController.forceOneDisplayColumnSection.property.collectAsState()
            val showOnlyFavorites by sortFilterController.onlyFavoritesSection.property.collectAsState()
            val displayType = SearchScreen.DisplayType.fromSerializedValue(
                viewModel.displayType.collectAsState().value
            )
            val entries = viewModel.results.collectAsLazyPagingItems()
            SearchScreen(
                viewModel = viewModel,
                entries = entries,
                bottomSheet = {
                    SortFilterOptionsPanel(
                        sections = { sortFilterController.sections },
                        sectionState = { sortFilterController.state },
                        showClear = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 320.dp)
                    )
                },
                showGridByDefault = { showGridByDefault },
                showRandomCatalogImage = { showRandomCatalogImage },
                forceOneDisplayColumn = { forceOneDisplayColumn },
                displayType = { displayType },
                onDisplayTypeToggle = viewModel::onDisplayTypeToggle,
                gridState = gridState,
                onFavoriteToggle = viewModel::onFavoriteToggle,
                onIgnoredToggle = viewModel::onIgnoredToggle,
                onEntryClick = onEntryClick,
                shouldShowCount = { viewModel.query.isNotEmpty() || showOnlyFavorites },
                itemToSharedElementId = { it.value.id },
                itemRow = { entry, onFavoriteToggle, modifier ->
                    StampRallyListRow(entry, onFavoriteToggle, modifier)
                }
            )
        }
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
                .padding(start = 16.dp)
        ) {
            Text(
                text = stampRally.hostTable,
                style = MaterialTheme.typography.titleLarge
                    .copy(fontFamily = FontFamily.Monospace),
                modifier = Modifier
                    .sharedElement("hostTable", stampRally.id, zIndexInOverlay = 1f)
                    .padding(vertical = 8.dp)
            )

            Text(
                text = stampRally.fandom,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .sharedElement("fandom", stampRally.id, zIndexInOverlay = 1f)
                    .weight(1f)
                    .padding(vertical = 8.dp)
            )

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                if (stampRally.prizeLimit != null) {
                    Text(
                        text = stringResource(
                            R.string.alley_stamp_rally_prize_limit,
                            stampRally.prizeLimitText(),
                        ),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                val totalCost = stampRally.totalCost
                if (totalCost != null) {
                    Text(
                        text = if (totalCost == 0) {
                            stringResource(R.string.alley_stamp_rally_total_free)
                        } else {
                            stringResource(
                                R.string.alley_stamp_rally_total_paid,
                                totalCost,
                            )
                        },
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }

            val favorite = entry.favorite
            IconButton(
                onClick = { onFavoriteToggle(!favorite) },
                modifier = Modifier
                    .sharedElement("favorite", stampRally.id, zIndexInOverlay = 1f)
                    .align(Alignment.Top)
            ) {
                Icon(
                    imageVector = if (favorite) {
                        Icons.Filled.Favorite
                    } else {
                        Icons.Filled.FavoriteBorder
                    },
                    contentDescription = stringResource(
                        R.string.alley_stamp_rally_favorite_icon_content_description
                    ),
                )
            }
        }
    }
}
