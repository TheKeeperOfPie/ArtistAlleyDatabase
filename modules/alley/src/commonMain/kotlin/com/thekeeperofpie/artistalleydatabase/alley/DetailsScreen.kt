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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
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
import com.thekeeperofpie.artistalleydatabase.alley.ui.ImagePager
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
        val fullscreenImagesState = remember { FullscreenImagesState() }
        Scaffold(
            topBar = {
                TopBar(
                    sharedElementId = sharedElementId,
                    title = title,
                    favorite = favorite,
                    onFavoriteToggle = onFavoriteToggle,
                    onClickBack = {
                        if (fullscreenImagesState.index != null) {
                            fullscreenImagesState.index = null
                        } else {
                            onClickBack()
                        }
                    },
                    onClickOpenInMap = onClickOpenInMap,
                )
            },
            modifier = Modifier.sharedBounds("itemContainer", sharedElementId)
        ) {
            Box(Modifier.padding(it)) {
                val windowSizeClass = currentWindowSizeClass()
                if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
                    ExpandedLayout(
                        fullscreenImagesState = fullscreenImagesState,
                        sharedElementId = sharedElementId,
                        images = images,
                        content = content,
                    )
                } else {
                    CompactLayout(
                        fullscreenImagesState = fullscreenImagesState,
                        sharedElementId = sharedElementId,
                        images = images,
                        initialImageIndex = initialImageIndex,
                        content = content,
                    )
                }
            }
        }
    }

    @Composable
    private fun ExpandedLayout(
        fullscreenImagesState: FullscreenImagesState,
        sharedElementId: Any,
        images: () -> List<CatalogImage>,
        content: @Composable ColumnScope.() -> Unit,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
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
                                .clickable {
                                    fullscreenImagesState.index =
                                        if (images.size > 1) index + 1 else 0
                                }
                                .sharedElement("image", image.uri)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }

        val finalShowFullImagesIndex = fullscreenImagesState.index
        if (finalShowFullImagesIndex != null) {
            val pagerState = rememberImagePagerState(images, finalShowFullImagesIndex)
            FullscreenImagePager(
                state = fullscreenImagesState,
                pagerState = pagerState,
                sharedElementId = sharedElementId,
                images = images,
            )
        }
    }

    @Composable
    private fun CompactLayout(
        fullscreenImagesState: FullscreenImagesState,
        sharedElementId: Any,
        images: () -> List<CatalogImage>,
        initialImageIndex: Int,
        content: @Composable ColumnScope.() -> Unit,
    ) {
        val headerPagerState = rememberImagePagerState(images, initialImageIndex)
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SmallImageHeader(
                sharedElementId = sharedElementId,
                images = images,
                headerPagerState = headerPagerState,
                fullscreenImagesState = fullscreenImagesState,
            )

            content()

            Spacer(Modifier.height(32.dp))
        }

        FullscreenImagePager(
            state = fullscreenImagesState,
            pagerState = headerPagerState,
            sharedElementId = sharedElementId,
            images = images,
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
    private fun rememberImagePagerState(
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
        headerPagerState: PagerState,
        fullscreenImagesState: FullscreenImagesState,
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
                fullscreenImagesState = fullscreenImagesState,
            )
        }
    }

    @Composable
    private fun ImagePager(
        sharedElementId: Any,
        pagerState: PagerState,
        images: List<CatalogImage>,
        fullscreenImagesState: FullscreenImagesState,
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
                AnimatedVisibility(
                    visible = fullscreenImagesState.index == null,
                    enter = EnterTransition.None,
                    exit = ExitTransition.None,
                ) {
                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                        if (page == 0 && images.size > 1) {
                            SmallImageGrid(
                                targetHeight = 0,
                                images = images,
                                onImageClick = { index, _ ->
                                    scope.launch {
                                        pagerState.animateScrollToPage(index + 1)
                                    }
                                }
                            )
                        } else {
                            ZoomPanBox(
                                state = zoomPanState,
                                onClick = { fullscreenImagesState.index = pagerState.currentPage },
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .height(IMAGE_HEIGHT)
                                        .fillMaxWidth()
                                        .pointerInput(zoomPanState, page) {
                                            detectTapGestures(
                                                onTap = {
                                                    fullscreenImagesState.index =
                                                        pagerState.currentPage
                                                },
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
                }
            }

            AnimatedVisibility(
                visible = fullscreenImagesState.index == null && images.size > 1
                        && zoomPanState.canPanExternal(),
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
                visible = fullscreenImagesState.index == null
                        && pagerState.currentPage != 0
                        && zoomPanState.canPanExternal(),
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

    @Composable
    private fun FullscreenImagePager(
        state: FullscreenImagesState,
        pagerState: PagerState,
        sharedElementId: Any,
        images: () -> List<CatalogImage>,
    ) {
        val coroutineScope = rememberCoroutineScope()
        val imageIndex = state.index
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
                    state.index = null
                    coroutineScope.launch {
                        pagerState.scrollToPage(fullPagerState.currentPage)
                    }
                }
                Surface(
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(16.dp)
                        .copy(alpha = 0.75f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    @Suppress("NAME_SHADOWING")
                    val images = images()
                    ImagePager(
                        images = images,
                        pagerState = pagerState,
                        sharedElementId = sharedElementId,
                        onClickPage = null,
                        clipCorners = false,
                        imageContentScale = ContentScale.Fit,
                        onClickOutside = { state.index = null },
                    )
                }
            }
        }
    }

    @Stable
    class FullscreenImagesState {
        var index by mutableStateOf<Int?>(null)
    }
}
