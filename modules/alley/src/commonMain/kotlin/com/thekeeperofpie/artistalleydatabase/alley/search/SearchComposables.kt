@file:OptIn(ExperimentalMaterial3Api::class)

package com.thekeeperofpie.artistalleydatabase.alley.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_display_type_filter
import artistalleydatabase.modules.alley.generated.resources.alley_display_type_icon_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_generic_filter_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_search_clear_filters
import artistalleydatabase.modules.alley.generated.resources.alley_search_no_results
import artistalleydatabase.modules.alley.generated.resources.alley_search_results_filtered_out
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeader
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.alley.ui.DisplaySettings
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.filled.FilterList
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterSection2
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.toggle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SearchNoResults() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(Res.string.alley_search_no_results),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
internal fun SearchMoreResults(unfilteredCount: () -> Int, itemCount: () -> Int, onClick: () -> Unit) {
    val filteredOut = unfilteredCount() - itemCount()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            pluralStringResource(
                Res.plurals.alley_search_results_filtered_out,
                filteredOut,
                filteredOut,
            )
        )
        Button(onClick = onClick) {
            Text(stringResource(Res.string.alley_search_clear_filters))
        }
    }
}

@Composable
internal fun SearchDisplayTypeFilterSection(
    displaySettings: DisplaySettings,
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
) {
    FilterSection2(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        options = SearchDisplayType.entries,
        filterIn = { setOf(displaySettings.displayType) },
        filterNotIn = { emptySet() },
        title = { Text(stringResource(Res.string.alley_display_type_filter)) },
        titleDropdownContentDescriptionRes = Res.string.alley_display_type_icon_content_description,
        onOptionClick = {
            val [newFilterIn] = FilterSectionState.onClick(
                options = SearchDisplayType.entries,
                selectionMethod = SortFilterSectionState.Filter.SelectionMethod.EXACTLY_ONE,
                filterIn = setOf(displaySettings.displayType),
                filterNotIn = emptySet(),
                filter = it,
            )
            displaySettings.displayType = newFilterIn.single()
        },
        optionLabel = { Text(stringResource(it.label)) },
        optionLeadingIcon = { option, _ ->
            Icon(option.icon, stringResource(option.label))
        }
    )
}
