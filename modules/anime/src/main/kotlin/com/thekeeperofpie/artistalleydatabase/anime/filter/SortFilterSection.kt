package com.thekeeperofpie.artistalleydatabase.anime.filter

import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterEntry
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterSection
import com.thekeeperofpie.artistalleydatabase.compose.filter.RangeData
import com.thekeeperofpie.artistalleydatabase.compose.filter.RangeDataFilterSection
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortAndFilterComposables.SortFilterHeaderText
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortAndFilterComposables.SortSection
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.reflect.KClass

sealed class SortFilterSection(val id: String) {

    constructor(id: Int) : this(id.toString())

    abstract fun showingPreview(): Boolean

    abstract fun clear()

    @Composable
    abstract fun Content(state: ExpandedState, showDivider: Boolean)

    class Sort<SortType : SortOption>(
        private val enumClass: KClass<SortType>,
        private var defaultEnabled: SortType?,
        @StringRes val headerTextRes: Int,
    ) : SortFilterSection(headerTextRes) {
        var sortOptions by mutableStateOf(SortEntry.options(enumClass, defaultEnabled))
        var sortAscending by mutableStateOf(false)

        override fun showingPreview() =
            sortOptions.any { it.state != FilterIncludeExcludeState.DEFAULT }

        fun changeDefaultEnabled(defaultEnabled: SortType?) {
            this.defaultEnabled = defaultEnabled
            sortOptions = SortEntry.options(enumClass, defaultEnabled)
        }

        override fun clear() {
            sortOptions = SortEntry.options(enumClass, defaultEnabled)
            sortAscending = false
        }

        @Composable
        override fun Content(state: ExpandedState, showDivider: Boolean) {
            SortSection(
                headerTextRes = headerTextRes,
                expanded = { state.expandedState[id] ?: false },
                onExpandedChange = { state.expandedState[id] = it },
                sortOptions = { sortOptions },
                onSortClick = { selected ->
                    val list = sortOptions.toMutableList()
                    val existing = list.withIndex().first { it.value.value == selected }
                    val newOption = if (existing.value.state == FilterIncludeExcludeState.INCLUDE) {
                        list[(existing.index + 1) % list.size].value
                    } else {
                        selected
                    }
                    sortOptions = list.apply {
                        replaceAll {
                            if (it.value == newOption) {
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
                onSortAscendingChange = { sortAscending = it },
                showDivider = showDivider,
            )
        }
    }

    open class Filter<FilterType : Any?>(
        id: String,
        private val title: @Composable () -> String,
        @StringRes private val titleDropdownContentDescriptionRes: Int,
        @StringRes private val includeExcludeIconContentDescriptionRes: Int,
        private var values: List<FilterType>,
        private val includedSetting: MutableStateFlow<FilterType>? = null,
        private val includedSettings: MutableStateFlow<List<FilterType>>? = null,
        private val excludedSettings: MutableStateFlow<List<FilterType>>? = null,
        private val valueToText: @Composable (FilterEntry.FilterEntryImpl<FilterType>) -> String,
        var selectionMethod: SelectionMethod = SelectionMethod.ALLOW_EXCLUDE,
    ) : SortFilterSection(id) {
        enum class SelectionMethod {
            SINGLE_EXCLUSIVE,
            ONLY_INCLUDE,
            ALLOW_EXCLUDE,
        }

        constructor(
            @StringRes titleRes: Int,
            @StringRes titleDropdownContentDescriptionRes: Int,
            @StringRes includeExcludeIconContentDescriptionRes: Int,
            values: List<FilterType>,
            includedSetting: MutableStateFlow<FilterType>? = null,
            includedSettings: MutableStateFlow<List<FilterType>>? = null,
            excludedSettings: MutableStateFlow<List<FilterType>>? = null,
            valueToText: @Composable (FilterEntry.FilterEntryImpl<FilterType>) -> String,
            selectionMethod: SelectionMethod = SelectionMethod.ALLOW_EXCLUDE,
        ) : this(
            id = titleRes.toString(),
            title = { stringResource(titleRes) },
            titleDropdownContentDescriptionRes = titleDropdownContentDescriptionRes,
            includeExcludeIconContentDescriptionRes = includeExcludeIconContentDescriptionRes,
            values = values,
            includedSetting = includedSetting,
            includedSettings = includedSettings,
            excludedSettings = excludedSettings,
            valueToText = valueToText,
            selectionMethod = selectionMethod,
        )

        var filterOptions by mutableStateOf(
            FilterEntry.values(
                values,
                included = includedSetting?.value?.let(::listOf)
                    ?: includedSettings?.value
                    ?: emptyList(),
                excluded = excludedSettings?.value ?: emptyList(),
            )
        )

        fun setDefaultValues(values: List<FilterEntry.FilterEntryImpl<FilterType>>) {
            this.values = values.map { it.value }
            filterOptions = values
        }

        override fun showingPreview() =
            filterOptions.any { it.state != FilterIncludeExcludeState.DEFAULT }

        override fun clear() {
            filterOptions = FilterEntry.values(values)
        }

        @Composable
        override fun Content(state: ExpandedState, showDivider: Boolean) {
            FilterSection(
                expanded = { state.expandedState[id] ?: false },
                onExpandedChange = { state.expandedState[id] = it },
                entries = { filterOptions },
                onEntryClick = { selected ->
                    if (selected.clickable) {
                        filterOptions = filterOptions.toMutableList()
                            .apply {
                                when (selectionMethod) {
                                    SelectionMethod.SINGLE_EXCLUSIVE -> {
                                        val existing =
                                            withIndex().first { it.value.value == selected.value }
                                        val newOption =
                                            if (existing.value.state == FilterIncludeExcludeState.INCLUDE) {
                                                this[(existing.index + 1) % size].value
                                            } else {
                                                selected.value
                                            }
                                        replaceAll {
                                            if (it.value == newOption) {
                                                it.copy(state = FilterIncludeExcludeState.INCLUDE)
                                            } else {
                                                it.copy(state = FilterIncludeExcludeState.DEFAULT)
                                            }
                                        }
                                    }
                                    SelectionMethod.ONLY_INCLUDE -> replaceAll {
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
                                    SelectionMethod.ALLOW_EXCLUDE -> replaceAll {
                                        if (it.value == selected.value) {
                                            it.copy(state = it.state.next())
                                        } else it
                                    }
                                }
                            }

                        if (includedSetting != null) {
                            includedSetting.value = filterOptions
                                .first { it.state == FilterIncludeExcludeState.INCLUDE }
                                .value
                        } else {
                            includedSettings?.value = filterOptions
                                .filter { it.state == FilterIncludeExcludeState.INCLUDE }
                                .map { it.value }
                        }
                        excludedSettings?.value = filterOptions
                            .filter { it.state == FilterIncludeExcludeState.EXCLUDE }
                            .map { it.value }
                    }
                },
                title = { title() },
                titleDropdownContentDescriptionRes = titleDropdownContentDescriptionRes,
                valueToText = valueToText,
                includeExcludeIconContentDescriptionRes = includeExcludeIconContentDescriptionRes,
                showDivider = showDivider,
                showIcons = selectionMethod == SelectionMethod.ALLOW_EXCLUDE,
            )
        }
    }

    class Range(
        @StringRes private val titleRes: Int,
        @StringRes private val titleDropdownContentDescriptionRes: Int,
        val initialData: RangeData,
        private val unboundedMax: Boolean = false,
    ) : SortFilterSection(titleRes) {

        var data by mutableStateOf(initialData)

        override fun showingPreview() = data.summaryText != null

        override fun clear() {
            data = initialData
        }

        @Composable
        override fun Content(state: ExpandedState, showDivider: Boolean) {
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
                showDivider = showDivider,
            )
        }
    }

    class Switch(
        @StringRes private val titleRes: Int,
        private val defaultEnabled: Boolean,
    ) : SortFilterSection(titleRes) {
        var enabled by mutableStateOf(defaultEnabled)

        override fun showingPreview() = false

        override fun clear() {
            enabled = defaultEnabled
        }

        @Composable
        override fun Content(state: ExpandedState, showDivider: Boolean) {
            SwitchRow(
                titleRes = titleRes,
                enabled = { enabled },
                onEnabledChanged = { enabled = it },
                showDivider = showDivider,
            )
        }
    }

    class SwitchBySetting(
        @StringRes private val titleRes: Int,
        val settings: AnimeSettings,
        property: (AnimeSettings) -> MutableStateFlow<Boolean>,
    ) : SortFilterSection(titleRes) {

        private val stateFlow = property(settings)

        override fun showingPreview() = false

        override fun clear() {
            // This is persistent, can't be cleared
        }

        @Composable
        override fun Content(state: ExpandedState, showDivider: Boolean) {
            val enabled by stateFlow.collectAsState()
            SwitchRow(
                titleRes = titleRes,
                enabled = { enabled },
                onEnabledChanged = { stateFlow.value = it },
                showDivider = showDivider,
            )
        }
    }

    class Group<Child : SortFilterSection>(
        @StringRes private val titleRes: Int,
        @StringRes private val titleDropdownContentDescriptionRes: Int,
        children: List<Child> = emptyList(),
        private val onlyShowChildIfSingle: Boolean = false,
    ) : SortFilterSection(titleRes) {

        var children by mutableStateOf(children)

        override fun showingPreview() = children.any { it.showingPreview() }

        override fun clear() {
            children.forEach(SortFilterSection::clear)
        }

        @Composable
        override fun Content(state: ExpandedState, showDivider: Boolean) {
            if (onlyShowChildIfSingle && children.size == 1) {
                children.first().Content(state = state, showDivider = showDivider)
                return
            }
            val expanded = state.expandedState[id] ?: false
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { state.expandedState[id] = !expanded }
                    .animateContentSize()
            ) {
                SortFilterHeaderText(
                    true,
                    titleRes,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                )

                TrailingDropdownIconButton(
                    expanded = expanded,
                    contentDescription = stringResource(titleDropdownContentDescriptionRes),
                    onClick = { state.expandedState[id] = !expanded },
                )
            }

            if (expanded || showingPreview()) {
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    children.forEachIndexed { index, section ->
                        section.Content(state = state, showDivider = index != children.lastIndex)
                    }
                }
            }

            if (showDivider) {
                Divider()
            }
        }
    }

    class Spacer(id: String = "spacer", val height: Dp) : SortFilterSection(id) {

        override fun showingPreview() = false

        override fun clear() {
            // Not applicable
        }

        @Composable
        override fun Content(state: ExpandedState, showDivider: Boolean) {
            Spacer(Modifier.height(height))
        }
    }

    @Composable
    protected fun SwitchRow(
        @StringRes titleRes: Int,
        enabled: () -> Boolean,
        onEnabledChanged: (Boolean) -> Unit,
        showDivider: Boolean,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEnabledChanged(!enabled()) }
        ) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .weight(1f)
            )

            Switch(
                checked = enabled(),
                onCheckedChange = onEnabledChanged,
                modifier = Modifier.padding(end = 16.dp),
            )
        }

        if (showDivider) {
            Divider()
        }
    }

    abstract class Custom(id: String) : SortFilterSection(id)

    class ExpandedState {
        val expandedState = mutableStateMapOf<String, Boolean>()
    }
}
