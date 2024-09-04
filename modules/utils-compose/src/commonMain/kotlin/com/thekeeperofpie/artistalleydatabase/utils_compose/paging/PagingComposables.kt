package com.thekeeperofpie.artistalleydatabase.utils_compose.paging

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import artistalleydatabase.modules.utils_compose.generated.resources.error_loading
import artistalleydatabase.modules.utils_compose.generated.resources.retry
import com.thekeeperofpie.artistalleydatabase.utils_compose.UtilsStrings
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun PagingErrorItem(
    lazyPagingItems: LazyPagingItems<*>,
    errorTextRes: StringResource = UtilsStrings.error_loading,
    retryButtonTextRes: StringResource = UtilsStrings.retry,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(errorTextRes))
        Button(onClick = lazyPagingItems::refresh) {
            Text(stringResource(retryButtonTextRes))
        }
    }
}
