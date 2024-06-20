package com.thekeeperofpie.artistalleydatabase.alley

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Dimension
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.thekeeperofpie.artistalleydatabase.compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.compose.LocalAnimatedVisibilityScope
import com.thekeeperofpie.artistalleydatabase.compose.ZoomPanBox
import com.thekeeperofpie.artistalleydatabase.compose.rememberZoomPanState
import com.thekeeperofpie.artistalleydatabase.compose.sharedBounds
import com.thekeeperofpie.artistalleydatabase.compose.sharedElement
import com.thekeeperofpie.artistalleydatabase.compose.skipToLookaheadSize
import kotlinx.coroutines.launch
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
        initialImageIndex: Int,
        content: @Composable ColumnScope.() -> Unit,
    ) {
        var showFullImagesIndex by rememberSaveable { mutableStateOf<Int?>(null) }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = title,
                    navigationIcon = { ArrowBackIconButton(onClickBack) },
                    actions = {
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
                                    R.string.alley_artist_favorite_icon_content_description
                                ),
                            )
                        }
                    },
                    modifier = Modifier
                        .skipToLookaheadSize()
                        .sharedBounds("container", sharedElementId, zIndexInOverlay = 1f)
                )
            }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                @Suppress("NAME_SHADOWING")
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
                                R.string.alley_artist_catalog_image_none
                            )
                        )
                    }
                } else {
                    val pagerState = rememberPagerState(
                        initialPage = initialImageIndex.coerceAtMost(images.size - 1),
                        pageCount = {
                            if (images.isEmpty()) {
                                0
                            } else if (images.size == 1) {
                                1
                            } else {
                                images.size + 1
                            }
                        },
                    )
                    ImagePager(
                        pagerState = pagerState,
                        sharedElementId = sharedElementId,
                        images = images,
                        showFullImagesIndex = showFullImagesIndex,
                        onShowFullImagesIndexChange = { showFullImagesIndex = it },
                    )
                }

                content()

                Spacer(Modifier.height(32.dp))
            }
        }

        val imageIndex = showFullImagesIndex
        AnimatedVisibility(
            visible = imageIndex != null,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            CompositionLocalProvider(
                LocalAnimatedVisibilityScope provides this,
            ) {
                BackHandler { showFullImagesIndex = null }
                Surface(
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(16.dp)
                        .copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    @Suppress("NAME_SHADOWING")
                    val images = images()
                    val pagerState = rememberPagerState(
                        initialPage = imageIndex ?: 0,
                        pageCount = {
                            if (images.isEmpty()) {
                                0
                            } else if (images.size == 1) {
                                1
                            } else {
                                images.size + 1
                            }
                        },
                    )
                    val zoomPanState = rememberZoomPanState()
                    val coroutineScope = rememberCoroutineScope()
                    Box {
                        HorizontalPager(
                            state = pagerState,
                            pageSpacing = 16.dp,
                            userScrollEnabled = images.size > 1 && zoomPanState.canPanExternal(),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            if (it == 0 && images.size > 1) {
                                ImageGrid(
                                    targetHeight = null,
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
                                    onClick = { showFullImagesIndex = null },
                                ) {
                                    val image = images[(it - 1).coerceAtLeast(0)]
                                    AsyncImage(
                                        model = image.uri,
                                        contentScale = ContentScale.Fit,
                                        fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                                        contentDescription = stringResource(R.string.alley_artist_catalog_image),
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
                                            .sharedBounds("image", image.uri)
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
                                pagerState = pagerState,
                                pageCount = pagerState.pageCount,
                                modifier = Modifier
                                    .padding(8.dp)
                            )
                        }

                        AnimatedVisibility(
                            visible = pagerState.currentPage != 0 && zoomPanState.canPanExternal(),
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier.align(Alignment.BottomEnd)
                        ) {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(0)
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.GridOn,
                                    contentDescription = stringResource(
                                        R.string.alley_show_catalog_grid_content_description
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ImagePager(
        sharedElementId: Any,
        pagerState: PagerState,
        images: List<CatalogImage>,
        showFullImagesIndex: Int?,
        onShowFullImagesIndexChange: (Int?) -> Unit,
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
                    .sharedBounds("imageContainer", sharedElementId)
                    .height(IMAGE_HEIGHT)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) { page ->
                if (page == 0 && images.size > 1) {
                    ImageGrid(
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
                        onClick = { onShowFullImagesIndexChange(null) },
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .height(IMAGE_HEIGHT)
                                .fillMaxWidth()
                                .pointerInput(zoomPanState, page) {
                                    detectTapGestures(
                                        onTap = { onShowFullImagesIndexChange(page) },
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
                            AnimatedVisibility(visible = showFullImagesIndex == null) {
                                CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(image.uri)
                                            .size(
                                                width = Dimension.Undefined,
                                                targetHeight
                                            )
                                            .build(),
                                        contentScale = ContentScale.Fit,
                                        contentDescription = stringResource(R.string.alley_artist_catalog_image),
                                        modifier = Modifier
                                            .sharedBounds("image", image.uri)
                                            .height(IMAGE_HEIGHT)
                                    )
                                }
                            }
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
                HorizontalPagerIndicator(
                    pagerState = pagerState,
                    pageCount = pagerState.pageCount,
                    modifier = Modifier
                        .sharedBounds(
                            "pagerIndicator",
                            sharedElementId,
                            zIndexInOverlay = 1f
                        )
                        .padding(8.dp)
                )
            }

            AnimatedVisibility(
                visible = pagerState.currentPage != 0 && zoomPanState.canPanExternal(),
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                IconButton(onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                }) {
                    Icon(
                        imageVector = Icons.Filled.GridOn,
                        contentDescription = stringResource(
                            R.string.alley_show_catalog_grid_content_description
                        )
                    )
                }
            }
        }
    }
}
