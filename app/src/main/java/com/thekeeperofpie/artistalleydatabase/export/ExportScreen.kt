package com.thekeeperofpie.artistalleydatabase.export

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import com.thekeeperofpie.artistalleydatabase.compose.ButtonFooter
import com.thekeeperofpie.artistalleydatabase.compose.ChooseUriRow
import com.thekeeperofpie.artistalleydatabase.compose.LinearProgressWithIndicator
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText

object ExportScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    operator fun invoke(
        onClickNav: () -> Unit = {},
        uriString: () -> String = { "" },
        onUriStringEdit: (String) -> Unit = {},
        onContentUriSelected: (Uri?) -> Unit = {},
        onClickExport: () -> Unit = {},
        exportProgress: () -> Float? = { 0.5f },
        errorRes: () -> Pair<Int, Exception?>? = { null },
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
                SnackbarErrorText(
                    errorRes()?.first,
                    errorRes()?.second,
                    onErrorDismiss = onErrorDismiss
                )
            },
        ) {
            Content(
                paddingValues = it,
                uriString = uriString,
                onUriStringEdit = onUriStringEdit,
                onContentUriSelected = onContentUriSelected,
                onClickExport = onClickExport,
                exportProgress = exportProgress,
            )
        }
    }

    @Composable
    private fun Content(
        paddingValues: PaddingValues,
        uriString: () -> String,
        onUriStringEdit: (String) -> Unit = {},
        onContentUriSelected: (Uri?) -> Unit = {},
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