package com.thekeeperofpie.artistalleydatabase.anime.studio

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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anilist.fragment.MediaNavigationData
import com.anilist.fragment.StudioListRowFragment
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaWithListStatusEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaListQuickEditIconButton
import com.thekeeperofpie.artistalleydatabase.anime.ui.ListRowFavoritesSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.ListRowSmallImage
import com.thekeeperofpie.artistalleydatabase.compose.fadingEdgeEnd
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.PlaceholderHighlight
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.placeholder

@OptIn(ExperimentalMaterial3Api::class)
object StudioListRow {

    @Composable
    operator fun invoke(
        screenKey: String,
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
                    navigationCallback.onStudioClick(
                        entry.studio.id.toString(),
                        entry.studio.name
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
                    screenKey = screenKey,
                    viewer = viewer,
                    entry = entry,
                    mediaWidth = mediaWidth,
                    mediaHeight = mediaHeight,
                    onClickListEdit = onClickListEdit,
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
        screenKey: String,
        viewer: AniListViewer?,
        entry: Entry?,
        onClickListEdit: (MediaNavigationData) -> Unit,
        mediaWidth: Dp,
        mediaHeight: Dp,
    ) {
        val media = entry?.media?.takeIf { it.isNotEmpty() }
            ?: listOf(null, null, null, null, null)
        val context = LocalContext.current
        val density = LocalDensity.current
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                // SubcomposeLayout doesn't support fill max width, so use a really large number.
                // The parent will clamp the actual width so all content still fits on screen.
                .size(width = LocalConfiguration.current.screenWidthDp.dp, height = mediaHeight)
                .fadingEdgeEnd()
        ) {
            itemsIndexed(
                media,
                key = { index, item -> item?.media?.id ?: "placeholder_$index" },
            ) { index, item ->
                SharedElement(key = "anime_media_${item?.media?.id}_image", screenKey = screenKey) {
                    Box {
                        val navigationCallback = LocalNavigationCallback.current
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
                            width = mediaWidth,
                            height = mediaHeight,
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
        val studio: StudioListRowFragment,
        val media: List<MediaWithListStatusEntry>,
    )
}
