package com.thekeeperofpie.artistalleydatabase.anime.studios

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
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
import com.anilist.data.fragment.StudioListRowFragment
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.anime.ui.ListRowFavoritesSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.fadingEdgeEnd
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
object StudioListRow {

    @Composable
    operator fun <MediaEntry> invoke(
        entry: Entry<MediaEntry>?,
        mediaRow: LazyListScope.(List<MediaEntry?>) -> Unit,
        modifier: Modifier = Modifier,
        mediaHeight: Dp = 180.dp,
    ) {
        val navigationController = LocalNavigationController.current
        ElevatedCard(
            onClick = {
                if (entry != null) {
                    navigationController.navigate(
                        StudioDestinations.StudioMedias(
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
                    entry = entry,
                    mediaHeight = mediaHeight,
                    mediaRow = mediaRow,
                )
            }
        }
    }

    @Composable
    private fun NameText(entry: Entry<*>?, modifier: Modifier = Modifier) {
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
    private fun <MediaEntry> MediaRow(
        entry: Entry<MediaEntry>?,
        mediaHeight: Dp,
        mediaRow: LazyListScope.(List<MediaEntry?>) -> Unit,
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
            mediaRow(media)
        }
    }

    interface Entry<MediaEntry> {
        val studio: StudioListRowFragment
        val media: List<MediaEntry>
    }
}
