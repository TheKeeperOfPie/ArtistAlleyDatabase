package com.thekeeperofpie.artistalleydatabase.alley

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_catalog_image
import artistalleydatabase.modules.alley.generated.resources.alley_artist_catalog_image_none
import artistalleydatabase.modules.alley.generated.resources.alley_favorite_icon_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_open_in_map
import artistalleydatabase.modules.alley.generated.resources.alley_show_catalog_grid_content_description
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.size.Dimension
import com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImagePreviewProvider
import com.thekeeperofpie.artistalleydatabase.alley.ui.HorizontalPagerIndicator
import com.thekeeperofpie.artistalleydatabase.alley.ui.PreviewDark
import com.thekeeperofpie.artistalleydatabase.alley.ui.SmallImageGrid
import com.thekeeperofpie.artistalleydatabase.alley.ui.currentWindowSizeClass
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberImagePagerState
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedBounds
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.ZoomPanBox
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalAnimatedVisibilityScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.animateEnterExit
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.skipToLookaheadSize
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.rememberZoomPanState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
object DetailsScreen {

    private val IMAGE_HEIGHT = 320.dp

    @Composable
    operator fun invoke(
        title: @Composable () -> Unit,
        sharedElementId: Any,
        favorite: () -> Boolean?,
        images: () -> List<CatalogImage>,
        initialImageIndex: Int,
        eventSink: (Event) -> Unit,
        content: LazyListScope.() -> Unit,
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    sharedElementId = sharedElementId,
                    title = title,
                    favorite = favorite,
                    onFavoriteToggle = { eventSink(Event.FavoriteToggle(it)) },
                    onClickBack = { eventSink(Event.NavigateBack) },
                    onClickOpenInMap = { eventSink(Event.OpenMap) },
                )
            },
            modifier = Modifier.sharedBounds("itemContainer", sharedElementId)
        ) {
            Box(Modifier.padding(it)) {
                val windowSizeClass = currentWindowSizeClass()
                if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
                    ExpandedLayout(
                        sharedElementId = sharedElementId,
                        images = images,
                        onClickImage = { eventSink(Event.OpenImage(it)) },
                        content = content,
                    )
                } else {
                    CompactLayout(
                        sharedElementId = sharedElementId,
                        images = images,
                        initialImageIndex = initialImageIndex,
                        onClickImage = { eventSink(Event.OpenImage(it)) },
                        content = content,
                    )
                }
            }
        }
    }

    @Composable
    private fun ExpandedLayout(
        sharedElementId: Any,
        images: () -> List<CatalogImage>,
        onClickImage: (imageIndex: Int) -> Unit,
        content: LazyListScope.() -> Unit,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            val images = images()
            val hasImages = images.isNotEmpty()
            val width = LocalWindowConfiguration.current.screenWidthDp
            val horizontalContentPadding = if (!hasImages && width > 800.dp) {
                (width - 800.dp) / 2
            } else {
                0.dp
            }
            LazyColumn(
                contentPadding = PaddingValues(
                    start = horizontalContentPadding,
                    end = horizontalContentPadding,
                    bottom = 32.dp,
                ),
                modifier = Modifier
                    .fillMaxHeight()
                    .conditionally(hasImages) { width(400.dp) }
                    .conditionally(!hasImages) { fillMaxWidth() }
            ) {
                content()
            }

            if (hasImages) {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(500.dp),
                    contentPadding = PaddingValues(8.dp),
                    verticalItemSpacing = 8.dp,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxHeight().weight(1f)
                ) {
                    itemsIndexed(images) { index, image ->
                        val loadingColor = MaterialTheme.colorScheme.surfaceColorAtElevation(16.dp)
                        val placeholderPainter =
                            remember(MaterialTheme.colorScheme) { ColorPainter(loadingColor) }
                        AsyncImage(
                            model = image.uri,
                            contentScale = ContentScale.FillWidth,
                            contentDescription = stringResource(Res.string.alley_artist_catalog_image),
                            placeholder = placeholderPainter,
                            modifier = Modifier
                                .clickable { onClickImage(if (images.size > 1) index + 1 else 0) }
                                .sharedElement("image", image.uri)
                                .fillMaxWidth()
                                .conditionally(image.width != null && image.height != null) {
                                    aspectRatio(image.width!!.toFloat() / image.height!!)
                                }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun CompactLayout(
        sharedElementId: Any,
        images: () -> List<CatalogImage>,
        initialImageIndex: Int,
        onClickImage: (imageIndex: Int) -> Unit,
        content: LazyListScope.() -> Unit,
    ) {
        val headerPagerState = rememberImagePagerState(images, initialImageIndex)
        LazyColumn(
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item("detailsHeader") {
                SmallImageHeader(
                    sharedElementId = sharedElementId,
                    images = images,
                    headerPagerState = headerPagerState,
                    onClickImage = onClickImage,
                )
            }

            content()
        }
    }

    @Composable
    private fun TopBar(
        sharedElementId: Any,
        title: @Composable () -> Unit,
        favorite: () -> Boolean?,
        onFavoriteToggle: (Boolean) -> Unit,
        onClickBack: () -> Unit,
        onClickOpenInMap: () -> Unit,
    ) {
        TopAppBar(
            title = title,
            navigationIcon = { ArrowBackIconButton(onClickBack) },
            actions = {
                IconButton(
                    onClick = onClickOpenInMap,
                    modifier = Modifier.animateEnterExit()
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = stringResource(Res.string.alley_open_in_map),
                    )
                }

                val favorite = favorite()
                AnimatedVisibility(favorite != null, enter = fadeIn(), exit = fadeOut()) {
                    val favoriteNotNull = favorite == true
                    IconButton(
                        onClick = { onFavoriteToggle(!favoriteNotNull) },
                        modifier = Modifier.sharedElement(
                            "favorite",
                            sharedElementId,
                            zIndexInOverlay = 1f,
                        )
                    ) {
                        Icon(
                            imageVector = if (favoriteNotNull) {
                                Icons.Filled.Favorite
                            } else {
                                Icons.Filled.FavoriteBorder
                            },
                            contentDescription = stringResource(
                                Res.string.alley_favorite_icon_content_description
                            ),
                        )
                    }
                }
            },
            modifier = Modifier
                .skipToLookaheadSize()
                .sharedBounds("container", sharedElementId, zIndexInOverlay = 1f)
        )
    }

    @Composable
    private fun SmallImageHeader(
        sharedElementId: Any,
        images: () -> List<CatalogImage>,
        headerPagerState: PagerState,
        onClickImage: (imageIndex: Int) -> Unit,
    ) {
        val images = images()
        if (images.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
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
        } else {
            ImagePager(
                pagerState = headerPagerState,
                sharedElementId = sharedElementId,
                images = images,
                onClickImage = onClickImage,
            )
        }
    }

    @Composable
    fun ImagePager(
        sharedElementId: Any,
        pagerState: PagerState,
        images: List<CatalogImage>,
        onClickImage: (imageIndex: Int) -> Unit,
    ) {
        val zoomPanState = rememberZoomPanState()
        val scope = rememberCoroutineScope()
        val targetHeight = LocalDensity.current.run {
            Dimension.Pixels(
                IMAGE_HEIGHT.toPx().roundToInt()
            )
        }
        Box {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = images.size > 1 && zoomPanState.canPanExternal(),
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .height(IMAGE_HEIGHT)
                    .fillMaxWidth()
                    .sharedElement("imageContainer", sharedElementId)
                    .clipToBounds()
            ) { page ->
                if (page == 0 && images.size > 1) {
                    SmallImageGrid(
                        targetHeight = 0,
                        images = images,
                        onImageClick = { index, _ ->
                            scope.launch {
                                pagerState.animateScrollToPage(index + 1)
                            }
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    ZoomPanBox(
                        state = zoomPanState,
                        onClick = { onClickImage(pagerState.currentPage) },
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .height(IMAGE_HEIGHT)
                                .fillMaxWidth()
                                .pointerInput(zoomPanState, page) {
                                    detectTapGestures(
                                        onTap = { onClickImage(pagerState.currentPage) },
                                        onDoubleTap = {
                                            scope.launch {
                                                zoomPanState.toggleZoom(it, size)
                                            }
                                        }
                                    )
                                }
                        ) {
                            val image = images[(page - 1).coerceAtLeast(0)]
                            AsyncImage(
                                model = ImageRequest.Builder(LocalPlatformContext.current)
                                    .data(image.uri)
                                    .size(
                                        width = Dimension.Undefined,
                                        targetHeight
                                    )
                                    .build(),
                                contentScale = ContentScale.Fit,
                                contentDescription = stringResource(Res.string.alley_artist_catalog_image),
                                modifier = Modifier
                                    .sharedElement("image", image.uri)
                                    .height(IMAGE_HEIGHT)
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = images.size > 1 && zoomPanState.canPanExternal(),
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                    HorizontalPagerIndicator(
                        pagerState = pagerState,
                        modifier = Modifier
                            .sharedElement(
                                "pagerIndicator",
                                sharedElementId,
                                zIndexInOverlay = 1f,
                            )
                            .padding(8.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = pagerState.currentPage != 0 && zoomPanState.canPanExternal(),
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        },
                        modifier = Modifier.sharedElement("gridIcon", sharedElementId)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.GridOn,
                            contentDescription = stringResource(
                                Res.string.alley_show_catalog_grid_content_description
                            )
                        )
                    }
                }
            }
        }
    }

    sealed interface Event {
        data class FavoriteToggle(val favorite: Boolean) : Event
        data object NavigateBack : Event
        data class OpenImage(val imageIndex: Int) : Event
        data object OpenMap : Event
    }
}

@Preview
@Composable
private fun DetailsScreen() = PreviewDark {
    val images = CatalogImagePreviewProvider.values.take(4).toList()
    DetailsScreen(
        title = { Text("Details title") },
        sharedElementId = "sharedElementId",
        favorite = { true },
        images = { images },
        initialImageIndex = 1,
        eventSink = {},
    ) {
        item {
            Box(
                Modifier.fillMaxSize()
                    .padding(16.dp)
                    .height(400.dp)
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(16.dp))
            )
        }
    }
}

@Preview
@Composable
private fun ImagePagerGrid() = PreviewDark {
    val images = CatalogImagePreviewProvider.values.take(4).toList()
    DetailsScreen.ImagePager(
        sharedElementId = "sharedElementId",
        pagerState = rememberImagePagerState( images = { images }, initialImageIndex = 0, ),
        images = images,
        onClickImage = {},
    )
}
