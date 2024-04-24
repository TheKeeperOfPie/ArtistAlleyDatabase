package com.thekeeperofpie.artistalleydatabase.anime.character

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.size.Dimension
import com.anilist.CharacterAdvancedSearchQuery.Data.Page.Character
import com.anilist.fragment.CharacterNavigationData
import com.anilist.fragment.CharacterWithRoleAndFavorites
import com.anilist.fragment.MediaNavigationData
import com.anilist.fragment.StaffNavigationData
import com.anilist.type.CharacterRole
import com.thekeeperofpie.artistalleydatabase.android_utils.MutableSingle
import com.thekeeperofpie.artistalleydatabase.android_utils.getValue
import com.thekeeperofpie.artistalleydatabase.android_utils.setValue
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaWithListStatusEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaListQuickEditIconButton
import com.thekeeperofpie.artistalleydatabase.anime.ui.CharacterCoverImage
import com.thekeeperofpie.artistalleydatabase.anime.ui.ListRowSmallImage
import com.thekeeperofpie.artistalleydatabase.anime.utils.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.AutoSharedElement
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.LocalColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.PlaceholderHighlight
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.placeholder
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio

@OptIn(ExperimentalFoundationApi::class)
object CharacterListRow {

    private val MIN_HEIGHT = 156.dp
    private val IMAGE_WIDTH = 108.dp
    private val MEDIA_WIDTH = 80.dp
    private val MEDIA_HEIGHT = 120.dp

    @Composable
    operator fun invoke(
        screenKey: String,
        viewer: AniListViewer?,
        entry: Entry?,
        onClickListEdit: (MediaNavigationData) -> Unit,
        modifier: Modifier = Modifier,
        showRole: Boolean = false,
    ) {
        var imageWidthToHeightRatio by remember { MutableSingle(1f) }
        val navigationCallback = LocalNavigationCallback.current
        val colorCalculationState = LocalColorCalculationState.current
        val onClick = {
            if (entry != null) {
                navigationCallback.onCharacterClick(
                    entry.character,
                    null,
                    imageWidthToHeightRatio,
                    colorCalculationState.getColorsNonComposable(entry.character.id.toString())
                        .first,
                )
            }
        }

        ElevatedCard(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = MIN_HEIGHT)
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
                    onRatioAvailable = { imageWidthToHeightRatio = it }
                )

                Column(
                    modifier = Modifier
                        .heightIn(min = MIN_HEIGHT)
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
                        screenKey = screenKey,
                        viewer = viewer,
                        entry = entry,
                        onClickListEdit = onClickListEdit,
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
        onRatioAvailable: (Float) -> Unit,
    ) {
        val fullscreenImageHandler = LocalFullscreenImageHandler.current
        val colorCalculationState = LocalColorCalculationState.current
        CharacterCoverImage(
            screenKey = screenKey,
            characterId = entry?.character?.id?.toString(),
            image = ImageRequest.Builder(LocalContext.current)
                .data(entry?.character?.image?.large)
                .crossfade(true)
                .allowHardware(colorCalculationState.allowHardware(entry?.character?.id?.toString()))
                .size(
                    width = Dimension.Pixels(LocalDensity.current.run { IMAGE_WIDTH.roundToPx() }),
                    height = Dimension.Undefined
                )
                .build(),
            contentScale = ContentScale.Crop,
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
            modifier = Modifier
                .fillMaxHeight()
                .heightIn(min = MIN_HEIGHT)
                .width(IMAGE_WIDTH)
                .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .placeholder(
                    visible = entry == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        entry?.character?.image?.large?.let(fullscreenImageHandler::openImage)
                    },
                    onLongClickLabel = stringResource(
                        R.string.anime_character_image_long_press_preview
                    ),
                )
        )
    }

    @Composable
    private fun NameText(entry: Entry?, modifier: Modifier = Modifier) {
        Text(
            text = entry?.character?.name?.primaryName() ?: "Loading...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            modifier = modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
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
                .padding(horizontal = 12.dp)
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
        screenKey: String,
        viewer: AniListViewer?,
        entry: Entry?,
        onClickListEdit: (MediaNavigationData) -> Unit,
    ) {
        val media = entry?.media?.takeIf { it.isNotEmpty() }
            ?: listOf(null, null, null, null, null)
        val context = LocalContext.current
        val density = LocalDensity.current
        val navigationCallback = LocalNavigationCallback.current
        val colorCalculationState = LocalColorCalculationState.current
        val voiceActor = entry?.voiceActor()
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                // SubcomposeLayout doesn't support fill max width, so use a really large number.
                // The parent will clamp the actual width so all content still fits on screen.
                .size(width = LocalConfiguration.current.screenWidthDp.dp, height = MEDIA_HEIGHT)
        ) {
            if (voiceActor?.image?.large != null) {
                val staffId = voiceActor.id
                item(staffId) {
                    AutoSharedElement(key = "anime_staff_${staffId}_image", screenKey = screenKey) {
                        ListRowSmallImage(
                            context = context,
                            density = density,
                            ignored = false,
                            image = voiceActor.image?.large,
                            contentDescriptionTextRes = R.string.anime_staff_image_content_description,
                            onClick = { ratio ->
                                navigationCallback.onStaffClick(
                                    staff = voiceActor,
                                    favorite = null,
                                    imageWidthToHeightRatio = ratio,
                                    color = colorCalculationState
                                        .getColorsNonComposable(staffId.toString()).first
                                )
                            },
                            width = MEDIA_WIDTH,
                            height = MEDIA_HEIGHT,
                        )
                    }
                }
            }

            itemsIndexed(
                media,
                key = { index, item -> item?.media?.id ?: "placeholder_$index" },
            ) { index, item ->
                AutoSharedElement(key = "anime_media_${item?.media?.id}_image", screenKey = screenKey) {
                    Box {
                        ListRowSmallImage(
                            context = context,
                            density = density,
                            ignored = item?.ignored ?: false,
                            image = item?.media?.coverImage?.extraLarge,
                            contentDescriptionTextRes = R.string.anime_media_cover_image_content_description,
                            onClick = { ratio ->
                                if (item != null) {
                                    navigationCallback.onMediaClick(item.media, ratio)
                                }
                            },
                            width = MEDIA_WIDTH,
                            height = MEDIA_HEIGHT,
                        )

                        if (viewer != null && item != null) {
                            MediaListQuickEditIconButton(
                                viewer = viewer,
                                mediaType = item.media.type,
                                media = item,
                                maxProgress = MediaUtils.maxProgress(item.media),
                                maxProgressVolumes = item.media.volumes,
                                onClick = { onClickListEdit(item.media) },
                                padding = 6.dp,
                                modifier = Modifier.align(Alignment.BottomStart)
                            )
                        }
                    }
                }
            }
        }
    }

    data class Entry(
        val character: CharacterNavigationData,
        val role: CharacterRole?,
        val media: List<MediaWithListStatusEntry>,
        val favorites: Int?,
        private val voiceActors: Map<String?, List<StaffNavigationData>>,
    ) {
        constructor(character: Character, media: List<MediaWithListStatusEntry>) : this(
            character = character,
            role = null,
            media = media,
            favorites = character.favourites,
            voiceActors = character.media?.edges?.filterNotNull()
                ?.flatMap { it.voiceActors?.filterNotNull().orEmpty() }
                ?.groupBy { it.languageV2 }
                .orEmpty()
        )

        constructor(
            character: CharacterWithRoleAndFavorites,
            media: List<MediaWithListStatusEntry>,
        ) : this(
            character = character.node,
            role = character.role,
            media = media,
            favorites = character.node.favourites,
            voiceActors = character.voiceActors?.filterNotNull().orEmpty()
                .groupBy { it.languageV2 }
        )

        @Composable
        fun voiceActor() = AniListUtils.selectVoiceActor(voiceActors)?.firstOrNull()
    }
}
