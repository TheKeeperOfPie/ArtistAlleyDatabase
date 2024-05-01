package com.thekeeperofpie.artistalleydatabase.compose

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.paging.compose.LazyPagingItems
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR

@Composable
fun PagingErrorItem(
    lazyPagingItems: LazyPagingItems<*>,
    @StringRes errorTextRes: Int = UtilsStringR.error_loading,
    @StringRes retryButtonTextRes: Int = UtilsStringR.retry,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(errorTextRes))
        Button(onClick = lazyPagingItems::refresh) {
            Text(stringResource(id = retryButtonTextRes))
        }
    }
}
