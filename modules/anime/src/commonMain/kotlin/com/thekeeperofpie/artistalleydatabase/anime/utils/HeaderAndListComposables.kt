@file:OptIn(ExperimentalMaterial3Api::class)

package com.thekeeperofpie.artistalleydatabase.anime.utils

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.utils_compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterController
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import org.jetbrains.compose.resources.StringResource

@Composable
fun <ListEntryType : Any> HeaderAndListScreen(
    viewModel: HeaderAndListViewModel<*, *, ListEntryType, *, *>,
    headerTextRes: StringResource?,
    header: @Composable BoxScope.(progress: Float) -> Unit,
    itemKey: (ListEntryType) -> Any,
    item: @Composable LazyGridItemScope.(ListEntryType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        snapAnimationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    // TODO: Show root entry error
    val sortFilterController = viewModel.sortFilterController
    SortFilterBottomScaffold(
        topBar = {
            CollapsingToolbar(
                maxHeight = 356.dp,
                pinnedHeight = 120.dp,
                scrollBehavior = scrollBehavior,
                content = header,
            )
        },
        sortFilterController = sortFilterController,
        modifier = modifier,
    ) {
        List(
            viewModel = viewModel,
            scrollBehavior = scrollBehavior,
            headerTextRes = headerTextRes,
            scaffoldPadding = it,
            itemKey = itemKey,
            item = item,
            sortFilterController = sortFilterController,
        )
    }
}

@Composable
fun <ListEntryType : Any> HeaderAndMediaListScreen(
    viewModel: HeaderAndListViewModel<*, *, ListEntryType, *, *>,
    editViewModel: MediaEditViewModel,
    headerTextRes: StringResource?,
    header: @Composable() (BoxScope.(progress: Float) -> Unit),
    itemKey: (ListEntryType) -> Any,
    item: @Composable() (LazyGridItemScope.(ListEntryType?) -> Unit),
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        snapAnimationSpec = spring(stiffness = Spring.StiffnessMedium)
    )
    val snackbarHostState = remember { SnackbarHostState() }

    val error = viewModel.entry.error
    val errorString = error?.message()
    LaunchedEffect(errorString) {
        if (errorString != null) {
            snackbarHostState.showSnackbar(
                message = errorString,
                withDismissAction = true,
                duration = SnackbarDuration.Long,
            )
        }
    }

    val sortFilterController = viewModel.sortFilterController
    MediaEditBottomSheetScaffold(
        viewModel = editViewModel,
        snackbarHostState = snackbarHostState,
    ) {
        SortFilterBottomScaffold(
            topBar = {
                CollapsingToolbar(
                    maxHeight = 356.dp,
                    pinnedHeight = 120.dp,
                    scrollBehavior = scrollBehavior,
                    content = header,
                )
            },
            sortFilterController = sortFilterController,
        ) {
            List(
                viewModel = viewModel,
                scrollBehavior = scrollBehavior,
                headerTextRes = headerTextRes,
                scaffoldPadding = it,
                itemKey = itemKey,
                item = item,
                sortFilterController = sortFilterController,
            )
        }
    }
}

@Composable
private fun <ListEntryType : Any> List(
    viewModel: HeaderAndListViewModel<*, *, ListEntryType, *, *>,
    scrollBehavior: TopAppBarScrollBehavior,
    headerTextRes: StringResource?,
    scaffoldPadding: PaddingValues,
    itemKey: (ListEntryType) -> Any,
    item: @Composable LazyGridItemScope.(ListEntryType?) -> Unit,
    sortFilterController: SortFilterController<*>,
) {
    val gridState = rememberLazyGridState()
    val items = viewModel.items.collectAsLazyPagingItems()
    sortFilterController.ImmediateScrollResetEffect(gridState)
    VerticalList(
        itemHeaderText = headerTextRes,
        items = items,
        itemKey = itemKey,
        gridState = gridState,
        onRefresh = items::refresh,
        contentPadding = PaddingValues(bottom = 32.dp),
        nestedScrollConnection = scrollBehavior.nestedScrollConnection,
        modifier = Modifier.padding(scaffoldPadding)
    ) {
        item(it)
    }
}
