package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.alley.LocalStableRandomSeed
import com.thekeeperofpie.artistalleydatabase.alley.R
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistListRow
import com.thekeeperofpie.artistalleydatabase.compose.ScrollStateSaver
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortFilterOptionsPanel

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
        val gridState = scrollStateSaver.lazyStaggeredGridState()
        val sortFilterController = viewModel.sortFilterController
        sortFilterController.ImmediateScrollResetEffect(gridState)

        CompositionLocalProvider(LocalStableRandomSeed provides viewModel.randomSeed) {
            val showGridByDefault by sortFilterController.gridByDefaultSection.property.collectAsState()
            val showRandomCatalogImage by sortFilterController.randomCatalogImageSection.property.collectAsState()
            val forceOneDisplayColumn by sortFilterController.forceOneDisplayColumnSection.property.collectAsState()
            val displayType = SearchScreen.DisplayType.fromSerializedValue(
                viewModel.displayType.collectAsState().value
            )
            val entries = viewModel.results.collectAsLazyPagingItems()
            SearchScreen(
                viewModel = viewModel,
                title = { viewModel.lockedSeries ?: viewModel.lockedMerch },
                actions = if (onClickMap == null) {
                    null
                } else {
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
                entries = entries,
                scaffoldState = scaffoldState,
                bottomSheet = {
                    SortFilterOptionsPanel(
                        sections = { sortFilterController.sections },
                        sectionState = { sortFilterController.state },
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
                shouldShowCount = {
                    viewModel.query.isNotEmpty()
                            || sortFilterController.onlyFavoritesSection.enabled
                            || sortFilterController.onlyCatalogImagesSection.enabled
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
