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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.annotation.ExperimentalCoilApi
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.size.Dimension
import com.anilist.fragment.MediaNavigationData
import com.anilist.fragment.UserNavigationData
import com.thekeeperofpie.artistalleydatabase.android_utils.MutableSingle
import com.thekeeperofpie.artistalleydatabase.android_utils.getValue
import com.thekeeperofpie.artistalleydatabase.android_utils.setValue
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestinations
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaWithListStatusEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaListQuickEditIconButton
import com.thekeeperofpie.artistalleydatabase.anime.ui.ListRowSmallImage
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserAvatarImage
import com.thekeeperofpie.artistalleydatabase.anime.utils.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.anime.utils.items
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.LocalColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.fadingEdgeEnd
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.PlaceholderHighlight
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.placeholder
import com.thekeeperofpie.artistalleydatabase.compose.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.AutoSharedElement
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.animateSharedTransitionWithOtherState
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.rememberSharedContentState
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.sharedElement
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalSharedTransitionApi::class, ExperimentalCoilApi::class
)
object UserListRow {

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
    ) {
        var imageWidthToHeightRatio by remember { MutableSingle(1f) }
        val navigationCallback = LocalNavigationCallback.current
        ElevatedCard(
            onClick = {
                if (entry != null) {
                    navigationCallback.onUserClick(
                        entry.user,
                        imageWidthToHeightRatio,
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = MIN_HEIGHT)
        ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                UserImage(
                    screenKey = screenKey,
                    entry = entry,
                    onClick = {
                        if (entry != null) {
                            navigationCallback.onUserClick(
                                entry.user,
                                imageWidthToHeightRatio,
                            )
                        }
                    },
                    onRatioAvailable = { imageWidthToHeightRatio = it }
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
    private fun UserImage(
        screenKey: String,
        entry: Entry?,
        onClick: () -> Unit,
        onRatioAvailable: (Float) -> Unit,
    ) {
        val fullscreenImageHandler = LocalFullscreenImageHandler.current
        val colorCalculationState = LocalColorCalculationState.current
        UserAvatarImage(
            screenKey = screenKey,
            userId = entry?.user?.id.toString(),
            image = ImageRequest.Builder(LocalContext.current)
                .data(entry?.user?.avatar?.large)
                .crossfade(true)
                .allowHardware(colorCalculationState.allowHardware(entry?.user?.id?.toString()))
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
                        id = entry.user.id.toString(),
                        image = it.result.image,
                        colorCalculationState = colorCalculationState,
                    )
                }
            },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .fillMaxHeight()
                .heightIn(min = MIN_HEIGHT)
                .width(IMAGE_WIDTH)
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
                        R.string.anime_user_image_long_press_preview
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
        screenKey: String,
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
                AutoSharedElement(
                    key = "anime_media_${it?.media?.id}_image",
                    screenKey = screenKey
                ) {
                    Box {
                        val navigationCallback = LocalNavigationCallback.current
                        val languageOptionMedia = LocalLanguageOptionMedia.current
                        val sharedTransitionKey = it?.media?.id?.toString()
                            ?.let { SharedTransitionKey.makeKeyForId(it) }
                        val sharedContentState = rememberSharedContentState(sharedTransitionKey, "media_image")
                        val imageState = rememberCoilImageState(it?.media?.coverImage?.extraLarge)
                        ListRowSmallImage(
                            density = density,
                            ignored = it?.ignored ?: false,
                            imageState = imageState,
                            contentDescriptionTextRes = R.string.anime_media_cover_image_content_description,
                            width = MEDIA_WIDTH,
                            height = MEDIA_HEIGHT,
                            onClick = {
                                if (it?.media != null) {
                                    navigationCallback.navigate(
                                        AnimeDestinations.MediaDetails(
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
    }

    data class Entry(val user: UserNavigationData, val media: List<MediaWithListStatusEntry>)
}
