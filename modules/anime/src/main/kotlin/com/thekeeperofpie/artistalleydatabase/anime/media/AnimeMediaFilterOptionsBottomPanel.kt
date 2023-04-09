package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Switch
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
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.utils.IncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.ItemDropdown
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.compose.TrailingDropdownIconButton

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
object AnimeMediaFilterOptionsBottomPanel {

    @Composable
    operator fun <SortOption : AnimeMediaFilterController.Data.SortOption> invoke(
        modifier: Modifier = Modifier,
        topBar: @Composable () -> Unit = {},
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
            sheetTonalElevation = 4.dp,
            sheetShadowElevation = 4.dp,
            topBar = topBar,
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
                .verticalScroll(rememberScrollState())
        ) {
            val data = filterData()

            Divider()

            Section(
                titleRes = R.string.anime_media_filter_sort_label,
                titleDropdownContentDescriptionRes = R.string.anime_media_filter_sort_content_description,
                defaultExpanded = true,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                ) {
                    ItemDropdown(
                        label = R.string.anime_media_filter_sort_order_label,
                        value = stringResource(data.sort().textRes),
                        iconContentDescription = R.string.anime_media_filter_sort_order_dropdown_content_description,
                        values = { filterData().defaultOptions },
                        textForValue = { stringResource(it.textRes) },
                        onSelectItem = { data.onSortChanged(it) },
                    )

                    // Default sort doesn't allow changing ascending
                    if (data.sort() != filterData().defaultOptions.first()) {
                        ItemDropdown(
                            label = R.string.anime_media_filter_sort_direction_label,
                            value = ascendingText(data.sortAscending()),
                            iconContentDescription = R.string.anime_media_filter_sort_direction_dropdown_content_description,
                            values = { listOf(true, false) },
                            textForValue = { ascendingText(it) },
                            onSelectItem = data.onSortAscendingChanged,
                        )
                    }
                }
            }

            FilterSection(
                entries = { filterData().statuses() },
                onEntryClicked = { filterData().onStatusClicked(it.value) },
                titleRes = R.string.anime_media_filter_status_label,
                titleDropdownContentDescriptionRes = R.string.anime_media_filter_status_content_description,
                valueToText = { stringResource(it.textRes) },
                includeExcludeIconContentDescriptionRes = R.string.anime_media_filter_status_chip_state_content_description,
                defaultExpanded = true,
            )

            FilterSection(
                entries = { filterData().formats() },
                onEntryClicked = { filterData().onFormatClicked(it.value) },
                titleRes = R.string.anime_media_filter_format_label,
                titleDropdownContentDescriptionRes = R.string.anime_media_filter_format_content_description,
                valueToText = { stringResource(it.textRes) },
                includeExcludeIconContentDescriptionRes = R.string.anime_media_filter_format_chip_state_content_description,
                defaultExpanded = true,
            )

            FilterSection(
                entries = { filterData().genres() },
                onEntryClicked = { filterData().onGenreClicked(it.value) },
                titleRes = R.string.anime_media_filter_genre_label,
                titleDropdownContentDescriptionRes = R.string.anime_media_filter_genre_content_description,
                valueToText = { it.value },
                includeExcludeIconContentDescriptionRes = R.string.anime_media_filter_genre_chip_state_content_description,
            )

            TagSection(
                tags = { filterData().tags() },
                onTagClicked = filterData().onTagClicked,
            )

            AdultSection(
                showAdult = { filterData().showAdult() },
                onShowAdultToggled = filterData().onShowAdultToggled,
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
    private fun Section(
        @StringRes titleRes: Int,
        @StringRes titleDropdownContentDescriptionRes: Int,
        defaultExpanded: Boolean = false,
        content: @Composable (expanded: Boolean) -> Unit
    ) {
        var expanded by remember { mutableStateOf(defaultExpanded) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
        ) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .weight(1f)
            )
            TrailingDropdownIconButton(
                expanded = expanded,
                contentDescription = stringResource(titleDropdownContentDescriptionRes),
                onClick = { expanded = !expanded },
            )
        }

        content(expanded)

        Divider()
    }

    @Composable
    private fun <Entry : MediaFilterEntry<*>> FilterSection(
        entries: @Composable () -> List<Entry>,
        onEntryClicked: (Entry) -> Unit,
        @StringRes titleRes: Int,
        @StringRes titleDropdownContentDescriptionRes: Int,
        valueToText: @Composable (Entry) -> String,
        @StringRes includeExcludeIconContentDescriptionRes: Int,
        defaultExpanded: Boolean = false,
    ) {
        Section(
            titleRes = titleRes,
            titleDropdownContentDescriptionRes = titleDropdownContentDescriptionRes,
            defaultExpanded = defaultExpanded,
        ) { expanded ->
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .animateContentSize()
            ) {
                entries().forEach {
                    if (!expanded && it.state == IncludeExcludeState.DEFAULT) return@forEach
                    FilterChip(
                        selected = it.state != IncludeExcludeState.DEFAULT,
                        onClick = { onEntryClicked(it) },
                        label = { Text(valueToText(it)) },
                        leadingIcon = {
                            IncludeExcludeIcon(it, includeExcludeIconContentDescriptionRes)
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun TagSection(
        tags: @Composable () -> Map<String, AnimeMediaFilterController.TagSection>,
        onTagClicked: (Int) -> Unit
    ) {
        Section(
            titleRes = R.string.anime_media_filter_tag_label,
            titleDropdownContentDescriptionRes = R.string.anime_media_filter_tag_content_description,
        ) { expanded ->
            Column(modifier = Modifier.animateContentSize()) {
                @Suppress("NAME_SHADOWING")
                val tags = tags()
                val children =
                    tags.values.filterIsInstance<AnimeMediaFilterController.TagSection.Tag>()
                if (children.isNotEmpty()) {
                    TagChips(
                        tags = children,
                        level = 0,
                        parentExpanded = expanded,
                        onTagClicked = onTagClicked,
                    )
                }

                val subcategoriesToShow = if (expanded) {
                    tags.values
                } else {
                    tags.values.mapNotNull {
                        it.filter { it.state != IncludeExcludeState.DEFAULT }
                    }
                }.filterIsInstance<AnimeMediaFilterController.TagSection.Category>()

                subcategoriesToShow.forEachIndexed { index, section ->
                    TagSubsection(
                        name = section.name,
                        children = section.children.values,
                        parentExpanded = expanded,
                        level = 0,
                        onTagClicked = onTagClicked,
                        isLast = index == subcategoriesToShow.size - 1,
                    )
                }
            }
        }
    }

    @Composable
    private fun TagSubsection(
        name: String,
        children: Collection<AnimeMediaFilterController.TagSection>,
        parentExpanded: Boolean,
        level: Int,
        onTagClicked: (Int) -> Unit,
        isLast: Boolean,
    ) {
        var expanded by remember(name) { mutableStateOf(false) }
        val startPadding = 16.dp * level + 32.dp
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .run {
                    if (parentExpanded) {
                        clickable { expanded = !expanded }
                    } else this
                }
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = startPadding, top = 8.dp, end = 16.dp, bottom = 8.dp)
                    .weight(1f)
            )

            if (parentExpanded) {
                TrailingDropdownIconButton(
                    expanded = expanded,
                    contentDescription = stringResource(
                        R.string.anime_media_filter_tag_category_expand_content_description,
                        name
                    ),
                    onClick = { expanded = !expanded },
                )
            }
        }

        val tags = children.filterIsInstance<AnimeMediaFilterController.TagSection.Tag>()
        val tagsToShow = if (expanded) {
            tags
        } else {
            tags.filter { it.state != IncludeExcludeState.DEFAULT }
        }

        val subcategories =
            children.filterIsInstance<AnimeMediaFilterController.TagSection.Category>()
        val subcategoriesToShow = if (expanded) {
            subcategories
        } else {
            subcategories.mapNotNull {
                it.filter { it.state != IncludeExcludeState.DEFAULT }
                        as? AnimeMediaFilterController.TagSection.Category
            }
        }

        val dividerStartPadding = startPadding - 8.dp

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            if (tagsToShow.isNotEmpty()) {
                TagChips(tagsToShow, parentExpanded = expanded, level, onTagClicked)
            }

            subcategoriesToShow.forEachIndexed { index, section ->
                TagSubsection(
                    section.name,
                    section.children.values,
                    parentExpanded = parentExpanded && expanded,
                    level + 1,
                    onTagClicked,
                    isLast = index == subcategoriesToShow.size - 1,
                )
            }
        }

        if (!isLast) {
            Divider(modifier = Modifier.padding(start = dividerStartPadding))
        }
    }

    @Composable
    private fun TagChips(
        tags: List<AnimeMediaFilterController.TagSection.Tag>,
        parentExpanded: Boolean,
        level: Int,
        onTagClicked: (Int) -> Unit
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp * level + 48.dp, end = 16.dp),
        ) {
            tags.forEach {
                if (!parentExpanded && it.state == IncludeExcludeState.DEFAULT) return@forEach
                FilterChip(
                    selected = it.state != IncludeExcludeState.DEFAULT,
                    onClick = { onTagClicked(it.value.id) },
                    label = { AutoHeightText(it.value.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = it.containerColor
                    ),
                    leadingIcon = {
                        IncludeExcludeIcon(
                            it,
                            R.string.anime_media_filter_tag_chip_state_content_description,
                        )
                    },
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .height(32.dp)
                )
            }
        }
    }

    @Composable
    private fun AdultSection(
        showAdult: @Composable () -> Boolean,
        onShowAdultToggled: (Boolean) -> Unit,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.anime_media_filter_show_adult_content),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .weight(1f)
            )

            Switch(
                checked = showAdult(),
                onCheckedChange = onShowAdultToggled,
                modifier = Modifier.padding(end = 16.dp),
            )
        }
    }

    @Composable
    private fun IncludeExcludeIcon(
        entry: MediaFilterEntry<*>,
        @StringRes contentDescriptionRes: Int
    ) {
        if (entry.state == IncludeExcludeState.DEFAULT) {
            if (entry.leadingIconVector != null) {
                Icon(
                    imageVector = entry.leadingIconVector!!,
                    contentDescription = stringResource(entry.leadingIconContentDescription!!),
                    modifier = Modifier
                        .padding(vertical = 6.dp)
                        .size(20.dp)
                )
            }
        } else {
            when (entry.state) {
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