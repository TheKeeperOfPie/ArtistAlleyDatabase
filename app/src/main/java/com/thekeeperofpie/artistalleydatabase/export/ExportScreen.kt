package com.thekeeperofpie.artistalleydatabase.export

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.ui.ButtonFooter
import com.thekeeperofpie.artistalleydatabase.ui.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.ui.theme.ArtistAlleyDatabaseTheme

object ExportScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    operator fun invoke(
        uriString: String = "",
        onUriStringEdit: (String) -> Unit = {},
        onContentUriSelected: (Uri?) -> Unit = {},
        onClickExport: () -> Unit = {},
        errorRes: Pair<Int, Exception?>? = null,
        onErrorDismiss: () -> Unit = { },
    ) {
        ArtistAlleyDatabaseTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Scaffold(snackbarHost = {
                    SnackbarErrorText(errorRes?.first, onErrorDismiss = onErrorDismiss)
                }) {
                    Content(it, uriString, onUriStringEdit, onContentUriSelected, onClickExport)
                }
            }
        }
    }

    @Composable
    private fun Content(
        paddingValues: PaddingValues,
        uriString: String,
        onUriStringEdit: (String) -> Unit = {},
        onContentUriSelected: (Uri?) -> Unit = {},
        onClickExport: () -> Unit = {},
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    OutlinedTextField(
                        value = uriString,
                        onValueChange = onUriStringEdit,
                        label = { Text(stringResource(R.string.export_destination)) },
                        placeholder = { Text(stringResource(R.string.content_uri_placeholder)) },
                        modifier = Modifier
                            .weight(1f, true)
                            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp)
                    )

                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.CreateDocument(),
                        onResult = { onContentUriSelected(it) }
                    )

                    IconButton(
                        onClick = {
                            launcher.launch("${ExportUtils.currentDateTimeFileName()}.zip")
                        },
                        Modifier.heightIn(min = 48.dp)
                            .fillMaxHeight()
                            .padding(end = 16.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(
                                R.string.select_export_destination_content_description
                            )
                        )
                    }
                }
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