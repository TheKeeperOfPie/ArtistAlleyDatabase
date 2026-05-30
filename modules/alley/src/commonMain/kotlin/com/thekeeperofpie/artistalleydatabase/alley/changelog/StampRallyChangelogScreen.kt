package com.thekeeperofpie.artistalleydatabase.alley.changelog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.visible
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_catalog_image
import artistalleydatabase.modules.alley.generated.resources.alley_changelog_title
import artistalleydatabase.modules.alley.generated.resources.alley_changelog_title_added
import artistalleydatabase.modules.alley.generated.resources.alley_changelog_title_updated
import artistalleydatabase.modules.alley.generated.resources.alley_next_page
import artistalleydatabase.modules.alley.generated.resources.alley_previous_page
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.composables.core.ScrollArea
import com.composables.core.rememberScrollAreaState
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.GetSeriesTitles
import com.thekeeperofpie.artistalleydatabase.alley.artist.MerchRow
import com.thekeeperofpie.artistalleydatabase.alley.artist.SeriesRow
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagUtils
import com.thekeeperofpie.artistalleydatabase.alley.ui.PrimaryVerticalScrollbar
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberSharedContentState
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.automirrored.filled.ArrowLeft
import com.thekeeperofpie.artistalleydatabase.icons.automirrored.filled.ArrowRight
import com.thekeeperofpie.artistalleydatabase.icons.filled.ImageNotSupported
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import org.jetbrains.compose.resources.stringResource

internal object StampRallyChangelogScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyGraph,
        dataYear: DataYear,
        onClickBack: () -> Unit,
        onClickStampRally: (StampRallyChangelogEntry) -> Unit,
        onClickSeries: (String) -> Unit,
        onClickMerch: (String) -> Unit,
        onClickImage: (StampRallyChangelogEntry, CatalogImage) -> Unit,
        viewModel: StampRallyChangelogViewModel = viewModel {
            graph.stampRallyChangelogViewModelFactory.create(dataYear)
        },
    ) {
        val changes by viewModel.changes.collectAsStateWithLifecycle()
        val seriesTitles by viewModel.seriesEntryCache.series.collectAsStateWithLifecycle()
        StampRallyChangelogScreen(
            changes = { changes },
            seriesTitles = { seriesTitles },
            onClickBack = onClickBack,
            onClickStampRally = onClickStampRally,
            onClickSeries = onClickSeries,
            onClickMerch = onClickMerch,
            onClickImage = onClickImage,
        )
    }

    @Composable
    operator fun invoke(
        changes: () -> List<DayChange>,
        seriesTitles: () -> Map<String, GetSeriesTitles>,
        onClickBack: () -> Unit,
        onClickStampRally: (StampRallyChangelogEntry) -> Unit,
        onClickSeries: (String) -> Unit,
        onClickMerch: (String) -> Unit,
        onClickImage: (StampRallyChangelogEntry, CatalogImage) -> Unit,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = { ArrowBackIconButton(onClickBack) },
                    title = { Text(stringResource(Res.string.alley_changelog_title)) },
                )
            },
        ) {
            val listState = rememberLazyListState()
            val scrollAreaState = rememberScrollAreaState(listState)
            ScrollArea(state = scrollAreaState, modifier = Modifier.fillMaxSize().padding(it)) {
                Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(bottom = 200.dp),
                        modifier = Modifier.widthIn(max = 960.dp)
                    ) {
                        changes().forEach {
                            day(
                                dayChange = it,
                                seriesTitles = seriesTitles,
                                onClickStampRally = onClickStampRally,
                                onClickSeries = onClickSeries,
                                onClickMerch = onClickMerch,
                                onClickImage = onClickImage,
                            )
                        }
                    }

                    PrimaryVerticalScrollbar(listState)
                }
            }
        }
    }

    private fun LazyListScope.day(
        dayChange: DayChange,
        seriesTitles: () -> Map<String, GetSeriesTitles>,
        onClickStampRally: (StampRallyChangelogEntry) -> Unit,
        onClickSeries: (String) -> Unit,
        onClickMerch: (String) -> Unit,
        onClickImage: (StampRallyChangelogEntry, CatalogImage) -> Unit,
    ) {
        item(key = listOf("header", dayChange.date), contentType = "header") {
            Text(
                text = dayChange.date.format(LocalDate.Formats.ISO),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                )
            )
        }
        if (dayChange.added.isNotEmpty()) {
            item(key = listOf("headerAdded", dayChange.date), contentType = "headerAdded") {
                Text(
                    text = stringResource(Res.string.alley_changelog_title_added),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            itemsIndexed(
                items = dayChange.added,
                key = { _, change -> listOf(change.stampRallyId, change.date) },
                contentType = { _, _ -> "stampRallyRow" },
            ) { index, change ->
                StampRallyRow(
                    stampRally = change,
                    isLast = index == dayChange.added.lastIndex,
                    seriesTitles = seriesTitles,
                    onClick = { onClickStampRally(change) },
                    onClickSeries = onClickSeries,
                    onClickMerch = onClickMerch,
                    onClickImage = { onClickImage(change, it) },
                )
            }
        }

        if (dayChange.updated.isNotEmpty()) {
            if (dayChange.added.isNotEmpty()) {
                item(
                    key = listOf("dividerUpdated", dayChange.date),
                    contentType = "dividerUpdated"
                ) {
                    HorizontalDivider(modifier = Modifier.padding(start = 8.dp))
                }
            }
            item(key = listOf("headerUpdated", dayChange.date), contentType = "headerUpdated") {
                Text(
                    text = stringResource(Res.string.alley_changelog_title_updated),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            itemsIndexed(
                items = dayChange.updated,
                key = { _, change -> listOf(change.stampRallyId, change.date) },
                contentType = { _, _ -> "stampRallyRow" },
            ) { index, change ->
                StampRallyRow(
                    stampRally = change,
                    isLast = index == dayChange.updated.lastIndex,
                    seriesTitles = seriesTitles,
                    onClick = { onClickStampRally(change) },
                    onClickSeries = onClickSeries,
                    onClickMerch = onClickMerch,
                    onClickImage = { onClickImage(change, it) },
                )
            }
        }

        item(key = listOf("divider", dayChange.date), contentType = "divider") {
            HorizontalDivider(thickness = 2.dp)
        }
    }

    @Composable
    private fun StampRallyRow(
        stampRally: StampRallyChangelogEntry,
        isLast: Boolean,
        seriesTitles: () -> Map<String, GetSeriesTitles>,
        onClick: () -> Unit,
        onClickSeries: (String) -> Unit,
        onClickMerch: (String) -> Unit,
        onClickImage: (CatalogImage) -> Unit,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
            ) {
                Text(
                    text = stampRally.rally.fandom,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            val images = stampRally.images
            if (images.isNotEmpty()) {
                Box {
                    // TODO: Split by DataYear
                    val listState = rememberLazyListState()
                    LazyRow(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(items = images, key = { it.coilImageModel.toString() }) {
                            val sharedContentState =
                                rememberSharedContentState("image", it.coilImageModel)
                            AsyncImage(
                                model = ImageRequest.Builder(LocalPlatformContext.current)
                                    .data(it.coilImageModel)
                                    .placeholderMemoryCacheKey(it.coilImageModel.toString())
                                    .build(),
                                contentScale = ContentScale.Fit,
                                fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                                contentDescription = stringResource(Res.string.alley_artist_catalog_image),
                                modifier = Modifier
                                    .height(ChangelogUtils.ImageHeight)
                                    .widthIn(min = 48.dp)
                                    .sharedElement(state = sharedContentState)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onClickImage(it) }
                            )
                        }
                    }

                    val scrollSize = with(LocalDensity.current) { ChangelogUtils.ImageHeight.toPx() }
                    val scope = rememberCoroutineScope()

                    val previousPageInteractionSource = remember { MutableInteractionSource() }
                    IconButton(
                        onClick = {
                            scope.launch {
                                if (listState.firstVisibleItemScrollOffset > scrollSize / 10) {
                                    listState.animateScrollToItem(listState.firstVisibleItemIndex)
                                } else {
                                    val target = listState.firstVisibleItemIndex - 1
                                    if (target >= 0) {
                                        listState.animateScrollToItem(target)
                                    }
                                    if (listState.firstVisibleItemIndex == target + 1) {
                                        listState.animateScrollBy(-scrollSize)
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .sharedElement("previousPage", stampRally.stampRallyId, zIndexInOverlay = 1f)
                            .align(Alignment.CenterStart)
                            .hoverable(previousPageInteractionSource)
                            .visible(listState.canScrollBackward)
                    ) {
                        val previousPageIsHovered by previousPageInteractionSource.collectIsHoveredAsState()
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowLeft,
                            contentDescription = stringResource(Res.string.alley_previous_page),
                            modifier = Modifier.padding(8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceDim
                                        .copy(alpha = if (previousPageIsHovered) 0.15f else 0.5f),
                                    shape = CircleShape,
                                )
                        )
                    }

                    val nextPageInteractionSource = remember { MutableInteractionSource() }
                    IconButton(
                        onClick = {
                            scope.launch {
                                val target = listState.layoutInfo.visibleItemsInfo.lastIndex + 1
                                if (target < listState.layoutInfo.totalItemsCount) {
                                    listState.animateScrollToItem(target)
                                }
                                if (listState.layoutInfo.visibleItemsInfo.lastIndex == target - 1) {
                                    listState.animateScrollBy(scrollSize)
                                }
                            }
                        },
                        modifier = Modifier
                            .sharedElement("nextPage", stampRally.stampRallyId, zIndexInOverlay = 1f)
                            .align(Alignment.CenterEnd)
                            .hoverable(nextPageInteractionSource)
                            .visible(listState.canScrollForward)
                    ) {
                        val nextPageIsHovered by nextPageInteractionSource.collectIsHoveredAsState()
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowRight,
                            contentDescription = stringResource(Res.string.alley_next_page),
                            modifier = Modifier.padding(8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceDim
                                        .copy(alpha = if (nextPageIsHovered) 0.15f else 0.5f),
                                    shape = CircleShape,
                                )
                        )
                    }
                }
            }

            val series = stampRally.rally.series
            val merch = stampRally.rally.merch

            if (series.isNotEmpty() || merch.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
            }

            val seriesTitles = seriesTitles()
            if (series.isNotEmpty()) {
                SeriesRow(
                    series = series.take(TagUtils.TAGS_TO_SHOW).mapNotNull { seriesTitles[it] },
                    hasMoreSeries = series.size > TagUtils.TAGS_TO_SHOW,
                    onSeriesClick = onClickSeries,
                    onMoreClick = onClick,
                    modifier = Modifier.padding(start = 32.dp)
                )
            }

            if (merch.isNotEmpty()) {
                MerchRow(
                    merch = merch.take(TagUtils.TAGS_TO_SHOW),
                    hasMoreMerch = merch.size > TagUtils.TAGS_TO_SHOW,
                    onMerchClick = onClickMerch,
                    onMoreClick = onClick,
                    modifier = Modifier.padding(start = 48.dp)
                )
            }

            if (!isLast) {
                HorizontalDivider(modifier = Modifier.padding(start = 12.dp))
            }
        }
    }

    data class DayChange(
        val date: LocalDate,
        val added: List<StampRallyChangelogEntry>,
        val updated: List<StampRallyChangelogEntry>,
    )
}
