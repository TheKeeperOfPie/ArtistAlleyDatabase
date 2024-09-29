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
import artistalleydatabase.app.generated.resources.Res
import artistalleydatabase.app.generated.resources.export
import artistalleydatabase.app.generated.resources.export_destination
import artistalleydatabase.app.generated.resources.nav_drawer_export
import artistalleydatabase.app.generated.resources.progress
import com.thekeeperofpie.artistalleydatabase.ui.ChooseUriRow
import com.thekeeperofpie.artistalleydatabase.ui.LinearProgressWithIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.ButtonFooter
import com.thekeeperofpie.artistalleydatabase.utils_compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

object ExportScreen {

    @Composable
    operator fun invoke(
        viewModel: ExportViewModel,
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
        errorRes: () -> Pair<StringResource, Exception?>? = { null },
        onErrorDismiss: () -> Unit = { },
        content: @Composable (PaddingValues) -> Unit,
    ) {
        Scaffold(
            topBar = {
                AppBar(
                    text = stringResource(Res.string.nav_drawer_export),
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
                    Res.string.export_destination,
                    uriString(),
                    onUriStringEdit,
                    onClickChoose = {
                        launcher.launch("${ExportUtils.currentDateTimeFileName()}.zip")
                    }
                )

                LinearProgressWithIndicator(
                    text = stringResource(Res.string.progress),
                    progress = exportProgress()
                )
            }

            ButtonFooter(onClickExport, Res.string.export)
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
