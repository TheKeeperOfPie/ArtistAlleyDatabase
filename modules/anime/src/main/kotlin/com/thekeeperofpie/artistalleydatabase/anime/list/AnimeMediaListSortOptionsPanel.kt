package com.thekeeperofpie.artistalleydatabase.anime.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.ItemDropdown

object AnimeMediaListSortOptionsPanel {

    @Composable
    operator fun invoke(
        sort: @Composable () -> MediaListSortOption = { MediaListSortOption.STATUS },
        onSortChanged: (MediaListSortOption) -> Unit = {},
        sortAscending: @Composable () -> Boolean = { false },
        onSortAscendingChanged: (Boolean) -> Unit = {},
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            ItemDropdown(
                label = R.string.anime_media_filter_sort_label,
                value = stringResource(sort().textRes),
                iconContentDescription = R.string.anime_media_filter_sort_dropdown_content_description,
                values = { MediaListSortOption.values().toList() },
                textForValue = { stringResource(it.textRes) },
                onSelectItem = onSortChanged,
            )

            Spacer(
                Modifier
                    .fillMaxWidth()
                    .height(12.dp)
            )

            // Default sort doesn't allow changing ascending
            if (sort() != MediaListSortOption.DEFAULT) {
                ItemDropdown(
                    label = R.string.anime_media_filter_sort_ascending_label,
                    value = ascendingText(sortAscending()),
                    iconContentDescription = R.string.anime_media_filter_sort_ascending_dropdown_content_description,
                    values = { listOf(true, false) },
                    textForValue = { ascendingText(it) },
                    onSelectItem = onSortAscendingChanged,
                )
            }
        }
    }

    @Composable
    private fun ascendingText(ascending: Boolean) = stringResource(
        if (ascending) {
            R.string.anime_media_filter_sort_ascending
        } else {
            R.string.anime_media_filter_sort_descending
        }
    )
}

@Preview
@Composable
private fun Preview() {
    AnimeMediaListSortOptionsPanel(sort = { MediaListSortOption.STARTED_ON })
}