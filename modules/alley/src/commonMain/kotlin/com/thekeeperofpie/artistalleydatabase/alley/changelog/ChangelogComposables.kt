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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.visible
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_catalog_image
import artistalleydatabase.modules.alley.generated.resources.alley_changelog_title_added
import artistalleydatabase.modules.alley.generated.resources.alley_changelog_title_updated
import artistalleydatabase.modules.alley.generated.resources.alley_next_page
import artistalleydatabase.modules.alley.generated.resources.alley_previous_page
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.thekeeperofpie.artistalleydatabase.alley.GetSeriesTitles
import com.thekeeperofpie.artistalleydatabase.alley.artist.MerchRow
import com.thekeeperofpie.artistalleydatabase.alley.artist.SeriesRow
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallySeriesImage
import com.thekeeperofpie.artistalleydatabase.alley.rallies.startTableOrDefault
import com.thekeeperofpie.artistalleydatabase.alley.series.name
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagUtils
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberSharedContentState
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.automirrored.filled.ArrowLeft
import com.thekeeperofpie.artistalleydatabase.icons.automirrored.filled.ArrowRight
import com.thekeeperofpie.artistalleydatabase.icons.filled.ImageNotSupported
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.fadingEdgeEnd
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChangelogImages(
    sharedElementId: Any,
    images: List<CatalogImage>,
    onClickImage: (CatalogImage) -> Unit,
) {
    Box {
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
                val width = it.width
                val height = it.height
                val ratio = if (width != null && height != null) {
                    width.toFloat() / height
                } else {
                    null
                }
                AsyncImage(
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .data(it.coilImageModel)
                        .placeholderMemoryCacheKey(it.coilImageModel.toString())
                        .build(),
                    contentScale = if (ratio == null) ContentScale.Fit else ContentScale.Crop,
                    fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                    contentDescription = stringResource(Res.string.alley_artist_catalog_image),
                    modifier = Modifier
                        .height(ChangelogUtils.ImageHeight)
                        .run {
                            if (ratio != null) {
                                width(ChangelogUtils.ImageHeight * ratio)
                            } else {
                                widthIn(min = 48.dp)
                            }
                        }
                        .sharedElement(state = sharedContentState)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
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
                .sharedElement("previousPage", sharedElementId, zIndexInOverlay = 1f)
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
                .sharedElement("nextPage", sharedElementId, zIndexInOverlay = 1f)
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

@Composable
fun ChangelogArtistRow(
    artist: ArtistChangelogEntry,
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
            val lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.Both,
            )
            val booth = artist.booth
            Text(
                // Always render 3 characters
                text = booth?.ifEmpty { null } ?: "   ",
                style = MaterialTheme.typography.titleMedium
                    .copy(
                        fontFamily = FontFamily.Monospace,
                        lineHeightStyle = lineHeightStyle,
                    ),
            )

            Text(
                text = artist.name,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
                    .copy(lineHeightStyle = lineHeightStyle),
            )
        }

        val images = artist.images
        if (images.isNotEmpty()) {
            ChangelogImages(
                sharedElementId = artist.artistId,
                images = images,
                onClickImage = onClickImage
            )
        }

        val hasSeries = artist.seriesHighlighted.isNotEmpty() || artist.seriesRemaining.isNotEmpty()
        val hasMerch = artist.merchHighlighted.isNotEmpty() || artist.merchRemaining.isNotEmpty()

        val seriesTitles = seriesTitles()
        if (hasSeries) {
            SeriesRow(
                seriesHighlighted = artist.seriesHighlighted.mapNotNull { seriesTitles[it] },
                seriesRemaining = artist.seriesRemaining.mapNotNull { seriesTitles[it] },
                hasMoreSeries = (artist.seriesHighlighted.size + artist.seriesRemaining.size) > TagUtils.TAGS_TO_SHOW,
                onSeriesClick = onClickSeries,
                onMoreClick = onClick,
                modifier = Modifier.padding(start = 32.dp)
            )
        }

        if (hasMerch) {
            MerchRow(
                merchHighlighted = artist.merchHighlighted,
                merchRemaining = artist.merchRemaining,
                hasMoreMerch = (artist.merchHighlighted.size + artist.merchRemaining.size) > TagUtils.TAGS_TO_SHOW,
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


@Composable
fun ChangelogStampRallyRow(
    stampRally: StampRallyChangelogEntry,
    isLast: Boolean,
    seriesTitles: () -> Map<String, GetSeriesTitles>,
    seriesImage: (seriesId: String) -> String?,
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
            val lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.Both,
            )
            val booth = stampRally.rally.startTableOrDefault
            Text(
                // Always render 3 characters
                text = booth?.ifEmpty { null } ?: "   ",
                style = MaterialTheme.typography.titleMedium
                    .copy(
                        fontFamily = FontFamily.Monospace,
                        lineHeightStyle = lineHeightStyle,
                    ),
            )

            Text(
                text = stampRally.rally.fandom,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
                    .copy(lineHeightStyle = lineHeightStyle),
            )
        }

        val images = stampRally.images
        if (images.isNotEmpty()) {
            ChangelogImages(
                sharedElementId = stampRally.stampRallyId,
                images = images,
                onClickImage = onClickImage,
            )
        } else {
            Spacer(Modifier.height(8.dp))
        }

        Row {
            val seriesId = stampRally.rally.series.firstOrNull()
            StampRallySeriesImage(
                stampRallyId = stampRally.rally.id,
                seriesId = seriesId,
                startTable = stampRally.rally.startTableOrDefault,
                image = { seriesId?.let(seriesImage) }
            )

            Column(modifier = Modifier.weight(1f)) {
                val series = stampRally.rally.series
                val merch = stampRally.rally.merch

                if (series.isNotEmpty() || merch.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .padding(start = 16.dp, bottom = 8.dp)
                            .fillMaxWidth()
                            .fadingEdgeEnd(
                                startOpaque = 0.dp,
                                startTransparent = 0.dp,
                                endOpaque = 32.dp,
                                endTransparent = 16.dp,
                            )
                    ) {
                        val colors = AssistChipDefaults.assistChipColors(
                            labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        )
                        val border = AssistChipDefaults.assistChipBorder(false)
                        val seriesTitles = seriesTitles()
                        val languageOption = LocalLanguageOptionMedia.current
                        series.forEach {
                            AssistChip(
                                colors = colors,
                                border = border,
                                onClick = { onClickSeries(it) },
                                label = {
                                    Text(text = seriesTitles[it]?.name(languageOption) ?: it)
                                },
                                modifier = Modifier.height(24.dp)
                            )
                        }
                        merch.forEach {
                            AssistChip(
                                colors = colors,
                                border = border,
                                onClick = { onClickMerch(it) },
                                label = { Text(text = it) },
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }
                }
            }
        }

        if (!isLast) {
            HorizontalDivider(modifier = Modifier.padding(start = 12.dp))
        }
    }
}

fun LazyListScope.changelogDayHeader(
    listState: LazyListState,
    date: LocalDate,
    key: String = "changelogDayHeader",
) {
    val headerKey = listOf(key, date)
    stickyHeader(key = headerKey, "ChangelogDayHeader") { headerIndex ->
        val pinned by remember {
            derivedStateOf {
                val offset = listState.layoutInfo.visibleItemsInfo
                    .find { it.key == headerKey }
                    ?.offset ?: return@derivedStateOf false
                offset <= 0
            }
        }
        Column {
            Text(
                text = date.format(LocalDate.Formats.ISO),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            if (pinned) {
                HorizontalDivider()
            }
        }
    }
}

fun LazyListScope.artistChangelogDay(
    date: LocalDate,
    added: List<ArtistChangelogEntry>,
    updated: List<ArtistChangelogEntry>,
    seriesTitles: () -> Map<String, GetSeriesTitles>,
    onClickArtist: (ArtistChangelogEntry) -> Unit,
    onClickSeries: (String) -> Unit,
    onClickMerch: (String) -> Unit,
    onClickImage: (ArtistChangelogEntry, CatalogImage) -> Unit,
) {
    if (added.isNotEmpty()) {
        item(key = listOf("artistHeaderAdded", date), contentType = "headerAdded") {
            Text(
                text = stringResource(Res.string.alley_changelog_title_added),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        itemsIndexed(
            items = added,
            key = { _, change -> listOf("artist", change.artistId, change.date) },
            contentType = { _, _ -> "artistRow" },
        ) { index, change ->
            ChangelogArtistRow(
                artist = change,
                isLast = index == added.lastIndex,
                seriesTitles = seriesTitles,
                onClick = { onClickArtist(change) },
                onClickSeries = onClickSeries,
                onClickMerch = onClickMerch,
                onClickImage = { onClickImage(change, it) },
            )
        }
    }

    if (updated.isNotEmpty()) {
        if (added.isNotEmpty()) {
            item(
                key = listOf("artistDividerUpdated", date),
                contentType = "dividerUpdated"
            ) {
                HorizontalDivider(modifier = Modifier.padding(start = 8.dp))
            }
        }
        item(key = listOf("artistHeaderUpdated", date), contentType = "headerUpdated") {
            Text(
                text = stringResource(Res.string.alley_changelog_title_updated),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        itemsIndexed(
            items = updated,
            key = { _, change -> listOf("artist", change.artistId, change.date) },
            contentType = { _, _ -> "artistRow" },
        ) { index, change ->
            ChangelogArtistRow(
                artist = change,
                isLast = index == updated.lastIndex,
                seriesTitles = seriesTitles,
                onClick = { onClickArtist(change) },
                onClickSeries = onClickSeries,
                onClickMerch = onClickMerch,
                onClickImage = { onClickImage(change, it) },
            )
        }
    }

    item(key = listOf("artistDivider", date), contentType = "divider") {
        HorizontalDivider(thickness = 2.dp)
    }
}

fun LazyListScope.stampRallyChangelogDay(
    date: LocalDate,
    added: List<StampRallyChangelogEntry>,
    updated: List<StampRallyChangelogEntry>,
    seriesTitles: () -> Map<String, GetSeriesTitles>,
    seriesImage: (seriesId: String) -> String?,
    onClickStampRally: (StampRallyChangelogEntry) -> Unit,
    onClickSeries: (String) -> Unit,
    onClickMerch: (String) -> Unit,
    onClickImage: (StampRallyChangelogEntry, CatalogImage) -> Unit,
) {
    if (added.isNotEmpty()) {
        item(key = listOf("rallyHeaderAdded", date), contentType = "headerAdded") {
            Text(
                text = stringResource(Res.string.alley_changelog_title_added),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        itemsIndexed(
            items = added,
            key = { _, change -> listOf("rally", change.stampRallyId, change.date) },
            contentType = { _, _ -> "stampRallyRow" },
        ) { index, change ->
            ChangelogStampRallyRow(
                stampRally = change,
                isLast = index == added.lastIndex,
                seriesTitles = seriesTitles,
                seriesImage = seriesImage,
                onClick = { onClickStampRally(change) },
                onClickSeries = onClickSeries,
                onClickMerch = onClickMerch,
                onClickImage = { onClickImage(change, it) },
            )
        }
    }

    if (updated.isNotEmpty()) {
        if (added.isNotEmpty()) {
            item(
                key = listOf("rallyDividerUpdated", date),
                contentType = "dividerUpdated"
            ) {
                HorizontalDivider(modifier = Modifier.padding(start = 8.dp))
            }
        }
        item(key = listOf("rallyHeaderUpdated", date), contentType = "headerUpdated") {
            Text(
                text = stringResource(Res.string.alley_changelog_title_updated),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        itemsIndexed(
            items = updated,
            key = { _, change -> listOf("rally", change.stampRallyId, change.date) },
            contentType = { _, _ -> "stampRallyRow" },
        ) { index, change ->
            ChangelogStampRallyRow(
                stampRally = change,
                isLast = index == updated.lastIndex,
                seriesTitles = seriesTitles,
                seriesImage = seriesImage,
                onClick = { onClickStampRally(change) },
                onClickSeries = onClickSeries,
                onClickMerch = onClickMerch,
                onClickImage = { onClickImage(change, it) },
            )
        }
    }

    item(key = listOf("rallyDivider", date), contentType = "divider") {
        HorizontalDivider(thickness = 2.dp)
    }
}
