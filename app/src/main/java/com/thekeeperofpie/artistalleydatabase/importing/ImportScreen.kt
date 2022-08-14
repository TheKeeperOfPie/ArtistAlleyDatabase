package com.thekeeperofpie.artistalleydatabase.importing

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.thekeeperofpie.artistalleydatabase.compose.ButtonFooter
import com.thekeeperofpie.artistalleydatabase.compose.ChooseUriRow
import com.thekeeperofpie.artistalleydatabase.compose.LinearProgressWithIndicator
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText

object ImportScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    operator fun invoke(
        onClickNav: () -> Unit = {},
        uriString: String = "",
        onUriStringEdit: (String) -> Unit = {},
        onContentUriSelected: (Uri?) -> Unit = {},
        dryRun: () -> Boolean = { false },
        onToggleDryRun: (Boolean) -> Unit = {},
        replaceAll: () -> Boolean = { false },
        onToggleReplaceAll: (Boolean) -> Unit = {},
        onClickImport: () -> Unit = {},
        importProgress: () -> Float? = { 0.5f },
        errorRes: Pair<Int, Exception?>? = null,
        onErrorDismiss: () -> Unit = { },
    ) {
        Scaffold(
            topBar = {
                AppBar(
                    text = stringResource(R.string.nav_drawer_import),
                    onClickNav = onClickNav
                )
            },
            snackbarHost = {
                SnackbarErrorText(errorRes?.first, onErrorDismiss = onErrorDismiss)
            },
        ) {
            Content(
                paddingValues = it,
                uriString = uriString,
                onUriStringEdit = onUriStringEdit,
                onContentUriSelected = onContentUriSelected,
                dryRun = dryRun,
                onToggleDryRun = onToggleDryRun,
                replaceAll = replaceAll,
                onToggleReplaceAll = onToggleReplaceAll,
                onClickImport = onClickImport,
                importProgress = importProgress,
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun Content(
        paddingValues: PaddingValues,
        uriString: String,
        onUriStringEdit: (String) -> Unit = {},
        onContentUriSelected: (Uri?) -> Unit = {},
        dryRun: () -> Boolean = { false },
        onToggleDryRun: (Boolean) -> Unit = {},
        replaceAll: () -> Boolean = { false },
        onToggleReplaceAll: (Boolean) -> Unit = {},
        onClickImport: () -> Unit = {},
        importProgress: () -> Float? = { 0.5f }
    ) {
        Column(
            Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Column(
                Modifier
                    .weight(1f, true)
                    .verticalScroll(rememberScrollState())
            ) {
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent(),
                    onResult = { onContentUriSelected(it) }
                )
                ChooseUriRow(
                    R.string.import_source,
                    uriString,
                    onUriStringEdit,
                    onClickChoose = { launcher.launch("*/*") }
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onToggleDryRun(!dryRun())
                        }
                ) {
                    Checkbox(
                        checked = dryRun(),
                        onCheckedChange = null,
                        Modifier.padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 8.dp
                        )
                    )

                    Text(stringResource(R.string.import_dry_run))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onToggleReplaceAll(!replaceAll())
                        }
                ) {
                    Checkbox(
                        checked = replaceAll(),
                        onCheckedChange = null,
                        Modifier.padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 8.dp
                        )
                    )

                    Text(stringResource(R.string.import_replace_all))
                }

                LinearProgressWithIndicator(
                    text = stringResource(R.string.progress),
                    progress = importProgress()
                )
            }

            ButtonFooter(onClickImport, R.string.import_button)
        }
    }
}

@Preview
@Composable
fun Preview() {
    ImportScreen()
}