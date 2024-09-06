package com.thekeeperofpie.artistalleydatabase.anime.user

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.annotation.ExperimentalCoilApi
import coil3.request.crossfade
import coil3.size.Dimension
import com.anilist.fragment.MediaNavigationData
import com.anilist.fragment.UserNavigationData
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_cover_image_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_user_image_long_press_preview
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaWithListStatusEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaListQuickEditIconButton
import com.thekeeperofpie.artistalleydatabase.anime.ui.ListRowSmallImage
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserAvatarImage
import com.thekeeperofpie.artistalleydatabase.anime.utils.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoHeightText
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.animateSharedTransitionWithOtherState
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.rememberSharedContentState
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.fadingEdgeEnd
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.request
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.items

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalSharedTransitionApi::class, ExperimentalCoilApi::class
)
object UserListRow {

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
        val avatarImageState = rememberCoilImageState(entry?.user?.avatar?.large)
        val navigationCallback = LocalNavigationCallback.current
        val sharedTransitionKey = entry?.user?.id?.toString()
            ?.let { SharedTransitionKey.makeKeyForId(it) }
        val onUserClick = {
            if (entry != null) {
                navigationCallback.navigate(
                    AnimeDestination.User(
                        userId = entry.user.id.toString(),
                        sharedTransitionKey = sharedTransitionKey,
                        headerParams = UserHeaderParams(
                            name = entry.user.name,
                            bannerImage = null,
                            coverImage = avatarImageState.toImageState(),
                        ),
                    )
                )
            }
        }
        ElevatedCard(
            onClick = onUserClick,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = MIN_HEIGHT)
        ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                UserImage(
                    entry = entry,
                    imageState = avatarImageState,
                    onClick = onUserClick,
                    modifier = Modifier.sharedElement(sharedTransitionKey, "user_image")
                )

                Column(
                    modifier = Modifier
                        .heightIn(min = MIN_HEIGHT)
                        .padding(bottom = 12.dp)
                ) {
                    NameText(
                        entry = entry,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(Alignment.Top)
                    )

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
    private fun UserImage(
        entry: Entry?,
        onClick: () -> Unit,
        imageState: CoilImageState,
        modifier: Modifier,
    ) {
        val fullscreenImageHandler = LocalFullscreenImageHandler.current
        UserAvatarImage(
            imageState = imageState,
            image = imageState.request()
                .crossfade(true)
                .size(
                    width = Dimension.Pixels(LocalDensity.current.run { IMAGE_WIDTH.roundToPx() }),
                    height = Dimension.Undefined
                )
                .build(),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxHeight()
                .heightIn(min = MIN_HEIGHT)
                .width(IMAGE_WIDTH)
                .then(modifier)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .placeholder(
                    visible = entry == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        entry?.user?.avatar?.large?.let(fullscreenImageHandler::openImage)
                    },
                    onLongClickLabel = stringResource(
                        Res.string.anime_user_image_long_press_preview
                    ),
                )
        )
    }

    @Composable
    private fun NameText(entry: Entry?, modifier: Modifier = Modifier) {
        AutoHeightText(
            text = entry?.user?.name ?: "Placeholder username",
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
    private fun MediaRow(
        viewer: AniListViewer?,
        entry: Entry?,
        onClickListEdit: (MediaNavigationData) -> Unit,
    ) {
        val media = entry?.media?.takeIf { it.isNotEmpty() } ?: return
        val context = LocalContext.current
        val density = LocalDensity.current
        LazyRow(
            contentPadding = PaddingValues(start = 16.dp, end = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(top = 8.dp)
                // SubcomposeLayout doesn't support fill max width, so use a really large number.
                // The parent will clamp the actual width so all content still fits on screen.
                .size(width = LocalConfiguration.current.screenWidthDp.dp, height = MEDIA_HEIGHT)
                .fadingEdgeEnd()
        ) {
            items(
                data = media,
                placeholderCount = 5,
                key = { it.media.id },
                contentType = { "media" },
            ) {
                Box {
                    val navigationCallback = LocalNavigationCallback.current
                    val languageOptionMedia = LocalLanguageOptionMedia.current
                    val sharedTransitionKey = it?.media?.id?.toString()
                        ?.let { SharedTransitionKey.makeKeyForId(it) }
                    val sharedContentState =
                        rememberSharedContentState(sharedTransitionKey, "media_image")
                    val imageState = rememberCoilImageState(it?.media?.coverImage?.extraLarge)
                    ListRowSmallImage(
                        density = density,
                        ignored = it?.ignored ?: false,
                        imageState = imageState,
                        contentDescriptionTextRes = Res.string.anime_media_cover_image_content_description,
                        width = MEDIA_WIDTH,
                        height = MEDIA_HEIGHT,
                        onClick = {
                            if (it?.media != null) {
                                navigationCallback.navigate(
                                    AnimeDestination.MediaDetails(
                                        mediaNavigationData = it.media,
                                        coverImage = imageState.toImageState(),
                                        languageOptionMedia = languageOptionMedia,
                                        sharedTransitionKey = sharedTransitionKey,
                                    )
                                )
                            }
                        },
                        modifier = Modifier.sharedElement(sharedContentState)
                    )

                    if (viewer != null && it != null) {
                        MediaListQuickEditIconButton(
                            viewer = viewer,
                            mediaType = it.media.type,
                            media = it,
                            maxProgress = MediaUtils.maxProgress(it.media),
                            maxProgressVolumes = it.media.volumes,
                            onClick = { onClickListEdit(it.media) },
                            padding = 6.dp,
                            // API is broken, doesn't return the viewer's entry
                            forceListEditIcon = true,
                            modifier = Modifier
                                .animateSharedTransitionWithOtherState(sharedContentState)
                                .align(Alignment.BottomStart)
                        )
                    }
                }
            }
        }
    }

    data class Entry(val user: UserNavigationData, val media: List<MediaWithListStatusEntry>)
}
