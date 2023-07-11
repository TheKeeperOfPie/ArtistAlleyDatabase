package com.thekeeperofpie.artistalleydatabase.anime.user

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Dimension
import com.anilist.UserSearchQuery.Data.Page.User
import com.anilist.fragment.MediaNavigationData
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.thekeeperofpie.artistalleydatabase.android_utils.MutableSingle
import com.thekeeperofpie.artistalleydatabase.android_utils.getValue
import com.thekeeperofpie.artistalleydatabase.android_utils.setValue
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio

@OptIn(ExperimentalFoundationApi::class)
object UserListRow {

    @Composable
    operator fun invoke(
        entry: Entry,
        onLongPressImage: (Entry) -> Unit = {},
        colorCalculationState: ColorCalculationState = ColorCalculationState(),
        navigationCallback: AnimeNavigator.NavigationCallback =
            AnimeNavigator.NavigationCallback(null),
    ) {
        var imageWidthToHeightRatio by remember { MutableSingle(1f) }
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp)
                .clickable(
                    enabled = true, // TODO: placeholder,
                    onClick = {
                        navigationCallback.onUserClick(
                            entry.user,
                            imageWidthToHeightRatio,
                        )
                    },
                )
        ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                UserImage(
                    entry = entry,
                    onClick = {
                        navigationCallback.onUserClick(
                            entry.user,
                            imageWidthToHeightRatio,
                        )
                    },
                    onLongPressImage = { onLongPressImage(entry) },
                    colorCalculationState = colorCalculationState,
                    onRatioAvailable = { imageWidthToHeightRatio = it }
                )

                Column(
                    modifier = Modifier
                        .heightIn(min = 180.dp)
                        .padding(bottom = 12.dp)
                ) {
                    NameText(
                        entry = entry,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(Alignment.Top)
                    )

                    Spacer(Modifier.weight(1f))

                    MediaRow(entry = entry, onMediaClick = navigationCallback::onMediaClick)
                }
            }
        }
    }

    @Composable
    private fun UserImage(
        entry: Entry,
        onClick: () -> Unit = {},
        onLongPressImage: () -> Unit,
        colorCalculationState: ColorCalculationState,
        onRatioAvailable: (Float) -> Unit,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(entry.user.avatar?.large)
                .crossfade(true)
                .allowHardware(colorCalculationState.hasColor(entry.user.id.toString()))
                .size(
                    width = Dimension.Pixels(LocalDensity.current.run { 130.dp.roundToPx() }),
                    height = Dimension.Undefined
                )
                .build(),
            contentScale = ContentScale.Crop,
            fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
            onSuccess = {
                onRatioAvailable(it.widthToHeightRatio())
                ComposeColorUtils.calculatePalette(
                    entry.user.id.toString(),
                    it,
                    colorCalculationState,
                )
            },
            contentDescription = stringResource(R.string.anime_user_image),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .fillMaxHeight()
                .heightIn(min = 180.dp)
                .width(130.dp)
                .placeholder(
                    visible = false, // TODO: placeholder,
                    highlight = PlaceholderHighlight.shimmer(),
                )
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongPressImage,
                    onLongClickLabel = stringResource(
                        R.string.anime_user_image_long_press_preview
                    ),
                )
        )
    }

    @Composable
    private fun NameText(entry: Entry, modifier: Modifier = Modifier) {
        AutoHeightText(
            text = entry.user.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            modifier = modifier
                .padding(start = 12.dp, top = 10.dp, end = 16.dp)
                .placeholder(
                    visible = false, // TODO: placeholder
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
    }

    @Composable
    private fun MediaRow(
        entry: Entry,
        onMediaClick: (MediaNavigationData, imageWidthToHeightRatio: Float) -> Unit
    ) {
        val media = entry.media.takeIf { it.isNotEmpty() } ?: return
        val context = LocalContext.current
        val density = LocalDensity.current
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                // SubcomposeLayout doesn't support fill max width, so use a really large number.
                // The parent will clamp the actual width so all content still fits on screen.
                .size(width = LocalConfiguration.current.screenWidthDp.dp, height = 96.dp)
        ) {
            items(media, key = { it.id }) {
                var imageWidthToHeightRatio by remember { mutableStateOf<Float?>(null) }
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(it.coverImage?.extraLarge)
                        .size(
                            width = density.run { 64.dp.roundToPx() },
                            height = density.run { 96.dp.roundToPx() },
                        )
                        .crossfade(true)
                        .build(),
                    contentScale = ContentScale.Crop,
                    contentDescription = stringResource(
                        R.string.anime_media_cover_image_content_description
                    ),
                    onSuccess = { imageWidthToHeightRatio = it.widthToHeightRatio() },
                    modifier = Modifier
                        .size(width = 64.dp, height = 96.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
                        .clickable { onMediaClick(it, imageWidthToHeightRatio ?: 1f) }
                        .placeholder(
                            visible = imageWidthToHeightRatio == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                )
            }
        }
    }

    open class Entry(val user: User) {
        val anime = user.favourites?.anime?.edges
            ?.filterNotNull()
            ?.sortedBy { it.favouriteOrder }
            ?.mapNotNull { it.node }
            .orEmpty()

        val manga = user.favourites?.manga?.edges
            ?.filterNotNull()
            ?.sortedBy { it.favouriteOrder }
            ?.mapNotNull { it.node }
            .orEmpty()

        val media = (anime + manga).distinctBy { it.id }
    }
}
