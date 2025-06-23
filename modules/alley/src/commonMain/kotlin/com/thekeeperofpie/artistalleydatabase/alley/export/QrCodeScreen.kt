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
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_close_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_export_download_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_export_notes_warning
import artistalleydatabase.modules.alley.generated.resources.alley_export_qr_code_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_export_qr_code_explanation
import artistalleydatabase.modules.alley.generated.resources.alley_export_qr_code_or_open_explanation
import artistalleydatabase.modules.alley.generated.resources.alley_export_qr_code_url_label
import com.thekeeperofpie.artistalleydatabase.alley.settings.ImportExportUtils
import com.thekeeperofpie.artistalleydatabase.alley.ui.QuestionAnswer
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import org.jetbrains.compose.resources.stringResource

internal object QrCodeScreen {

    @Composable
    operator fun invoke(data: () -> String?, onClickDownload: () -> Unit) {
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
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 16.dp, end = 16.dp, bottom = 12.dp)
                    ) {
                        OutlinedCard(Modifier.fillMaxWidth()) {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.error)) {
                                        append(
                                            "WARNING: There are reports that this breaks iOS " +
                                                    "devices permanently, use at your own risk! " +
                                                    "Android should work fine."
                                        )
                                    }
                                },
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        OutlinedCard(Modifier.fillMaxWidth()) {
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
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    val data = data()
                    val exportUrl = remember(data) { data?.let(ImportExportUtils::getImportUrl) }
                    if (exportUrl == null) {
                        CircularProgressIndicator()
                    } else {
                        Text(stringResource(Res.string.alley_export_qr_code_explanation))

                        Image(
                            painter = rememberQrCodePainter(exportUrl),
                            contentDescription = stringResource(Res.string.alley_export_qr_code_content_description),
                            modifier = Modifier.background(Color.White, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        )

                        Text(stringResource(Res.string.alley_export_qr_code_or_open_explanation))
                        OutlinedTextField(
                            value = exportUrl,
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
}
