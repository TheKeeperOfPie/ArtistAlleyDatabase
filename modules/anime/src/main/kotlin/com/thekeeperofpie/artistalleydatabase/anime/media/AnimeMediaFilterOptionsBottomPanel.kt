package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anilist.type.MediaSeason
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.utils.IncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.ItemDropdown
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.compose.TrailingDropdownIcon
import com.thekeeperofpie.artistalleydatabase.compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.compose.dropdown.DropdownMenuItem
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Calendar

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
        var airingDateShown by remember { mutableStateOf<Boolean?>(null) }
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            val data = filterData()

            Divider()

            SortSection(
                defaultOptions = data.defaultOptions,
                sort = { data.sort() },
                onSortChanged = data.onSortChanged,
                sortAscending = { data.sortAscending() },
                onSortAscendingChanged = data.onSortAscendingChanged,
            )

            FilterSection(
                entries = { data.statuses() },
                onEntryClicked = { data.onStatusClicked(it.value) },
                titleRes = R.string.anime_media_filter_status_label,
                titleDropdownContentDescriptionRes = R.string.anime_media_filter_status_content_description,
                valueToText = { stringResource(it.textRes) },
                includeExcludeIconContentDescriptionRes = R.string.anime_media_filter_status_chip_state_content_description,
                defaultExpanded = true,
            )

            FilterSection(
                entries = { data.formats() },
                onEntryClicked = { data.onFormatClicked(it.value) },
                titleRes = R.string.anime_media_filter_format_label,
                titleDropdownContentDescriptionRes = R.string.anime_media_filter_format_content_description,
                valueToText = { stringResource(it.textRes) },
                includeExcludeIconContentDescriptionRes = R.string.anime_media_filter_format_chip_state_content_description,
                defaultExpanded = true,
            )

            FilterSection(
                entries = { data.genres() },
                onEntryClicked = { data.onGenreClicked(it.value) },
                titleRes = R.string.anime_media_filter_genre_label,
                titleDropdownContentDescriptionRes = R.string.anime_media_filter_genre_content_description,
                valueToText = { it.value },
                includeExcludeIconContentDescriptionRes = R.string.anime_media_filter_genre_chip_state_content_description,
            )

            TagSection(
                tags = { data.tags() },
                onTagClicked = data.onTagClicked,
            )

            AiringDateSection(
                data = { data.airingDate() },
                onSeasonChanged = data.onSeasonChanged,
                onSeasonYearChanged = data.onSeasonYearChanged,
                onIsAdvancedToggled = data.onAiringDateIsAdvancedToggled,
                onRequestDatePicker = { airingDateShown = it },
                onDateChange = data.onAiringDateChange,
            )

            if (data.onListEnabled()) {
                DropdownSection(
                    titleRes = R.string.anime_media_filter_on_list_label,
                    selectedOption = { data.onListSelectedOption() },
                    options = { data.onListOptions() },
                    onOptionSelected = data.onOnListSelected,
                )
            }

            AdultSection(
                showAdult = { data.showAdult() },
                onShowAdultToggled = data.onShowAdultToggled,
            )

            AiringDateDialog(
                shownForStartDate = { airingDateShown },
                onShownForStartDateToggled = { airingDateShown = it },
                onDateChange = data.onAiringDateChange,
            )
        }
    }

    @Composable
    private fun AiringDateDialog(
        shownForStartDate: () -> Boolean?,
        onShownForStartDateToggled: (Boolean?) -> Unit,
        onDateChange: (start: Boolean, Long?) -> Unit,
    ) {
        val shown = shownForStartDate()
        if (shown != null) {
            val datePickerState = rememberDatePickerState()
            val confirmEnabled by remember { derivedStateOf { datePickerState.selectedDateMillis != null } }
            DatePickerDialog(
                onDismissRequest = { onShownForStartDateToggled(null) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onShownForStartDateToggled(null)
                            onDateChange(shown, datePickerState.selectedDateMillis)
                        },
                        enabled = confirmEnabled,
                    ) {
                        Text(text = stringResource(UtilsStringR.confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onShownForStartDateToggled(null) }) {
                        Text(text = stringResource(UtilsStringR.cancel))
                    }
                },
                modifier = Modifier.fillMaxSize(),
            ) {
                DatePicker(datePickerState)
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

    @Composable
    private fun Section(
        @StringRes titleRes: Int,
        @StringRes titleDropdownContentDescriptionRes: Int,
        summaryText: (@Composable () -> String)? = null,
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
                    .wrapContentWidth()
            )

            if (!expanded && summaryText != null) {
                Text(
                    text = summaryText(),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .align(Alignment.CenterVertically)
                        .wrapContentWidth()
                )
            }

            Spacer(Modifier.weight(1f))

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
        var expanded by remember { mutableStateOf(defaultExpanded) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
                    .animateContentSize()
            ) {
                Text(
                    // Use a zero width space to invalidate the Composable, or otherwise the width
                    // will not change in response to expanded. This might be a bug in Compose.
                    text = stringResource(titleRes) + "\u200B".takeIf { expanded }.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .run {
                            if (expanded) {
                                fillMaxWidth()
                            } else {
                                wrapContentWidth()
                            }
                        }
                        .padding(vertical = 10.dp)
                        .align(Alignment.CenterVertically)
                )

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

            TrailingDropdownIconButton(
                expanded = expanded,
                contentDescription = stringResource(titleDropdownContentDescriptionRes),
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.Top),
            )
        }

        Divider()
    }

    @Composable
    private fun <T, Entry : MediaFilterDropdownEntry<T>> DropdownSection(
        @StringRes titleRes: Int,
        selectedOption: @Composable () -> Entry,
        options: () -> List<Entry>,
        onOptionSelected: (Entry) -> Unit,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .wrapContentWidth(Alignment.Start)
            )

            Spacer(Modifier.weight(1f))

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.padding(start = 16.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .menuAnchor()
                ) {
                    Text(
                        text = selectedOption().toText(),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .wrapContentWidth(Alignment.Start)
                            .widthIn(min = 96.dp)
                    )

                    TrailingDropdownIcon(
                        expanded = expanded,
                        contentDescription = stringResource(
                            selectedOption().dropdownContentDescriptionRes
                        ),
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, end = 12.dp)
                    )
                }

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    options().forEach {
                        DropdownMenuItem(
                            onClick = {
                                expanded = false
                                onOptionSelected(it)
                            },
                            text = { Text(it.toText()) },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
            }
        }

        Divider()
    }

    @Composable
    private fun <SortOption : AnimeMediaFilterController.Data.SortOption> SortSection(
        defaultOptions: List<SortOption>,
        sort: @Composable () -> SortOption,
        onSortChanged: (SortOption) -> Unit = {},
        sortAscending: @Composable () -> Boolean = { false },
        onSortAscendingChanged: (Boolean) -> Unit = {},
    ) {
        Section(
            titleRes = R.string.anime_media_filter_sort_label,
            titleDropdownContentDescriptionRes = R.string.anime_media_filter_sort_content_description,
            summaryText = {
                stringResource(sort().textRes) + " - " + ascendingText(sortAscending())
            },
            defaultExpanded = true
        ) { expanded ->
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                ) {
                    ItemDropdown(
                        label = R.string.anime_media_filter_sort_order_label,
                        value = stringResource(sort().textRes),
                        iconContentDescription = R.string.anime_media_filter_sort_order_dropdown_content_description,
                        values = { defaultOptions },
                        textForValue = { stringResource(it.textRes) },
                        onSelectItem = { onSortChanged(it) },
                    )

                    // Default sort doesn't allow changing ascending
                    if (sort() != defaultOptions.first()) {
                        ItemDropdown(
                            label = R.string.anime_media_filter_sort_direction_label,
                            value = ascendingText(sortAscending()),
                            iconContentDescription = R.string.anime_media_filter_sort_direction_dropdown_content_description,
                            values = { listOf(true, false) },
                            textForValue = { ascendingText(it) },
                            onSelectItem = onSortAscendingChanged,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun TagSection(
        tags: @Composable () -> Map<String, AnimeMediaFilterController.TagSection>,
        onTagClicked: (String) -> Unit
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
        onTagClicked: (String) -> Unit,
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
        onTagClicked: (String) -> Unit
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
                    onClick = { onTagClicked(it.id) },
                    enabled = it.clickable,
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
    private fun AiringDateSection(
        data: @Composable () -> AnimeMediaFilterController.AiringDate,
        onSeasonChanged: (MediaSeason?) -> Unit,
        onSeasonYearChanged: (String) -> Unit,
        onIsAdvancedToggled: (Boolean) -> Unit,
        onRequestDatePicker: (Boolean) -> Unit,
        onDateChange: (start: Boolean, Long?) -> Unit,
    ) {
        Section(
            titleRes = R.string.anime_media_filter_airing_date,
            titleDropdownContentDescriptionRes = R.string.anime_media_filter_airing_date_content_description,
            summaryText = {
                @Suppress("NAME_SHADOWING")
                when (val data = data()) {
                    is AnimeMediaFilterController.AiringDate.Basic -> {
                        val season = data.season
                        val seasonYear = data.seasonYear.toIntOrNull()
                        when {
                            season != null && seasonYear != null ->
                                "${stringResource(season.toTextRes())} - $seasonYear"
                            season != null -> stringResource(season.toTextRes())
                            seasonYear != null -> seasonYear.toString()
                            else -> ""
                        }
                    }
                    is AnimeMediaFilterController.AiringDate.Advanced -> {
                        val startDate =
                            data.startDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
                        val endDate =
                            data.endDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))

                        when {
                            startDate != null && endDate != null -> "$startDate - $endDate"
                            startDate != null -> startDate
                            endDate != null -> endDate
                            else -> ""
                        }
                    }
                }
            }
        ) { expanded ->
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column {
                    @Suppress("NAME_SHADOWING")
                    val data = data()
                    val tabIndex = if (data is AnimeMediaFilterController.AiringDate.Basic) 0 else 1
                    TabRow(
                        selectedTabIndex = tabIndex,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Tab(
                            selected = tabIndex == 0,
                            text = { Text(stringResource(R.string.anime_media_filter_airing_date_season_basic)) },
                            onClick = { onIsAdvancedToggled(false) },
                        )

                        Tab(
                            selected = tabIndex == 1,
                            text = { Text(stringResource(R.string.anime_media_filter_airing_date_season_advanced)) },
                            onClick = { onIsAdvancedToggled(true) },
                        )
                    }

                    when (data) {
                        is AnimeMediaFilterController.AiringDate.Basic ->
                            AiringDateBasicSection(
                                data = data,
                                onSeasonChanged = onSeasonChanged,
                                onSeasonYearChanged = onSeasonYearChanged,
                            )
                        is AnimeMediaFilterController.AiringDate.Advanced ->
                            AiringDateAdvancedSection(
                                data = data,
                                onRequestDatePicker = onRequestDatePicker,
                                onDateChange = onDateChange,
                            )
                    }
                }
            }
        }
    }

    @Composable
    private fun AiringDateBasicSection(
        data: AnimeMediaFilterController.AiringDate.Basic,
        onSeasonChanged: (MediaSeason?) -> Unit,
        onSeasonYearChanged: (String) -> Unit,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        ) {
            ItemDropdown(
                label = R.string.anime_media_filter_airing_date_season,
                value = stringResource(data.season.toTextRes()),
                iconContentDescription = R.string.anime_media_filter_airing_date_season_dropdown_content_description,
                values = {
                    listOf(
                        null,
                        MediaSeason.WINTER,
                        MediaSeason.SPRING,
                        MediaSeason.SUMMER,
                        MediaSeason.FALL,
                    )
                },
                textForValue = { stringResource(it.toTextRes()) },
                onSelectItem = onSeasonChanged,
                modifier = Modifier.weight(1f),
            )

            TextField(
                value = data.seasonYear,
                onValueChange = onSeasonYearChanged,
                label = { Text(stringResource(R.string.anime_media_filter_airing_date_season_year)) },
                leadingIcon = if (data.seasonYear.isNotBlank()) {
                    @Composable {
                        IconButton(onClick = {
                            data.seasonYear.toIntOrNull()?.let {
                                onSeasonYearChanged((it - 1).toString())
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.RemoveCircleOutline,
                                contentDescription = stringResource(
                                    R.string.anime_media_filter_airing_date_season_year_decrement_content_description
                                ),
                            )
                        }
                    }
                } else null,
                trailingIcon = {
                    Row {
                        val isYearBlank = data.seasonYear.isBlank()
                        if (!isYearBlank) {
                            IconButton(onClick = {
                                data.seasonYear.toIntOrNull()?.let {
                                    onSeasonYearChanged((it + 1).toString())
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.AddCircleOutline,
                                    contentDescription = stringResource(
                                        R.string.anime_media_filter_airing_date_season_year_increment_content_description
                                    ),
                                )
                            }
                        }

                        IconButton(onClick = {
                            if (isYearBlank) {
                                onSeasonYearChanged(
                                    Calendar.getInstance()[Calendar.YEAR].toString()
                                )
                            } else {
                                onSeasonYearChanged("")
                            }
                        }) {
                            Icon(
                                imageVector = if (isYearBlank) {
                                    Icons.Filled.CalendarToday
                                } else {
                                    Icons.Filled.Clear
                                },
                                contentDescription = stringResource(
                                    if (isYearBlank) {
                                        R.string.anime_media_filter_airing_date_season_year_today_content_description
                                    } else {
                                        R.string.anime_media_filter_airing_date_season_year_clear_content_description
                                    },
                                ),
                            )
                        }
                    }
                },
                modifier = Modifier.weight(1f),
            )
        }
    }

    @Composable
    private fun AiringDateAdvancedSection(
        data: AnimeMediaFilterController.AiringDate.Advanced,
        onRequestDatePicker: (forStart: Boolean) -> Unit,
        onDateChange: (start: Boolean, Long?) -> Unit,
    ) {
        val localDateUtcNow = LocalDate.now()
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        ) {
            val startDate =
                data.startDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                    .orEmpty()
            val endDate =
                data.endDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                    .orEmpty()

            val startInteractionSource = remember { MutableInteractionSource() }
            LaunchedEffect(startInteractionSource) {
                startInteractionSource.interactions.collect {
                    if (it is PressInteraction.Release) {
                        onRequestDatePicker(true)
                    }
                }
            }

            val hasStartDate = data.startDate != null
            val isTodayBeforeEnd = data.endDate == null || localDateUtcNow.isBefore(data.endDate)
                    || localDateUtcNow.isEqual(data.endDate)
            TextField(
                value = startDate,
                onValueChange = {},
                readOnly = true,
                label = if (hasStartDate) {
                    {
                        Text(
                            stringResource(R.string.anime_media_filter_airing_date_start_date_label)
                        )
                    }
                } else null,
                placeholder = { Text(stringResource(R.string.anime_media_filter_airing_date_start_date_label)) },
                trailingIcon = {
                    if (hasStartDate || isTodayBeforeEnd) {
                        IconButton(onClick = {
                            onDateChange(
                                true,
                                if (hasStartDate) {
                                    null
                                } else {
                                    LocalDate.of(
                                        localDateUtcNow.year,
                                        localDateUtcNow.month,
                                        localDateUtcNow.dayOfMonth
                                    )
                                        .atStartOfDay(ZoneOffset.UTC)
                                        .toInstant()
                                        .toEpochMilli()
                                }
                            )
                        }) {
                            Icon(
                                imageVector = if (hasStartDate) {
                                    Icons.Filled.Clear
                                } else {
                                    Icons.Filled.CalendarToday
                                },
                                contentDescription = stringResource(
                                    R.string.anime_media_filter_airing_date_today_content_description
                                ),
                            )
                        }
                    }
                },
                interactionSource = startInteractionSource,
                modifier = Modifier
                    .weight(1f),
            )

            val endInteractionSource = remember { MutableInteractionSource() }
            LaunchedEffect(endInteractionSource) {
                endInteractionSource.interactions.collect {
                    if (it is PressInteraction.Release) {
                        onRequestDatePicker(false)
                    }
                }
            }

            val hasEndDate = data.endDate != null
            val isTodayAfterStart =
                data.startDate == null || localDateUtcNow.isAfter(data.startDate)
                        || localDateUtcNow.isEqual(data.startDate)
            TextField(
                value = endDate,
                onValueChange = {},
                readOnly = true,
                label = if (hasEndDate) {
                    {
                        Text(
                            stringResource(R.string.anime_media_filter_airing_date_end_date_label)
                        )
                    }
                } else null,
                placeholder = { Text(stringResource(R.string.anime_media_filter_airing_date_end_date_label)) },
                trailingIcon = {
                    if (hasEndDate || isTodayAfterStart) {
                        IconButton(onClick = {
                            onDateChange(
                                false,
                                if (hasEndDate) {
                                    null
                                } else {
                                    LocalDate.of(
                                        localDateUtcNow.year,
                                        localDateUtcNow.month,
                                        localDateUtcNow.dayOfMonth
                                    )
                                        .atStartOfDay(ZoneOffset.UTC)
                                        .toInstant()
                                        .toEpochMilli()
                                }
                            )
                        }) {
                            Icon(
                                imageVector = if (hasEndDate) {
                                    Icons.Filled.Clear
                                } else {
                                    Icons.Filled.CalendarToday
                                },
                                contentDescription = stringResource(
                                    R.string.anime_media_filter_airing_date_today_content_description
                                ),
                            )
                        }
                    }
                },
                interactionSource = endInteractionSource,
                modifier = Modifier
                    .weight(1f),
            )
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