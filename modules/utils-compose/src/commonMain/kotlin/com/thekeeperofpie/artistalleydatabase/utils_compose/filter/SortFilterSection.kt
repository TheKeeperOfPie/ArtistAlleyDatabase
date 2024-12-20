package com.thekeeperofpie.artistalleydatabase.utils_compose.filter

import androidx.annotation.MainThread
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.utils_compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortAndFilterComposables.SortFilterHeaderText
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortAndFilterComposables.SortSection
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3Api::class)
sealed class SortFilterSection(val id: String) {

    constructor(id: Int) : this(id.toString())

    abstract fun showingPreview(): Boolean

    open fun nonDefault() = showingPreview()

    abstract fun clear()

    @Composable
    abstract fun Content(state: ExpandedState, showDivider: Boolean)

    class SortBySetting<SortType : SortOption>(
        enumClass: KClass<SortType>,
        headerTextRes: StringResource,
        sortProperty: MutableStateFlow<String>,
        sortAscendingProperty: MutableStateFlow<Boolean>,
        deserialize: (String) -> SortType,
        serialize: (SortType?) -> String,
    ) : Sort<SortType>(
        enumClass = enumClass,
        defaultEnabled = deserialize(sortProperty.value),
        headerTextRes = headerTextRes,
        defaultSortAscending = sortAscendingProperty.value,
        onSortSelected = { sortProperty.value = serialize(it) },
        onSortAscendingChange = { sortAscendingProperty.value = it },
    )

    open class Sort<SortType : SortOption>(
        private val enumClass: KClass<SortType>,
        private var defaultEnabled: SortType?,
        val headerTextRes: StringResource,
        val defaultSortAscending: Boolean = false,
        private val onSortSelected: (SortType?) -> Unit = {},
        private val onSortAscendingChange: (Boolean) -> Unit = {},
        private var options: List<SortType> = enumClass.java.enumConstants?.toList().orEmpty(),
    ) : SortFilterSection(headerTextRes.key) {
        var sortOptions
                by mutableStateOf(options.map(::SortEntry).withSelectedOption(defaultEnabled))
            private set
        var sortAscending by mutableStateOf(defaultSortAscending)
        var clickable by mutableStateOf(true)

        override fun showingPreview() =
            sortOptions.any { it.state != FilterIncludeExcludeState.DEFAULT }

        override fun nonDefault() = showingPreview()
                && sortOptions.filter { it.state != FilterIncludeExcludeState.DEFAULT }
            .map { it.value } != listOf(defaultEnabled)

        @MainThread
        fun setOptions(options: List<SortType>) {
            this.options = options
            var filteredOptions = sortOptions.filter { options.contains(it.value) }
            if (filteredOptions.none { it.state == FilterIncludeExcludeState.INCLUDE }) {
                filteredOptions = filteredOptions.withSelectedOption(defaultEnabled)
            }
            sortOptions = filteredOptions
        }

        fun changeDefault(defaultEnabled: SortType, sortAscending: Boolean, lockSort: Boolean) {
            this.defaultEnabled = defaultEnabled
            sortOptions = sortOptions.withSelectedOption(defaultEnabled)
            onSortSelected(defaultEnabled)
            changeSortAscending(sortAscending)
            clickable = !lockSort
        }

        fun changeSelected(selected: SortType, sortAscending: Boolean) {
            if (sortOptions.singleOrNull { it.state == FilterIncludeExcludeState.INCLUDE }?.value != selected) {
                onSelected(selected)
            }
            changeSortAscending(sortAscending)
        }

        override fun clear() {
            sortOptions = options.map(::SortEntry).withSelectedOption(defaultEnabled)
            onSortSelected(defaultEnabled)
            changeSortAscending(defaultSortAscending)
        }

        private fun changeSortAscending(sortAscending: Boolean) {
            this.sortAscending = sortAscending
            onSortAscendingChange(sortAscending)
        }

        private fun onSelected(selected: SortType) {
            val list = sortOptions.toMutableList()
            val existing = list.withIndex().first { it.value.value == selected }
            if (existing == selected) return
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
            onSortSelected(newOption)
        }

        @Composable
        override fun Content(state: ExpandedState, showDivider: Boolean) {
            SortSection(
                headerTextRes = headerTextRes,
                expanded = { state.expandedState[id] ?: false },
                onExpandedChange = { state.expandedState[id] = it },
                sortOptions = { sortOptions },
                onSortClick = ::onSelected,
                sortAscending = { sortAscending },
                onSortAscendingChange = ::changeSortAscending,
                clickable = clickable,
                showDivider = showDivider,
            )
        }
    }

    open class Filter<FilterType : Any?>(
        id: String,
        private val title: @Composable () -> String,
        private val titleDropdownContentDescriptionRes: StringResource,
        private val includeExcludeIconContentDescriptionRes: StringResource,
        private var values: List<FilterType>,
        private val includedSetting: MutableStateFlow<FilterType>? = null,
        private val includedSettings: MutableStateFlow<List<FilterType>>? = null,
        private val excludedSettings: MutableStateFlow<List<FilterType>>? = null,
        private val valueToText: @Composable (FilterEntry.FilterEntryImpl<FilterType>) -> String,
        private val valueToImage: (@Composable (FilterEntry.FilterEntryImpl<FilterType>) -> String?)? = null,
        var selectionMethod: SelectionMethod = SelectionMethod.ALLOW_EXCLUDE,
    ) : SortFilterSection(id) {
        enum class SelectionMethod {
            SINGLE_EXCLUSIVE,
            ONLY_INCLUDE,
            ALLOW_EXCLUDE,
        }

        constructor(
            titleRes: StringResource,
            titleDropdownContentDescriptionRes: StringResource,
            includeExcludeIconContentDescriptionRes: StringResource,
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

        private var locked by mutableStateOf(false)

        fun setDefaultValues(values: List<FilterEntry.FilterEntryImpl<FilterType>>) {
            this.values = values.map { it.value }
            filterOptions = values
        }

        override fun showingPreview() =
            filterOptions.any { it.state != FilterIncludeExcludeState.DEFAULT }

        override fun clear() {
            filterOptions = FilterEntry.values(values)
        }

        fun setIncluded(selected: FilterType, locked: Boolean) {
            filterOptions = FilterEntry.values(values, included = listOf(selected))
            this.locked = locked
        }

        fun setExcluded(selected: FilterType, locked: Boolean) {
            filterOptions = FilterEntry.values(values, excluded = listOf(selected))
            this.locked = locked
        }

        @Composable
        override fun Content(state: ExpandedState, showDivider: Boolean) {
            FilterSection(
                expanded = { state.expandedState[id] ?: false },
                onExpandedChange = { state.expandedState[id] = it },
                entries = { filterOptions },
                onEntryClick = { selected ->
                    if (!locked && selected.clickable) {
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
                                        } else it
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
                valueToImage = valueToImage,
                iconContentDescriptionRes = includeExcludeIconContentDescriptionRes,
                locked = locked,
                showDivider = showDivider,
                showIcons = selectionMethod == SelectionMethod.ALLOW_EXCLUDE,
            )
        }
    }

    class Range(
        private val titleRes: StringResource,
        private val titleDropdownContentDescriptionRes: StringResource,
        val initialData: RangeData,
        private val unboundedMax: Boolean = false,
    ) : SortFilterSection(titleRes.key) {

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
        private val titleRes: StringResource,
        private val defaultEnabled: Boolean,
    ) : SortFilterSection(titleRes.key) {
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
        private val titleRes: StringResource,
        val property: MutableStateFlow<Boolean>,
    ) : SortFilterSection(titleRes.key) {

        override fun showingPreview() = false

        override fun clear() {
            // This is persistent, can't be cleared
        }

        @Composable
        override fun Content(state: ExpandedState, showDivider: Boolean) {
            val enabled by property.collectAsState()
            SwitchRow(
                titleRes = titleRes,
                enabled = { enabled },
                onEnabledChanged = { property.value = it },
                showDivider = showDivider,
            )
        }
    }

    class TriStateBoolean(
        private val titleRes: StringResource,
        private val defaultEnabled: Boolean?,
    ) : SortFilterSection(titleRes.key) {
        var enabled by mutableStateOf(defaultEnabled)

        override fun showingPreview() = false

        override fun clear() {
            enabled = defaultEnabled
        }

        @Composable
        override fun Content(state: ExpandedState, showDivider: Boolean) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        enabled = when (enabled) {
                            true -> false
                            false -> null
                            null -> true
                        }
                    }
            ) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .weight(1f)
                )

                TriStateCheckbox(
                    state = when (enabled) {
                        true -> ToggleableState.On
                        false -> ToggleableState.Off
                        null -> ToggleableState.Indeterminate
                    },
                    onClick = {
                        enabled = when (enabled) {
                            true -> false
                            false -> null
                            null -> true
                        }
                    },
                    modifier = Modifier.padding(end = 16.dp)
                )
            }

            if (showDivider) {
                HorizontalDivider()
            }
        }
    }

    class Group<Child : SortFilterSection>(
        private val titleRes: StringResource,
        private val titleDropdownContentDescriptionRes: StringResource,
        children: List<Child> = emptyList(),
        private val onlyShowChildIfSingle: Boolean = false,
    ) : SortFilterSection(titleRes.key) {

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
                HorizontalDivider()
            }
        }
    }

    class Dropdown<T>(
        private val labelTextRes: StringResource,
        private val values: List<T>,
        private val valueToText: @Composable (T) -> String,
        private val property: MutableStateFlow<T>,
    ) : SortFilterSection(labelTextRes.key) {
        override fun showingPreview() = false
        override fun clear() = Unit

        @Composable
        override fun Content(state: ExpandedState, showDivider: Boolean) {
            var expanded by remember { mutableStateOf(false) }
            var selectedIndex by rememberSaveable {
                mutableIntStateOf(values.indexOf(property.value).coerceAtLeast(0))
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
            ) {
                Text(
                    text = stringResource(labelTextRes),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .heightIn(min = 32.dp)
                        .wrapContentHeight(Alignment.CenterVertically)
                        .align(Alignment.CenterVertically)
                )

                Box {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = valueToText(values[selectedIndex]),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )

                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        values.forEachIndexed { index, item ->
                            DropdownMenuItem(
                                text = { Text(valueToText(item)) },
                                onClick = {
                                    selectedIndex = index
                                    expanded = false
                                    property.value = values[index]
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
            }

            if (showDivider) {
                HorizontalDivider()
            }
        }
    }

    class Suggestions<Suggestion : Suggestions.Suggestion>(
        private val titleRes: StringResource,
        private val titleDropdownContentDescriptionRes: StringResource,
        private val suggestions: List<Suggestion>,
        private val onSuggestionClick: (Suggestion) -> Unit,
    ) : SortFilterSection(titleRes.key) {
        override fun showingPreview() = false

        override fun clear() {
            // No state to clear
        }

        @Composable
        override fun Content(state: ExpandedState, showDivider: Boolean) {
            SuggestionsSection(
                expanded = { state.expandedState[id] ?: false },
                onExpandedChange = { state.expandedState[id] = it },
                suggestions = { suggestions },
                onSuggestionClick = onSuggestionClick,
                suggestionToText = { it.text() },
                title = { stringResource(titleRes) },
                titleDropdownContentDescriptionRes = titleDropdownContentDescriptionRes,
                showDivider = showDivider,
            )
        }

        interface Suggestion {
            @Composable
            fun text(): String
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
        titleRes: StringResource,
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
            HorizontalDivider()
        }
    }

    abstract class Custom(id: String) : SortFilterSection(id)

    @Stable
    class ExpandedState {
        val expandedState = mutableStateMapOf<String, Boolean>()
    }
}
