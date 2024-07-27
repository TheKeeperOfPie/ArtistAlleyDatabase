@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalCoilApi::class)

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
import coil3.annotation.ExperimentalCoilApi
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.size.Dimension
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionVoiceActor
import com.thekeeperofpie.artistalleydatabase.anilist.VoiceActorLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffUtils.subtitleName
import com.thekeeperofpie.artistalleydatabase.anime.ui.CharacterCoverImage
import com.thekeeperofpie.artistalleydatabase.anime.ui.StaffCoverImage
import com.thekeeperofpie.artistalleydatabase.anime.ui.UpperHalfBiasAlignment
import com.thekeeperofpie.artistalleydatabase.anime.ui.blurForScreenshotMode
import com.thekeeperofpie.artistalleydatabase.compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.compose.AutoSizeText
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.compose.PagingErrorItem
import com.thekeeperofpie.artistalleydatabase.compose.image.CoilImage
import com.thekeeperofpie.artistalleydatabase.compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.compose.image.colorsOrDefault
import com.thekeeperofpie.artistalleydatabase.compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.compose.image.request
import com.thekeeperofpie.artistalleydatabase.compose.optionalClickable
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.PlaceholderHighlight
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.placeholder
import com.thekeeperofpie.artistalleydatabase.compose.recomposeHighlighter
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.sharedElement
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

@Composable
fun CharacterSmallCard(
    id: EntryId,
    sharedTransitionKey: SharedTransitionKey?,
    sharedTransitionIdentifier: String,
    innerSharedTransitionKey: SharedTransitionKey?,
    innerSharedTransitionIdentifier: String,
    image: String?,
    innerImage: String?,
    imageState: CoilImageState? = rememberImageStateBelowInnerImage(image, innerImage),
    innerImageState: CoilImageState? = rememberCoilImageState(innerImage),
    onClick: () -> Unit,
    onClickInnerImage: (() -> Unit)? = null,
    width: Dp = 100.dp,
    isStaffMain: Boolean = false,
    content: @Composable (textColor: Color) -> Unit,
) {
    val defaultTextColor = MaterialTheme.typography.bodyMedium.color
    val colors = imageState.colorsOrDefault()

    val animationProgress by animateIntAsState(
        if (colors.containerColor.isUnspecified) 0 else 255,
        label = "Character card color fade in",
    )

    val containerColor = when {
        colors.containerColor.isUnspecified || animationProgress == 0 ->
            MaterialTheme.colorScheme.surface
        animationProgress == 255 -> colors.containerColor
        else -> Color(
            ColorUtils.compositeColors(
                ColorUtils.setAlphaComponent(
                    colors.containerColor.toArgb(),
                    animationProgress
                ),
                MaterialTheme.colorScheme.surface.toArgb()
            )
        )
    }

    val textColor = when {
        colors.textColor.isUnspecified || animationProgress == 0 -> defaultTextColor
        animationProgress == 255 -> colors.textColor
        else -> Color(
            ColorUtils.compositeColors(
                ColorUtils.setAlphaComponent(
                    colors.textColor.toArgb(),
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
            val imageRequest = imageState.request()
                .crossfade(true)
                .size(
                    width = density.run { width.roundToPx() },
                    height = density.run { (width * 1.5f).roundToPx() },
                )
                .build()
            val imageModifier = Modifier
                .size(width = width, height = width * 1.5f)
                .sharedElement(sharedTransitionKey, sharedTransitionIdentifier)
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                    )
                )
            if (isStaffMain) {
                StaffCoverImage(
                    imageState = imageState,
                    image = imageRequest,
                    modifier = imageModifier,
                    contentScale = ContentScale.Crop
                )
            } else {
                CharacterCoverImage(
                    imageState = imageState,
                    image = imageRequest,
                    modifier = imageModifier,
                    contentScale = ContentScale.Crop
                )
            }

            if (innerImage != null) {
                var showInnerImage by remember { mutableStateOf(true) }
                if (showInnerImage) {
                    val clipShape = RoundedCornerShape(topStart = 8.dp)
                    val size = LocalDensity.current.run { 40.dp.roundToPx() }
                    CoilImage(
                        state = innerImageState,
                        model = innerImageState.request()
                            .crossfade(true)
                            .listener(onError = { _, _ ->
                                showInnerImage = false
                            })
                            .size(width = size, height = size)
                            .build(),
                        contentScale = ContentScale.Crop,
                        alignment = UpperHalfBiasAlignment,
                        contentDescription = stringResource(
                            // TODO: Swap based on innerImageKey
                            if (isStaffMain) {
                                R.string.anime_character_image_content_description
                            } else {
                                R.string.anime_media_voice_actor_image
                            }
                        ),
                        modifier = Modifier
                            .size(width = 40.dp, height = 40.dp)
                            .sharedElement(innerSharedTransitionKey, innerSharedTransitionIdentifier)
                            .align(Alignment.BottomEnd)
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
    mediaHeaderParams: MediaHeaderParams,
    charactersInitial: List<DetailsCharacter>,
    charactersDeferred: () -> LazyPagingItems<DetailsCharacter>,
    @StringRes viewAllContentDescriptionTextRes: Int? = null,
) {
    if (charactersDeferred().itemCount.coerceAtLeast(charactersInitial.size) == 0) return
    item("charactersHeader-$titleRes") {
        val navigationCallback = LocalNavigationCallback.current
        DetailsSectionHeader(
            text = stringResource(titleRes),
            onClickViewAll = {
                navigationCallback.navigate(
                    AnimeDestinations.MediaCharacters(
                        mediaId = mediaId,
                        headerParams = mediaHeaderParams,
                    )
                )
            },
            viewAllContentDescriptionTextRes = viewAllContentDescriptionTextRes,
            modifier = Modifier.recomposeHighlighter()
        )
    }

    item("charactersSection-$titleRes") {
        CharactersSection(
            screenKey = screenKey,
            charactersInitial = charactersInitial,
            charactersDeferred = { charactersDeferred() },
        )
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
            CharactersSectionItem(
                character = characters[it],
                showVoiceActorAsMain = false,
            )
        }
    }
}

@Composable
fun CharactersSection(
    screenKey: String,
    charactersInitial: List<DetailsCharacter>,
    charactersDeferred: @Composable () -> LazyPagingItems<DetailsCharacter>,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    showVoiceActorAsMain: Boolean = false,
) {
    @Suppress("NAME_SHADOWING")
    val charactersDeferred = charactersDeferred()
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = contentPadding,
        modifier = Modifier.recomposeHighlighter()
    ) {
        if (charactersDeferred.itemCount > 0) {
            items(
                count = charactersDeferred.itemCount,
                key = charactersDeferred.itemKey { it.id },
                contentType = charactersDeferred.itemContentType { "character" }
            ) {
                val character = charactersDeferred[it]
                CharactersSectionItem(
                    character = character,
                    showVoiceActorAsMain = showVoiceActorAsMain,
                )
            }
        } else {
            items(charactersInitial, key = { it.id }, contentType = { "character" }) {
                CharactersSectionItem(
                    character = it,
                    showVoiceActorAsMain = showVoiceActorAsMain,
                )
            }
        }

        if (charactersDeferred.loadState.hasError) {
            item("error") {
                PagingErrorItem(charactersDeferred)
            }
        }
    }
}

@Composable
fun CharactersSectionItem(
    character: DetailsCharacter?,
    showVoiceActorAsMain: Boolean,
) {
    val navigationCallback = LocalNavigationCallback.current
    val voiceActor = AniListUtils.selectVoiceActor(character?.languageToVoiceActor)
    val characterName = character?.character?.name?.primaryName()
    val image = if (showVoiceActorAsMain) voiceActor?.image else character?.image
    val innerImage = if (showVoiceActorAsMain) character?.image else voiceActor?.image
    val imageState = rememberImageStateBelowInnerImage(image, innerImage)
    val innerImageState = rememberCoilImageState(innerImage)
    val characterSharedTransitionKey = character?.id?.let { SharedTransitionKey.makeKeyForId(it) }
    val voiceActorTransitionKey = voiceActor?.id?.toString()?.let { SharedTransitionKey.makeKeyForId(it) }
    val onClickCharacter: () -> Unit = {
        if (character?.character != null) {
            navigationCallback.navigate(
                AnimeDestinations.CharacterDetails(
                    characterId = character.id,
                    sharedTransitionKey = characterSharedTransitionKey,
                    headerParams = CharacterHeaderParams(
                        name = characterName,
                        subtitle = null,
                        favorite = null,
                        coverImage = if (showVoiceActorAsMain) {
                            innerImageState
                        } else {
                            imageState
                        }.toImageState(),
                    )
                )
            )
        }
    }
    val voiceActorName = voiceActor?.staff?.name?.primaryName()
    val voiceActorSubtitle = voiceActor?.staff?.name?.subtitleName()
    val onClickVoiceActor: () -> Unit = {
        voiceActor?.let {
            navigationCallback.navigate(
                AnimeDestinations.StaffDetails(
                    staffId = voiceActor.id.toString(),
                    sharedTransitionKey = voiceActorTransitionKey,
                    headerParams = StaffHeaderParams(
                        name = voiceActorName,
                        subtitle = voiceActorSubtitle,
                        coverImage = if (showVoiceActorAsMain) {
                            imageState
                        } else {
                            innerImageState
                        }.toImageState(),
                        favorite = null,
                    )
                )
            )
        }
    }
    CharactersSectionItem(
        character = character,
        id = if (showVoiceActorAsMain) {
            EntryId("anime_staff", voiceActor?.id.orEmpty())
        } else {
            EntryId("anime_character", character?.id.orEmpty())
        },
        sharedTransitionKey = if (showVoiceActorAsMain) {
            voiceActorTransitionKey
        } else {
            characterSharedTransitionKey
        },
        sharedTransitionIdentifier = if (showVoiceActorAsMain) {
            "staff_image"
        } else {
            "character_image"
        },
        innerSharedTransitionKey = if (showVoiceActorAsMain) {
            characterSharedTransitionKey
        } else {
            voiceActorTransitionKey
        },
        innerSharedTransitionIdentifier = if (showVoiceActorAsMain) {
            "character_image"
        } else {
            "staff_image"
        },
        imageState = imageState,
        innerImageState = innerImageState,
        onClick = if (showVoiceActorAsMain) {
            onClickVoiceActor
        } else {
            onClickCharacter
        },
        onClickInnerImage = if (showVoiceActorAsMain) {
            onClickCharacter
        } else {
            onClickVoiceActor
        },
        text = {
            if (showVoiceActorAsMain) {
                voiceActor?.name.orEmpty()
            } else {
                character?.name?.primaryName().orEmpty()
            }
        },
        isStaffMain = showVoiceActorAsMain,
    )
}

@Composable
fun CharactersSectionItem(
    character: DetailsCharacter?,
    id: EntryId,
    sharedTransitionKey: SharedTransitionKey?,
    sharedTransitionIdentifier: String,
    innerSharedTransitionKey: SharedTransitionKey?,
    innerSharedTransitionIdentifier: String,
    imageState: CoilImageState?,
    innerImageState: CoilImageState?,
    onClick: () -> Unit,
    onClickInnerImage: () -> Unit,
    text: @Composable () -> String,
    isStaffMain: Boolean = false,
) {
    val voiceActor = AniListUtils.selectVoiceActor(character?.languageToVoiceActor)

    val navigationCallback = LocalNavigationCallback.current
    CharacterSmallCard(
        id = id,
        sharedTransitionKey = sharedTransitionKey,
        sharedTransitionIdentifier = sharedTransitionIdentifier,
        innerSharedTransitionKey = innerSharedTransitionKey,
        innerSharedTransitionIdentifier = innerSharedTransitionIdentifier,
        image = imageState?.uri,
        imageState = imageState,
        onClick = onClick,
        innerImage = innerImageState?.uri,
        innerImageState = innerImageState,
        onClickInnerImage = onClickInnerImage,
        isStaffMain = isStaffMain,
    ) { textColor ->
        AutoResizeHeightText(
            text = text(),
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
    val coverImageState = rememberCoilImageState(character?.character?.image?.large)
    val navigationCallback = LocalNavigationCallback.current
    val characterName = character?.character?.name?.primaryName()

    val voiceActor = AniListUtils.selectVoiceActor(
        map = character?.languageToVoiceActor,
        voiceActorLanguage = voiceActorLanguage
    )
    val characterSharedTransitionKey = character?.id?.let { SharedTransitionKey.makeKeyForId(it) }
    val voiceActorSharedTransitionKey = voiceActor?.id?.let { SharedTransitionKey.makeKeyForId(it) }
    ElevatedCard(
        onClick = {
            character?.character?.let {
                navigationCallback.navigate(
                    AnimeDestinations.CharacterDetails(
                        characterId = character.id.toString(),
                        sharedTransitionKey = characterSharedTransitionKey,
                        headerParams = CharacterHeaderParams(
                            name = characterName,
                            subtitle = null,
                            favorite = null,
                            coverImage = coverImageState.toImageState(),
                        )
                    )
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
                imageState = coverImageState,
                image = coverImageState.request()
                    .crossfade(true)
                    .allowHardware(true)
                    .size(width = Dimension.Pixels(width), height = Dimension.Undefined)
                    .build(),
                modifier = Modifier
                    .width(imageWidth)
                    .fillMaxHeight()
                    .sharedElement(characterSharedTransitionKey, "character_image")
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                    .placeholder(
                        visible = character == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    ),
                contentScale = ContentScale.Crop
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
                val voiceActorImageState = rememberCoilImageState(voiceActor?.image)
                val voiceActorName = voiceActor?.staff?.name?.primaryName()
                val voiceActorSubtitle = voiceActor?.staff?.name?.subtitleName()
                StaffCoverImage(
                    imageState = voiceActorImageState,
                    image = voiceActorImageState.request()
                        .crossfade(true)
                        .allowHardware(true)
                        .size(width = Dimension.Pixels(width), height = Dimension.Undefined)
                        .build(),
                    modifier = Modifier
                        .width(imageWidth)
                        .fillMaxHeight()
                        .sharedElement(voiceActorSharedTransitionKey, "staff_image")
                        .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                        .placeholder(
                            visible = character == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                        .clickable {
                            if (voiceActor != null) {
                                navigationCallback.navigate(
                                    AnimeDestinations.StaffDetails(
                                        staffId = voiceActor.staff.id.toString(),
                                        sharedTransitionKey = voiceActorSharedTransitionKey,
                                        headerParams = StaffHeaderParams(
                                            name = voiceActorName,
                                            subtitle = voiceActorSubtitle,
                                            coverImage = voiceActorImageState.toImageState(),
                                            favorite = null,
                                        )
                                    )
                                )
                            }
                        },
                    contentScale = ContentScale.Crop,
                    contentDescriptionTextRes = R.string.anime_media_voice_actor_image
                )
            }
        }
    }
}

/**
 * For use in something like [CharacterSmallCard] where a smaller inner image is shown overlaid
 * on the bottom right. This will exclude that area from the palette calculation area.
 */
@Composable
fun rememberImageStateBelowInnerImage(
    uri: String?,
    innerImage: String?,
) = rememberCoilImageState(
    uri = uri,
    cacheKey = null,
    heightStartThreshold = 3 / 4f,
    // Only capture left 3/5ths to ignore
    // part covered by voice actor
    widthEndThreshold = if (innerImage == null) 1f else 3 / 5f,
    selectMaxPopulation = true,
    requestColors = true,
)
