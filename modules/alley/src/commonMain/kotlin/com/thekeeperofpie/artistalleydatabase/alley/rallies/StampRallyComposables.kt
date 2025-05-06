@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.rallies

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_cost_free
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_cost_paid
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_favorite_disabled
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_favorite_icon_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_prize_limit
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_total_cost
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.alley.AlleyUtils
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.alley.ui.Tooltip
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedBounds
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.skipToLookaheadSize
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import org.jetbrains.compose.resources.stringResource

@Composable
fun StampRallyTitle(
    year: DataYear,
    id: String,
    hostTable: String?,
    fandom: String?,
    useSharedElement: Boolean = true,
) {
    SelectionContainer {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val isCurrentYear = remember(year) { AlleyUtils.isCurrentYear(year) }
            if (!isCurrentYear) {
                Text(text = "${stringResource(year.shortName)} - ")
            }

            Text(
                text = hostTable.orEmpty(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .conditionally(useSharedElement) {
                        sharedElement("hostTable", id)
                    }
                    .placeholder(
                        visible = hostTable == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )

            Text(text = " - ", modifier = Modifier.skipToLookaheadSize())

            Text(
                text = fandom.orEmpty(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .conditionally(useSharedElement) {
                        sharedElement("fandom", id)
                    }
                    .weight(1f)
                    .placeholder(
                        visible = hostTable == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
        }
    }
}

@Composable
fun StampRallyListRow(
    entry: StampRallyEntryGridModel,
    onFavoriteToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val stampRally = entry.stampRally
    Row(
        verticalAlignment = Alignment.CenterVertically,
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

        Spacer(Modifier.width(16.dp))

        Text(
            text = stampRally.fandom,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .sharedElement("fandom", stampRally.id, zIndexInOverlay = 1f)
                .weight(1f)
                .padding(vertical = 8.dp)
        )

        Spacer(Modifier.width(16.dp))

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
                        stringResource(Res.string.alley_stamp_rally_cost_free)
                    } else {
                        stringResource(
                            Res.string.alley_stamp_rally_total_cost,
                            totalCost,
                        )
                    },
                    style = MaterialTheme.typography.labelSmall,
                )
            } else {
                val textRes = when {
                    stampRally.tableMin == -1L -> Res.string.alley_stamp_rally_cost_paid
                    stampRally.tableMin == -0L -> Res.string.alley_stamp_rally_cost_free
                    stampRally.tableMin != null -> Res.string.alley_stamp_rally_cost_paid
                    else -> null
                }
                if (textRes != null) {
                    Text(
                        text = stringResource(textRes),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }

        if (entry.stampRally.confirmed) {
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
        } else {
            Tooltip(
                text = stringResource(Res.string.alley_stamp_rally_favorite_disabled),
                showOnClick = true,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier =
                        modifier
                            .minimumInteractiveComponentSize()
                            .size(IconButtonDefaults.smallContainerSize()),
                ) {
                    CompositionLocalProvider(LocalContentColor provides IconButtonDefaults.iconButtonColors().disabledContentColor) {
                        Icon(
                            imageVector = Icons.Filled.FavoriteBorder,
                            contentDescription = stringResource(Res.string.alley_stamp_rally_favorite_disabled),
                        )
                    }
                }
            }
        }
    }
}
