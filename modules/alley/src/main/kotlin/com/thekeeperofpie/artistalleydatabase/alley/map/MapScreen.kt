package com.thekeeperofpie.artistalleydatabase.alley.map

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.Popup
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.thekeeperofpie.artistalleydatabase.alley.R
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.compose.conditionally

@OptIn(ExperimentalFoundationApi::class)
object MapScreen {

    private val itemWidth = 80.dp
    private val itemHeight = 64.dp

    @Composable
    operator fun invoke(
        transformState: TransformState,
        onArtistClick: (ArtistEntryGridModel, Int) -> Unit,
    ) {
        val viewModel = hiltViewModel<MapViewModel>()
        val gridData = viewModel.gridData.result
        if (gridData != null) {
            val onArtistClickState by rememberUpdatedState(onArtistClick)
            val itemProvider =
                remember(gridData) { ItemProvider(viewModel, gridData, onArtistClickState) }
            val contentPaddingPixels = LocalDensity.current.run { 32.dp.toPx() }

            val itemWidthPixels =
                LocalDensity.current.run { itemWidth.toPx() } * transformState.scale
            val itemHeightPixels =
                LocalDensity.current.run { itemHeight.toPx() } * transformState.scale
            val maxX =
                ((gridData.maxRow + 1) * itemWidthPixels).coerceAtLeast(transformState.size.width.toFloat())
            val maxY =
                ((gridData.maxColumn + 1) * itemHeightPixels).coerceAtLeast(transformState.size.height.toFloat())

            transformState.xRange = 0f..maxX - transformState.size.width
            transformState.yRange = -maxY..-transformState.size.height.toFloat()
            transformState.scaleRange = (transformState.size.width / maxX)..3f

            LazyLayout(
                itemProvider = { itemProvider },
                modifier = Modifier
                    .onSizeChanged { transformState.onSizeChange(it) }
                    .transformable(transformState.transformableState)
                    .fillMaxSize()
                    .clipToBounds()
            ) { constraints ->
                val boundaries = getBounds(
                    offset = transformState.translation,
                    itemHeightPixels = itemWidthPixels,
                    itemWidthPixels = itemHeightPixels,
                    constraints = constraints,
                )
                val visibleTables = itemProvider.getVisibleTables(
                    itemWidthPixels = itemWidthPixels,
                    itemHeightPixels = itemHeightPixels,
                    boundaries = boundaries,
                )

                val itemConstraints = Constraints(
                    minWidth = itemWidthPixels.toInt(),
                    minHeight = itemHeightPixels.toInt(),
                    maxWidth = itemWidthPixels.toInt(),
                    maxHeight = itemHeightPixels.toInt(),
                )
                val measured = visibleTables.map { (index, table) ->
                    table to measure(index, itemConstraints)
                }

                layout(constraints.maxWidth, constraints.maxHeight) {
                    measured.forEach { (table, placeables) ->
                        val offsetX = table.gridX * itemWidthPixels
                        val offsetY = -table.gridY * itemHeightPixels
                        val translation = transformState.translation
                        val xPosition = contentPaddingPixels + offsetX - translation.x
                        val yPosition = offsetY - translation.y - contentPaddingPixels
                        placeables.forEach {
                            it.placeRelative(xPosition.toInt(), yPosition.toInt())
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TablePopup(
        entry: ArtistEntryGridModel?,
        onFavoriteToggle: (Boolean) -> Unit,
        onIgnoredToggle: (Boolean) -> Unit,
        onClick: (ArtistEntryGridModel, Int) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val ignored = entry?.ignored ?: false
        val pagerState = rememberPagerState(
            initialPage = 0,
            pageCount = { entry?.images?.size ?: 0 },
        )
        OutlinedCard(
            modifier = modifier
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .combinedClickable(
                    onClick = { entry?.let { onClick(it, pagerState.settledPage) } },
                    onLongClick = { onIgnoredToggle(!ignored) }
                )
                .alpha(if (entry?.ignored == true) 0.38f else 1f)
        ) {
            if (entry == null) {
                CircularProgressIndicator()
                return@OutlinedCard
            }

            val images = entry.images

            if (images.isNotEmpty()) {
                var minHeight by remember { mutableIntStateOf(0) }
                val coroutineScope = rememberCoroutineScope()
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    val density = LocalDensity.current
                    HorizontalPager(
                        state = pagerState,
                        pageSpacing = 16.dp,
                        modifier = Modifier
                            .heightIn(min = density.run { minHeight.toDp() })
                            .onSizeChanged {
                                if (it.height > minHeight) {
                                    minHeight = it.height
                                }
                            }
                    ) {
                        BoxWithConstraints {
                            val image = images[it]
                            AsyncImage(
                                model = image.uri,
                                contentScale = ContentScale.FillWidth,
                                fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                                contentDescription = stringResource(R.string.alley_artist_catalog_image),
                                modifier = Modifier
                                    .clickable { onClick(entry, pagerState.settledPage) }
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .conditionally(image.width != null && image.height != null) {
                                        height((image.height!! / image.width!!.toFloat()) * maxWidth)
                                    }
                            )
                        }
                    }

                    if (images.size > 1) {
                        HorizontalPagerIndicator(
                            pagerState = pagerState,
                            pageCount = pagerState.pageCount,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(8.dp)
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(start = 16.dp)
                    .conditionally(images.size > 1) { fillMaxWidth() }
            ) {
                Text(
                    text = entry.value.booth,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Text(
                    text = entry.value.name,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 12.dp)
                )

                val favorite = entry.favorite
                IconButton(onClick = { onFavoriteToggle(!favorite) }) {
                    Icon(
                        imageVector = if (favorite) {
                            Icons.Filled.Favorite
                        } else {
                            Icons.Filled.FavoriteBorder
                        },
                        contentDescription = stringResource(
                            R.string.alley_artist_favorite_icon_content_description
                        ),
                    )
                }
            }
        }
    }

    class ItemProvider(
        private val viewModel: MapViewModel,
        private val gridData: MapViewModel.GridData,
        private val onArtistClick: (ArtistEntryGridModel, Int) -> Unit,
    ) : LazyLayoutItemProvider {
        override val itemCount = gridData.tables.size

        @Composable
        override fun Item(index: Int, key: Any) {
            var showPopup by remember { mutableStateOf(false) }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = RectangleShape,
                    )
                    .clickable { showPopup = true }
            ) {
                val table = gridData.tables[index]
                if (showPopup) {
                    BackHandler { showPopup = false }
                    var tableEntry by remember { mutableStateOf<ArtistEntryGridModel?>(null) }
                    LaunchedEffect(table) {
                        tableEntry = viewModel.tableEntry(table)
                    }
                    Popup(
                        alignment = Alignment.TopCenter,
                        onDismissRequest = { showPopup = false },
                    ) {
                        val entry = tableEntry
                        if (entry == null) {
                            CircularProgressIndicator()
                        } else {
                            TablePopup(
                                entry = entry,
                                onFavoriteToggle = {
                                    entry.favorite = it
                                    viewModel.onFavoriteToggle(entry, it)
                                },
                                onIgnoredToggle = {
                                    entry.ignored = it
                                    viewModel.onIgnoredToggle(entry, it)
                                },
                                onClick = onArtistClick,
                                modifier = Modifier.sizeIn(maxWidth = 300.dp)
                            )
                        }
                    }
                }
                Text(text = table.booth)
            }
        }

        fun getVisibleTables(
            itemWidthPixels: Float,
            itemHeightPixels: Float,
            boundaries: LayoutBounds,
        ) = gridData.tables.asSequence()
            .withIndex()
            .filter { (index, table) ->
                val offsetX = table.gridX * itemWidthPixels
                val offsetY = -table.gridY * itemHeightPixels
                // Couldn't get X laziness to work
                /*boundaries.rangeX.contains(offsetX) && */boundaries.rangeY.contains(offsetY)
            }
            .toList()
    }

    data class LayoutBounds(
        val rangeX: ClosedFloatingPointRange<Float>,
        val rangeY: ClosedFloatingPointRange<Float>,
    )

    private fun getBounds(
        offset: Offset,
        itemHeightPixels: Float,
        itemWidthPixels: Float,
        constraints: Constraints,
    ): LayoutBounds {
        val maxWidth = constraints.maxWidth.toFloat()
        val maxHeight = constraints.maxHeight.toFloat()
        val extraWidth = itemWidthPixels * 2f
        val extraHeight = itemHeightPixels * 2f
        return LayoutBounds(
            offset.x - extraWidth..maxWidth + extraWidth,
            offset.y - extraHeight..maxHeight + extraHeight,
        )
    }

    @Composable
    fun rememberTransformState() =
        rememberSaveable(LocalDensity.current, saver = TransformState.Saver) {
            TransformState()
        }

    class TransformState(
        initialTranslationX: Float = 0f,
        initialTranslationY: Float = 0f,
        initialScale: Float = 1f,
        var layoutSize: IntSize = IntSize(0, 0),
        var xRange: ClosedFloatingPointRange<Float> = 0f..0f,
        var yRange: ClosedFloatingPointRange<Float> = 0f..0f,
        var scaleRange: ClosedFloatingPointRange<Float> = 0.5f..3f,
    ) {
        var size by mutableStateOf(IntSize(0, 0))
            private set
        var translation by mutableStateOf(Offset(initialTranslationX, initialTranslationY))
        var scale by mutableFloatStateOf(initialScale)

        fun onSizeChange(size: IntSize) {
            this.size = size
            if (translation.y == 0f) {
                translation = translation.copy(y = -size.height.toFloat())
            }
        }

        val transformableState = TransformableState { zoomChange, panChange, _ ->
            val translation = translation - panChange
            val scale = this.scale
            val newScale = scale * zoomChange
            val direction = if (newScale > scale) 1f else -1f
            val extraOffsetX = (scale - newScale) * layoutSize.width * direction
            val extraOffsetY = (scale - newScale) * layoutSize.height * direction
            this.translation = translation.copy(
                x = (translation.x + extraOffsetX).coerceIn(xRange),
                y = (translation.y + extraOffsetY).coerceIn(yRange),
            )
            this.scale = (scale * zoomChange).coerceIn(scaleRange)
        }

        companion object {
            val Saver: Saver<TransformState, *> = listSaver(
                save = { listOf(it.translation.x, it.translation.y, it.scale) },
                restore = {
                    TransformState(
                        initialTranslationX = it[0],
                        initialTranslationY = it[1],
                        initialScale = it[2],
                    )
                }
            )
        }

    }
}
