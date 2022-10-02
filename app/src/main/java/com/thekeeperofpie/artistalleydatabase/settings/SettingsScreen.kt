package com.thekeeperofpie.artistalleydatabase.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText

object SettingsScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    operator fun invoke(
        onClickNav: () -> Unit = {},
        errorRes: () -> Pair<Int, Exception?>? = { null },
        onErrorDismiss: () -> Unit = {},
        onClickAniListClear: () -> Unit = {},
        onClickVgmdbClear: () -> Unit = {},
    ) {
        Scaffold(
            topBar = {
                AppBar(
                    text = stringResource(R.string.nav_drawer_settings),
                    onClickNav = onClickNav
                )
            },
            snackbarHost = {
                SnackbarErrorText(errorRes()?.first, onErrorDismiss = onErrorDismiss)
            },
        ) {
            Column(Modifier.padding(it)) {
                ButtonRow(
                    title = R.string.settings_clear_aniList_cache,
                    buttonText = R.string.clear,
                    onClick = onClickAniListClear
                )

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.inverseOnSurface)
                )

                ButtonRow(
                    title = R.string.settings_clear_vgmdb_cache,
                    buttonText = R.string.clear,
                    onClick = onClickVgmdbClear
                )
            }
        }
    }

    @Composable
    fun ButtonRow(@StringRes title: Int, @StringRes buttonText: Int, onClick: () -> Unit) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp)
        ) {
            Text(
                text = stringResource(title),
                Modifier
                    .weight(1f)
            )
            FilledTonalButton(onClick = onClick) { Text(text = stringResource(buttonText)) }
        }
    }
}

@Preview
@Composable
fun Preview() {
    SettingsScreen()
}