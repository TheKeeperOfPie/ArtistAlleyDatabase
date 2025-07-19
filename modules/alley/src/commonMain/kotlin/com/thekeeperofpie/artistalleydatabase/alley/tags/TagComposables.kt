package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_favorite_icon_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_unfavorite_dialog_text
import coil3.compose.AsyncImage
import com.anilist.data.type.MediaType
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.favorite.UnfavoriteDialog
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.ui.IconButtonWithTooltip
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListDataUtils
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
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
    series: SeriesEntry?,
    image: () -> String?,
    favoritesButton: @Composable () -> Unit = {},
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    showAllTitles: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .optionalClickable(onClick)
            .height(IntrinsicSize.Min)
    ) {
        favoritesButton()

        AsyncImage(
            model = image(),
            null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxHeight()
                .width(56.dp)
                .height(80.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        val languageOptionMedia = LocalLanguageOptionMedia.current
        val title = if (showAllTitles && series != null) {
            val colorScheme = MaterialTheme.colorScheme
            remember(series, languageOptionMedia, colorScheme) {
                val name = series.name(languageOptionMedia)
                val otherTitles = listOf(
                    series.titlePreferred,
                    series.titleEnglish,
                    series.titleRomaji,
                    series.titleNative,
                ).distinct() - name
                buildAnnotatedString {
                    withStyle(SpanStyle(color = colorScheme.secondary)) {
                        append(name)
                    }
                    if (otherTitles.isNotEmpty()) {
                        otherTitles.forEach {
                            append(" / ")
                            append(it)
                        }
                    }
                }
            }
        } else {
            buildAnnotatedString {
                append(series?.name(languageOptionMedia))
            }
        }
        Text(
            text = title,
            style = textStyle,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .placeholder(
                    visible = series == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )

        val uriHandler = LocalUriHandler.current
        if (series?.aniListId != null) {
            val mediaType = when (series.aniListType) {
                "ANIME" -> MediaType.ANIME
                "MANGA" -> MediaType.MANGA
                else -> MediaType.UNKNOWN__
            }
            val icon = when (series.aniListType) {
                "ANIME" -> Icons.Default.Monitor
                "MANGA" -> Icons.Default.Book
                else -> Icons.Default.Monitor
            }
            val aniListUrl = AniListDataUtils.mediaUrl(mediaType, series.aniListId.toString())
            IconButtonWithTooltip(
                imageVector = icon,
                tooltipText = aniListUrl,
                onClick = { uriHandler.openUri(aniListUrl) },
                allowPopupHover = false,
            )
        }

        if (series?.link != null) {
            IconButtonWithTooltip(
                imageVector = Icons.Default.Link,
                tooltipText = series.link,
                onClick = { uriHandler.openUri(series.link) },
                allowPopupHover = false,
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
