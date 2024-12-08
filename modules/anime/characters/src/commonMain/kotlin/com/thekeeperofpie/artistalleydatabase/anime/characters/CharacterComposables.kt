package com.thekeeperofpie.artistalleydatabase.anime.characters

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.characters.generated.resources.Res
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_role_main
import artistalleydatabase.modules.anime.ui.generated.resources.anime_character_image_content_description
import artistalleydatabase.modules.anime.ui.generated.resources.anime_voice_actor_image
import coil3.request.crossfade
import coil3.size.Dimension
import com.anilist.data.fragment.CharacterNavigationData
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionVoiceActor
import com.thekeeperofpie.artistalleydatabase.anilist.VoiceActorLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterDetails
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.StaffUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.StaffUtils.subtitleName
import com.thekeeperofpie.artistalleydatabase.anime.ui.CharacterCoverImage
import com.thekeeperofpie.artistalleydatabase.anime.ui.ListRowSmallImage
import com.thekeeperofpie.artistalleydatabase.anime.ui.StaffCoverImage
import com.thekeeperofpie.artistalleydatabase.anime.ui.StaffDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.UpperHalfBiasAlignment
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoSizeText
import com.thekeeperofpie.artistalleydatabase.utils_compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.utils_compose.GridUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalSharedTransitionPrefixKeys
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImage
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.allowHardware
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.blurForScreenshotMode
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.colorsOrDefault
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.request
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavHostController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.optionalClickable
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.PagingErrorItem
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemContentType
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.recomposeHighlighter
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import artistalleydatabase.modules.anime.ui.generated.resources.Res as UiRes

@Composable
fun CharacterSmallCard(
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

    val animationProgress by animateFloatAsState(
        if (colors.containerColor.isUnspecified) 0f else 1f,
        label = "Character card color fade in",
    )

    val containerColor = when {
        colors.containerColor.isUnspecified || animationProgress == 0f ->
            MaterialTheme.colorScheme.surface
        animationProgress == 1f -> colors.containerColor
        else -> colors.containerColor.copy(alpha = animationProgress)
            .compositeOver(MaterialTheme.colorScheme.surface)
    }

    val textColor = when {
        colors.textColor.isUnspecified || animationProgress == 0f -> defaultTextColor
        animationProgress == 1f -> colors.textColor
        else -> colors.textColor.copy(alpha = animationProgress)
            .compositeOver(defaultTextColor)
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
                                UiRes.string.anime_character_image_content_description
                            } else {
                                UiRes.string.anime_voice_actor_image
                            }
                        ),
                        modifier = Modifier
                            .size(width = 40.dp, height = 40.dp)
                            .sharedElement(
                                innerSharedTransitionKey,
                                innerSharedTransitionIdentifier,
                                zIndexInOverlay = 1f,
                            )
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

fun LazyGridScope.charactersSection(
    titleRes: StringResource,
    characters: LazyPagingItems<CharacterDetails>,
    viewAllRoute: (() -> NavDestination)?,
    viewAllContentDescriptionTextRes: StringResource? = null,
    staffDetailsRoute: StaffDetailsRoute,
) {
    if (characters.itemCount == 0) return
    item(
        key = "charactersHeader-$titleRes",
        span = GridUtils.maxSpanFunction,
        contentType = "detailsSectionHeader",
    ) {
        val navHostController = LocalNavHostController.current
        DetailsSectionHeader(
            text = stringResource(titleRes),
            onClickViewAll = if (viewAllRoute != null) {
                {
                    navHostController.navigate(viewAllRoute())
                }
            } else null,
            viewAllContentDescriptionTextRes = viewAllContentDescriptionTextRes,
            modifier = Modifier.recomposeHighlighter()
        )
    }

    item(
        key = "charactersSection-$titleRes",
        span = GridUtils.maxSpanFunction,
        contentType = "charactersSection",
    ) {
        CharactersSection(
            characters = characters,
            staffDetailsRoute = staffDetailsRoute,
        )
    }
}

fun LazyGridScope.charactersSection(
    titleRes: StringResource,
    charactersInitial: List<CharacterDetails>,
    charactersDeferred: () -> LazyPagingItems<CharacterDetails>,
    viewAllRoute: (() -> NavDestination)?,
    viewAllContentDescriptionTextRes: StringResource? = null,
    staffDetailsRoute: StaffDetailsRoute,
) {
    if (charactersDeferred().itemCount.coerceAtLeast(charactersInitial.size) == 0) return
    item(
        key = "charactersHeader-$titleRes",
        span = GridUtils.maxSpanFunction,
        contentType = "detailsSectionHeader",
    ) {
        val navHostController = LocalNavHostController.current
        DetailsSectionHeader(
            text = stringResource(titleRes),
            onClickViewAll = if (viewAllRoute != null) {
                {
                    navHostController.navigate(viewAllRoute())
                }
            } else null,
            viewAllContentDescriptionTextRes = viewAllContentDescriptionTextRes,
            modifier = Modifier.recomposeHighlighter()
        )
    }

    item(
        key = "charactersSection-$titleRes",
        span = GridUtils.maxSpanFunction,
        contentType = "charactersSection",
    ) {
        CharactersSection(
            charactersInitial = charactersInitial,
            charactersDeferred = { charactersDeferred() },
            staffDetailsRoute = staffDetailsRoute,
        )
    }
}

@Composable
private fun CharactersSection(
    characters: LazyPagingItems<CharacterDetails>,
    staffDetailsRoute: StaffDetailsRoute,
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
                staffDetailsRoute = staffDetailsRoute,
            )
        }
    }
}

@Composable
fun CharactersSection(
    charactersInitial: List<CharacterDetails>,
    charactersDeferred: @Composable () -> LazyPagingItems<CharacterDetails>,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    showVoiceActorAsMain: Boolean = false,
    staffDetailsRoute: StaffDetailsRoute,
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
                    staffDetailsRoute = staffDetailsRoute,
                )
            }
        } else {
            items(charactersInitial, key = { it.id }, contentType = { "character" }) {
                CharactersSectionItem(
                    character = it,
                    showVoiceActorAsMain = showVoiceActorAsMain,
                    staffDetailsRoute = staffDetailsRoute,
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
    character: CharacterDetails?,
    showVoiceActorAsMain: Boolean,
    staffDetailsRoute: StaffDetailsRoute,
) {
    val sharedTransitionScopeKey = character?.id.toString()
    SharedTransitionKeyScope(sharedTransitionScopeKey) {
        val navHostController = LocalNavHostController.current
        val voiceActor = AniListUtils.selectVoiceActor(character?.languageToVoiceActor)
        val characterName = character?.character?.name?.primaryName()
        val image = if (showVoiceActorAsMain) voiceActor?.image else character?.image
        val innerImage = if (showVoiceActorAsMain) character?.image else voiceActor?.image
        val imageState = rememberImageStateBelowInnerImage(image, innerImage)
        val innerImageState = rememberCoilImageState(innerImage)
        val characterSharedTransitionKey =
            character?.id?.let { SharedTransitionKey.makeKeyForId(it) }

        // Manually attach a scope to mirror StaffListRow
        val voiceActorTransitionKey = voiceActor?.id?.let {
            SharedTransitionKey.makeKeyWithScope(it, "staff_card", voiceActor.idWithRole)
        }
        val onClickCharacter: () -> Unit = {
            if (character?.character != null) {
                navHostController.navigate(
                    CharacterDestinations.CharacterDetails(
                        characterId = character.id,
                        sharedTransitionScopeKey = sharedTransitionScopeKey,
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
                navHostController.navigate(
                    staffDetailsRoute(
                        voiceActor.id,
                        voiceActorTransitionKey,
                        voiceActorName,
                        voiceActorSubtitle,
                        if (showVoiceActorAsMain) {
                            imageState
                        } else {
                            innerImageState
                        }.toImageState(),
                        null,
                    )
                )
            }
        }
        CharactersSectionItem(
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
}

@Composable
fun CharactersSectionItem(
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
    CharacterSmallCard(
        sharedTransitionKey = sharedTransitionKey,
        sharedTransitionIdentifier = sharedTransitionIdentifier,
        innerSharedTransitionKey = innerSharedTransitionKey,
        innerSharedTransitionIdentifier = innerSharedTransitionIdentifier,
        image = imageState?.uri,
        innerImage = innerImageState?.uri,
        imageState = imageState,
        innerImageState = innerImageState,
        onClick = onClick,
        onClickInnerImage = onClickInnerImage,
        isStaffMain = isStaffMain,
    ) { textColor ->
        AutoResizeHeightText(
            text = text(),
            color = textColor,
            style = MaterialTheme.typography.bodyMedium.copy(lineBreak = LineBreak.Heading),
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
    imageWidth: Dp,
    minHeight: Dp,
    character: CharacterDetails?,
    voiceActorLanguage: VoiceActorLanguageOption = LocalLanguageOptionVoiceActor.current.first,
    staffDetailsRoute: StaffDetailsRoute,
) {
    val sharedTransitionScopeKey = character?.id.toString()
    SharedTransitionKeyScope(sharedTransitionScopeKey) {
        val coverImageState = rememberCoilImageState(character?.character?.image?.large)
        val navHostController = LocalNavHostController.current
        val characterName = character?.character?.name?.primaryName()

        val voiceActor = AniListUtils.selectVoiceActor(
            map = character?.languageToVoiceActor,
            voiceActorLanguage = voiceActorLanguage
        )
        val characterSharedTransitionKey =
            character?.id?.let { SharedTransitionKey.makeKeyForId(it) }
        val voiceActorSharedTransitionKey =
            voiceActor?.id?.let { SharedTransitionKey.makeKeyForId(it) }
        ElevatedCard(
            onClick = {
                character?.character?.let {
                    navHostController.navigate(
                        CharacterDestinations.CharacterDetails(
                            characterId = character.id,
                            sharedTransitionScopeKey = sharedTransitionScopeKey,
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
                                    character?.roleTextRes ?: Res.string.anime_character_role_main
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
                                    navHostController.navigate(
                                        staffDetailsRoute(
                                            voiceActor.staff.id.toString(),
                                            voiceActorSharedTransitionKey,
                                            voiceActorName,
                                            voiceActorSubtitle,
                                            voiceActorImageState.toImageState(),
                                            null,
                                        )
                                    )
                                }
                            },
                        contentScale = ContentScale.Crop,
                        contentDescriptionTextRes = UiRes.string.anime_voice_actor_image
                    )
                }
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

fun LazyListScope.horizontalCharactersRow(characters: List<CharacterNavigationData>) {
    items(characters, key = { it.id }) {
        val navHostController = LocalNavHostController.current
        val characterName = it.name?.primaryName()
        val imageState = rememberCoilImageState(it.image?.large)
        val sharedTransitionKey = SharedTransitionKey.makeKeyForId(it.id.toString())
        val sharedTransitionScopeKey = LocalSharedTransitionPrefixKeys.current
        ListRowSmallImage(
            ignored = false,
            imageState = imageState,
            contentDescriptionTextRes = UiRes.string.anime_character_image_content_description,
            onClick = {
                navHostController.navigate(
                    CharacterDestinations.CharacterDetails(
                        characterId = it.id.toString(),
                        sharedTransitionScopeKey = sharedTransitionScopeKey,
                        headerParams = CharacterHeaderParams(
                            name = characterName,
                            subtitle = null,
                            favorite = null,
                            coverImage = imageState.toImageState(),
                        )
                    )
                )
            },
            width = 80.dp,
            height = 120.dp,
            modifier = Modifier.sharedElement(sharedTransitionKey, "character_image")
        )
    }
}
