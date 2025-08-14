package com.thekeeperofpie.artistalleydatabase.alley.export

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_close_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_export_download_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_export_notes_warning
import artistalleydatabase.modules.alley.generated.resources.alley_export_qr_code_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_export_qr_code_data_year_label
import artistalleydatabase.modules.alley.generated.resources.alley_export_qr_code_explanation
import artistalleydatabase.modules.alley.generated.resources.alley_export_qr_code_or_open_explanation
import artistalleydatabase.modules.alley.generated.resources.alley_export_qr_code_url_label
import com.thekeeperofpie.artistalleydatabase.alley.fullName
import com.thekeeperofpie.artistalleydatabase.alley.settings.ImportExportUtils
import com.thekeeperofpie.artistalleydatabase.alley.ui.QuestionAnswer
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.StateUtils
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
internal object QrCodeScreen {

    private val DataYearSaver = StateUtils.jsonSaver<DataYear>()

    @Composable
    operator fun invoke(
        exportPartialForYear: suspend (DataYear) -> String,
        onClickDownload: () -> Unit,
    ) {
        OutlinedCard {
            Column {
                Row {
                    val navigationController = LocalNavigationController.current
                    IconButton(onClick = navigationController::popBackStack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(Res.string.alley_close_content_description),
                        )
                    }

                    OutlinedCard(
                        Modifier.fillMaxWidth().padding(top = 16.dp, end = 16.dp, bottom = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.alley_export_notes_warning),
                                modifier = Modifier.weight(1f)
                            )
                            FilledTonalIconButton(onClickDownload) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = stringResource(Res.string.alley_export_download_content_description),
                                )
                            }
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    var dataYear by rememberSaveable(stateSaver = DataYearSaver) {
                        mutableStateOf(
                            DataYear.LATEST
                        )
                    }

                    DataYearDropdown(
                        year = { dataYear },
                        onYearChange = { dataYear = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    val exportUrl by produceState<String?>(null, dataYear) {
                        value = ImportExportUtils.getImportUrl(exportPartialForYear(dataYear))
                    }
                    val exportUrlRead = exportUrl
                    if (exportUrlRead == null) {
                        CircularProgressIndicator()
                    } else {
                        Text(stringResource(Res.string.alley_export_qr_code_explanation))

                        Image(
                            painter = rememberQrCodePainter(exportUrlRead),
                            contentDescription = stringResource(Res.string.alley_export_qr_code_content_description),
                            modifier = Modifier.background(Color.White, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        )

                        Text(stringResource(Res.string.alley_export_qr_code_or_open_explanation))
                        OutlinedTextField(
                            value = exportUrlRead,
                            label = { Text(stringResource(Res.string.alley_export_qr_code_url_label)) },
                            onValueChange = {},
                            readOnly = true,
                        )
                    }

                    QuestionAnswer(
                        "Importing didn't work?",
                        "Make sure both this device and the other device are on the most up " +
                                "to date version of the site. This might require closing all tabs " +
                                "for the site and turning off anything that would block the update " +
                                "like VPN. \n\nIf that doesn't work, consider copy-pasting or using " +
                                "the file export + import instead.",
                    )
                }
            }
        }
    }

    @Composable
    private fun DataYearDropdown(
        year: () -> DataYear,
        onYearChange: (DataYear) -> Unit,
        modifier: Modifier,
    ) {
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = modifier,
        ) {
            TextField(
                readOnly = true,
                value = stringResource(year().fullName),
                onValueChange = {},
                label = { Text(stringResource(Res.string.alley_export_qr_code_data_year_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier.fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                DataYear.entries.forEach {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(it.fullName)) },
                        onClick = {
                            onYearChange(it)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun QrCodeScreenPreview() {
    QrCodeScreen(exportPartialForYear = { "EXPORT_DATA" }, onClickDownload = {})
}
