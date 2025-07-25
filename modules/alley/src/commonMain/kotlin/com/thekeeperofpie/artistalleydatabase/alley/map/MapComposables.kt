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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.Popup
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_catalog_image
import artistalleydatabase.modules.alley.generated.resources.alley_favorite_icon_content_description
import coil3.compose.AsyncImage
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.favorite.UnfavoriteDialog
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
        table.section?.color ?: MaterialTheme.colorScheme.primaryContainer
    }.copy(alpha = 0.25f).compositeOver(MaterialTheme.colorScheme.surface),
    borderWidth: Dp = 1.dp,
    borderColor: Color = MaterialTheme.colorScheme.onSurface,
    textColor: Color = if (table.image != null) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        table.section?.textColor?.copy(alpha = 0.33f)
            ?.compositeOver(MaterialTheme.colorScheme.onSurface)
            ?: MaterialTheme.colorScheme.onPrimaryContainer
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

        var unfavoriteDialogEntry by remember {
            mutableStateOf<ArtistEntryGridModel?>(null)
        }

        if (showPopup) {
            BackHandler { showPopup = false }
            var tableEntries by remember { mutableStateOf<List<ArtistEntryGridModel>?>(null) }
            LaunchedEffect(table) {
                tableEntries = withContext(CustomDispatchers.IO) {
                    when (table) {
                        is Table.Single ->
                            mapViewModel.tableEntry(table.year, table.artistId)?.let { listOf(it) }
                        is Table.Shared -> table.artistIds.mapNotNull {
                            mapViewModel.tableEntry(table.year, it)
                        }.ifEmpty { null }
                    }

                }
            }
            Popup(
                alignment = Alignment.TopCenter,
                onDismissRequest = { showPopup = false },
            ) {
                TablePopup(
                    table = table,
                    entries = tableEntries,
                    onFavoriteToggle = { entry, favorite ->
                        if (favorite) {
                            entry.favorite = favorite
                            mapViewModel.onFavoriteToggle(entry, favorite)
                        } else {
                            unfavoriteDialogEntry = entry
                        }
                    },
                    onIgnoredToggle = { entry, ignored ->
                        entry.ignored = ignored
                        mapViewModel.onIgnoredToggle(entry, ignored)
                    },
                    onClick = onArtistClick,
                    modifier = Modifier.sizeIn(maxWidth = 300.dp)
                )
            }
        }

        UnfavoriteDialog(
            entry = { unfavoriteDialogEntry },
            onClearEntry = { unfavoriteDialogEntry = null },
            onRemoveFavorite = {
                unfavoriteDialogEntry?.favorite = false
                mapViewModel.onFavoriteToggle(it, false)
            },
        )

        Text(
            autoSize = TextAutoSize.StepBased(
                minFontSize = 8.sp,
                maxFontSize = LocalTextStyle.current.fontSize,
            ),
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
    defaultBackground: Color = if (table.image != null) {
        MaterialTheme.colorScheme.primary
    } else {
        table.section?.color ?: MaterialTheme.colorScheme.primaryContainer
    }.copy(alpha = 0.25f).compositeOver(MaterialTheme.colorScheme.surface),
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
        defaultBackground
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
    entries: List<ArtistEntryGridModel>?,
    onFavoriteToggle: (ArtistEntryGridModel, Boolean) -> Unit,
    onIgnoredToggle: (ArtistEntryGridModel, Boolean) -> Unit,
    onClick: (ArtistEntryGridModel, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        if (entries == null) {
            CircularProgressIndicator()
        } else {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                when (table) {
                    is Table.Single -> SingleTablePopup(
                        entry = entries.single(),
                        imageIndex = table.imageIndex,
                        onFavoriteToggle = onFavoriteToggle,
                        onIgnoredToggle = onIgnoredToggle,
                        onClick = onClick,
                    )
                    is Table.Shared ->
                        entries.forEach {
                            SingleTablePopup(
                                entry = it,
                                imageIndex = null,
                                onFavoriteToggle = onFavoriteToggle,
                                onIgnoredToggle = onIgnoredToggle,
                                onClick = onClick,
                            )
                        }
                }
            }
        }
    }
}

@Composable
fun SingleTablePopup(
    entry: ArtistEntryGridModel,
    imageIndex: Int?,
    onFavoriteToggle: (ArtistEntryGridModel, Boolean) -> Unit,
    onIgnoredToggle: (ArtistEntryGridModel, Boolean) -> Unit,
    onClick: (ArtistEntryGridModel, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ignored = entry.ignored
    val imagesSize = entry.images.size
    val pagerState = rememberPagerState(
        initialPage = imageIndex?.coerceAtMost(imagesSize) ?: 0,
        pageCount = { imagesSize },
    )
    Column(
        modifier = modifier
            .alpha(if (entry.ignored) 0.38f else 1f)
            .combinedClickable(
                onClick = {
                    // This is a terrible hack, but DetailsScreen shows a grid in index 0
                    // while MapScreen does not, so the image index needs to be 1 higher
                    // to line up correctly when opening DetailsScreen
                    onClick(entry, pagerState.settledPage + 1)
                },
                onLongClick = { onIgnoredToggle(entry, !ignored) }
            )
    ) {
        val images = entry.images
        if (images.isNotEmpty()) {
            var minHeight by remember { mutableIntStateOf(0) }
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
            IconButton(onClick = { onFavoriteToggle(entry, !favorite) }) {
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
