package com.thekeeperofpie.artistalleydatabase.alley.edit.series

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_action_add
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_action_refresh_content_description
import coil3.compose.AsyncImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.series.textRes
import com.thekeeperofpie.artistalleydatabase.alley.ui.TwoWayGrid
import com.thekeeperofpie.artistalleydatabase.utils_compose.StaticSearchBar
import org.jetbrains.compose.resources.stringResource

internal object SeriesListScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyEditGraph,
        onClickEditSeries: (SeriesInfo, SeriesColumn) -> Unit,
        onClickAddSeries: () -> Unit,
        viewModel: SeriesListViewModel = viewModel {
            graph.seriesListViewModelFactory.create(createSavedStateHandle())
        },
    ) {
        SeriesListScreen(
            query = viewModel.query,
            series = viewModel.series.collectAsLazyPagingItems(),
            loadImage = viewModel::loadImage,
            onRefresh = viewModel::refresh,
            onClickEditSeries = onClickEditSeries,
            onClickAddSeries = onClickAddSeries,
        )
    }

    @Composable
    private operator fun invoke(
        query: TextFieldState,
        series: LazyPagingItems<SeriesInfo>,
        loadImage: (SeriesInfo) -> String?,
        onRefresh: () -> Unit,
        onClickEditSeries: (SeriesInfo, SeriesColumn) -> Unit,
        onClickAddSeries: () -> Unit,
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
                                    contentDescription = stringResource(Res.string.alley_edit_series_action_refresh_content_description)
                                )
                            }
                        },
                    )
                }
            },
            floatingActionButton = {
                LargeFloatingActionButton(onClick = onClickAddSeries) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(Res.string.alley_edit_series_action_add),
                    )
                }
            },
        ) { scaffoldPadding ->
            TwoWayGrid(
                header = {},
                rows = series,
                unfilteredCount = { series.itemCount },
                columns = SeriesColumn.entries,
                columnHeader = {
                    val text = stringResource(it.text)
                    Text(
                        text = text,
                        maxLines = if (text.contains(Regex("\\s"))) 2 else 1,
                        textAlign = TextAlign.Center,
                        autoSize = TextAutoSize.StepBased(maxFontSize = LocalTextStyle.current.fontSize),
                        modifier = Modifier
                            .width(it.size)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                },
                pinnedColumns = 3,
                tableCell = { row, column ->
                    val modifier = Modifier
                        .width(column.size)
                        .clickable {
                            if (row != null) {
                                onClickEditSeries(row, column)
                            }
                        }
                    when (column) {
                        SeriesColumn.IMAGE -> {
                            AsyncImage(
                                model = row?.let { loadImage(it) },
                                contentDescription = null,
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        SeriesColumn.CANONICAL -> FieldText(value = row?.id, modifier = modifier)
                        SeriesColumn.NOTES -> FieldText(value = row?.notes, modifier = modifier)
                        SeriesColumn.ANILIST_ID -> FieldText(
                            value = row?.aniListId,
                            modifier = modifier
                        )
                        SeriesColumn.ANILIST_TYPE -> FieldText(
                            value = row?.aniListType?.textRes?.let { stringResource(it) },
                            modifier = modifier
                        )
                        SeriesColumn.SOURCE_TYPE -> FieldText(
                            value = row?.source?.textRes?.let { stringResource(it) },
                            modifier = modifier
                        )
                        SeriesColumn.TITLE_ENGLISH -> FieldText(
                            value = row?.titleEnglish,
                            modifier = modifier
                        )
                        SeriesColumn.TITLE_ROMAJI -> FieldText(
                            value = row?.titleRomaji,
                            modifier = modifier
                        )
                        SeriesColumn.TITLE_NATIVE -> FieldText(
                            value = row?.titleNative,
                            modifier = modifier
                        )
                        SeriesColumn.TITLE_PREFERRED -> FieldText(
                            value = row?.titlePreferred,
                            modifier = modifier
                        )
                        SeriesColumn.SYNONYMS -> FieldText(
                            value = row?.synonyms?.joinToString(separator = "\n"),
                            modifier = modifier
                        )
                        SeriesColumn.WIKIPEDIA_ID -> FieldText(
                            value = row?.wikipediaId,
                            modifier = modifier
                        )
                        SeriesColumn.EXTERNAL_LINK -> {
                            val link = row?.link
                            Text(
                                text = if (link.isNullOrBlank()) {
                                    AnnotatedString("")
                                } else {
                                    buildAnnotatedString {
                                        withLink(LinkAnnotation.Url(link)) {
                                            append(link)
                                        }
                                    }
                                },
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        SeriesColumn.UUID -> FieldText(value = row?.uuid, modifier = modifier)
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
}
