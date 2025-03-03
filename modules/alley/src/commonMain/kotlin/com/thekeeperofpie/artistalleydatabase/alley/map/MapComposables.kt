@file:OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.map

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.Popup
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_catalog_image
import artistalleydatabase.modules.alley.generated.resources.alley_favorite_icon_content_description
import coil3.compose.AsyncImage
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.ui.HorizontalPagerIndicator
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource

@Composable
fun TableCell(
    mapViewModel: MapViewModel,
    table: Table,
    background: Color = if (table.image != null) {
        MaterialTheme.colorScheme.primary
    } else {
        table.section.color.copy(alpha = 0.25f).compositeOver(MaterialTheme.colorScheme.surface)
    },
    borderWidth: Dp = 1.dp,
    borderColor: Color = MaterialTheme.colorScheme.onSurface,
    textColor: Color = if (table.image != null) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        table.section.textColor.copy(alpha = 0.33f)
            .compositeOver(MaterialTheme.colorScheme.onSurface)
    },
    showImages: Boolean = true,
    onArtistClick: (ArtistEntryGridModel, Int) -> Unit,
) {
    var showPopup by remember { mutableStateOf(false) }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp)
            .background(background)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RectangleShape,
            )
            .clickable { showPopup = true }
    ) {
        val imageUri = table.image?.uri
        if (showImages && imageUri != null) {
            BoxWithConstraints {
                val minSize = LocalDensity.current.run { 80.dp }
                if (maxWidth > minSize && maxHeight > minSize) {
                    AsyncImage(
                        model = imageUri,
                        contentScale = ContentScale.FillWidth,
                        fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                        contentDescription = stringResource(Res.string.alley_artist_catalog_image),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        if (showPopup) {
            BackHandler { showPopup = false }
            var tableEntry by remember { mutableStateOf<ArtistEntryGridModel?>(null) }
            LaunchedEffect(table) {
                tableEntry = withContext(CustomDispatchers.IO) {
                    mapViewModel.tableEntry(table)
                }
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
                        table = table,
                        entry = entry,
                        onFavoriteToggle = {
                            entry.favorite = it
                            mapViewModel.onFavoriteToggle(entry, it)
                        },
                        onIgnoredToggle = {
                            entry.ignored = it
                            mapViewModel.onIgnoredToggle(entry, it)
                        },
                        onClick = onArtistClick,
                        modifier = Modifier.sizeIn(maxWidth = 300.dp)
                    )
                }
            }
        }
        Text(
            text = table.booth,
            color = textColor,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun HighlightedTableCell(
    mapViewModel: MapViewModel,
    table: Table,
    highlight: Boolean,
    showImages: Boolean = true,
    onArtistClick: (ArtistEntryGridModel, Int) -> Unit,
) {
    val borderWidth = if (highlight) 2.dp else 1.dp
    val borderColor = if (highlight) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val background = if (highlight) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }
    val textColor = if (highlight) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    TableCell(
        mapViewModel = mapViewModel,
        table = table,
        background = background,
        borderWidth = borderWidth,
        borderColor = borderColor,
        textColor = textColor,
        showImages = showImages,
        onArtistClick = onArtistClick,
    )
}

@Composable
fun TablePopup(
    table: Table,
    entry: ArtistEntryGridModel?,
    onFavoriteToggle: (Boolean) -> Unit,
    onIgnoredToggle: (Boolean) -> Unit,
    onClick: (ArtistEntryGridModel, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ignored = entry?.ignored ?: false
    val imagesSize = entry?.images?.size ?: 0
    val pagerState = rememberPagerState(
        initialPage = table.imageIndex?.coerceAtMost(imagesSize) ?: 0,
        pageCount = { imagesSize },
    )
    OutlinedCard(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = {
                    entry?.let {
                        // This is a terrible hack, but DetailsScreen shows a grid in index 0
                        // while MapScreen does not, so the image index needs to be 1 higher
                        // to line up correctly when opening DetailsScreen
                        onClick(it, pagerState.settledPage + 1)
                    }
                },
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
                            contentDescription = stringResource(Res.string.alley_artist_catalog_image),
                            modifier = Modifier
                                .clickable {
                                    // See above comment about hack
                                    onClick(entry, pagerState.settledPage + 1)
                                }
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
            val booth = entry.artist.booth
            if (booth != null) {
                Text(
                    text = booth,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            Text(
                text = entry.artist.name,
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
                        Res.string.alley_favorite_icon_content_description
                    ),
                )
            }
        }
    }
}
