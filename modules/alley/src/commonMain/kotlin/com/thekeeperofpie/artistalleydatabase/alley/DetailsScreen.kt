package com.thekeeperofpie.artistalleydatabase.alley

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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
import com.thekeeperofpie.artistalleydatabase.alley.ui.HorizontalPagerIndicator
import com.thekeeperofpie.artistalleydatabase.alley.ui.SmallImageGrid
import com.thekeeperofpie.artistalleydatabase.alley.ui.currentWindowSizeClass
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedBounds
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.BackHandler
import com.thekeeperofpie.artistalleydatabase.utils_compose.ZoomPanBox
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalAnimatedVisibilityScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.animateEnterExit
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.skipToLookaheadSize
import com.thekeeperofpie.artistalleydatabase.utils_compose.rememberZoomPanState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
object DetailsScreen {

    private val IMAGE_HEIGHT = 320.dp

    @Composable
    operator fun invoke(
        title: @Composable () -> Unit,
        sharedElementId: Any,
        favorite: () -> Boolean,
        onFavoriteToggle: (Boolean) -> Unit,
        images: () -> List<CatalogImage>,
        onClickBack: () -> Unit,
        onClickOpenInMap: () -> Unit,
        initialImageIndex: Int,
        content: @Composable ColumnScope.() -> Unit,
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    sharedElementId = sharedElementId,
                    title = title,
                    favorite = favorite,
                    onFavoriteToggle = onFavoriteToggle,
                    onClickBack = onClickBack,
                    onClickOpenInMap = onClickOpenInMap,
                )
            },
            modifier = Modifier.sharedBounds("itemContainer", sharedElementId)
        ) {
            val windowSizeClass = currentWindowSizeClass()
            if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
                ExpandedLayout(images = images, scaffoldPadding = it, content = content)
            } else {
                CompactLayout(
                    sharedElementId = sharedElementId,
                    images = images,
                    initialImageIndex = initialImageIndex,
                    scaffoldPadding = it,
                    content = content,
                )
            }
        }
    }

    @Composable
    private fun ExpandedLayout(
        images: () -> List<CatalogImage>,
        scaffoldPadding: PaddingValues,
        content: @Composable ColumnScope.() -> Unit,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(scaffoldPadding)
        ) {
            val images = images()
            val hasImages = images.isNotEmpty()
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxHeight()
                    .width(if (hasImages) 400.dp else 800.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                content()
                Spacer(Modifier.height(32.dp))
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
                        AsyncImage(
                            model = image.uri,
                            contentScale = ContentScale.FillWidth,
                            contentDescription = stringResource(Res.string.alley_artist_catalog_image),
                            modifier = Modifier
                                .clickable { /* TODO */ }
                                .sharedElement("gridImage", image.uri)
                                .fillMaxWidth()
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
        scaffoldPadding: PaddingValues,
        content: @Composable ColumnScope.() -> Unit,
    ) {
        var showFullImagesIndex by rememberSaveable { mutableStateOf<Int?>(null) }
        val headerPagerState = rememberSmallImageHeaderState(images, initialImageIndex)
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(scaffoldPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SmallImageHeader(
                headerPagerState = headerPagerState,
                sharedElementId = sharedElementId,
                images = images,
                showFullImagesIndex = { showFullImagesIndex },
                onShowFullImagesIndexChange = {
                    showFullImagesIndex = headerPagerState.currentPage
                },
            )

            content()

            Spacer(Modifier.height(32.dp))
        }

        FullscreenImagePager(
            headerPagerState = headerPagerState,
            sharedElementId = sharedElementId,
            images = images,
            showFullImagesIndex = showFullImagesIndex,
            onShowFullImagesIndexChange = { showFullImagesIndex = it },
        )
    }

    @Composable
    private fun TopBar(
        sharedElementId: Any,
        title: @Composable () -> Unit,
        favorite: () -> Boolean,
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

                IconButton(
                    onClick = { onFavoriteToggle(!favorite()) },
                    modifier = Modifier.sharedElement(
                        "favorite",
                        sharedElementId,
                        zIndexInOverlay = 1f,
                    )
                ) {
                    Icon(
                        imageVector = if (favorite()) {
                            Icons.Filled.Favorite
                        } else {
                            Icons.Filled.FavoriteBorder
                        },
                        contentDescription = stringResource(
                            Res.string.alley_favorite_icon_content_description
                        ),
                    )
                }
            },
            modifier = Modifier
                .skipToLookaheadSize()
                .sharedBounds("container", sharedElementId, zIndexInOverlay = 1f)
        )
    }

    @Composable
    private fun rememberSmallImageHeaderState(
        images: () -> List<CatalogImage>,
        initialImageIndex: Int,
    ): PagerState {
        @Suppress("NAME_SHADOWING")
        val images = images()
        val pageCount = when {
            images.isEmpty() -> 0
            images.size == 1 -> 1
            else -> images.size + 1
        }
        return rememberPagerState(
            initialPage = initialImageIndex.coerceAtMost(pageCount - 1),
            pageCount = { pageCount },
        )
    }

    @Composable
    private fun SmallImageHeader(
        sharedElementId: Any,
        images: () -> List<CatalogImage>,
        showFullImagesIndex: () -> Int?,
        onShowFullImagesIndexChange: (Int) -> Unit,
        headerPagerState: PagerState,
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
                showFullImagesIndex = showFullImagesIndex(),
                onShowFullImagesIndexChange = {
                    onShowFullImagesIndexChange(headerPagerState.currentPage)
                },
            )
        }
    }

    @Composable
    private fun ImagePager(
        sharedElementId: Any,
        pagerState: PagerState,
        images: List<CatalogImage>,
        showFullImagesIndex: Int?,
        onShowFullImagesIndexChange: () -> Unit,
    ) {
        val zoomPanState = rememberZoomPanState()
        val coroutineScope = rememberCoroutineScope()
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
                AnimatedVisibility(
                    visible = showFullImagesIndex == null,
                    enter = EnterTransition.None,
                    exit = ExitTransition.None,
                ) {
                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                        if (page == 0 && images.size > 1) {
                            SmallImageGrid(
                                targetHeight = 0,
                                images = images,
                                onImageClick = { index, _ ->
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index + 1)
                                    }
                                }
                            )
                        } else {
                            ZoomPanBox(
                                state = zoomPanState,
                                onClick = { onShowFullImagesIndexChange() },
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .height(IMAGE_HEIGHT)
                                        .fillMaxWidth()
                                        .pointerInput(zoomPanState, page) {
                                            detectTapGestures(
                                                onTap = { onShowFullImagesIndexChange() },
                                                onDoubleTap = {
                                                    coroutineScope.launch {
                                                        zoomPanState.toggleZoom(
                                                            it,
                                                            size
                                                        )
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
                }
            }

            AnimatedVisibility(
                visible = showFullImagesIndex == null && images.size > 1 && zoomPanState.canPanExternal(),
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
                visible = showFullImagesIndex == null
                        && pagerState.currentPage != 0
                        && zoomPanState.canPanExternal(),
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
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

    @Composable
    private fun FullscreenImagePager(
        headerPagerState: PagerState,
        sharedElementId: Any,
        images: () -> List<CatalogImage>,
        showFullImagesIndex: Int?,
        onShowFullImagesIndexChange: (Int?) -> Unit,
    ) {
        val coroutineScope = rememberCoroutineScope()
        val imageIndex = showFullImagesIndex
        AnimatedVisibility(
            visible = imageIndex != null,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            val images = images()
            val fullPagerState = rememberPagerState(
                initialPage = imageIndex ?: 0,
                pageCount = {
                    when {
                        images.isEmpty() -> 0
                        images.size == 1 -> 1
                        else -> images.size + 1
                    }
                },
            )
            CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                BackHandler {
                    onShowFullImagesIndexChange(null)
                    coroutineScope.launch {
                        headerPagerState.scrollToPage(fullPagerState.currentPage)
                    }
                }
                Surface(
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(16.dp)
                        .copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    @Suppress("NAME_SHADOWING")
                    val images = images()
                    val zoomPanState = rememberZoomPanState()
                    Box {
                        HorizontalPager(
                            state = fullPagerState,
                            pageSpacing = 16.dp,
                            userScrollEnabled = images.size > 1 && zoomPanState.canPanExternal(),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (it == 0 && images.size > 1) {
                                SmallImageGrid(
                                    targetHeight = null,
                                    images = images,
                                    onImageClick = { index, _ ->
                                        coroutineScope.launch {
                                            headerPagerState.animateScrollToPage(index + 1)
                                        }
                                    }
                                )
                            } else {
                                ZoomPanBox(
                                    state = zoomPanState,
                                    onClick = {
                                        onShowFullImagesIndexChange(null)
                                        coroutineScope.launch {
                                            headerPagerState.scrollToPage(fullPagerState.currentPage)
                                        }
                                    },
                                ) {
                                    val image = images[(it - 1).coerceAtLeast(0)]
                                    AsyncImage(
                                        model = image.uri,
                                        contentScale = ContentScale.Fit,
                                        fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                                        contentDescription = stringResource(Res.string.alley_artist_catalog_image),
                                        modifier = Modifier
                                            .pointerInput(zoomPanState) {
                                                detectTapGestures(
                                                    // Consume click events so that tapping image doesn't dismiss
                                                    onTap = {},
                                                    onDoubleTap = {
                                                        coroutineScope.launch {
                                                            zoomPanState.toggleZoom(it, size)
                                                        }
                                                    }
                                                )
                                            }
                                            .sharedElement("image", image.uri)
                                            .fillMaxWidth()
                                            .align(Alignment.Center)
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = zoomPanState.canPanExternal(),
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier.align(Alignment.BottomCenter)
                        ) {
                            HorizontalPagerIndicator(
                                pagerState = fullPagerState,
                                modifier = Modifier
                                    .sharedElement(
                                        "pagerIndicator",
                                        sharedElementId,
                                        zIndexInOverlay = 1f,
                                    )
                                    .padding(8.dp)
                            )
                        }

                        AnimatedVisibility(
                            visible = fullPagerState.currentPage != 0 && zoomPanState.canPanExternal(),
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier.align(Alignment.BottomEnd)
                        ) {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        headerPagerState.animateScrollToPage(0)
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
        }
    }
}
