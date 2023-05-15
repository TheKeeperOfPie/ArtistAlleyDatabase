package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anilist.type.MediaSeason
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.ui.StartEndDateDialog
import com.thekeeperofpie.artistalleydatabase.anime.ui.StartEndDateRow
import com.thekeeperofpie.artistalleydatabase.anime.utils.IncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.BottomSheetScaffoldNoAppBarOffset
import com.thekeeperofpie.artistalleydatabase.compose.ButtonFooter
import com.thekeeperofpie.artistalleydatabase.compose.CustomOutlinedTextField
import com.thekeeperofpie.artistalleydatabase.compose.ItemDropdown
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.compose.rememberBottomSheetScaffoldState
import com.thekeeperofpie.artistalleydatabase.compose.rememberStandardBottomSheetState
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Calendar
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
object AnimeMediaFilterOptionsBottomPanel {

    @Composable
    operator fun <SortOption : AnimeMediaFilterController.Data.SortOption> invoke(
        modifier: Modifier = Modifier,
        topBar: (@Composable () -> Unit)? = null,
        filterData: () -> AnimeMediaFilterController.Data<SortOption>,
        onTagLongClicked: (String) -> Unit = {},
        errorRes: () -> Int? = { null },
        exception: () -> Exception? = { null },
        expandedForPreview: Boolean = false,
        showLoadSave: Boolean = false,
        bottomNavigationState: BottomNavigationState? = null,
        content: @Composable (PaddingValues) -> Unit,
    ) {
        val scaffoldState = if (expandedForPreview) {
            rememberBottomSheetScaffoldState(rememberStandardBottomSheetState(SheetValue.Expanded))
        } else {
            rememberBottomSheetScaffoldState()
        }

        val scope = rememberCoroutineScope()
        BackHandler(enabled = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
            scope.launch {
                scaffoldState.bottomSheetState.partialExpand()
            }
        }

        BottomSheetScaffoldNoAppBarOffset(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 48.dp +
                    (bottomNavigationState?.run { bottomNavBarPadding() + bottomOffset() } ?: 0.dp),
            sheetDragHandle = {
                Box(Modifier.fillMaxWidth()) {
                    BottomSheetDefaults.DragHandle(modifier = Modifier.align(Alignment.Center))

                    val targetValue = scaffoldState.bottomSheetState.targetValue
                    val expandAllAlpha by animateFloatAsState(
                        targetValue = if (targetValue == SheetValue.Expanded) 1f else 0f,
                        label = "Anime filter expand all alpha",
                    )

                    val collapseOnClose = filterData().collapseOnClose()
                    LaunchedEffect(targetValue) {
                        if (targetValue != SheetValue.Expanded) {
                            if (collapseOnClose) {
                                filterData().onClickExpandAll(false)
                            }
                        }
                    }

                    IconButton(
                        onClick = { filterData().run { onClickExpandAll(showExpandAll()) } },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .alpha(expandAllAlpha)
                    ) {
                        Icon(
                            imageVector = if (filterData().showExpandAll()) {
                                Icons.Filled.UnfoldMore
                            } else {
                                Icons.Filled.UnfoldLess
                            },
                            contentDescription = stringResource(
                                R.string.anime_media_filter_expand_all_content_description
                            ),
                        )
                    }
                }
            },
            sheetContent = {
                OptionsPanel(
                    filterData = filterData,
                    onTagLongClicked = onTagLongClicked,
                    showLoadSave = showLoadSave,
                    modifier = Modifier.padding(
                        bottom = bottomNavigationState
                            ?.run { bottomNavBarPadding() + bottomOffset() } ?: 0.dp
                    )
                )
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
        filterData: () -> AnimeMediaFilterController.Data<SortOption>,
        onTagLongClicked: (String) -> Unit,
        showLoadSave: Boolean,
        modifier: Modifier = Modifier,
    ) {
        var airingDateShown by remember { mutableStateOf<Boolean?>(null) }
        Column(
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .animateContentSize()
        ) {
            val data = filterData()

            Divider()

            SortSection(
                expanded = { data.expanded(AnimeMediaFilterController.Section.SORT) },
                onExpandedChanged = {
                    data.setExpanded(AnimeMediaFilterController.Section.SORT, it)
                },
                data = filterData,
            )

            FilterSection(
                expanded = { data.expanded(AnimeMediaFilterController.Section.STATUS) },
                onExpandedChanged = {
                    data.setExpanded(AnimeMediaFilterController.Section.STATUS, it)
                },
                entries = { data.statuses() },
                onEntryClicked = { data.onStatusClicked(it.value) },
                titleRes = R.string.anime_media_filter_status_label,
                titleDropdownContentDescriptionRes = R.string.anime_media_filter_status_content_description,
                valueToText = { stringResource(it.textRes) },
                includeExcludeIconContentDescriptionRes = R.string.anime_media_filter_status_chip_state_content_description,
            )

            FilterSection(
                expanded = { data.expanded(AnimeMediaFilterController.Section.LIST_STATUS) },
                onExpandedChanged = {
                    data.setExpanded(AnimeMediaFilterController.Section.LIST_STATUS, it)
                },
                entries = { data.listStatuses() },
                onEntryClicked = { data.onListStatusClicked(it.value) },
                titleRes = R.string.anime_media_filter_list_status_label,
                titleDropdownContentDescriptionRes = R.string.anime_media_filter_list_status_content_description,
                valueToText = { stringResource(it.textRes) },
                includeExcludeIconContentDescriptionRes = R.string.anime_media_filter_list_status_chip_state_content_description,
            )

            FilterSection(
                expanded = { data.expanded(AnimeMediaFilterController.Section.FORMAT) },
                onExpandedChanged = {
                    data.setExpanded(AnimeMediaFilterController.Section.FORMAT, it)
                },
                entries = { data.formats() },
                onEntryClicked = { data.onFormatClicked(it.value) },
                titleRes = R.string.anime_media_filter_format_label,
                titleDropdownContentDescriptionRes = R.string.anime_media_filter_format_content_description,
                valueToText = { stringResource(it.textRes) },
                includeExcludeIconContentDescriptionRes = R.string.anime_media_filter_format_chip_state_content_description,
            )

            FilterSection(
                expanded = { data.expanded(AnimeMediaFilterController.Section.GENRES) },
                onExpandedChanged = {
                    data.setExpanded(AnimeMediaFilterController.Section.GENRES, it)
                },
                entries = { data.genres() },
                onEntryClicked = { data.onGenreClicked(it.value) },
                titleRes = R.string.anime_media_filter_genre_label,
                titleDropdownContentDescriptionRes = R.string.anime_media_filter_genre_content_description,
                valueToText = { it.value },
                includeExcludeIconContentDescriptionRes = R.string.anime_media_filter_genre_chip_state_content_description,
            )

            TagSection(
                expanded = { data.expanded(AnimeMediaFilterController.Section.TAGS) },
                onExpandedChanged = {
                    data.setExpanded(AnimeMediaFilterController.Section.TAGS, it)
                },
                tags = { data.tags() },
                onTagClicked = data.onTagClicked,
                onTagLongClicked = onTagLongClicked,
                tagRank = { data.tagRank() },
                onTagRankChanged = data.onTagRankChanged,
            )

            AiringDateSection(
                expanded = { data.expanded(AnimeMediaFilterController.Section.AIRING_DATE) },
                onExpandedChanged = {
                    data.setExpanded(AnimeMediaFilterController.Section.AIRING_DATE, it)
                },
                data = { data.airingDate() },
                onSeasonChanged = data.onSeasonChanged,
                onSeasonYearChanged = data.onSeasonYearChanged,
                onIsAdvancedToggled = data.onAiringDateIsAdvancedToggled,
                onRequestDatePicker = { airingDateShown = it },
                onDateChange = data.onAiringDateChange,
            )

            if (data.onListEnabled()) {
                FilterSection(
                    expanded = { data.expanded(AnimeMediaFilterController.Section.ON_LIST) },
                    onExpandedChanged = {
                        data.setExpanded(AnimeMediaFilterController.Section.ON_LIST, it)
                    },
                    entries = { data.onListOptions() },
                    onEntryClicked = { data.onOnListClicked(it) },
                    titleRes = R.string.anime_media_filter_on_list_label,
                    titleDropdownContentDescriptionRes = R.string.anime_media_filter_on_list_dropdown_content_description,
                    valueToText = { stringResource(it.textRes) },
                    includeExcludeIconContentDescriptionRes = R.string.anime_media_filter_on_list_chip_state_content_description,
                    showIcons = false,
                )
            }

            RangeDataSection(
                expanded = { data.expanded(AnimeMediaFilterController.Section.AVERAGE_SCORE) },
                onExpandedChanged = {
                    data.setExpanded(AnimeMediaFilterController.Section.AVERAGE_SCORE, it)
                },
                range = { data.averageScoreRange() },
                onRangeChange = data.onAverageScoreChanged,
                titleRes = R.string.anime_media_filter_average_score_label,
                titleDropdownContentDescriptionRes = R.string.anime_media_filter_average_score_expand_content_description,
            )

            RangeDataSection(
                expanded = { data.expanded(AnimeMediaFilterController.Section.EPISODES) },
                onExpandedChanged = {
                    data.setExpanded(AnimeMediaFilterController.Section.EPISODES, it)
                },
                range = { data.episodesRange() },
                onRangeChange = data.onEpisodesChanged,
                titleRes = R.string.anime_media_filter_episodes_label,
                titleDropdownContentDescriptionRes = R.string.anime_media_filter_episodes_expand_content_description,
            )

            FilterSection(
                expanded = { data.expanded(AnimeMediaFilterController.Section.SOURCE) },
                onExpandedChanged = {
                    data.setExpanded(AnimeMediaFilterController.Section.SOURCE, it)
                },
                entries = { data.sources() },
                onEntryClicked = { data.onSourceClicked(it.value) },
                titleRes = R.string.anime_media_filter_source_label,
                titleDropdownContentDescriptionRes = R.string.anime_media_filter_source_expand_content_description,
                valueToText = { stringResource(it.textRes) },
                includeExcludeIconContentDescriptionRes = R.string.anime_media_filter_source_chip_state_content_description,
                showIcons = false,
            )

            AdultSection(
                showAdult = { data.showAdult() },
                onShowAdultToggled = data.onShowAdultToggled,
            )

            CollapseOnCloseSection(
                collapseOnClose = { data.collapseOnClose() },
                onCollapseOnCloseToggled = data.onCollapseOnCloseToggled,
            )

            ShowIgnoredSection(
                showIgnored = { data.showIgnored() },
                onShowIgnoredToggled = data.onShowIgnoredToggled,
            )

            ActionsSection(
                showLoadSave = showLoadSave,
                onClearFilter = data.onClearFilter,
                onLoadFilter = data.onLoadFilter,
                onSaveFilter = data.onSaveFilter,
            )

            if (airingDateShown != null) {
                StartEndDateDialog(
                    shownForStartDate = airingDateShown,
                    onShownForStartDateToggled = { airingDateShown = it },
                    onDateChange = data.onAiringDateChange,
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

    @Composable
    private fun Section(
        expanded: Boolean,
        onExpandedChanged: (Boolean) -> Unit,
        @StringRes titleRes: Int,
        @StringRes titleDropdownContentDescriptionRes: Int,
        summaryText: (@Composable () -> String?)? = null,
        onSummaryClicked: () -> Unit = {},
        content: @Composable () -> Unit
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChanged(!expanded) }
        ) {
            FlowRow(
                verticalArrangement = Arrangement.Center,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
                    .animateContentSize()
            ) {
                SectionHeaderText(expanded, titleRes)

                if (!expanded) {
                    summaryText?.invoke()?.let {
                        FilterChip(
                            selected = true,
                            onClick = onSummaryClicked,
                            label = { Text(it) },
                            modifier = Modifier
                                .padding(0.dp)
                                .heightIn(min = 32.dp)
                        )
                    }
                }
            }

            TrailingDropdownIconButton(
                expanded = expanded,
                contentDescription = stringResource(titleDropdownContentDescriptionRes),
                onClick = { onExpandedChanged(!expanded) },
            )
        }

        content()

        Divider()
    }

    @Composable
    private fun RowScope.SectionHeaderText(
        expanded: Boolean,
        @StringRes titleRes: Int,
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
                .padding(top = 8.dp, bottom = 8.dp, end = 8.dp)
                .heightIn(min = 32.dp)
                .wrapContentHeight(Alignment.CenterVertically)
                .align(Alignment.CenterVertically)
        )
    }

    @Composable
    private fun <Entry : MediaFilterEntry<*>> FilterSection(
        expanded: () -> Boolean,
        onExpandedChanged: (Boolean) -> Unit,
        entries: @Composable () -> List<Entry>,
        onEntryClicked: (Entry) -> Unit,
        @StringRes titleRes: Int,
        @StringRes titleDropdownContentDescriptionRes: Int,
        valueToText: @Composable (Entry) -> String,
        @StringRes includeExcludeIconContentDescriptionRes: Int,
        showIcons: Boolean = true,
    ) {
        @Suppress("NAME_SHADOWING")
        val expanded = expanded()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChanged(!expanded) }
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
                    .animateContentSize()
            ) {
                SectionHeaderText(expanded, titleRes)

                entries().forEach {
                    if (!expanded && it.state == IncludeExcludeState.DEFAULT) return@forEach
                    FilterChip(
                        selected = it.state != IncludeExcludeState.DEFAULT,
                        onClick = { onEntryClicked(it) },
                        label = { Text(valueToText(it)) },
                        leadingIcon = if (!showIcons) null else {
                            {
                                IncludeExcludeIcon(it, includeExcludeIconContentDescriptionRes)
                            }
                        },
                        modifier = Modifier
                            .animateContentSize()
                            .heightIn(min = 32.dp)
                    )
                }
            }

            TrailingDropdownIconButton(
                expanded = expanded,
                contentDescription = stringResource(titleDropdownContentDescriptionRes),
                onClick = { onExpandedChanged(!expanded) },
                modifier = Modifier.align(Alignment.Top),
            )
        }

        Divider()
    }

    @Composable
    private fun <SortOption : AnimeMediaFilterController.Data.SortOption> SortSection(
        expanded: () -> Boolean,
        onExpandedChanged: (Boolean) -> Unit,
        data: () -> AnimeMediaFilterController.Data<SortOption>,
    ) {
        @Suppress("NAME_SHADOWING")
        val data = data()

        @Suppress("NAME_SHADOWING")
        val expanded = expanded()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChanged(!expanded) }
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
                    .animateContentSize()
            ) {
                SectionHeaderText(expanded, R.string.anime_media_filter_sort_label)

                data.sortOptions().forEach {
                    if (!expanded && it.state == IncludeExcludeState.DEFAULT) return@forEach
                    FilterChip(
                        selected = it.state != IncludeExcludeState.DEFAULT,
                        onClick = { data.onSortClicked(it.value) },
                        label = { Text(stringResource(it.value.textRes)) },
                        modifier = Modifier.animateContentSize()
                    )
                }

                if (!expanded && data.sortOptions()
                        .any { it.state != IncludeExcludeState.DEFAULT }
                ) {
                    val sortAscending = data.sortAscending()
                    FilterChip(
                        selected = true,
                        onClick = { data.onSortAscendingChanged(!sortAscending) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (sortAscending) {
                                    Icons.Filled.ArrowUpward
                                } else {
                                    Icons.Filled.ArrowDownward
                                },
                                contentDescription = stringResource(
                                    if (sortAscending) {
                                        R.string.anime_media_filter_sort_direction_ascending_content_description
                                    } else {
                                        R.string.anime_media_filter_sort_direction_descending_content_description
                                    }
                                ),
                            )
                        },
                        label = { Text(ascendingText(sortAscending)) }
                    )
                }
            }

            TrailingDropdownIconButton(
                expanded = expanded,
                contentDescription = stringResource(R.string.anime_media_filter_sort_content_description),
                onClick = { onExpandedChanged(!expanded) },
                modifier = Modifier.align(Alignment.Top),
            )
        }

        if (expanded && data.sortOptions().any { it.state != IncludeExcludeState.DEFAULT }) {
            val sortAscending = data.sortAscending()
            Text(
                text = stringResource(R.string.anime_media_filter_sort_direction_label),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 8.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp)
                    .animateContentSize()
            ) {
                FilterChip(
                    selected = sortAscending,
                    onClick = { data.onSortAscendingChanged(true) },
                    label = { Text(ascendingText(true)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.ArrowUpward,
                            contentDescription = stringResource(
                                R.string.anime_media_filter_sort_direction_ascending_content_description,
                            ),
                        )
                    },
                    modifier = Modifier.animateContentSize()
                )

                FilterChip(
                    selected = !sortAscending,
                    onClick = { data.onSortAscendingChanged(false) },
                    label = { Text(ascendingText(false)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.ArrowDownward,
                            contentDescription = stringResource(
                                R.string.anime_media_filter_sort_direction_descending_content_description,
                            ),
                        )
                    },
                    modifier = Modifier.animateContentSize()
                )
            }
        }

        Divider()
    }

    @Composable
    private fun TagSection(
        expanded: () -> Boolean,
        onExpandedChanged: (Boolean) -> Unit,
        tags: @Composable () -> Map<String, AnimeMediaFilterController.TagSection>,
        onTagClicked: (String) -> Unit,
        onTagLongClicked: (String) -> Unit,
        tagRank: @Composable () -> String,
        onTagRankChanged: (String) -> Unit,
    ) {
        @Suppress("NAME_SHADOWING")
        val expanded = expanded()
        Section(
            expanded = expanded,
            onExpandedChanged = onExpandedChanged,
            titleRes = R.string.anime_media_filter_tag_label,
            titleDropdownContentDescriptionRes = R.string.anime_media_filter_tag_content_description,
            summaryText = {
                @Suppress("NAME_SHADOWING")
                when (val tagRank = tagRank().toIntOrNull()?.coerceIn(0, 100)) {
                    null, 0 -> null
                    100 -> "== 100"
                    else -> "≥ $tagRank"
                }
            },
            onSummaryClicked = { onTagRankChanged("") },
        ) {
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
                        onTagLongClicked = onTagLongClicked,
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
                        onTagLongClicked = onTagLongClicked,
                        showDivider = expanded || index != subcategoriesToShow.size - 1,
                    )
                }

                if (expanded) {
                    Text(
                        text = stringResource(R.string.anime_media_filter_tag_rank_label),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 8.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        @Suppress("NAME_SHADOWING")
                        val tagRank = tagRank()
                        Slider(
                            value = tagRank.toIntOrNull()?.coerceIn(0, 100)?.toFloat() ?: 0f,
                            valueRange = 0f..100f,
                            steps = 100,
                            onValueChange = { onTagRankChanged(it.roundToInt().toString()) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                        )

                        CustomOutlinedTextField(
                            value = tagRank,
                            onValueChange = { onTagRankChanged(it) },
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            contentPadding = OutlinedTextFieldDefaults.contentPadding(
                                start = 12.dp,
                                top = 8.dp,
                                end = 12.dp,
                                bottom = 8.dp
                            ),
                            modifier = Modifier.width(64.dp),
                        )
                    }
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
        onTagLongClicked: (String) -> Unit,
        showDivider: Boolean,
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
                TagChips(
                    tagsToShow,
                    parentExpanded = expanded,
                    level,
                    onTagClicked,
                    onTagLongClicked
                )
            }

            subcategoriesToShow.forEachIndexed { index, section ->
                TagSubsection(
                    section.name,
                    section.children.values,
                    parentExpanded = parentExpanded && expanded,
                    level + 1,
                    onTagClicked,
                    onTagLongClicked,
                    showDivider = index != subcategoriesToShow.size - 1,
                )
            }
        }

        if (showDivider) {
            Divider(modifier = Modifier.padding(start = dividerStartPadding))
        }
    }

    @Composable
    private fun TagChips(
        tags: List<AnimeMediaFilterController.TagSection.Tag>,
        parentExpanded: Boolean,
        level: Int,
        onTagClicked: (String) -> Unit,
        onTagLongClicked: (String) -> Unit,
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp * level + 48.dp, end = 16.dp)
                .animateContentSize(),
        ) {
            tags.forEach {
                if (!parentExpanded && it.state == IncludeExcludeState.DEFAULT) return@forEach
                com.thekeeperofpie.artistalleydatabase.compose.FilterChip(
                    selected = it.state != IncludeExcludeState.DEFAULT,
                    onClick = { onTagClicked(it.id) },
                    onLongClickLabel = stringResource(
                        R.string.anime_media_tag_long_click_content_description
                    ),
                    onLongClick = { onTagLongClicked(it.id) },
                    enabled = it.clickable,
                    label = { AutoHeightText(it.value.name) },
                    leadingIcon = {
                        IncludeExcludeIcon(
                            it,
                            R.string.anime_media_filter_tag_chip_state_content_description,
                        )
                    },
                    modifier = Modifier.animateContentSize()
                )
            }
        }
    }

    @Composable
    private fun AiringDateSection(
        expanded: () -> Boolean,
        onExpandedChanged: (Boolean) -> Unit,
        data: @Composable () -> AnimeMediaFilterController.AiringDate,
        onSeasonChanged: (MediaSeason?) -> Unit,
        onSeasonYearChanged: (String) -> Unit,
        onIsAdvancedToggled: (Boolean) -> Unit,
        onRequestDatePicker: (Boolean) -> Unit,
        onDateChange: (start: Boolean, Long?) -> Unit,
    ) {
        @Suppress("NAME_SHADOWING")
        val expanded = expanded()

        @Suppress("NAME_SHADOWING")
        val data = data()
        Section(
            expanded = expanded,
            onExpandedChanged = onExpandedChanged,
            titleRes = R.string.anime_media_filter_airing_date,
            titleDropdownContentDescriptionRes = R.string.anime_media_filter_airing_date_content_description,
            summaryText = {
                when (data) {
                    is AnimeMediaFilterController.AiringDate.Basic -> {
                        val season = data.season
                        val seasonYear = data.seasonYear.toIntOrNull()
                        when {
                            season != null && seasonYear != null ->
                                "${stringResource(season.toTextRes())} - $seasonYear"
                            season != null -> stringResource(season.toTextRes())
                            seasonYear != null -> seasonYear.toString()
                            else -> null
                        }
                    }
                    is AnimeMediaFilterController.AiringDate.Advanced -> {
                        val startDate =
                            data.startDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
                        val endDate =
                            data.endDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))

                        when {
                            startDate != null && endDate != null -> {
                                if (data.startDate == data.endDate) {
                                    startDate
                                } else {
                                    "$startDate - $endDate"
                                }
                            }
                            startDate != null -> "≥ $startDate"
                            endDate != null -> "≤ $endDate"
                            else -> null
                        }
                    }
                }
            },
            onSummaryClicked = {
                when (data) {
                    is AnimeMediaFilterController.AiringDate.Basic -> {
                        onSeasonChanged(null)
                        onSeasonYearChanged("")
                    }
                    is AnimeMediaFilterController.AiringDate.Advanced -> {
                        onDateChange(true, null)
                        onDateChange(false, null)
                    }
                }
            },
        ) {
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
                value = data.season?.toTextRes()?.let { stringResource(it) }.orEmpty(),
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
                textForValue = { it?.toTextRes()?.let { stringResource(it) }.orEmpty() },
                onSelectItem = onSeasonChanged,
                modifier = Modifier.weight(1f),
            )

            TextField(
                value = data.seasonYear,
                onValueChange = onSeasonYearChanged,
                label = { Text(stringResource(R.string.anime_media_filter_airing_date_season_year)) },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
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
        StartEndDateRow(
            startDate = data.startDate,
            endDate = data.endDate,
            onRequestDatePicker = onRequestDatePicker,
            onDateChange = onDateChange,
        )
    }

    @Composable
    private fun RangeDataSection(
        expanded: () -> Boolean,
        onExpandedChanged: (Boolean) -> Unit,
        range: @Composable () -> AnimeMediaFilterController.RangeData,
        onRangeChange: (String, String) -> Unit,
        @StringRes titleRes: Int,
        @StringRes titleDropdownContentDescriptionRes: Int,
    ) {
        @Suppress("NAME_SHADOWING")
        val expanded = expanded()
        Section(
            expanded = expanded,
            onExpandedChanged = onExpandedChanged,
            titleRes = titleRes,
            titleDropdownContentDescriptionRes = titleDropdownContentDescriptionRes,
            summaryText = { range().summaryText },
            onSummaryClicked = { onRangeChange("", "") },
        ) {
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                ) {
                    @Suppress("NAME_SHADOWING")
                    val range = range()
                    CustomOutlinedTextField(
                        value = range.startString,
                        onValueChange = { onRangeChange("0", range.endString) },
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        contentPadding = OutlinedTextFieldDefaults.contentPadding(
                            start = 12.dp,
                            top = 8.dp,
                            end = 12.dp,
                            bottom = 8.dp
                        ),
                        // TODO: Figure out a text size dependent width or get wrap width working
                        modifier = Modifier.width(64.dp),
                    )

                    RangeSlider(
                        value = range.value,
                        valueRange = range.valueRange,
                        steps = range.maxValue,
                        onValueChange = {
                            onRangeChange(
                                it.start.roundToInt().toString(),
                                it.endInclusive.roundToInt()
                                    .takeIf { range.hardMax || it != range.maxValue }
                                    ?.toString()
                                    .orEmpty()
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                    )

                    CustomOutlinedTextField(
                        value = range.endString,
                        onValueChange = { onRangeChange(range.startString, it) },
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        contentPadding = OutlinedTextFieldDefaults.contentPadding(
                            start = 12.dp,
                            top = 8.dp,
                            end = 12.dp,
                            bottom = 8.dp
                        ),
                        modifier = Modifier.width(64.dp),
                    )
                }
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

        Divider()
    }

    @Composable
    private fun CollapseOnCloseSection(
        collapseOnClose: @Composable () -> Boolean,
        onCollapseOnCloseToggled: (Boolean) -> Unit,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.anime_media_filter_collapse_on_close),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .weight(1f)
            )

            Switch(
                checked = collapseOnClose(),
                onCheckedChange = onCollapseOnCloseToggled,
                modifier = Modifier.padding(end = 16.dp),
            )
        }

        Divider()
    }

    @Composable
    private fun ShowIgnoredSection(
        showIgnored: @Composable () -> Boolean,
        onShowIgnoredToggled: (Boolean) -> Unit,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.anime_media_filter_show_ignored),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .weight(1f)
            )

            Switch(
                checked = showIgnored(),
                onCheckedChange = onShowIgnoredToggled,
                modifier = Modifier.padding(end = 16.dp),
            )
        }
    }

    @Composable
    private fun ActionsSection(
        showLoadSave: Boolean,
        onClearFilter: () -> Unit,
        onLoadFilter: () -> Unit,
        onSaveFilter: () -> Unit,
    ) {
        if (showLoadSave) {
            ButtonFooter(
                UtilsStringR.clear to onClearFilter,
                UtilsStringR.load to onLoadFilter,
                UtilsStringR.save to onSaveFilter,
            )
        } else {
            ButtonFooter(UtilsStringR.clear to onClearFilter)
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
