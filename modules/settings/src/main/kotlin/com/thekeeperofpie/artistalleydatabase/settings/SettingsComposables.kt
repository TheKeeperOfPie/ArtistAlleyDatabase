package com.thekeeperofpie.artistalleydatabase.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
internal fun ButtonRow(
    @StringRes labelTextRes: Int,
    @StringRes buttonTextRes: Int,
    onClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp)
    ) {
        Text(
            text = stringResource(labelTextRes),
            modifier = Modifier.weight(1f)
        )
        FilledTonalButton(onClick = onClick) {
            Text(text = stringResource(buttonTextRes))
        }
    }
}
