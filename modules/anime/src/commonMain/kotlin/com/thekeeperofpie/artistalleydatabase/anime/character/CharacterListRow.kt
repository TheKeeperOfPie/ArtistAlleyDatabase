package com.thekeeperofpie.artistalleydatabase.anime.character

import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_character_favorites_icon_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_character_image_long_press_preview
import artistalleydatabase.modules.anime.generated.resources.anime_staff_image_content_description
import artistalleydatabase.modules.anime.ui.generated.resources.anime_media_cover_image_content_description
import co.touchlab.kermit.Logger
import coil3.request.crossfade
import com.anilist.data.CharacterAdvancedSearchQuery.Data.Page.Character
import com.anilist.data.fragment.CharacterNavigationData
import com.anilist.data.fragment.CharacterWithRoleAndFavorites
import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.fragment.StaffNavigationData
import com.anilist.data.type.CharacterRole
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaWithListStatusEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaListQuickEditIconButton
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffUtils.subtitleName
import com.thekeeperofpie.artistalleydatabase.anime.ui.CharacterCoverImage
import com.thekeeperofpie.artistalleydatabase.anime.ui.ListRowSmallImage
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalSharedTransitionPrefixKeys
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.animateSharedTransitionWithOtherState
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.rememberSharedContentState
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.request
import org.jetbrains.compose.resources.stringResource
import artistalleydatabase.modules.anime.ui.generated.resources.Res as UiRes

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
object CharacterListRow {

    private val MIN_HEIGHT = 156.dp
    private val IMAGE_WIDTH = 108.dp
    private val MEDIA_WIDTH = 80.dp
    private val MEDIA_HEIGHT = 120.dp

    @Composable
    operator fun invoke(
        viewer: AniListViewer?,
        entry: Entry?,
        onClickListEdit: (MediaNavigationData) -> Unit,
        modifier: Modifier = Modifier,
        showRole: Boolean = false,
    ) {
        val coverImageState = rememberCoilImageState(entry?.character?.image?.large)
        val navigationCallback = LocalNavigationCallback.current
        val characterName = entry?.character?.name?.primaryName()
        val characterSharedTransitionKey = entry?.character?.id?.toString()?.let { SharedTransitionKey.makeKeyForId(it) }
        val sharedTransitionScopeKey = LocalSharedTransitionPrefixKeys.current
        val onClick = {
            if (entry != null) {
                navigationCallback.navigate(
                    AnimeDestination.CharacterDetails(
                        characterId = entry.character.id.toString(),
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
                    imageState = coverImageState,
                    sharedTransitionKey = characterSharedTransitionKey,
                    entry = entry,
                    onClick = onClick,
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
        imageState: CoilImageState,
        sharedTransitionKey: SharedTransitionKey?,
        entry: Entry?,
        onClick: () -> Unit,
    ) {
        val fullscreenImageHandler = LocalFullscreenImageHandler.current
        CharacterCoverImage(
            imageState = imageState,
            image = imageState.request()
                .crossfade(true)
                .build(),
            modifier = Modifier
                .fillMaxHeight()
                .heightIn(min = MIN_HEIGHT)
                .width(IMAGE_WIDTH)
                .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                .sharedElement(sharedTransitionKey, "character_image")
                .also {
                    Logger.d("SharedDebug") { "CharacterListRow Image = $sharedTransitionKey - character_image" }
                }
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
                        Res.string.anime_character_image_long_press_preview
                    ),
                ),
            contentScale = ContentScale.Crop
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
                        Res.string.anime_character_favorites_icon_content_description
                    ),
                )
            }
        }
    }

    @Composable
    private fun MediaRow(
        viewer: AniListViewer?,
        entry: Entry?,
        onClickListEdit: (MediaNavigationData) -> Unit,
    ) {
        val media = entry?.media?.takeIf { it.isNotEmpty() }
            ?: listOf(null, null, null, null, null)
        val density = LocalDensity.current
        val navigationCallback = LocalNavigationCallback.current
        val voiceActor = entry?.voiceActor()
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                // SubcomposeLayout doesn't support fill max width, so use a really large number.
                // The parent will clamp the actual width so all content still fits on screen.
                .size(width = LocalWindowConfiguration.current.screenWidthDp, height = MEDIA_HEIGHT)
        ) {
            if (voiceActor?.image?.large != null) {
                val staffId = voiceActor.id
                item(staffId) {
                    val voiceActorName = voiceActor.name?.primaryName()
                    val voiceActorSubtitle = voiceActor.name?.subtitleName()
                    val voiceActorImageState = rememberCoilImageState(voiceActor.image?.large)
                    val sharedTransitionKey = SharedTransitionKey.makeKeyForId(voiceActor.id.toString())
                    ListRowSmallImage(
                        density = density,
                        ignored = false,
                        imageState = voiceActorImageState,
                        contentDescriptionTextRes = Res.string.anime_staff_image_content_description,
                        onClick = {
                            navigationCallback.navigate(
                                AnimeDestination.StaffDetails(
                                    staffId = voiceActor.id.toString(),
                                    sharedTransitionKey = sharedTransitionKey,
                                    headerParams = StaffHeaderParams(
                                        name = voiceActorName,
                                        subtitle = voiceActorSubtitle,
                                        coverImage = voiceActorImageState.toImageState(),
                                        favorite = null,
                                    )
                                )
                            )
                        },
                        width = MEDIA_WIDTH,
                        height = MEDIA_HEIGHT,
                        modifier = Modifier.sharedElement(sharedTransitionKey, "staff_image")
                    )
                }
            }

            itemsIndexed(
                media,
                key = { index, item -> item?.media?.id ?: "placeholder_$index" },
            ) { index, item ->
                Box {
                    val languageOptionMedia = LocalLanguageOptionMedia.current
                    val imageState = rememberCoilImageState(item?.media?.coverImage?.extraLarge)
                    val sharedTransitionKey = item?.media?.id?.toString()
                        ?.let { SharedTransitionKey.makeKeyForId(it) }
                    val sharedContentState =
                        rememberSharedContentState(sharedTransitionKey, "media_image")
                    ListRowSmallImage(
                        density = density,
                        ignored = item?.mediaFilterable?.ignored ?: false,
                        imageState = imageState,
                        contentDescriptionTextRes = UiRes.string.anime_media_cover_image_content_description,
                        onClick = {
                            if (item != null) {
                                navigationCallback.navigate(
                                    AnimeDestination.MediaDetails(
                                        mediaNavigationData = item.media,
                                        coverImage = imageState.toImageState(),
                                        languageOptionMedia = languageOptionMedia,
                                        sharedTransitionKey = sharedTransitionKey,
                                    )
                                )
                            }
                        },
                        width = MEDIA_WIDTH,
                        height = MEDIA_HEIGHT,
                        modifier = Modifier.sharedElement(sharedContentState)
                    )

                    if (viewer != null && item != null) {
                        MediaListQuickEditIconButton(
                            viewer = viewer,
                            mediaType = item.media.type,
                            media = item.mediaFilterable,
                            maxProgress = MediaUtils.maxProgress(item.media),
                            maxProgressVolumes = item.media.volumes,
                            onClick = { onClickListEdit(item.media) },
                            padding = 6.dp,
                            modifier = Modifier
                                .animateSharedTransitionWithOtherState(sharedContentState)
                                .align(Alignment.BottomStart)
                        )
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
