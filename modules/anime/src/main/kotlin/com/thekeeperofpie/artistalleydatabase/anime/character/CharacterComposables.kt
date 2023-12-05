@file:OptIn(ExperimentalMaterial3Api::class)

package com.thekeeperofpie.artistalleydatabase.anime.character

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateIntAsState
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.toArgb
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
import com.anilist.fragment.MediaHeaderData
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.android_utils.MutableSingle
import com.thekeeperofpie.artistalleydatabase.android_utils.getValue
import com.thekeeperofpie.artistalleydatabase.android_utils.setValue
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionVoiceActor
import com.thekeeperofpie.artistalleydatabase.anilist.VoiceActorLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.ui.CharacterCoverImage
import com.thekeeperofpie.artistalleydatabase.anime.ui.StaffCoverImage
import com.thekeeperofpie.artistalleydatabase.anime.ui.blurForScreenshotMode
import com.thekeeperofpie.artistalleydatabase.compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.compose.AutoSizeText
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.compose.LocalColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.optionalClickable
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.PlaceholderHighlight
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.placeholder
import com.thekeeperofpie.artistalleydatabase.compose.recomposeHighlighter
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

@Composable
fun CharacterSmallCard(
    screenKey: String,
    id: EntryId,
    image: String?,
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
    val colorCalculationState = LocalColorCalculationState.current
    val colors = colorCalculationState.getColors(id.scopedId)

    val animationProgress by animateIntAsState(
        if (colors.first.isUnspecified) 0 else 255,
        label = "Character card color fade in",
    )

    val containerColor = when {
        colors.first.isUnspecified || animationProgress == 0 ->
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
        colors.second.isUnspecified || animationProgress == 0 -> defaultTextColor
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
        modifier = Modifier
            .width(width)
            .padding(bottom = 2.dp),
    ) {
        Box {
            val density = LocalDensity.current
            CharacterCoverImage(
                screenKey = screenKey,
                characterId = id.valueId,
                image = ImageRequest.Builder(LocalContext.current)
                    .data(image)
                    .crossfade(true)
                    .allowHardware(colorCalculationState.allowHardware(id.scopedId))
                    .size(
                        width = density.run { width.roundToPx() },
                        height = density.run { (width * 1.5f).roundToPx() },
                    )
                    .build(),
                contentScale = ContentScale.Crop,
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

            if (innerImage != null) {
                var showInnerImage by remember { mutableStateOf(true) }
                if (showInnerImage) {
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
                                    .crossfade(true)
                                    .listener(onError = { _, _ ->
                                        showInnerImage = false
                                    })
                                    .size(width = size, height = size)
                                    .build(),
                                contentScale = ContentScale.Crop,
                                contentDescription = stringResource(
                                    // TODO: Swap based on innerImageKey
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
                                    .blurForScreenshotMode()
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
    onClickViewAll: ((AnimeNavigator.NavigationCallback) -> Unit)? = null,
    @StringRes viewAllContentDescriptionTextRes: Int? = null,
) {
    if (characters.itemCount == 0) return
    item("charactersHeader-$titleRes") {
        val navigationCallback = LocalNavigationCallback.current
        DetailsSectionHeader(
            text = stringResource(titleRes),
            onClickViewAll = remember { onClickViewAll?.let { { it(navigationCallback) } } },
            viewAllContentDescriptionTextRes = viewAllContentDescriptionTextRes,
            modifier = Modifier.recomposeHighlighter()
        )
    }

    item("charactersSection-$titleRes") {
        CharactersSection(
            screenKey = screenKey,
            characters = characters,
        )
    }
}

fun LazyListScope.charactersSection(
    screenKey: String,
    @StringRes titleRes: Int,
    mediaId: String,
    media: MediaHeaderData?,
    mediaFavorite: Boolean?,
    charactersInitial: List<DetailsCharacter>,
    charactersDeferred: () -> LazyPagingItems<DetailsCharacter>,
    mediaCoverImageWidthToHeightRatio: () -> Float,
    @StringRes viewAllContentDescriptionTextRes: Int? = null,
) {
    if (charactersDeferred().itemCount.coerceAtLeast(charactersInitial.size) == 0) return
    item("charactersHeader-$titleRes") {
        val navigationCallback = LocalNavigationCallback.current
        DetailsSectionHeader(
            text = stringResource(titleRes),
            onClickViewAll = remember(mediaId, media, mediaFavorite) {
                {
                    navigationCallback.onMediaCharactersClick(
                        mediaId = mediaId,
                        media = media,
                        favorite = mediaFavorite,
                        imageWidthToHeightRatio = mediaCoverImageWidthToHeightRatio(),
                    )
                }
            },
            viewAllContentDescriptionTextRes = viewAllContentDescriptionTextRes,
            modifier = Modifier.recomposeHighlighter()
        )
    }

    item("charactersSection-$titleRes") {
        CharactersSection(screenKey, charactersInitial, charactersDeferred)
    }
}

@Composable
private fun CharactersSection(
    screenKey: String,
    characters: LazyPagingItems<DetailsCharacter>,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.recomposeHighlighter()
    ) {
        items(
            count = characters.itemCount,
            key = characters.itemKey { it.id },
            contentType = characters.itemContentType { "character" },
        ) {
            CharactersSectionItem(screenKey = screenKey, character = characters[it])
        }
    }
}

@Composable
private fun CharactersSection(
    screenKey: String,
    charactersInitial: List<DetailsCharacter>,
    charactersDeferred: () -> LazyPagingItems<DetailsCharacter>,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.recomposeHighlighter()
    ) {
        @Suppress("NAME_SHADOWING")
        val charactersDeferred = charactersDeferred()
        if (charactersDeferred.itemCount > 0) {
            items(
                count = charactersDeferred.itemCount,
                key = charactersDeferred.itemKey { it.id },
                contentType = charactersDeferred.itemContentType { "character" }
            ) {
                val character = charactersDeferred[it]
                CharactersSectionItem(screenKey = screenKey, character = character)
            }
        } else {
            items(charactersInitial, key = { it.id }, contentType = { "character" }) {
                CharactersSectionItem(screenKey = screenKey, character = it)
            }
        }
    }
}

@Composable
fun CharactersSectionItem(screenKey: String, character: DetailsCharacter?) {
    var imageWidthToHeightRatio by remember { MutableSingle(1f) }
    var innerImageWidthToHeightRatio by remember { MutableSingle(1f) }
    val voiceActor = AniListUtils.selectVoiceActor(character?.languageToVoiceActor)

    val navigationCallback = LocalNavigationCallback.current
    val colorCalculationState = LocalColorCalculationState.current
    CharacterSmallCard(
        screenKey = screenKey,
        id = EntryId("anime_character", character?.id.orEmpty()),
        image = character?.image,
        onClick = {
            character?.character?.let {
                navigationCallback.onCharacterClick(
                    character.character,
                    null,
                    imageWidthToHeightRatio,
                    colorCalculationState.getColorsNonComposable(character.id).first,
                )
            }
        },
        innerImage = voiceActor?.image,
        innerImageKey = "anime_staff_${voiceActor?.id}_image",
        onClickInnerImage = voiceActor?.image?.let {
            {
                navigationCallback.onStaffClick(
                    voiceActor.staff,
                    null,
                    innerImageWidthToHeightRatio,
                    colorCalculationState.getColorsNonComposable(voiceActor.id).first,
                )
            }
        },
        onImageSuccess = { imageWidthToHeightRatio = it.widthToHeightRatio() },
        onInnerImageSuccess = {
            innerImageWidthToHeightRatio = it.widthToHeightRatio()
        },
    ) { textColor ->
        AutoResizeHeightText(
            text = character?.name?.primaryName().orEmpty(),
            color = textColor,
            style = MaterialTheme.typography.bodyMedium.copy(
                lineBreak = LineBreak(
                    strategy = LineBreak.Strategy.Balanced,
                    strictness = LineBreak.Strictness.Strict,
                    wordBreak = LineBreak.WordBreak.Default,
                )
            ),
            minTextSizeSp = 8f,
            textAlignment = Alignment.TopStart,
            modifier = Modifier
                .size(width = 100.dp, height = 56.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun CharacterCard(
    screenKey: String,
    imageWidth: Dp,
    minHeight: Dp,
    character: DetailsCharacter?,
    voiceActorLanguage: VoiceActorLanguageOption = LocalLanguageOptionVoiceActor.current.first,
) {
    var imageWidthToHeightRatio by remember { MutableSingle(1f) }
    val navigationCallback = LocalNavigationCallback.current
    val colorCalculationState = LocalColorCalculationState.current
    ElevatedCard(
        onClick = {
            character?.character?.let {
                navigationCallback.onCharacterClick(
                    it,
                    null,
                    imageWidthToHeightRatio,
                    colorCalculationState.getColorsNonComposable(it.id.toString()).first,
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
            CharacterCoverImage(
                screenKey = screenKey,
                characterId = character?.id,
                image = ImageRequest.Builder(LocalContext.current)
                    .data(character?.image)
                    .crossfade(true)
                    .allowHardware(true)
                    .size(width = Dimension.Pixels(width), height = Dimension.Undefined)
                    .build(),
                contentScale = ContentScale.Crop,
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

            val voiceActor = AniListUtils.selectVoiceActor(
                map = character?.languageToVoiceActor,
                voiceActorLanguage = voiceActorLanguage
            )

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
                        text = character?.name?.primaryName() ?: "FirstName LastName",
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
                            color = MaterialTheme.typography.labelSmall.color
                                .takeOrElse { LocalContentColor.current }
                                .copy(alpha = 0.8f),
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
                StaffCoverImage(
                    screenKey = screenKey,
                    staffId = voiceActor?.id,
                    image = ImageRequest.Builder(LocalContext.current)
                        .data(voiceActor?.image)
                        .crossfade(true)
                        .allowHardware(true)
                        .size(width = Dimension.Pixels(width), height = Dimension.Undefined)
                        .build(),
                    contentScale = ContentScale.Crop,
                    contentDescriptionTextRes = R.string.anime_media_voice_actor_image,
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
                                    colorCalculationState.getColorsNonComposable(
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
