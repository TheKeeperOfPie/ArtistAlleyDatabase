@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.thekeeperofpie.artistalleydatabase.compose.filter

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortAndFilterComposables.SortFilterHeaderText
import com.thekeeperofpie.compose_proxy.R

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
object SortAndFilterComposables {

    @Composable
    fun RowScope.SortFilterHeaderText(
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
    fun <SortType : SortOption> SortSection(
        @StringRes headerTextRes: Int,
        expanded: () -> Boolean,
        onExpandedChange: (Boolean) -> Unit,
        sortOptions: @Composable () -> List<SortEntry<SortType>>,
        onSortClick: (SortType) -> Unit,
        sortAscending: @Composable () -> Boolean,
        onSortAscendingChange: (Boolean) -> Unit,
    ) {
        @Suppress("NAME_SHADOWING")
        val expanded = expanded()
        Column(modifier = Modifier.clickable { onExpandedChange(!expanded) }) {
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
                            onClick = { onSortClick(it.value) },
                            label = { Text(stringResource(it.value.textRes)) },
                            modifier = Modifier.animateContentSize()
                        )
                    }

                    if (!expanded && sortOptions()
                            .any { it.state != FilterIncludeExcludeState.DEFAULT }
                    ) {
                        val sortAscending = sortAscending()
                        FilterChip(
                            selected = true,
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
                                            R.string.sort_direction_ascending_content_description
                                        } else {
                                            R.string.sort_direction_descending_content_description
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
                    contentDescription = stringResource(R.string.sort_expand_content_description),
                    onClick = { onExpandedChange(!expanded) },
                    modifier = Modifier.align(Alignment.Top),
                )
            }

            AnimatedVisibility(
                visible = expanded && sortOptions()
                    .any { it.state != FilterIncludeExcludeState.DEFAULT },
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column {
                    val sortAscending = sortAscending()
                    Text(
                        text = stringResource(R.string.sort_direction_label),
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
                            onClick = { onSortAscendingChange(true) },
                            label = { Text(ascendingText(true)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.ArrowUpward,
                                    contentDescription = stringResource(
                                        R.string.sort_direction_ascending_content_description,
                                    ),
                                )
                            },
                            modifier = Modifier.animateContentSize()
                        )

                        FilterChip(
                            selected = !sortAscending,
                            onClick = { onSortAscendingChange(false) },
                            label = { Text(ascendingText(false)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDownward,
                                    contentDescription = stringResource(
                                        R.string.sort_direction_descending_content_description,
                                    ),
                                )
                            },
                            modifier = Modifier.animateContentSize()
                        )
                    }
                }
            }
        }

        Divider()
    }

    @Composable
    private fun ascendingText(ascending: Boolean) = stringResource(
        if (ascending) {
            R.string.sort_ascending
        } else {
            R.string.sort_descending
        }
    )
}


@Composable
fun <Entry : FilterEntry<*>> FilterSection(
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    entries: @Composable () -> List<Entry>,
    onEntryClick: (Entry) -> Unit,
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
            .clickable { onExpandedChange(!expanded) }
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
                .animateContentSize()
        ) {
            SortFilterHeaderText(expanded, titleRes)

            entries().forEach {
                if (!expanded && it.state == FilterIncludeExcludeState.DEFAULT) return@forEach
                FilterChip(
                    selected = it.state != FilterIncludeExcludeState.DEFAULT,
                    onClick = { onEntryClick(it) },
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
            onClick = { onExpandedChange(!expanded) },
            modifier = Modifier.align(Alignment.Top),
        )
    }

    Divider()
}


@Composable
fun IncludeExcludeIcon(
    entry: FilterEntry<*>,
    @StringRes contentDescriptionRes: Int
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
