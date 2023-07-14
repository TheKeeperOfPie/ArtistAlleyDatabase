package com.thekeeperofpie.artistalleydatabase.anime.character

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Dimension
import com.anilist.CharacterAdvancedSearchQuery.Data.Page.Character
import com.anilist.fragment.CharacterNavigationData
import com.anilist.fragment.CharacterWithRoleAndFavorites
import com.anilist.fragment.MediaNavigationData
import com.anilist.fragment.StaffNavigationData
import com.anilist.type.CharacterRole
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.android_utils.MutableSingle
import com.thekeeperofpie.artistalleydatabase.android_utils.getValue
import com.thekeeperofpie.artistalleydatabase.android_utils.setValue
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio

@OptIn(ExperimentalFoundationApi::class)
object CharacterListRow {

    @Composable
    operator fun invoke(
        screenKey: String,
        entry: Entry?,
        modifier: Modifier = Modifier,
        showRole: Boolean = false,
        onLongPressImage: (Entry) -> Unit = {},
        colorCalculationState: ColorCalculationState = ColorCalculationState(),
        navigationCallback: AnimeNavigator.NavigationCallback =
            AnimeNavigator.NavigationCallback(null),
    ) {
        var imageWidthToHeightRatio by remember { MutableSingle(1f) }
        val onClick = {
            if (entry != null) {
                navigationCallback.onCharacterClick(
                    entry.character,
                    imageWidthToHeightRatio,
                    colorCalculationState.getColors(entry.character.id.toString()).first,
                )
            }
        }

        ElevatedCard(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp)
                .clickable(
                    enabled = true, // TODO: placeholder,
                    onClick = onClick,
                )
        ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                CharacterImage(
                    screenKey = screenKey,
                    entry = entry,
                    onClick = onClick,
                    onLongPressImage = { if (entry != null) onLongPressImage(entry) },
                    colorCalculationState = colorCalculationState,
                    onRatioAvailable = { imageWidthToHeightRatio = it }
                )

                Column(
                    modifier = Modifier
                        .heightIn(min = 180.dp)
                        .padding(bottom = 12.dp)
                ) {
                    Row(Modifier.fillMaxWidth()) {
                        if (showRole) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .wrapContentHeight(Alignment.Top)
                            ) {
                                NameText(entry = entry)
                                RoleText(entry = entry)
                            }
                        } else {
                            NameText(
                                entry = entry,
                                modifier = Modifier
                                    .weight(1f)
                                    .wrapContentHeight(Alignment.Top)
                            )
                        }

                        StatsSection(entry = entry)
                    }

                    Spacer(Modifier.weight(1f))

                    MediaRow(
                        entry = entry,
                        colorCalculationState = colorCalculationState,
                        navigationCallback = navigationCallback,
                    )
                }
            }
        }
    }

    @Composable
    private fun CharacterImage(
        screenKey: String,
        entry: Entry?,
        onClick: () -> Unit,
        onLongPressImage: () -> Unit,
        colorCalculationState: ColorCalculationState,
        onRatioAvailable: (Float) -> Unit,
    ) {
        SharedElement(
            key = "anime_character_${entry?.character?.id}_image",
            screenKey = screenKey,
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(entry?.character?.image?.large)
                    .crossfade(true)
                    .allowHardware(colorCalculationState.hasColor(entry?.character?.id?.toString()))
                    .size(
                        width = Dimension.Pixels(LocalDensity.current.run { 130.dp.roundToPx() }),
                        height = Dimension.Undefined
                    )
                    .build(),
                contentScale = ContentScale.Crop,
                fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                onSuccess = {
                    onRatioAvailable(it.widthToHeightRatio())
                    if (entry != null) {
                        ComposeColorUtils.calculatePalette(
                            entry.character.id.toString(),
                            it,
                            colorCalculationState,
                        )
                    }
                },
                contentDescription = stringResource(R.string.anime_character_image),
                modifier = Modifier
                    .fillMaxHeight()
                    .heightIn(min = 180.dp)
                    .width(130.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .placeholder(
                        visible = entry == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongPressImage,
                        onLongClickLabel = stringResource(
                            R.string.anime_character_image_long_press_preview
                        ),
                    )
            )
        }
    }

    @Composable
    private fun NameText(entry: Entry?, modifier: Modifier = Modifier) {
        Text(
            text = entry?.character?.name?.userPreferred ?: "Loading...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            modifier = modifier
                .padding(start = 12.dp, top = 10.dp, end = 16.dp)
                .placeholder(
                    visible = entry == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
    }

    @Composable
    private fun RoleText(entry: Entry?, modifier: Modifier = Modifier) {
        Text(
            text = entry?.role?.toTextRes()?.let { stringResource(it) } ?: "Main",
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier
                .padding(start = 12.dp, end = 16.dp)
                .placeholder(
                    visible = entry == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
    }

    @Composable
    private fun StatsSection(
        entry: Entry?,
    ) {
        val loading = entry == null
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
                val favorites = entry?.favorites
                AutoHeightText(
                    text = favorites?.toString() ?: "000",
                    style = MaterialTheme.typography.labelLarge,
                )

                Icon(
                    imageVector = when {
                        favorites == null -> Icons.Outlined.PeopleAlt
                        favorites > 2000 -> Icons.Filled.PeopleAlt
                        favorites > 1000 -> Icons.Outlined.PeopleAlt
                        favorites > 100 -> Icons.Filled.Person
                        else -> Icons.Filled.PersonOutline
                    },
                    contentDescription = stringResource(
                        R.string.anime_character_favorites_icon_content_description
                    ),
                )
            }
        }
    }

    @Composable
    private fun MediaRow(
        entry: Entry?,
        colorCalculationState: ColorCalculationState,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        val media = entry?.media?.takeIf { it.isNotEmpty() }
            ?: listOf(null, null, null, null, null)
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
            if (entry?.voiceActor?.image?.large != null) {
                item(entry.voiceActor.id) {
                    StaffOrMediaImage(
                        context = context,
                        density = density,
                        image = entry.voiceActor.image?.large,
                        contentDescriptionTextRes = R.string.anime_staff_image_content_description,
                        onClick = { ratio ->
                            navigationCallback.onStaffClick(
                                entry.voiceActor,
                                ratio,
                                colorCalculationState.getColors(
                                    entry.voiceActor.id.toString()
                                ).first
                            )
                        },
                    )
                }
            }

            itemsIndexed(
                media,
                key = { index, item -> if (item == null) "placeholder_$index" else item.id },
            ) { index, item ->
                StaffOrMediaImage(
                    context = context,
                    density = density,
                    image = item?.coverImage?.extraLarge,
                    contentDescriptionTextRes = R.string.anime_media_cover_image_content_description,
                    onClick = { ratio ->
                        if (item != null) {
                            navigationCallback.onMediaClick(item, ratio)
                        }
                    },
                )
            }
        }
    }

    @Composable
    private fun StaffOrMediaImage(
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
            contentDescription = stringResource(contentDescriptionTextRes),
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

    open class Entry(
        val character: CharacterNavigationData,
        val role: CharacterRole?,
        val media: List<MediaNavigationData>,
        val favorites: Int?,
        val voiceActor: StaffNavigationData?,
    ) {
        constructor(character: Character) : this(
            character = character,
            role = null,
            media = character.media?.edges?.mapNotNull { it?.node }.orEmpty().distinctBy { it.id },
            favorites = character.favourites,
            voiceActor = character.media?.edges?.filterNotNull()
                ?.flatMap { it.voiceActors?.filterNotNull().orEmpty() }
                ?.groupBy { it.languageV2 }
                ?.let {
                    it["Japanese"]?.firstOrNull() ?: it.entries.firstOrNull()?.value?.firstOrNull()
                }
        )

        constructor(character: CharacterWithRoleAndFavorites) : this(
            character = character.node,
            role = character.role,
            media = character.node.media?.nodes?.filterNotNull().orEmpty().distinctBy { it.id },
            favorites = character.node.favourites,
            voiceActor = character.voiceActors?.filterNotNull().orEmpty()
                .groupBy { it.languageV2 }
                .let {
                    it["Japanese"]?.firstOrNull() ?: it.entries.firstOrNull()?.value?.firstOrNull()
                }
        )
    }
}
