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
import artistalleydatabase.app.generated.resources.Res
import artistalleydatabase.app.generated.resources.import_button
import artistalleydatabase.app.generated.resources.import_dry_run
import artistalleydatabase.app.generated.resources.import_replace_all
import artistalleydatabase.app.generated.resources.import_source
import artistalleydatabase.app.generated.resources.import_sync_after
import artistalleydatabase.app.generated.resources.nav_drawer_import
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

object ImportScreen {

    @Composable
    operator fun invoke(
        viewModel: ImportViewModel,
        upIconOption: UpIconOption?,
    ) {
        Scaffolding(
            upIconOption = upIconOption,
            errorRes = { viewModel.errorResource },
            onErrorDismiss = { viewModel.errorResource = null },
        ) {
            Content(
                paddingValues = it,
                uriString = viewModel.importUriString.orEmpty(),
                onUriStringEdit = { viewModel.importUriString = it },
                onContentUriSelected = { viewModel.importUriString = it?.toString() },
                dryRun = { viewModel.dryRun },
                onToggleDryRun = { viewModel.dryRun = !viewModel.dryRun },
                replaceAll = { viewModel.replaceAll },
                onToggleReplaceAll = { viewModel.replaceAll = !viewModel.replaceAll },
                syncAfter = { viewModel.syncAfter },
                onToggleSyncAfter = { viewModel.syncAfter = !viewModel.syncAfter },
                onClickImport = viewModel::onClickImport,
                importProgress = { viewModel.importProgress },
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Scaffolding(
        upIconOption: UpIconOption?,
        errorRes: () -> Pair<StringResource, Exception?>?,
        onErrorDismiss: () -> Unit,
        content: @Composable (PaddingValues) -> Unit,
    ) {
        Scaffold(
            topBar = {
                AppBar(
                    text = stringResource(Res.string.nav_drawer_import),
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
        uriString: String,
        onUriStringEdit: (String) -> Unit = {},
        onContentUriSelected: (Uri?) -> Unit = {},
        dryRun: () -> Boolean = { false },
        onToggleDryRun: (Boolean) -> Unit = {},
        replaceAll: () -> Boolean = { false },
        onToggleReplaceAll: (Boolean) -> Unit = {},
        syncAfter: () -> Boolean = { false },
        onToggleSyncAfter: (Boolean) -> Unit = {},
        onClickImport: () -> Unit = {},
        importProgress: () -> Float? = { 0.5f },
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
                    Res.string.import_source,
                    uriString,
                    onUriStringEdit,
                    onClickChoose = { launcher.launch("*/*") }
                )

                OptionRow(
                    checked = dryRun,
                    textRes = Res.string.import_dry_run,
                    onCheckedChange = onToggleDryRun,
                )

                OptionRow(
                    checked = replaceAll,
                    textRes = Res.string.import_replace_all,
                    onCheckedChange = onToggleReplaceAll,
                )

                OptionRow(
                    checked = syncAfter,
                    textRes = Res.string.import_sync_after,
                    onCheckedChange = onToggleSyncAfter,
                )

                LinearProgressWithIndicator(
                    text = stringResource(Res.string.progress),
                    progress = importProgress()
                )
            }

            ButtonFooter(onClickImport, Res.string.import_button)
        }
    }

    @Composable
    private fun OptionRow(
        checked: () -> Boolean,
        textRes: StringResource,
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
    ImportScreen.Scaffolding(
        upIconOption = UpIconOption.Back {},
        errorRes = { null },
        onErrorDismiss = { },
    ) {
        ImportScreen.Content(
            paddingValues = it,
            uriString = "",
            onUriStringEdit = { },
            onContentUriSelected = { },
            dryRun = { true },
            onToggleDryRun = { },
            replaceAll = { true },
            onToggleReplaceAll = { },
            syncAfter = { false },
            onToggleSyncAfter = { },
            onClickImport = { },
            importProgress = { 0.5f },
        )
    }
}
