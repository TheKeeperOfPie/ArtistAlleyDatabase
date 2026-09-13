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
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_generic_filter_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_search_clear_filters
import artistalleydatabase.modules.alley.generated.resources.alley_search_no_results
import artistalleydatabase.modules.alley.generated.resources.alley_search_results_filtered_out
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeader
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.filled.FilterList
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BottomSheetFilterDataYearHeader(
    dataYearHeaderState: DataYearHeaderState,
    scaffoldState: BottomSheetScaffoldState?,
    onOpenChangelog: (DataYear) -> Unit,
    onOpenSettings: () -> Unit,
) {
    DataYearHeader(
        state = dataYearHeaderState,
        onOpenChangelog = onOpenChangelog,
        onOpenSettings = onOpenSettings,
    ) {
        val scope = rememberCoroutineScope()
        if (scaffoldState != null) {
            IconButton(onClick = {
                if (scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded) {
                    scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                } else {
                    scope.launch { scaffoldState.bottomSheetState.expand() }
                }
            }) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = stringResource(Res.string.alley_generic_filter_content_description),
                )
            }
        }
    }
}

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
