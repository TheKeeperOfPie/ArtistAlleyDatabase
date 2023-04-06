package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.bottomBorder

object AnimeListMediaRow {

    @Composable
    operator fun invoke(entry: Entry) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp)
                .bottomBorder(1.dp, MaterialTheme.colorScheme.onSurface)
        ) {
            AsyncImage(
                model = entry.image,
                contentScale = ContentScale.Crop,
                fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                contentDescription = stringResource(R.string.anime_media_cover_image),
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .heightIn(120.dp)
                    .width(80.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .placeholder(
                        visible = entry == Entry.Loading,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )

            Text(
                text = entry.title ?: "Loading...",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    .placeholder(
                        visible = entry == Entry.Loading,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
        }
    }

    interface Entry {
        object Loading : Entry {
            override val image = null
            override val title = ""
        }

        val image: String?
        val title: String?
    }
}