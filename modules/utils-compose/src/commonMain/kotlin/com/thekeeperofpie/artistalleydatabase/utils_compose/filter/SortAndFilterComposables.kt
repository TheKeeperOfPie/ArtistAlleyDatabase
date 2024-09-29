@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.thekeeperofpie.artistalleydatabase.utils_compose.filter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.utils_compose.generated.resources.Res
import artistalleydatabase.modules.utils_compose.generated.resources.clear
import artistalleydatabase.modules.utils_compose.generated.resources.section_expand_all_content_description
import artistalleydatabase.modules.utils_compose.generated.resources.sort_ascending
import artistalleydatabase.modules.utils_compose.generated.resources.sort_descending
import artistalleydatabase.modules.utils_compose.generated.resources.sort_direction_ascending_content_description
import artistalleydatabase.modules.utils_compose.generated.resources.sort_direction_descending_content_description
import artistalleydatabase.modules.utils_compose.generated.resources.sort_direction_label
import artistalleydatabase.modules.utils_compose.generated.resources.sort_expand_content_description
import coil3.compose.AsyncImage
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoSizeText
import com.thekeeperofpie.artistalleydatabase.utils_compose.BackHandler
import com.thekeeperofpie.artistalleydatabase.utils_compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.utils_compose.CustomOutlinedTextField
import com.thekeeperofpie.artistalleydatabase.utils_compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortAndFilterComposables.SortFilterHeaderText
import com.thekeeperofpie.artistalleydatabase.utils_compose.isImeVisibleKmp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
object SortAndFilterComposables {

    @Composable
    fun RowScope.SortFilterHeaderText(
        expanded: Boolean,
        titleRes: StringResource,
        modifier: Modifier = Modifier,
    ) = SortFilterHeaderText(
        expanded = expanded,
        title = { stringResource(titleRes) },
        modifier = modifier,
    )

    @Composable
    fun RowScope.SortFilterHeaderText(
        expanded: Boolean,
        title: @Composable () -> String,
        modifier: Modifier = Modifier,
    ) {
        Text(
            // Use a zero width space to invalidate the Composable, or otherwise the width
            // will not change in response to expanded. This might be a bug in Compose.
            text = title() + "\u200B".takeIf { expanded }.orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            modifier = modifier
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
    fun <SortType : SortOption> SortSection(
        headerTextRes: StringResource,
        expanded: () -> Boolean,
        onExpandedChange: (Boolean) -> Unit,
        sortOptions: @Composable () -> List<SortEntry<SortType>>,
        onSortClick: (SortType) -> Unit,
        sortAscending: @Composable () -> Boolean,
        onSortAscendingChange: (Boolean) -> Unit,
        clickable: Boolean = true,
        showDivider: Boolean = true,
    ) {
        @Suppress("NAME_SHADOWING")
        val expanded = expanded() && clickable
        Column(
            modifier = Modifier.conditionally(clickable) {
                clickable { onExpandedChange(!expanded) }
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                        .animateContentSize()
                ) {
                    SortFilterHeaderText(expanded, headerTextRes)

                    sortOptions().forEach {
                        if (!expanded && it.state == FilterIncludeExcludeState.DEFAULT) return@forEach
                        FilterChip(
                            selected = it.state != FilterIncludeExcludeState.DEFAULT,
                            enabled = clickable,
                            onClick = { onSortClick(it.value) },
                            label = { Text(stringResource(it.value.textRes)) },
                            modifier = Modifier.animateContentSize()
                        )
                    }

                    if (!expanded && sortOptions()
                            .any { it.state != FilterIncludeExcludeState.DEFAULT
                                    && it.value.supportsAscending }
                    ) {
                        val sortAscending = sortAscending()
                        FilterChip(
                            selected = true,
                            enabled = clickable,
                            onClick = { onSortAscendingChange(!sortAscending) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (sortAscending) {
                                        Icons.Filled.ArrowUpward
                                    } else {
                                        Icons.Filled.ArrowDownward
                                    },
                                    contentDescription = stringResource(
                                        if (sortAscending) {
                                            Res.string.sort_direction_ascending_content_description
                                        } else {
                                            Res.string.sort_direction_descending_content_description
                                        }
                                    ),
                                )
                            },
                            label = { Text(ascendingText(sortAscending)) }
                        )
                    }
                }

                if (clickable) {
                    TrailingDropdownIconButton(
                        expanded = expanded,
                        contentDescription = stringResource(Res.string.sort_expand_content_description),
                        onClick = { onExpandedChange(!expanded) },
                        modifier = Modifier.align(Alignment.Top),
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded && sortOptions()
                    .filter { it.state != FilterIncludeExcludeState.DEFAULT }
                    .any { it.value.supportsAscending },
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column {
                    val sortAscending = sortAscending()
                    Text(
                        text = stringResource(Res.string.sort_direction_label),
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
                            enabled = clickable,
                            onClick = { onSortAscendingChange(true) },
                            label = { Text(ascendingText(true)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.ArrowUpward,
                                    contentDescription = stringResource(
                                        Res.string.sort_direction_ascending_content_description,
                                    ),
                                )
                            },
                            modifier = Modifier.animateContentSize()
                        )

                        FilterChip(
                            selected = !sortAscending,
                            enabled = clickable,
                            onClick = { onSortAscendingChange(false) },
                            label = { Text(ascendingText(false)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDownward,
                                    contentDescription = stringResource(
                                        Res.string.sort_direction_descending_content_description,
                                    ),
                                )
                            },
                            modifier = Modifier.animateContentSize()
                        )
                    }
                }
            }
        }

        if (showDivider) {
            HorizontalDivider()
        }
    }

    @Composable
    private fun ascendingText(ascending: Boolean) = stringResource(
        if (ascending) {
            Res.string.sort_ascending
        } else {
            Res.string.sort_descending
        }
    )
}

@Composable
fun <Entry : FilterEntry<*>> FilterSection(
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    entries: @Composable () -> List<Entry>,
    onEntryClick: (Entry) -> Unit,
    title: @Composable () -> String,
    titleDropdownContentDescriptionRes: StringResource,
    valueToText: @Composable (Entry) -> String,
    valueToImage: (@Composable (Entry) -> String?)? = null,
    iconContentDescriptionRes: StringResource,
    locked: Boolean = true,
    showDivider: Boolean = true,
    showIcons: Boolean = true,
) {
    @Suppress("NAME_SHADOWING")
    val expanded = expanded() && !locked
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !locked) { onExpandedChange(!expanded) }
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
                .animateContentSize()
        ) {
            SortFilterHeaderText(expanded, title)

            entries().forEach {
                if (!expanded && it.state == FilterIncludeExcludeState.DEFAULT) return@forEach
                val customIcon = valueToImage?.invoke(it)
                val leadingIcon: (@Composable () -> Unit)? = if (customIcon != null) {
                    {
                        AsyncImage(
                            model = customIcon,
                            contentDescription = stringResource(iconContentDescriptionRes),
                            modifier = Modifier
                                .size(24.dp)
                                .padding(2.dp),
                        )
                    }
                } else if (!showIcons) {
                    null
                } else {
                    {
                        IncludeExcludeIcon(it, iconContentDescriptionRes)
                    }
                }
                FilterChip(
                    selected = it.state != FilterIncludeExcludeState.DEFAULT,
                    onClick = { onEntryClick(it) },
                    enabled = it.clickable && !locked,
                    label = { Text(valueToText(it)) },
                    leadingIcon = leadingIcon,
                    modifier = Modifier
                        .animateContentSize()
                        .heightIn(min = 32.dp)
                )
            }
        }

        if (!locked) {
            TrailingDropdownIconButton(
                expanded = expanded,
                contentDescription = stringResource(titleDropdownContentDescriptionRes),
                onClick = { onExpandedChange(!expanded) },
                modifier = Modifier.align(Alignment.Top),
            )
        }
    }

    if (showDivider) {
        HorizontalDivider()
    }
}

@Composable
fun <T> SuggestionsSection(
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    suggestions: @Composable () -> List<T>,
    suggestionToText: @Composable (T) -> String,
    onSuggestionClick: (T) -> Unit,
    title: @Composable () -> String,
    titleDropdownContentDescriptionRes: StringResource,
    showDivider: Boolean = true,
) {
    @Suppress("NAME_SHADOWING")
    val expanded = expanded()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandedChange(!expanded) }
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
                .animateContentSize()
        ) {
            SortFilterHeaderText(expanded, title)

            if (expanded) {
                suggestions().forEach {
                    SuggestionChip(
                        onClick = { onSuggestionClick(it) },
                        label = { Text(suggestionToText(it)) })
                }
            }
        }

        TrailingDropdownIconButton(
            expanded = expanded,
            contentDescription = stringResource(titleDropdownContentDescriptionRes),
            onClick = { onExpandedChange(!expanded) },
            modifier = Modifier.align(Alignment.Top),
        )
    }

    if (showDivider) {
        HorizontalDivider()
    }
}


@Composable
fun IncludeExcludeIcon(
    entry: FilterEntry<*>,
    contentDescriptionRes: StringResource,
) {
    if (entry.state == FilterIncludeExcludeState.DEFAULT) {
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
            FilterIncludeExcludeState.DEFAULT -> null
            FilterIncludeExcludeState.INCLUDE -> Icons.Filled.Check
            FilterIncludeExcludeState.EXCLUDE -> Icons.Filled.Close
        }?.let {
            Icon(
                imageVector = it,
                contentDescription = stringResource(contentDescriptionRes)
            )
        }
    }
}


@Composable
fun CustomFilterSection(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    titleRes: StringResource,
    titleDropdownContentDescriptionRes: StringResource,
    summaryText: (@Composable () -> String?)? = null,
    onSummaryClick: () -> Unit = {},
    showDivider: Boolean = true,
    content: @Composable () -> Unit,
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

    if (showDivider) {
        HorizontalDivider()
    }
}

data class RangeData(
    val maxValue: Int = 100,
    val hardMax: Boolean = false,
    val startString: String = "0",
    val endString: String = if (hardMax) maxValue.toString() else "",
) {
    val startInt = startString.toIntOrNull()?.takeIf { it > 0 }?.let {
        if (hardMax) it.coerceAtMost(maxValue) else it
    }
    val endInt = endString.toIntOrNull()?.takeIf { it > 0 }?.let {
        if (hardMax) it.coerceAtMost(maxValue) else it
    }

    val apiStart = startInt?.takeIf { it > 0 }
    val apiEnd = endInt?.takeIf { it != maxValue || !hardMax }?.let { it + 1 }

    val summaryText = if (startInt != null && endInt != null) {
        if (startInt == endInt) {
            startInt.toString()
        } else if (endInt == maxValue && hardMax) {
            "≥ $startInt"
        } else {
            "$startString - $endString"
        }
    } else if (startInt != null) {
        "≥ $startInt"
    } else if (endInt != null) {
        if (hardMax && endInt == maxValue) null else "≤ $endInt"
    } else null

    val value = if (startInt != null && endInt != null) {
        startInt.coerceAtMost(maxValue).toFloat()..endInt.coerceAtMost(maxValue).toFloat()
    } else if (startInt != null) {
        startInt.coerceAtMost(maxValue).toFloat()..maxValue.toFloat()
    } else if (endInt != null) {
        0f..endInt.toFloat()
    } else {
        0f..maxValue.toFloat()
    }

    val valueRange = 0f..maxValue.toFloat()
}

@Composable
fun RangeDataFilterSection(
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    range: @Composable () -> RangeData,
    onRangeChange: (String, String) -> Unit,
    titleRes: StringResource,
    titleDropdownContentDescriptionRes: StringResource,
    showDivider: Boolean = true,
) {
    @Suppress("NAME_SHADOWING")
    val expanded = expanded()
    CustomFilterSection(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        titleRes = titleRes,
        titleDropdownContentDescriptionRes = titleDropdownContentDescriptionRes,
        summaryText = { range().summaryText },
        onSummaryClick = { onRangeChange("", "") },
        showDivider = showDivider,
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
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        autoCorrectEnabled = KeyboardOptions.Default.autoCorrectEnabled,
                    ),
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
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        autoCorrectEnabled = KeyboardOptions.Default.autoCorrectEnabled,
                    ),
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
fun SortFilterOptionsPanel(
    sections: () -> List<SortFilterSection>,
    sectionState: () -> SortFilterSection.ExpandedState,
    modifier: Modifier = Modifier,
    showClear: Boolean = true,
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

        if (showClear) {
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = { sections().forEach(SortFilterSection::clear) }) {
                    Text(text = stringResource(Res.string.clear))
                }
            }
        }
    }
}

@Composable
fun SheetDragHandle(
    sortFilterController: SortFilterController<*>?,
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
            @Suppress("NAME_SHADOWING")
            val targetValue = targetValue()

            val expandedState = sortFilterController.state.expandedState
            LaunchedEffect(targetValue) {
                if (targetValue != SheetValue.Expanded) {
                    if (collapseOnClose) {
                        expandedState.clear()
                    }
                }
            }

            val showExpandAll by remember { derivedStateOf { expandedState.none { it.value } } }

            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
            ) {
                val activatedCount by remember {
                    derivedStateOf { sortFilterController.sections.count { it.nonDefault() } }
                }

                val badgeProgress by animateFloatAsState(
                    targetValue = if (activatedCount > 0) 1f else 0f,
                    label = "Sort filter badge progress",
                )
                if (badgeProgress > 0f) {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .size(32.dp * badgeProgress)
                            .background(MaterialTheme.colorScheme.secondary, CircleShape)
                            .padding(4.dp * badgeProgress)
                            .align(Alignment.CenterVertically)
                    ) {
                        AutoSizeText(
                            text = activatedCount.coerceAtLeast(1).toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                AnimatedVisibility(visible = targetValue == SheetValue.Expanded) {
                    IconButton(
                        onClick = {
                            if (showExpandAll) {
                                sortFilterController.sections.forEach {
                                    expandedState[it.id] = true
                                    if (it is SortFilterSection.Group<*> && it.children.size == 1) {
                                        expandedState[it.children.first().id] = true
                                    }
                                }
                            } else {
                                expandedState.clear()
                            }
                        },
                    ) {
                        Icon(
                            imageVector = if (showExpandAll) {
                                Icons.Filled.UnfoldMore
                            } else {
                                Icons.Filled.UnfoldLess
                            },
                            contentDescription = stringResource(
                                Res.string.section_expand_all_content_description
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SheetContent(
    sortFilterController: SortFilterController<*>?,
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
fun SortFilterBottomScaffold(
    sortFilterController: SortFilterController<*>?,
    modifier: Modifier = Modifier,
    topBar: @Composable (() -> Unit)? = null,
    sheetState: SheetState = rememberStandardBottomSheetState(),
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(sheetState),
    bottomNavigationState: BottomNavigationState? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val bottomSheetState = scaffoldState.bottomSheetState
    BackHandler(
        enabled = bottomSheetState.targetValue == SheetValue.Expanded
                && !WindowInsets.isImeVisibleKmp
    ) {
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
