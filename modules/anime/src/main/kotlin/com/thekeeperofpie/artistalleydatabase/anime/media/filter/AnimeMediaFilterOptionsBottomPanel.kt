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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
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
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.BottomSheetScaffoldNoAppBarOffset
import com.thekeeperofpie.artistalleydatabase.compose.ButtonFooter
import com.thekeeperofpie.artistalleydatabase.compose.CustomOutlinedTextField
import com.thekeeperofpie.artistalleydatabase.compose.ItemDropdown
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterSection
import com.thekeeperofpie.artistalleydatabase.compose.filter.IncludeExcludeIcon
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortAndFilterComposables.SortFilterHeaderText
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortAndFilterComposables.SortSection
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortOption
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
    operator fun <SortType : SortOption> invoke(
        modifier: Modifier = Modifier,
        topBar: (@Composable () -> Unit)? = null,
        filterData: () -> AnimeMediaFilterController.Data<SortType>,
        onTagLongClick: (String) -> Unit = {},
        errorRes: () -> Int? = { null },
        exception: () -> Exception? = { null },
        expandedForPreview: Boolean = false,
        showLoadSave: Boolean = true,
        showIgnoredFilter: Boolean = true,
        bottomNavigationState: BottomNavigationState? = null,
        ignoreAppBarOffset: Boolean = true,
        content: @Composable (PaddingValues) -> Unit,
    ) {
        if (ignoreAppBarOffset) {
            PanelIgnoreAppBarOffset(
                modifier = modifier,
                topBar = topBar,
                filterData = filterData,
                onTagLongClick = onTagLongClick,
                errorRes = errorRes,
                exception = exception,
                expandedForPreview = expandedForPreview,
                showLoadSave = showLoadSave,
                showIgnoredFilter = showIgnoredFilter,
                bottomNavigationState = bottomNavigationState,
                content = content
            )
        } else {
            Panel(
                modifier = modifier,
                topBar = topBar,
                filterData = filterData,
                onTagLongClick = onTagLongClick,
                errorRes = errorRes,
                exception = exception,
                expandedForPreview = expandedForPreview,
                showLoadSave = showLoadSave,
                showIgnoredFilter = showIgnoredFilter,
                bottomNavigationState = bottomNavigationState,
                content = content
            )
        }
    }

    @Composable
    private fun <SortType : SortOption> PanelIgnoreAppBarOffset(
        modifier: Modifier = Modifier,
        topBar: (@Composable () -> Unit)? = null,
        filterData: () -> AnimeMediaFilterController.Data<SortType>,
        onTagLongClick: (String) -> Unit = {},
        errorRes: () -> Int? = { null },
        exception: () -> Exception? = { null },
        expandedForPreview: Boolean = false,
        showLoadSave: Boolean = true,
        showIgnoredFilter: Boolean = true,
        bottomNavigationState: BottomNavigationState? = null,
        content: @Composable (PaddingValues) -> Unit,
    ) {
        val scaffoldState = if (expandedForPreview) {
            rememberBottomSheetScaffoldState(rememberStandardBottomSheetState(SheetValue.Expanded))
        } else {
            rememberBottomSheetScaffoldState(
                rememberStandardBottomSheetState(
                    confirmValueChange = { it != SheetValue.Hidden },
                    skipHiddenState = true,
                )
            )
        }

        val scope = rememberCoroutineScope()
        BackHandler(enabled = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
            scope.launch {
                scaffoldState.bottomSheetState.partialExpand()
            }
        }

        BottomSheetScaffoldNoAppBarOffset(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 56.dp +
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
                    onTagLongClick = onTagLongClick,
                    showLoadSave = showLoadSave,
                    showIgnoredFilter = showIgnoredFilter,
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
    private fun <SortType : SortOption> Panel(
        modifier: Modifier = Modifier,
        topBar: (@Composable () -> Unit)? = null,
        filterData: () -> AnimeMediaFilterController.Data<SortType>,
        onTagLongClick: (String) -> Unit = {},
        errorRes: () -> Int? = { null },
        exception: () -> Exception? = { null },
        expandedForPreview: Boolean = false,
        showLoadSave: Boolean = true,
        showIgnoredFilter: Boolean = true,
        bottomNavigationState: BottomNavigationState? = null,
        content: @Composable (PaddingValues) -> Unit,
    ) {
        val scaffoldState = if (expandedForPreview) {
            androidx.compose.material3.rememberBottomSheetScaffoldState(
                androidx.compose.material3.rememberStandardBottomSheetState(
                    SheetValue.Expanded
                )
            )
        } else {
            androidx.compose.material3.rememberBottomSheetScaffoldState(
                androidx.compose.material3.rememberStandardBottomSheetState(
                    confirmValueChange = { it != SheetValue.Hidden },
                    skipHiddenState = true,
                )
            )
        }

        val scope = rememberCoroutineScope()
        BackHandler(enabled = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
            scope.launch {
                scaffoldState.bottomSheetState.partialExpand()
            }
        }

        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 56.dp +
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
                    onTagLongClick = onTagLongClick,
                    showLoadSave = showLoadSave,
                    showIgnoredFilter = showIgnoredFilter,
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
    private fun <SortType : SortOption> OptionsPanel(
        filterData: () -> AnimeMediaFilterController.Data<SortType>,
        onTagLongClick: (String) -> Unit,
        showLoadSave: Boolean,
        showIgnoredFilter: Boolean,
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
                headerTextRes = R.string.anime_media_filter_sort_label,
                expanded = { data.expanded(AnimeMediaFilterController.Section.SORT) },
                onExpandedChange = {
                    data.setExpanded(AnimeMediaFilterController.Section.SORT, it)
                },
                sortOptions = { filterData().sortOptions() },
                onSortClick = { filterData().onSortClick(it) },
                sortAscending = { filterData().sortAscending() },
                onSortAscendingChange = { filterData().onSortAscendingChange(it) }
            )

            FilterSection(
                expanded = { data.expanded(AnimeMediaFilterController.Section.STATUS) },
                onExpandedChange = {
                    data.setExpanded(AnimeMediaFilterController.Section.STATUS, it)
                },
                entries = { data.statuses() },
                onEntryClick = { data.onStatusClick(it.value) },
                titleRes = R.string.anime_media_filter_status_label,
                titleDropdownContentDescriptionRes = R.string.anime_media_filter_status_content_description,
                valueToText = { stringResource(it.textRes) },
                includeExcludeIconContentDescriptionRes = R.string.anime_media_filter_status_chip_state_content_description,
            )

            val viewer = data.viewer()
            if (viewer != null) {
                FilterSection(
                    expanded = { data.expanded(AnimeMediaFilterController.Section.LIST_STATUS) },
                    onExpandedChange = {
                        data.setExpanded(AnimeMediaFilterController.Section.LIST_STATUS, it)
                    },
                    entries = { data.listStatuses() },
                    onEntryClick = { data.onListStatusClick(it.value) },
                    titleRes = R.string.anime_media_filter_list_status_label,
                    titleDropdownContentDescriptionRes = R.string.anime_media_filter_list_status_content_description,
                    valueToText = { stringResource(it.textRes) },
                    includeExcludeIconContentDescriptionRes = R.string.anime_media_filter_list_status_chip_state_content_description,
                )
            }

            FilterSection(
                expanded = { data.expanded(AnimeMediaFilterController.Section.FORMAT) },
                onExpandedChange = {
                    data.setExpanded(AnimeMediaFilterController.Section.FORMAT, it)
                },
                entries = { data.formats() },
                onEntryClick = { data.onFormatClick(it.value) },
                titleRes = R.string.anime_media_filter_format_label,
                titleDropdownContentDescriptionRes = R.string.anime_media_filter_format_content_description,
                valueToText = { stringResource(it.textRes) },
                includeExcludeIconContentDescriptionRes = R.string.anime_media_filter_format_chip_state_content_description,
            )

            FilterSection(
                expanded = { data.expanded(AnimeMediaFilterController.Section.GENRES) },
                onExpandedChange = {
                    data.setExpanded(AnimeMediaFilterController.Section.GENRES, it)
                },
                entries = { data.genres() },
                onEntryClick = { data.onGenreClick(it.value) },
                titleRes = R.string.anime_media_filter_genre_label,
                titleDropdownContentDescriptionRes = R.string.anime_media_filter_genre_content_description,
                valueToText = { it.value },
                includeExcludeIconContentDescriptionRes = R.string.anime_media_filter_genre_chip_state_content_description,
            )

            TagSection(
                expanded = { data.expanded(AnimeMediaFilterController.Section.TAGS) },
                onExpandedChange = {
                    data.setExpanded(AnimeMediaFilterController.Section.TAGS, it)
                },
                tags = { data.tags() },
                onTagClick = data.onTagClick,
                onTagLongClick = onTagLongClick,
                tagRank = { data.tagRank() },
                onTagRankChange = data.onTagRankChange,
            )

            if (data.airingDateEnabled()) {
                AiringDateSection(
                    expanded = { data.expanded(AnimeMediaFilterController.Section.AIRING_DATE) },
                    onExpandedChange = {
                        data.setExpanded(AnimeMediaFilterController.Section.AIRING_DATE, it)
                    },
                    data = { data.airingDate() },
                    onSeasonChange = data.onSeasonChange,
                    onSeasonYearChange = data.onSeasonYearChange,
                    onIsAdvancedToggle = data.onAiringDateIsAdvancedToggle,
                    onRequestDatePicker = { airingDateShown = it },
                    onDateChange = data.onAiringDateChange,
                )
            }

            if (viewer != null && data.onListEnabled()) {
                FilterSection(
                    expanded = { data.expanded(AnimeMediaFilterController.Section.ON_LIST) },
                    onExpandedChange = {
                        data.setExpanded(AnimeMediaFilterController.Section.ON_LIST, it)
                    },
                    entries = { data.onListOptions() },
                    onEntryClick = { data.onOnListClick(it) },
                    titleRes = R.string.anime_media_filter_on_list_label,
                    titleDropdownContentDescriptionRes = R.string.anime_media_filter_on_list_dropdown_content_description,
                    valueToText = { stringResource(it.textRes) },
                    includeExcludeIconContentDescriptionRes = R.string.anime_media_filter_on_list_chip_state_content_description,
                    showIcons = false,
                )
            }

            RangeDataSection(
                expanded = { data.expanded(AnimeMediaFilterController.Section.AVERAGE_SCORE) },
                onExpandedChange = {
                    data.setExpanded(AnimeMediaFilterController.Section.AVERAGE_SCORE, it)
                },
                range = { data.averageScoreRange() },
                onRangeChange = data.onAverageScoreChange,
                titleRes = R.string.anime_media_filter_average_score_label,
                titleDropdownContentDescriptionRes = R.string.anime_media_filter_average_score_expand_content_description,
            )

            RangeDataSection(
                expanded = { data.expanded(AnimeMediaFilterController.Section.EPISODES) },
                onExpandedChange = {
                    data.setExpanded(AnimeMediaFilterController.Section.EPISODES, it)
                },
                range = { data.episodesRange() },
                onRangeChange = data.onEpisodesChange,
                titleRes = R.string.anime_media_filter_episodes_label,
                titleDropdownContentDescriptionRes = R.string.anime_media_filter_episodes_expand_content_description,
            )

            FilterSection(
                expanded = { data.expanded(AnimeMediaFilterController.Section.SOURCE) },
                onExpandedChange = {
                    data.setExpanded(AnimeMediaFilterController.Section.SOURCE, it)
                },
                entries = { data.sources() },
                onEntryClick = { data.onSourceClick(it.value) },
                titleRes = R.string.anime_media_filter_source_label,
                titleDropdownContentDescriptionRes = R.string.anime_media_filter_source_expand_content_description,
                valueToText = { stringResource(it.textRes) },
                includeExcludeIconContentDescriptionRes = R.string.anime_media_filter_source_chip_state_content_description,
                showIcons = false,
            )

            AdultSection(
                showAdult = { data.showAdult() },
                onShowAdultChange = data.onShowAdultToggle,
            )

            CollapseOnCloseSection(
                collapseOnClose = { data.collapseOnClose() },
                onCollapseOnCloseChange = data.onCollapseOnCloseToggle,
            )

            if (showIgnoredFilter) {
                ShowIgnoredSection(
                    showIgnored = { data.showIgnored() },
                    onShowIgnoredChange = data.onShowIgnoredToggle,
                )
            }

            ActionsSection(
                showLoadSave = showLoadSave,
                onClearFilter = data.onClearFilter,
                onLoadFilter = data.onLoadFilter,
                onSaveFilter = data.onSaveFilter,
            )

            if (airingDateShown != null) {
                StartEndDateDialog(
                    shownForStartDate = airingDateShown,
                    onShownForStartDateChange = { airingDateShown = it },
                    onDateChange = data.onAiringDateChange,
                )
            }
        }
    }

    @Composable
    private fun Section(
        expanded: Boolean,
        onExpandedChange: (Boolean) -> Unit,
        @StringRes titleRes: Int,
        @StringRes titleDropdownContentDescriptionRes: Int,
        summaryText: (@Composable () -> String?)? = null,
        onSummaryClick: () -> Unit = {},
        content: @Composable () -> Unit
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(!expanded) }
        ) {
            FlowRow(
                verticalArrangement = Arrangement.Center,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
                    .animateContentSize()
            ) {
                SortFilterHeaderText(expanded, titleRes)

                if (!expanded) {
                    summaryText?.invoke()?.let {
                        FilterChip(
                            selected = true,
                            onClick = onSummaryClick,
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
                onClick = { onExpandedChange(!expanded) },
            )
        }

        content()

        Divider()
    }

    @Composable
    private fun TagSection(
        expanded: () -> Boolean,
        onExpandedChange: (Boolean) -> Unit,
        tags: @Composable () -> Map<String, AnimeMediaFilterController.TagSection>,
        onTagClick: (String) -> Unit,
        onTagLongClick: (String) -> Unit,
        tagRank: @Composable () -> String,
        onTagRankChange: (String) -> Unit,
    ) {
        @Suppress("NAME_SHADOWING")
        val expanded = expanded()
        Section(
            expanded = expanded,
            onExpandedChange = onExpandedChange,
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
            onSummaryClick = { onTagRankChange("") },
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
                        onTagClick = onTagClick,
                        onTagLongClick = onTagLongClick,
                    )
                }

                val subcategoriesToShow = if (expanded) {
                    tags.values
                } else {
                    tags.values.mapNotNull {
                        it.filter { it.state != FilterIncludeExcludeState.DEFAULT }
                    }
                }.filterIsInstance<AnimeMediaFilterController.TagSection.Category>()

                subcategoriesToShow.forEachIndexed { index, section ->
                    TagSubsection(
                        name = section.name,
                        children = section.children.values,
                        parentExpanded = expanded,
                        level = 0,
                        onTagClick = onTagClick,
                        onTagLongClick = onTagLongClick,
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
                            onValueChange = { onTagRankChange(it.roundToInt().toString()) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                        )

                        CustomOutlinedTextField(
                            value = tagRank,
                            onValueChange = { onTagRankChange(it) },
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
        onTagClick: (String) -> Unit,
        onTagLongClick: (String) -> Unit,
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
            tags.filter { it.state != FilterIncludeExcludeState.DEFAULT }
        }

        val subcategories =
            children.filterIsInstance<AnimeMediaFilterController.TagSection.Category>()
        val subcategoriesToShow = if (expanded) {
            subcategories
        } else {
            subcategories.mapNotNull {
                it.filter { it.state != FilterIncludeExcludeState.DEFAULT }
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
                    onTagClick,
                    onTagLongClick
                )
            }

            subcategoriesToShow.forEachIndexed { index, section ->
                TagSubsection(
                    section.name,
                    section.children.values,
                    parentExpanded = parentExpanded && expanded,
                    level + 1,
                    onTagClick,
                    onTagLongClick,
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
        onTagClick: (String) -> Unit,
        onTagLongClick: (String) -> Unit,
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp * level + 48.dp, end = 16.dp)
                .animateContentSize(),
        ) {
            tags.forEach {
                if (!parentExpanded && it.state == FilterIncludeExcludeState.DEFAULT) return@forEach
                com.thekeeperofpie.artistalleydatabase.compose.FilterChip(
                    selected = it.state != FilterIncludeExcludeState.DEFAULT,
                    onClick = { onTagClick(it.id) },
                    onLongClickLabel = stringResource(
                        R.string.anime_media_tag_long_click_content_description
                    ),
                    onLongClick = { onTagLongClick(it.id) },
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
        onExpandedChange: (Boolean) -> Unit,
        data: @Composable () -> AnimeMediaFilterController.AiringDate,
        onSeasonChange: (MediaSeason?) -> Unit,
        onSeasonYearChange: (String) -> Unit,
        onIsAdvancedToggle: (Boolean) -> Unit,
        onRequestDatePicker: (Boolean) -> Unit,
        onDateChange: (start: Boolean, Long?) -> Unit,
    ) {
        @Suppress("NAME_SHADOWING")
        val expanded = expanded()

        @Suppress("NAME_SHADOWING")
        val data = data()
        Section(
            expanded = expanded,
            onExpandedChange = onExpandedChange,
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
            onSummaryClick = {
                when (data) {
                    is AnimeMediaFilterController.AiringDate.Basic -> {
                        onSeasonChange(null)
                        onSeasonYearChange("")
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
                            onClick = { onIsAdvancedToggle(false) },
                        )

                        Tab(
                            selected = tabIndex == 1,
                            text = { Text(stringResource(R.string.anime_media_filter_airing_date_season_advanced)) },
                            onClick = { onIsAdvancedToggle(true) },
                        )
                    }

                    when (data) {
                        is AnimeMediaFilterController.AiringDate.Basic ->
                            AiringDateBasicSection(
                                data = data,
                                onSeasonChange = onSeasonChange,
                                onSeasonYearChange = onSeasonYearChange,
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
        onSeasonChange: (MediaSeason?) -> Unit,
        onSeasonYearChange: (String) -> Unit,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        ) {
            ItemDropdown(
                label = R.string.anime_media_filter_airing_date_season,
                value = data.season,
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
                onSelectItem = onSeasonChange,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )

            TextField(
                value = data.seasonYear,
                onValueChange = onSeasonYearChange,
                label = { Text(stringResource(R.string.anime_media_filter_airing_date_season_year)) },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                leadingIcon = if (data.seasonYear.isNotBlank()) {
                    @Composable {
                        IconButton(onClick = {
                            data.seasonYear.toIntOrNull()?.let {
                                onSeasonYearChange((it - 1).toString())
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
                                    onSeasonYearChange((it + 1).toString())
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
                                onSeasonYearChange(
                                    Calendar.getInstance()[Calendar.YEAR].toString()
                                )
                            } else {
                                onSeasonYearChange("")
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
                modifier = Modifier.width(IntrinsicSize.Min),
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
        onExpandedChange: (Boolean) -> Unit,
        range: @Composable () -> AnimeMediaFilterController.RangeData,
        onRangeChange: (String, String) -> Unit,
        @StringRes titleRes: Int,
        @StringRes titleDropdownContentDescriptionRes: Int,
    ) {
        @Suppress("NAME_SHADOWING")
        val expanded = expanded()
        Section(
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            titleRes = titleRes,
            titleDropdownContentDescriptionRes = titleDropdownContentDescriptionRes,
            summaryText = { range().summaryText },
            onSummaryClick = { onRangeChange("", "") },
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
        onShowAdultChange: (Boolean) -> Unit,
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
                onCheckedChange = onShowAdultChange,
                modifier = Modifier.padding(end = 16.dp),
            )
        }

        Divider()
    }

    @Composable
    private fun CollapseOnCloseSection(
        collapseOnClose: @Composable () -> Boolean,
        onCollapseOnCloseChange: (Boolean) -> Unit,
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
                onCheckedChange = onCollapseOnCloseChange,
                modifier = Modifier.padding(end = 16.dp),
            )
        }

        Divider()
    }

    @Composable
    private fun ShowIgnoredSection(
        showIgnored: @Composable () -> Boolean,
        onShowIgnoredChange: (Boolean) -> Unit,
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
                onCheckedChange = onShowIgnoredChange,
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
