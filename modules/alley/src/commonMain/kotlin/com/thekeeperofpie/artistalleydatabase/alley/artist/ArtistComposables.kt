package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_favorite_icon_content_description
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedBounds
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.skipToLookaheadSize
import org.jetbrains.compose.resources.stringResource

@Composable
fun ArtistTitle(artist: ArtistEntry) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = artist.booth,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.sharedElement(
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
                .sharedElement("name", artist.id, zIndexInOverlay = 1f)
                .weight(1f)
        )
    }
}

@Composable
fun ArtistListRow(
    entry: ArtistEntryGridModel,
    onFavoriteToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val artist = entry.artist
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .sharedBounds("container", artist.id, zIndexInOverlay = 1f)
            .fillMaxWidth()
    ) {
        Text(
            text = artist.booth,
            style = MaterialTheme.typography.titleLarge
                .copy(fontFamily = FontFamily.Monospace),
            modifier = Modifier
                .padding(start = 16.dp, top = 12.dp, bottom = 12.dp)
                .sharedElement("booth", artist.id, zIndexInOverlay = 1f)
        )

        Text(
            text = artist.name,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .sharedElement("name", artist.id, zIndexInOverlay = 1f)
                .weight(1f)
                .padding(vertical = 12.dp)
        )

        val favorite = entry.favorite
        IconButton(
            onClick = { onFavoriteToggle(!favorite) },
            modifier = Modifier.sharedElement(
                "favorite",
                artist.id,
                zIndexInOverlay = 1f,
            )
        ) {
            Icon(
                imageVector = if (favorite) {
                    Icons.Filled.Favorite
                } else {
                    Icons.Filled.FavoriteBorder
                },
                contentDescription = stringResource(
                    Res.string.alley_favorite_icon_content_description
                ),
            )
        }
    }
}
