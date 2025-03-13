package com.thekeeperofpie.artistalleydatabase.alley.rallies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_favorite_icon_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_prize_limit
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_total_free
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_total_paid
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedBounds
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import org.jetbrains.compose.resources.stringResource

@Composable
fun StampRallyListRow(
    entry: StampRallyEntryGridModel,
    onFavoriteToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val stampRally = entry.stampRally
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .sharedBounds("container", stampRally.id, zIndexInOverlay = 1f)
            .padding(start = 16.dp)
    ) {
        Text(
            text = stampRally.hostTable,
            style = MaterialTheme.typography.titleLarge
                .copy(fontFamily = FontFamily.Monospace),
            modifier = Modifier
                .sharedElement("hostTable", stampRally.id, zIndexInOverlay = 1f)
                .padding(vertical = 8.dp)
        )

        Text(
            text = stampRally.fandom,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .sharedElement("fandom", stampRally.id, zIndexInOverlay = 1f)
                .weight(1f)
                .padding(vertical = 8.dp)
        )

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            if (stampRally.prizeLimit != null) {
                Text(
                    text = stringResource(
                        Res.string.alley_stamp_rally_prize_limit,
                        stampRally.prizeLimitText(),
                    ),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            val totalCost = stampRally.totalCost
            if (totalCost != null) {
                Text(
                    text = if (totalCost == 0L) {
                        stringResource(Res.string.alley_stamp_rally_total_free)
                    } else {
                        stringResource(
                            Res.string.alley_stamp_rally_total_paid,
                            totalCost,
                        )
                    },
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }

        val favorite = entry.favorite
        IconButton(
            onClick = { onFavoriteToggle(!favorite) },
            modifier = Modifier
                .sharedElement("favorite", stampRally.id, zIndexInOverlay = 1f)
                .align(Alignment.Top)
        ) {
            Icon(
                imageVector = if (favorite) {
                    Icons.Filled.Favorite
                } else {
                    Icons.Filled.FavoriteBorder
                },
                contentDescription = stringResource(
                    Res.string.alley_stamp_rally_favorite_icon_content_description
                ),
            )
        }
    }
}
