package com.thekeeperofpie.artistalleydatabase.utils_compose.filter

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.OnChangeEffect
import com.thekeeperofpie.artistalleydatabase.utils_compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortAndFilterComposables.SortFilterHeaderText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.reflect.KClass

@Stable
class SortFilterState<FilterParams>(
    val sections: StateFlow<List<SortFilterSectionState>>,
    val filterParams: StateFlow<FilterParams>,
    val collapseOnClose: StateFlow<Boolean>,
) {
    val expanded = SortFilterSection.ExpandedState()

    @Composable
    fun ImmediateScrollResetEffect(lazyGridState: LazyGridState) {
        OnChangeEffect(currentValue = filterParams.collectAsStateWithLifecycle().value) {
            lazyGridState.scrollToItem(0)
        }
    }
}

@Stable
sealed class SortFilterSectionState(val id: String) {

    abstract fun clear()

    @Composable
    abstract fun isDefault(): Boolean

    @Composable
    abstract fun Content(state: SortFilterSection.ExpandedState, showDivider: Boolean)

    @Stable
    class Sort<SortType : SortOption>(
        private val enumClass: KClass<SortType>,
        val headerText: StringResource,
        private val defaultSort: SortType,
        private val sortAscending: MutableStateFlow<Boolean>?,
        private val sortOption: MutableStateFlow<SortType>,
        var sortOptions: MutableStateFlow<List<SortType>> =
            MutableStateFlow(enumClass.java.enumConstants?.toList().orEmpty()),
    ) : SortFilterSectionState(headerText.key) {

        override fun clear() {
            sortOption.value = defaultSort
        }

        @Composable
        override fun isDefault() =
            sortOption.collectAsStateWithLifecycle().value == defaultSort

        @Composable
        override fun Content(state: SortFilterSection.ExpandedState, showDivider: Boolean) {
            val sortOptions by sortOptions.collectAsStateWithLifecycle()
            var sortOption by sortOption.collectAsMutableStateWithLifecycle()
            val sortAscending = sortAscending?.collectAsMutableStateWithLifecycle()
            SortAndFilterComposables.SortSection(
                headerTextRes = headerText,
                expanded = { state.expandedState[id] == true },
                onExpandedChange = { state.expandedState[id] = it },
                sortOptions = { sortOptions },
                sortOptionsEnabled = { setOf(sortOption) },
                onSortClick = {
                    sortOption = if (sortOption == it) {
                        sortOptions[(sortOptions.indexOf(it) + 1) % sortOptions.size]
                    } else {
                        it
                    }
                },
                // TODO: Disable sortAscending at this level instead of the enum?
                sortAscending = { sortAscending?.value == true },
                onSortAscendingChange = {
                    if (sortAscending != null) {
                        sortAscending.value = it
                    }
                },
                clickable = sortOptions.size > 1,
                showDivider = showDivider,
            )
        }
    }

    @Stable
    class Filter<FilterType>(
        id: String,
        private val title: @Composable () -> String,
        private val titleDropdownContentDescription: StringResource,
        private val includeExcludeIconContentDescription: StringResource,
        private var options: StateFlow<List<FilterType>>,
        private val lockedFilterIn: Set<FilterType> = emptySet(),
        private val filterIn: MutableStateFlow<Set<FilterType>>,
        private val filterNotIn: MutableStateFlow<Set<FilterType>>,
        private val valueToText: @Composable (FilterType) -> String,
        private val valueToImage: (@Composable (FilterType, enabled: Boolean?) -> String?)? = null,
        var selectionMethod: SelectionMethod = SelectionMethod.ALLOW_EXCLUDE,
    ) : SortFilterSectionState(id) {
        constructor(
            title: StringResource,
            titleDropdownContentDescription: StringResource,
            includeExcludeIconContentDescription: StringResource,
            options: StateFlow<List<FilterType>>,
            lockedFilterIn: Set<FilterType> = emptySet(),
            filterIn: MutableStateFlow<Set<FilterType>>,
            filterNotIn: MutableStateFlow<Set<FilterType>>,
            valueToText: @Composable (FilterType) -> String,
            valueToImage: (@Composable (FilterType, enabled: Boolean?) -> String?)? = null,
            selectionMethod: SelectionMethod = SelectionMethod.ALLOW_EXCLUDE,
        ) : this(
            title = { stringResource(title) },
            id = title.key,
            titleDropdownContentDescription = titleDropdownContentDescription,
            includeExcludeIconContentDescription = includeExcludeIconContentDescription,
            options = options,
            lockedFilterIn = lockedFilterIn,
            filterIn = filterIn,
            filterNotIn = filterNotIn,
            valueToText = valueToText,
            valueToImage = valueToImage,
            selectionMethod = selectionMethod,
        )

        enum class SelectionMethod {
            AT_MOST_ONE,
            ONLY_INCLUDE,
            ALLOW_EXCLUDE,
        }

        private var locked by mutableStateOf(false)

        override fun clear() {
            filterIn.value = lockedFilterIn
            filterNotIn.value = emptySet()
        }

        @Composable
        override fun isDefault() = filterIn.collectAsStateWithLifecycle().value.isEmpty()
                && filterNotIn.collectAsStateWithLifecycle().value.isEmpty()

        @Composable
        override fun Content(state: SortFilterSection.ExpandedState, showDivider: Boolean) {
            val options by options.collectAsStateWithLifecycle()
            var filterIn by filterIn.collectAsMutableStateWithLifecycle()
            var filterNotIn by filterNotIn.collectAsMutableStateWithLifecycle()
            FilterSection(
                expanded = { state.expandedState[id] == true },
                onExpandedChange = { state.expandedState[id] = it },
                options = options,
                disabledOptions = lockedFilterIn,
                filterIn = filterIn,
                filterNotIn = filterNotIn,
                onFilterClick = {
                    if (lockedFilterIn.contains(it)) return@FilterSection
                    when (selectionMethod) {
                        SelectionMethod.AT_MOST_ONE -> {
                            if (lockedFilterIn.isNotEmpty()) return@FilterSection
                            filterIn = if (it in filterIn) emptySet() else setOf(it)
                            filterNotIn = emptySet()
                        }
                        SelectionMethod.ONLY_INCLUDE -> {
                            if (filterIn.contains(it)) {
                                filterIn -= it
                            } else {
                                filterIn += it
                            }
                        }
                        SelectionMethod.ALLOW_EXCLUDE -> {
                            if (filterIn.contains(it)) {
                                filterIn -= it
                                filterNotIn += it
                            } else if (filterNotIn.contains(it)) {
                                filterNotIn -= it
                            } else {
                                filterIn += it
                            }
                        }
                    }
                },
                title = title,
                titleDropdownContentDescriptionRes = titleDropdownContentDescription,
                valueToText = valueToText,
                valueToImage = valueToImage,
                iconContentDescriptionRes = includeExcludeIconContentDescription,
                locked = locked,
                showDivider = showDivider,
                showIcons = selectionMethod == SelectionMethod.ALLOW_EXCLUDE,
            )
        }
    }

    @Stable
    class Range(
        private val title: StringResource,
        private val titleDropdownContentDescription: StringResource,
        val initialData: RangeData,
        val data: MutableStateFlow<RangeData>,
        // TODO: Can this be de-duped with hardMax?
        private val unboundedMax: Boolean = false,
    ) : SortFilterSectionState(title.key) {
        override fun clear() {
            data.value = initialData
        }

        @Composable
        override fun isDefault() = data.collectAsStateWithLifecycle().value == initialData

        @Composable
        override fun Content(state: SortFilterSection.ExpandedState, showDivider: Boolean) {
            var data by data.collectAsMutableStateWithLifecycle()
            RangeDataFilterSection(
                expanded = { state.expandedState[id] == true },
                onExpandedChange = { state.expandedState[id] = it },
                range = { data },
                onRangeChange = { start, end ->
                    data = data.copy(
                        startString = start,
                        endString = end.takeIf { !unboundedMax || it != "${data.maxValue}" }
                            .orEmpty()
                    )
                },
                titleRes = title,
                titleDropdownContentDescriptionRes = titleDropdownContentDescription,
                showDivider = showDivider,
            )
        }
    }

    @Stable
    class Group<Child : SortFilterSectionState>(
        private val title: StringResource,
        private val titleDropdownContentDescription: StringResource,
        val children: StateFlow<List<Child>>,
        private val onlyShowChildIfSingle: Boolean = false,
    ) : SortFilterSectionState(title.key) {

        override fun clear() {
            children.value.forEach(SortFilterSectionState::clear)
        }

        @Composable
        override fun isDefault() =
            children.collectAsStateWithLifecycle().value.all { it.isDefault() }

        @Composable
        override fun Content(state: SortFilterSection.ExpandedState, showDivider: Boolean) {
            val children by children.collectAsStateWithLifecycle()
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
                    title,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                )

                TrailingDropdownIconButton(
                    expanded = expanded,
                    contentDescription = stringResource(titleDropdownContentDescription),
                    onClick = { state.expandedState[id] = !expanded },
                )
            }

            if (expanded || !isDefault()) {
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

    @Stable
    class SwitchBySetting(
        private val title: StringResource,
        val property: MutableStateFlow<Boolean>,
    ) : SortFilterSectionState(title.key) {

        @Composable
        override fun isDefault() = true

        override fun clear() {
            // This is persistent, can't be cleared
        }

        @Composable
        override fun Content(state: SortFilterSection.ExpandedState, showDivider: Boolean) {
            var enabled by property.collectAsMutableStateWithLifecycle()
            SortAndFilterComposables.SwitchRow(
                title = title,
                enabled = { enabled },
                onEnabledChanged = { enabled = it },
                showDivider = showDivider,
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Stable
    class Dropdown<T>(
        private val labelTextRes: StringResource,
        private val values: List<T>,
        private val valueToText: @Composable (T) -> String,
        private val property: MutableStateFlow<T>,
    ) : SortFilterSectionState(labelTextRes.key) {
        override fun clear() = Unit

        @Composable
        override fun isDefault() = true

        @Composable
        override fun Content(state: SortFilterSection.ExpandedState, showDivider: Boolean) {
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

    @Stable
    class Suggestions<Suggestion : Suggestions.Suggestion>(
        private val title: StringResource,
        private val titleDropdownContentDescription: StringResource,
        private val suggestions: List<Suggestion>,
        private val onSuggestionClick: (Suggestion) -> Unit,
    ) : SortFilterSectionState(title.key) {
        override fun clear() {
            // No state to clear
        }

        @Composable
        override fun isDefault() = true

        @Composable
        override fun Content(state: SortFilterSection.ExpandedState, showDivider: Boolean) {
            SuggestionsSection(
                expanded = { state.expandedState[id] == true },
                onExpandedChange = { state.expandedState[id] = it },
                suggestions = { suggestions },
                onSuggestionClick = onSuggestionClick,
                suggestionToText = { it.text() },
                title = { stringResource(title) },
                titleDropdownContentDescriptionRes = titleDropdownContentDescription,
                showDivider = showDivider,
            )
        }

        interface Suggestion {
            @Composable
            fun text(): String
        }
    }

    class Switch(
        private val title: StringResource,
        private val defaultEnabled: Boolean,
        private val enabled: MutableStateFlow<Boolean>,
    ) : SortFilterSectionState(title.key) {
        override fun clear() {
            enabled.value = defaultEnabled
        }

        @Composable
        override fun isDefault() = enabled.collectAsStateWithLifecycle().value == defaultEnabled

        @Composable
        override fun Content(state: SortFilterSection.ExpandedState, showDivider: Boolean) {
            var enabled by enabled.collectAsMutableStateWithLifecycle()
            SortAndFilterComposables.SwitchRow(
                title = title,
                enabled = { enabled },
                onEnabledChanged = { enabled = it },
                showDivider = showDivider,
            )
        }
    }

    class TriStateBoolean(
        private val titleRes: StringResource,
        private val defaultEnabled: Boolean?,
        private val enabled: MutableStateFlow<Boolean?>,
    ) : SortFilterSectionState(titleRes.key) {
        @Composable
        override fun isDefault() = enabled.collectAsStateWithLifecycle().value == defaultEnabled

        override fun clear() {
            enabled.value = defaultEnabled
        }

        @Composable
        override fun Content(state: SortFilterSection.ExpandedState, showDivider: Boolean) {
            var enabled by enabled.collectAsMutableStateWithLifecycle()
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

    @Stable
    abstract class Custom(id: String) : SortFilterSectionState(id)
}
