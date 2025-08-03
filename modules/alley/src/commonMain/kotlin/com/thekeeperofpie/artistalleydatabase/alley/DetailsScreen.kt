package com.thekeeperofpie.artistalleydatabase.alley

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_catalog_image
import artistalleydatabase.modules.alley.generated.resources.alley_artist_catalog_image_none
import artistalleydatabase.modules.alley.generated.resources.alley_favorite_icon_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_open_in_map
import artistalleydatabase.modules.alley.generated.resources.alley_unfavorite_dialog_text_generic
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImagePreviewProvider
import com.thekeeperofpie.artistalleydatabase.alley.favorite.UnfavoriteDialog
import com.thekeeperofpie.artistalleydatabase.alley.images.ImagePager
import com.thekeeperofpie.artistalleydatabase.alley.images.rememberImagePagerState
import com.thekeeperofpie.artistalleydatabase.alley.ui.ImageFallbackBanner
import com.thekeeperofpie.artistalleydatabase.alley.ui.PreviewDark
import com.thekeeperofpie.artistalleydatabase.alley.ui.currentWindowSizeClass
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedBounds
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.animateEnterExit
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
object DetailsScreen {

    @Composable
    operator fun invoke(
        title: @Composable () -> Unit,
        sharedElementId: Any,
        favorite: () -> Boolean?,
        images: () -> List<CatalogImage>,
        fallbackYear: () -> DataYear?,
        imagePagerState: PagerState,
        eventSink: (Event) -> Unit,
        content: LazyListScope.() -> Unit,
    ) {
        Scaffold(
            topBar = {
                var showUnfavoriteDialog by remember { mutableStateOf(false) }
                TopBar(
                    sharedElementId = sharedElementId,
                    title = title,
                    favorite = favorite,
                    onFavoriteToggle = {
                        if (it) {
                            eventSink(Event.FavoriteToggle(true))
                        } else {
                            showUnfavoriteDialog = true
                        }
                    },
                    onClickBack = { eventSink(Event.NavigateUp) },
                    onClickOpenInMap = { eventSink(Event.OpenMap) },
                )

                if (showUnfavoriteDialog) {
                    UnfavoriteDialog(
                        text = stringResource(Res.string.alley_unfavorite_dialog_text_generic),
                        onDismissRequest = { showUnfavoriteDialog = false },
                        onRemoveFavorite = { eventSink(Event.FavoriteToggle(false)) },
                    )
                }
            },
            modifier = Modifier.sharedBounds("itemContainer", sharedElementId)
        ) {
            Box(Modifier.padding(it)) {
                val windowSizeClass = currentWindowSizeClass()
                if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
                    ExpandedLayout(
                        images = images,
                        fallbackYear = fallbackYear,
                        onClickImage = { eventSink(Event.OpenImage(it)) },
                        content = content,
                    )
                } else {
                    CompactLayout(
                        sharedElementId = sharedElementId,
                        images = images,
                        fallbackYear = fallbackYear,
                        imagePagerState = imagePagerState,
                        onClickImage = { eventSink(Event.OpenImage(it)) },
                        content = content,
                    )
                }
            }
        }
    }

    @Composable
    private fun ExpandedLayout(
        images: () -> List<CatalogImage>,
        fallbackYear: () -> DataYear?,
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
                Column {
                    val fallbackYear = fallbackYear()
                    if (fallbackYear != null) {
                        ImageFallbackBanner(fallbackYear)
                    }

                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(500.dp),
                        contentPadding = PaddingValues(8.dp),
                        verticalItemSpacing = 8.dp,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxHeight().weight(1f)
                    ) {
                        itemsIndexed(images) { index, image ->
                            val loadingColor =
                                MaterialTheme.colorScheme.surfaceColorAtElevation(16.dp)
                            val placeholderPainter =
                                remember(MaterialTheme.colorScheme) { ColorPainter(loadingColor) }
                            AsyncImage(
                                model = ImageRequest.Builder(LocalPlatformContext.current)
                                    .data(image.uri)
                                    .placeholderMemoryCacheKey(image.uri.toString())
                                    .build(),
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
    }

    @Composable
    private fun CompactLayout(
        sharedElementId: Any,
        images: () -> List<CatalogImage>,
        fallbackYear: () -> DataYear?,
        imagePagerState: PagerState,
        onClickImage: (imageIndex: Int) -> Unit,
        content: LazyListScope.() -> Unit,
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item("detailsHeader") {
                SmallImageHeader(
                    sharedElementId = sharedElementId,
                    images = images,
                    fallbackYear = fallbackYear,
                    headerPagerState = imagePagerState,
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
                        modifier = Modifier.sharedElement("favorite", sharedElementId)
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
            modifier = Modifier.sharedBounds("container", sharedElementId)
        )
    }

    @Composable
    private fun SmallImageHeader(
        sharedElementId: Any,
        images: () -> List<CatalogImage>,
        fallbackYear: () -> DataYear?,
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
            Column {
                ImagePager(
                    images = images,
                    pagerState = headerPagerState,
                    sharedElementId = sharedElementId,
                    onClickPage = onClickImage,
                )
                val fallbackYear = fallbackYear()
                if (fallbackYear != null) {
                    ImageFallbackBanner(fallbackYear)
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

@Preview
@Composable
private fun DetailsScreen() = PreviewDark {
    val images = CatalogImagePreviewProvider.values.take(4).toList()
    DetailsScreen(
        title = { Text("Details title") },
        sharedElementId = "sharedElementId",
        favorite = { true },
        images = { images },
        fallbackYear = { null },
        imagePagerState = rememberImagePagerState(images, 1),
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
    ImagePager(
        sharedElementId = "sharedElementId",
        pagerState = rememberImagePagerState(images = images, initialImageIndex = 0),
        images = images,
        onClickPage = {},
    )
}
