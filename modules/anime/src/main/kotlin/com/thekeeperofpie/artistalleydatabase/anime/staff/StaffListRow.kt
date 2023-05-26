package com.thekeeperofpie.artistalleydatabase.anime.staff

import android.content.Context
import androidx.annotation.StringRes
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
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Dimension
import com.anilist.StaffSearchQuery.Data.Page.Staff
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
object StaffListRow {

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
                        navigationCallback.onStaffClick(
                            entry.staff,
                            imageWidthToHeightRatio,
                        )
                    },
                )
        ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                StaffImage(
                    entry = entry,
                    onClick = {
                        navigationCallback.onStaffClick(
                            entry.staff,
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
                    Row(Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            NameText(entry = entry)
                            OccupationsText(entry = entry)
                        }

                        StatsSection(entry = entry)
                    }

                    Spacer(Modifier.weight(1f))

                    CharactersAndMediaRow(
                        entry = entry,
                        navigationCallback = navigationCallback,
                    )
                }
            }
        }
    }

    @Composable
    private fun StaffImage(
        entry: Entry,
        onClick: () -> Unit = {},
        onLongPressImage: () -> Unit,
        colorCalculationState: ColorCalculationState,
        onRatioAvailable: (Float) -> Unit,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(entry.staff.image?.large)
                .crossfade(true)
                .allowHardware(colorCalculationState.hasColor(entry.staff.id.toString()))
                .size(
                    width = Dimension.Pixels(LocalDensity.current.run { 130.dp.roundToPx() }),
                    height = Dimension.Undefined,
                )
                .build(),
            contentScale = ContentScale.Crop,
            fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
            onSuccess = {
                onRatioAvailable(it.widthToHeightRatio())
                ComposeColorUtils.calculatePalette(
                    entry.staff.id.toString(),
                    it,
                    colorCalculationState,
                )
            },
            contentDescription = stringResource(R.string.anime_staff_image),
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
                        R.string.anime_staff_image_long_press_preview
                    ),
                )
        )
    }

    @Composable
    private fun NameText(entry: Entry, modifier: Modifier = Modifier) {
        AutoHeightText(
            text = entry.staff.name?.userPreferred ?: "Loading...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(Alignment.Top)
                .padding(start = 12.dp, top = 10.dp, end = 16.dp)
                .placeholder(
                    visible = false, // TODO: placeholder
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
    }

    @Composable
    private fun OccupationsText(entry: Entry) {
        if (entry.occupations.isEmpty()) return
        Text(
            text = entry.occupations.joinToString(separator = " - "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.typography.bodySmall.color
                .takeOrElse { LocalContentColor.current }
                .copy(alpha = 0.8f),
            modifier = Modifier
                .wrapContentHeight()
                .padding(start = 12.dp, top = 4.dp, end = 16.dp, bottom = 10.dp)
                .placeholder(
                    visible = false, // TODO: placeholder,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
    }

    @Composable
    private fun StatsSection(
        entry: Entry,
    ) {
        val favorites = entry.staff.favourites ?: return
        val loading = false // TODO: placeholder
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(24.dp)
                    .placeholder(
                        visible = loading,
                        highlight = PlaceholderHighlight.shimmer(),
                    ),
            ) {
                AutoHeightText(
                    text = favorites.toString(),
                    style = MaterialTheme.typography.labelLarge,
                )

                Icon(
                    imageVector = when {
                        favorites > 2000 -> Icons.Filled.PeopleAlt
                        favorites > 1000 -> Icons.Outlined.PeopleAlt
                        favorites > 100 -> Icons.Filled.Person
                        else -> Icons.Filled.PersonOutline
                    },
                    contentDescription = stringResource(
                        R.string.anime_staff_favorites_icon_content_description
                    ),
                )
            }
        }
    }

    @Composable
    private fun CharactersAndMediaRow(
        entry: Entry,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        val media = entry.media
        val characters = entry.characters
        if (media.isEmpty() && characters.isEmpty()) return
        val context = LocalContext.current
        val density = LocalDensity.current
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                // SubcomposeLayout doesn't support fill max width, so use a really large number.
                // The parent will clamp the actual width so all content still fits on screen.
                .size(width = 39393.dp, height = 96.dp)
        ) {
            items(characters, key = { it.id }) {
                CharacterOrMediaImage(
                    context = context,
                    density = density,
                    image = it.image?.large,
                    contentDescriptionTextRes = R.string.anime_character_image_content_description,
                    onClick = { ratio -> navigationCallback.onCharacterClick(it, ratio) }
                )
            }

            items(media, key = { it.id }) {
                CharacterOrMediaImage(
                    context = context,
                    density = density,
                    image = it.coverImage?.extraLarge,
                    contentDescriptionTextRes = R.string.anime_media_cover_image_content_description,
                    onClick = { ratio -> navigationCallback.onMediaClick(it, ratio) }
                )
            }
        }
    }

    @Composable
    private fun CharacterOrMediaImage(
        context: Context,
        density: Density,
        image: String?,
        @StringRes contentDescriptionTextRes: Int,
        onClick: (imageWidthToHeightRatio: Float) -> Unit,
    ) {
        var imageWidthToHeightRatio by remember { mutableStateOf<Float?>(null) }
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(image)
                .size(
                    width = density.run { 64.dp.roundToPx() },
                    height = density.run { 96.dp.roundToPx() },
                )
                .crossfade(true)
                .build(),
            contentScale = ContentScale.Crop,
            contentDescription = stringResource(
                contentDescriptionTextRes

            ),
            onSuccess = { imageWidthToHeightRatio = it.widthToHeightRatio() },
            modifier = Modifier
                .size(width = 64.dp, height = 96.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
                .clickable { onClick(imageWidthToHeightRatio ?: 1f) }
                .placeholder(
                    visible = imageWidthToHeightRatio == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
    }

    open class Entry(val staff: Staff) {
        val characters = staff.characters?.nodes?.filterNotNull().orEmpty().distinctBy { it.id }
        val media = staff.staffMedia?.nodes?.filterNotNull().orEmpty().distinctBy { it.id }
        val occupations = staff.primaryOccupations?.filterNotNull().orEmpty()
    }
}
