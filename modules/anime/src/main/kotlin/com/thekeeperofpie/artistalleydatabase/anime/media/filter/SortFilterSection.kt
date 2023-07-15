package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterEntry
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterSection
import com.thekeeperofpie.artistalleydatabase.compose.filter.RangeData
import com.thekeeperofpie.artistalleydatabase.compose.filter.RangeDataFilterSection
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortAndFilterComposables.SortSection
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortOption
import kotlin.reflect.KClass

sealed class SortFilterSection(val id: String) {

    constructor(id: Int) : this(id.toString())

    @Composable
    abstract fun Content(state: ExpandedState)

    class Sort<SortType : SortOption>(
        private val enumClass: KClass<SortType>,
        private var defaultEnabled: SortType?,
        @StringRes val headerTextRes: Int,
    ) : SortFilterSection(headerTextRes) {
        var sortOptions by mutableStateOf(SortEntry.options(enumClass, defaultEnabled))
        var sortAscending by mutableStateOf(false)

        fun changeDefaultEnabled(defaultEnabled: SortType?) {
            this.defaultEnabled = defaultEnabled
            sortOptions = SortEntry.options(enumClass, defaultEnabled)
        }

        @Composable
        override fun Content(state: ExpandedState) {
            SortSection(
                headerTextRes = headerTextRes,
                expanded = { state.expandedState[id] ?: false },
                onExpandedChange = { state.expandedState[id] = it },
                sortOptions = { sortOptions },
                onSortClick = { selected ->
                    sortOptions = sortOptions.toMutableList()
                        .apply {
                            replaceAll {
                                if (it.value == selected) {
                                    val newState =
                                        if (it.state != FilterIncludeExcludeState.INCLUDE) {
                                            FilterIncludeExcludeState.INCLUDE
                                        } else {
                                            FilterIncludeExcludeState.DEFAULT
                                        }
                                    it.copy(state = newState)
                                } else it.copy(state = FilterIncludeExcludeState.DEFAULT)
                            }
                        }
                },
                sortAscending = { sortAscending },
                onSortAscendingChange = { sortAscending = it }
            )
        }
    }

    class Filter<FilterType : Any>(
        @StringRes private val titleRes: Int,
        @StringRes private val titleDropdownContentDescriptionRes: Int,
        @StringRes private val includeExcludeIconContentDescriptionRes: Int,
        private val values: List<FilterType>,
        private val valueToText: @Composable (FilterEntry.FilterEntryImpl<FilterType>) -> String,
        var exclusive: Boolean = false,
    ) : SortFilterSection(titleRes) {
        var filterOptions by mutableStateOf(FilterEntry.values(values))

        @Composable
        override fun Content(state: ExpandedState) {
            FilterSection(
                expanded = { state.expandedState[id] ?: false },
                onExpandedChange = { state.expandedState[id] = it },
                entries = { filterOptions },
                onEntryClick = { selected ->
                    filterOptions = filterOptions.toMutableList()
                        .apply {
                            if (exclusive) {
                                replaceAll {
                                    if (it.value == selected.value) {
                                        val newState =
                                            if (it.state != FilterIncludeExcludeState.INCLUDE) {
                                                FilterIncludeExcludeState.INCLUDE
                                            } else {
                                                FilterIncludeExcludeState.DEFAULT
                                            }
                                        it.copy(state = newState)
                                    } else it.copy(state = FilterIncludeExcludeState.DEFAULT)
                                }
                            } else {
                                replaceAll {
                                    if (it.value == selected.value) {
                                        it.copy(state = it.state.next())
                                    } else it
                                }
                            }
                        }
                },
                titleRes = titleRes,
                titleDropdownContentDescriptionRes = titleDropdownContentDescriptionRes,
                valueToText = valueToText,
                includeExcludeIconContentDescriptionRes = includeExcludeIconContentDescriptionRes,
                showIcons = !exclusive,
            )
        }
    }

    class Range(
        @StringRes private val titleRes: Int,
        @StringRes private val titleDropdownContentDescriptionRes: Int,
        data: RangeData,
        private val unboundedMax: Boolean = false,
    ) : SortFilterSection(titleRes) {

        var data by mutableStateOf(data)

        @Composable
        override fun Content(state: ExpandedState) {
            RangeDataFilterSection(
                expanded = { state.expandedState[id] ?: false },
                onExpandedChange = { state.expandedState[id] = it },
                range = { data },
                onRangeChange = { start, end ->
                    data = data.copy(
                        startString = start,
                        endString = end.takeIf { !unboundedMax || it != "${data.maxValue}" }
                            .orEmpty()
                    )
                },
                titleRes = titleRes,
                titleDropdownContentDescriptionRes = titleDropdownContentDescriptionRes,
            )
        }
    }

    class Switch(
        @StringRes private val titleRes: Int,
        enabled: Boolean,
    ) : SortFilterSection(titleRes) {
        var enabled by mutableStateOf(enabled)

        @Composable
        override fun Content(state: ExpandedState) {
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
                        .weight(1f)
                )

                Switch(
                    checked = enabled,
                    onCheckedChange = { enabled = it },
                    modifier = Modifier.padding(end = 16.dp),
                )
            }

            Divider()
        }
    }

    abstract class Custom(id: String) : SortFilterSection(id)

    class ExpandedState {
        val expandedState = mutableStateMapOf<String, Boolean>()
    }
}
