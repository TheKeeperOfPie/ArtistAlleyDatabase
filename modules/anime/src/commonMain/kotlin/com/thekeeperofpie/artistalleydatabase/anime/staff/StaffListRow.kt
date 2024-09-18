package com.thekeeperofpie.artistalleydatabase.anime.staff

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_character_image_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_cover_image_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_staff_image_long_press_preview
import coil3.annotation.ExperimentalCoilApi
import com.anilist.StaffSearchQuery
import com.anilist.UserFavoritesStaffQuery
import com.anilist.fragment.CharacterNavigationData
import com.anilist.fragment.MediaNavigationData
import com.anilist.fragment.StaffNavigationData
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaWithListStatusEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaListQuickEditIconButton
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffUtils.primaryName
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffUtils.subtitleName
import com.thekeeperofpie.artistalleydatabase.anime.ui.ListRowFavoritesSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.ListRowSmallImage
import com.thekeeperofpie.artistalleydatabase.anime.ui.StaffCoverImage
import com.thekeeperofpie.artistalleydatabase.anime.utils.LocalFullscreenImageHandler
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class,
    ExperimentalCoilApi::class
)
object StaffListRow {

    private val MIN_HEIGHT = 156.dp
    private val IMAGE_WIDTH = 108.dp
    private val MEDIA_WIDTH = 80.dp
    private val MEDIA_HEIGHT = 120.dp

    @Composable
    operator fun invoke(
        viewer: AniListViewer?,
        entry: Entry?,
        onClickListEdit: (MediaNavigationData) -> Unit,
    ) {
        val coverImageState = rememberCoilImageState(entry?.staff?.image?.large)
        val navigationCallback = LocalNavigationCallback.current
        val staffName = entry?.staff?.name?.primaryName()
        val staffSubtitle = entry?.staff?.name?.subtitleName()
        val sharedTransitionKey = entry?.staff?.id?.toString()
            ?.let { SharedTransitionKey.makeKeyForId(it) }
        val onClick = {
            if (entry != null) {
                navigationCallback.navigate(
                    AnimeDestination.StaffDetails(
                        staffId = entry.staff.id.toString(),
                        sharedTransitionKey = sharedTransitionKey,
                        headerParams = StaffHeaderParams(
                            name = staffName,
                            subtitle = staffSubtitle,
                            coverImage = coverImageState.toImageState(),
                            favorite = null,
                        )
                    )
                )
            }
        }
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = MIN_HEIGHT)
                .clickable(
                    enabled = true, // TODO: placeholder,
                    onClick = onClick,
                )
        ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                StaffImage(
                    entry = entry,
                    coverImageState = coverImageState,
                    onClick = onClick,
                )

                Column(
                    modifier = Modifier
                        .heightIn(min = MIN_HEIGHT)
                        .padding(bottom = 12.dp)
                ) {
                    Row(Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            NameText(entry = entry)
                            OccupationsText(entry = entry)
                        }

                        ListRowFavoritesSection(
                            loading = entry == null,
                            favorites = entry?.favorites,
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    CharactersAndMediaRow(
                        viewer = viewer,
                        entry = entry,
                        onClickListEdit = onClickListEdit,
                    )
                }
            }
        }
    }

    @Composable
    private fun StaffImage(
        entry: Entry?,
        coverImageState: CoilImageState,
        onClick: () -> Unit,
    ) {
        val fullscreenImageHandler = LocalFullscreenImageHandler.current
        StaffCoverImage(
            imageState = coverImageState,
            image = coverImageState.request().build(),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .fillMaxHeight()
                .heightIn(min = MIN_HEIGHT)
                .width(IMAGE_WIDTH)
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
                        Res.string.anime_staff_image_long_press_preview
                    ),
                ),
            contentScale = ContentScale.Crop
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
                .padding(horizontal = 12.dp, vertical = 10.dp)
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
        viewer: AniListViewer?,
        entry: Entry?,
        onClickListEdit: (MediaNavigationData) -> Unit,
    ) {
        val media = entry?.media.orEmpty()
        val characters = entry?.characters.orEmpty()
        if (media.isEmpty() && characters.isEmpty()) return
        val density = LocalDensity.current
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                // SubcomposeLayout doesn't support fill max width, so use a really large number.
                // The parent will clamp the actual width so all content still fits on screen.
                .size(width = LocalWindowConfiguration.current.screenWidthDp, height = MEDIA_HEIGHT)
        ) {
            items(characters, key = { it.id }) {
                val navigationCallback = LocalNavigationCallback.current
                val characterName = it.name?.primaryName()
                val imageState = rememberCoilImageState(it.image?.large)
                val sharedTransitionKey = SharedTransitionKey.makeKeyForId(it.id.toString())
                val sharedTransitionScopeKey = LocalSharedTransitionPrefixKeys.current
                ListRowSmallImage(
                    density = density,
                    ignored = false,
                    imageState = imageState,
                    contentDescriptionTextRes = Res.string.anime_character_image_content_description,
                    onClick = {
                        navigationCallback.navigate(
                            AnimeDestination.CharacterDetails(
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
                    width = MEDIA_WIDTH,
                    height = MEDIA_HEIGHT,
                    modifier = Modifier.sharedElement(sharedTransitionKey, "character_image")
                )
            }

            items(media, key = { it.media.id }) {
                Box {
                    val navigationCallback = LocalNavigationCallback.current
                    val title = it.media.title?.primaryTitle()
                    val sharedTransitionKey =
                        SharedTransitionKey.makeKeyForId(it.media.id.toString())
                    val sharedContentState = rememberSharedContentState(sharedTransitionKey, "media_image")
                    val imageState = rememberCoilImageState(it.media.coverImage?.extraLarge)
                    ListRowSmallImage(
                        density = density,
                        ignored = it.mediaFilterable.ignored,
                        imageState = imageState,
                        contentDescriptionTextRes = Res.string.anime_media_cover_image_content_description,
                        onClick = {
                            navigationCallback.navigate(
                                AnimeDestination.MediaDetails(
                                    mediaId = it.media.id.toString(),
                                    title = title,
                                    coverImage = imageState.toImageState(),
                                    sharedTransitionKey = sharedTransitionKey,
                                    headerParams = MediaHeaderParams(
                                        coverImage = imageState.toImageState(),
                                        title = title,
                                        mediaWithListStatus = it.media,
                                    )
                                )
                            )
                        },
                        width = MEDIA_WIDTH,
                        height = MEDIA_HEIGHT,
                        modifier = Modifier.sharedElement(sharedTransitionKey, "media_image")
                    )

                    if (viewer != null) {
                        MediaListQuickEditIconButton(
                            viewer = viewer,
                            mediaType = it.media.type,
                            media = it.mediaFilterable,
                            maxProgress = MediaUtils.maxProgress(it.media),
                            maxProgressVolumes = it.media.volumes,
                            onClick = { onClickListEdit(it.media) },
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
        val staff: StaffNavigationData,
        val media: List<MediaWithListStatusEntry>,
        val characters: List<CharacterNavigationData>,
        val favorites: Int?,
        val occupations: List<String>,
    ) {
        constructor(
            staff: StaffSearchQuery.Data.Page.Staff,
            media: List<MediaWithListStatusEntry>,
        ) : this(
            staff = staff,
            media = media,
            characters = staff.characters?.nodes?.filterNotNull().orEmpty().distinctBy { it.id },
            favorites = staff.favourites,
            occupations = staff.primaryOccupations?.filterNotNull().orEmpty(),
        )
        constructor(
            staff: UserFavoritesStaffQuery.Data.User.Favourites.Staff.Node,
            media: List<MediaWithListStatusEntry>,
        ) : this(
            staff = staff,
            media = media,
            characters = staff.characters?.nodes?.filterNotNull().orEmpty().distinctBy { it.id },
            favorites = staff.favourites,
            occupations = staff.primaryOccupations?.filterNotNull().orEmpty(),
        )
    }
}
