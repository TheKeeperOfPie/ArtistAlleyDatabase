package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anilist.MediaTagsQuery
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.utils.IncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.compose.ItemDropdown
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.compose.TrailingDropdownIconButton

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
object AnimeMediaFilterOptionsBottomPanel {

    @Composable
    operator fun <SortOption : AnimeMediaFilterController.Data.SortOption> invoke(
        modifier: Modifier = Modifier,
        filterData: () -> AnimeMediaFilterController.Data<SortOption>,
        errorRes: () -> Int? = { null },
        exception: () -> Exception? = { null },
        expandedForPreview: Boolean = false,
        content: @Composable (PaddingValues) -> Unit,
    ) {
        val scaffoldState = if (expandedForPreview) {
            rememberBottomSheetScaffoldState(rememberStandardBottomSheetState(SheetValue.Expanded))
        } else {
            rememberBottomSheetScaffoldState()
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
                .padding(vertical = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            val data = filterData()

            ItemDropdown(
                label = R.string.anime_media_filter_sort_label,
                value = stringResource(data.sort().textRes),
                iconContentDescription = R.string.anime_media_filter_sort_dropdown_content_description,
                values = { filterData().defaultOptions },
                textForValue = { stringResource(it.textRes) },
                onSelectItem = { data.onSortChanged(it) },
                modifier = Modifier.padding(horizontal = 16.dp),
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
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            FilterSection(
                entries = { filterData().statuses() },
                onEntryClicked = { filterData().onStatusClicked(it.value.first) },
                titleRes = R.string.anime_media_filter_status_label,
                titleDropdownContentDescription = R.string.anime_media_filter_status_content_description,
                valueToText = { stringResource(it.second) },
                includeExcludeIconContentDescription = R.string.anime_media_filter_status_chip_state_content_description,
                defaultExpanded = true,
            )

            FilterSection(
                entries = { filterData().formats() },
                onEntryClicked = { filterData().onFormatClicked(it.value.first) },
                titleRes = R.string.anime_media_filter_format_label,
                titleDropdownContentDescription = R.string.anime_media_filter_format_content_description,
                valueToText = { stringResource(it.second) },
                includeExcludeIconContentDescription = R.string.anime_media_filter_format_chip_state_content_description,
                defaultExpanded = true,
            )

            FilterSection(
                entries = { filterData().genres() },
                onEntryClicked = { filterData().onGenreClicked(it.value) },
                titleRes = R.string.anime_media_filter_genre_label,
                titleDropdownContentDescription = R.string.anime_media_filter_genre_content_description,
                valueToText = { it },
                includeExcludeIconContentDescription = R.string.anime_media_filter_genre_chip_state_content_description,
            )

            TagSection(
                tagsByCategory = { filterData().tagsByCategory() },
                onTagClicked = filterData().onTagClicked,
            )
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

    @Composable
    private fun <T> FilterSection(
        entries: @Composable () -> List<MediaFilterEntry<T>>,
        onEntryClicked: (MediaFilterEntry<T>) -> Unit,
        @StringRes titleRes: Int,
        @StringRes titleDropdownContentDescription: Int,
        valueToText: @Composable (T) -> String,
        @StringRes includeExcludeIconContentDescription: Int,
        defaultExpanded: Boolean = false,
    ) {
        var rootExpanded by remember { mutableStateOf(defaultExpanded) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { rootExpanded = !rootExpanded }
        ) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .weight(1f)
            )
            TrailingDropdownIconButton(
                expanded = rootExpanded,
                contentDescription = stringResource(titleDropdownContentDescription),
                onClick = { rootExpanded = !rootExpanded },
            )
        }

        AnimatedVisibility(
            visible = rootExpanded,
            enter = expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)

            ) {
                entries().forEach {
                    FilterChip(
                        selected = it.state != IncludeExcludeState.DEFAULT,
                        onClick = { onEntryClicked(it) },
                        label = { Text(valueToText(it.value)) },
                        leadingIcon = {
                            IncludeExcludeIcon(it.state, includeExcludeIconContentDescription)
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun TagSection(
        tagsByCategory: @Composable () -> Map<String?,
                List<MediaFilterEntry<MediaTagsQuery.Data.MediaTagCollection>>>,
        onTagClicked: (Int) -> Unit
    ) {
        var rootExpanded by remember { mutableStateOf(false) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { rootExpanded = !rootExpanded }
        ) {
            Text(
                text = stringResource(R.string.anime_media_filter_tag_label),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .weight(1f)
            )
            TrailingDropdownIconButton(
                expanded = rootExpanded,
                contentDescription = stringResource(
                    R.string.anime_media_filter_tag_content_description
                ),
                onClick = { rootExpanded = !rootExpanded },
            )
        }

        AnimatedVisibility(
            visible = rootExpanded,
            enter = expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            Column {
                @Suppress("NAME_SHADOWING")
                val tagsByCategory = tagsByCategory()
                tagsByCategory.forEach { (category, entries) ->
                    var categoryExpanded by remember { mutableStateOf(false) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoryExpanded = !categoryExpanded }
                    ) {
                        val categoryText =
                            category ?: stringResource(R.string.anime_media_filter_tag_misc)
                        Text(
                            text = categoryText,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 32.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
                                .weight(1f)
                        )
                        TrailingDropdownIconButton(
                            expanded = categoryExpanded,
                            contentDescription = stringResource(
                                R.string.anime_media_filter_tag_category_expand_content_description,
                                categoryText
                            ),
                            onClick = { categoryExpanded = !categoryExpanded },
                        )
                    }

                    AnimatedVisibility(
                        visible = categoryExpanded,
                        enter = expandVertically(expandFrom = Alignment.Top),
                        exit = shrinkVertically(shrinkTowards = Alignment.Top)
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 48.dp, end = 16.dp),
                        ) {
                            entries.forEach {
                                FilterChip(
                                    selected = it.state != IncludeExcludeState.DEFAULT,
                                    onClick = { onTagClicked(it.value.id) },
                                    label = { Text(it.value.name) },
                                    leadingIcon = {
                                        IncludeExcludeIcon(
                                            it.state,
                                            R.string.anime_media_filter_tag_chip_state_content_description
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun IncludeExcludeIcon(
        state: IncludeExcludeState,
        @StringRes contentDescriptionRes: Int
    ) {
        when (state) {
            IncludeExcludeState.DEFAULT -> null
            IncludeExcludeState.INCLUDE -> Icons.Filled.Check
            IncludeExcludeState.EXCLUDE -> Icons.Filled.Close
        }?.let {
            Icon(
                imageVector = it,
                contentDescription = stringResource(contentDescriptionRes)
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    AnimeMediaFilterOptionsBottomPanel(
        filterData = { AnimeMediaFilterController.Data.forPreview<MediaSortOption>() },
        expandedForPreview = true,
    ) {
        Text("Sample content")
    }
}