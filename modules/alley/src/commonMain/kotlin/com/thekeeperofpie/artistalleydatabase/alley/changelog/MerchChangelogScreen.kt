package com.thekeeperofpie.artistalleydatabase.alley.changelog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_changelog_title
import com.composables.core.ScrollArea
import com.composables.core.rememberScrollAreaState
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.tags.MerchChips
import com.thekeeperofpie.artistalleydatabase.alley.ui.PrimaryVerticalScrollbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

object MerchChangelogScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyGraph,
        onClickBack: () -> Unit,
        onClickMerch: (String) -> Unit,
        viewModel: MerchChangelogViewModel = viewModel {
            graph.merchChangelogViewModelFactory()
        },
    ) {
        val changes by viewModel.changes.collectAsStateWithLifecycle()
        MerchChangelogScreen(
            changes = { changes },
            onClickBack = onClickBack,
            onClickMerch = onClickMerch,
        )
    }

    @Composable
    operator fun invoke(
        changes: () -> List<DayChange>,
        onClickBack: () -> Unit,
        onClickMerch: (String) -> Unit,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = { ArrowBackIconButton(onClickBack) },
                    title = { Text(stringResource(Res.string.alley_changelog_title)) },
                )
            },
        ) {
            val listState = rememberLazyListState()
            val scrollAreaState = rememberScrollAreaState(listState)
            ScrollArea(state = scrollAreaState, modifier = Modifier.fillMaxSize().padding(it)) {
                Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(bottom = 200.dp),
                        modifier = Modifier.widthIn(max = 960.dp)
                    ) {
                        changes().forEach {
                            item(key = listOf("header", it.date), contentType = "header") {
                                ChangelogDayHeader(it.date)
                            }

                            item(key = listOf("merchIds", it.date), contentType = "merchIds") {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
                                    MerchChips(merch = it.merchIds, onClick = onClickMerch)
                                    HorizontalDivider()
                                }
                            }
                        }
                    }

                    PrimaryVerticalScrollbar(listState)
                }
            }
        }
    }

    data class DayChange(
        val date: LocalDate,
        val merchIds: List<String>,
    )
}
