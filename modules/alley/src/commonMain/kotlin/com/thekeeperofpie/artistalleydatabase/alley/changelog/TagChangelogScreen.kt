package com.thekeeperofpie.artistalleydatabase.alley.changelog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
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
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_only_confirmed_tags
import artistalleydatabase.modules.alley.generated.resources.alley_tag_changelog_title
import com.composables.core.ScrollArea
import com.composables.core.rememberScrollAreaState
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.GetSeriesTitles
import com.thekeeperofpie.artistalleydatabase.alley.fullName
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.series.name
import com.thekeeperofpie.artistalleydatabase.alley.tags.MerchRow
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesRow
import com.thekeeperofpie.artistalleydatabase.alley.ui.PrimaryVerticalScrollbar
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortAndFilterComposables
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

object TagChangelogScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyGraph,
        dataYear: DataYear,
        seriesId: String?,
        merchId: String?,
        onClickBack: () -> Unit,
        onClickArtist: (ArtistChangelogEntry) -> Unit,
        onClickStampRally: (StampRallyChangelogEntry) -> Unit,
        onClickSeries: (String) -> Unit,
        onClickMerch: (String) -> Unit,
        onClickArtistImage: (ArtistChangelogEntry, CatalogImage) -> Unit,
        onClickStampRallyImage: (StampRallyChangelogEntry, CatalogImage) -> Unit,
        viewModel: TagChangelogViewModel = viewModel {
            graph.tagChangelogViewModelFactory.create(
                dataYear = dataYear,
                seriesId = seriesId,
                merchId = merchId,
                savedStateHandle = createSavedStateHandle(),
            )
        },
    ) {
        val changes by viewModel.changes.collectAsStateWithLifecycle()
        val seriesTitles by viewModel.seriesEntryCache.series.collectAsStateWithLifecycle()
        var showOnlyConfirmedTags by viewModel.showOnlyConfirmedTags.collectAsMutableStateWithLifecycle()
        val series by viewModel.series.collectAsStateWithLifecycle()
        val merch by viewModel.merch.collectAsStateWithLifecycle()
        TagChangelogScreen(
            dataYear = dataYear,
            changes = { changes },
            series = { series },
            merch = { merch },
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
            onSeriesFavoriteToggle = viewModel::onSeriesFavoriteToggle,
            onMerchFavoriteToggle = viewModel::onMerchFavoriteToggle,
        )
    }

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        changes: () -> List<DayChange>,
        seriesTitles: () -> Map<String, GetSeriesTitles>,
        seriesImage: (seriesId: String) -> String?,
        series: () -> SeriesWithUserData?,
        merch: () -> MerchWithUserData?,
        showOnlyConfirmedTags: () -> Boolean,
        onChangeShowOnlyConfirmedTags: (Boolean) -> Unit,
        onClickBack: () -> Unit,
        onClickArtist: (ArtistChangelogEntry) -> Unit,
        onClickStampRally: (StampRallyChangelogEntry) -> Unit,
        onClickSeries: (String) -> Unit,
        onClickMerch: (String) -> Unit,
        onClickArtistImage: (ArtistChangelogEntry, CatalogImage) -> Unit,
        onClickStampRallyImage: (StampRallyChangelogEntry, CatalogImage) -> Unit,
        onSeriesFavoriteToggle: (SeriesWithUserData, Boolean) -> Unit,
        onMerchFavoriteToggle: (MerchWithUserData, Boolean) -> Unit,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = { ArrowBackIconButton(onClickBack) },
                    title = {
                        val name = series()?.series?.name(LocalLanguageOptionMedia.current)
                            ?: merch()?.merch?.name.orEmpty()
                        Text(stringResource(Res.string.alley_tag_changelog_title, name))
                    },
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
                            SeriesHeader(
                                year = dataYear,
                                series = series,
                                seriesImage = { series()?.let { seriesImage(it.series.id) } },
                                showOnlyConfirmedTags = { showOnlyConfirmedTags() },
                                onChangeShowOnlyConfirmedTags = onChangeShowOnlyConfirmedTags,
                                onFavoriteToggle = onSeriesFavoriteToggle,
                            )
                            MerchHeader(
                                year = dataYear,
                                merch = merch,
                                showOnlyConfirmedTags = { showOnlyConfirmedTags() },
                                onChangeShowOnlyConfirmedTags = onChangeShowOnlyConfirmedTags,
                                onFavoriteToggle = onMerchFavoriteToggle,
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
    private fun SeriesHeader(
        year: DataYear,
        series: () -> SeriesWithUserData?,
        seriesImage: () -> String?,
        showOnlyConfirmedTags: () -> Boolean,
        onChangeShowOnlyConfirmedTags: (Boolean) -> Unit,
        onFavoriteToggle: (SeriesWithUserData, Boolean) -> Unit,
    ) {
        val series = series() ?: return
        Header(
            year = year,
            showOnlyConfirmedTags = showOnlyConfirmedTags,
            onChangeShowOnlyConfirmedTags = onChangeShowOnlyConfirmedTags,
        ) {
            SeriesRow(
                data = series,
                image = seriesImage,
                textStyle = LocalTextStyle.current,
                showAllTitles = true,
                showNotes = true,
                onFavoriteToggle = {
                    onFavoriteToggle(series, it)
                },
            )
        }
    }

    @Composable
    private fun MerchHeader(
        year: DataYear,
        merch: () -> MerchWithUserData?,
        showOnlyConfirmedTags: () -> Boolean,
        onChangeShowOnlyConfirmedTags: (Boolean) -> Unit,
        onFavoriteToggle: (MerchWithUserData, Boolean) -> Unit,
    ) {
        val merch = merch() ?: return
        Header(
            year = year,
            showOnlyConfirmedTags = showOnlyConfirmedTags,
            onChangeShowOnlyConfirmedTags = onChangeShowOnlyConfirmedTags,
        ) {
            MerchRow(
                data = merch,
                onFavoriteToggle = {
                    onFavoriteToggle(merch, it)
                },
            )
        }
    }

    @Composable
    private fun Header(
        year: DataYear,
        showOnlyConfirmedTags: () -> Boolean,
        onChangeShowOnlyConfirmedTags: (Boolean) -> Unit,
        content: @Composable () -> Unit,
    ) {
        Column {
            Card {
                content()
                HorizontalDivider()
                SortAndFilterComposables.SwitchRow(
                    title = Res.string.alley_filter_show_only_confirmed_tags,
                    enabled = showOnlyConfirmedTags,
                    onEnabledChanged = onChangeShowOnlyConfirmedTags,
                    showDivider = false,
                )
            }
            Text(
                text = stringResource(year.fullName),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }

    data class DayChange(
        val date: LocalDate,
        val addedArtists: List<ArtistChangelogEntry>,
        val updatedArtists: List<ArtistChangelogEntry>,
        val addedRallies: List<StampRallyChangelogEntry>,
        val updatedRallies: List<StampRallyChangelogEntry>,
    )
}
