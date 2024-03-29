package com.thekeeperofpie.artistalleydatabase.importing

import android.net.Uri
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
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import com.thekeeperofpie.artistalleydatabase.compose.ButtonFooter
import com.thekeeperofpie.artistalleydatabase.compose.ChooseUriRow
import com.thekeeperofpie.artistalleydatabase.compose.LinearProgressWithIndicator
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption

object ImportScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    operator fun invoke(
        viewModel: ImportViewModel = hiltViewModel(),
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
        errorRes: () -> Pair<Int, Exception?>?,
        onErrorDismiss: () -> Unit,
        content: @Composable (PaddingValues) -> Unit,
    ) {
        Scaffold(
            topBar = {
                AppBar(
                    text = stringResource(R.string.nav_drawer_import),
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
                    R.string.import_source,
                    uriString,
                    onUriStringEdit,
                    onClickChoose = { launcher.launch("*/*") }
                )

                OptionRow(
                    checked = dryRun,
                    textRes = R.string.import_dry_run,
                    onCheckedChange = onToggleDryRun,
                )

                OptionRow(
                    checked = replaceAll,
                    textRes = R.string.import_replace_all,
                    onCheckedChange = onToggleReplaceAll,
                )

                OptionRow(
                    checked = syncAfter,
                    textRes = R.string.import_sync_after,
                    onCheckedChange = onToggleSyncAfter,
                )

                LinearProgressWithIndicator(
                    text = stringResource(R.string.progress),
                    progress = importProgress()
                )
            }

            ButtonFooter(onClickImport, R.string.import_button)
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
