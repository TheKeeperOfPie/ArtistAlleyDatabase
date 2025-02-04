package com.thekeeperofpie.artistalleydatabase.alley.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoSizeText
import com.thekeeperofpie.artistalleydatabase.utils_compose.VerticalDivider
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.enums.EnumEntries

@OptIn(ExperimentalFoundationApi::class)
object TwoWayGrid {

    val modifierDefaultCellPadding = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)

    @Composable
    operator fun <T, ColumnType> invoke(
        rows: LazyPagingItems<T>,
        columns: EnumEntries<ColumnType>,
        tableCell: @Composable (row: T?, column: ColumnType) -> Unit,
        listState: LazyListState = rememberLazyListState(),
        topOffset: Dp = 0.dp,
        contentPadding: PaddingValues = PaddingValues(0.dp),
        modifier: Modifier = Modifier,
    ) where T : Any, ColumnType : Enum<ColumnType>, ColumnType : Column {
        val horizontalScrollState = rememberScrollState()
        val width = remember(columns) {
            val dividerCount = columns.size - 1
            columns.fold(0.dp) { width, column -> width + column.size } +
                    (DividerDefaults.Thickness * dividerCount)
        }
        LazyColumn(
            state = listState,
            contentPadding = contentPadding,
            modifier = modifier.width(width)
        ) {
            stickyHeader {
                Column(Modifier.padding(top = topOffset)) {
                    Row(
                        Modifier.height(IntrinsicSize.Min)
                            .horizontalScroll(horizontalScrollState)
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(16.dp))
                    ) {
                        columns.forEachIndexed { columnIndex, column ->
                            AutoSizeText(
                                text = stringResource(column.text),
                                modifier = Modifier.requiredWidth(column.size)
                                    .then(modifierDefaultCellPadding)
                            )
                            if (columnIndex != columns.lastIndex) {
                                VerticalDivider()
                            }
                        }
                    }
                    HorizontalDivider()
                }
            }
            items(rows.itemCount) { index ->
                Column {
                    Row(
                        Modifier.height(IntrinsicSize.Min)
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        columns.forEachIndexed { columnIndex, column ->
                            Box(Modifier.requiredWidth(column.size)) {
                                tableCell(rows[index], column)
                            }
                            if (columnIndex != columns.lastIndex) {
                                VerticalDivider()
                            }
                        }
                    }

                    if (index != rows.itemCount - 1) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    interface Column {
        val size: Dp
        val text: StringResource
    }
}
