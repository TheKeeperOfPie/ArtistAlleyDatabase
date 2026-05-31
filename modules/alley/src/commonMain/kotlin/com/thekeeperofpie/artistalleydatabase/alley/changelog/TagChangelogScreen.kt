package com.thekeeperofpie.artistalleydatabase.alley.changelog

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_changelog_show_only_confirmed_tags
import artistalleydatabase.modules.alley.generated.resources.alley_changelog_title
import com.composables.core.ScrollArea
import com.composables.core.rememberScrollAreaState
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.GetSeriesTitles
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeExpo2026Changelog
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.ui.PrimaryVerticalScrollbar
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

object TagChangelogScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyGraph,
        dataYear: DataYear,
        series: String?,
        merch: String?,
        onClickBack: () -> Unit,
        onClickArtist: (ArtistEntryAnimeExpo2026Changelog) -> Unit,
        onClickStampRally: (StampRallyChangelogEntry) -> Unit,
        onClickSeries: (String) -> Unit,
        onClickMerch: (String) -> Unit,
        onClickArtistImage: (ArtistEntryAnimeExpo2026Changelog, CatalogImage) -> Unit,
        onClickStampRallyImage: (StampRallyChangelogEntry, CatalogImage) -> Unit,
        viewModel: TagChangelogViewModel = viewModel {
            graph.tagChangelogViewModelFactory.create(
                dataYear = dataYear,
                series = series,
                merch = merch,
                savedStateHandle = createSavedStateHandle(),
            )
        },
    ) {
        val changes by viewModel.changes.collectAsStateWithLifecycle()
        val seriesTitles by viewModel.seriesEntryCache.series.collectAsStateWithLifecycle()
        var showOnlyConfirmedTags by viewModel.showOnlyConfirmedTags.collectAsMutableStateWithLifecycle()
        TagChangelogScreen(
            changes = { changes },
            seriesTitles = { seriesTitles },
            seriesImage = viewModel::seriesImage,
            showOnlyConfirmedTags = { showOnlyConfirmedTags },
            onChangeShowOnlyConfirmedTags = { showOnlyConfirmedTags = it },
            onClickBack = onClickBack,
            onClickArtist = onClickArtist,
            onClickStampRally = onClickStampRally,
            onClickSeries = onClickSeries,
            onClickMerch = onClickMerch,
            onClickArtistImage = onClickArtistImage,
            onClickStampRallyImage = onClickStampRallyImage,
        )
    }

    @Composable
    operator fun invoke(
        changes: () -> List<DayChange>,
        seriesTitles: () -> Map<String, GetSeriesTitles>,
        seriesImage: (seriesId: String) -> String?,
        showOnlyConfirmedTags: () -> Boolean,
        onChangeShowOnlyConfirmedTags: (Boolean) -> Unit,
        onClickBack: () -> Unit,
        onClickArtist: (ArtistEntryAnimeExpo2026Changelog) -> Unit,
        onClickStampRally: (StampRallyChangelogEntry) -> Unit,
        onClickSeries: (String) -> Unit,
        onClickMerch: (String) -> Unit,
        onClickArtistImage: (ArtistEntryAnimeExpo2026Changelog, CatalogImage) -> Unit,
        onClickStampRallyImage: (StampRallyChangelogEntry, CatalogImage) -> Unit,
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
                        item("changelogFilterHeader") {
                            FilterHeader(
                                showOnlyConfirmedTags = { showOnlyConfirmedTags() },
                                onChangeShowOnlyConfirmedTags = onChangeShowOnlyConfirmedTags,
                            )
                        }

                        changes().forEach {
                            item(key = listOf("header", it.date), contentType = "header") {
                                ChangelogDayHeader(it.date)
                            }
                            artistChangelogDay(
                                date = it.date,
                                added = it.addedArtists,
                                updated = it.updatedArtists,
                                seriesTitles = seriesTitles,
                                onClickArtist = onClickArtist,
                                onClickSeries = onClickSeries,
                                onClickMerch = onClickMerch,
                                onClickImage = onClickArtistImage,
                            )
                            stampRallyChangelogDay(
                                date = it.date,
                                added = it.addedRallies,
                                updated = it.updatedRallies,
                                seriesTitles = seriesTitles,
                                seriesImage = seriesImage,
                                onClickStampRally = onClickStampRally,
                                onClickSeries = onClickSeries,
                                onClickMerch = onClickMerch,
                                onClickImage = onClickStampRallyImage,
                            )
                        }
                    }

                    PrimaryVerticalScrollbar(listState)
                }
            }
        }
    }

    @Composable
    fun FilterHeader(
        showOnlyConfirmedTags: () -> Boolean,
        onChangeShowOnlyConfirmedTags: (Boolean) -> Unit,
    ) {
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
            Spacer(Modifier.width(16.dp))
            FilterChip(
                selected = showOnlyConfirmedTags(),
                label = {
                    Text(stringResource(Res.string.alley_changelog_show_only_confirmed_tags))
                },
                onClick = { onChangeShowOnlyConfirmedTags(!showOnlyConfirmedTags()) },
            )
            Spacer(Modifier.width(16.dp))
        }
    }

    data class DayChange(
        val date: LocalDate,
        val addedArtists: List<ArtistEntryAnimeExpo2026Changelog>,
        val updatedArtists: List<ArtistEntryAnimeExpo2026Changelog>,
        val addedRallies: List<StampRallyChangelogEntry>,
        val updatedRallies: List<StampRallyChangelogEntry>,
    )
}
