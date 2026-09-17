@file:OptIn(ExperimentalComposeUiApi::class)

package com.thekeeperofpie.artistalleydatabase.utils_compose.filter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import artistalleydatabase.modules.utils_compose.generated.resources.Res
import artistalleydatabase.modules.utils_compose.generated.resources.section_expand_all_content_description
import artistalleydatabase.modules.utils_compose.generated.resources.sort_ascending
import artistalleydatabase.modules.utils_compose.generated.resources.sort_descending
import artistalleydatabase.modules.utils_compose.generated.resources.sort_direction_ascending_content_description
import artistalleydatabase.modules.utils_compose.generated.resources.sort_direction_descending_content_description
import artistalleydatabase.modules.utils_compose.generated.resources.sort_direction_label
import artistalleydatabase.modules.utils_compose.generated.resources.sort_expand_content_description
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.filled.ArrowDownward
import com.thekeeperofpie.artistalleydatabase.icons.filled.ArrowUpward
import com.thekeeperofpie.artistalleydatabase.icons.filled.UnfoldLess
import com.thekeeperofpie.artistalleydatabase.icons.filled.UnfoldMore
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.toggle
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.utils_compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.animateEnterExit
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.renderMaybeInSharedTransitionScopeOverlay
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState.Filter.SelectionMethod
import com.thekeeperofpie.artistalleydatabase.utils_compose.isImeVisibleKmp
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.SnapshotStateSetSerializer
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.replaceAll
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
private fun Header(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
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
    ) {
        ProvideTextStyle(MaterialTheme.typography.titleMedium) {
            content()
        }
    }
}

@Composable
fun <SortType> SortSection(
    header: @Composable () -> Unit,
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    sortOptions: () -> List<SortType>,
    sortOption: () -> SortType,
    onSortClick: (SortType) -> Unit,
    sortOptionLabel: @Composable (SortType) -> Unit,
    sortAscending: () -> Boolean?,
    onSortAscendingChange: (Boolean) -> Unit,
    clickable: Boolean = true,
) {
    @Suppress("NAME_SHADOWING")
    val expanded = expanded() && clickable
    Column(
        modifier = Modifier.clickable(enabled = clickable) { onExpandedChange(!expanded) }
    ) {
        val sortAscending = sortAscending()
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
                Header(expanded = expanded, content = header)

                val sortOption = sortOption()
                sortOptions().forEach {
                    val enabled = it == sortOption
                    if (!expanded && !enabled) return@forEach
                    FilterChip(
                        selected = enabled,
                        enabled = clickable,
                        onClick = { onSortClick(it) },
                        label = { sortOptionLabel(it) },
                        modifier = Modifier.animateContentSize()
                    )
                }

                if (!expanded && sortAscending != null) {
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
            visible = expanded && sortAscending != null,
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

                if (sortAscending != null) {
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
    }
}

@Composable
fun SwitchSection(
    title: @Composable () -> Unit,
    enabled: () -> Boolean,
    onEnabledChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEnabledChanged(!enabled()) }
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .weight(1f)
        ) {
            ProvideTextStyle(MaterialTheme.typography.titleMedium) {
                title()
            }
        }

        Switch(
            checked = enabled(),
            onCheckedChange = onEnabledChanged,
            modifier = Modifier.padding(end = 16.dp),
        )
    }
}

@Serializable
class FilterSectionState<T>(
    @Serializable(SnapshotStateSetSerializer::class)
    val filterIn: SnapshotStateSet<T> = mutableStateSetOf(),
    @Serializable(SnapshotStateSetSerializer::class)
    val filterNotIn: SnapshotStateSet<T> = mutableStateSetOf(),
    val filterLockedIn: T? = null,
) {
    fun clear() {
        Snapshot.withMutableSnapshot {
            filterIn.replaceAll(setOfNotNull(filterLockedIn))
            filterNotIn.clear()
        }
    }

    companion object {
        fun <T> onClick(
            options: List<T>,
            selectionMethod: SelectionMethod,
            filterIn: Set<T>,
            filterNotIn: Set<T>,
            filter: T,
            filterLockedIn: T? = null,
        ): Pair<Set<T>, Set<T>> {
            if (filterLockedIn == filter) return filterIn to filterNotIn
            val filterIn = filterIn.toMutableSet()
            val filterNotIn = filterNotIn.toMutableSet()
            when (selectionMethod) {
                SelectionMethod.AT_MOST_ONE -> {
                    if (filterLockedIn != null) return filterIn to filterNotIn
                    if (filter in filterIn) {
                        filterIn.clear()
                    } else {
                        filterIn.clear()
                        filterIn.add(filter)
                    }
                    filterNotIn.clear()
                }
                SelectionMethod.EXACTLY_ONE -> {
                    filterIn.clear()
                    filterIn.addAll(
                        setOf(
                            if (filter in filterIn) {
                                options[(options.indexOf(filter) + 1) % options.size]
                            } else {
                                filter
                            }
                        )
                    )
                    filterNotIn.clear()
                }
                SelectionMethod.ONLY_INCLUDE -> filterIn.toggle(filter)
                SelectionMethod.ONLY_INCLUDE_WITH_EXCLUSIVE_FIRST -> {
                    if (filterIn.contains(filter)) {
                        filterIn -= filter
                    } else if (filter == options.first()) {
                        filterIn.clear()
                        filterIn.add(options.first())
                    } else {
                        filterIn.clear()
                        filterIn.addAll(filterIn + filter - options.first())
                    }
                }
                SelectionMethod.ALLOW_EXCLUDE -> {
                    if (filterIn.contains(filter)) {
                        filterIn -= filter
                        filterNotIn += filter
                    } else if (filterNotIn.contains(filter)) {
                        filterNotIn -= filter
                    } else {
                        filterIn += filter
                    }
                }
            }
            return filterIn to filterNotIn
        }
    }
}

@Composable
fun <FilterType> FilterSection2(
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    options: List<FilterType>,
    state: FilterSectionState<FilterType>,
    selectionMethod: SelectionMethod,
    title: @Composable () -> Unit,
    titleDropdownContentDescriptionRes: StringResource,
    optionLabel: @Composable (FilterType) -> Unit,
    optionLeadingIcon: (@Composable (FilterType, enabled: Boolean?) -> Unit)? = null,
    locked: Boolean = false,
) {
    FilterSection2(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        options = options,
        filterIn = { state.filterIn },
        filterNotIn = { state.filterNotIn },
        filterLockedIn = { state.filterLockedIn },
        title = title,
        titleDropdownContentDescriptionRes = titleDropdownContentDescriptionRes,
        onOptionClick = {
            val (newFilterIn, newFilterNotIn) = FilterSectionState.onClick(
                options = options,
                selectionMethod = selectionMethod,
                filterIn = state.filterIn,
                filterNotIn = state.filterNotIn,
                filterLockedIn = state.filterLockedIn,
                filter = it
            )
            Snapshot.withMutableSnapshot {
                state.filterIn.replaceAll(newFilterIn)
                state.filterNotIn.replaceAll(newFilterNotIn)
            }
        },
        optionLabel = optionLabel,
        optionLeadingIcon = optionLeadingIcon,
        locked = locked,
    )
}

@Composable
fun <FilterType> FilterSection2(
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    options: List<FilterType>,
    filterIn: () -> Set<FilterType>,
    filterNotIn: () -> Set<FilterType>,
    title: @Composable () -> Unit,
    titleDropdownContentDescriptionRes: StringResource,
    onOptionClick: (FilterType) -> Unit,
    optionLabel: @Composable (FilterType) -> Unit,
    optionLeadingIcon: (@Composable (FilterType, enabled: Boolean?) -> Unit)? = null,
    filterLockedIn: () -> FilterType? = { null },
    locked: Boolean = false,
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
            Header(expanded = expanded, content = title)

            options.forEach { filter ->
                val enabled = when {
                    filterIn().contains(filter) -> true
                    filterNotIn().contains(filter) -> false
                    else -> null
                }
                if (!expanded && enabled == null) return@forEach
                FilterChip(
                    selected = enabled != null,
                    onClick = { onOptionClick(filter) },
                    enabled = filter != filterLockedIn() && !locked,
                    label = { optionLabel(filter) },
                    leadingIcon = optionLeadingIcon?.let { { it(filter, enabled) } },
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
}

@Composable
fun CustomFilterSection(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    header: @Composable () -> Unit,
    headerDropdownContentDescriptionRes: StringResource,
    summaryText: (@Composable () -> String?)? = null,
    onSummaryClick: () -> Unit = {},
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
            Header(expanded, content = header)

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
            contentDescription = stringResource(headerDropdownContentDescriptionRes),
            onClick = { onExpandedChange(!expanded) },
        )
    }

    content()
}

@Composable
fun SectionGroup(
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    header: @Composable () -> Unit,
    headerDropdownContentDescriptionRes: StringResource,
    content: @Composable () -> Unit,
) {
    val expanded = expanded()
    CustomFilterSection(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        header = header,
        headerDropdownContentDescriptionRes = headerDropdownContentDescriptionRes,
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(modifier = Modifier.padding(start = 16.dp)) {
                content()
            }
        }
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


@Composable
fun SortFilterBottomScaffold2(
    modifier: Modifier = Modifier,
    topBar: @Composable (() -> Unit)? = null,
    sheetState: SheetState = rememberBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        enabledValues = setOf(SheetValue.PartiallyExpanded, SheetValue.Expanded),
    ),
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(sheetState),
    sheetPeekHeight: Dp = Dp.Unspecified,
    bottomNavigationState: BottomNavigationState? = null,
    sheetContent: @Composable ColumnScope.() -> Unit,
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
        sheetPeekHeight = (sheetPeekHeight.takeIf { it.isSpecified } ?: 56.dp) +
                (bottomNavigationState?.bottomOffsetPadding() ?: 0.dp),
        sheetDragHandle = null,
        sheetContent = {
            // Handle compact layouts with a bottom nav bar
            val extraAnimateHeight = with(LocalDensity.current) { 100.dp.roundToPx() }
            Column(
                modifier = modifier
                    .animateEnterExit(
                        enter = slideInVertically { it + extraAnimateHeight },
                        exit = slideOutVertically { it + extraAnimateHeight },
                    )
                    .renderMaybeInSharedTransitionScopeOverlay(1f)
                    .sharedElement(SharedTransitionKey.makeKeyForId("sortFilterBottomSheet"), "")
                    .clip(BottomSheetDefaults.ExpandedShape)
            ) {
                sheetContent()
            }
        },
        sheetContainerColor = Color.Unspecified,
        sheetTonalElevation = 4.dp,
        sheetShadowElevation = 4.dp,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = topBar,
        modifier = modifier,
        content = content,
        // TODO: Error state
        // snackbarHost = {},
    )
}

@Composable
fun SortFilterBottomScaffoldSheetContent(
    sheetState: SheetState,
    actions: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()

    Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (sheetState.currentValue == SheetValue.Expanded) {
                        scope.launch { sheetState.partialExpand() }
                    } else {
                        scope.launch { sheetState.expand() }
                    }
                }
        ) {
            BottomSheetDefaults.DragHandle(
                color = LocalContentColor.current,
                modifier = Modifier.align(Alignment.Center)
            )

            if (actions != null) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                ) {
                    actions()
                }
            }
        }
    }

    Column(
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        content()
    }
}

@Composable
fun SectionsExpandIndicator(
    allSectionsExpanded: () -> Boolean,
    onSectionsExpandedChanged: (Boolean) -> Unit,
    activatedCount: () -> Int,
    targetValue: () -> SheetValue,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        val activatedCount = activatedCount()
        AnimatedVisibility(
            visible = activatedCount > 0,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .sizeIn(minWidth = 32.dp, minHeight = 32.dp)
                    .background(MaterialTheme.colorScheme.secondary, CircleShape)
                    .padding(4.dp)
            ) {
                AutoHeightText(
                    text = activatedCount.coerceAtLeast(1).toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        val allSectionsExpanded = allSectionsExpanded()
        AnimatedVisibility(visible = targetValue() == SheetValue.Expanded) {
            IconButton(onClick = { onSectionsExpandedChanged(!allSectionsExpanded) }) {
                Icon(
                    imageVector = if (allSectionsExpanded) {
                        Icons.Filled.UnfoldLess
                    } else {
                        Icons.Filled.UnfoldMore
                    },
                    contentDescription = stringResource(
                        Res.string.section_expand_all_content_description
                    ),
                )
            }
        }
    }
}
