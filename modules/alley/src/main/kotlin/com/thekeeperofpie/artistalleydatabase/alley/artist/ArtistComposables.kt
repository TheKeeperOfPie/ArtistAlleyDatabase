package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.thekeeperofpie.artistalleydatabase.compose.sharedBounds
import com.thekeeperofpie.artistalleydatabase.compose.skipToLookaheadSize

@Composable
fun ArtistTitle(artist: ArtistEntry) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = artist.booth,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.sharedBounds(
                "booth",
                artist.id,
                zIndexInOverlay = 1f,
            )
        )

        Text(text = " - ", modifier = Modifier.skipToLookaheadSize())

        Text(
            text = artist.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .sharedBounds("name", artist.id, zIndexInOverlay = 1f)
                .weight(1f)
        )
    }
}
