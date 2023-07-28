@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.character

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import coil.size.Dimension
import com.anilist.fragment.CharacterNavigationData
import com.anilist.fragment.StaffNavigationData
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.android_utils.MutableSingle
import com.thekeeperofpie.artistalleydatabase.android_utils.getValue
import com.thekeeperofpie.artistalleydatabase.android_utils.setValue
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.AutoSizeText
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.compose.optionalClickable
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

@Composable
fun CharacterSmallCard(
    screenKey: String,
    id: EntryId,
    image: String?,
    colorCalculationState: ColorCalculationState,
    onClick: () -> Unit,
    innerImage: String? = null,
    innerImageKey: String = "invalid_key",
    onClickInnerImage: (() -> Unit)? = null,
    onImageSuccess: (AsyncImagePainter.State.Success) -> Unit = {},
    onInnerImageSuccess: (AsyncImagePainter.State.Success) -> Unit = {},
    width: Dp = 100.dp,
    content: @Composable (textColor: Color) -> Unit,
) {
    val defaultTextColor = MaterialTheme.typography.bodyMedium.color
    val colors = colorCalculationState.colorMap[id.scopedId]

    val animationProgress by animateIntAsState(
        if (colors == null) 0 else 255,
        label = "Character card color fade in",
    )

    val containerColor = when {
        colors == null || animationProgress == 0 ->
            MaterialTheme.colorScheme.surface
        animationProgress == 255 -> colors.first
        else -> Color(
            ColorUtils.compositeColors(
                ColorUtils.setAlphaComponent(
                    colors.first.toArgb(),
                    animationProgress
                ),
                MaterialTheme.colorScheme.surface.toArgb()
            )
        )
    }

    val textColor = when {
        colors == null || animationProgress == 0 -> defaultTextColor
        animationProgress == 255 -> colors.second
        else -> Color(
            ColorUtils.compositeColors(
                ColorUtils.setAlphaComponent(
                    colors.second.toArgb(),
                    animationProgress
                ),
                defaultTextColor.toArgb()
            )
        )
    }

    ElevatedCard(
        onClick = onClick,
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        modifier = Modifier.width(width).padding(bottom = 2.dp),
    ) {
        Box {
            val density = LocalDensity.current
            SharedElement(
                key = "${id.scopedId}_image",
                screenKey = screenKey,
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(image)
                        .crossfade(true)
                        .allowHardware(colorCalculationState.hasColor(id.scopedId))
                        .size(
                            width = density.run { width.roundToPx() },
                            height = density.run { (width * 1.5f).roundToPx() },
                        )
                        .build(),
                    contentScale = ContentScale.Crop,
                    fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                    contentDescription = stringResource(
                        R.string.anime_character_image_content_description
                    ),
                    onSuccess = {
                        onImageSuccess(it)
                        ComposeColorUtils.calculatePalette(
                            id = id.scopedId,
                            success = it,
                            colorCalculationState = colorCalculationState,
                            heightStartThreshold = 3 / 4f,
                            // Only capture left 3/5ths to ignore
                            // part covered by voice actor
                            widthEndThreshold = if (innerImage == null) 1f else 3 / 5f,
                            selectMaxPopulation = true,
                        )
                    },
                    modifier = Modifier
                        .size(width = width, height = width * 1.5f)
                        .clip(
                            RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 12.dp,
                            )
                        )
                )
            }

            if (innerImage != null) {
                var showInnerImage by remember { mutableStateOf(true) }
                if (showInnerImage) {
                    var show by rememberSaveable(id) { mutableStateOf(false) }
                    val clipShape = RoundedCornerShape(topStart = 8.dp)
                    val size = LocalDensity.current.run { 40.dp.roundToPx() }
                    Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                        SharedElement(
                            key = innerImageKey,
                            screenKey = screenKey,
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(innerImage)
                                    .crossfade(false)
                                    .listener(onError = { _, _ ->
                                        showInnerImage = false
                                    }, onSuccess = { _, _ ->
                                        show = true
                                    })
                                    .size(width = size, height = size)
                                    .build(),
                                contentScale = ContentScale.Crop,
                                contentDescription = stringResource(
                                    R.string.anime_media_voice_actor_image
                                ),
                                onSuccess = onInnerImageSuccess,
                                modifier = Modifier
                                    .size(width = 40.dp, height = 40.dp)
                                    .clip(clipShape)
                                    .optionalClickable(onClickInnerImage)
                                    .border(
                                        width = 1.dp,
                                        color = containerColor,
                                        shape = clipShape
                                    )
                            )
                        }
                    }
                }
            }
        }

        content(textColor)
    }
}

fun LazyListScope.charactersSection(
    screenKey: String,
    @StringRes titleRes: Int,
    characters: LazyPagingItems<DetailsCharacter>,
    onCharacterClick: (CharacterNavigationData, favorite: Boolean?, imageWidthToHeightRatio: Float, color: Color?) -> Unit,
    onCharacterLongClick: (String) -> Unit,
    onStaffClick: (StaffNavigationData, favorite: Boolean?, imageWidthToHeightRatio: Float, color: Color?) -> Unit,
    onClickViewAll: (() -> Unit)? = null,
    @StringRes viewAllContentDescriptionTextRes: Int? = null,
    colorCalculationState: ColorCalculationState,
) {
    if (characters.itemCount == 0) return
    item("charactersHeader-$titleRes") {
        DetailsSectionHeader(
            stringResource(titleRes),
            onClickViewAll = onClickViewAll,
            viewAllContentDescriptionTextRes = viewAllContentDescriptionTextRes,
        )
    }

    item("charactersSection-$titleRes") {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(
                count = characters.itemCount,
                key = characters.itemKey { it.id },
                contentType = characters.itemContentType { "character" },
            ) {
                val character = characters[it]
                var imageWidthToHeightRatio by remember { MutableSingle(1f) }
                var innerImageWidthToHeightRatio by remember { MutableSingle(1f) }
                val voiceActor = (character?.languageToVoiceActor?.get("Japanese")
                    ?: character?.languageToVoiceActor?.values?.firstOrNull())
                CharacterSmallCard(
                    screenKey = screenKey,
                    id = EntryId("anime_character", character?.id.orEmpty()),
                    image = character?.image,
                    colorCalculationState = colorCalculationState,
                    onClick = {
                        character?.character?.let {
                            onCharacterClick(
                                character.character,
                                null,
                                imageWidthToHeightRatio,
                                colorCalculationState.getColors(character.id).first,
                            )
                        }
                    },
                    innerImage = voiceActor?.image,
                    innerImageKey = "anime_staff_${voiceActor?.id}_image",
                    onClickInnerImage = voiceActor?.image?.let {
                        {
                            onStaffClick(
                                voiceActor.staff,
                                null,
                                innerImageWidthToHeightRatio,
                                colorCalculationState.getColors(voiceActor.id).first,
                            )
                        }
                    },
                    onImageSuccess = { imageWidthToHeightRatio = it.widthToHeightRatio() },
                    onInnerImageSuccess = {
                        innerImageWidthToHeightRatio = it.widthToHeightRatio()
                    },
                ) { textColor ->
                    AutoHeightText(
                        text = character?.name.orEmpty(),
                        color = textColor,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineBreak = LineBreak(
                                strategy = LineBreak.Strategy.Balanced,
                                strictness = LineBreak.Strictness.Strict,
                                wordBreak = LineBreak.WordBreak.Default,
                            )
                        ),
                        minTextSizeSp = 8f,
                        modifier = Modifier
                            .size(width = 100.dp, height = 56.dp)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun CharacterCard(
    screenKey: String,
    imageWidth: Dp,
    minHeight: Dp,
    character: DetailsCharacter?,
    colorCalculationState: ColorCalculationState,
    navigationCallback: AnimeNavigator.NavigationCallback,
) {
    var imageWidthToHeightRatio by remember { MutableSingle(1f) }
    ElevatedCard(
        onClick = {
            character?.character?.let {
                navigationCallback.onCharacterClick(
                    it,
                    null,
                    imageWidthToHeightRatio,
                    colorCalculationState.getColors(it.id.toString()).first,
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .heightIn(min = minHeight)
        ) {
            val density = LocalDensity.current
            val width = density.run { imageWidth.roundToPx() }
            SharedElement(
                key = "anime_character_${character?.id}_image",
                screenKey = screenKey,
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(character?.image)
                        .crossfade(true)
                        .allowHardware(true)
                        .size(width = Dimension.Pixels(width), height = Dimension.Undefined)
                        .build(),
                    contentScale = ContentScale.Crop,
                    fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                    contentDescription = stringResource(
                        R.string.anime_character_image_content_description
                    ),
                    onSuccess = { imageWidthToHeightRatio = it.widthToHeightRatio() },
                    modifier = Modifier
                        .width(imageWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                        .placeholder(
                            visible = character == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                )
            }

            val voiceActor = (character?.languageToVoiceActor?.get("Japanese")
                ?: character?.languageToVoiceActor?.values?.firstOrNull())

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(vertical = 10.dp)
                ) {
                    Text(
                        text = character?.name ?: "FirstName LastName",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.placeholder(
                            visible = character == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                    )

                    if (character == null || character.roleTextRes != null) {
                        Text(
                            text = stringResource(
                                character?.roleTextRes ?: R.string.anime_character_role_main
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.placeholder(
                                visible = character == null,
                                highlight = PlaceholderHighlight.shimmer(),
                            )
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                if (character == null || voiceActor != null) {
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier
                            .wrapContentWidth()
                            .align(Alignment.End)
                    ) {
                        AutoSizeText(
                            text = voiceActor?.name ?: "FirstName LastName",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .padding(vertical = 10.dp)
                                .placeholder(
                                    visible = character == null,
                                    highlight = PlaceholderHighlight.shimmer(),
                                )
                        )
                    }
                }
            }

            if (character == null || voiceActor?.image != null) {
                var voiceActorImageWidthToHeightRatio by remember { MutableSingle(1f) }
                SharedElement(
                    key = "anime_staff_${voiceActor?.id}_image",
                    screenKey = screenKey,
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(voiceActor?.image)
                            .crossfade(true)
                            .allowHardware(true)
                            .size(width = Dimension.Pixels(width), height = Dimension.Undefined)
                            .build(),
                        contentScale = ContentScale.Crop,
                        fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                        contentDescription = stringResource(
                            R.string.anime_media_voice_actor_image
                        ),
                        onSuccess = {
                            voiceActorImageWidthToHeightRatio = it.widthToHeightRatio()
                        },
                        modifier = Modifier
                            .width(imageWidth)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                            .placeholder(
                                visible = character == null,
                                highlight = PlaceholderHighlight.shimmer(),
                            )
                            .clickable {
                                if (voiceActor != null) {
                                    navigationCallback.onStaffClick(
                                        voiceActor.staff,
                                        null,
                                        voiceActorImageWidthToHeightRatio,
                                        colorCalculationState.getColors(
                                            voiceActor.staff.id.toString()
                                        ).first,
                                    )
                                }
                            }
                    )
                }
            }
        }
    }
}
