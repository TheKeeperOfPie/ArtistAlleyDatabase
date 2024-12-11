package com.thekeeperofpie.artistalleydatabase.anime.studios

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope

fun <MediaEntry> LazyGridScope.studiosSection(
    studios: List<StudioListRowFragmentEntry<MediaEntry>>,
    hasMore: Boolean,
    mediaRow: LazyListScope.(List<MediaEntry?>) -> Unit,
) {
    itemsIndexed(
        items = studios,
        key = { _, item -> item.studio.id },
        contentType = { _, _ -> "studio" },
    ) { index, item ->
        SharedTransitionKeyScope("user_favorite_studio_row", item.studio.id.toString()) {
            StudioListRow(
                entry = item,
                mediaHeight = 96.dp,
                mediaRow = { media -> mediaRow(media) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = if (index == studios.lastIndex && !hasMore) 0.dp else 16.dp,
                    ),
            )
        }
    }
}
