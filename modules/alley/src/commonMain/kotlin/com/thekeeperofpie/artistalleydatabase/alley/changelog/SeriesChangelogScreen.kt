package com.thekeeperofpie.artistalleydatabase.alley.changelog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.thekeeperofpie.artistalleydatabase.alley.GetSeriesTitles
import com.thekeeperofpie.artistalleydatabase.alley.series.name
import com.thekeeperofpie.artistalleydatabase.alley.tags.SmallSeriesCard
import com.thekeeperofpie.artistalleydatabase.alley.ui.PrimaryVerticalScrollbar
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

object SeriesChangelogScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyGraph,
        onClickBack: () -> Unit,
        onClickSeries: (String) -> Unit,
        viewModel: SeriesChangelogViewModel = viewModel {
            graph.seriesChangelogViewModelFactory()
        },
    ) {
        val changes by viewModel.changes.collectAsStateWithLifecycle()
        SeriesChangelogScreen(
            changes = { changes },
            seriesImage = viewModel::seriesImage,
            onClickBack = onClickBack,
            onClickSeries = onClickSeries,
        )
    }

    @Composable
    operator fun invoke(
        changes: () -> List<DayChange>,
        seriesImage: (seriesId: String) -> String?,
        onClickBack: () -> Unit,
        onClickSeries: (String) -> Unit,
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
                    val languageOption = LocalLanguageOptionMedia.current
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(bottom = 200.dp),
                        modifier = Modifier.widthIn(max = 960.dp)
                    ) {
                        changes().forEach {
                            item(key = listOf("header", it.date), contentType = "header") {
                                ChangelogDayHeader(it.date)
                            }

                            item(key = listOf("seriesIds", it.date), contentType = "seriesIds") {
                                Column {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 8.dp
                                        )
                                    ) {
                                        it.seriesIds.forEach {
                                            SmallSeriesCard(
                                                seriesId = it.id,
                                                seriesTitle = it.name(languageOption),
                                                image = seriesImage(it.id),
                                                onClick = { onClickSeries(it.id) },
                                                modifier = Modifier.width(120.dp)
                                            )
                                        }
                                    }
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
        val seriesIds: List<GetSeriesTitles>,
    )
}
