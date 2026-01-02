package com.thekeeperofpie.artistalleydatabase.alley.edit.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph

object AdminScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyEditGraph,
        viewModel: AdminViewModel = viewModel {
            graph.adminViewModelFactory.create(createSavedStateHandle())
        },
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column {
                FilledTonalButton(onClick = viewModel::onClickCreate) {
                    Text("Create databases")
                }
            }
        }
    }
}
