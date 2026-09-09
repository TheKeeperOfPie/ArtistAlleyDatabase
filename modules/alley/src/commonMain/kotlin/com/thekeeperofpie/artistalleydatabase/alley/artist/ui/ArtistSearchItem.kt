package com.thekeeperofpie.artistalleydatabase.alley.artist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistWithUserDataProvider
import com.thekeeperofpie.artistalleydatabase.alley.images.ImagePager
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchItemCard
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchItemImage
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchScreen.DisplayType
import com.thekeeperofpie.artistalleydatabase.alley.search.rememberSearchPagerState
import com.thekeeperofpie.artistalleydatabase.alley.series.ui.SeriesRow
import com.thekeeperofpie.artistalleydatabase.alley.ui.FavoriteIconButton
import com.thekeeperofpie.artistalleydatabase.alley.ui.ImageFallbackBanner
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedBounds
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.ImageWithDimensions
import com.thekeeperofpie.artistalleydatabase.utils_compose.border
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_preview.AlleyPreview

@Composable
fun ArtistSearchItem(
    displayType: DisplayType,
    artistWithUserData: ArtistWithUserData,
    showGridByDefault: Boolean,
    showRandomCatalogImage: Boolean,
    blockCrossAxisScrolling: () -> Boolean,
    showOutdatedCatalogs: () -> Boolean,
    onFavoriteToggle: (Boolean) -> Unit,
    onIgnoredToggle: (Boolean) -> Unit,
    onClick: (imageIndex: Int) -> Unit,
    onClickFullscreen: (imageIndex: Int) -> Unit,
    tagRow: @Composable () -> Unit,
) {
    val artist = artistWithUserData.artist
    val userEntry = artistWithUserData.userEntry
    val sharedElementId = artist.id
    val showingFallback = artist.images.isEmpty() &&
            artistWithUserData.fallbackImages.isNotEmpty() &&
            showOutdatedCatalogs()
    val images = if (showingFallback) {
        artistWithUserData.fallbackImages
    } else {
        artistWithUserData.images
    }
    val pagerState = rememberSearchPagerState(
        entryId = artist.id,
        images = images,
        showGridByDefault = showGridByDefault,
        showRandomCatalogImage = showRandomCatalogImage,
    )
    val ignored = userEntry.ignored
    when (displayType) {
        DisplayType.LIST -> {
            ArtistListRow(
                artistWithUserData = artistWithUserData,
                onFavoriteToggle = onFavoriteToggle,
                tagRow = tagRow,
                modifier = Modifier
                    .sharedBounds("itemContainer", sharedElementId)
                    .combinedClickable(
                        onClick = { onClick(1) },
                        onLongClick = { onIgnoredToggle(!ignored) }
                    )
                    .alpha(if (ignored) 0.38f else 1f)
                    .border(
                        width = 1.dp,
                        color = DividerDefaults.color,
                        start = true,
                        bottom = true,
                    )
            )
        }
        DisplayType.CARD ->
            SearchItemCard(
                ignored = ignored,
                onIgnoredToggle = onIgnoredToggle,
                onClick = { onClick(pagerState.settledPage) },
                modifier = Modifier.sharedBounds("itemContainer", sharedElementId)
            ) {
                ArtistImagePager(
                    pagerState = pagerState,
                    images = images,
                    ignored = ignored,
                    fallbackYear = artist.fallbackImageYear,
                    sharedElementId = sharedElementId,
                    blockCrossAxisScrolling = blockCrossAxisScrolling,
                    showingFallback = showingFallback,
                    onClick = onClick,
                    onClickFullscreen = onClickFullscreen,
                )
                ArtistListRow(
                    artistWithUserData = artistWithUserData,
                    onFavoriteToggle = onFavoriteToggle,
                    tagRow = tagRow,
                )
            }
        DisplayType.IMAGE -> {
            SearchItemImage(
                images = images,
                ignored = ignored,
                onIgnoredToggle = onIgnoredToggle,
                onClick = { onClick(pagerState.settledPage) },
                imagePager = {
                    Box {
                        ArtistImagePager(
                            pagerState = pagerState,
                            images = images,
                            ignored = ignored,
                            fallbackYear = artist.fallbackImageYear,
                            sharedElementId = sharedElementId,
                            blockCrossAxisScrolling = blockCrossAxisScrolling,
                            showingFallback = showingFallback,
                            clipCorners = false,
                            onClick = onClick,
                            onClickFullscreen = onClickFullscreen,
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(topEnd = 12.dp)
                                )
                        ) {
                            val booth = artist.booth
                            if (booth != null) {
                                Text(
                                    text = booth,
                                    style = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                                    modifier = Modifier
                                        .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                                        .sharedElement(
                                            "booth",
                                            sharedElementId,
                                            zIndexInOverlay = 1f
                                        )
                                )
                            }

                            FavoriteIconButton(
                                entryText = { artist.name },
                                favorite = { userEntry.favorite },
                                onFavoriteToggle = onFavoriteToggle,
                                modifier = Modifier
                                    .sharedElement(
                                        "favorite",
                                        sharedElementId,
                                        zIndexInOverlay = 1f
                                    )
                            )
                        }
                    }
                },
                noImageContent = {
                    ArtistListRow(
                        artistWithUserData = artistWithUserData,
                        onFavoriteToggle = onFavoriteToggle,
                        tagRow = null,
                    )
                },
                modifier = Modifier.sharedBounds("itemContainer", sharedElementId),
            )
        }
        DisplayType.TABLE -> throw IllegalArgumentException()
    }
}

@Composable
private fun ArtistImagePager(
    pagerState: PagerState,
    images: List<ImageWithDimensions>,
    ignored: Boolean,
    fallbackYear: DataYear?,
    sharedElementId: Any,
    blockCrossAxisScrolling: () -> Boolean,
    showingFallback: Boolean,
    onClick: ((Int) -> Unit)?,
    onClickFullscreen: ((index: Int) -> Unit)?,
    modifier: Modifier = Modifier,
    clipCorners: Boolean = true,
) {
    if (images.isNotEmpty() && !ignored) {
        Column(modifier = modifier) {
            ImagePager(
                images = images,
                pagerState = pagerState,
                sharedElementId = sharedElementId,
                blockCrossAxisScrolling = blockCrossAxisScrolling,
                onClickPage = onClick,
                onClickFullscreen = onClickFullscreen,
                clipCorners = clipCorners,
                modifier = Modifier.conditionally(
                    showingFallback,
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                    )
                )
            )
            if (showingFallback) {
                ImageFallbackBanner(
                    sharedElementId = sharedElementId,
                    fallbackYear = fallbackYear!!,
                )
            }
        }
    }
}

@Composable
private fun Preview(displayType: DisplayType) {
    val artistWithUserData = ArtistWithUserDataProvider.values.first()
    ArtistSearchItem(
        displayType = displayType,
        artistWithUserData = artistWithUserData,
        showGridByDefault = false,
        showRandomCatalogImage = false,
        blockCrossAxisScrolling = { false },
        showOutdatedCatalogs = { true },
        onFavoriteToggle = {},
        onIgnoredToggle = {},
        onClick = {},
        onClickFullscreen = {},
        tagRow = {
            SeriesRow(
                series = emptyList(),
                onSeriesClick = {},
                onMoreClick = {},
                modifier = Modifier.padding(start = 12.dp)
            )
        },
    )
}

@AlleyPreview
@Composable
private fun ArtistSearchItemCardPreview() = Preview(DisplayType.CARD)

@AlleyPreview
@Composable
private fun ArtistSearchItemImagePreview() = Preview(DisplayType.IMAGE)

@AlleyPreview
@Composable
private fun ArtistSearchItemListPreview() = Preview(DisplayType.LIST)
