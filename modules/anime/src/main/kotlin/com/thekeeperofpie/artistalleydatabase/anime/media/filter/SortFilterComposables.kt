@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.anilist.type.MediaSeason
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.filter.SortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.filter.SortFilterSection
import com.thekeeperofpie.artistalleydatabase.anime.media.LocalMediaTagDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.ui.StartEndDateRow
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.BottomSheetScaffoldNoAppBarOffset
import com.thekeeperofpie.artistalleydatabase.compose.CustomOutlinedTextField
import com.thekeeperofpie.artistalleydatabase.compose.FilterChip
import com.thekeeperofpie.artistalleydatabase.compose.ItemDropdown
import com.thekeeperofpie.artistalleydatabase.compose.MinWidthTextField
import com.thekeeperofpie.artistalleydatabase.compose.SheetState
import com.thekeeperofpie.artistalleydatabase.compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.compose.filter.CustomFilterSection
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.compose.filter.IncludeExcludeIcon
import com.thekeeperofpie.artistalleydatabase.compose.rememberBottomSheetScaffoldState
import com.thekeeperofpie.artistalleydatabase.compose.rememberStandardBottomSheetState
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.roundToInt

@Composable
fun SortFilterOptionsPanel(
    sections: () -> List<SortFilterSection>,
    sectionState: () -> SortFilterSection.ExpandedState,
    modifier: Modifier = Modifier,
) {
    HorizontalDivider()
    Column(modifier = modifier) {
        Column(
            Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .animateContentSize()
        ) {
            val state = sectionState()
            sections().forEach {
                it.Content(state, showDivider = true)
            }
        }
        HorizontalDivider()
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = { sections().forEach(SortFilterSection::clear) }) {
                Text(text = stringResource(UtilsStringR.clear))
            }
        }
    }
}

@Composable
fun SortFilterBottomScaffoldNoAppBarOffset(
    sortFilterController: SortFilterController?,
    modifier: Modifier = Modifier,
    topBar: @Composable (() -> Unit)? = null,
    sheetState: SheetState = rememberStandardBottomSheetState(
        confirmValueChange = { it != SheetValue.Hidden },
        skipHiddenState = true,
    ),
    bottomNavigationState: BottomNavigationState? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val scaffoldState = rememberBottomSheetScaffoldState(sheetState)

    val scope = rememberCoroutineScope()
    val bottomSheetState = scaffoldState.bottomSheetState
    BackHandler(bottomSheetState.targetValue == SheetValue.Expanded && !WindowInsets.isImeVisible) {
        scope.launch { bottomSheetState.partialExpand() }
    }

    BottomSheetScaffoldNoAppBarOffset(
        scaffoldState = scaffoldState,
        sheetPeekHeight = if (sortFilterController == null) {
            0.dp
        } else {
            56.dp + (bottomNavigationState?.bottomOffsetPadding() ?: 0.dp)
        },
        sheetDragHandle = {
            SheetDragHandle(
                sortFilterController = sortFilterController,
                targetValue = { bottomSheetState.targetValue },
                onClick = {
                    if (bottomSheetState.currentValue == SheetValue.Expanded) {
                        scope.launch { bottomSheetState.partialExpand() }
                    } else {
                        scope.launch { bottomSheetState.expand() }
                    }
                },
            )
        },
        sheetContent = {
            SheetContent(
                sortFilterController = sortFilterController,
                bottomNavigationState = bottomNavigationState,
            )
        },
        sheetTonalElevation = 4.dp,
        sheetShadowElevation = 4.dp,
        topBar = topBar,
        modifier = modifier,
        content = content,
        // TODO: Error state
        // snackbarHost = {},
    )
}

@Composable
fun SortFilterBottomScaffold(
    sortFilterController: SortFilterController?,
    modifier: Modifier = Modifier,
    topBar: @Composable (() -> Unit)? = null,
    bottomNavigationState: BottomNavigationState? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val scaffoldState = androidx.compose.material3.rememberBottomSheetScaffoldState(
        androidx.compose.material3.rememberStandardBottomSheetState(
            confirmValueChange = { it != SheetValue.Hidden },
            skipHiddenState = true,
        )
    )

    val scope = rememberCoroutineScope()
    val bottomSheetState = scaffoldState.bottomSheetState
    BackHandler(enabled = bottomSheetState.targetValue == SheetValue.Expanded) {
        scope.launch { bottomSheetState.partialExpand() }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = if (sortFilterController == null) {
            0.dp
        } else {
            56.dp + (bottomNavigationState?.bottomOffsetPadding() ?: 0.dp)
        },
        sheetDragHandle = {
            SheetDragHandle(
                sortFilterController = sortFilterController,
                targetValue = { bottomSheetState.targetValue },
                onClick = {
                    if (bottomSheetState.currentValue == SheetValue.Expanded) {
                        scope.launch { bottomSheetState.partialExpand() }
                    } else {
                        scope.launch { bottomSheetState.expand() }
                    }
                },
            )
        },
        sheetContent = {
            SheetContent(
                sortFilterController = sortFilterController,
                bottomNavigationState = bottomNavigationState,
            )
        },
        sheetTonalElevation = 4.dp,
        sheetShadowElevation = 4.dp,
        topBar = topBar,
        modifier = modifier,
        content = content,
        // TODO: Error state
        // snackbarHost = {},
    )
}

@Composable
private fun SheetDragHandle(
    sortFilterController: SortFilterController?,
    targetValue: () -> SheetValue,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        BottomSheetDefaults.DragHandle(modifier = Modifier.align(Alignment.Center))

        val collapseOnClose = sortFilterController?.collapseOnClose()
        if (collapseOnClose != null) {
            val targetValue = targetValue()
            val expandAllAlpha by animateFloatAsState(
                targetValue = if (targetValue == SheetValue.Expanded) 1f else 0f,
                label = "Anime filter expand all alpha",
            )

            val expandedState = sortFilterController.state.expandedState
            LaunchedEffect(targetValue) {
                if (targetValue != SheetValue.Expanded) {
                    if (collapseOnClose) {
                        expandedState.clear()
                    }
                }
            }

            val showExpandAll = expandedState.none { it.value }

            IconButton(
                onClick = {
                    if (showExpandAll) {
                        sortFilterController.sections.forEach {
                            expandedState[it.id] = true
                        }
                    } else {
                        expandedState.clear()
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .alpha(expandAllAlpha)
            ) {
                Icon(
                    imageVector = if (showExpandAll) {
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
    }
}

@Composable
private fun SheetContent(
    sortFilterController: SortFilterController?,
    bottomNavigationState: BottomNavigationState?,
) {
    if (sortFilterController != null) {
        SortFilterOptionsPanel(
            sections = { sortFilterController.sections },
            sectionState = { sortFilterController.state },
            modifier = Modifier.padding(
                bottom = bottomNavigationState?.bottomOffsetPadding() ?: 0.dp
            )
        )
    }
}

@Composable
fun AiringDateSection(
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    data: @Composable () -> AiringDate,
    onSeasonChange: (MediaSeason?) -> Unit,
    onSeasonYearChange: (String) -> Unit,
    onIsAdvancedToggle: (Boolean) -> Unit,
    onRequestDatePicker: (Boolean) -> Unit,
    onDateChange: (start: Boolean, Long?) -> Unit,
    showDivider: Boolean,
) {
    @Suppress("NAME_SHADOWING")
    val expanded = expanded()

    @Suppress("NAME_SHADOWING")
    val data = data()
    CustomFilterSection(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        titleRes = R.string.anime_media_filter_airing_date,
        titleDropdownContentDescriptionRes = R.string.anime_media_filter_airing_date_content_description,
        summaryText = {
            when (data) {
                is AiringDate.Basic -> {
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
                is AiringDate.Advanced -> data.summaryText()
            }
        },
        onSummaryClick = {
            when (data) {
                is AiringDate.Basic -> {
                    onSeasonChange(null)
                    onSeasonYearChange("")
                }
                is AiringDate.Advanced -> {
                    onDateChange(true, null)
                    onDateChange(false, null)
                }
            }
        },
        showDivider = showDivider,
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column {
                @Suppress("NAME_SHADOWING")
                val data = data()
                val tabIndex = if (data is AiringDate.Basic) 0 else 1
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
                    is AiringDate.Basic ->
                        AiringDateBasicSection(
                            data = data,
                            onSeasonChange = onSeasonChange,
                            onSeasonYearChange = onSeasonYearChange,
                        )
                    is AiringDate.Advanced -> AiringDateAdvancedSection(
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
    data: AiringDate.Basic,
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

        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        val minWidth = screenWidth / 2 - 32.dp
        val leadingIcon = @Composable {
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

        MinWidthTextField(
            value = data.seasonYear,
            onValueChange = onSeasonYearChange,
            label = { Text(stringResource(R.string.anime_media_filter_airing_date_season_year)) },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            leadingIcon = leadingIcon.takeIf { data.seasonYear.isNotBlank() },
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
            minWidth = minWidth,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .widthIn(max = (screenWidth - 160.dp).coerceAtLeast(minWidth)),
        )
    }
}

@Composable
fun AiringDateAdvancedSection(
    data: AiringDate.Advanced,
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
fun TagSection(
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    tags: @Composable () -> Map<String, TagSection>,
    onTagClick: (String) -> Unit,
    tagRank: @Composable () -> String,
    onTagRankChange: (String) -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    showDivider: Boolean,
) {
    @Suppress("NAME_SHADOWING")
    val expanded = expanded()
    CustomFilterSection(
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
        showDivider = showDivider,
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            if (expanded) {
                TextField(
                    value = query,
                    placeholder = {
                        Text(text = stringResource(R.string.anime_media_tag_search_placeholder))
                    },
                    trailingIcon = {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(
                                    R.string.anime_media_tag_search_clear_content_description
                                ),
                            )
                        }
                    },
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            @Suppress("NAME_SHADOWING")
            val tags = tags()
            val children =
                tags.values.filterIsInstance<TagSection.Tag>()
            if (children.isNotEmpty()) {
                TagChips(
                    tags = children,
                    level = 0,
                    onTagClick = onTagClick,
                )
            }

            val subcategoriesToShow = if (expanded) {
                tags.values
            } else {
                if (query.isNotBlank()) {
                    tags.values.mapNotNull {
                        it.filter {
                            it.state != FilterIncludeExcludeState.DEFAULT
                                    || it.name.contains(query, ignoreCase = true)
                        }
                    }
                } else {
                    tags.values.mapNotNull {
                        it.filter { it.state != FilterIncludeExcludeState.DEFAULT }
                    }
                }
            }.filterIsInstance<TagSection.Category>()

            subcategoriesToShow.forEachIndexed { index, section ->
                TagSubsection(
                    name = section.name,
                    children = section.children.values,
                    parentExpanded = expanded,
                    level = 0,
                    onTagClick = onTagClick,
                    query = query,
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
    children: Collection<TagSection>,
    parentExpanded: Boolean,
    level: Int,
    onTagClick: (String) -> Unit,
    query: String,
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

    val tags = children.filterIsInstance<TagSection.Tag>()
    val tagsToShow = if (expanded) {
        tags
    } else {
        if (query.isNotBlank()) {
            tags.filter {
                it.state != FilterIncludeExcludeState.DEFAULT
                        || it.name.contains(query, ignoreCase = true)
            }
        } else {
            tags.filter { it.state != FilterIncludeExcludeState.DEFAULT }
        }
    }

    val subcategories =
        children.filterIsInstance<TagSection.Category>()
    val subcategoriesToShow = if (expanded) {
        subcategories
    } else {
        if (query.isNotBlank()) {
            subcategories.mapNotNull {
                it.filter {
                    it.state != FilterIncludeExcludeState.DEFAULT
                            || it.name.contains(query, ignoreCase = true)
                } as? TagSection.Category
            }
        } else {
            subcategories.mapNotNull {
                it.filter { it.state != FilterIncludeExcludeState.DEFAULT } as? TagSection.Category
            }
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
                level,
                onTagClick,
            )
        }

        subcategoriesToShow.forEachIndexed { index, section ->
            TagSubsection(
                section.name,
                section.children.values,
                parentExpanded = parentExpanded && expanded,
                level + 1,
                onTagClick,
                query = query,
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
    tags: List<TagSection.Tag>,
    level: Int,
    onTagClick: (String) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp * level + 48.dp, end = 16.dp)
            .animateContentSize(),
    ) {
        val mediaTagDialogController = LocalMediaTagDialogController.current
        tags.forEach {
            FilterChip(
                selected = it.state != FilterIncludeExcludeState.DEFAULT,
                onClick = { onTagClick(it.id) },
                onLongClickLabel = stringResource(
                    R.string.anime_media_tag_long_click_content_description
                ),
                onLongClick = { mediaTagDialogController?.onLongClickTag(it.id) },
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