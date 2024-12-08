package com.thekeeperofpie.artistalleydatabase.anime.studio

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.ui.generated.resources.Res
import artistalleydatabase.modules.anime.ui.generated.resources.anime_media_cover_image_content_description
import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.fragment.StudioListRowFragment
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaWithListStatusEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.media.data.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaListQuickEditIconButton
import com.thekeeperofpie.artistalleydatabase.anime.ui.ListRowFavoritesSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.ListRowSmallImage
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.animateSharedTransitionWithOtherState
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.rememberSharedContentState
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.fadingEdgeEnd
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
object StudioListRow {

    @Composable
    operator fun invoke(
        viewer: AniListViewer?,
        entry: Entry?,
        onClickListEdit: (MediaNavigationData) -> Unit,
        modifier: Modifier = Modifier,
        mediaWidth: Dp = 120.dp,
        mediaHeight: Dp = 180.dp,
    ) {
        val navigationCallback = LocalNavigationCallback.current
        ElevatedCard(
            onClick = {
                if (entry != null) {
                    navigationCallback.navigate(
                        AnimeDestination.StudioMedias(
                            studioId = entry.studio.id.toString(),
                            name = entry.studio.name,
                        )
                    )
                }
            },
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(bottom = 12.dp)
            ) {
                Row(Modifier.fillMaxWidth()) {
                    NameText(
                        entry = entry,
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentHeight(Alignment.Top)
                    )

                    ListRowFavoritesSection(
                        loading = entry == null,
                        favorites = entry?.studio?.favourites,
                    )
                }

                MediaRow(
                    viewer = viewer,
                    entry = entry,
                    onClickListEdit = onClickListEdit,
                    mediaWidth = mediaWidth,
                    mediaHeight = mediaHeight,
                )
            }
        }
    }

    @Composable
    private fun NameText(entry: Entry?, modifier: Modifier = Modifier) {
        Text(
            text = entry?.studio?.name ?: "Loading...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            modifier = modifier
                .padding(horizontal = 16.dp, vertical = 10.dp)
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
        mediaWidth: Dp,
        mediaHeight: Dp,
    ) {
        val media = entry?.media?.takeIf { it.isNotEmpty() }
            ?: listOf(null, null, null, null, null)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                // SubcomposeLayout doesn't support fill max width, so use a really large number.
                // The parent will clamp the actual width so all content still fits on screen.
                .size(width = LocalWindowConfiguration.current.screenWidthDp, height = mediaHeight)
                .fadingEdgeEnd()
        ) {
            itemsIndexed(
                media,
                key = { index, item -> item?.media?.id ?: "placeholder_$index" },
            ) { index, item ->
                Box {
                    val navigationCallback = LocalNavigationCallback.current
                    val title = item?.media?.title?.primaryTitle()
                    val sharedTransitionKey = item?.media?.id?.toString()
                        ?.let { SharedTransitionKey.makeKeyForId(it) }
                    val sharedContentState =
                        rememberSharedContentState(sharedTransitionKey, "media_image")
                    val imageState = rememberCoilImageState(item?.media?.coverImage?.extraLarge)
                    ListRowSmallImage(
                        ignored = item?.mediaFilterable?.ignored ?: false,
                        imageState = imageState,
                        contentDescriptionTextRes = Res.string.anime_media_cover_image_content_description,
                        onClick = {
                            if (item != null) {
                                navigationCallback.navigate(
                                    AnimeDestination.MediaDetails(
                                        mediaId = item.media.id.toString(),
                                        title = title,
                                        coverImage = imageState.toImageState(),
                                        sharedTransitionKey = sharedTransitionKey,
                                        headerParams = MediaHeaderParams(
                                            coverImage = imageState.toImageState(),
                                            title = title,
                                            mediaWithListStatus = item.media,
                                        )
                                    )
                                )
                            }
                        },
                        width = mediaWidth,
                        height = mediaHeight,
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
        val studio: StudioListRowFragment,
        val media: List<MediaWithListStatusEntry>,
    )
}
