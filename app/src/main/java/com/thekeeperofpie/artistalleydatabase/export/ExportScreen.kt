package com.thekeeperofpie.artistalleydatabase.export

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.thekeeperofpie.anichive.R
import com.thekeeperofpie.artistalleydatabase.ui.ChooseUriRow
import com.thekeeperofpie.artistalleydatabase.ui.LinearProgressWithIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.ButtonFooter
import com.thekeeperofpie.artistalleydatabase.utils_compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption

object ExportScreen {

    @Composable
    operator fun invoke(
        viewModel: ExportViewModel = hiltViewModel(),
        upIconOption: UpIconOption?,
    ) {

        Scaffolding(
            upIconOption = upIconOption,
            errorRes = { viewModel.errorResource },
            onErrorDismiss = { viewModel.errorResource = null },
        ) {
            Content(
                paddingValues = it,
                uriString = { viewModel.exportUriString.orEmpty() },
                onUriStringEdit = { viewModel.exportUriString = it },
                onContentUriSelected = { viewModel.exportUriString = it?.toString() },
                onClickExport = viewModel::onClickExport,
                exportProgress = { viewModel.exportProgress },
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Scaffolding(
        upIconOption: UpIconOption?,
        errorRes: () -> Pair<Int, Exception?>? = { null },
        onErrorDismiss: () -> Unit = { },
        content: @Composable (PaddingValues) -> Unit,
    ) {
        Scaffold(
            topBar = {
                AppBar(
                    text = stringResource(R.string.nav_drawer_export),
                    upIconOption = upIconOption,
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
            content(it)
        }
    }

    @Composable
    fun Content(
        paddingValues: PaddingValues,
        uriString: () -> String,
        onUriStringEdit: (String) -> Unit = {},
        onContentUriSelected: (Uri?) -> Unit = {},
        onClickExport: () -> Unit = {},
        exportProgress: () -> Float? = { 0.5f },
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

    @Composable
    private fun OptionRow(
        checked: () -> Boolean,
        @StringRes textRes: Int,
        onCheckedChange: (Boolean) -> Unit,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked()) }
        ) {
            Checkbox(
                checked = checked(),
                onCheckedChange = onCheckedChange,
            )

            Text(stringResource(textRes))
        }
    }
}

@Preview
@Composable
fun Preview() {
    ExportScreen.Scaffolding(
        upIconOption = UpIconOption.Back {},
        errorRes = { null },
        onErrorDismiss = { },
    ) {
        ExportScreen.Content(
            paddingValues = it,
            uriString = { "" },
            onUriStringEdit = {},
            onContentUriSelected = {},
            onClickExport = {},
            exportProgress = { 0.5f },
        )
    }
}
