package com.thekeeperofpie.artistalleydatabase.alley.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
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
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_catalog_available_fallback_prompt
import artistalleydatabase.modules.alley.generated.resources.alley_artist_catalog_available_fallback_prompt_always_show
import artistalleydatabase.modules.alley.generated.resources.alley_artist_catalog_available_fallback_prompt_show
import artistalleydatabase.modules.alley.generated.resources.alley_artist_catalog_image_none
import artistalleydatabase.modules.alley.generated.resources.alley_favorite_icon_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_open_in_map
import artistalleydatabase.modules.alley.generated.resources.alley_unfavorite_dialog_text_generic
import com.thekeeperofpie.artistalleydatabase.alley.favorite.UnfavoriteDialog
import com.thekeeperofpie.artistalleydatabase.alley.fullName
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImagePreviewProvider
import com.thekeeperofpie.artistalleydatabase.alley.images.ImageGrid
import com.thekeeperofpie.artistalleydatabase.alley.images.ImagePager
import com.thekeeperofpie.artistalleydatabase.alley.images.rememberImagePagerState
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.alley.ui.ImageFallbackBanner
import com.thekeeperofpie.artistalleydatabase.alley.ui.PreviewDark
import com.thekeeperofpie.artistalleydatabase.alley.ui.currentWindowSizeClass
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedBounds
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.animateEnterExit
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
object DetailsScreen {

    @Composable
    operator fun invoke(
        title: @Composable () -> Unit,
        sharedElementId: Any,
        favorite: () -> Boolean?,
        catalog: () -> LoadingResult<DetailsScreenCatalog>,
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
                        sharedElementId = sharedElementId,
                        catalog = catalog,
                        onClickImage = { eventSink(Event.OpenImage(it)) },
                        onShowFallback = { eventSink(Event.ShowFallback) },
                        onAlwaysShowFallback = { eventSink(Event.AlwaysShowFallback) },
                        content = content,
                    )
                } else {
                    CompactLayout(
                        sharedElementId = sharedElementId,
                        catalog = catalog,
                        imagePagerState = imagePagerState,
                        onClickImage = { eventSink(Event.OpenImage(it)) },
                        onShowFallback = { eventSink(Event.ShowFallback) },
                        onAlwaysShowFallback = { eventSink(Event.AlwaysShowFallback) },
                        content = content,
                    )
                }
            }
        }
    }

    @Composable
    private fun ExpandedLayout(
        sharedElementId: Any,
        catalog: () -> LoadingResult<DetailsScreenCatalog>,
        onClickImage: (imageIndex: Int) -> Unit,
        onShowFallback: () -> Unit,
        onAlwaysShowFallback: () -> Unit,
        content: LazyListScope.() -> Unit,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            val catalog = catalog()
            val images = catalog.result?.images.orEmpty()
            val hasImages = images.isNotEmpty()
            val width = LocalWindowConfiguration.current.screenWidthDp
            val horizontalContentPadding = if (!hasImages && width > 800.dp) {
                (width - 800.dp) / 2
            } else {
                0.dp
            }
            val fallbackYear = catalog.result?.fallbackYear
            val showFallbackImages = catalog.result?.showOutdatedCatalogs
            val showFallbackPrompt = !hasImages && showFallbackImages == false &&
                    fallbackYear != null
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
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
                item("availableFallbackPrompt") {
                    if (catalog.loading) {
                        LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth())
                    } else if (showFallbackPrompt) {
                        AvailableFallbackPrompt(
                            fallbackYear = fallbackYear,
                            onShowFallback = onShowFallback,
                            onAlwaysShowFallback = onAlwaysShowFallback,
                        )
                    }
                }
                content()
            }
            if (hasImages) {
                Column {
                    if (fallbackYear != null) {
                        ImageFallbackBanner(sharedElementId, fallbackYear)
                    }

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
        catalog: () -> LoadingResult<DetailsScreenCatalog>,
        imagePagerState: PagerState,
        onClickImage: (imageIndex: Int) -> Unit,
        onShowFallback: () -> Unit,
        onAlwaysShowFallback: () -> Unit,
        content: LazyListScope.() -> Unit,
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item("detailsHeader") {
                SmallImageHeader(
                    sharedElementId = sharedElementId,
                    catalog = catalog,
                    headerPagerState = imagePagerState,
                    onClickImage = onClickImage,
                    onShowFallback = onShowFallback,
                    onAlwaysShowFallback = onAlwaysShowFallback,
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
        catalog: () -> LoadingResult<DetailsScreenCatalog>,
        headerPagerState: PagerState,
        onClickImage: (imageIndex: Int) -> Unit,
        onShowFallback: () -> Unit,
        onAlwaysShowFallback: () -> Unit,
    ) {
        val catalog = catalog()
        val images = catalog.result?.images
        val fallbackYear = catalog.result?.fallbackYear
        if (images.isNullOrEmpty()) {
            val showFallbackImages = catalog.result?.showOutdatedCatalogs
            if (showFallbackImages == false && fallbackYear != null) {
                AvailableFallbackPrompt(
                    fallbackYear = fallbackYear,
                    onShowFallback = onShowFallback,
                    onAlwaysShowFallback = onAlwaysShowFallback,
                )
            } else {
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
            }
        } else {
            Column {
                ImagePager(
                    images = images,
                    pagerState = headerPagerState,
                    sharedElementId = sharedElementId,
                    onClickPage = onClickImage,
                )
                if (fallbackYear != null) {
                    ImageFallbackBanner(sharedElementId, fallbackYear)
                }
            }
        }
    }

    @Composable
    private fun AvailableFallbackPrompt(
        fallbackYear: DataYear,
        onShowFallback: () -> Unit,
        onAlwaysShowFallback: () -> Unit,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .heightIn(min = 200.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.BrokenImage,
                    contentDescription = stringResource(
                        Res.string.alley_artist_catalog_image_none
                    )
                )
                Text(
                    text = stringResource(
                        Res.string.alley_artist_catalog_available_fallback_prompt,
                        stringResource(fallbackYear.fullName),
                    )
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(onClick = onShowFallback) {
                        Text(
                            text = stringResource(
                                Res.string.alley_artist_catalog_available_fallback_prompt_show,
                                stringResource(fallbackYear.shortName),
                            )
                        )
                    }
                    Button(onClick = onAlwaysShowFallback) {
                        Text(
                            text = stringResource(
                                Res.string.alley_artist_catalog_available_fallback_prompt_always_show
                            )
                        )
                    }
                }
            }
        }
    }

    sealed interface Event {
        data class FavoriteToggle(val favorite: Boolean) : Event
        data object NavigateUp : Event
        data class OpenImage(val imageIndex: Int) : Event
        data object OpenMap : Event
        data object ShowFallback : Event
        data object AlwaysShowFallback : Event
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
        catalog = { LoadingResult.success(DetailsScreenCatalog(images, false, null)) },
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
