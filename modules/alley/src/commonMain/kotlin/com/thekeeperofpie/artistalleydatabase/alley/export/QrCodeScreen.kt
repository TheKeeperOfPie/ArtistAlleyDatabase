package com.thekeeperofpie.artistalleydatabase.alley.export

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_close_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_export_notes_warning
import artistalleydatabase.modules.alley.generated.resources.alley_export_qr_code_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_export_qr_code_data_year_label
import artistalleydatabase.modules.alley.generated.resources.alley_export_qr_code_explanation
import artistalleydatabase.modules.alley.generated.resources.alley_export_qr_code_intro
import artistalleydatabase.modules.alley.generated.resources.alley_export_qr_code_json_download
import artistalleydatabase.modules.alley.generated.resources.alley_export_qr_code_or_open_explanation
import artistalleydatabase.modules.alley.generated.resources.alley_export_qr_code_type_json_file
import artistalleydatabase.modules.alley.generated.resources.alley_export_qr_code_type_json_include_metadata
import artistalleydatabase.modules.alley.generated.resources.alley_export_qr_code_type_label
import artistalleydatabase.modules.alley.generated.resources.alley_export_qr_code_type_qr_code
import artistalleydatabase.modules.alley.generated.resources.alley_export_qr_code_url_label
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.fullName
import com.thekeeperofpie.artistalleydatabase.alley.settings.ImportExportUtils
import com.thekeeperofpie.artistalleydatabase.alley.ui.InfiniteProgressIndicator
import com.thekeeperofpie.artistalleydatabase.alley.ui.QuestionAnswer
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.filled.Close
import com.thekeeperofpie.artistalleydatabase.icons.filled.Download
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.FilledTonalButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.StateUtils
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val ExportTypeSaver = StateUtils.jsonSaver<ExportType>()
private val DataYearSaver = StateUtils.jsonSaver<DataYear>()

@Composable
internal fun QrCodeScreen(
    graph: ArtistAlleyGraph,
    onNavigateBack: () -> Unit,
    viewModel: QrCodeViewModel = viewModel { graph.qrCodeViewModel() },
) {
    QrCodeScreen(
        exportPartialForYear = viewModel::exportPartialForYear,
        onNavigateBack = onNavigateBack,
        onClickDownload = viewModel::download,
    )
}

@Composable
private fun QrCodeScreen(
    exportPartialForYear: suspend (DataYear) -> String,
    onNavigateBack: () -> Unit,
    onClickDownload: (includeMetadata: Boolean) -> Unit,
) {
    OutlinedCard {
        Column {
            Row(modifier = Modifier.padding(end = 16.dp)) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(Res.string.alley_close_content_description),
                    )
                }
                Text(
                    text = stringResource(Res.string.alley_export_qr_code_intro),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                var exportType by rememberSaveable(stateSaver = ExportTypeSaver) {
                    mutableStateOf(ExportType.QR_CODE)
                }

                ExportTypeDropdown(
                    type = { exportType },
                    onTypeChange = { exportType = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )

                when (exportType) {
                    ExportType.QR_CODE -> QrCodeSection(exportPartialForYear)
                    ExportType.JSON_FILE -> JsonFileSection(onClickDownload)
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
private fun ExportTypeDropdown(
    type: () -> ExportType,
    onTypeChange: (ExportType) -> Unit,
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
            value = stringResource(type().textRes),
            onValueChange = {},
            label = { Text(stringResource(Res.string.alley_export_qr_code_type_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier.fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            ExportType.entries.forEach {
                DropdownMenuItem(
                    text = { Text(text = stringResource(it.textRes)) },
                    onClick = {
                        onTypeChange(it)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    modifier = Modifier.fillMaxWidth()
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

@Composable
private fun QrCodeSection(
    exportPartialForYear: suspend (DataYear) -> String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        var dataYear by rememberSaveable(stateSaver = DataYearSaver) {
            mutableStateOf(DataYear.LATEST)
        }

        DataYearDropdown(
            year = { dataYear },
            onYearChange = { dataYear = it },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(Res.string.alley_export_notes_warning),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        val exportUrl by produceState<String?>(null, dataYear) {
            value = ImportExportUtils.getImportUrl(exportPartialForYear(dataYear))
        }
        val exportUrlRead = exportUrl
        if (exportUrlRead == null) {
            InfiniteProgressIndicator()
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
    }
}

@Composable
private fun JsonFileSection(onClickDownload: (includeMetadata: Boolean) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        var includeMetadata by rememberSaveable { mutableStateOf(false) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                .clickable { includeMetadata = !includeMetadata }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Checkbox(
                checked = includeMetadata,
                onCheckedChange = { includeMetadata = it },
            )

            Text(stringResource(Res.string.alley_export_qr_code_type_json_include_metadata))
        }

        FilledTonalButton(
            icon = Icons.Default.Download,
            text = stringResource(Res.string.alley_export_qr_code_json_download),
            onClick = { onClickDownload(includeMetadata) },
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
        )
    }
}

private enum class ExportType(val textRes: StringResource) {
    QR_CODE(Res.string.alley_export_qr_code_type_qr_code),
    JSON_FILE(Res.string.alley_export_qr_code_type_json_file),
}

@Preview
@Composable
private fun QrCodeScreenPreview() {
    QrCodeScreen(
        exportPartialForYear = { "EXPORT_DATA" },
        onNavigateBack = {},
        onClickDownload = {},
    )
}

@Preview
@Composable
private fun JsonFileSectionPreview() {
    OutlinedCard {
        JsonFileSection(onClickDownload = {})
    }
}
