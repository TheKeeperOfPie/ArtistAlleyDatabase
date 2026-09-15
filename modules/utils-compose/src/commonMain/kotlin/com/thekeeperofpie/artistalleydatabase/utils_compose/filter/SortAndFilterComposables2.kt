package com.thekeeperofpie.artistalleydatabase.utils_compose.filter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.utils_compose.generated.resources.Res
import artistalleydatabase.modules.utils_compose.generated.resources.sort_ascending
import artistalleydatabase.modules.utils_compose.generated.resources.sort_descending
import artistalleydatabase.modules.utils_compose.generated.resources.sort_direction_ascending_content_description
import artistalleydatabase.modules.utils_compose.generated.resources.sort_direction_descending_content_description
import artistalleydatabase.modules.utils_compose.generated.resources.sort_direction_label
import artistalleydatabase.modules.utils_compose.generated.resources.sort_expand_content_description
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.filled.ArrowDownward
import com.thekeeperofpie.artistalleydatabase.icons.filled.ArrowUpward
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.toggle
import com.thekeeperofpie.artistalleydatabase.utils_compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState.Filter.SelectionMethod
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.SnapshotStateSetSerializer
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.replaceAll
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
    internal fun onClick(options: List<T>, selectionMethod: SelectionMethod, filter: T) {
        if (filterLockedIn == filter) return
        Snapshot.withMutableSnapshot {
            when (selectionMethod) {
                SelectionMethod.AT_MOST_ONE -> {
                    if (filterLockedIn != null) return
                    if (filter in filterIn) {
                        filterIn.clear()
                    } else {
                        filterIn.replaceAll(setOf(filter))
                    }
                    filterNotIn.clear()
                }
                SelectionMethod.EXACTLY_ONE -> {
                    filterIn.replaceAll(
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
                        filterIn.replaceAll(setOf(options.first()))
                    } else {
                        filterIn
                            .replaceAll(filterIn + filter - options.first())
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
    optionLeadingIcon: (@Composable (FilterType) -> Unit)? = null,
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
                    state.filterIn.contains(filter) -> true
                    state.filterNotIn.contains(filter) -> false
                    else -> null
                }
                if (!expanded && enabled == null) return@forEach
                FilterChip(
                    selected = enabled != null,
                    onClick = { state.onClick(options, selectionMethod, filter) },
                    enabled = filter != state.filterLockedIn && !locked,
                    label = { optionLabel(filter) },
                    leadingIcon = optionLeadingIcon?.let { { it(filter) } },
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
private fun ascendingText(ascending: Boolean) = stringResource(
    if (ascending) {
        Res.string.sort_ascending
    } else {
        Res.string.sort_descending
    }
)
