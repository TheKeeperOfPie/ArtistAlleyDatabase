@file:OptIn(ExperimentalMaterial3Api::class)

package com.thekeeperofpie.artistalleydatabase.anime.activity

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.anilist.UserSocialActivityQuery
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.conditionally

@Composable
fun TextActivitySmallCard(
    activity: UserSocialActivityQuery.Data.Page.TextActivityActivity?,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        onClick = {
            // TODO: Link to full activity
        },
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            val image = activity?.user?.avatar?.large
            if (activity == null || image != null) {
                AsyncImage(
                    model = image,
                    contentDescription = stringResource(R.string.anime_user_image),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .placeholder(
                            visible = activity == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                )
            }

            Text(
                text = activity?.user?.name ?: "USERNAME",
                modifier = Modifier
                    .placeholder(
                        visible = activity == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
        }

        if (activity == null || activity.text != null) {
            Text(
                text = activity?.text ?: "Placeholder text",
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .conditionally(activity == null) { fillMaxWidth() }
                    .placeholder(
                        visible = activity == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
        }
    }
}

@Composable
fun ListActivitySmallCard(
    screenKey: String,
    activity: UserSocialActivityQuery.Data.Page.ListActivityActivity?,
    colorCalculationState: ColorCalculationState,
    navigationCallback: AnimeNavigator.NavigationCallback,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        onClick = {
            // TODO: Link to full activity
        },
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            val image = activity?.user?.avatar?.large
            if (activity == null || image != null) {
                val shape = RoundedCornerShape(12.dp)
                AsyncImage(
                    model = image,
                    contentDescription = stringResource(R.string.anime_user_image),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(shape)
                        .border(width = Dp.Hairline, MaterialTheme.colorScheme.primary, shape)
                        .placeholder(
                            visible = activity == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                )
            }

            Column {
                Text(
                    text = activity?.user?.name ?: "USERNAME",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .placeholder(
                            visible = activity == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                )

                // API returns "1" if the status is "plans to watch", which is redundant, strip it
                val progress =
                    if (activity?.status == "plans to watch") null else activity?.progress
                val status = listOfNotNull(activity?.status, progress).joinToString(separator = " ")
                if (activity == null || status.isNotBlank()) {
                    Text(
                        text = status.ifBlank { "Placeholder text" },
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .conditionally(activity == null) { fillMaxWidth() }
                            .placeholder(
                                visible = activity == null,
                                highlight = PlaceholderHighlight.shimmer(),
                            )
                    )
                }
            }
        }

        AnimeMediaCompactListRow(
            screenKey = screenKey,
            entry = activity?.media?.let {
                // TODO: Ignored
                AnimeMediaCompactListRow.Entry(it, false)
            },
            onLongClick = {
                // TODO: Ignored
            },
            onTagLongClick = {
                // TODO: Tag long click
            },
            onLongPressImage = {
                // TODO: Image long click
            },
            colorCalculationState = colorCalculationState,
            navigationCallback = navigationCallback,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        )
    }
}
