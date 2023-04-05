package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.ItemDropdown
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText

@OptIn(ExperimentalMaterial3Api::class)
object AnimeMediaFilterOptionsBottomPanel {

    @Composable
    operator fun <SortOption : AnimeMediaFilterController.Data.SortOption> invoke(
        modifier: Modifier = Modifier,
        filterData: () -> AnimeMediaFilterController.Data<SortOption>,
        errorRes: () -> Int? = { null },
        exception: () -> Exception? = { null },
        content: @Composable (PaddingValues) -> Unit,
    ) {
        val scaffoldState = rememberBottomSheetScaffoldState()
        LaunchedEffect(true) {
            // An initial value of HIDE crashes, so just hide it manually
            scaffoldState.bottomSheetState.hide()
        }

        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetContent = {
                OptionsPanel(filterData = filterData)
            },
            snackbarHost = {
                @Suppress("NAME_SHADOWING")
                val errorRes = errorRes()
                if (errorRes != null) {
                    SnackbarErrorText(errorRes, exception())
                } else {
                    // Bottom sheet requires at least one measurable component
                    Spacer(modifier = Modifier.size(0.dp))
                }
            },
            modifier = modifier,
            content = content
        )
    }

    @Composable
    private fun <SortOption : AnimeMediaFilterController.Data.SortOption> OptionsPanel(
        filterData: () -> AnimeMediaFilterController.Data<SortOption>
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            val data = filterData()

            ItemDropdown(
                label = R.string.anime_media_filter_sort_label,
                value = stringResource(data.sort().textRes),
                iconContentDescription = R.string.anime_media_filter_sort_dropdown_content_description,
                values = { filterData().defaultOptions },
                textForValue = { stringResource(it.textRes) },
                onSelectItem = { data.onSortChanged(it) },
            )

            Spacer(
                Modifier
                    .fillMaxWidth()
                    .height(12.dp)
            )

            // Default sort doesn't allow changing ascending
            if (data.sort() != filterData().defaultOptions.first()) {
                ItemDropdown(
                    label = R.string.anime_media_filter_sort_ascending_label,
                    value = ascendingText(data.sortAscending()),
                    iconContentDescription = R.string.anime_media_filter_sort_ascending_dropdown_content_description,
                    values = { listOf(true, false) },
                    textForValue = { ascendingText(it) },
                    onSelectItem = data.onSortAscendingChanged,
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
    AnimeMediaFilterOptionsBottomPanel(
        filterData = { AnimeMediaFilterController.Data.forPreview<MediaSortOption>() }
    ) {}
}