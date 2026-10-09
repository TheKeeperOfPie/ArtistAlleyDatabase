package com.thekeeperofpie.artistalleydatabase.alley.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_catalog_image_none
import artistalleydatabase.modules.alley.generated.resources.alley_open_in_map
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImagePreviewProvider
import com.thekeeperofpie.artistalleydatabase.alley.images.ImageGrid
import com.thekeeperofpie.artistalleydatabase.alley.images.ImagePager
import com.thekeeperofpie.artistalleydatabase.alley.images.rememberImagePagerState
import com.thekeeperofpie.artistalleydatabase.alley.ui.FavoriteIconButton
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedBounds
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.filled.BrokenImage
import com.thekeeperofpie.artistalleydatabase.icons.filled.Map
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.GridUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.animateEnterExit
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.currentWindowSizeClass
import com.thekeeperofpie.artistalleydatabase.utils_preview.AlleyPreview
import org.jetbrains.compose.resources.stringResource

object DetailsScreen {

    private val DETAILS_HORIZONTAL_ARRANGEMENT = Arrangement.spacedBy(8.dp)
    private val MAX_DETAILS_WIDTH = 440.dp

    @Composable
    operator fun invoke(
        title: @Composable () -> Unit,
        sharedElementId: Any,
        favorite: () -> Boolean?,
        catalog: () -> DetailsScreenCatalog,
        imagePagerState: PagerState,
        eventSink: (Event) -> Unit,
        fallbackHeader: (@Composable () -> Unit)? = null,
        content: LazyGridScope.(columnCount: Int) -> Unit,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = title,
                    navigationIcon = {
                        ArrowBackIconButton(onClick = { eventSink(Event.NavigateUp) })
                    },
                    actions = {
                        IconButton(
                            onClick = { eventSink(Event.OpenMap) },
                            modifier = Modifier.animateEnterExit()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = stringResource(Res.string.alley_open_in_map),
                            )
                        }

                        FavoriteIconButton(
                            entryText = { null },
                            favorite = favorite,
                            onFavoriteToggle = { eventSink(Event.FavoriteToggle(it)) },
                            modifier = Modifier.sharedElement("favorite", sharedElementId)
                        )
                    },
                    modifier = Modifier.sharedBounds("container", sharedElementId)
                )
            },
            modifier = Modifier.sharedBounds("itemContainer", sharedElementId)
        ) {
            Box(Modifier.padding(it)) {
                val windowSizeClass = currentWindowSizeClass()
                val density = LocalDensity.current
                val bodyTextStyle = MaterialTheme.typography.bodyMediumEmphasized
                val gridCells = remember(density) {
                    // Try and fit a significant number of characters in each title for readability
                    val size = with(density) { (bodyTextStyle.fontSize * 4).toDp() }
                        .coerceAtLeast(80.dp)
                    GridCells.Adaptive(size)
                }
                if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
                    ExpandedLayout(
                        catalog = catalog,
                        gridCells = gridCells,
                        onClickImage = { eventSink(Event.OpenImage(it)) },
                        fallbackHeader = fallbackHeader,
                        content = content,
                    )
                } else {
                    CompactLayout(
                        sharedElementId = sharedElementId,
                        catalog = catalog,
                        gridCells = gridCells,
                        imagePagerState = imagePagerState,
                        onClickImage = { eventSink(Event.OpenImage(it)) },
                        fallbackHeader = fallbackHeader,
                        content = content,
                    )
                }
            }
        }
    }

    @Composable
    private fun ExpandedLayout(
        catalog: () -> DetailsScreenCatalog,
        gridCells: GridCells,
        onClickImage: (imageIndex: Int) -> Unit,
        fallbackHeader: (@Composable () -> Unit)? = null,
        content: LazyGridScope.(columnCount: Int) -> Unit,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            val catalog = catalog()
            val images = catalog.images
            val hasImages = images.isNotEmpty()
            val width = LocalWindowConfiguration.current.screenWidthDp
            val horizontalContentPadding = if (!hasImages && width > 800.dp) {
                (width - 800.dp) / 2
            } else {
                0.dp
            }.coerceAtLeast(16.dp)
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxHeight()
                    .conditionally(hasImages) { width(MAX_DETAILS_WIDTH) }
                    .conditionally(!hasImages) { fillMaxWidth() }
            ) {
                val density = LocalDensity.current
                val columnCount = remember(gridCells, density, maxWidth, horizontalContentPadding) {
                    with(gridCells) {
                        with(density) {
                            val availableSize =
                                (maxWidth - (horizontalContentPadding * 2)).roundToPx()
                            val spacing = DETAILS_HORIZONTAL_ARRANGEMENT.spacing.roundToPx()
                            density.calculateCrossAxisCellSizes(
                                availableSize = availableSize,
                                spacing = spacing,
                            ).size
                        }
                    }
                }

                LazyVerticalGrid(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = DETAILS_HORIZONTAL_ARRANGEMENT,
                    contentPadding = PaddingValues(
                        start = horizontalContentPadding,
                        end = horizontalContentPadding,
                        bottom = 64.dp,
                    ),
                    columns = gridCells,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (!hasImages && fallbackHeader != null) {
                        item("availableFallbackPrompt", GridUtils.maxSpanFunction) {
                            fallbackHeader()
                        }
                    }
                    content(columnCount)
                }
            }
            if (hasImages) {
                Column {
                    fallbackHeader?.invoke()
                    ImageGrid(
                        images = images,
                        onClickImage = {
                            // Adjust by 1 to account for grid on full screen
                            onClickImage(it + 1)
                        },
                        modifier = Modifier.fillMaxHeight().weight(1f)
                    )
                }
            }
        }
    }

    @Composable
    private fun CompactLayout(
        sharedElementId: Any,
        catalog: () -> DetailsScreenCatalog,
        gridCells: GridCells,
        imagePagerState: PagerState,
        onClickImage: (imageIndex: Int) -> Unit,
        fallbackHeader: (@Composable () -> Unit)?,
        content: LazyGridScope.(columnCount: Int) -> Unit,
    ) {
        BoxWithConstraints {
            val density = LocalDensity.current
            val columnCount = remember(gridCells, density, maxWidth) {
                with(gridCells) {
                    with(density) {
                        val availableSize = (maxWidth - 32.dp).roundToPx()
                        val spacing = DETAILS_HORIZONTAL_ARRANGEMENT.spacing.roundToPx()
                        density.calculateCrossAxisCellSizes(
                            availableSize = availableSize,
                            spacing = spacing,
                        ).size
                    }
                }
            }
            LazyVerticalGrid(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = DETAILS_HORIZONTAL_ARRANGEMENT,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 64.dp),
                columns = gridCells,
                modifier = Modifier.fillMaxSize()
            ) {
                item("detailsHeader", GridUtils.maxSpanFunction) {
                    SmallImageHeader(
                        sharedElementId = sharedElementId,
                        catalog = catalog,
                        headerPagerState = imagePagerState,
                        onClickImage = onClickImage,
                        fallbackHeader = fallbackHeader,
                        // Offset to remove content padding since the header is full width
                        modifier = Modifier.layout { measurable, constraints ->
                            val newConstraints = constraints.offset(32.dp.roundToPx())
                            val placeable = measurable.measure(newConstraints)
                            layout(placeable.width, placeable.height) {
                                placeable.placeRelative(0, 0)
                            }
                        },
                    )
                }

                content(columnCount)
            }
        }
    }

    @Composable
    private fun SmallImageHeader(
        sharedElementId: Any,
        catalog: () -> DetailsScreenCatalog,
        headerPagerState: PagerState,
        onClickImage: (imageIndex: Int) -> Unit,
        modifier: Modifier = Modifier,
        fallbackHeader: (@Composable () -> Unit)?,
    ) {
        val catalog = catalog()
        val images = catalog.images
        val fallbackYear = catalog.fallbackYear
        val showFallbackImages = catalog.showOutdatedCatalogs
        if (images.isEmpty()) {
            if (showFallbackImages == false && fallbackYear != null) {
                fallbackHeader?.invoke()
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Filled.BrokenImage,
                        contentDescription = stringResource(
                            Res.string.alley_artist_catalog_image_none
                        )
                    )
                }
            }
        } else {
            Column(modifier = modifier) {
                ImagePager(
                    images = images,
                    pagerState = headerPagerState,
                    sharedElementId = sharedElementId,
                    onClickPage = onClickImage,
                    onClickFullscreen = null,
                )
                if (fallbackHeader != null) {
                    fallbackHeader()
                }
            }
        }
    }

    sealed interface Event {
        data class FavoriteToggle(val favorite: Boolean) : Event
        data object NavigateUp : Event
        data class OpenImage(val imageIndex: Int) : Event
        data object OpenMap : Event
    }
}

@AlleyPreview
@Composable
private fun DetailsScreen() {
    val images = CatalogImagePreviewProvider.values.take(4).toList()
    DetailsScreen(
        title = { Text("Details title") },
        sharedElementId = "sharedElementId",
        favorite = { true },
        catalog = { DetailsScreenCatalog(images, false, null) },
        imagePagerState = rememberImagePagerState(images, 1),
        eventSink = {},
    ) {
        item(span = GridUtils.maxSpanFunction) {
            Box(
                Modifier.fillMaxWidth()
                    .height(400.dp)
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(16.dp))
            )
        }
    }
}

@AlleyPreview
@Composable
private fun ImagePagerGrid() {
    val images = CatalogImagePreviewProvider.values.take(4).toList()
    ImagePager(
        sharedElementId = "sharedElementId",
        pagerState = rememberImagePagerState(images = images, initialImageIndex = 0),
        images = images,
        onClickPage = {},
        onClickFullscreen = {},
    )
}
