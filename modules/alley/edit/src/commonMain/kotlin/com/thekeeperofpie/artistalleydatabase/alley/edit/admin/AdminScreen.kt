package com.thekeeperofpie.artistalleydatabase.alley.edit.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.form.ArtistFormAccessKey
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography
import com.thekeeperofpie.artistalleydatabase.utils_compose.ThemeAwareElevatedCard
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher

object AdminScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyEditGraph,
        onDebugOpenForm: (formLink: String) -> Unit,
        viewModel: AdminViewModel = viewModel { graph.adminViewModel },
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ThemeAwareElevatedCard {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        FilledTonalButton(onClick = viewModel::onClickCreate) {
                            Text("Create databases")
                        }

                        val fakeArtistFormLink by viewModel.fakeArtistFormLink.collectAsStateWithLifecycle()
                        FilledTonalButton(onClick = {
                            fakeArtistFormLink?.let {
                                ArtistFormAccessKey.setKey(it.substringAfter("?${AlleyCryptography.ACCESS_KEY_PARAM}="))
                                onDebugOpenForm(it)
                            }
                        }) {
                            Text("Open fake artist form")
                        }

                        FilledTonalButton(onClick = viewModel::onClickClearFakeArtistData) {
                            Text("Clear fake artist data")
                        }
                    }
                }
                ThemeAwareElevatedCard {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        val remoteSyncFileLauncher =
                            rememberFilePickerLauncher(FileKitType.File("html")) {
                                viewModel.onRemoteSyncFileChosen(it)
                            }

                        FilledTonalButton(onClick = { remoteSyncFileLauncher.launch() }) {
                            Text("Load remote HTML")
                        }

                        val diff by viewModel.remoteSyncDiff.collectAsStateWithLifecycle()
                        if (diff.isNotEmpty()) {
                            FilledTonalButton(onClick = { viewModel.submitRemoteDiff(diff) }) {
                                Text("Submit ${diff.size} entries")
                            }
                        }
                    }
                }
            }
        }
    }
}
