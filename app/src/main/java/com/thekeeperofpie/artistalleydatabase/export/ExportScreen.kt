package com.thekeeperofpie.artistalleydatabase.export

import android.net.Uri
import android.webkit.MimeTypeMap
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
import com.thekeeperofpie.artistalleydatabase.ui.AppBar
import com.thekeeperofpie.artistalleydatabase.ui.ButtonFooter
import com.thekeeperofpie.artistalleydatabase.ui.ChooseUriRow
import com.thekeeperofpie.artistalleydatabase.ui.LinearProgressWithIndicator
import com.thekeeperofpie.artistalleydatabase.ui.SnackbarErrorText

object ExportScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    operator fun invoke(
        onClickNav: () -> Unit = {},
        uriString: () -> String = { "" },
        onUriStringEdit: (String) -> Unit = {},
        onContentUriSelected: (Uri?) -> Unit = {},
        userReadable: () -> Boolean = { true },
        onToggleUserReadable: (Boolean) -> Unit = {},
        onClickExport: () -> Unit = {},
        exportProgress: () -> Float? = { 0.5f },
        errorRes: Pair<Int, Exception?>? = null,
        onErrorDismiss: () -> Unit = { },
    ) {
        Scaffold(
            topBar = {
                AppBar(
                    text = stringResource(R.string.nav_drawer_export),
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
                userReadable = userReadable,
                onToggleUserReadable = onToggleUserReadable,
                onClickExport = onClickExport,
                exportProgress = exportProgress,
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun Content(
        paddingValues: PaddingValues,
        uriString: () -> String,
        onUriStringEdit: (String) -> Unit = {},
        onContentUriSelected: (Uri?) -> Unit = {},
        userReadable: () -> Boolean,
        onToggleUserReadable: (Boolean) -> Unit = {},
        onClickExport: () -> Unit = {},
        exportProgress: () -> Float? = { 0.5f }
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
                    contract = ActivityResultContracts.CreateDocument(
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension(".zip") ?: "*/*"
                    ),
                    onResult = { onContentUriSelected(it) }
                )

                ChooseUriRow(
                    R.string.export_destination,
                    uriString(),
                    onUriStringEdit,
                    onClickChoose = {
                        launcher.launch("${ExportUtils.currentDateTimeFileName()}.zip")
                    }
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onToggleUserReadable(!userReadable())
                        }
                ) {
                    Checkbox(
                        checked = userReadable(),
                        onCheckedChange = null,
                        Modifier.padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 8.dp
                        )
                    )

                    Text(stringResource(R.string.export_user_readable))
                }

                LinearProgressWithIndicator(
                    text = stringResource(R.string.progress),
                    progress = exportProgress()
                )
            }

            ButtonFooter(onClickExport, R.string.export)
        }
    }
}

@Preview
@Composable
fun Preview() {
    ExportScreen()
}