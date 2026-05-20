package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalFlexBoxApi
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexBoxConfig
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_expand_series
import artistalleydatabase.modules.alley.generated.resources.alley_favorite_icon_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_unfavorite_dialog_text
import coil3.compose.AsyncImage
import com.anilist.data.type.MediaType
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.alley.favorite.UnfavoriteDialog
import com.thekeeperofpie.artistalleydatabase.alley.links.Logo
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.models.AniListType
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.series.name
import com.thekeeperofpie.artistalleydatabase.alley.series.otherTitles
import com.thekeeperofpie.artistalleydatabase.alley.ui.UnrecognizedTagIcon
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListDataUtils
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.filled.Book
import com.thekeeperofpie.artistalleydatabase.icons.filled.Favorite
import com.thekeeperofpie.artistalleydatabase.icons.filled.FavoriteBorder
import com.thekeeperofpie.artistalleydatabase.icons.filled.Link
import com.thekeeperofpie.artistalleydatabase.icons.filled.Monitor
import com.thekeeperofpie.artistalleydatabase.utils_compose.GridUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.ThemeAwareElevatedCard
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.fadingEdgeBottom
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImage
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.request
import com.thekeeperofpie.artistalleydatabase.utils_compose.optionalClickable
import org.jetbrains.compose.resources.stringResource

@Composable
fun SeriesRow(
    data: SeriesWithUserData?,
    image: () -> String?,
    onFavoriteToggle: (Boolean) -> Unit,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    showAllTitles: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    SeriesRow(
        series = data?.series,
        image = image,
        favoritesButton = {
            val languageOptionMedia = LocalLanguageOptionMedia.current
            FavoriteIconButton(
                favorite = data?.userEntry?.favorite,
                onFavoriteToggle = onFavoriteToggle,
                title = data?.series?.name(languageOptionMedia),
            )
        },
        onClick = onClick,
        showAllTitles = showAllTitles,
        textStyle = textStyle,
        modifier = modifier,
    )
}

@Composable
fun SeriesRow(
    series: SeriesInfo?,
    image: () -> String?,
    favoritesButton: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    showAllTitles: Boolean = false,
    showUnknownIndicator: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .optionalClickable(onClick)
            .height(IntrinsicSize.Min)
    ) {
        favoritesButton?.invoke()

        Box {
            AsyncImage(
                model = image(),
                null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxHeight()
                    .width(56.dp)
                    .height(80.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            if (series?.tmdbId != null && series.tmdbType != null) {
                Icon(
                    imageVector = Logo.TMDB.icon,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.BottomEnd)
                        .padding(2.dp)
                )
            }
        }

        // For highlighting tag that needs resolution in edit app
        if (series?.faked == true && showUnknownIndicator) {
            UnrecognizedTagIcon()
        }

        Column(
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .placeholder(
                    visible = series == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        ) {
            val languageOptionMedia = LocalLanguageOptionMedia.current
            val name = series?.name(languageOptionMedia).orEmpty()
            Text(
                text = name,
                color = textStyle.color.takeOrElse { MaterialTheme.colorScheme.secondary },
                style = textStyle,
            )

            if (showAllTitles) {
                val otherTitles = series?.otherTitles(languageOptionMedia)
                if (!otherTitles.isNullOrEmpty()) {
                    Text(
                        text = otherTitles.joinToString(separator = " / "),
                        style = textStyle,
                        modifier = Modifier.padding(start = 32.dp)
                    )
                }
            }
        }

        val uriHandler = LocalUriHandler.current
        if (series?.aniListId != null) {
            val mediaType = when (series.aniListType) {
                AniListType.NONE -> MediaType.UNKNOWN__
                AniListType.ANIME -> MediaType.ANIME
                AniListType.MANGA -> MediaType.MANGA
            }
            val icon = when (series.aniListType) {
                AniListType.NONE -> Icons.Default.Monitor
                AniListType.ANIME -> Icons.Default.Monitor
                AniListType.MANGA -> Icons.Default.Book
            }
            val aniListUrl = AniListDataUtils.mediaUrl(mediaType, series.aniListId.toString())
            TooltipIconButton(
                icon = icon,
                tooltipText = aniListUrl,
                onClick = { uriHandler.openUri(aniListUrl) },
            )
        }

        val link = series?.resolvedLink
        if (link != null) {
            TooltipIconButton(
                icon = Icons.Default.Link,
                tooltipText = link,
                onClick = { uriHandler.openUri(link) },
            )
        }
    }
}

@Composable
fun MerchRow(
    merch: String?,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = merch.orEmpty(),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .optionalClickable(onClick = onClick.takeIf { expanded })
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .placeholder(
                visible = merch == null,
                highlight = PlaceholderHighlight.shimmer(),
            )
    )
}

@Composable
fun MerchRow(
    data: MerchWithUserData?,
    onFavoriteToggle: (Boolean) -> Unit,
    showNotes: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .optionalClickable(onClick)
            .padding(end = 16.dp, top = 8.dp, bottom = 8.dp)
    ) {
        val merch = data?.merch
        FavoriteIconButton(
            favorite = data?.userEntry?.favorite,
            onFavoriteToggle = onFavoriteToggle,
            title = merch?.name,
        )

        Column {
            Text(
                text = merch?.name.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .placeholder(
                        visible = merch == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
            if (showNotes && (merch == null || !merch.notes.isNullOrEmpty())) {
                Text(
                    text = merch?.notes.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .placeholder(
                            visible = merch == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                )
            }
        }
    }
}

@Composable
private fun FavoriteIconButton(
    favorite: Boolean?,
    onFavoriteToggle: (Boolean) -> Unit,
    title: String?,
) {
    var showUnfavoriteDialog by remember { mutableStateOf(false) }
    IconButton(
        onClick = {
            if (favorite == true) {
                showUnfavoriteDialog = true
            } else {
                onFavoriteToggle(true)
            }
        },
        enabled = favorite != null,
    ) {
        Icon(
            imageVector = if (favorite == true) {
                Icons.Filled.Favorite
            } else {
                Icons.Filled.FavoriteBorder
            },
            contentDescription = stringResource(
                Res.string.alley_favorite_icon_content_description
            ),
        )
    }

    if (showUnfavoriteDialog) {
        UnfavoriteDialog(
            text = stringResource(Res.string.alley_unfavorite_dialog_text, title.orEmpty()),
            onDismissRequest = { showUnfavoriteDialog = false },
            onRemoveFavorite = { onFavoriteToggle(false) },
        )
    }
}

fun LazyGridScope.series(
    key: String,
    series: List<SeriesWithUserData>,
    image: (SeriesWithUserData) -> String?,
    columnCount: Int,
    randomizedIndexes: List<Int>,
    expanded: () -> Boolean,
    onExpanded: () -> Unit,
    onClick: (SeriesWithUserData) -> Unit,
) {
    val canExpand = series.size > (2 * columnCount)
    items(
        count = if (expanded() || !canExpand) {
            series.size
        } else {
            (columnCount * 2).coerceAtMost(series.size)
        },
        key = { "$key-series-${series[it].series.id}" },
    ) {
        val expanded = expanded() || !canExpand
        val series = series[if (expanded) it else randomizedIndexes[it]]
        val imageState = rememberCoilImageState(image(series))
        if (!expanded && it >= columnCount) {
            FadedSeriesCard(
                series = series,
                imageState = imageState,
                onClick = onClick,
                modifier = Modifier.animateItem()
            )
        } else {
            NormalSeriesCard(
                series = series,
                imageState = imageState,
                onClick = onClick,
                modifier = Modifier.animateItem()
            )
        }
    }

    if (!expanded() && canExpand) {
        item("$key-seriesExpand", GridUtils.maxSpanFunction) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                FilledTonalButton(onClick = onExpanded) {
                    Text(stringResource(Res.string.alley_expand_series))
                }
            }
        }
    }
}

@Composable
private fun NormalSeriesCard(
    series: SeriesWithUserData,
    imageState: CoilImageState,
    onClick: (SeriesWithUserData) -> Unit,
    modifier: Modifier = Modifier,
) {
    ThemeAwareElevatedCard(onClick = { onClick(series) }, modifier = modifier) {
        val colors = imageState.colors
        val containerColor = colors.containerColor
            .takeOrElse { MaterialTheme.colorScheme.surfaceVariant }
        Column(modifier = Modifier.background(containerColor)) {
            CoilImage(
                state = imageState,
                model = imageState.request().build(),
                contentScale = ContentScale.Crop,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.66f)
            )
            val languageOptionMedia = LocalLanguageOptionMedia.current
            val name = series.series.name(languageOptionMedia)
            val textColor = colors.textColor
                .takeOrElse { MaterialTheme.colorScheme.onSurfaceVariant }
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMediumEmphasized,
                color = textColor,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun FadedSeriesCard(
    series: SeriesWithUserData,
    imageState: CoilImageState,
    onClick: (SeriesWithUserData) -> Unit,
    modifier: Modifier = Modifier,
) {
    ThemeAwareElevatedCard(onClick = { onClick(series) }, modifier = modifier.fadingEdgeBottom(firstStop = 0.5f)) {
        val containerColor = imageState.colors.containerColor
            .takeOrElse { MaterialTheme.colorScheme.surfaceVariant }
        CoilImage(
            state = imageState,
            model = imageState.request().build(),
            contentScale = ContentScale.Crop,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.66f)
                .background(containerColor)
        )
    }
}

@OptIn(ExperimentalFlexBoxApi::class)
@Composable
fun MerchChips(
    merch: List<String>,
    onClick: (String) -> Unit,
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 32.dp) {
        FlexBox(
            config = FlexBoxConfig {
                wrap(FlexWrap.Wrap)
                gap(8.dp)
            },
        ) {
            merch.forEach {
                AssistChip(label = { Text(it) }, onClick = { onClick(it) })
            }
        }
    }
}
