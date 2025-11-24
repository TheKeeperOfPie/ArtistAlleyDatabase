package com.thekeeperofpie.artistalleydatabase.alley.edit.series

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_action_add
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_action_edit_content_description
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_aniList_id
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_aniList_type
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_canonical
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_edit
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_external_link
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_image
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_source_type
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_title_english
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_title_native
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_title_romaji
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_uuid
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_series_header_wikipedia_id
import coil3.compose.AsyncImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.series.textRes
import com.thekeeperofpie.artistalleydatabase.alley.ui.TwoWayGrid
import com.thekeeperofpie.artistalleydatabase.utils_compose.StaticSearchBar
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

object SeriesListScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyEditGraph,
        onClickEditSeries: (SeriesInfo) -> Unit,
        onClickAddSeries: () -> Unit,
        viewModel: SeriesListViewModel = viewModel { graph.seriesListViewModel() },
    ) {
        SeriesListScreen(
            series = viewModel.series.collectAsLazyPagingItems(),
            loadImage = viewModel::loadImage,
            onClickEditSeries = onClickEditSeries,
            onClickAddSeries = onClickAddSeries,
        )
    }

    @Composable
    private operator fun invoke(
        series: LazyPagingItems<SeriesInfo>,
        loadImage: (SeriesInfo) -> String?,
        onClickEditSeries: (SeriesInfo) -> Unit,
        onClickAddSeries: () -> Unit,
    ) {
        Scaffold(
            topBar = {
                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier.fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    val query = rememberTextFieldState()
                    StaticSearchBar(query = query, modifier = Modifier.widthIn(max = 1200.dp))
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
                columns = Column.entries,
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
                    when (column) {
                        Column.EDIT -> {
                            Box(
                                contentAlignment = Alignment.TopCenter,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                IconButton(onClick = {
                                    if (row != null) {
                                        onClickEditSeries(row)
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = stringResource(Res.string.alley_edit_series_action_edit_content_description)
                                    )
                                }
                            }
                        }
                        Column.IMAGE -> {
                            AsyncImage(
                                model = row?.let { loadImage(it) },
                                contentDescription = null,
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Column.CANONICAL -> FieldText(row?.id)
                        Column.NOTES -> FieldText(row?.notes)
                        Column.ANILIST_ID -> FieldText(row?.aniListId)
                        Column.ANILIST_TYPE -> FieldText(row?.aniListType)
                        Column.SOURCE_TYPE -> FieldText(
                            row?.source?.textRes?.let { stringResource(it) }
                        )
                        Column.TITLE_ENGLISH -> FieldText(row?.titleEnglish)
                        Column.TITLE_ROMAJI -> FieldText(row?.titleRomaji)
                        Column.TITLE_NATIVE -> FieldText(row?.titleNative)
                        Column.WIKIPEDIA_ID -> FieldText(row?.wikipediaId)
                        Column.EXTERNAL_LINK -> {
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
                        Column.UUID -> FieldText(row?.uuid)
                    }
                },
                modifier = Modifier.fillMaxSize()
                    .padding(scaffoldPadding)
            )
        }
    }

    @Composable
    private fun FieldText(value: Any?) {
        Text(
            text = value?.toString().orEmpty(),
            modifier = Modifier.padding(16.dp)
        )
    }

    private enum class Column(
        override val text: StringResource,
        override val size: Dp = 160.dp,
    ) : TwoWayGrid.Column {
        EDIT(size = 48.dp, text = Res.string.alley_edit_series_header_edit),
        IMAGE(size = 64.dp, text = Res.string.alley_edit_series_header_image),
        CANONICAL(text = Res.string.alley_edit_series_header_canonical),
        NOTES(text = Res.string.alley_edit_series_header_notes),
        ANILIST_ID(text = Res.string.alley_edit_series_header_aniList_id),
        ANILIST_TYPE(text = Res.string.alley_edit_series_header_aniList_type),
        SOURCE_TYPE(text = Res.string.alley_edit_series_header_source_type),
        TITLE_ENGLISH(text = Res.string.alley_edit_series_header_title_english),
        TITLE_ROMAJI(text = Res.string.alley_edit_series_header_title_romaji),
        TITLE_NATIVE(text = Res.string.alley_edit_series_header_title_native),
        WIKIPEDIA_ID(text = Res.string.alley_edit_series_header_wikipedia_id),
        EXTERNAL_LINK(text = Res.string.alley_edit_series_header_external_link),
        UUID(text = Res.string.alley_edit_series_header_uuid),
    }
}
