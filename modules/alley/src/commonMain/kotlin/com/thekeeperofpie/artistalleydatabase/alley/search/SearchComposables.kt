@file:OptIn(ExperimentalMaterial3Api::class)

package com.thekeeperofpie.artistalleydatabase.alley.search

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_generic_filter_content_description
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeader
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeaderState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BottomSheetFilterDataYearHeader(
    dataYearHeaderState: DataYearHeaderState,
    scaffoldState: BottomSheetScaffoldState?,
    onOpenExport: () -> Unit,
    onOpenChangelog: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    DataYearHeader(
        state = dataYearHeaderState,
        onOpenExport = onOpenExport,
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
