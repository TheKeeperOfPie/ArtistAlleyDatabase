package com.thekeeperofpie.artistalleydatabase.alley.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import org.jetbrains.compose.resources.StringResource
import kotlin.enums.EnumEntries

@OptIn(ExperimentalFoundationApi::class)
object TwoWayGrid {

    val modifierDefaultCellPadding = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)

    @Composable
    operator fun <T, ColumnType> invoke(
        header: LazyListScope.() -> Unit,
        rows: LazyPagingItems<T>,
        columns: EnumEntries<ColumnType>,
        columnHeader: @Composable (column: ColumnType) -> Unit,
        tableCell: @Composable (row: T?, column: ColumnType) -> Unit,
        noResultsHeader: @Composable (() -> Unit)? = null,
        listState: LazyListState = rememberLazyListState(),
        horizontalScrollState: ScrollState = rememberScrollState(),
        topOffset: Dp = 0.dp,
        contentPadding: PaddingValues = PaddingValues(0.dp),
        modifier: Modifier = Modifier,
    ) where T : Any, ColumnType : Enum<ColumnType>, ColumnType : Column {
        val width = remember(columns) {
            val dividerCount = columns.size - 1
            columns.fold(0.dp) { width, column -> width + column.size } +
                    (DividerDefaults.Thickness * dividerCount)
        }

        CompositionLocalProvider(LocalOverscrollFactory provides null) {
            LazyColumn(
                state = listState,
                contentPadding = contentPadding,
                modifier = modifier.width(width)
            ) {
                header()

                stickyHeader("tableHeaders") {
                    Row(
                        Modifier.height(IntrinsicSize.Min)
                            .padding(top = topOffset)
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(16.dp))
                    ) {
                        columns.firstOrNull()?.let { columnHeader(it) }
                        VerticalDivider()
                        Row(Modifier.weight(1f).horizontalScroll(horizontalScrollState)) {
                            columns.drop(1).forEachIndexed { columnIndex, column ->
                                columnHeader(column)
                                if (columnIndex != columns.lastIndex - 1) {
                                    VerticalDivider()
                                }
                            }
                        }
                    }
                    HorizontalDivider()
                }

                if (rows.itemCount == 0 && noResultsHeader != null) {
                    item("tableNoResultsHeader") {
                        noResultsHeader()
                    }
                }

                items(rows.itemCount) { index ->
                    Row(Modifier.height(IntrinsicSize.Min)) {
                        columns.firstOrNull()?.let {
                            Box(Modifier.requiredWidth(it.size)) {
                                tableCell(rows[index], it)
                            }
                        }
                        VerticalDivider()
                        Row(Modifier.weight(1f).horizontalScroll(horizontalScrollState)) {
                            columns.drop(1).forEachIndexed { columnIndex, column ->
                                Box(Modifier.requiredWidth(column.size)) {
                                    tableCell(rows[index], column)
                                }
                                if (columnIndex != columns.lastIndex - 1) {
                                    VerticalDivider()
                                }
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
