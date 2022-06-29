package com.thekeeperofpie.artistalleydatabase.export

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.ui.ButtonFooter
import com.thekeeperofpie.artistalleydatabase.ui.SnackbarErrorText

object ExportScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    operator fun invoke(
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
        Scaffold(snackbarHost = {
            SnackbarErrorText(errorRes?.first, onErrorDismiss = onErrorDismiss)
        }) {
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
                    contract = ActivityResultContracts.CreateDocument(),
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

                val progress = exportProgress()
                AnimatedVisibility(
                    visible = progress != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Column(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.export_progress),
                            style = MaterialTheme.typography.labelLarge
                        )

                        LinearProgressIndicator(
                            progress = progress ?: 0f,
                            modifier = Modifier.fillMaxWidth()
                                .padding(top = 10.dp, bottom = 10.dp)
                        )
                    }
                }
            }

            ButtonFooter(onClickExport, R.string.export)
        }
    }

    @Composable
    fun ChooseUriRow(
        @StringRes label: Int,
        uriString: String,
        onUriStringEdit: (String) -> Unit = {},
        onClickChoose: () -> Unit = {},
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            OutlinedTextField(
                value = uriString,
                onValueChange = onUriStringEdit,
                readOnly = true,
                label = { Text(stringResource(label)) },
                modifier = Modifier
                    .weight(1f, true)
                    .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp)
                    .clickable(false) {}
            )

            IconButton(
                onClick = onClickChoose,
                Modifier
                    .heightIn(min = 48.dp)
                    .padding(end = 16.dp)
                    .background(Color.LightGray, Shapes.Full)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(
                        R.string.select_export_destination_content_description
                    ),
                )
            }
        }
    }
}

@Preview
@Composable
fun Preview() {
    ExportScreen()
}