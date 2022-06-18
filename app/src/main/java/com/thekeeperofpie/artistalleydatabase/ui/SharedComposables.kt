package com.thekeeperofpie.artistalleydatabase.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun ColumnScope.ButtonFooter(onClick: () -> Unit, @StringRes textRes: Int) {
    TextButton(onClick = onClick, Modifier.align(Alignment.End)) {
        Text(
            stringResource(textRes),
            Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp)
        )
    }
}

@Composable
fun ButtonFooter(vararg pairs: Pair<Int, () -> Unit>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        pairs.reversed().forEach { (stringRes, onClick) ->
            TextButton(onClick = onClick) {
                Text(
                    stringResource(stringRes),
                    Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SnackbarErrorText(@StringRes errorRes: Int?, onErrorDismiss: () -> Unit) {
    if (errorRes != null) {
        val dismissState = rememberDismissState()
        if (dismissState.currentValue != DismissValue.Default) {
            LaunchedEffect(errorRes) {
                onErrorDismiss()
                dismissState.snapTo(DismissValue.Default)
            }
        }
        SwipeToDismiss(state = dismissState, background = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                contentColor = contentColorFor(MaterialTheme.colorScheme.background),
                modifier = Modifier.fillMaxSize()
            ) {

            }
        }) {
            Text(
                text = stringResource(id = errorRes),
                color = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.secondary)
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 12.dp,
                        bottom = 12.dp
                    )
            )
        }
    }
}