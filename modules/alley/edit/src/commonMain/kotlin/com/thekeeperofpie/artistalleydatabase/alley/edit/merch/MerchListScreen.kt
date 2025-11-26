package com.thekeeperofpie.artistalleydatabase.alley.edit.merch

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_merch_action_add
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_merch_action_refresh_content_description
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_merch_header_canonical
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_merch_header_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_merch_header_uuid
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.ui.TwoWayGrid
import com.thekeeperofpie.artistalleydatabase.utils_compose.StaticSearchBar
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

object MerchListScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyEditGraph,
        onClickEditMerch: (MerchInfo) -> Unit,
        onClickAddMerch: () -> Unit,
        viewModel: MerchListViewModel = viewModel {
            graph.merchListViewModelFactory.create(createSavedStateHandle())
        },
    ) {
        MerchListScreen(
            query = viewModel.query,
            merch = viewModel.merch.collectAsLazyPagingItems(),
            onRefresh = viewModel::refresh,
            onClickEditMerch = onClickEditMerch,
            onClickAddMerch = onClickAddMerch,
        )
    }

    @Composable
    operator fun invoke(
        query: TextFieldState,
        merch: LazyPagingItems<MerchInfo>,
        onRefresh: () -> Unit,
        onClickEditMerch: (MerchInfo) -> Unit,
        onClickAddMerch: () -> Unit,
    ) {
        Scaffold(
            topBar = {
                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier.fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    StaticSearchBar(
                        query = query,
                        modifier = Modifier.widthIn(max = 1200.dp),
                        trailingIcon = {
                            IconButton(onClick = onRefresh) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = stringResource(Res.string.alley_edit_merch_action_refresh_content_description)
                                )
                            }
                        },
                    )
                }
            },
            floatingActionButton = {
                LargeFloatingActionButton(onClick = onClickAddMerch) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(Res.string.alley_edit_merch_action_add),
                    )
                }
            },
        ) { scaffoldPadding ->
            TwoWayGrid(
                header = {},
                rows = merch,
                unfilteredCount = { merch.itemCount },
                columns = Column.entries,
                columnHeader = {
                    Text(
                        text = stringResource(it.text),
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        autoSize = TextAutoSize.StepBased(maxFontSize = LocalTextStyle.current.fontSize),
                        modifier = Modifier
                            .width(it.size)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                },
                tableCell = { row, column ->
                    val modifier = Modifier
                        .width(column.size)
                        .clickable {
                            if (row != null) {
                                onClickEditMerch(row)
                            }
                        }
                    when (column) {
                        Column.MERCH -> FieldText(value = row?.name, modifier = modifier)
                        Column.NOTES -> FieldText(value = row?.notes, modifier = modifier)
                        Column.UUID -> FieldText(value = row?.uuid, modifier = modifier)
                    }
                },
                modifier = Modifier.fillMaxSize()
                    .padding(scaffoldPadding)
                    .pullToRefresh(
                        isRefreshing = false,
                        state = rememberPullToRefreshState(),
                        onRefresh = onRefresh,
                    )
            )
        }
    }

    @Composable
    private fun FieldText(value: Any?, modifier: Modifier = Modifier) {
        Text(
            text = value?.toString().orEmpty(),
            modifier = modifier
                .fillMaxHeight()
                .padding(16.dp)
        )
    }

    private enum class Column(
        override val text: StringResource,
        override val size: Dp = 200.dp,
    ) : TwoWayGrid.Column {
        MERCH(text = Res.string.alley_edit_merch_header_canonical),
        NOTES(text = Res.string.alley_edit_merch_header_notes, size = 450.dp),
        UUID(text = Res.string.alley_edit_merch_header_uuid, size = 400.dp),
    }
}
