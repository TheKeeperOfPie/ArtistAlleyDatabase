package com.thekeeperofpie.artistalleydatabase.anime.staff

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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.takeOrElse
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
import com.anilist.StaffSearchQuery.Data.Page.Staff
import com.anilist.fragment.MediaNavigationData
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.android_utils.MutableSingle
import com.thekeeperofpie.artistalleydatabase.android_utils.getValue
import com.thekeeperofpie.artistalleydatabase.android_utils.setValue
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.ui.ListRowFavoritesSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.ListRowSmallImage
import com.thekeeperofpie.artistalleydatabase.anime.ui.StaffCoverImage
import com.thekeeperofpie.artistalleydatabase.anime.utils.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio

@OptIn(ExperimentalFoundationApi::class)
object StaffListRow {

    @Composable
    operator fun invoke(
        screenKey: String,
        entry: Entry?,
        colorCalculationState: ColorCalculationState = ColorCalculationState(),
    ) {
        var imageWidthToHeightRatio by remember { MutableSingle(1f) }
        val navigationCallback = LocalNavigationCallback.current
        val onClick = {
            if (entry != null) {
                navigationCallback.onStaffClick(
                    entry.staff,
                    null,
                    imageWidthToHeightRatio,
                    colorCalculationState.getColors(entry.staff.id.toString()).first,
                )
            }
        }
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp)
                .clickable(
                    enabled = true, // TODO: placeholder,
                    onClick = onClick,
                )
        ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                StaffImage(
                    screenKey = screenKey,
                    entry = entry,
                    onClick = onClick,
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

                        ListRowFavoritesSection(
                            loading = entry == null,
                            favorites = entry?.staff?.favourites,
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    CharactersAndMediaRow(
                        screenKey = screenKey,
                        entry = entry,
                        colorCalculationState = colorCalculationState,
                    )
                }
            }
        }
    }

    @Composable
    private fun StaffImage(
        screenKey: String,
        entry: Entry?,
        onClick: () -> Unit,
        colorCalculationState: ColorCalculationState,
        onRatioAvailable: (Float) -> Unit,
    ) {
        val fullscreenImageHandler = LocalFullscreenImageHandler.current
        StaffCoverImage(
            screenKey = screenKey,
            staffId = entry?.staff?.id?.toString(),
            image = ImageRequest.Builder(LocalContext.current)
                .data(entry?.staff?.image?.large)
                .crossfade(true)
                .allowHardware(colorCalculationState.hasColor(entry?.staff?.id?.toString()))
                .size(
                    width = Dimension.Pixels(LocalDensity.current.run { 130.dp.roundToPx() }),
                    height = Dimension.Undefined,
                )
                .build(),
            contentScale = ContentScale.Crop,
            onSuccess = {
                onRatioAvailable(it.widthToHeightRatio())
                if (entry != null) {
                    ComposeColorUtils.calculatePalette(
                        entry.staff.id.toString(),
                        it,
                        colorCalculationState,
                    )
                }
            },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .fillMaxHeight()
                .heightIn(min = 180.dp)
                .width(130.dp)
                .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                .placeholder(
                    visible = entry == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        entry?.staff?.image?.large?.let(fullscreenImageHandler::openImage)
                    },
                    onLongClickLabel = stringResource(
                        R.string.anime_staff_image_long_press_preview
                    ),
                )
        )
    }

    @Composable
    private fun NameText(entry: Entry?, modifier: Modifier = Modifier) {
        Text(
            text = entry?.staff?.name?.primaryName() ?: "Loading...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(Alignment.Top)
                .padding(start = 12.dp, top = 10.dp, end = 16.dp)
                .placeholder(
                    visible = entry == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
    }

    @Composable
    private fun OccupationsText(entry: Entry?) {
        if (entry?.occupations?.isEmpty() != false) return
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
    private fun CharactersAndMediaRow(
        screenKey: String,
        entry: Entry?,
        colorCalculationState: ColorCalculationState,
    ) {
        val media = entry?.media.orEmpty()
        val characters = entry?.characters.orEmpty()
        if (media.isEmpty() && characters.isEmpty()) return
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
            items(characters, key = { it.id }) {
                SharedElement(key = "anime_character_${it.id}_image", screenKey = screenKey) {
                    val navigationCallback = LocalNavigationCallback.current
                    ListRowSmallImage(
                        context = context,
                        density = density,
                        ignored = false,
                        image = it.image?.large,
                        contentDescriptionTextRes = R.string.anime_character_image_content_description,
                        onClick = { ratio ->
                            navigationCallback.onCharacterClick(
                                it,
                                null,
                                ratio,
                                colorCalculationState.getColors(it.id.toString()).first,
                            )
                        }
                    )
                }
            }

            items(media, key = { it.media.id }) {
                SharedElement(key = "anime_media_${it.media.id}_image", screenKey = screenKey) {
                    val navigationCallback = LocalNavigationCallback.current
                    ListRowSmallImage(
                        context = context,
                        density = density,
                        ignored = it.ignored,
                        image = it.media.coverImage?.extraLarge,
                        contentDescriptionTextRes = R.string.anime_media_cover_image_content_description,
                        onClick = { ratio -> navigationCallback.onMediaClick(it.media, ratio) }
                    )
                }
            }
        }
    }

    data class Entry(
        val staff: Staff,
        val media: List<MediaEntry>,
    ) {
        val characters = staff.characters?.nodes?.filterNotNull().orEmpty().distinctBy { it.id }
        val occupations = staff.primaryOccupations?.filterNotNull().orEmpty()

        data class MediaEntry(
            val media: MediaNavigationData,
            val isAdult: Boolean?,
            val ignored: Boolean = false,
        )
    }
}
